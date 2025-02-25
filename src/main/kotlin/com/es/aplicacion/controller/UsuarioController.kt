package com.es.aplicacion.controller

import com.es.aplicacion.dto.LoginUsuarioDTO
import com.es.aplicacion.dto.UsuarioActualizarDto
import com.es.aplicacion.dto.UsuarioDTO
import com.es.aplicacion.dto.UsuarioRegisterDTO
import com.es.aplicacion.error.exception.UnauthorizedException
import com.es.aplicacion.model.Usuario
import com.es.aplicacion.service.TokenService
import com.es.aplicacion.service.UsuarioService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/usuarios")
class UsuarioController {

    @Autowired
    private lateinit var authenticationManager: AuthenticationManager
    @Autowired
    private lateinit var tokenService: TokenService
    @Autowired
    private lateinit var usuarioService: UsuarioService

    @PostMapping("/registrarse")
    fun insert(
        httpRequest: HttpServletRequest,
        @RequestBody usuarioRegisterDTO: UsuarioRegisterDTO
    ) : ResponseEntity<UsuarioDTO>{

        if(usuarioRegisterDTO.username.isBlank() && usuarioRegisterDTO.password.isBlank()){
            throw UnauthorizedException("los campos usuarios y contraseñas deben de estar rellenos")
        }
        val usuario = usuarioService.insertUser(usuarioRegisterDTO)

        return ResponseEntity(usuario, HttpStatus.CREATED)

    }

    @PostMapping("/acceder")
    fun login(@RequestBody usuario: LoginUsuarioDTO): ResponseEntity<Map<String, String>> {

        val authentication: Authentication
        try {
            authentication = authenticationManager.authenticate(UsernamePasswordAuthenticationToken(usuario.username, usuario.password))
        } catch (e: AuthenticationException) {
            throw UnauthorizedException("Credenciales incorrectas")
        }

        // SI PASAMOS LA AUTENTICACIÓN, SIGNIFICA QUE ESTAMOS BIEN AUTENTICADOS
        // PASAMOS A GENERAR EL TOKEN
        var token = tokenService.generarToken(authentication)

        return ResponseEntity(mapOf("token" to token), HttpStatus.CREATED)
    }

    @DeleteMapping("/eliminarUsuario/{username}")
    fun eliminarUsuario(@PathVariable username:String, authentication: Authentication): ResponseEntity<String> {
        if(username == authentication.name || authentication.authorities.any { it.authority == "ROLE_ADMIN" }){
            return usuarioService.eliminarUsuario(username)
        }else{
            throw UnauthorizedException("No tienes autorizacion de eliminar a otros usuarios")
        }
    }

    @PutMapping("/actualizarUsuario/{username}")
    fun actualizarUsuario(@PathVariable username:String, authentication: Authentication,@RequestBody nuevoUsuario: UsuarioActualizarDto): ResponseEntity<String> {
        if(username == authentication.name || authentication.authorities.any { it.authority == "ROLE_ADMIN" }){
            return usuarioService.actualizarUsuario(username,nuevoUsuario)
        }else{
            throw UnauthorizedException("No tienes autorizacion de eliminar a otros usuarios")
        }
    }

    @GetMapping("/listarusuarios")
    fun listarUsuarios(): ResponseEntity<List<UsuarioDTO>> {
        return usuarioService.listarUsuarios()
    }


}