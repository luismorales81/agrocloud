# 🚀 Instrucciones de Deployment - AgroCloud

## 📋 Carpetas que NO se suben a GitHub

### 🚫 Frontend (React/Vite)
- **`node_modules/`** - Dependencias de npm (se instalan automáticamente)
- **`dist/`** - Archivos compilados (se generan en el servidor)
- **`dist-ssr/`** - Archivos SSR (se generan en el servidor)

### 🚫 Backend (Spring Boot)
- **`target/`** - Archivos compilados de Maven (se generan en el servidor)
- **`*.class`** - Archivos de bytecode Java
- **`*.jar`** - Archivos ejecutables (se generan en el servidor)

## ✅ Carpetas que SÍ se suben a GitHub

### 📁 Frontend
- **`src/`** - Código fuente React/TypeScript
- **`public/`** - Archivos estáticos (iconos, favicon, etc.)
- **`package.json`** - Dependencias y scripts
- **`vite.config.ts`** - Configuración de Vite
- **`tailwind.config.js`** - Configuración de Tailwind CSS
- **`tsconfig.json`** - Configuración de TypeScript

### 📁 Backend
- **`src/`** - Código fuente Java
- **`pom.xml`** - Dependencias y configuración de Maven
- **`Dockerfile`** - Configuración para contenedores
- **`railway.json`** - Configuración específica de Railway

## 🔄 Proceso de Build Automático

### 🌐 Vercel (Frontend)
1. **Detecta** el repositorio de GitHub
2. **Instala** dependencias: `npm install`
3. **Compila** el proyecto: `npm run build`
4. **Genera** la carpeta `dist/` automáticamente
5. **Sirve** los archivos desde `dist/`

### 🚂 Railway (Backend)
1. **Detecta** el repositorio de GitHub
2. **Instala** dependencias: `mvn dependency:resolve`
3. **Compila** el proyecto: `mvn clean package`
4. **Genera** la carpeta `target/` automáticamente
5. **Ejecuta** el JAR desde `target/`

## ⚠️ Importante

### ❌ NO subir a GitHub:
- `node_modules/` (muy pesada, se instala automáticamente)
- `dist/` (se genera en el servidor)
- `target/` (se genera en el servidor)
- Archivos de configuración local (`.env`, `application-local.properties`)

### ✅ SÍ subir a GitHub:
- Código fuente completo
- Archivos de configuración del proyecto
- Documentación
- Scripts de deployment
- Archivos estáticos (iconos, imágenes)

## 🎯 Beneficios de esta Configuración

1. **📦 Repositorio Ligero**: Sin archivos generados automáticamente
2. **🔄 Build Consistente**: Cada deployment genera archivos frescos
3. **🚀 Deployment Rápido**: Solo se sube código fuente
4. **🔒 Seguridad**: No se exponen archivos de configuración local
5. **📈 Escalabilidad**: Fácil de clonar y deployar en cualquier servidor

## 📝 Notas Adicionales

- **Vercel** y **Railway** detectan automáticamente el tipo de proyecto
- Los archivos `.gitignore` están configurados correctamente
- El proceso de build es completamente automático
- No es necesario subir archivos compilados manualmente

**¡El proyecto está optimizado para deployment automático!**
