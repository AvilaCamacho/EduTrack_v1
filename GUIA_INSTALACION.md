# Guía de Instalación y Configuración - EduTrack v1

## Requisitos Previos

1. **Java Development Kit (JDK) 17 o superior**
   - Descarga desde: https://www.oracle.com/java/technologies/downloads/
   - Verifica la instalación: `java -version`

2. **Apache Maven 3.6 o superior**
   - Descarga desde: https://maven.apache.org/download.cgi
   - Verifica la instalación: `mvn -version`

3. **Cuenta de Oracle Cloud Database**
   - Necesitarás acceso a Oracle Cloud Infrastructure (OCI)
   - Una base de datos Autonomous Database creada

## Paso 1: Configurar la Base de Datos Oracle Cloud

### 1.1 Crear la Base de Datos

1. Inicia sesión en Oracle Cloud Console: https://cloud.oracle.com/
2. Ve a "Autonomous Database"
3. Crea una nueva base de datos (Autonomous Transaction Processing o Autonomous Data Warehouse)
4. Guarda las credenciales del usuario ADMIN

### 1.2 Descargar el Wallet

1. En la página de tu base de datos, haz clic en "DB Connection"
2. Descarga el "Instance Wallet"
3. Proporciona una contraseña para el wallet (guárdala de forma segura)
4. Descarga el archivo ZIP del wallet

### 1.3 Instalar el Wallet en el Proyecto

1. Extrae el contenido del ZIP descargado
2. Copia TODOS los archivos extraídos a la carpeta: `src/main/resources/wallet/`
3. Los archivos deben incluir:
   - `cwallet.sso`
   - `ewallet.p12`
   - `tnsnames.ora`
   - `sqlnet.ora`
   - `ojdbc.properties`
   - `truststore.jks`
   - `keystore.jks`

## Paso 2: Configurar las Credenciales

### 2.1 Editar database.properties

Abre el archivo `src/main/resources/database.properties` y actualiza los valores:

```properties
# Ruta al wallet (normalmente no necesitas cambiar esto si usas la carpeta resources/wallet)
db.wallet.path=src/main/resources/wallet

# Tu usuario de base de datos (por defecto ADMIN o crea uno nuevo)
db.username=ADMIN

# Tu contraseña de base de datos
db.password=TU_CONTRASEÑA_AQUI

# TNS Alias - encuéntralo en el archivo tnsnames.ora dentro del wallet
# Busca un nombre como "dbname_high", "dbname_medium", o "dbname_low"
db.tns.alias=dbname_high
```

### 2.2 Encontrar el TNS Alias

1. Abre el archivo `src/main/resources/wallet/tnsnames.ora`
2. Verás entradas como estas:
   ```
   dbname_high = (description= ...)
   dbname_medium = (description= ...)
   dbname_low = (description= ...)
   ```
3. Usa cualquiera de estos nombres (recomendado: `dbname_high` para mejor rendimiento)

## Paso 3: Crear las Tablas en la Base de Datos

### Opción A: Usando SQL*Plus

1. Descarga Oracle Instant Client desde: https://www.oracle.com/database/technologies/instant-client/downloads.html
2. Instala SQL*Plus
3. Ejecuta:
   ```bash
   sqlplus ADMIN/TU_CONTRASEÑA@dbname_high @database_setup.sql
   ```

### Opción B: Usando Oracle Cloud Console

1. Ve a tu base de datos en Oracle Cloud Console
2. Haz clic en "Database Actions" → "SQL"
3. Abre el archivo `database_setup.sql`
4. Copia y pega todo el contenido
5. Ejecuta el script

## Paso 4: Compilar el Proyecto

```bash
# Navega al directorio del proyecto
cd EduTrack_v1

# Limpia y compila el proyecto
mvn clean compile
```

## Paso 5: Ejecutar la Aplicación

```bash
# Ejecuta la aplicación con Maven
mvn javafx:run
```

## Usuarios de Prueba

El script de base de datos crea automáticamente estos usuarios:

### Maestro
- **Usuario:** `maestro1`
- **Contraseña:** `password123`

### Alumnos
- **Usuario:** `alumno1` - **Contraseña:** `password123`
- **Usuario:** `alumno2` - **Contraseña:** `password123`
- **Usuario:** `alumno3` - **Contraseña:** `password123`

## Funcionalidades del Sistema

### Panel del Maestro

1. **Crear Grupos**
   - Haz clic en "Crear Grupo"
   - Ingresa el nombre y descripción del grupo
   - El grupo aparecerá en la lista

2. **Agregar Alumnos a un Grupo**
   - Selecciona un grupo de la lista
   - Haz clic en "Agregar Alumno"
   - Selecciona el alumno de la lista
   - El alumno se agregará al grupo

3. **Pasar Lista**
   - Selecciona un grupo
   - Haz clic en "Pasar Lista"
   - Marca los alumnos presentes (por defecto todos están marcados)
   - Haz clic en "Guardar"

4. **Eliminar Alumnos**
   - Selecciona un grupo
   - Selecciona un alumno de la tabla
   - Haz clic en "Eliminar Alumno"

5. **Eliminar Grupos**
   - Selecciona un grupo
   - Haz clic en "Eliminar Grupo"
   - Confirma la eliminación

### Panel del Alumno

1. **Ver Grupos**
   - En la parte izquierda verás todos los grupos a los que perteneces

2. **Ver Asistencia**
   - En la parte derecha verás tu historial de asistencia
   - Incluye la fecha y el estado (Presente/Ausente)

## Solución de Problemas

### Error: "No se puede conectar a la base de datos"

**Solución:**
1. Verifica que todos los archivos del wallet estén en `src/main/resources/wallet/`
2. Confirma que las credenciales en `database.properties` sean correctas
3. Asegúrate de que el TNS Alias coincida con el nombre en `tnsnames.ora`

### Error: "Could not find or load main class"

**Solución:**
1. Ejecuta `mvn clean compile` nuevamente
2. Asegúrate de estar usando Java 17 o superior: `java -version`

### Error: "FXML Load Exception"

**Solución:**
1. Verifica que los archivos FXML estén en `src/main/resources/fxml/`
2. Ejecuta `mvn clean compile` para copiar los recursos

### Error: "JavaFX runtime components are missing"

**Solución:**
1. No uses `java -jar` directamente
2. Usa `mvn javafx:run` para ejecutar la aplicación
3. Maven se encargará de las dependencias de JavaFX

## Crear un Ejecutable Independiente

Para crear un JAR con todas las dependencias:

```bash
mvn clean package
```

Para ejecutar el JAR:
```bash
mvn javafx:run
```

## Personalización

### Cambiar Colores del CSS

Edita `src/main/resources/css/style.css`:
- Colores principales: líneas con `#667eea` y `#764ba2`
- Botón primario: `.primary-button { -fx-background-color: #4CAF50; }`
- Botón de peligro: `.danger-button { -fx-background-color: #e74c3c; }`

### Agregar Más Usuarios

Ejecuta en la base de datos:
```sql
INSERT INTO users (username, password, user_type, full_name, email)
VALUES ('nuevo_usuario', 'contraseña', 'TEACHER', 'Nombre Completo', 'email@example.com');
COMMIT;
```

## Seguridad

⚠️ **IMPORTANTE:**
1. **NUNCA** commits archivos del wallet a Git
2. **NUNCA** compartas las credenciales de la base de datos
3. En producción, usa contraseñas encriptadas
4. Cambia las contraseñas por defecto de los usuarios de prueba

## Soporte

Para problemas o preguntas:
1. Revisa el archivo README.md
2. Verifica los logs de error en la consola
3. Consulta la documentación de Oracle Cloud

## Licencia

Este proyecto es de código abierto bajo licencia MIT.
