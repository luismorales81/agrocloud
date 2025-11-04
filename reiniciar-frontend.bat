@echo off
echo ========================================
echo Reiniciando Frontend
echo ========================================
echo.

cd agrogestion-frontend

echo [1] Limpiando cache...
call npm cache clean --force >nul 2>&1

echo [2] Instalando dependencias...
call npm install

echo [3] Iniciando servidor de desarrollo...
echo.
echo ========================================
echo ✅ Frontend iniciado
echo ========================================
echo.
echo El servidor se esta iniciando...
echo Abre tu navegador en la URL que aparezca
echo (generalmente http://localhost:5173)
echo.
echo Presiona Ctrl+C para detener el servidor
echo.

call npm run dev

pause











