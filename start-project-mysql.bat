@echo off
echo ========================================
echo    AgroCloud - Sistema Completo con MySQL
echo ========================================
echo.

echo 1. Verificando que MySQL esté ejecutándose...
netstat -ano | findstr ":3306" > nul
if %errorlevel% neq 0 (
    echo ❌ ERROR: MySQL no está ejecutándose en el puerto 3306
    echo    Asegúrate de que XAMPP esté iniciado y MySQL esté corriendo
    echo    - Abre XAMPP Control Panel
    echo    - Inicia MySQL
    echo.
    echo Presiona cualquier tecla para continuar o Ctrl+C para cancelar...
    pause
    echo.
    echo Verificando nuevamente...
    netstat -ano | findstr ":3306" > nul
    if %errorlevel% neq 0 (
        echo ❌ MySQL aún no está ejecutándose. Cancelando...
        pause
        exit /b 1
    )
)
echo ✅ MySQL está ejecutándose en el puerto 3306

echo.
echo 2. Verificando que Java esté instalado...
java -version > nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ ERROR: Java no está instalado o no está en el PATH
    echo    Instala Java 17 o superior
    pause
    exit /b 1
)
echo ✅ Java está instalado

echo.
echo 3. Verificando que Node.js esté instalado...
node --version > nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ ERROR: Node.js no está instalado o no está en el PATH
    echo    Instala Node.js 16 o superior
    pause
    exit /b 1
)
echo ✅ Node.js está instalado

echo.
echo 4. Terminando procesos existentes en puertos 8080 y 3000...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080"') do (
    taskkill /f /pid %%a > nul 2>&1
)
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3000"') do (
    taskkill /f /pid %%a > nul 2>&1
)
echo ✅ Puertos liberados

echo.
echo 5. Iniciando Backend con MySQL (puerto 8080)...
start "Backend AgroCloud MySQL" cmd /k "cd agrogestion-backend && .\mvnw.cmd spring-boot:run -Dspring.profiles.active=mysql"

echo 6. Esperando 20 segundos para que el backend se inicie completamente...
echo    Esto puede tomar más tiempo en la primera ejecución...
timeout /t 20 /nobreak > nul

echo 7. Verificando que el backend esté funcionando...
curl -s http://localhost:8080/api/health > nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️  El backend aún se está iniciando, continuando con el frontend...
) else (
    echo ✅ Backend está funcionando correctamente
)

echo.
echo 8. Iniciando Frontend (puerto 3000)...
start "Frontend AgroCloud" cmd /k "cd agrogestion-frontend && npm run dev"

echo.
echo ========================================
echo 🚀 Servicios iniciados con MySQL:
echo ========================================
echo.
echo 📱 Frontend:     http://localhost:3000
echo 🔧 Backend:      http://localhost:8080
echo 📚 Swagger UI:   http://localhost:8080/swagger-ui.html
echo 🗄️  phpMyAdmin:   http://localhost/phpmyadmin
echo.
echo 📊 Base de datos MySQL:
echo    - Host: localhost:3306
echo    - Base de datos: agroclouddb
echo    - Usuario: agrocloudbd
echo    - Contraseña: Jones1212
echo.
echo 🔑 Credenciales por defecto:
echo    - Email: admin@agrocloud.com
echo    - Password: admin123
echo.
echo 💡 Nueva funcionalidad disponible:
echo    - Gestión de Ingresos
echo    - Balance de Costos y Beneficios
echo    - Reportes financieros
echo.
echo ⏳ Los servicios pueden tardar unos minutos en estar completamente disponibles
echo    especialmente en la primera ejecución.
echo.
echo Presiona cualquier tecla para cerrar esta ventana...
pause
