@echo off
echo ========================================
echo PROBAR HEALTH CHECK ENDPOINT
echo ========================================
echo.

echo [1/3] Verificando si el backend esta corriendo...
timeout /t 2 /nobreak >nul

echo.
echo [2/3] Probando endpoint /actuator/health...
echo.

curl -X GET http://localhost:8080/actuator/health -H "Content-Type: application/json"

echo.
echo.
echo [3/3] Probando endpoint /actuator/info...
echo.

curl -X GET http://localhost:8080/actuator/info -H "Content-Type: application/json"

echo.
echo.
echo ========================================
echo PRUEBA COMPLETADA
echo ========================================
echo.
echo Si ves {"status":"UP"}, el health check esta funcionando correctamente.
echo Si ves un error, asegurate de que el backend este corriendo.
echo.
pause

