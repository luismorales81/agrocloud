@echo off
echo ====================================
echo Diagnostico de Usuario Consultor Externo
echo ====================================
echo.
echo Este script compilara el backend con el nuevo endpoint de diagnostico
echo y luego abrira la pagina de pruebas.
echo.
echo Pasos:
echo 1. Compilar el backend
echo 2. Reiniciar el backend
echo 3. Abrir pagina de diagnostico
echo.
pause

echo.
echo [1/3] Compilando backend...
cd agrogestion-backend
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: No se pudo compilar el backend
    pause
    exit /b 1
)

echo.
echo [2/3] Reiniciando backend...
echo NOTA: Debes reiniciar manualmente el backend o ejecutar iniciar-backend.bat en otra ventana
echo.
pause

echo.
echo [3/3] Abriendo pagina de diagnostico...
cd ..
start test-diagnostico-usuario.html

echo.
echo ====================================
echo Instrucciones:
echo ====================================
echo 1. Inicia sesion con el usuario Consultor Externo
echo 2. Haz clic en "Ver Mi Informacion"
echo 3. Haz clic en "Verificar Acceso a Insumos"
echo 4. Haz clic en "Obtener Lista de Insumos"
echo 5. Copia los resultados y compartilos
echo ====================================
pause

