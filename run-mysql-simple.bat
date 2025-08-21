@echo off
echo ========================================
echo   AGROCLOUD - MYSQL LOCAL SIMPLE
echo ========================================
echo.

echo 1. Verificando que MySQL esté ejecutándose...
echo    Si no tienes MySQL instalado, instala XAMPP o MySQL Server
echo.

echo 2. Compilando la aplicación...
cd agrogestion-backend
call mvn clean compile

echo.
echo 3. Ejecutando la aplicación con perfil de desarrollo...
call mvn spring-boot:run -Dspring-boot.run.profiles=dev

echo.
echo ========================================
echo   APLICACION EJECUTANDOSE EN:
echo   - Backend: http://localhost:8080
echo   - Swagger: http://localhost:8080/swagger-ui.html
echo ========================================
echo.
echo Usuario por defecto: admin
echo Password por defecto: admin123
echo.
echo IMPORTANTE: Asegúrate de tener MySQL ejecutándose en puerto 3306
echo.
pause
