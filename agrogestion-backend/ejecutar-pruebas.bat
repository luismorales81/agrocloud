@echo off
echo ========================================
echo   AgroGestion - Ejecutando Pruebas
echo ========================================
echo.

echo 1. Verificando Java...
java -version
if %errorlevel% neq 0 (
    echo ERROR: Java no encontrado. Por favor instale Java 17 o superior.
    pause
    exit /b 1
)
echo Java OK
echo.

echo 2. Verificando conexion a MySQL...
echo Verificando que MySQL este ejecutandose en localhost:3306...
echo Si MySQL no esta ejecutandose, por favor inicie XAMPP o MySQL Server.
echo.
pause

echo 3. Ejecutando pruebas automatizadas...
echo.
echo Ejecutando todas las pruebas del sistema AgroGestion...
echo.

mvn test -Dspring.profiles.active=test

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   TODAS LAS PRUEBAS COMPLETADAS
    echo ========================================
    echo.
    echo Las pruebas se ejecutaron exitosamente.
    echo Revise el reporte de cobertura en target/site/jacoco/index.html
    echo.
) else (
    echo.
    echo ========================================
    echo   ERROR EN LAS PRUEBAS
    echo ========================================
    echo.
    echo Algunas pruebas fallaron. Revise los logs para mas detalles.
    echo.
)

echo 4. Generando reporte de cobertura...
mvn jacoco:report

echo.
echo ========================================
echo   PRUEBAS FINALIZADAS
echo ========================================
echo.
echo Reportes disponibles en:
echo - target/site/jacoco/index.html (Cobertura de codigo)
echo - target/surefire-reports/ (Reportes de pruebas)
echo.
pause
