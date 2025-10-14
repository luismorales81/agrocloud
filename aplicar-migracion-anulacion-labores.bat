@echo off
chcp 65001 >nul
echo ========================================
echo   APLICAR MIGRACIÓN: Sistema de Anulación de Labores
echo ========================================
echo.
echo Esta migración agrega:
echo - Nuevo estado ANULADA al enum EstadoLabor
echo - Campos de auditoría: motivo_anulacion, fecha_anulacion, usuario_anulacion_id
echo - Índices para mejorar performance
echo.
echo ========================================
echo.

set /p DB_NAME="Nombre de la base de datos [agrocloud_db]: " || set DB_NAME=agrocloud_db
set /p DB_USER="Usuario MySQL [root]: " || set DB_USER=root
set /p DB_PASSWORD="Contraseña MySQL: "

echo.
echo Aplicando migración...
echo.

mysql -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% < agrogestion-backend\src\main\resources\db\migration\V1_11__Add_Anulacion_Fields_To_Labores.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   ✅ MIGRACIÓN APLICADA EXITOSAMENTE
    echo ========================================
    echo.
    echo Cambios realizados:
    echo - ✅ Agregado estado ANULADA
    echo - ✅ Agregados campos de auditoría
    echo - ✅ Creados índices
    echo.
    echo El sistema ya está listo para:
    echo - Cancelar labores planificadas con restauración automática
    echo - Anular labores ejecutadas con justificación
    echo.
) else (
    echo.
    echo ========================================
    echo   ❌ ERROR AL APLICAR MIGRACIÓN
    echo ========================================
    echo.
    echo Por favor verifica:
    echo - Credenciales de MySQL
    echo - Nombre de la base de datos
    echo - Permisos del usuario
    echo.
)

pause
