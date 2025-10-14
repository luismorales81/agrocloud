@echo off
chcp 65001 >nul
echo ========================================
echo   MIGRACIÓN: Unificar Sistema de Cosechas
echo ========================================
echo.
echo Esta migración realiza:
echo - Migra datos de 'cosechas' a 'historial_cosechas'
echo - Elimina tabla 'cosechas' duplicada
echo - Unifica el sistema usando solo 'historial_cosechas'
echo.
echo ⚠️  IMPORTANTE: Se hará backup de datos existentes
echo ========================================
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 agrocloud < agrogestion-backend\src\main\resources\db\migration\V1_12__Drop_Cosechas_Table.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   ✅ MIGRACIÓN APLICADA EXITOSAMENTE
    echo ========================================
    echo.
    echo Cambios realizados:
    echo - ✅ Datos migrados de cosechas a historial_cosechas
    echo - ✅ Tabla 'cosechas' eliminada
    echo - ✅ Sistema unificado completado
    echo.
    echo El sistema ahora:
    echo - Guarda cosechas en historial_cosechas
    echo - Reportes usan datos completos
    echo - Análisis de suelo disponible
    echo - Recomendaciones de descanso funcionales
    echo.
) else (
    echo.
    echo ========================================
    echo   ❌ ERROR AL APLICAR MIGRACIÓN
    echo ========================================
    echo.
    echo Por favor verifica:
    echo - Credenciales de MySQL
    echo - Nombre de la base de datos: agrocloud
    echo - Permisos del usuario
    echo.
)

pause
