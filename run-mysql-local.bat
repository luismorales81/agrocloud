@echo off
echo ========================================
echo    AgroGestion - Ejecucion Local
echo ========================================
echo.

echo 1. Verificando que XAMPP MySQL este corriendo...
echo    - Abre XAMPP Control Panel
echo    - Inicia MySQL si no esta corriendo
echo    - Presiona cualquier tecla cuando MySQL este activo
pause

echo.
echo 2. Configurando base de datos...
echo    - Abre phpMyAdmin en: http://localhost/phpmyadmin
echo    - Crea una nueva base de datos llamada 'agrocloud'
echo    - O ejecuta el script setup-mysql-local.sql
echo    - Presiona cualquier tecla cuando la BD este lista
pause

echo.
echo 3. Iniciando Backend con MySQL...
cd agrogestion-backend
echo Ejecutando: mvn spring-boot:run -Dspring.profiles.active=mysql
mvn spring-boot:run -Dspring.profiles.active=mysql

echo.
echo Backend iniciado en: http://localhost:8080
echo Swagger UI: http://localhost:8080/swagger-ui.html
echo.
echo Presiona Ctrl+C para detener el backend
pause
