@echo off
echo ========================================
echo INSERTANDO DATOS DE PRUEBA EN MYSQL
echo ========================================

echo.
echo Ejecutando script maestro...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 < insertar-todos-los-datos-prueba.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo DATOS INSERTADOS EXITOSAMENTE
    echo ========================================
    echo.
    echo Los siguientes datos han sido insertados:
    echo - 14 insumos generales (fertilizantes, semillas, combustibles, etc.)
    echo - 10 agroquímicos (herbicidas, fungicidas, insecticidas)
    echo - 8 dosis configuradas para agroquímicos
    echo.
    echo El sistema ahora tiene datos reales para probar.
) else (
    echo.
    echo ========================================
    echo ERROR AL INSERTAR DATOS
    echo ========================================
    echo.
    echo Verifique que MySQL esté ejecutándose y que la base de datos 'agrocloud' exista.
)

pause
