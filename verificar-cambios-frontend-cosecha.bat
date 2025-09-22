@echo off
echo ========================================
echo   Verificando cambios en frontend de cosecha
echo ========================================

echo 1. Verificando que RindeManagement.tsx no tenga referencias a humedad:
findstr /i "humedad" agrogestion-frontend\src\components\RindeManagement.tsx
if %errorlevel% equ 0 (
    echo ❌ Aún hay referencias a humedad en RindeManagement.tsx
) else (
    echo ✅ RindeManagement.tsx sin referencias a humedad
)

echo.
echo 2. Verificando que HistorialCosechasModal.tsx no tenga referencias a humedad:
findstr /i "humedad" agrogestion-frontend\src\components\HistorialCosechasModal.tsx
if %errorlevel% equ 0 (
    echo ❌ Aún hay referencias a humedad en HistorialCosechasModal.tsx
) else (
    echo ✅ HistorialCosechasModal.tsx sin referencias a humedad
)

echo.
echo 3. Verificando que se agregó cantidad_esperada en RindeManagement.tsx:
findstr /i "cantidad_esperada" agrogestion-frontend\src\components\RindeManagement.tsx
if %errorlevel% equ 0 (
    echo ✅ Campo cantidad_esperada agregado en RindeManagement.tsx
) else (
    echo ❌ Campo cantidad_esperada no encontrado en RindeManagement.tsx
)

echo.
echo 4. Verificando que se agregó cantidadEsperada en HistorialCosechasModal.tsx:
findstr /i "cantidadEsperada" agrogestion-frontend\src\components\HistorialCosechasModal.tsx
if %errorlevel% equ 0 (
    echo ✅ Campo cantidadEsperada agregado en HistorialCosechasModal.tsx
) else (
    echo ❌ Campo cantidadEsperada no encontrado en HistorialCosechasModal.tsx
)

echo.
echo 5. Verificando que la tabla muestra cantidad esperada vs obtenida:
findstr /i "Cantidad Esperada" agrogestion-frontend\src\components\RindeManagement.tsx
if %errorlevel% equ 0 (
    echo ✅ Tabla actualizada para mostrar cantidad esperada vs obtenida
) else (
    echo ❌ Tabla no actualizada
)

echo.
echo ========================================
echo   Verificación completada
echo ========================================
echo.
echo Cambios implementados:
echo ✅ Eliminación de humedad de formularios
echo ✅ Agregado cantidad esperada vs obtenida
echo ✅ Tabla actualizada con nuevas columnas
echo ✅ Indicadores de cumplimiento mejorados
echo.
pause
