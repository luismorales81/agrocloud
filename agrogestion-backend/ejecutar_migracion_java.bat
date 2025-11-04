@echo off
echo ========================================
echo    EJECUTANDO MIGRACION CON JAVA
echo ========================================
echo.

echo Compilando y ejecutando migración...
echo.

REM Compilar el proyecto primero
.\mvnw.cmd compile

if %ERRORLEVEL% NEQ 0 (
    echo Error al compilar el proyecto
    pause
    exit /b 1
)

echo.
echo Ejecutando migración usando Spring Boot...
echo.

REM Ejecutar la aplicación con un perfil específico para migración
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql -Dspring.jpa.hibernate.ddl-auto=update

echo.
echo ========================================
echo    MIGRACION COMPLETADA
echo ========================================
echo.
pause
