@echo off
echo ========================================
echo PRUEBA DEL ENDPOINT DE AGROQUIMICOS
echo ========================================
echo.
echo Este script probara el endpoint de sugerir dosis
echo.
echo IMPORTANTE: El backend debe estar corriendo
echo.
echo Presiona cualquier tecla para continuar...
pause >nul
echo.

echo ========================================
echo Probando: Sugerir dosis para insumo ID 1, tipo FOLIAR
echo ========================================
echo.

curl -X GET "http://localhost:8080/api/v1/aplicaciones-agroquimicas/dosis/sugerir?insumoId=1&tipoAplicacion=FOLIAR" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbi5lbXByZXNhQGFncm9jbG91ZC5jb20iLCJpYXQiOjE3Mjk4MzE2MDAsImV4cCI6MTcyOTgzNTIwMH0.test"

echo.
echo.
echo ========================================
echo Probando: Listar todas las aplicaciones
echo ========================================
echo.

curl -X GET "http://localhost:8080/api/v1/aplicaciones-agroquimicas" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbi5lbXByZXNhQGFncm9jbG91ZC5jb20iLCJpYXQiOjE3Mjk4MzE2MDAsImV4cCI6MTcyOTgzNTIwMH0.test"

echo.
echo.
echo ========================================
echo Si ves respuestas JSON arriba, los endpoints funcionan!
echo ========================================
echo.
pause











