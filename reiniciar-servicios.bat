@echo off
echo ========================================
echo   Reiniciando Servicios AgroCloud
echo ========================================
echo.
echo Este script reiniciara backend y frontend
echo.
pause

echo.
echo Deteniendo servicios (si estan corriendo)...
taskkill /F /FI "WINDOWTITLE eq Backend AgroCloud*" 2>nul
taskkill /F /FI "WINDOWTITLE eq Frontend AgroCloud*" 2>nul
timeout /t 3 /nobreak > nul

echo.
echo Iniciando Backend...
cd agrogestion-backend
start "Backend AgroCloud" cmd /k "mvnw.cmd spring-boot:run"
cd ..

echo.
echo Esperando 20 segundos para que el backend inicie...
timeout /t 20 /nobreak

echo.
echo Iniciando Frontend...
cd agrogestion-frontend
start "Frontend AgroCloud" cmd /k "npm run dev"
cd ..

echo.
echo ========================================
echo Servicios reiniciados:
echo ========================================
echo Frontend: http://localhost:3000
echo Backend:  http://localhost:8080
echo.
echo Las contraseñas han sido actualizadas en la BD
echo Usa: tecnico.juan@agrocloud.com / admin123
echo.
pause

