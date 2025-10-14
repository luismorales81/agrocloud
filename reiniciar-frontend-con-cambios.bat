@echo off
echo ========================================
echo Reiniciando Frontend con Cambios
echo ========================================
echo.

echo 1. Deteniendo procesos de Node.js...
taskkill /F /IM node.exe 2>nul
timeout /t 2 >nul

echo 2. Navegando a frontend...
cd agrogestion-frontend

echo 3. Limpiando caché de npm...
rd /s /q node_modules\.cache 2>nul

echo 4. Iniciando frontend...
echo.
echo ✅ El frontend se iniciará ahora
echo ✅ Cuando veas "webpack compiled", el frontend está listo
echo ✅ Luego abre el navegador y limpia caché (Ctrl+Shift+Delete)
echo.

start cmd /k "npm start"

echo.
echo ========================================
echo Frontend iniciado en nueva ventana
echo ========================================
pause




