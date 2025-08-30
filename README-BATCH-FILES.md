# Archivos Batch para AgroCloud

## 📁 Archivos Disponibles

### 🚀 `start-project-mysql.bat`
**Inicia todos los servicios del proyecto con MySQL**

**Funcionalidades:**
- ✅ Verifica que MySQL esté ejecutándose
- ✅ Verifica que Java y Node.js estén instalados
- ✅ Termina procesos existentes en puertos 8080 y 3000
- ✅ Inicia el backend con perfil MySQL
- ✅ Inicia el frontend
- ✅ Muestra URLs y credenciales

**Uso:**
```bash
start-project-mysql.bat
```

### 🛑 `stop-project.bat`
**Detiene todos los servicios del proyecto**

**Funcionalidades:**
- ✅ Termina procesos en puerto 8080 (Backend)
- ✅ Termina procesos en puerto 3000 (Frontend)
- ✅ Termina procesos Java relacionados
- ✅ Termina procesos Node.js relacionados

**Uso:**
```bash
stop-project.bat
```

### 📊 `check-status.bat`
**Verifica el estado de todos los servicios**

**Funcionalidades:**
- ✅ Verifica estado de MySQL (puerto 3306)
- ✅ Verifica estado del Backend (puerto 8080)
- ✅ Verifica estado del Frontend (puerto 3000)
- ✅ Prueba conectividad del Backend
- ✅ Muestra resumen del estado

**Uso:**
```bash
check-status.bat
```

## 🎯 Uso Recomendado

### 1. Primera vez o después de cambios:
```bash
# 1. Detener servicios existentes
stop-project.bat

# 2. Iniciar todos los servicios
start-project-mysql.bat
```

### 2. Verificar estado:
```bash
check-status.bat
```

### 3. Reiniciar rápidamente:
```bash
start-project-mysql.bat
```

## 🔧 Requisitos Previos

### Software Necesario:
- ✅ **XAMPP** con MySQL ejecutándose
- ✅ **Java 17** o superior
- ✅ **Node.js 16** o superior
- ✅ **Maven** (incluido en el proyecto)

### Configuración de Base de Datos:
- ✅ Base de datos: `agroclouddb`
- ✅ Usuario: `agrocloudbd`
- ✅ Contraseña: `Jones1212`
- ✅ Tabla `ingresos` creada

## 🌐 URLs del Sistema

Una vez iniciados los servicios:

| Servicio | URL | Descripción |
|----------|-----|-------------|
| Frontend | http://localhost:3000 | Interfaz principal |
| Backend | http://localhost:8080 | API REST |
| Swagger | http://localhost:8080/swagger-ui.html | Documentación API |
| phpMyAdmin | http://localhost/phpmyadmin | Gestión de base de datos |

## 🔑 Credenciales por Defecto

| Campo | Valor |
|-------|-------|
| Email | admin@agrocloud.com |
| Password | admin123 |

## 💡 Nueva Funcionalidad

Con la tabla `ingresos` creada, ahora tienes acceso a:

- **Gestión de Ingresos**: Registrar y gestionar ingresos/beneficios
- **Balance de Costos y Beneficios**: Análisis financiero completo
- **Reportes Financieros**: Estadísticas y reportes detallados

## ⚠️ Solución de Problemas

### Error: "MySQL no está ejecutándose"
1. Abre XAMPP Control Panel
2. Inicia MySQL
3. Verifica que esté en puerto 3306

### Error: "Java no está instalado"
1. Descarga e instala Java 17 o superior
2. Agrega Java al PATH del sistema
3. Reinicia la terminal

### Error: "Node.js no está instalado"
1. Descarga e instala Node.js 16 o superior
2. Agrega Node.js al PATH del sistema
3. Reinicia la terminal

### Error: "Puerto ya en uso"
1. Ejecuta `stop-project.bat`
2. Espera unos segundos
3. Ejecuta `start-project-mysql.bat`

### Error: "Backend no responde"
1. Verifica que MySQL esté ejecutándose
2. Verifica que la tabla `ingresos` esté creada
3. Revisa los logs del backend en la ventana correspondiente

## 📝 Logs y Debugging

### Ver logs del Backend:
- Los logs aparecen en la ventana "Backend AgroCloud MySQL"
- Busca errores de conexión a la base de datos
- Verifica que el perfil MySQL esté activo

### Ver logs del Frontend:
- Los logs aparecen en la ventana "Frontend AgroCloud"
- Busca errores de conexión al backend
- Verifica que las dependencias estén instaladas

## 🔄 Flujo de Trabajo Recomendado

1. **Inicio del día:**
   ```bash
   start-project-mysql.bat
   ```

2. **Verificar estado:**
   ```bash
   check-status.bat
   ```

3. **Durante el desarrollo:**
   - Los servicios se reinician automáticamente
   - Usa `check-status.bat` para verificar

4. **Fin del día:**
   ```bash
   stop-project.bat
   ```

## 📞 Soporte

Si encuentras problemas:

1. Ejecuta `check-status.bat` para diagnosticar
2. Revisa los logs en las ventanas de los servicios
3. Verifica que todos los requisitos estén cumplidos
4. Ejecuta `stop-project.bat` y luego `start-project-mysql.bat`
