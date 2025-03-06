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

    fun crearTarea(tarea: CreateTaskDto): ResponseEntity<String> {

        if (tarea.nombre.isBlank()) {
            throw BadRequestException("el nombre de la tarea no puede estar vacio")
        }

        if (tarea.descripcion.isBlank()) {
            throw BadRequestException("la descripcion no puede estar vacio")
        }
        if(usuarioRepository.findByUsername(tarea.autor).isEmpty){
            throw BadRequestException("el autor no existe")
        }

        tareaRepository.save(
            Tarea(
                _id = (tareaRepository.findAll().maxByOrNull { it._id ?: 0 }?._id?.plus(1)) ?: 0,
                nombre = tarea.nombre,
                descripcion = tarea.descripcion,
                estado = false,
                autor = tarea.autor
            )
        )
        return ResponseEntity.ok("la tarea: ${tarea.nombre} ha sido creada")
    }

    fun actualizarTarea(authentication: Authentication, id: String): ResponseEntity<String> {
        val tarea = tareaRepository.findBy_id(id).getOrNull()
        if (tarea == null) {
            throw NotFoundException("la tarea no existe")
        } else {
            if (!tarea.estado) {
                tarea.estado = true
                tareaRepository.save(tarea)
                return ResponseEntity.ok("tarea ${tarea.nombre} ha sido marcada como completa")
            } else {
                if (authentication.authorities.any { it.authority == "ROLE_ADMIN" }) {
                    tarea.estado = false
                    tareaRepository.save(tarea)
                    return ResponseEntity.ok("tarea ${tarea.nombre} ha sido marcada como incompleta")
                }
                throw UnauthorizedException("no tiene autorizacion para desmarcar la tarea como completada")
            }
        }
    }

    fun listarTareas(authentication: Authentication): ResponseEntity<MutableList<Tarea>> {
        if(authentication.authorities.any { it.authority == "ROLE_ADMIN" }){
            return ResponseEntity.ok(tareaRepository.findAll())
        }else{
            val lista = tareaRepository.findByAutor(authentication.name).toMutableList()
            return ResponseEntity.ok(lista)
        }

    }

    fun eliminarTarea(id: String, authentication: Authentication): ResponseEntity<String> {
        val tarea = tareaRepository.findBy_id(id).getOrNull() ?: throw NotFoundException("la tarea no existe")
        if (tarea.autor == authentication.name || authentication.authorities.any { it.authority == "ROLE_ADMIN" }) {
            tareaRepository.delete(tarea)
            return ResponseEntity.ok("la tarea ${tarea.nombre} ha sido eliminada")
        }
        throw UnauthorizedException("no tiene autorizacion para borrar la tarea")
    }

}