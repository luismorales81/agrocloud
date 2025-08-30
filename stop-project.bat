@echo off
echo ========================================
echo    AgroCloud - Deteniendo Servicios
echo ========================================
echo.

echo 1. Deteniendo procesos en puerto 8080 (Backend)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080"') do (
    echo    Terminando proceso PID: %%a
    taskkill /f /pid %%a > nul 2>&1
    if !errorlevel! equ 0 (
        echo    ✅ Proceso terminado
    ) else (
        echo    ⚠️  No se pudo terminar el proceso
    )
)

echo.
echo 2. Deteniendo procesos en puerto 3000 (Frontend)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3000"') do (
    echo    Terminando proceso PID: %%a
    taskkill /f /pid %%a > nul 2>&1
    if !errorlevel! equ 0 (
        echo    ✅ Proceso terminado
    ) else (
        echo    ⚠️  No se pudo terminar el proceso
    )
)

echo.
echo 3. Deteniendo procesos de Java relacionados con AgroCloud...
tasklist /fi "imagenname eq java.exe" /fo csv | findstr /i "agrocloud\|spring" > nul
if %errorlevel% equ 0 (
    taskkill /f /im java.exe > nul 2>&1
    echo    ✅ Procesos Java terminados
) else (
    echo    ℹ️  No se encontraron procesos Java de AgroCloud
)

echo.
echo 4. Deteniendo procesos de Node.js relacionados con AgroCloud...
tasklist /fi "imagenname eq node.exe" /fo csv | findstr /i "agrocloud\|vite" > nul
if %errorlevel% equ 0 (
    taskkill /f /im node.exe > nul 2>&1
    echo    ✅ Procesos Node.js terminados
) else (
    echo    ℹ️  No se encontraron procesos Node.js de AgroCloud
)

echo.
echo ========================================
echo ✅ Servicios detenidos:
echo ========================================
echo.
echo 📱 Frontend (puerto 3000): DETENIDO
echo 🔧 Backend (puerto 8080): DETENIDO
echo.
echo 💡 Para reiniciar los servicios, ejecuta:
echo    start-project-mysql.bat
echo.
echo Presiona cualquier tecla para cerrar...
pause
