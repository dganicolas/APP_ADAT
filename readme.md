- a. Nombre del proyecto


- Nombre aplicacion: 
  - homeTask


- b. Descripción detallada de los documentos que intervendrán en el proyecto, así como sus campos.


- Direccion:
  - calle: calle de la direccion
  - num: numero de la calle
  - cp: codigo postal
  - ciudad: ciudad de la direccion
  - municipio: municipio de la direccion
  - provincia: provincia de la direccion
  

- Usuarios:
  - _id: id unico que nos proporciona mongodb
  - username: nombre unico de usuario
  - password: contraseña del usuario
  - email: email unico de usuario
  - roles: roles de usuario: "USER", "ADMIN"
  - direccion: direccion del usuario


- Tareas:
  - nombre: nombre de la tarea
  - descripcion: descripcion de la tarea
  - estado: true o false, dependiendo si esta realizada o no
  - autor: autor de la tarea
  - encargado: quien se encargara de la tarea


- a. Indicar los endpoints que se van a desarrollar para cada documento.


- coleccion: Usuarios
  - /usuarios/registrarse
- /usuarios/acceder
- /usuarios/eliminarUsuario/{usuarioABorrrar}
- /usuario/actualizarUsuario/{username}


- coleccion: tareas
  - /tareas/crear 
  - /tareas/listarTodasLasTareas
  - /tareas/listarTareasPorUsuarios/{nombre}
  - /tareas/actualizarEstadoTarea/{nombre}
  - /tareas/eliminar/{nombre}
  -  /tareas/encargarse/{nombre}/{encargado}


- b. Describir cada uno de los endpoints. Realiza una explicación sencilla de cada endpoint.


  - coleccion: Usuarios 
    - esta coleccion guardara todo lo referente a la entidad usuarios 
    - /usuarios/registrarse
    - se añadira un nuevo usuario a la BBDD, y retornara el usuarioDTO sin la contraseña(username y email)
    - /usuarios/acceder
      - el usuario accedera mediante username y contraseña al sistema y este retornara un token si es valido o un 401 unauthorized
    - /usuarios/eliminarUsuario/{usuarioABorrrar}
      - el mismo usuario o admin podra borrar usuario existente(si no es admin se borrara el mismo)
    - /usuario/actualizarUsuario/{username}
      - el mismo usuario o admin puede editar los usuarios(si no es admin solo puede editarse el mismo)
   
 
  - coleccion: tareas
    - /tareas/crear
      - se crearan nuevas tareas que se ligan a su nombre de usuario (creador de la tarea), si es admin el admin puede decidir quien crea la tarea
    - /tareas/listarTodasLasTareas
      - listara todas las tareas que esten activas o no completadas
    - /tareas/listarTareasPorUsuarios/{nombre}
      - lista todas las tareas que haya creado ese usuario
    - /tareas/actualizarEstadoTarea/{nombre}
      - cambiara el estado de la tarea por "pendiente" a "realizada" o viceversa
    - /tareas/eliminar/{tarea}
      - solo los admin pueden eliminar las tareas vigentes o realizadas o el propio autor
    - /tareas/encargarse/{nombre}/{encargado}
      - el propio usuario pueden encargarse de esa tarea o el admin poner quien se encarga 


- c. Describe la lógica de negocio que va a contener tu aplicación.
  - los nombre de usuarios deben de tener minimo 3 letras o mas y un maximo de 12 letras
  - Los nombres de usuarios, son unico y no repetible
  - Los nombre de tareas debe de ser unicos
  - solos los admin pueden borrar todo tipo de tareas
  - los usuarios que no son admin no pueden poner como autor a otros usuarios
  - los usuarios se pueden encargar de la tarea
  - los admin pueden poner de encargado a cualquier usuario
  - las tareas no completadas pueden cambiar de encargado
  - las tareas completadas son inmutables


- d. Describe las excepciones que vas a generar y los códigos de estado que vas a poner en todos los casos.
  - 400 Bad request: El cliente me envia mal los datos en el cuerpo de la peticion
  - 401 Unauthorized: El cliente no es admin e intenta hacer tareas de admin 
  - 403 forbidden: el usuarios que no es admin esta intentando ver todos los usuarios que estan conectados o registrados 
  - 404 not found: el usuario intenta acceder a una tarea que no existe


- e. Describe las restricciones de seguridad que vas a aplicar dentro de tu API REST
  - solo los usuarios autenticados pueden crear tareas 
  - los admin pueden ver todos los usuarios que estan registrados en la aplicacion
  - los usuarios solo pueden editar sus propias tareas
  - los usuarios no admin solo podran encargarse ellos mismo de las tares y no poner a otros encargados
  - los usuarios no autenticados solo podran logearse o registrarse

#  PRUEBAS GESTIÓN USUARIOS

### Configura tu app para que se conecte a una base de datos MongoDB.
- codigo que de mi app esta bien conectada a mongo
- se encarga de creear una instancia de una consulta a nuestra api
```kotlin
object RetrofitClient {
    private const val BASE_URL = "https://app-adat-9a4d.onrender.com/"
    
    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }
}
```
- endpoints que tratamos en la parte cliente
- aqui configuramos a retrofit con los diferentes endpoints de mi api
```kotlin
interface ApiService {
  @POST("usuarios/registrarse")
  suspend fun registrarUsuario(@Body usuario: UsuarioRegisterDTO): Response<UsuarioDTO?>

  @POST("usuarios/acceder")
  suspend fun loginUsuario(@Body usuario: LoginUsuarioDTO): Response<Map<String, String>>
}
```
# Plantea y documenta varias pruebas que muestren el correcto funcionamiento de la API en estos escenarios
# caso del register exitoso
-bases de datos, render y app antes del inicio de login exitoso
![img.png](src%2Fmain%2Fresources%2Fimg.png)
- caso exitoso 1: el register se hace correctamente
![img_1.png](src%2Fmain%2Fresources%2Fimg_1.png)

# caso del register fallidos
- caso fallido 1: nombre existente en la base de datos
![img_2.png](src%2Fmain%2Fresources%2Fimg_2.png)
- caso fallido 2: contraseñas distintas
![img_3.png](src%2Fmain%2Fresources%2Fimg_3.png)
- caso fallido 3: provincia no existe
![img_4.png](src%2Fmain%2Fresources%2Fimg_4.png)
- caso fallido 4: el email es invalido
![img_5.png](src%2Fmain%2Fresources%2Fimg_5.png)

# casos de login exitoso
- caso exitoso 1: me devuelve el token
![img_6.png](src%2Fmain%2Fresources%2Fimg_6.png)

# casos de login fallido 
- caso fallido 1: el usuario no existe en la BD
![img_1.png](img_1.png)