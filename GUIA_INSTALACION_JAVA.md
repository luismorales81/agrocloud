# ☕ Guía Rápida: Instalación de Java en Windows

## 🚀 **Opción 1: Instalación Automática (Recomendada)**

1. **Ejecutar el script automático:**
   ```cmd
   # Doble clic en el archivo
   install-java.bat
   ```

2. **Reiniciar la terminal** después de la instalación

3. **Verificar instalación:**
   ```cmd
   java --version
   ```

## 📥 **Opción 2: Instalación Manual**

### Paso 1: Descargar Java
1. Ve a: https://adoptium.net/
2. Descarga: **Eclipse Temurin JDK 17** para Windows x64
3. Ejecuta el instalador MSI

### Paso 2: Configurar Variables de Entorno
1. **Abrir Variables de Entorno:**
   - Presiona `Windows + R`
   - Escribe `sysdm.cpl` y presiona Enter
   - Pestaña "Opciones avanzadas"
   - Botón "Variables de entorno"

2. **Crear JAVA_HOME:**
   - En "Variables del sistema" → "Nueva"
   - **Nombre:** `JAVA_HOME`
   - **Valor:** `C:\Program Files\Eclipse Adoptium\jdk-17.0.10+7` (o la ruta donde se instaló)

3. **Agregar al PATH:**
   - Encuentra la variable `Path`
   - Haz clic en "Editar"
   - Haz clic en "Nueva"
   - Agrega: `%JAVA_HOME%\bin`
   - Haz clic en "Aceptar" en todas las ventanas

### Paso 3: Verificar Instalación
1. **Abrir una nueva terminal** (CMD o PowerShell)
2. **Ejecutar:**
   ```cmd
   java --version
   ```

## ✅ **Verificación Exitosa**

Si la instalación fue correcta, deberías ver algo como:
```
openjdk 17.0.10 2024-01-16
OpenJDK Runtime Environment Temurin-17.0.10+7 (build 17.0.10+7)
OpenJDK 64-Bit Server VM Temurin-17.0.10+7 (build 17.0.10+7, mixed mode, sharing)
```

## 🔧 **Solución de Problemas**

### Error: "java no se reconoce"
1. Verificar que las variables de entorno estén configuradas
2. Reiniciar la terminal
3. Verificar que la ruta en JAVA_HOME sea correcta

### Error: "Acceso denegado"
1. Ejecutar CMD como administrador
2. Verificar permisos en `C:\Program Files\Eclipse Adoptium`

### Error: "JAVA_HOME not found"
1. Verificar que JAVA_HOME esté configurado
2. Verificar que la ruta apunte a un directorio válido
3. Reiniciar la terminal

## 🎯 **Después de Instalar Java**

Una vez que Java esté instalado, puedes ejecutar AgroGestion:

```cmd
# Verificar Maven Wrapper
cd agrogestion-backend
.\mvnw.cmd --version

# Ejecutar el backend
.\mvnw.cmd spring-boot:run

# O usar el script completo
cd ..
start.bat
```

## 🌐 **URLs Importantes**

- **Java Download**: https://adoptium.net/
- **Java Documentation**: https://docs.oracle.com/en/java/

---

**¡Java instalado correctamente! ☕**
