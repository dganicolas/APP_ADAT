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

        //valido que tod0 me venga correcto, en caso negativo lanzo un error
        if (tarea.nombre.isBlank()) {
            throw BadRequestException("el nombre de la tarea no puede estar vacio")
        }
        if (tarea.descripcion.isBlank()) {
            throw BadRequestException("la descripcion no puede estar vacio")
        }
        if (usuarioRepository.findByUsername(tarea.autor).isEmpty) {
            throw BadRequestException("el autor no existe")
        }

        //guardo la tarea por defecto en no completada
        tareaRepository.save(
            Tarea(
                nombre = tarea.nombre,
                descripcion = tarea.descripcion,
                estado = false,
                autor = tarea.autor
            )
        )
        //devuelvo un 200 como correcto
        return ResponseEntity.ok(tarea)
    }

    //actualizo el estado de la tarea si esta o no completa
    fun actualizarEstado(authentication: Authentication, id: String): ResponseEntity<Map<String,String>> {
        //intento buscar el id de la tarea
        val tarea = tareaRepository.findBy_id(id).getOrNull()
        //si la tarea no existe lanzo una excepcion
        if (tarea == null) {
            throw NotFoundException("la tarea no existe")
        } else {
            //si el usuario tiene los privilegios suficiente entonces
            if (authentication.authorities.any { it.authority == "ROLE_ADMIN" } || authentication.name == tarea.autor) {
                //marco la tarea com completada o no completada depende como me llegue
                //si llega false, se marca true y viceversa
                tarea.estado = !tarea.estado
                //guardo la tarea con el estado actualizado
                tareaRepository.save(tarea)
                //devuelvo un 200 como correcto y un mensaje informativo
                return ResponseEntity.ok(mapOf("mensaje" to "tarea ${tarea.nombre} ha sido marcada como ${if (tarea.estado) "completada" else "incompleta"}"))
            }
            //si el usuario no tiene privilegios lanzo un error
            throw UnauthorizedException("no tiene autorizacion para desmarcar la tarea como completada")
        }
    }

    //retorno una tarea pòr su id
    fun tenerTareaPorid(authentication: Authentication, id: String): ResponseEntity<Tarea> {
        //intento encontrar la tarea
        val tarea = tareaRepository.findBy_id(id).getOrNull() ?: throw BadRequestException("tarea no existe")
        //miro si tiene los privilegios suficiente
        if (authentication.authorities.any { it.authority == "ROLE_ADMIN" } || authentication.name == tarea.autor) {
            //retorno su tarea
            return ResponseEntity.ok(tarea)
        } else {
            //lanzo error si el usuario intenta buscar una tarea que no es suya
            throw UnauthorizedException("no tiene autorizacion para borrar la tarea")
        }
    }

    fun listarTareas(authentication: Authentication): ResponseEntity<MutableList<Tarea>> {
        //si el usuario tiene rol de admin retorno todas las tareas
        if (authentication.authorities.any { it.authority == "ROLE_ADMIN" }) {
            return ResponseEntity.ok(tareaRepository.findAll())
        }
        //si no retorno solo las tareas del usuario
        else {
            val lista = tareaRepository.findByAutor(authentication.name).toMutableList()
            return ResponseEntity.ok(lista)
        }

    }

    fun eliminarTarea(id: String, authentication: Authentication): ResponseEntity<Map<String,String>> {
        //recupero la tarea de la db
        val tarea = tareaRepository.findBy_id(id).getOrNull() ?: throw NotFoundException("la tarea no existe")
        //miro si tiene los privilegios suficiente
        if (tarea.autor == authentication.name || authentication.authorities.any { it.authority == "ROLE_ADMIN" }) {
            //elimino la tarea y retorno un mensasje informativo
            tareaRepository.delete(tarea)
            return ResponseEntity.ok(mapOf("mensaje" to "la tarea ${tarea.nombre} ha sido eliminada"))
        }
        throw UnauthorizedException("no tiene autorizacion para borrar la tarea")
    }

    fun actualizarTarea(tarea: Tarea, authentication: Authentication): ResponseEntity<Map<String, String>> {

        //reviso si esta la tarea
        var tareaExiste = tareaRepository.findBy_id(tarea._id?:"").getOrNull() ?: throw NotFoundException("la tarea no existe")
        if (usuarioRepository.findByUsername(tarea.autor).isEmpty) {
            throw BadRequestException("el autor no existe")
        }
        //reviso si los campos me vienen relleno
        if(tarea.descripcion.isEmpty()|| tarea.nombre.isEmpty()){
            throw BadRequestException("campos incompletos")
        }
        //actualizo con los nuevos campos
        tareaExiste.nombre = tarea.nombre
        tareaExiste.estado = tarea.estado
        tareaExiste.descripcion =tarea.descripcion
        tareaExiste.autor =tarea.autor
        //actualizo en la base de datos
        tareaRepository.save(tareaExiste)
        //devuelvo un 200 con un mensaje informativo
        return ResponseEntity.ok(mapOf("mensaje" to "la tarea ${tareaExiste.nombre} ha sido actualizada"))
    }

}