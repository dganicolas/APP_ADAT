package com.es.aplicacion.model

import org.bson.codecs.pojo.annotations.BsonId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

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
    @BsonId @Id
    val _id: String?=null,
    var nombre: String,
    var descripcion:String,
    var estado:Boolean,
    var autor:String,
)