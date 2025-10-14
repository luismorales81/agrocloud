@echo off
echo ========================================
echo Creando archivos de entorno
echo ========================================
echo.

cd agrogestion-frontend

echo Creando .env.development...
(
echo # Variables de Entorno - DESARROLLO
echo VITE_API_BASE_URL=http://localhost:8080
echo VITE_ENV=development
echo VITE_APP_NAME=AgroCloud
echo VITE_APP_VERSION=1.0.0
) > .env.development

echo Creando .env.production...
(
echo # Variables de Entorno - PRODUCCION
echo VITE_API_BASE_URL=https://api.tudominio.com
echo VITE_ENV=production
echo VITE_APP_NAME=AgroCloud
echo VITE_APP_VERSION=1.0.0
) > .env.production

echo.
echo ========================================
echo Archivos creados exitosamente
echo ========================================
echo.
echo IMPORTANTE: Edita .env.production con tu dominio real
echo.
pause

