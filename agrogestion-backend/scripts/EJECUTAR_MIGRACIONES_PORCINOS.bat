@echo off
echo ========================================
echo    EJECUTAR MIGRACIONES PORCINOS
echo ========================================
echo.
echo Este script ejecuta las migraciones V1_102 y V1_103
echo.
echo IMPORTANTE: Asegurate de tener MySQL corriendo y la base de datos 'agrocloud' creada
echo.

REM Leer las migraciones y ejecutarlas
echo Ejecutando migracion V1_102...
mysql -u root -p123456 agrocloud < "%~dp0..\src\main\resources\db\migration\V1_102__Create_Catalogos_Faltantes_Porcinos.sql"

if %ERRORLEVEL% NEQ 0 (
    echo ERROR al ejecutar V1_102
    pause
    exit /b 1
)

echo Ejecutando migracion V1_103...
mysql -u root -p123456 agrocloud < "%~dp0..\src\main\resources\db\migration\V1_103__Add_FKs_To_Existing_Entities.sql"

if %ERRORLEVEL% NEQ 0 (
    echo ERROR al ejecutar V1_103
    pause
    exit /b 1
)

echo.
echo ========================================
echo    MIGRACIONES COMPLETADAS
echo ========================================
echo.
pause


















