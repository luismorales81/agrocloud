@echo off
echo ========================================
echo    AgroGestion Frontend
echo ========================================
echo.

echo 1. Verificando dependencias del frontend...
cd agrogestion-frontend

echo.
echo 2. Instalando dependencias (si es necesario)...
call npm install

echo.
echo 3. Iniciando servidor de desarrollo...
echo Ejecutando: npm run dev
call npm run dev

echo.
echo Frontend iniciado en: http://localhost:5173
echo.
echo Presiona Ctrl+C para detener el frontend
pause
