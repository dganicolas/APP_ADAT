package com.es.aplicacion.service

import com.es.aplicacion.dto.UsuarioDTO
import com.es.aplicacion.dto.UsuarioRegisterDTO
import com.es.aplicacion.error.exception.BadRequestException
import com.es.aplicacion.error.exception.NotFoundException
import com.es.aplicacion.error.exception.UnauthorizedException
import com.es.aplicacion.model.Usuario
import com.es.aplicacion.repository.UsuarioRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

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

    fun insertUser(usuarioInsertadoDTO: UsuarioRegisterDTO) : UsuarioDTO? {

        if (usuarioInsertadoDTO.passwordRepeat !=usuarioInsertadoDTO.password){
            throw BadRequestException("las contraseñas no son iguales")
        }
        if (usuarioInsertadoDTO.rol  != null && usuarioInsertadoDTO.rol != "USER" && usuarioInsertadoDTO.rol != "ADMIN"){
            throw BadRequestException("El usuario tiene un rol desconocido")
        }
        if(usuarioRepository.findByUsername(usuarioInsertadoDTO.username).isPresent){
            throw BadRequestException("username ya esta registrado en la base de datos")
        }
        val Provincias = apiService.obtenerDatosDesdeApi()
        val provinciaEscogida = Provincias?.data?.stream()?.filter {
            it.PRO == usuarioInsertadoDTO.direccion.provincia
        }?.findFirst()?.orElseThrow{
            NotFoundException("Provincia")
        }
        usuarioInsertadoDTO.password=passwordEncoder.encode(usuarioInsertadoDTO.password)

        usuarioRepository.insert(Usuario(
            null,
            usuarioInsertadoDTO.username,
            usuarioInsertadoDTO.password,
            usuarioInsertadoDTO.email,
            usuarioInsertadoDTO.rol!!,
            usuarioInsertadoDTO.direccion
        ))

        return UsuarioDTO(
            usuarioInsertadoDTO.username,
            usuarioInsertadoDTO.email,
            usuarioInsertadoDTO.rol
        )

    }
}