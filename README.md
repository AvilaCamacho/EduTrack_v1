# EduTrack v1

Sistema de Gestión Educativa - Aplicación de escritorio JavaFX con base de datos Oracle Cloud

## Descripción

El propósito de este proyecto es desarrollar, mediante JavaFX, un sistema de registro de asistencia escolar que permita a los docentes gestionar de forma práctica y eficiente la asistencia de sus alumnos.

El sistema facilitará el control diario registrando tres estados posibles: presente, ausente y retardo, además de permitir al docente administrar grupos y alumnos mediante operaciones CRUD.

Una vez registrada la asistencia, la aplicación mostrará una pestaña de estadísticas donde el docente podrá visualizar de manera clara y gráfica los porcentajes de asistencia, ausencias y retardos de cada grupo. Estas estadísticas permitirán comprender el comportamiento de los alumnos sin necesidad de generar archivos externos.

El objetivo es ofrecer una herramienta intuitiva, ágil y visualmente clara para apoyar la organización y el seguimiento académico dentro del aula.

## Características

EduTrack es una aplicación de escritorio desarrollada en JavaFX que permite la gestión educativa con dos tipos de usuarios:

- **Maestros**: Pueden crear grupos, gestionar alumnos y pasar lista de asistencia
- **Alumnos**: Pueden ver sus grupos y su historial de asistencia

La aplicación se conecta a una base de datos Oracle Cloud utilizando una wallet para autenticación segura.

## Características

### Panel del Maestro
- ✅ Crear y eliminar grupos
- ✅ Agregar y eliminar alumnos de los grupos
- ✅ Pasar lista de asistencia
- ✅ Ver alumnos por grupo

### Panel del Alumno
- ✅ Ver grupos en los que está inscrito
- ✅ Ver historial de asistencia
- ✅ Consultar estado de asistencia (Presente/Ausente)

## Requisitos

- Java 17 o superior
- Maven 3.6 o superior
- Oracle Cloud Database con wallet configurado
- JavaFX 21.0.1

## Configuración

### 1. Configurar la Base de Datos Oracle Cloud

1. Descarga la wallet de tu base de datos Oracle Cloud
2. Extrae el contenido de la wallet en la carpeta `src/main/resources/wallet/`
3. La wallet debe contener los siguientes archivos:
   - `cwallet.sso`
   - `ewallet.p12`
   - `tnsnames.ora`
   - `sqlnet.ora`
   - `ojdbc.properties`
   - Y otros archivos de certificados

### 2. Configurar las Credenciales

Edita el archivo `src/main/resources/database.properties` con tus credenciales:

```properties
db.wallet.path=src/main/resources/wallet
db.username=TU_USUARIO
db.password=TU_CONTRASEÑA
db.tns.alias=TU_TNS_ALIAS
```

El TNS Alias lo puedes encontrar en el archivo `tnsnames.ora` dentro de tu wallet.

### 3. Ejecutar el Script de Base de Datos

Conecta a tu base de datos Oracle Cloud y ejecuta el script `database_setup.sql` para crear las tablas necesarias:

```sql
sqlplus username/password@tns_alias @database_setup.sql
```

Este script creará:
- Tabla `users` (usuarios)
- Tabla `groups` (grupos)
- Tabla `group_students` (relación grupo-alumnos)
- Tabla `attendance` (asistencia)
- Datos de ejemplo (1 maestro y 3 alumnos)

### 4. Usuarios de Prueba

El script de base de datos crea los siguientes usuarios de prueba:

**Maestro:**
- Usuario: `maestro1`
- Contraseña: `password123`

**Alumnos:**
- Usuario: `alumno1` - Contraseña: `password123`
- Usuario: `alumno2` - Contraseña: `password123`
- Usuario: `alumno3` - Contraseña: `password123`

## Compilación y Ejecución

### Compilar el proyecto

```bash
mvn clean compile
```

### Ejecutar la aplicación

```bash
mvn javafx:run
```

### Crear un JAR ejecutable

```bash
mvn clean package
```

El JAR se generará en `target/edutrack-v1-1.0-SNAPSHOT.jar`

## Estructura del Proyecto

```
EduTrack_v1/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── edutrack/
│       │           ├── controller/         # Controladores JavaFX
│       │           │   ├── LoginController.java
│       │           │   ├── TeacherDashboardController.java
│       │           │   └── StudentDashboardController.java
│       │           ├── database/           # Capa de acceso a datos
│       │           │   ├── DatabaseConnection.java
│       │           │   ├── UserDAO.java
│       │           │   ├── GroupDAO.java
│       │           │   ├── GroupStudentDAO.java
│       │           │   └── AttendanceDAO.java
│       │           ├── model/              # Modelos de datos
│       │           │   ├── User.java
│       │           │   ├── Group.java
│       │           │   ├── GroupStudent.java
│       │           │   └── Attendance.java
│       │           ├── util/               # Utilidades
│       │           │   └── SessionManager.java
│       │           └── Main.java           # Clase principal
│       └── resources/
│           ├── fxml/                       # Archivos de interfaz
│           │   ├── Login.fxml
│           │   ├── TeacherDashboard.fxml
│           │   │   └── StudentDashboard.fxml
│           ├── css/                        # Estilos CSS
│           │   └── style.css
│           ├── wallet/                     # Wallet de Oracle Cloud
│           │   └── (archivos de wallet)
│           └── database.properties         # Configuración de BD
├── database_setup.sql                      # Script de inicialización
├── pom.xml                                 # Configuración Maven
└── README.md
```

## Tecnologías Utilizadas

- **JavaFX 21.0.1**: Framework para la interfaz gráfica
- **Oracle JDBC 21.9.0.0**: Driver para conexión a Oracle Cloud
- **Maven**: Gestión de dependencias y construcción
- **CSS**: Estilos personalizados para la interfaz

## Características Técnicas

- **Patrón Singleton**: Para la gestión de conexión a base de datos y sesión de usuario
- **Patrón DAO**: Para el acceso a datos
- **FXML + CSS**: Separación de lógica y presentación
- **Oracle Wallet**: Autenticación segura con Oracle Cloud
- **Interfaz Responsiva**: Diseño adaptable con componentes JavaFX

## Solución de Problemas

### Error de conexión a la base de datos
- Verifica que la wallet esté en la ubicación correcta
- Asegúrate de que las credenciales en `database.properties` sean correctas
- Verifica que el TNS Alias coincida con el nombre en `tnsnames.ora`

### Error al cargar FXML
- Verifica que los archivos FXML estén en `src/main/resources/fxml/`
- Asegúrate de que los nombres de los controladores sean correctos

### Error de JavaFX
- Verifica que tengas Java 17 o superior instalado
- Asegúrate de que las dependencias de JavaFX se descarguen correctamente con Maven

## Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.

## Autor

EduTrack Development Team