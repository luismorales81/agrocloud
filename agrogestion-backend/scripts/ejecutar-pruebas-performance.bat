@echo off
echo ========================================
echo PRUEBAS DE PERFORMANCE - AGROGESTION
echo ========================================
echo.

REM Verificar que Java esté instalado
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java no está instalado o no está en el PATH
    echo Por favor instala Java 17 o superior
    pause
    exit /b 1
)

REM Verificar que Maven esté instalado
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven no está instalado o no está en el PATH
    echo Por favor instala Maven
    pause
    exit /b 1
)

echo ✅ Java y Maven están instalados
echo.

REM Crear directorio para reportes
if not exist "performance-reports" mkdir performance-reports

echo [1/6] Compilando aplicación...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ❌ Error al compilar la aplicación
    pause
    exit /b 1
)
echo ✅ Aplicación compilada exitosamente
echo.

echo [2/6] Iniciando aplicación...
start "AgroGestion App" java -jar target\agrocloud-backend-*.jar --spring.profiles.active=test --server.port=8080

REM Esperar a que la aplicación esté lista
echo ⏳ Esperando a que la aplicación esté lista...
timeout /t 30 /nobreak >nul

REM Verificar que la aplicación esté funcionando
curl -s http://localhost:8080/api/auth/test >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ La aplicación no está respondiendo en el puerto 8080
    echo Verifica que la aplicación se haya iniciado correctamente
    pause
    exit /b 1
)
echo ✅ Aplicación iniciada y respondiendo
echo.

echo [3/6] Descargando JMeter...
if not exist "apache-jmeter-5.5" (
    echo Descargando Apache JMeter 5.5...
    powershell -Command "Invoke-WebRequest -Uri 'https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-5.5.tgz' -OutFile 'jmeter.tgz'"
    if %errorlevel% neq 0 (
        echo ❌ Error al descargar JMeter
        pause
        exit /b 1
    )
    
    echo Extrayendo JMeter...
    tar -xzf jmeter.tgz
    if %errorlevel% neq 0 (
        echo ❌ Error al extraer JMeter
        pause
        exit /b 1
    )
    
    del jmeter.tgz
)
echo ✅ JMeter disponible
echo.

echo [4/6] Creando plan de pruebas JMeter...
cat > performance-test.jmx << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0" jmeter="5.5">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="AgroGestion Performance Test" enabled="true">
      <stringProp name="TestPlan.comments">Plan de pruebas de performance para AgroGestion</stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.tearDown_on_shutdown">true</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
      <elementProp name="TestPlan.arguments" elementType="Arguments" guiclass="ArgumentsPanel" testclass="Arguments" testname="User Defined Variables" enabled="true">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
      <stringProp name="TestPlan.user_define_classpath"></stringProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="Thread Group" enabled="true">
        <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController" guiclass="LoopControllerGui" testclass="LoopController" testname="Loop Controller" enabled="true">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <stringProp name="LoopController.loops">5</stringProp>
        </elementProp>
        <stringProp name="ThreadGroup.num_threads">20</stringProp>
        <stringProp name="ThreadGroup.ramp_time">10</stringProp>
        <boolProp name="ThreadGroup.scheduler">false</boolProp>
        <stringProp name="ThreadGroup.duration"></stringProp>
        <stringProp name="ThreadGroup.delay"></stringProp>
        <boolProp name="ThreadGroup.same_user_on_next_iteration">true</boolProp>
      </ThreadGroup>
      <hashTree>
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="GET /api/auth/test" enabled="true">
          <elementProp name="HTTPsampler.Arguments" elementType="Arguments" guiclass="HTTPArgumentsPanel" testclass="Arguments" testname="User Defined Variables" enabled="true">
            <collectionProp name="Arguments.arguments"/>
          </elementProp>
          <stringProp name="HTTPSampler.domain">localhost</stringProp>
          <stringProp name="HTTPSampler.port">8080</stringProp>
          <stringProp name="HTTPSampler.protocol">http</stringProp>
          <stringProp name="HTTPSampler.contentEncoding"></stringProp>
          <stringProp name="HTTPSampler.path">/api/auth/test</stringProp>
          <stringProp name="HTTPSampler.method">GET</stringProp>
          <boolProp name="HTTPSampler.follow_redirects">true</boolProp>
          <boolProp name="HTTPSampler.auto_redirects">false</boolProp>
          <boolProp name="HTTPSampler.use_keepalive">true</boolProp>
          <boolProp name="HTTPSampler.DO_MULTIPART_POST">false</boolProp>
          <stringProp name="HTTPSampler.embedded_url_re"></stringProp>
          <stringProp name="HTTPSampler.connect_timeout"></stringProp>
          <stringProp name="HTTPSampler.response_timeout"></stringProp>
        </HTTPSamplerProxy>
        <hashTree>
          <ResponseAssertion guiclass="AssertionGui" testclass="ResponseAssertion" testname="Response Assertion" enabled="true">
            <collectionProp name="Asserion.test_strings">
              <stringProp name="49586">200</stringProp>
            </collectionProp>
            <stringProp name="Assertion.custom_message"></stringProp>
            <stringProp name="Assertion.test_field">Assertion.response_code</stringProp>
            <boolProp name="Assertion.assume_success">false</boolProp>
            <intProp name="Assertion.test_type">1</intProp>
          </ResponseAssertion>
          <hashTree/>
        </hashTree>
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="GET /api/v1/campos" enabled="true">
          <elementProp name="HTTPsampler.Arguments" elementType="Arguments" guiclass="HTTPArgumentsPanel" testclass="Arguments" testname="User Defined Variables" enabled="true">
            <collectionProp name="Arguments.arguments"/>
          </elementProp>
          <stringProp name="HTTPSampler.domain">localhost</stringProp>
          <stringProp name="HTTPSampler.port">8080</stringProp>
          <stringProp name="HTTPSampler.protocol">http</stringProp>
          <stringProp name="HTTPSampler.contentEncoding"></stringProp>
          <stringProp name="HTTPSampler.path">/api/v1/campos</stringProp>
          <stringProp name="HTTPSampler.method">GET</stringProp>
          <boolProp name="HTTPSampler.follow_redirects">true</boolProp>
          <boolProp name="HTTPSampler.auto_redirects">false</boolProp>
          <boolProp name="HTTPSampler.use_keepalive">true</boolProp>
          <boolProp name="HTTPSampler.DO_MULTIPART_POST">false</boolProp>
          <stringProp name="HTTPSampler.embedded_url_re"></stringProp>
          <stringProp name="HTTPSampler.connect_timeout"></stringProp>
          <stringProp name="HTTPSampler.response_timeout"></stringProp>
        </HTTPSamplerProxy>
        <hashTree>
          <ResponseAssertion guiclass="AssertionGui" testclass="ResponseAssertion" testname="Response Assertion" enabled="true">
            <collectionProp name="Asserion.test_strings">
              <stringProp name="49586">200</stringProp>
            </collectionProp>
            <stringProp name="Assertion.custom_message"></stringProp>
            <stringProp name="Assertion.test_field">Assertion.response_code</stringProp>
            <boolProp name="Assertion.assume_success">false</boolProp>
            <intProp name="Assertion.test_type">1</intProp>
          </ResponseAssertion>
          <hashTree/>
        </hashTree>
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="GET /api/v1/insumos" enabled="true">
          <elementProp name="HTTPsampler.Arguments" elementType="Arguments" guiclass="HTTPArgumentsPanel" testclass="Arguments" testname="User Defined Variables" enabled="true">
            <collectionProp name="Arguments.arguments"/>
          </elementProp>
          <stringProp name="HTTPSampler.domain">localhost</stringProp>
          <stringProp name="HTTPSampler.port">8080</stringProp>
          <stringProp name="HTTPSampler.protocol">http</stringProp>
          <stringProp name="HTTPSampler.contentEncoding"></stringProp>
          <stringProp name="HTTPSampler.path">/api/v1/insumos</stringProp>
          <stringProp name="HTTPSampler.method">GET</stringProp>
          <boolProp name="HTTPSampler.follow_redirects">true</boolProp>
          <boolProp name="HTTPSampler.auto_redirects">false</boolProp>
          <boolProp name="HTTPSampler.use_keepalive">true</boolProp>
          <boolProp name="HTTPSampler.DO_MULTIPART_POST">false</boolProp>
          <stringProp name="HTTPSampler.embedded_url_re"></stringProp>
          <stringProp name="HTTPSampler.connect_timeout"></stringProp>
          <stringProp name="HTTPSampler.response_timeout"></stringProp>
        </HTTPSamplerProxy>
        <hashTree>
          <ResponseAssertion guiclass="AssertionGui" testclass="ResponseAssertion" testname="Response Assertion" enabled="true">
            <collectionProp name="Asserion.test_strings">
              <stringProp name="49586">200</stringProp>
            </collectionProp>
            <stringProp name="Assertion.custom_message"></stringProp>
            <stringProp name="Assertion.test_field">Assertion.response_code</stringProp>
            <boolProp name="Assertion.assume_success">false</boolProp>
            <intProp name="Assertion.test_type">1</intProp>
          </ResponseAssertion>
          <hashTree/>
        </hashTree>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
EOF
echo ✅ Plan de pruebas creado
echo.

echo [5/6] Ejecutando pruebas de performance...
apache-jmeter-5.5\bin\jmeter.bat -n -t performance-test.jmx -l performance-reports\performance-results.jtl -e -o performance-reports\performance-report\
if %errorlevel% neq 0 (
    echo ❌ Error al ejecutar las pruebas de performance
    pause
    exit /b 1
)
echo ✅ Pruebas de performance completadas
echo.

echo [6/6] Generando reporte de resumen...
echo ## Reporte de Performance - AgroGestion > performance-reports\performance-summary.md
echo ### Fecha: %date% %time% >> performance-reports\performance-summary.md
echo ### Configuracion: >> performance-reports\performance-summary.md
echo - Usuarios concurrentes: 20 >> performance-reports\performance-summary.md
echo - Tiempo de ramp-up: 10 segundos >> performance-reports\performance-summary.md
echo - Iteraciones por usuario: 5 >> performance-reports\performance-summary.md
echo. >> performance-reports\performance-summary.md
echo ### Endpoints probados: >> performance-reports\performance-summary.md
echo - GET /api/auth/test >> performance-reports\performance-summary.md
echo - GET /api/v1/campos >> performance-reports\performance-summary.md
echo - GET /api/v1/insumos >> performance-reports\performance-summary.md
echo. >> performance-reports\performance-summary.md
echo ### Resultados: >> performance-reports\performance-summary.md
echo Los resultados detallados estan disponibles en el directorio performance-reports\performance-report\ >> performance-reports\performance-summary.md

echo ✅ Reporte de resumen generado
echo.

echo ========================================
echo 🎉 PRUEBAS DE PERFORMANCE COMPLETADAS
echo ========================================
echo.
echo 📊 Reportes generados en: performance-reports\
echo 📈 Reporte HTML: performance-reports\performance-report\index.html
echo 📋 Resumen: performance-reports\performance-summary.md
echo.
echo Para ver el reporte HTML, abre: performance-reports\performance-report\index.html
echo.

REM Preguntar si quiere abrir el reporte
set /p open_report="¿Deseas abrir el reporte HTML ahora? (s/n): "
if /i "%open_report%"=="s" (
    start performance-reports\performance-report\index.html
)

echo.
echo Presiona cualquier tecla para continuar...
pause >nul
