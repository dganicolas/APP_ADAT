package com.es.aplicacion.repository;

import com.es.aplicacion.model.Tarea
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository;
import java.util.*

@Repository
interface TareaRepository : MongoRepository<Tarea, String> {

    fun findBy_id(nombre: String) : Optional<Tarea>
    fun findByAutor(autor: String): List<Tarea>
}