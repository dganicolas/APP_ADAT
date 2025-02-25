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

    ////Usuario con rol USER
    //    //Ver todas SUS tareas V
    //    //Marcar como hecha una tarea propia
    //    //Eliminar una tarea propia
    //    //Darse de alta A SÍ MISMO una tarea
    //    //  Esto quiere decir que si otro usuario, que no es ADMIN,
    //    //  intenta darle de alta una tarea a otro usuario debería
    //    //  salir una excepción 403 Forbidden
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
    fun listarTareasPorAutor(authentication: Authentication): ResponseEntity<List<Tarea>>{
        return tareaService.listarTareasPorAutor(authentication.name)
    }

    @DeleteMapping("/eliminar/{nombre}")
    fun eliminartarea(@PathVariable nombre:String,authentication: Authentication): ResponseEntity<String> {
        if(nombre.isBlank()){
            throw BadRequestException("el nombre de la tarea debe estar presente")
        }
        return tareaService.eliminarTarea(nombre,authentication)
    }

}