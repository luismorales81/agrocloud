# Guía para Ejecutar el Script de Ingresos

## ⚠️ Problema Identificado
El error que estás viendo indica que estás intentando ejecutar un archivo de documentación (`.md`) como si fuera SQL. Esto causa errores de sintaxis.

## ✅ Solución Paso a Paso

### Paso 1: Abrir phpMyAdmin
1. Abre tu navegador web
2. Ve a `http://localhost/phpmyadmin` (o la URL de tu phpMyAdmin)
3. Inicia sesión con tus credenciales

### Paso 2: Seleccionar la Base de Datos
1. En el panel izquierdo, selecciona la base de datos `agrocloud` (o la que uses)
2. Haz clic en la pestaña "SQL" en la parte superior

### Paso 3: Ejecutar el Script Correcto
1. **NO copies el contenido del archivo `.md`**
2. **SÍ copia el contenido del archivo SQL correcto**

### Paso 4: Copiar el Script SQL
Copia y pega este script en la ventana SQL de phpMyAdmin:

```sql
-- Script limpio para crear la tabla de ingresos
-- Ejecutar este script en phpMyAdmin o cliente MySQL

-- Crear la tabla de ingresos
CREATE TABLE IF NOT EXISTS ingresos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    concepto VARCHAR(200) NOT NULL,
    descripcion VARCHAR(500),
    tipo_ingreso ENUM('VENTA_CULTIVO', 'VENTA_ANIMAL', 'SERVICIOS_AGRICOLAS', 'SUBSIDIOS', 'OTROS_INGRESOS') NOT NULL,
    fecha_ingreso DATE NOT NULL,
    monto DECIMAL(15,2) NOT NULL,
    unidad_medida VARCHAR(100),
    cantidad DECIMAL(10,2),
    cliente_comprador VARCHAR(200),
    estado ENUM('REGISTRADO', 'CONFIRMADO', 'CANCELADO') DEFAULT 'REGISTRADO',
    observaciones VARCHAR(1000),
    lote_id BIGINT,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_ingresos_usuario (usuario_id),
    INDEX idx_ingresos_fecha (fecha_ingreso),
    INDEX idx_ingresos_tipo (tipo_ingreso),
    INDEX idx_ingresos_estado (estado),
    INDEX idx_ingresos_lote (lote_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insertar datos de ejemplo
INSERT INTO ingresos (concepto, descripcion, tipo_ingreso, fecha_ingreso, monto, unidad_medida, cantidad, cliente_comprador, estado, lote_id, usuario_id) VALUES
('Venta de Soja', 'Venta de soja de la cosecha 2024', 'VENTA_CULTIVO', '2024-03-15', 150000.00, 'toneladas', 50.00, 'Cooperativa San Martín', 'CONFIRMADO', 1, 1),
('Venta de Maíz', 'Venta de maíz de la cosecha 2024', 'VENTA_CULTIVO', '2024-04-20', 80000.00, 'toneladas', 40.00, 'Molino Central', 'CONFIRMADO', 1, 1),
('Servicio de Siembra', 'Servicio de siembra para terceros', 'SERVICIOS_AGRICOLAS', '2024-02-10', 25000.00, 'hectáreas', 100.00, 'Estancia La Esperanza', 'CONFIRMADO', NULL, 1),
('Subsidio PROAGRO', 'Subsidio del programa PROAGRO', 'SUBSIDIOS', '2024-01-15', 45000.00, NULL, NULL, 'Gobierno Nacional', 'CONFIRMADO', NULL, 1),
('Venta de Ganado', 'Venta de novillos', 'VENTA_ANIMAL', '2024-05-10', 120000.00, 'cabezas', 20.00, 'Frigorífico del Sur', 'REGISTRADO', NULL, 1);

-- Verificar que se creó correctamente
SELECT 'Tabla ingresos creada exitosamente' as mensaje;
SELECT COUNT(*) as total_ingresos FROM ingresos;
```

### Paso 5: Ejecutar el Script
1. Haz clic en el botón "Continuar" o "Go"
2. Deberías ver un mensaje de éxito
3. Verifica que la tabla se creó en el panel izquierdo

### Paso 6: Verificar la Instalación
Ejecuta estas consultas para verificar:

```sql
-- Verificar que la tabla existe
SHOW TABLES LIKE 'ingresos';

-- Ver la estructura de la tabla
DESCRIBE ingresos;

-- Ver los datos de ejemplo
SELECT * FROM ingresos;
```

## 📁 Archivos Disponibles

### ✅ Archivos SQL Correctos:
- `agrogestion-backend/src/main/resources/create-ingresos-table-clean.sql`
- `agrogestion-backend/src/main/resources/migration-create-ingresos-table-simple.sql`

### ❌ Archivos que NO son SQL:
- `FUNCIONALIDAD_BALANCE_COSTOS_BENEFICIOS.md` (archivo de documentación)
- `INSTALACION_BALANCE_PASO_A_PASO.md` (archivo de documentación)

## 🔧 Solución de Problemas

### Error: "Variable name was expected"
- **Causa**: Estás ejecutando un archivo `.md` en lugar de `.sql`
- **Solución**: Usa solo el contenido SQL, no la documentación

### Error: "Table already exists"
- Ejecuta: `DROP TABLE IF EXISTS ingresos;`
- Luego ejecuta el script de creación

### Error: "Access denied"
- Verifica que tienes permisos de administrador
- Usa un usuario con privilegios CREATE, INSERT, SELECT

## ✅ Verificación Final

Después de ejecutar el script correctamente, deberías ver:

1. ✅ Mensaje: "Tabla ingresos creada exitosamente"
2. ✅ Resultado: "total_ingresos: 5"
3. ✅ La tabla `ingresos` aparece en el panel izquierdo de phpMyAdmin
4. ✅ Puedes ver los datos de ejemplo al hacer clic en la tabla

## 🚀 Próximos Pasos

Una vez que la tabla esté creada:

1. Reinicia el backend con el perfil MySQL
2. Prueba la funcionalidad desde el frontend
3. Integra los componentes en el menú principal

## 📞 Si Necesitas Ayuda

Si sigues teniendo problemas:

1. Asegúrate de estar ejecutando solo el contenido SQL
2. Verifica que estás en la base de datos correcta
3. Revisa que tienes permisos de administrador
4. Anota los errores exactos que aparecen
