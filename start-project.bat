@echo off
echo ========================================
echo    AgroGestion - Inicio Completo
echo ========================================
echo.

echo 1. Verificando prerequisitos...
echo    - Java 17+ instalado
echo    - Maven instalado
echo    - Node.js instalado
echo    - XAMPP MySQL corriendo
echo.

echo 2. Configurando base de datos...
echo    - Asegurate de que XAMPP MySQL este corriendo
echo    - Abre phpMyAdmin: http://localhost/phpmyadmin
echo    - Crea base de datos 'agrocloud' si no existe
echo    - Ejecuta el script setup-mysql-local.sql
echo    - Usuario: agrocloudbd / Contraseña: Jones1212
echo.

echo 3. Iniciando Backend...
echo    - Puerto: 8080
echo    - Swagger: http://localhost:8080/swagger-ui.html
echo.
start "Backend" cmd /k "cd agrogestion-backend && mvn spring-boot:run -Dspring.profiles.active=mysql"

echo 4. Esperando que el backend inicie...
timeout /t 15 /nobreak > nul

echo 5. Iniciando Frontend...
echo    - Puerto: 5173
echo    - URL: http://localhost:5173
echo.
start "Frontend" cmd /k "cd agrogestion-frontend && npm install && npm run dev"

echo.
echo ========================================
echo    Proyecto iniciado exitosamente!
echo ========================================
echo.
echo URLs disponibles:
echo - Frontend: http://localhost:5173
echo - Backend API: http://localhost:8080/api
echo - Swagger UI: http://localhost:8080/swagger-ui.html
echo - phpMyAdmin: http://localhost/phpmyadmin
echo.
echo Usuario por defecto:
echo - Username: admin
echo - Password: admin123
echo.
echo Presiona cualquier tecla para cerrar esta ventana...
pause > nul
