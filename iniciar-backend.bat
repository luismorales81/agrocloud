@echo off
echo ========================================
echo   Iniciando Backend
echo ========================================

echo 1. Verificando si el backend está ejecutándose...
netstat -an | findstr :8080

echo.
echo 2. Si no está ejecutándose, iniciando backend...
cd agrogestion-backend
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql

echo.
echo Backend iniciado
echo.
echo Presiona cualquier tecla para continuar...
pause
