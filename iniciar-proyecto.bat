@echo off
setlocal EnableExtensions
cd /d "%~dp0"

echo ========================================
echo    AgroCloud - Contenedores locales
echo ========================================
echo.

where docker >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker no esta en el PATH. Instala Docker Desktop y reinicia la terminal.
    pause
    exit /b 1
)

docker info >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker Desktop no esta en ejecucion. Abri Docker Desktop y espera a que este listo.
    pause
    exit /b 1
)

echo Levantando MySQL, backend y frontend con Docker Compose...
echo La primera vez puede tardar varios minutos (build + Flyway).
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0deploy\levantar-local.ps1"
if errorlevel 1 (
    echo.
    echo ERROR: No se pudo levantar el stack. Mira el mensaje de PowerShell arriba.
    pause
    exit /b 1
)

echo.
echo Abriendo http://localhost:3001
start "" "http://localhost:3001"

echo.
echo Mostrando logs del backend (contenedor). Cerra esa ventana no detiene Docker.
start "Backend AgroCloud (Docker)" /D "%~dp0" cmd /k "docker compose logs -f --tail 80 backend"

echo.
echo Login local: admin@localhost / AgrocloudLocal1
echo Frontend: http://localhost:3001
echo API:      http://localhost:3001/api/health
echo Para detener: docker compose down
echo.
pause
