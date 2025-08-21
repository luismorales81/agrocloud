@echo off
echo ========================================
echo    Verificacion Local - AgroGestion
echo ========================================
echo.

echo 1. Verificando Java...
java -version
if %errorlevel% neq 0 (
    echo ERROR: Java no esta instalado o no esta en el PATH
    pause
    exit /b 1
)

echo.
echo 2. Verificando Maven...
mvn -version
if %errorlevel% neq 0 (
    echo ERROR: Maven no esta instalado o no esta en el PATH
    pause
    exit /b 1
)

echo.
echo 3. Verificando puerto 8080...
netstat -an | findstr :8080
if %errorlevel% equ 0 (
    echo ADVERTENCIA: Puerto 8080 esta en uso
    echo Deteniendo procesos en puerto 8080...
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080') do (
        taskkill /f /pid %%a 2>nul
    )
    timeout /t 3 /nobreak > nul
)

echo.
echo 4. Compilando aplicacion...
cd agrogestion-backend
mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Error al compilar la aplicacion
    pause
    exit /b 1
)

echo.
echo 5. Verificando que el JAR se creo...
if not exist "target\agrocloud-backend-1.0.0.jar" (
    echo ERROR: No se encontro el archivo JAR compilado
    pause
    exit /b 1
)

echo.
echo 6. Iniciando aplicacion en modo Railway...
echo Ejecutando: java -Dspring.profiles.active=railway -jar target/agrocloud-backend-1.0.0.jar
echo.
echo NOTA: La aplicacion intentara conectarse a la base de datos MySQL
echo Si no tienes MySQL configurado, veras errores de conexion
echo.
echo Presiona Ctrl+C para detener la aplicacion
echo.

java -Dspring.profiles.active=railway -jar target/agrocloud-backend-1.0.0.jar

echo.
echo Aplicacion detenida
pause
