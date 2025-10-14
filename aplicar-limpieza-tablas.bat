@echo off
chcp 65001 >nul
echo ========================================
echo   LIMPIEZA: Eliminar Tablas No Utilizadas
echo ========================================
echo.
echo Esta migración eliminará:
echo - ❌ alquiler_maquinaria (redundante)
echo - ❌ mantenimiento_maquinaria (no implementada)
echo - ❌ auditoria_empresa (sin código)
echo - ❌ configuracion_empresa (sin código)
echo.
echo ✅ Se conservan:
echo - movimientos_inventario (EN USO)
echo - logs_acceso (útil para seguridad)
echo.
echo ⚠️ TODAS las tablas a eliminar tienen 0 registros
echo ⚠️ NO se perderá información
echo ========================================
echo.

pause

echo.
echo Aplicando migración...
echo.

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 agrocloud < agrogestion-backend\src\main\resources\db\migration\V1_13__Cleanup_Unused_Tables.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   ✅ LIMPIEZA COMPLETADA EXITOSAMENTE
    echo ========================================
    echo.
    echo Tablas eliminadas:
    echo - ✅ alquiler_maquinaria
    echo - ✅ mantenimiento_maquinaria
    echo - ✅ auditoria_empresa
    echo - ✅ configuracion_empresa
    echo.
    echo Base de datos optimizada:
    echo - Menos tablas huérfanas
    echo - Código más limpio
    echo - Mejor mantenibilidad
    echo.
) else (
    echo.
    echo ========================================
    echo   ❌ ERROR AL APLICAR MIGRACIÓN
    echo ========================================
    echo.
)

pause
