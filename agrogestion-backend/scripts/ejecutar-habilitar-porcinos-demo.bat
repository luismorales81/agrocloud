@echo off
setlocal
cd /d "%~dp0.."
echo Habilitando modulo Porcinos (pigs) para empresa demo...
mysql -u root -p123456 agrocloud < scripts\HABILITAR_PORCINOS_EMPRESA_DEMO.sql
if errorlevel 1 (
  echo Error al ejecutar el script. Verifique MySQL y credenciales.
  exit /b 1
)
echo Listo.
endlocal
