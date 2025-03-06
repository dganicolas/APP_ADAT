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
import org.intellij.lang.annotations.Pattern
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
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

    fun esEmailValido(email: String): Boolean {
        //reviso si el email es valido
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]{2,}$")
        return emailRegex.matches(email)
    }

    fun insertUser(usuarioInsertadoDTO: UsuarioRegisterDTO): UsuarioDTO? {
        //compruebo que el usuario no meta un nombre inferior a 3 y mayor a 12 de longitud
        if (usuarioInsertadoDTO.username.length > 12 || usuarioInsertadoDTO.username.length < 3) {
            throw BadRequestException(" el username debera tener 3 caracteres como minimo y 12 caracteres como maximo.")
        }

        //compruebo que la contraseña coincida
        if (usuarioInsertadoDTO.passwordRepeat != usuarioInsertadoDTO.password) {
            throw BadRequestException("las contraseñas no son iguales")
        }

        //compruebo que el rol este bien
        if (usuarioInsertadoDTO.rol != null && usuarioInsertadoDTO.rol != "USER" && usuarioInsertadoDTO.rol != "ADMIN") {
            throw BadRequestException("El usuario tiene un rol desconocido")
        }

        //compruebo si el email es valido
        if (!esEmailValido(usuarioInsertadoDTO.email)) {
            throw BadRequestException("el email es invalido")
        }

        //reviso que el username del usuario no exista
        if (usuarioRepository.findByUsername(usuarioInsertadoDTO.username).isPresent) {
            throw BadRequestException("username ya esta registrado en la base de datos")
        }
        //compruebo si la provincia existe en la vida real
        val Provincias = apiService.obtenerDatosDesdeApi()
        val provinciaEscogida = Provincias?.data?.stream()?.filter {
            it.PRO == usuarioInsertadoDTO.direccion.provincia.uppercase()
        }?.findFirst()?.orElseThrow {
            NotFoundException("Provincia no encontrada")
        }
        //hasheo la contraseña
        usuarioInsertadoDTO.password = passwordEncoder.encode(usuarioInsertadoDTO.password)

        //creo el usuario en la DB
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

        //retorno solo el nombre, email y rol al usuario
        return UsuarioDTO(
            usuarioInsertadoDTO.username,
            usuarioInsertadoDTO.email,
            usuarioInsertadoDTO.rol
        )

    }

    fun eliminarUsuario(username: String): ResponseEntity<Map<String,String>> {
        //consigo al usuarioen la db
        val usuario = usuarioRepository.findByUsername(username).getOrNull()
        if (usuario != null) {
            //si existe lo elimino y retorno mensaje informativo
            usuarioRepository.delete(usuario)
            return ResponseEntity.ok(mapOf("mensaje" to "Usuario $username eliminado correctamente"))
        } else {
            throw NotFoundException("El usuario no existe")
        }

    }

    fun actualizarUsuario(username: String, nuevoUsuario: UsuarioActualizarDto): ResponseEntity<Map<String,String>> {
        val usuario = usuarioRepository.findByUsername(username).getOrNull()
        if (usuario != null) {
            usuario.email = nuevoUsuario.email
            usuario.password = passwordEncoder.encode(nuevoUsuario.password)
            usuarioRepository.save(usuario)
            return ResponseEntity.ok(mapOf("mensaje" to "Usuario $username actualizado correctamente"))
        } else {
            throw NotFoundException("El usuario no existe")
        }
    }

    fun listarUsuarios(): ResponseEntity<List<UsuarioDTO>> {
        val listaUsuarios = emptyList<UsuarioDTO>().toMutableList()
        usuarioRepository.findAll().forEach {
            listaUsuarios.add(
                UsuarioDTO(
                    username = it.username,
                    email = it.email,
                    rol = it.roles
                )
            )
        }
        if (listaUsuarios.isEmpty()) {
            throw NotFoundException("no hay usuarios registrados")
        }
        return ResponseEntity.ok(listaUsuarios.toList())
    }

    fun esAdmin(authentication: Authentication): ResponseEntity<Boolean> {
        val esAdmin = authentication.authorities.any { it.authority == "ROLE_ADMIN" }
        return ResponseEntity.ok(esAdmin)
    }

    fun obtenerEmail(name: String?): ResponseEntity<Map<String, String>> {
        val usuario=usuarioRepository.findByUsername(name!!).getOrNull()?:throw NotFoundException("usuario no encontrado")
        return ResponseEntity.ok(mapOf("mensaje" to usuario.email))
    }

}