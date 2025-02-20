package com.es.aplicacion.controller

import com.es.aplicacion.dto.CreateTaskDto
import com.es.aplicacion.error.exception.BadRequestException
import com.es.aplicacion.error.exception.UnauthorizedException
import com.es.aplicacion.model.Tarea
import com.es.aplicacion.service.TareaService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/tareas")
class TareaController {

    @Autowired
    private lateinit var tareaService: TareaService

    @PostMapping("/crear")
    fun crearTarea(authentication: Authentication,@RequestBody tarea: CreateTaskDto): ResponseEntity<String> {
        if (authentication.name == tarea.autor ||
            authentication.authorities.any { it.authority == "ROLE_ADMIN" }
        ) {
            return tareaService.crearTarea(tarea)
        }
        throw UnauthorizedException("no tiene autorizacion para esa accion")
    }

    @PutMapping("/actualizarEstadoTarea/{nombre}")
    fun actualizarEstadoTarea(@PathVariable nombre:String,authentication: Authentication): ResponseEntity<String> {
        if(nombre.isBlank()){
            throw BadRequestException("el nombre de la tarea debe estar presente")
        }
        return tareaService.actualizarTarea(authentication,nombre)

    }

    @GetMapping("/listarTodasLasTareas")
    fun listarTodasLasTareas(): ResponseEntity<MutableList<Tarea>> {
        return tareaService.listarTareas()
    }

    @GetMapping("/listarTareasPorUsuarios/{nombre}")
    fun listarTareasPorAutor(@PathVariable nombre:String): ResponseEntity<List<Tarea>> {
        if(nombre.isBlank()){
            throw BadRequestException("el nombre de la tarea debe estar presente")
        }
        return tareaService.listarTareasPorAutor(nombre)
    }

    @DeleteMapping("/eliminar/{nombre}")
    fun eliminartarea(@PathVariable nombre:String,authentication: Authentication): ResponseEntity<String> {
        if(nombre.isBlank()){
            throw BadRequestException("el nombre de la tarea debe estar presente")
        }
        return tareaService.eliminarTarea(nombre,authentication)
    }

    @PutMapping("/encargase/{nombre}/{encargado}")
    fun encargarseDeLaTarea(@PathVariable nombre:String,
                            @PathVariable encargado:String,
                            authentication: Authentication): ResponseEntity<String> {
        if(nombre.isBlank()){
            throw BadRequestException("el nombre de la tarea debe estar presente")
        }
        return tareaService.encargarseDeLaTarea(nombre,encargado,authentication)
    }

    @PostMapping("/popularbbdd")
    fun popularBaseDeDatos(): ResponseEntity<String> {
        return tareaService.popularBaseDedatos()
    }
}