package com.es.aplicacion.model

import org.bson.codecs.pojo.annotations.BsonId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.Date

/*
- Tareas:
  - nombre: nombre de la tarea
  - descripcion: descripcion de la tarea
  - estado: true o false, dependiendo si esta realizada o no
  - autor: autor de la tarea
  - encargado: quien se encargara de la tarea
 */
@Document("Tarea")
data class Tarea(
    @Id
    val _id:Long?,
    val nombre: String,
    val descripcion:String,
    var estado:Boolean,
    val autor:String,
)