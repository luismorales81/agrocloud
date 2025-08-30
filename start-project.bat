@echo off
echo ========================================
echo    AgroCloud - Sistema Completo
echo ========================================
echo.

echo Iniciando Backend y Frontend...
echo.

echo 1. Iniciando Backend (puerto 8080)...
start "Backend AgroCloud" cmd /k "cd agrogestion-backend && mvnw spring-boot:run"

echo 2. Esperando 10 segundos para que el backend se inicie...
timeout /t 10 /nobreak > nul

echo 3. Iniciando Frontend (puerto 3000)...
start "Frontend AgroCloud" cmd /k "cd agrogestion-frontend && npm run dev"

echo.
echo ========================================
echo Servicios iniciados:
echo - Backend: http://localhost:8080
echo - Frontend: http://localhost:3000
echo - H2 Console: http://localhost:8080/h2-console
echo - Swagger UI: http://localhost:8080/swagger-ui.html
echo ========================================
echo.
echo Credenciales por defecto:
echo - Email: admin@agrocloud.com
echo - Password: admin123
echo.
pause
