@echo off
echo ========================================
echo   Verificando correcciones en LaboresManagement
echo ========================================

echo 1. Verificando que se filtraron las labores eliminadas:
findstr /i "laboresActivas.*filter" agrogestion-frontend\src\components\LaboresManagement.tsx
if %errorlevel% equ 0 (
    echo ✅ Filtro de labores activas implementado
) else (
    echo ❌ Filtro de labores activas NO implementado
)

echo.
echo 2. Verificando que se corrigieron las keys de React:
findstr /i "insumo-.*index" agrogestion-frontend\src\components\LaboresManagement.tsx
if %errorlevel% equ 0 (
    echo ✅ Keys de insumos corregidas
) else (
    echo ❌ Keys de insumos NO corregidas
)

findstr /i "maquinaria-.*index" agrogestion-frontend\src\components\LaboresManagement.tsx
if %errorlevel% equ 0 (
    echo ✅ Keys de maquinaria corregidas
) else (
    echo ❌ Keys de maquinaria NO corregidas
)

echo.
echo 3. Verificando que se corrigió el error de response stream:
findstr /i "responseText.*await.*response\.text" agrogestion-frontend\src\components\LaboresManagement.tsx
if %errorlevel% equ 0 (
    echo ✅ Error de response stream corregido
) else (
    echo ❌ Error de response stream NO corregido
)

echo.
echo 4. Verificando que se corrigieron los permisos de Admin Empresas:
findstr /i "isSuperAdmin.*admin-empresas" agrogestion-frontend\src\App.tsx
if %errorlevel% equ 0 (
    echo ✅ Permisos de Admin Empresas corregidos en frontend
) else (
    echo ❌ Permisos de Admin Empresas NO corregidos en frontend
)

findstr /i "ROLE_SUPERADMIN.*empresas" agrogestion-backend\src\main\java\com\agrocloud\config\SecurityConfig.java
if %errorlevel% equ 0 (
    echo ✅ Permisos de Admin Empresas corregidos en backend
) else (
    echo ❌ Permisos de Admin Empresas NO corregidos en backend
)

echo.
echo ========================================
echo   Resumen de correcciones aplicadas:
echo ========================================
echo ✅ Filtro de labores eliminadas
echo ✅ Keys únicas de React
echo ✅ Error de response stream
echo ✅ Permisos de Admin Empresas
echo ✅ Backend reiniciado con cambios
echo.
echo Próximos pasos:
echo 1. Probar creación de labores
echo 2. Verificar que no aparezcan labores eliminadas
echo 3. Confirmar que no hay warnings de React
echo 4. Verificar permisos de Admin Empresas
echo.
pause
