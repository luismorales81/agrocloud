@echo off
echo ========================================
echo   Agregando campo responsable a labores
echo ========================================

echo Usando configuración del proyecto:
echo - Base de datos: agrocloud
echo - Usuario: root
echo - Puerto: 3306
echo.

echo 1. Agregando columna 'responsable'...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; ALTER TABLE labores ADD COLUMN responsable VARCHAR(255) NULL COMMENT 'Nombre del responsable de la labor';"

if %errorlevel% equ 0 (
    echo ✅ Columna 'responsable' agregada exitosamente
) else (
    echo ❌ Error al agregar columna 'responsable' (puede que ya exista)
)

echo.
echo 2. Creando índice...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; CREATE INDEX idx_labores_responsable ON labores (responsable);"

if %errorlevel% equ 0 (
    echo ✅ Índice creado exitosamente
) else (
    echo ❌ Error al crear índice (puede que ya exista)
)

echo.
echo 3. Verificando estructura...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p123456 -e "USE agrocloud; DESCRIBE labores;" | findstr responsable

if %errorlevel% equ 0 (
    echo ✅ Campo 'responsable' verificado en la tabla
) else (
    echo ❌ Campo 'responsable' NO encontrado en la tabla
)

echo.
echo ========================================
echo   Migración completada
echo ========================================
echo.
echo El proyecto ya está iniciado y funcionando:
echo - Frontend: http://localhost:3000
echo - Backend:  http://localhost:8080
echo.
echo Ahora puedes probar crear/editar labores y verificar que:
echo 1. El campo responsable se guarda correctamente
echo 2. Los costos de mano de obra se muestran correctamente
echo.
pause
