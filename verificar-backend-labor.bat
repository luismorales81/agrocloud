@echo off
echo ========================================
echo   Verificando estado del backend
echo ========================================

echo 1. Verificando si el backend está ejecutándose en puerto 8080:
netstat -an | findstr :8080
if %errorlevel% equ 0 (
    echo ✅ Backend está ejecutándose en puerto 8080
) else (
    echo ❌ Backend NO está ejecutándose en puerto 8080
)

echo.
echo 2. Verificando logs del backend:
if exist "agrogestion-backend\backend.log" (
    echo ✅ Archivo de log encontrado
    echo Últimas líneas del log:
    powershell "Get-Content 'agrogestion-backend\backend.log' | Select-Object -Last 10"
) else (
    echo ❌ Archivo de log no encontrado
)

echo.
echo 3. Verificando si hay errores de compilación:
if exist "agrogestion-backend\target\classes" (
    echo ✅ Backend compilado correctamente
) else (
    echo ❌ Backend no compilado o con errores
)

echo.
echo 4. Verificando configuración de seguridad:
findstr /i "ROLE_SUPERADMIN.*empresas" agrogestion-backend\src\main\java\com\agrocloud\config\SecurityConfig.java
if %errorlevel% equ 0 (
    echo ✅ Configuración de seguridad actualizada
) else (
    echo ❌ Configuración de seguridad no actualizada
)

echo.
echo ========================================
echo   Recomendaciones:
echo ========================================
echo 1. Si el backend no está ejecutándose, ejecutar: .\iniciar-proyecto.bat
echo 2. Si hay errores de compilación, verificar logs
echo 3. Si la configuración no está actualizada, reiniciar el backend
echo.
pause
