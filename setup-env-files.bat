@echo off
echo ====================================================
echo  Configurando archivos .env para Frontend
echo ====================================================
echo.

cd agrogestion-frontend

echo Creando .env.testing...
if exist .env.testing.example (
    copy .env.testing.example .env.testing
    echo [OK] .env.testing creado
) else (
    echo [ADVERTENCIA] .env.testing.example no encontrado
)

echo.
echo Creando .env.production...
if exist .env.production.example (
    copy .env.production.example .env.production
    echo [OK] .env.production creado
) else (
    echo [ADVERTENCIA] .env.production.example no encontrado
)

echo.
echo ====================================================
echo Archivos .env creados exitosamente
echo ====================================================
echo.
echo IMPORTANTE:
echo 1. Edita .env.testing con la URL de Railway Testing
echo 2. Edita .env.production con la URL de Railway Production
echo.
echo Ejemplo:
echo   VITE_API_URL=https://tu-proyecto.up.railway.app
echo.
pause

