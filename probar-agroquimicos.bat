@echo off
echo ========================================
echo PRUEBA DEL MODULO DE AGROQUIMICOS
echo ========================================
echo.
echo Este script te mostrara como probar los nuevos endpoints
echo.
echo PASO 1: Configurar una dosis sugerida
echo ========================================
echo.
echo POST http://localhost:8080/api/v1/aplicaciones-agroquimicas/dosis
echo Content-Type: application/json
echo.
echo {
echo   "insumoId": 1,
echo   "tipoAplicacion": "FOLIAR",
echo   "dosisPorHa": 2.5,
echo   "unidadMedida": "litros",
echo   "descripcion": "Aplicacion foliar para control de malezas"
echo }
echo.
echo.
echo PASO 2: Obtener dosis sugerida
echo ========================================
echo.
echo GET http://localhost:8080/api/v1/aplicaciones-agroquimicas/dosis/sugerir?insumoId=1^&tipoAplicacion=FOLIAR
echo.
echo.
echo PASO 3: Crear una aplicacion (con calculo automatico)
echo ========================================
echo.
echo POST http://localhost:8080/api/v1/aplicaciones-agroquimicas
echo Content-Type: application/json
echo.
echo {
echo   "laborId": 1,
echo   "insumoId": 1,
echo   "tipoAplicacion": "FOLIAR",
echo   "observaciones": "Aplicacion realizada en horario matutino"
echo }
echo.
echo El sistema calculara automaticamente:
echo - Superficie del lote (ej: 2 ha)
echo - Dosis sugerida (ej: 2.5 litros/ha)
echo - Cantidad total = 2 ha x 2.5 litros/ha = 5 litros
echo - Validara el stock
echo - Descontara automaticamente del stock
echo.
echo.
echo PASO 4: Ver aplicaciones de una labor
echo ========================================
echo.
echo GET http://localhost:8080/api/v1/aplicaciones-agroquimicas/labor/1
echo.
echo.
echo PASO 5: Ver estadisticas de un insumo
echo ========================================
echo.
echo GET http://localhost:8080/api/v1/aplicaciones-agroquimicas/insumo/1/estadisticas
echo.
echo.
echo ========================================
echo IMPORTANTE: Asegurate de que el backend este corriendo
echo ========================================
echo.
echo Para iniciar el backend:
echo cd agrogestion-backend
echo mvnw.cmd spring-boot:run
echo.
echo Para probar los endpoints, usa:
echo - Postman
echo - cURL
echo - O cualquier cliente REST
echo.
pause











