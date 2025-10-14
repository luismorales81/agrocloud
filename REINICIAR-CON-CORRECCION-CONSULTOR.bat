@echo off
cls
echo ====================================================
echo  REINICIO CON CORRECCION - CONSULTOR EXTERNO
echo ====================================================
echo.
echo Esta correccion permite que el usuario Consultor Externo
echo pueda ver todos los listados (insumos, lotes, campos, etc.)
echo.
echo IMPORTANTE:
echo - El backend se esta compilando en segundo plano
echo - Espera a que termine la compilacion
echo - Luego este script reiniciara el backend
echo.
pause

echo.
echo ====================================================
echo [1/2] Deteniendo backend actual...
echo ====================================================
echo.
echo PRESIONA Ctrl+C en la ventana del backend para detenerlo
echo Luego presiona cualquier tecla aqui para continuar...
pause

echo.
echo ====================================================
echo [2/2] Iniciando backend con la correccion...
echo ====================================================
echo.
cd agrogestion-backend
call mvn spring-boot:run

echo.
echo ====================================================
echo Backend detenido
echo ====================================================
pause

