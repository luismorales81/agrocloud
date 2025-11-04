@echo off
echo ========================================
echo Verificando cambios en el frontend
echo ========================================
echo.

echo [1] Verificando InsumosManagement.tsx...
findstr /C:"DosisAplicacion" "agrogestion-frontend\src\components\InsumosManagement.tsx" >nul
if %ERRORLEVEL% EQU 0 (
    echo ✅ InsumosManagement.tsx - Seccion de dosis encontrada
) else (
    echo ❌ InsumosManagement.tsx - Seccion de dosis NO encontrada
)
echo.

echo [2] Verificando AplicacionesAgroquimicas.tsx...
if exist "agrogestion-frontend\src\components\AplicacionesAgroquimicas.tsx" (
    echo ✅ AplicacionesAgroquimicas.tsx - Archivo existe
) else (
    echo ❌ AplicacionesAgroquimicas.tsx - Archivo NO existe
)
echo.

echo [3] Verificando App.tsx...
findstr /C:"AplicacionesAgroquimicas" "agrogestion-frontend\src\App.tsx" >nul
if %ERRORLEVEL% EQU 0 (
    echo ✅ App.tsx - Import de AplicacionesAgroquimicas encontrado
) else (
    echo ❌ App.tsx - Import de AplicacionesAgroquimicas NO encontrado
)
echo.

echo [4] Verificando ruta en App.tsx...
findstr /C:"aplicaciones-agroquimicas" "agrogestion-frontend\src\App.tsx" >nul
if %ERRORLEVEL% EQU 0 (
    echo ✅ App.tsx - Ruta 'aplicaciones-agroquimicas' encontrada
) else (
    echo ❌ App.tsx - Ruta 'aplicaciones-agroquimicas' NO encontrada
)
echo.

echo ========================================
echo Verificacion completada
echo ========================================
echo.
echo Si todos los checks muestran ✅, los archivos estan correctos.
echo Solo necesitas REINICIAR el frontend para ver los cambios.
echo.
echo Para reiniciar:
echo 1. Deten el frontend (Ctrl+C)
echo 2. Ejecuta: cd agrogestion-frontend
echo 3. Ejecuta: npm run dev
echo.
pause











