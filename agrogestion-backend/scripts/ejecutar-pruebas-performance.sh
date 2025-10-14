#!/bin/bash

echo "========================================"
echo "PRUEBAS DE PERFORMANCE - AGROGESTION"
echo "========================================"
echo

# Verificar que Java esté instalado
if ! command -v java &> /dev/null; then
    echo "❌ Java no está instalado o no está en el PATH"
    echo "Por favor instala Java 17 o superior"
    exit 1
fi

# Verificar que Maven esté instalado
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven no está instalado o no está en el PATH"
    echo "Por favor instala Maven"
    exit 1
fi

echo "✅ Java y Maven están instalados"
echo

# Crear directorio para reportes
mkdir -p performance-reports

echo "[1/6] Compilando aplicación..."
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ Error al compilar la aplicación"
    exit 1
fi
echo "✅ Aplicación compilada exitosamente"
echo

echo "[2/6] Iniciando aplicación..."
nohup java -jar target/agrocloud-backend-*.jar --spring.profiles.active=test --server.port=8080 > app.log 2>&1 &
APP_PID=$!

# Esperar a que la aplicación esté lista
echo "⏳ Esperando a que la aplicación esté lista..."
for i in {1..30}; do
    if curl -f http://localhost:8080/api/auth/test > /dev/null 2>&1; then
        echo "✅ Aplicación iniciada y respondiendo"
        break
    fi
    echo "Esperando... ($i/30)"
    sleep 10
done

if [ $i -eq 30 ]; then
    echo "❌ La aplicación no está respondiendo en el puerto 8080"
    echo "Verifica que la aplicación se haya iniciado correctamente"
    kill $APP_PID 2>/dev/null
    exit 1
fi
echo

echo "[3/6] Descargando JMeter..."
if [ ! -d "apache-jmeter-5.5" ]; then
    echo "Descargando Apache JMeter 5.5..."
    wget -q https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-5.5.tgz
    if [ $? -ne 0 ]; then
        echo "❌ Error al descargar JMeter"
        kill $APP_PID 2>/dev/null
        exit 1
    fi
    
    echo "Extrayendo JMeter..."
    tar -xzf apache-jmeter-5.5.tgz
    if [ $? -ne 0 ]; then
        echo "❌ Error al extraer JMeter"
        kill $APP_PID 2>/dev/null
        exit 1
    fi
    
    rm jmeter.tgz
fi
echo "✅ JMeter disponible"
echo

echo "[4/6] Creando plan de pruebas JMeter..."
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
echo "✅ Plan de pruebas creado"
echo

echo "[5/6] Ejecutando pruebas de performance..."
apache-jmeter-5.5/bin/jmeter -n -t performance-test.jmx -l performance-reports/performance-results.jtl -e -o performance-reports/performance-report/
if [ $? -ne 0 ]; then
    echo "❌ Error al ejecutar las pruebas de performance"
    kill $APP_PID 2>/dev/null
    exit 1
fi
echo "✅ Pruebas de performance completadas"
echo

echo "[6/6] Generando reporte de resumen..."
cat > performance-reports/performance-summary.md << EOF
## Reporte de Performance - AgroGestion
### Fecha: $(date)
### Configuración:
- Usuarios concurrentes: 20
- Tiempo de ramp-up: 10 segundos
- Iteraciones por usuario: 5

### Endpoints probados:
- GET /api/auth/test
- GET /api/v1/campos
- GET /api/v1/insumos

### Resultados:
Los resultados detallados están disponibles en el directorio performance-reports/performance-report/
EOF

echo "✅ Reporte de resumen generado"
echo

echo "========================================"
echo "🎉 PRUEBAS DE PERFORMANCE COMPLETADAS"
echo "========================================"
echo
echo "📊 Reportes generados en: performance-reports/"
echo "📈 Reporte HTML: performance-reports/performance-report/index.html"
echo "📋 Resumen: performance-reports/performance-summary.md"
echo
echo "Para ver el reporte HTML, abre: performance-reports/performance-report/index.html"
echo

# Preguntar si quiere abrir el reporte
read -p "¿Deseas abrir el reporte HTML ahora? (s/n): " open_report
if [[ $open_report == "s" || $open_report == "S" ]]; then
    if command -v xdg-open &> /dev/null; then
        xdg-open performance-reports/performance-report/index.html
    elif command -v open &> /dev/null; then
        open performance-reports/performance-report/index.html
    else
        echo "No se pudo abrir el reporte automáticamente. Abre manualmente: performance-reports/performance-report/index.html"
    fi
fi

# Limpiar procesos
echo "🧹 Limpiando procesos..."
kill $APP_PID 2>/dev/null
sleep 5

echo
echo "Presiona Enter para continuar..."
read
