@echo off
echo ====================================================
echo  Generando Dump de Base de Datos
echo ====================================================
echo.

set DB_NAME=agrogestion_db
set OUTPUT_DIR=database-dumps
set TIMESTAMP=%date:~-4%%date:~3,2%%date:~0,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%

if not exist %OUTPUT_DIR% mkdir %OUTPUT_DIR%

echo [1/3] Generando estructura de tablas...
mysqldump -u root -p --no-data --skip-add-drop-table --skip-comments %DB_NAME% > %OUTPUT_DIR%\schema_%TIMESTAMP%.sql

echo.
echo [2/3] Generando datos completos...
mysqldump -u root -p --skip-comments %DB_NAME% > %OUTPUT_DIR%\full_backup_%TIMESTAMP%.sql

echo.
echo [3/3] Generando solo datos (sin estructura)...
mysqldump -u root -p --no-create-info --skip-comments %DB_NAME% > %OUTPUT_DIR%\data_only_%TIMESTAMP%.sql

echo.
echo ====================================================
echo Dumps generados exitosamente en: %OUTPUT_DIR%
echo ====================================================
echo.
echo Archivos creados:
echo - schema_%TIMESTAMP%.sql (solo estructura)
echo - full_backup_%TIMESTAMP%.sql (estructura + datos)
echo - data_only_%TIMESTAMP%.sql (solo datos)
echo.
pause

