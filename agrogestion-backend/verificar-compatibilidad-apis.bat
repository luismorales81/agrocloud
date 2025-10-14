@echo off
echo ========================================
echo VERIFICACION DE COMPATIBILIDAD APIs
echo ========================================
echo.

echo [1/5] Verificando endpoints de autenticacion...
curl -s -o nul -w "%%{http_code}" http://localhost:8080/api/auth/test
if %errorlevel% equ 0 (
    echo ✅ Endpoint /api/auth/test: OK
) else (
    echo ❌ Endpoint /api/auth/test: ERROR
)

echo.
echo [2/5] Verificando endpoints de campos...
curl -s -o nul -w "%%{http_code}" http://localhost:8080/api/v1/campos
if %errorlevel% equ 0 (
    echo ✅ Endpoint /api/v1/campos: OK
) else (
    echo ❌ Endpoint /api/v1/campos: ERROR
)

echo.
echo [3/5] Verificando endpoints de insumos...
curl -s -o nul -w "%%{http_code}" http://localhost:8080/api/v1/insumos
if %errorlevel% equ 0 (
    echo ✅ Endpoint /api/v1/insumos: OK
) else (
    echo ❌ Endpoint /api/v1/insumos: ERROR
)

echo.
echo [4/5] Verificando endpoints de roles...
curl -s -o nul -w "%%{http_code}" http://localhost:8080/api/roles
if %errorlevel% equ 0 (
    echo ✅ Endpoint /api/roles: OK
) else (
    echo ❌ Endpoint /api/roles: ERROR
)

echo.
echo [5/5] Verificando endpoints publicos...
curl -s -o nul -w "%%{http_code}" http://localhost:8080/api/public/insumos
if %errorlevel% equ 0 (
    echo ✅ Endpoint /api/public/insumos: OK
) else (
    echo ❌ Endpoint /api/public/insumos: ERROR
)

echo.
echo ========================================
echo VERIFICACION COMPLETADA
echo ========================================
echo.
echo Si todos los endpoints muestran "OK", la compatibilidad esta garantizada.
echo Si hay errores, verificar que el backend este ejecutandose en puerto 8080.
echo.
pause
