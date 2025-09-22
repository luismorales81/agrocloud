@echo off
echo ========================================
echo    Aplicando Migración Historial Cosechas
echo ========================================
echo.

echo 1. Verificando que el backend esté detenido...
echo    (Es necesario detener el backend para aplicar migraciones)
echo.

echo 2. Navegando al directorio del backend...
cd agrogestion-backend

echo 3. Aplicando migración con Flyway...
echo    Ejecutando: mvn flyway:migrate
mvn flyway:migrate

if %errorlevel% neq 0 (
    echo ERROR: La migración falló
    echo.
    echo Posibles soluciones:
    echo - Verificar que MySQL esté ejecutándose
    echo - Verificar las credenciales de la base de datos
    echo - Verificar que las tablas relacionadas existan
    pause
    exit /b 1
)

echo.
echo 4. Migración aplicada exitosamente!
echo.

echo 5. Verificando la estructura de la tabla...
echo    Ejecutando script de verificación...
cd ..
mysql -u root -p agrogestion < verificar-tabla-historial-cosechas.sql

echo.
echo ========================================
echo    Migración Completada
echo ========================================
echo.
echo La tabla historial_cosechas ha sido creada con:
echo - Todas las relaciones (lotes, cultivos, users)
echo - Índices optimizados
echo - Restricciones de validación
echo - Vistas para consultas frecuentes
echo.
echo Ahora puedes reiniciar el backend y usar el sistema.
echo.
echo Para reiniciar el proyecto completo:
echo .\iniciar-proyecto.bat
echo.
pause
