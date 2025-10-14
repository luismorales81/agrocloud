@echo off
echo ========================================
echo    AgroCloud - Iniciando Frontend
echo ========================================
echo.

echo Verificando Node.js...
node --version
if %errorlevel% neq 0 (
    echo ERROR: Node.js no esta instalado
    pause
    exit /b 1
)
echo Node.js OK
echo.

echo Iniciando servidor de desarrollo...
cd agrogestion-frontend
echo.
echo ========================================
echo Frontend corriendo en: http://localhost:5173
echo ========================================
echo.
echo Presiona Ctrl+C para detener el servidor
echo.

npm run dev
