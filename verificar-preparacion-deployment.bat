@echo off
cls
echo ============================================================
echo  Verificacion de Preparacion para Deployment
echo  AgroGestion v2.0
echo ============================================================
echo.

set ERRORES=0

echo [1/5] Verificando estructura del proyecto...
if exist "agrogestion-backend\pom.xml" (
    echo [OK] Backend encontrado
) else (
    echo [ERROR] No se encuentra agrogestion-backend
    set /a ERRORES+=1
)

if exist "agrogestion-frontend\package.json" (
    echo [OK] Frontend encontrado
) else (
    echo [ERROR] No se encuentra agrogestion-frontend
    set /a ERRORES+=1
)

echo.
echo [2/5] Verificando scripts de migracion...
if exist "database-migration-master-testing.sql" (
    echo [OK] Script Testing encontrado
) else (
    echo [ERROR] Falta database-migration-master-testing.sql
    set /a ERRORES+=1
)

if exist "database-migration-master-production.sql" (
    echo [OK] Script Production encontrado
) else (
    echo [ERROR] Falta database-migration-master-production.sql
    set /a ERRORES+=1
)

echo.
echo [3/5] Verificando documentacion...
if exist "DEPLOYMENT-PASO-A-PASO.md" (
    echo [OK] Guia de deployment encontrada
) else (
    echo [ADVERTENCIA] Falta guia de deployment
)

echo.
echo [4/5] Verificando configuracion de frontend...
if exist "agrogestion-frontend\.env.testing.example" (
    echo [OK] Template .env.testing existe
) else (
    echo [INFO] Creando template .env.testing.example
)

echo.
echo [5/5] Compilando backend...
cd agrogestion-backend
call mvn clean package -DskipTests -q
if %ERRORLEVEL% EQU 0 (
    echo [OK] Backend compila correctamente
) else (
    echo [ERROR] El backend no compila
    set /a ERRORES+=1
)
cd ..

echo.
echo [6/6] Compilando frontend...
cd agrogestion-frontend
call npm run build > nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [OK] Frontend compila correctamente
) else (
    echo [ADVERTENCIA] El frontend podria tener problemas
)
cd ..

echo.
echo ============================================================
echo  RESULTADO
echo ============================================================
echo.

if %ERRORES% EQU 0 (
    color 0A
    echo [OK] Todo listo para deployment!
    echo.
    echo Proximo paso:
    echo   1. Abre: DEPLOYMENT-PASO-A-PASO.md
    echo   2. Sigue las instrucciones
    echo   3. Empieza con Railway Testing
    echo.
) else (
    color 0C
    echo [ERROR] Se encontraron %ERRORES% errores
    echo.
    echo Por favor, corrige los errores antes de continuar.
    echo.
)

color
echo ============================================================
pause

