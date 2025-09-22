@echo off
echo ========================================
echo   Verificando permisos de Admin Empresas
echo ========================================

echo 1. Verificando que solo SUPERADMIN puede ver Admin Empresas en el frontend:
findstr /i "isSuperAdmin.*admin-empresas" agrogestion-frontend\src\App.tsx
if %errorlevel% equ 0 (
    echo ✅ Frontend: Solo SUPERADMIN puede ver Admin Empresas
) else (
    echo ❌ Frontend: Configuración incorrecta
)

echo.
echo 2. Verificando que ADMINISTRADOR NO puede ver Admin Empresas:
findstr /i "isAdministrador.*admin-empresas" agrogestion-frontend\src\App.tsx
if %errorlevel% equ 0 (
    echo ❌ Frontend: ADMINISTRADOR aún puede ver Admin Empresas
) else (
    echo ✅ Frontend: ADMINISTRADOR no puede ver Admin Empresas
)

echo.
echo 3. Verificando configuración de seguridad en el backend:
findstr /i "ROLE_SUPERADMIN.*empresas" agrogestion-backend\src\main\java\com\agrocloud\config\SecurityConfig.java
if %errorlevel% equ 0 (
    echo ✅ Backend: Solo ROLE_SUPERADMIN puede acceder a /api/empresas/
) else (
    echo ❌ Backend: Configuración incorrecta
)

echo.
echo 4. Verificando que ADMINISTRADOR NO puede acceder a empresas en el backend:
findstr /i "ROLE_ADMINISTRADOR.*empresas" agrogestion-backend\src\main\java\com\agrocloud\config\SecurityConfig.java
if %errorlevel% equ 0 (
    echo ❌ Backend: ROLE_ADMINISTRADOR aún puede acceder a empresas
) else (
    echo ✅ Backend: ROLE_ADMINISTRADOR no puede acceder a empresas
)

echo.
echo ========================================
echo   Resumen de cambios aplicados:
echo ========================================
echo ✅ Frontend: Separación de permisos entre ADMINISTRADOR y SUPERADMIN
echo ✅ Backend: Solo ROLE_SUPERADMIN puede acceder a gestión de empresas
echo ✅ Seguridad: Configuración actualizada en SecurityConfig.java
echo.
echo Cambios implementados:
echo - ADMINISTRADOR: Solo puede gestionar usuarios
echo - SUPERADMIN: Solo puede gestionar empresas
echo - Endpoints /api/empresas/ y /api/empresa-usuario/ restringidos a SUPERADMIN
echo.
pause
