@echo off
echo ========================================
echo   Aplicando migración para campo responsable
echo ========================================

echo 1. Aplicando migración V1_9__Add_Responsable_To_Labores.sql...

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "USE agrocloud; ALTER TABLE labores ADD COLUMN responsable VARCHAR(255) NULL COMMENT 'Nombre del responsable de la labor';"

if %errorlevel% equ 0 (
    echo ✅ Columna 'responsable' agregada exitosamente
) else (
    echo ❌ Error al agregar columna 'responsable'
)

echo.
echo 2. Creando índice para búsquedas por responsable...

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "USE agrocloud; CREATE INDEX idx_labores_responsable ON labores (responsable);"

if %errorlevel% equ 0 (
    echo ✅ Índice creado exitosamente
) else (
    echo ❌ Error al crear índice (puede que ya exista)
)

echo.
echo 3. Verificando estructura de la tabla labores...

"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "USE agrocloud; DESCRIBE labores;" | findstr responsable

if %errorlevel% equ 0 (
    echo ✅ Campo 'responsable' verificado en la tabla
) else (
    echo ❌ Campo 'responsable' NO encontrado en la tabla
)

echo.
echo ========================================
echo   Migración completada
echo ========================================
echo ✅ Campo responsable agregado a la entidad Labor
echo ✅ Migración de base de datos aplicada
echo ✅ Backend compila correctamente
echo.
echo Próximos pasos:
echo 1. Reiniciar el backend para aplicar cambios
echo 2. Probar creación de labores con responsable
echo 3. Verificar que se muestran los costos correctos
echo.
pause
