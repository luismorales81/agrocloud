# 🌾 AgroGestion - Sistema de Gestión Agrícola

Sistema completo de gestión agrícola con backend en Spring Boot y frontend en React.

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Arquitectura](#-arquitectura)
- [Prerrequisitos](#-prerrequisitos)
- [Instalación](#-instalación)
- [Configuración](#-configuración)
- [Ejecución](#-ejecución)
- [Uso](#-uso)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [API Endpoints](#-api-endpoints)
- [Troubleshooting](#-troubleshooting)

## 🚀 Características

### Backend (Spring Boot)
- ✅ **CRUD Completo**: Usuarios, Roles, Campos, Lotes, Cultivos, Insumos, Labores
- ✅ **Autenticación JWT**: Sistema seguro de autenticación
- ✅ **Base de Datos MySQL**: Persistencia robusta con JPA/Hibernate
- ✅ **API REST**: Documentada con Swagger/OpenAPI
- ✅ **Validaciones**: Bean Validation y manejo de errores
- ✅ **Reportes**: Generación de reportes en JSON

### Frontend (React)
- ✅ **Interfaz Responsive**: Diseño adaptativo con TailwindCSS
- ✅ **Autenticación**: Login/logout con JWT
- ✅ **Componentes Reutilizables**: UI modular y consistente
- ✅ **Interceptores Axios**: Manejo automático de tokens y errores
- ✅ **Notificaciones**: Sistema de alertas amigable
- ✅ **Dashboard**: Métricas y gráficos con Recharts

## 🏗️ Arquitectura

```
AgroGestion/
├── agrogestion-backend/          # Backend Spring Boot
│   ├── src/main/java/com/agrogestion/
│   │   ├── model/entity/         # Entidades JPA
│   │   ├── dto/                  # DTOs
│   │   ├── repository/           # Repositorios
│   │   ├── service/              # Servicios
│   │   ├── controller/           # Controladores REST
│   │   └── config/               # Configuraciones
│   ├── src/main/resources/
│   │   └── application.properties # Configuración BD
│   └── pom.xml                   # Dependencias Maven
├── agrogestion-frontend/         # Frontend React
│   ├── src/
│   │   ├── components/           # Componentes UI
│   │   ├── pages/                # Páginas
│   │   ├── contexts/             # Contextos React
│   │   └── services/             # Servicios API
│   ├── package.json              # Dependencias npm
│   └── tailwind.config.js        # Configuración Tailwind
└── database_script.sql           # Script SQL con datos de prueba
```

## 📋 Prerrequisitos

### Software Requerido

1. **Java 17+**
   ```bash
   # Verificar instalación
   java --version
   ```

2. **Maven 3.6+**
   ```bash
   # Verificar instalación
   mvn --version
   ```

3. **Node.js 16+**
   ```bash
   # Verificar instalación
   node --version
   npm --version
   ```

4. **MySQL 8.0+**
   ```bash
   # Verificar instalación
   mysql --version
   ```

### Instalación de Prerrequisitos

#### Windows
1. **Java**: Descargar desde [Oracle](https://www.oracle.com/java/technologies/downloads/) o usar [OpenJDK](https://adoptium.net/)
2. **Maven**: Descargar desde [Apache Maven](https://maven.apache.org/download.cgi)
3. **Node.js**: Descargar desde [Node.js](https://nodejs.org/)
4. **MySQL**: Descargar desde [MySQL](https://dev.mysql.com/downloads/mysql/)

#### macOS
```bash
# Usando Homebrew
brew install openjdk@17
brew install maven
brew install node
brew install mysql
```

#### Linux (Ubuntu/Debian)
```bash
# Java
sudo apt update
sudo apt install openjdk-17-jdk

# Maven
sudo apt install maven

# Node.js
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# MySQL
sudo apt install mysql-server
```

## 🔧 Instalación

### 1. Clonar el Repositorio

```bash
git clone <repository-url>
cd AgroGestion
```

### 2. Configurar Base de Datos MySQL

```bash
# Iniciar MySQL
sudo systemctl start mysql  # Linux
# o
brew services start mysql   # macOS
# o iniciar desde XAMPP/WAMP en Windows

# Conectar a MySQL
mysql -u root -p

# Crear base de datos y usuario
CREATE DATABASE agrogestion;
CREATE USER 'agrogestion'@'localhost' IDENTIFIED BY 'agrogestion123';
GRANT ALL PRIVILEGES ON agrogestion.* TO 'agrogestion'@'localhost';
FLUSH PRIVILEGES;
EXIT;

# Ejecutar script de base de datos
mysql -u agrogestion -p agrogestion < database_script.sql
```

### 3. Configurar Backend

```bash
cd agrogestion-backend

# Verificar configuración en application.properties
# Ajustar credenciales de base de datos si es necesario
```

### 4. Configurar Frontend

```bash
cd agrogestion-frontend

# Instalar dependencias
npm install
```

## ⚙️ Configuración

### Configuración de Base de Datos

Editar `agrogestion-backend/src/main/resources/application.properties`:

```properties
# Configuración de base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/agrogestion?useSSL=false&serverTimezone=UTC
spring.datasource.username=agrogestion
spring.datasource.password=agrogestion123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Configuración JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

### Configuración del Frontend

Editar `agrogestion-frontend/src/services/api.ts` si es necesario:

```typescript
const API_BASE_URL = 'http://localhost:8080/api';
```

## 🚀 Ejecución

### 1. Iniciar Backend

```bash
cd agrogestion-backend

# Ejecutar con Maven
mvn spring-boot:run

# O compilar y ejecutar
mvn clean compile
mvn spring-boot:run
```

**Backend estará disponible en:**
- API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html
- Actuator: http://localhost:8080/actuator

### 2. Iniciar Frontend

```bash
cd agrogestion-frontend

# Ejecutar en modo desarrollo
npm run dev
```

**Frontend estará disponible en:**
- http://localhost:5173

### 3. Verificar Funcionamiento

1. Abrir http://localhost:5173 en el navegador
2. Verificar que aparezca la página de login
3. Probar login con usuarios de prueba

## 👥 Uso

### Usuarios de Prueba

```
admin / admin123
tecnico / tecnico123
productor / productor123
```

### Funcionalidades Principales

1. **Login**: Autenticación con JWT
2. **Dashboard**: Métricas y resumen del sistema
3. **Usuarios**: Gestión de usuarios y roles
4. **Campos**: Registro y gestión de campos
5. **Lotes**: Gestión de lotes por campo
6. **Cultivos**: Registro de cultivos y variedades
7. **Insumos**: Control de stock y movimientos
8. **Labores**: Registro de actividades agrícolas
9. **Reportes**: Generación de reportes

## 📁 Estructura del Proyecto

### Backend Structure
```
src/main/java/com/agrogestion/
├── model/
│   ├── entity/           # Entidades JPA
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── Field.java
│   │   ├── Plot.java
│   │   ├── Crop.java
│   │   ├── Input.java
│   │   ├── InputMovement.java
│   │   └── Labor.java
│   └── dto/              # DTOs
│       └── UserDto.java
├── repository/           # Repositorios JPA
├── service/              # Lógica de negocio
├── controller/           # Controladores REST
└── config/               # Configuraciones
```

### Frontend Structure
```
src/
├── components/
│   └── ui/               # Componentes reutilizables
│       ├── Button.tsx
│       └── Input.tsx
├── contexts/
│   └── AuthContext.tsx   # Contexto de autenticación
├── pages/
│   └── Login.tsx         # Páginas
├── services/
│   └── api.ts            # Servicios API
└── App.tsx               # Componente principal
```

## 🔌 API Endpoints

### Autenticación
- `POST /api/auth/login` - Login de usuario

### Usuarios
- `GET /api/users` - Listar usuarios
- `POST /api/users` - Crear usuario
- `GET /api/users/{id}` - Obtener usuario
- `PUT /api/users/{id}` - Actualizar usuario
- `DELETE /api/users/{id}` - Eliminar usuario

### Campos
- `GET /api/fields` - Listar campos
- `POST /api/fields` - Crear campo
- `GET /api/fields/{id}` - Obtener campo
- `PUT /api/fields/{id}` - Actualizar campo
- `DELETE /api/fields/{id}` - Eliminar campo

### Lotes
- `GET /api/plots` - Listar lotes
- `POST /api/plots` - Crear lote
- `GET /api/plots/{id}` - Obtener lote
- `PUT /api/plots/{id}` - Actualizar lote
- `DELETE /api/plots/{id}` - Eliminar lote

### Cultivos
- `GET /api/crops` - Listar cultivos
- `POST /api/crops` - Crear cultivo
- `GET /api/crops/{id}` - Obtener cultivo
- `PUT /api/crops/{id}` - Actualizar cultivo
- `DELETE /api/crops/{id}` - Eliminar cultivo

### Insumos
- `GET /api/inputs` - Listar insumos
- `POST /api/inputs` - Crear insumo
- `GET /api/inputs/{id}` - Obtener insumo
- `PUT /api/inputs/{id}` - Actualizar insumo
- `DELETE /api/inputs/{id}` - Eliminar insumo

### Labores
- `GET /api/labors` - Listar labores
- `POST /api/labors` - Crear labor
- `GET /api/labors/{id}` - Obtener labor
- `PUT /api/labors/{id}` - Actualizar labor
- `DELETE /api/labors/{id}` - Eliminar labor

### Reportes
- `GET /api/reports/production` - Reporte de producción
- `GET /api/reports/stock` - Reporte de stock
- `GET /api/reports/costs` - Reporte de costos
- `GET /api/reports/general` - Reporte general

## 🔍 Troubleshooting

### Problemas Comunes

#### 1. Error: "Java no se reconoce"
```bash
# Verificar que Java esté en el PATH
echo $JAVA_HOME
java --version

# Configurar JAVA_HOME si es necesario
export JAVA_HOME=/path/to/java
export PATH=$JAVA_HOME/bin:$PATH
```

#### 2. Error: "Maven no se reconoce"
```bash
# Verificar que Maven esté en el PATH
mvn --version

# Configurar MAVEN_HOME si es necesario
export MAVEN_HOME=/path/to/maven
export PATH=$MAVEN_HOME/bin:$PATH
```

#### 3. Error de conexión a MySQL
```bash
# Verificar que MySQL esté ejecutándose
sudo systemctl status mysql

# Verificar conexión
mysql -u agrogestion -p -h localhost

# Verificar configuración en application.properties
```

#### 4. Error: "Cannot find module 'tailwindcss'"
```bash
cd agrogestion-frontend
npm install -D tailwindcss postcss autoprefixer @tailwindcss/postcss
```

#### 5. Error de CORS
```bash
# Verificar configuración CORS en SecurityConfig.java
# Asegurar que el frontend esté en http://localhost:5173
```

#### 6. Error de puerto ocupado
```bash
# Verificar puertos en uso
netstat -tulpn | grep :8080
netstat -tulpn | grep :5173

# Matar proceso si es necesario
kill -9 <PID>
```

### Logs y Debugging

#### Backend Logs
```bash
# Ver logs en tiempo real
tail -f agrogestion-backend/logs/application.log

# O en la consola donde ejecutaste mvn spring-boot:run
```

#### Frontend Logs
```bash
# Ver logs en la consola del navegador (F12)
# O en la terminal donde ejecutaste npm run dev
```

## 📝 Notas de Desarrollo

### Comandos Útiles

```bash
# Backend
mvn clean compile          # Limpiar y compilar
mvn spring-boot:run        # Ejecutar aplicación
mvn test                   # Ejecutar tests

# Frontend
npm run dev                # Servidor de desarrollo
npm run build              # Build de producción
npm run preview            # Preview del build
npm run lint               # Linting
```

### Variables de Entorno

```bash
# Backend
export SPRING_PROFILES_ACTIVE=dev
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_DATABASE=agrogestion
export MYSQL_USERNAME=agrogestion
export MYSQL_PASSWORD=agrogestion123

# Frontend
export VITE_API_BASE_URL=http://localhost:8080/api
```

## 🤝 Contribución

1. Fork el proyecto
2. Crear rama feature (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'Add AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

## 📞 Soporte

Para soporte técnico:
- Crear un issue en el repositorio
- Contactar al equipo de desarrollo
- Revisar la documentación de la API en Swagger UI

---

**¡AgroGestion está listo para usar! 🌱**

Sistema completo de gestión agrícola con todas las funcionalidades necesarias para administrar campos, cultivos, insumos y labores de manera eficiente.
