package com.es.aplicacion.service

import com.es.aplicacion.dto.CreateTaskDto
import com.es.aplicacion.error.exception.BadRequestException
import com.es.aplicacion.error.exception.NotFoundException
import com.es.aplicacion.error.exception.UnauthorizedException
import com.es.aplicacion.model.Tarea
import com.es.aplicacion.repository.TareaRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class TareaService() {

    @Autowired
    private lateinit var tareaRepository: TareaRepository

    fun crearTarea(tarea: CreateTaskDto): ResponseEntity<String> {

        if(tarea.nombre. isBlank()){
            throw BadRequestException("el nombre de la tarea no puede estar vacio")
        }

        if(tareaRepository.findByNombre(tarea.nombre).isPresent){
            throw BadRequestException("la tarea ya existe.")
        }

        if(tarea.descripcion. isBlank()){
            throw BadRequestException("la descripcion no puede estar vacio")
        }
        tareaRepository.save(
            Tarea(
                _id = null,
                nombre = tarea.nombre,
                descripcion = tarea.descripcion,
                estado = false,
                autor = tarea.autor,
                encargado = ""
            )
        )
        return ResponseEntity.ok("la tarea: ${tarea.nombre} ha sido creada")
    }

    fun actualizarTarea(authentication: Authentication, nombre: String): ResponseEntity<String> {
        val tarea = tareaRepository.findByNombre(nombre).getOrNull()
        if(tarea == null){
            throw NotFoundException("la tarea no existe")
        }else{
            if(!tarea.estado){
                tarea.estado = true
                tareaRepository.save(tarea)
                return ResponseEntity.ok("tarea ${tarea.nombre} ha sido marcada como completa")
            }else{
                if(authentication.authorities.any { it.authority == "ROLE_ADMIN" }){
                    tarea.estado = false
                    tareaRepository.save(tarea)
                    return ResponseEntity.ok("tarea ${tarea.nombre} ha sido marcada como incompleta")
                }
                throw UnauthorizedException("no tiene autorizacion para desmarcar la tarea como completada")
            }
        }
    }

    fun listarTareas(): ResponseEntity<MutableList<Tarea>> {
        return ResponseEntity.ok(tareaRepository.findAll())
    }

    fun listarTareasPorAutor(nombre: String): ResponseEntity<List<Tarea>> {
        val lista = tareaRepository.findByAutor(nombre)
        return ResponseEntity.ok(lista)
    }

    fun popularBaseDedatos(): ResponseEntity<String> {
        if(tareaRepository.findByNombre("tarea1").isPresent){
            throw BadRequestException("bases de datos ya cargada con datos de pruebas")
        }
        val tarea1 = Tarea(null, "Tarea 1", "Descripción de tarea 1", false, "usuario1", "")
        val tarea2 = Tarea(null, "Tarea 2", "Descripción de tarea 2", false, "usuario1", "")
        val tarea3 = Tarea(null, "Tarea 3", "Descripción de tarea 3", false, "usuario1", "")
        tareaRepository.saveAll(listOf(tarea1, tarea2, tarea3))
        return ResponseEntity.ok("bases de datos poblada con la informacion")
    }
}