# 🌾 AgroGestion - Instrucciones para Windows

Guía rápida para configurar y ejecutar AgroGestion en Windows.

## 📋 Prerrequisitos

### 1. Instalar Java 17+
- Descargar desde: https://adoptium.net/
- Instalar y configurar JAVA_HOME en variables de entorno
- Verificar: `java --version`

### 2. Instalar Maven 3.6+
- Descargar desde: https://maven.apache.org/download.cgi
- Extraer en `C:\Program Files\Apache\maven`
- Agregar `C:\Program Files\Apache\maven\bin` al PATH
- Verificar: `mvn --version`

### 3. Instalar Node.js 16+
- Descargar desde: https://nodejs.org/
- Instalar con opciones por defecto
- Verificar: `node --version` y `npm --version`

### 4. Instalar MySQL 8.0+
**Opción A: MySQL Installer**
- Descargar desde: https://dev.mysql.com/downloads/installer/
- Instalar MySQL Server y MySQL Workbench

**Opción B: XAMPP (Recomendado para desarrollo)**
- Descargar desde: https://www.apachefriends.org/
- Instalar XAMPP con MySQL
- Iniciar MySQL desde el panel de control de XAMPP

## 🚀 Configuración Rápida

### 1. Configurar Base de Datos

**Con XAMPP:**
1. Abrir XAMPP Control Panel
2. Iniciar MySQL
3. Abrir phpMyAdmin (http://localhost/phpmyadmin)
4. Crear base de datos: `agrogestion`
5. Importar archivo: `database_script.sql`

**Con MySQL Installer:**
1. Abrir MySQL Workbench
2. Conectar a MySQL Server
3. Ejecutar script: `database_script.sql`

### 2. Configurar Backend

1. Abrir PowerShell o CMD
2. Navegar al directorio del proyecto:
   ```cmd
   cd C:\Users\moral\AgroGestion
   ```

3. Verificar configuración en `agrogestion-backend\src\main\resources\application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/agrogestion?useSSL=false&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=tu_password_de_mysql
   ```

### 3. Configurar Frontend

1. En el mismo terminal:
   ```cmd
   cd agrogestion-frontend
   npm install
   ```

## 🎯 Ejecución

### Opción 1: Script Automático (Recomendado)

1. Doble clic en `start.bat`
2. El script verificará prerrequisitos y iniciará ambos servicios
3. Abrir http://localhost:5173 en el navegador

### Opción 2: Manual

**Terminal 1 - Backend:**
```cmd
cd agrogestion-backend
mvn spring-boot:run
```

**Terminal 2 - Frontend:**
```cmd
cd agrogestion-frontend
npm run dev
```

## 🔐 Acceso al Sistema

- **URL:** http://localhost:5173
- **Usuarios de prueba:**
  - `admin / admin123`
  - `tecnico / tecnico123`
  - `productor / productor123`

## 🔧 Troubleshooting

### Error: "Java no se reconoce"
1. Verificar que Java esté instalado
2. Configurar JAVA_HOME en variables de entorno:
   - Panel de Control → Sistema → Variables de Entorno
   - Nueva variable: `JAVA_HOME = C:\Program Files\Java\jdk-17`
   - Agregar `%JAVA_HOME%\bin` al PATH

### Error: "Maven no se reconoce"
1. Verificar que Maven esté instalado
2. Agregar al PATH: `C:\Program Files\Apache\maven\bin`

### Error: "MySQL no se conecta"
1. Verificar que MySQL esté ejecutándose
2. Verificar credenciales en `application.properties`
3. Probar conexión: `mysql -u root -p`

### Error: "Puerto 8080 ocupado"
1. Encontrar proceso: `netstat -ano | findstr :8080`
2. Matar proceso: `taskkill /PID <numero> /F`

### Error: "Puerto 5173 ocupado"
1. Encontrar proceso: `netstat -ano | findstr :5173`
2. Matar proceso: `taskkill /PID <numero> /F`

## 📁 Estructura de Archivos

```
AgroGestion/
├── agrogestion-backend/          # Backend Spring Boot
├── agrogestion-frontend/         # Frontend React
├── database_script.sql           # Script de base de datos
├── start.bat                     # Script de inicio para Windows
├── setup.sh                      # Script de configuración (Linux/macOS)
└── README.md                     # Documentación completa
```

## 🌐 URLs Importantes

- **Frontend:** http://localhost:5173
- **Backend API:** http://localhost:8080/api
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **Actuator:** http://localhost:8080/actuator

## 💡 Consejos

1. **Usar XAMPP** para MySQL es más fácil para desarrollo
2. **Mantener las ventanas de terminal abiertas** mientras ejecutas los servicios
3. **Verificar logs** en las terminales para diagnosticar problemas
4. **Usar Chrome DevTools** (F12) para ver errores del frontend

## 🆘 Soporte

Si tienes problemas:
1. Verificar que todos los prerrequisitos estén instalados
2. Revisar los logs en las terminales
3. Verificar que los puertos no estén ocupados
4. Consultar el README.md principal para más detalles

---

**¡AgroGestion está listo para usar en Windows! 🌱**
