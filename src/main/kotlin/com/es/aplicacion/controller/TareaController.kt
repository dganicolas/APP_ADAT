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

    //endpoint que crea una tarea nuevaen la DB
    @PostMapping("/crear")
    fun crearTarea(authentication: Authentication,@RequestBody tarea: CreateTaskDto): ResponseEntity<CreateTaskDto> {
        print("el usuario ${authentication.name} ha creado una tarea")
        //revisosi tiene los privilegios necesarios
        if (authentication.name == tarea.autor ||
            authentication.authorities.any { it.authority == "ROLE_ADMIN" }
        ) {
            return tareaService.crearTarea(tarea)
        }
        throw UnauthorizedException("no tiene autorizacion para esa accion")
    }

    @PutMapping("/actualizartarea")
    fun actualizarTarea(@RequestBody tarea:Tarea,authentication: Authentication): ResponseEntity<Map<String, String>> {
        print("el usuario ${authentication.name} ha actualizado una tarea")
        return tareaService.actualizarTarea(tarea,authentication)
    }
    @PutMapping("/actualizarEstadoTarea/{id}")
    fun actualizarEstadoTarea(@PathVariable id:String,authentication: Authentication): ResponseEntity<Map<String,String>> {
        print("el usuario ${authentication.name} ha actualizado el estado de una tarea")
        if(id.isBlank()){
            throw BadRequestException("el id de la tarea debe estar presente")
        }
        return tareaService.actualizarEstado(authentication,id)

    }

    @GetMapping("/tareaporid/{id}")
    fun tareaporid(@PathVariable id:String,authentication: Authentication): ResponseEntity<Tarea> {
        print("el usuario ${authentication.name} ha  consultado una tarea")
        return tareaService.tenerTareaPorid(authentication,id)
    }
    @GetMapping("/listarTodasLasTareas")
    fun listarTodasLasTareas(authentication: Authentication): ResponseEntity<MutableList<Tarea>> {
        print("el usuario ${authentication.name} ha listado todas sus tareas")
        return tareaService.listarTareas(authentication)
    }

    @DeleteMapping("/eliminar/{id}")
    fun eliminartarea(@PathVariable id:String,authentication: Authentication): ResponseEntity<Map<String,String>> {
        print("el usuario ${authentication.name} ha eliminado una tarea")
        if(id.isBlank()){
            throw BadRequestException("el id de la tarea debe estar presente")
        }
        return tareaService.eliminarTarea(id,authentication)
    }

}