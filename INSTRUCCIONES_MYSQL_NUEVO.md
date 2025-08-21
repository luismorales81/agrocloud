# 🔧 Instrucciones para Configurar MySQL - Nueva Instalación

## 📋 Datos de Conexión Actualizados

- **Host**: localhost
- **Puerto**: 3306
- **Base de datos**: agrocloud
- **Usuario**: agrocloudbd
- **Contraseña**: Jones1212

## 🚀 Pasos para Configurar la Base de Datos

### Paso 1: Verificar MySQL en XAMPP
1. Abre XAMPP Control Panel
2. Inicia MySQL
3. Verifica que esté corriendo en el puerto 3306

### Paso 2: Crear Base de Datos
1. Abre phpMyAdmin: http://localhost/phpmyadmin
2. Inicia sesión con:
   - Usuario: `agrocloudbd`
   - Contraseña: `Jones1212`
3. Haz clic en "Nueva" o "New"
4. Nombre de la base de datos: `agrocloud`
5. Collation: `utf8mb4_unicode_ci`
6. Haz clic en "Crear"

### Paso 3: Ejecutar Script SQL
1. Selecciona la base de datos `agrocloud`
2. Ve a la pestaña "SQL"
3. Copia y pega todo el contenido del archivo `setup-mysql-local.sql`
4. Haz clic en "Continuar"

### Paso 4: Verificar Instalación
El script creará:
- ✅ 8 tablas del sistema
- ✅ 3 roles (ADMIN, TECNICO, PRODUCTOR)
- ✅ 3 usuarios de prueba
- ✅ Datos de ejemplo para todos los módulos
- ✅ Índices para optimizar rendimiento

## 👥 Usuarios de Prueba Creados

| Usuario | Contraseña | Rol | Descripción |
|---------|------------|-----|-------------|
| `admin` | `admin123` | ADMIN | Administrador del sistema |
| `tecnico` | `admin123` | TECNICO | Técnico agrícola |
| `productor` | `admin123` | PRODUCTOR | Productor agrícola |

## 📊 Datos de Ejemplo Incluidos

### Campos
- Campo Norte (50.5 ha)
- Campo Sur (30.2 ha)
- Campo Este (25.8 ha)

### Lotes
- 6 lotes distribuidos en los campos
- Diferentes tipos de suelo
- Áreas específicas para cada cultivo

### Cultivos
- Soja (DM 53i54)
- Maíz (DK 72-10)
- Trigo (Baguette 19)
- Girasol (Paraíso 33)
- Sorgo (DK 46-15)

### Insumos
- Fertilizantes (Urea, Fosfato Diamónico)
- Herbicidas (Glifosato)
- Semillas certificadas
- Fungicidas e insecticidas

### Maquinaria
- Tractor John Deere 5075E
- Sembradora MaxEmerge 5
- Pulverizadora Jacto 600L
- Cosechadora New Holland CR
- Arado de discos

### Labores
- Siembras
- Fertilizaciones
- Pulverizaciones
- Cosechas

## 🔍 Verificar Configuración

### En phpMyAdmin
1. Ve a la base de datos `agrocloud`
2. Verifica que aparezcan todas las tablas
3. Revisa que los datos estén cargados

### Comandos de Verificación
```sql
-- Verificar tablas creadas
SHOW TABLES;

-- Verificar usuarios
SELECT username, email, first_name, last_name FROM users;

-- Verificar roles
SELECT name, description FROM roles;

-- Verificar campos
SELECT name, location, area_hectares FROM fields;

-- Verificar lotes
SELECT name, field_id, area_hectares FROM plots;
```

## 🚀 Ejecutar el Proyecto

Una vez configurada la base de datos:

### Opción 1: Script Automático
```bash
start-project.bat
```

### Opción 2: Manual
```bash
# Backend
cd agrogestion-backend
.\mvnw spring-boot:run -Dspring.profiles.active=mysql

# Frontend (en otra terminal)
cd agrogestion-frontend
npm run dev
```

## 🌐 URLs de Acceso

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **phpMyAdmin**: http://localhost/phpmyadmin

## 🔧 Troubleshooting

### Error de Conexión
- Verificar que MySQL esté corriendo en XAMPP
- Verificar credenciales en `application-mysql.properties`
- Verificar que la base de datos `agrocloud` exista

### Error de Permisos
- Verificar que el usuario `agrocloudbd` tenga permisos en la base de datos
- Verificar que la contraseña sea correcta

### Error de Puerto
- Verificar que MySQL esté en el puerto 3306
- Verificar que no haya conflictos de puertos

---

**¡La base de datos está lista para usar con el sistema AgroGestion! 🚀**
