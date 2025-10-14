@echo off
echo ========================================
echo   Verificando Datos de Cosecha
echo ========================================
echo.

echo Ejecutando consultas de verificacion...
echo.

mysql -u root -p agroclouddb < verificar-datos-cosecha.sql

echo.
echo ========================================
echo   Verificacion completada
echo ========================================
pause

