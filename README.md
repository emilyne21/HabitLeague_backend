# 20
# PivotHub

## CS 2031 - Desarrollo Basado en Plataformas
**Universidad de Ingeniería y Tecnología – UTEC**  
**Ciclo 2025-1**

---

## 👥 Integrantes del equipo

- Luis Fernando Maquera Quispe
- Amira Emilyn Pérez del Águila Mayorga
- Carlos Alith Quispe Curay
- Ana Frances Forsyth Belaunde

---

## 📑 Índice

1. [Introducción](#introducción)
2. [Identificación del Problema](#identificación-del-problema)
3. [Descripción de la Solución](#descripción-de-la-solución)
4. [Modelo de Entidades](#modelo-de-entidades)
5. [Testing y Manejo de Errores](#testing-y-manejo-de-errores)
6. [Medidas de Seguridad Implementadas](#medidas-de-seguridad-implementadas)
7. [Eventos y Asincronía](#eventos-y-asincronía)
8. [GitHub y Planificación](#github-y-planificación)
9. [Integración del Dockerfile con el despliegue en AWS EC2](#integración-del-dockerfile-con-el-despliegue-en-aws-ec2)
10. [Conclusión](#conclusión)
11. [Apéndices](#apéndices)

---

## 📌 Introducción

### Contexto
Durante marzo de 2025, nació la comunidad PivotHub como una iniciativa para fomentar el cambio de hábitos entre amigos mediante retos grupales. En su primera edición, 30 participantes se comprometieron a cumplir un hábito diario durante 30 días (como leer, hacer ejercicio o programar). Si alguien no cumplía, debía pagar una penalización económica, la cual se acumulaba en un pozo común que luego fue repartido entre quienes cumplieron más días. Esta experiencia fue organizada manualmente a través de un grupo de WhatsApp. Debido a su éxito y a la demanda por una experiencia más automatizada y escalable, nace la idea de crear PivotHub como plataforma tecnológica.

### Objetivos del Proyecto
- Permitir que usuarios creen o se unan a retos colectivos de hábitos (ej. ir al gimnasio)
- Registrar evidencia diaria de cumplimiento
- Aplicar penalizaciones automáticas por incumplimiento
- Recompensar a los más constantes
- Validar evidencias usando IA y ubicación GPS

---

## 🚨 Identificación del Problema

### Descripción del Problema
Muchas personas desean mejorar sus hábitos, pero carecen de herramientas que les ayuden a sostener el compromiso y la constancia en el tiempo. Además, el entorno social y la motivación extrínseca (como recompensas) juegan un rol clave en la construcción de nuevos hábitos. PivotHub responde a esta necesidad al introducir accountability social, penalizaciones económicas y una comunidad con metas compartidas. La experiencia piloto demostró que este enfoque es efectivo, y ahora se busca escalarlo a través de una plataforma web.

PivotHub aborda este problema con:
- Retos grupales
- Recompensas extrínsecas
- Validación objetiva de cumplimiento
- Automatización de seguimiento

---

## 🛠️ Descripción de la Solución

### Funcionalidades Implementadas
- Registro y login con autenticación JWT
- Crear y unirse a retos de hábitos
- Registro de evidencia diaria (imagen + ubicación)
- Validación automática con IA y geofencing

### Tecnologías Utilizadas
- Java 17
- Spring Boot 3.x
- PostgreSQL (Docker para desarrollo, RDS para producción)
- Spring Security + JWT
- Maven
- JPA/Hibernate
- Postman para documentación de API
- JUnit 5, Testcontainer y Mockito para testing
- AWS S2 para despliegue


---

## 🧱 Modelo de Entidades

### Diagrama E-R
![img.png](Modelo_entidad_relacion.png)

### Descripción de Entidades
- **User**: usuario registrado
- **Challenge**: reto de hábitos
- **ChallengeMember**: unión entre usuario y reto, almacena progreso
- **Evidence**: evidencia diaria con validación
- **Habit**: definición de un hábito con nombre, frecuencia y objetivo de cumplimiento
- **Identity**: faceta o rol de un usuario que agrupa hábitos relacionados
- **Routine**: colección de hábitos organizados que un usuario realiza conjuntamente


---

## 🧪 Testing y Manejo de Errores

### Niveles de Testing
- **Servicio (Unit Testing)**  
  Validación de la lógica de negocio con JUnit 5 y Mockito: mocks de repositorios y `ApplicationEventPublisher`, verificación de flujos correctos y manejo de excepciones.

- **Controller (Integration Testing)**  
  Pruebas de endpoints HTTP con `@SpringBootTest` y `MockMvc`: rutas, verbos, códigos de estado y payloads.

- **Repositorio (Persistence Testing)**  
  Verificación de la capa de datos con `@DataJpaTest`, Testcontainers y `TestEntityManager`: operaciones CRUD, consultas personalizadas y relaciones JPA.

### Resultados
- Cobertura de todos los servicios críticos.
- Validación de casos límite (evidencias duplicadas, uniones repetidas, parámetros inválidos).

### Manejo de Errores
Centralizado en `@RestControllerAdvice` (`GlobalExceptionHandler`), con respuestas uniformes que incluyen `timestamp`, `status`, `error`, `message` y `path`:

| Excepción                                  | HTTP Status             | Descripción                                                        |
|--------------------------------------------|-------------------------|--------------------------------------------------------------------|
| `MethodArgumentTypeMismatchException`      | 400 Bad Request         | “Parámetro 'x' con valor 'y' no es válido”                         |
| `MethodArgumentNotValidException`          | 400 Bad Request         | Errores de validación (mapa `field → mensaje`)                     |
| `IllegalArgumentException`                 | 400 Bad Request         | Mensaje de la excepción                                            |
| `BadCredentialsException`                  | 401 Unauthorized        | Credenciales inválidas                                             |
| `AccessDeniedException`                    | 403 Forbidden           | “No tienes permiso para realizar esta acción”                     |
| `EntityNotFoundException`, `UsernameNotFoundException` | 404 Not Found            | Entidad o usuario no encontrado                                     |
| `UserAlreadyExistsException`               | 409 Conflict            | Usuario ya existe                                                  |
| `ChallengeException`                       | 409 Conflict            | Conflicto específico en `/api/challenges`                          |
| `RuntimeException`                         | 500 Internal Server Error | Fallback genérico                                                 |

---

## 🔐 Medidas de Seguridad Implementadas
Spring Security con JWT:
Se utiliza un filtro personalizado llamado JwtAuthenticationFilter, que se añade antes del filtro estándar UsernamePasswordAuthenticationFilter en la cadena de seguridad. Este filtro extrae el token JWT del encabezado Authorization de cada solicitud, lo valida y, si es correcto, autentica al usuario y lo coloca en el contexto de seguridad de Spring (SecurityContextHolder).

Codificación de contraseñas con BCrypt:
Se define un PasswordEncoder utilizando la clase BCryptPasswordEncoder. Esta codificación se usa tanto al registrar nuevos usuarios como para verificar contraseñas en el inicio de sesión, garantizando que las contraseñas se almacenen de forma segura y no en texto plano.

Validación de tokens con filtro personalizado:
El JwtAuthenticationFilter es responsable de validar el token JWT. Extrae el token del encabezado Authorization, lo analiza, verifica su validez y extrae la información del usuario. Si todo es correcto, establece la autenticación del usuario para que el backend lo reconozca como autenticado en las siguientes operaciones.

Protección de rutas por rol:
Se protegen todas las rutas que no están explícitamente permitidas mediante .anyRequest().authenticated(), lo que obliga a que cualquier ruta no pública requiera autenticación. Sin embargo, no se aplican restricciones específicas por rol (hasRole() o hasAuthority()), por lo tanto, todos los usuarios autenticados tienen acceso por igual a las rutas protegidas.

Configuración de CORS:
Se configura un CorsConfigurationSource que permite solicitudes desde cualquier origen (*), acepta métodos HTTP como GET, POST, PUT, DELETE, y permite cualquier encabezado. Además, expone el encabezado Authorization para que el cliente pueda acceder al token JWT. Esto permite que el frontend interactúe con el backend sin problemas de políticas de mismo origen.

---

## 🔁 Eventos y Asincronía
@Amira

### Patrón de Publicación/Suscripción
Se emplea el patrón Pub/Sub nativo de Spring, basado en tres componentes principales:

- **Eventos**: Objetos que encapsulan información relevante (creación de retos, unión de usuarios)
- **Publisher**: Componente que publica el evento tras completar una operación (ApplicationEventPublisher)
- **Listeners**: Componentes suscritos a tipos específicos de eventos que ejecutan lógica adicional (envío de correos)

La asincronía se habilita mediante anotaciones:
- `@EnableAsync` en la configuración global
- `@Async("applicationTaskExecutor")` en los métodos listener

Esto permite que operaciones como el envío de correos no bloqueen el hilo principal de atención a peticiones HTTP.

### Tipos de Eventos Implementados

#### ChallengeCreatedEvent
```java
public class ChallengeCreatedEvent {
    private final Long   challengeId;
    private final String creatorEmail;
    private final String challengeName;
    // Constructor y getters...
}
```

**Propósito**: Transportar los datos mínimos necesarios para notificar al creador del reto.

**Campos**:
- `challengeId`: Identificación del reto recién creado
- `creatorEmail`: Correo electrónico del usuario que lo creó
- `challengeName`: Nombre legible del reto

#### UserJoinedChallengeEvent
```java
public class UserJoinedChallengeEvent {
    private final Long   userId;
    private final String userEmail;
    private final Long   challengeId;
    private final String challengeName;
    private final Long   challengeMemberId;
    // Constructor y getters...
}
```

**Propósito**: Notificar la unión de un usuario a un reto.

**Campos**: Incluyen tanto la identidad del usuario como del reto y la entidad de participación (ChallengeMember).

### Publicación de Eventos

#### En el método createChallenge
Ubicado en `ChallengeService.createChallenge(...)`, tras validar la duración y guardar la entidad Challenge:

```java
Challenge saved = challengeRepository.save(challenge);
publisher.publishEvent(new ChallengeCreatedEvent(
    saved.getId(),
    user.getEmail(),
    saved.getName()
));
return saved;
```

- **Publisher**: ApplicationEventPublisher inyectado mediante constructor
- **Momento**: Inmediatamente después de persistir el reto

#### En el método joinChallenge
En `ChallengeService.joinChallenge(...)`, tras crear y guardar el registro de participante:

```java
member = challengeMemberRepository.save(member);
publisher.publishEvent(new UserJoinedChallengeEvent(
    user.getId(),
    user.getEmail(),
    challenge.getId(),
    challenge.getName(),
    member.getId()
));
return member;
```

Esto garantiza que solo se notifique si la operación de unión ha concluido exitosamente.

### Listeners Asíncronos

#### Configuración de Asincronía
- `@EnableAsync` en configuración global
- `AsyncConfig` define un ThreadPoolTaskExecutor nombrado "applicationTaskExecutor"

#### Clase ChallengeEventListener
```java
@Component
public class ChallengeEventListener {
    private final JavaMailSender mailSender;
    private final String         fromAddress;

    @Async("applicationTaskExecutor")
    @EventListener
    public void handleUserJoined(UserJoinedChallengeEvent event) { /* ... */ }

    @Async("applicationTaskExecutor")
    @EventListener
    public void handleChallengeCreated(ChallengeCreatedEvent event) { /* ... */ }
}
```

##### Método handleUserJoined
**Objetivo**: Enviar correo al usuario que se une a un reto.

**Flujo**:
1. Extrae userEmail y challengeName del evento
2. Construye SimpleMailMessage con asunto y cuerpo personalizados
3. Invoca mailSender.send(...) en un hilo del pool

##### Método handleChallengeCreated
**Objetivo**: Notificar por correo al creador de un reto recién creado.

**Diferencia clave**: Utiliza únicamente creatorEmail y challengeName, omitiendo el ID en el cuerpo del mensaje para mantener un texto más amigable.

### Flujo de Ejecución Completo
1. Solicitud HTTP a /api/challenges (o endpoint de unión)
2. Controlador delega en ChallengeService
3. Service guarda entidad y publica evento
4. Spring detecta el evento y despacha al listener correspondiente
5. Listener —en un hilo separado— envía el correo sin afectar la respuesta al cliente
6. Respuesta HTTP retorna al usuario con éxito

---

## 📊 GitHub y Planificación
### Flujo de trabajo Git
- Uso de ramas por feature
- Issues por funcionalidad
- Pull Requests con revisiones internas
- GitHub Projects como tablero Kanban

### GitHub Actions (si aplicaste)
- (opcional) Automatización de pruebas o deploy

---

## 🐳 Integración del Dockerfile con el despliegue en AWS EC2

Para que el Dockerfile multi-stage que definiste se convierta en un servicio corriendo en la nube de AWS, es necesario encadenar dos procesos:

1. Construcción y empaquetado de la imagen
2. Publicación y ejecución en una instancia EC2

A continuación se detalla cómo conectar ambos programas:

### 1. Construcción de la imagen con Dockerfile

```dockerfile
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
COPY ../../habitback/Habitleague/.env .

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

- **Etapa build**: compila el proyecto y genera el JAR.
- **Etapa runtime**: empaqueta únicamente app.jar y tu archivo .env en una imagen ligera.

Tras este `docker build -t pivothub:latest .`, dispones de una imagen local lista para subir a un registro.

### 2. Publicación en un registro de contenedores (Amazon ECR)

Para que tus instancias EC2 puedan descargar la imagen, primero debes alojarla en un repositorio accesible:

1. Crear un repositorio en Amazon ECR (Elastic Container Registry).
2. Autenticarse y push con comandos AWS CLI:

```bash
aws ecr get-login-password --region us-west-2 \
  | docker login --username AWS --password-stdin 123456789012.dkr.ecr.us-west-2.amazonaws.com

docker tag pivothub:latest 123456789012.dkr.ecr.us-west-2.amazonaws.com/pivothub:latest
docker push 123456789012.dkr.ecr.us-west-2.amazonaws.com/pivothub:latest
```

Con ello, tu imagen queda disponible en `123456789012.dkr.ecr.us-west-2.amazonaws.com/pivothub:latest`.

### 3. Creación y configuración de la instancia EC2

- Elegir un AMI (p.ej. Amazon Linux 2 con soporte Docker).
- Asignar un IAM Role con permisos `ecr:GetAuthorizationToken`, `ecr:BatchGetImage`, `ecr:GetDownloadUrlForLayer`.
- Configurar el Security Group para permitir tráfico TCP 8080 desde Internet y SSH (22) solo desde tu IP.

### 4. Arranque automático del contenedor en EC2

En la sección User Data de la configuración EC2, pega un script bash:

```bash
#!/bin/bash
# Instalación y arranque de Docker
amazon-linux-extras install docker -y
systemctl enable docker
systemctl start docker

# Login a ECR y pull de imagen
$(aws ecr get-login-password --region us-west-2 \
  | docker login --username AWS --password-stdin 123456789012.dkr.ecr.us-west-2.amazonaws.com)
docker pull 123456789012.dkr.ecr.us-west-2.amazonaws.com/pivothub:latest

# Ejecución del contenedor
docker run -d \
  --name pivothub \
  -p 8080:8080 \
  --env-file /home/ec2-user/.env \
  123456789012.dkr.ecr.us-west-2.amazonaws.com/pivothub:latest
```

- Instala Docker si no está presente.
- Loguea en ECR y hace pull.
- Arranca el contenedor exponiendo el puerto 8080 y cargando variables desde .env.

### 5. Flujo final de despliegue

1. Ejecutas docker build localmente con tu Dockerfile.
2. Etiquetas y subes la imagen a ECR.
3. Creas o actualizas una EC2 basada en un AMI que tenga Docker.
4. EC2, al iniciarse, ejecuta el User Data que descarga y levanta tu contenedor.
5. El servicio de PivotHub queda accesible en la nube, en la IP pública o detrás de un Load Balancer.

---

## 🏁 Conclusión

### Logros del Proyecto
- Se cumplió con todas las funcionalidades del MVP
- El backend es seguro, escalable y documentado
- Implementación completa de pruebas unitarias e integración
- Infraestructura cloud en AWS que garantiza alta disponibilidad
- Modelo de entidades expandido para soportar nuevas funcionalidades


### Aprendizajes Clave
- Buenas prácticas de arquitectura en Spring
- Estrategias efectivas de testing para garantizar calidad
- Configuración e implementación de despliegue en la nube
- Coordinación efectiva en equipo técnico


### Trabajo Futuro
- Modelo IA real con OpenCV o Hugging Face
- Frontend completo para experiencia del usuario
- Escalamiento horizontal para soportar mayor carga de usuarios

---

## 📎 Apéndices

### Referencias
- Documentación de Spring Boot
- Certificación de Postman
- JJWT, JUnit 5, Mockito
