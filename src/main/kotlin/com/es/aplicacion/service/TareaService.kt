package com.es.aplicacion.service

import com.es.aplicacion.dto.CreateTaskDto
import com.es.aplicacion.error.exception.BadRequestException
import com.es.aplicacion.error.exception.NotFoundException
import com.es.aplicacion.error.exception.UnauthorizedException
import com.es.aplicacion.model.Tarea
import com.es.aplicacion.repository.TareaRepository
import com.es.aplicacion.repository.UsuarioRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class TareaService() {

    @Autowired
    private lateinit var tareaRepository: TareaRepository

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    fun crearTarea(tarea: CreateTaskDto): ResponseEntity<CreateTaskDto> {

        if (tarea.nombre.isBlank()) {
            throw BadRequestException("el nombre de la tarea no puede estar vacio")
        }

        if (tarea.descripcion.isBlank()) {
            throw BadRequestException("la descripcion no puede estar vacio")
        }
        if (usuarioRepository.findByUsername(tarea.autor).isEmpty) {
            throw BadRequestException("el autor no existe")
        }

        tareaRepository.save(
            Tarea(
                nombre = tarea.nombre,
                descripcion = tarea.descripcion,
                estado = false,
                autor = tarea.autor
            )
        )
        return ResponseEntity.ok(tarea)
    }

    fun actualizarEstado(authentication: Authentication, id: String): ResponseEntity<Map<String,String>> {
        val tarea = tareaRepository.findBy_id(id).getOrNull()
        if (tarea == null) {
            throw NotFoundException("la tarea no existe")
        } else {
            if (authentication.authorities.any { it.authority == "ROLE_ADMIN" } || authentication.name == tarea.autor) {
                tarea.estado = !tarea.estado
                tareaRepository.save(tarea)
                return ResponseEntity.ok(mapOf("mensaje" to "tarea ${tarea.nombre} ha sido marcada como ${if (tarea.estado) "completada" else "incompleta"}"))
            }
            throw UnauthorizedException("no tiene autorizacion para desmarcar la tarea como completada")
        }
    }

    fun tenerTareaPorid(authentication: Authentication, id: String): ResponseEntity<Tarea> {
        val tarea = tareaRepository.findBy_id(id).getOrNull() ?: throw BadRequestException("tarea no existe")
        if (authentication.authorities.any { it.authority == "ROLE_ADMIN" } || authentication.name == tarea.autor) {
            return ResponseEntity.ok(tarea)
        } else {
            throw UnauthorizedException("no tiene autorizacion para borrar la tarea")
        }
    }

    fun listarTareas(authentication: Authentication): ResponseEntity<MutableList<Tarea>> {
        if (authentication.authorities.any { it.authority == "ROLE_ADMIN" }) {
            return ResponseEntity.ok(tareaRepository.findAll())
        } else {
            val lista = tareaRepository.findByAutor(authentication.name).toMutableList()
            return ResponseEntity.ok(lista)
        }

    }

    fun eliminarTarea(id: String, authentication: Authentication): ResponseEntity<Map<String,String>> {
        val tarea = tareaRepository.findBy_id(id).getOrNull() ?: throw NotFoundException("la tarea no existe")
        if (tarea.autor == authentication.name || authentication.authorities.any { it.authority == "ROLE_ADMIN" }) {
            tareaRepository.delete(tarea)
            return ResponseEntity.ok(mapOf("mensaje" to "la tarea ${tarea.nombre} ha sido eliminada"))
        }
        throw UnauthorizedException("no tiene autorizacion para borrar la tarea")
    }

    fun actualizarTarea(tarea: Tarea, authentication: Authentication): ResponseEntity<Map<String, String>> {
        var tareaExiste = tareaRepository.findBy_id(tarea._id?:"").getOrNull() ?: throw NotFoundException("la tarea no existe")
        tareaExiste.nombre = tarea.nombre
        tareaExiste.estado = tarea.estado
        tareaExiste.descripcion =tarea.descripcion
        tareaExiste.autor =tarea.autor
        tareaRepository.save(tareaExiste)
        return ResponseEntity.ok(mapOf("mensaje" to "la tarea ${tareaExiste.nombre} ha sido actualizada"))
    }

}