@echo off
echo ========================================
echo   Monitoreando Logs del Backend
echo ========================================

echo 1. Verificando procesos Java del backend...
tasklist | findstr java

echo.
echo 2. Verificando puerto 8080...
netstat -an | findstr :8080

echo.
echo 3. Instrucciones para monitorear logs:
echo    - Abre otra ventana de terminal
echo    - Ve al directorio: cd agrogestion-backend
echo    - Ejecuta: .\mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
echo    - Crea una nueva labor desde el frontend
echo    - Observa los logs que aparecen en la consola

echo.
echo 4. Busca estos mensajes en los logs:
echo    - "CREAR LABOR DEBUG"
echo    - "Procesando X elementos de maquinaria"
echo    - "Procesando X elementos de mano de obra"

echo.
echo 5. Si NO aparecen estos logs, el problema está en la deserialización JSON
echo    Si SÍ aparecen pero no se guardan datos, el problema está en el procesamiento

echo.
echo Presiona cualquier tecla para continuar...
pause
