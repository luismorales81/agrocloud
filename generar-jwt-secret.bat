@echo off
cls
echo ============================================================
echo  Generador de JWT Secret
echo  AgroGestion v2.0
echo ============================================================
echo.
echo Este script genera un JWT_SECRET seguro para usar en
echo las variables de entorno de Railway.
echo.
echo Generando secret...
echo.

powershell -Command "[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }) -as [byte[]])"

echo.
echo ============================================================
echo.
echo Copia el texto de arriba y usalo como JWT_SECRET en Railway.
echo.
echo IMPORTANTE:
echo - Usa un secret DIFERENTE para Testing y Production
echo - Guarda los secrets en un lugar seguro
echo - Nunca los compartas publicamente
echo.
echo ============================================================
pause

