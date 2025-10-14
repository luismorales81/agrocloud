@echo off
echo ====================================
echo Verificando rol del usuario Consultor Externo
echo ====================================
echo.

mysql -u root -p --default-character-set=utf8mb4 agrogestion_db < verificar-rol-consultor-externo.sql

echo.
echo ====================================
echo Verificacion completada
echo ====================================
pause

