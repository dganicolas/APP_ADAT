package com.es.aplicacion.service

import com.es.aplicacion.domain.DatosProvincias
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class ExternalApiService(private val webClient: WebClient.Builder) {

    @Value("\${API_KEY}")
    private lateinit var apiKey: String

    fun obtenerDatosDesdeApi(): DatosProvincias? {
        return webClient.build()
            .get()
            .uri("https://apiv1.geoapi.es/provincias?type=JSON&key=$apiKey")
            .retrieve()
            .bodyToMono(DatosProvincias::class.java)
            .block() // ⚠️ Esto bloquea el hilo, usar `subscribe()` en código reactivo
    }

    fun validarMunicipio(cpro: String): Boolean {
        val respuesta = webClient.build()
            .get()
            .uri("https://apiv1.geoapi.es/municipios?CPRO=${cpro}&type=JSON&key=$apiKey")
            .retrieve()
            .bodyToMono(Map::class.java)
            .block()

        return respuesta?.isNotEmpty() ?: false
    }

    fun validarCiudad(ciudad: String): Boolean {
        val response = webClient.build()
            .get()
            .uri("https://apiv1.geoapi.es/municipios?type=JSON&key=$apiKey&NENTSI50=$ciudad")
            .retrieve()
            .bodyToMono(Map::class.java) // Recibimos un mapa genérico
            .block()

        return response?.isNotEmpty() ?: false
    }
}