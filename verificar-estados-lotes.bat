@echo off
echo ========================================
echo  Verificar Estados de Lotes
echo ========================================
echo.
echo Este script mostrara los estados actuales de todos los lotes
echo y cuales pueden ser sembrados o cosechados.
echo.
echo Presiona cualquier tecla para continuar...
pause > nul

echo.
echo Ejecutando consulta...
echo.

mysql -u root -p agrogestion < verificar-estados-lotes.sql

echo.
echo ========================================
echo  Analisis Completo
echo ========================================
echo.
echo Revisa los resultados arriba para ver:
echo.
echo 1. Todos los lotes con sus estados actuales
echo 2. Cantidad de lotes por estado
echo 3. Lotes que PUEDEN ser sembrados (boton verde aparece)
echo 4. Lotes que PUEDEN ser cosechados (boton naranja aparece)
echo 5. Ultimas labores realizadas
echo.
echo ========================================
echo.
pause
