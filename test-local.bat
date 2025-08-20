@echo off
echo === PROBANDO BUILD LOCAL ===

echo Verificando Java...
java -version

echo Verificando Maven Wrapper...
cd agrogestion-backend
.\mvnw.cmd -version

echo Limpiando build anterior...
if exist target rmdir /s /q target

echo Compilando aplicación...
.\mvnw.cmd clean package -DskipTests

echo Verificando JAR...
dir target\*.jar

echo Probando ejecución...
java -jar target\agrocloud-backend-1.0.0.jar

pause
