package com.es.aplicacion.service

import com.es.aplicacion.dto.UsuarioActualizarDto
import com.es.aplicacion.dto.UsuarioDTO
import com.es.aplicacion.dto.UsuarioRegisterDTO
import com.es.aplicacion.error.exception.BadRequestException
import com.es.aplicacion.error.exception.NotFoundException
import com.es.aplicacion.error.exception.UnauthorizedException
import com.es.aplicacion.model.Direccion
import com.es.aplicacion.model.Usuario
import com.es.aplicacion.repository.UsuarioRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class UsuarioService : UserDetailsService {

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var apiService: ExternalApiService

    override fun loadUserByUsername(username: String?): UserDetails {
        var usuario: Usuario = usuarioRepository
            .findByUsername(username!!)
            .orElseThrow {
                UnauthorizedException("$username no existente")
            }

        return User.builder()
            .username(usuario.username)
            .password(usuario.password)
            .roles(usuario.roles)
            .build()
    }

    fun insertUser(usuarioInsertadoDTO: UsuarioRegisterDTO): UsuarioDTO? {

        if (usuarioInsertadoDTO.username.length > 12 || usuarioInsertadoDTO.username.length < 3) {
            throw BadRequestException(" el username debera tener 3 caracteres como minimo y 12 caracteres como maximo.")
        }

        if (usuarioInsertadoDTO.passwordRepeat != usuarioInsertadoDTO.password) {
            throw BadRequestException("las contraseñas no son iguales")
        }
        if (usuarioInsertadoDTO.rol != null && usuarioInsertadoDTO.rol != "USER" && usuarioInsertadoDTO.rol != "ADMIN") {
            throw BadRequestException("El usuario tiene un rol desconocido")
        }
        if (usuarioRepository.findByUsername(usuarioInsertadoDTO.username).isPresent) {
            throw BadRequestException("username ya esta registrado en la base de datos")
        }
        val Provincias = apiService.obtenerDatosDesdeApi()
        val provinciaEscogida = Provincias?.data?.stream()?.filter {
            it.PRO == usuarioInsertadoDTO.direccion.provincia.uppercase()
        }?.findFirst()?.orElseThrow {
            NotFoundException("Provincia")
        }
        usuarioInsertadoDTO.password = passwordEncoder.encode(usuarioInsertadoDTO.password)

        usuarioRepository.insert(
            Usuario(
                null,
                usuarioInsertadoDTO.username,
                usuarioInsertadoDTO.password,
                usuarioInsertadoDTO.email,
                usuarioInsertadoDTO.rol!!,
                usuarioInsertadoDTO.direccion
            )
        )

        return UsuarioDTO(
            usuarioInsertadoDTO.username,
            usuarioInsertadoDTO.email,
            usuarioInsertadoDTO.rol
        )

    }

    fun eliminarUsuario(username: String): ResponseEntity<String> {
        val usuario = usuarioRepository.findByUsername(username).getOrNull()
        if (usuario != null) {
            usuarioRepository.delete(usuario)
            return ResponseEntity.ok("Usuario $username eliminado correctamente")
        } else {
            throw NotFoundException("El usuario no existe")
        }

    }

    fun actualizarUsuario(username: String, nuevoUsuario: UsuarioActualizarDto): ResponseEntity<String> {
        val usuario = usuarioRepository.findByUsername(username).getOrNull()
        if (usuario != null) {
            usuario.email = nuevoUsuario.email
            usuario.password = passwordEncoder.encode(nuevoUsuario.password)
            usuarioRepository.save(usuario)
            return ResponseEntity.ok("Usuario $username actualizado correctamente")
        } else {
            throw NotFoundException("El usuario no existe")
        }
    }

    fun listarUsuarios(): ResponseEntity<MutableList<Usuario>> {
        val listaUsuarios = usuarioRepository.findAll()
        if (listaUsuarios.isEmpty()) {
            throw NotFoundException("no hay usuarios registrados")
        }
        return ResponseEntity.ok(listaUsuarios)
    }

    fun popularBaseDeDatos(): ResponseEntity<String> {
        if (usuarioRepository.findByUsername("usuario1").isPresent){
            throw BadRequestException(" base de datos ya poblada")
        }
        val usuario1 = Usuario(
            _id = null,
            username = "usuario1",
            password = "password123",
            email = "usuario1@example.com",
            roles = "USER",
            direccion = Direccion("Avenida Principal", "123", "28001", "Madrid", "Centro", "COMUNIDAD DE MADRID")
        )
        val usuario2 = Usuario(
            _id = null,
            username = "usuario2",
            password = "password456",
            email = "usuario2@example.com",
            roles = "USER",
            direccion = Direccion("Avenida Principal", "123", "28001", "Cataluña", "Centro", "Barcelona")
        )
        usuarioRepository.save(usuario1)
        usuarioRepository.save(usuario2)
        return ResponseEntity.ok("bases de datos poblada con la informacion")
    }
}