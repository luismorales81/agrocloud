@echo off
echo ========================================
echo Iniciando Backend
echo ========================================
echo.

cd agrogestion-backend

echo Iniciando servidor en http://localhost:8080
echo.
echo Presiona Ctrl+C para detener el servidor
echo.

call mvnw.cmd spring-boot:run

pause











