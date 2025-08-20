# 🔧 Guía Rápida: Instalación de Maven en Windows

## 🚀 **Opción 1: Instalación Automática (Recomendada)**

1. **Ejecutar el script automático:**
   ```cmd
   # Doble clic en el archivo
   install-maven.bat
   ```

2. **Reiniciar la terminal** después de la instalación

3. **Verificar instalación:**
   ```cmd
   mvn --version
   ```

## 📥 **Opción 2: Instalación Manual**

### Paso 1: Descargar Maven
1. Ve a: https://maven.apache.org/download.cgi
2. Descarga: `apache-maven-3.9.6-bin.zip`

### Paso 2: Extraer Maven
1. Crea la carpeta: `C:\Program Files\Apache\maven`
2. Extrae el contenido del ZIP en esa carpeta
3. Deberías tener: `C:\Program Files\Apache\maven\apache-maven-3.9.6`

### Paso 3: Configurar Variables de Entorno
1. **Abrir Variables de Entorno:**
   - Presiona `Windows + R`
   - Escribe `sysdm.cpl` y presiona Enter
   - Pestaña "Opciones avanzadas"
   - Botón "Variables de entorno"

2. **Crear MAVEN_HOME:**
   - En "Variables del sistema" → "Nueva"
   - **Nombre:** `MAVEN_HOME`
   - **Valor:** `C:\Program Files\Apache\maven\apache-maven-3.9.6`

3. **Agregar al PATH:**
   - Encuentra la variable `Path`
   - Haz clic en "Editar"
   - Haz clic en "Nueva"
   - Agrega: `%MAVEN_HOME%\bin`
   - Haz clic en "Aceptar" en todas las ventanas

### Paso 4: Verificar Instalación
1. **Abrir una nueva terminal** (CMD o PowerShell)
2. **Ejecutar:**
   ```cmd
   mvn --version
   ```

## ✅ **Verificación Exitosa**

Si la instalación fue correcta, deberías ver algo como:
```
Apache Maven 3.9.6 (bc0240f3c744dd6b6ec2920b3cd08dcc295161ae)
Maven home: C:\Program Files\Apache\maven\apache-maven-3.9.6
Java version: 17.0.x, vendor: Oracle Corporation
```

## 🔧 **Solución de Problemas**

### Error: "mvn no se reconoce"
1. Verificar que las variables de entorno estén configuradas
2. Reiniciar la terminal
3. Verificar que la ruta en MAVEN_HOME sea correcta

### Error: "Java no se reconoce"
1. Instalar Java 17+ desde: https://adoptium.net/
2. Configurar JAVA_HOME en variables de entorno

### Error: "Acceso denegado"
1. Ejecutar CMD como administrador
2. Verificar permisos en `C:\Program Files\Apache`

## 🎯 **Después de Instalar Maven**

Una vez que Maven esté instalado, puedes ejecutar AgroGestion:

```cmd
# Ejecutar el sistema completo
start.bat

# O manualmente
cd agrogestion-backend
mvn spring-boot:run
```

---

**¡Maven instalado correctamente! 🎉**
