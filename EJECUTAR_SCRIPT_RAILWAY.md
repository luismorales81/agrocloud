# 🗄️ Cómo Ejecutar el Script SQL en Railway MySQL

## Paso 1: Acceder a la Consola de MySQL de Railway

### Opción A: Desde Railway Dashboard
1. Ve a tu proyecto en Railway Dashboard
2. Selecciona el servicio **MySQL**
3. Ve a la pestaña **"Connect"**
4. Haz clic en **"Connect to MySQL"**
5. Se abrirá una consola web de MySQL

### Opción B: Desde Variables (si tienes las credenciales)
Si tienes las credenciales, puedes usar cualquier cliente MySQL:
- Host: `mysql.railway.internal`
- Puerto: `3306`
- Usuario: `root`
- Contraseña: `WSoobrppUQbaPINdsRcoQVkUvtYKjmSe`
- Base de datos: `railway`

## Paso 2: Ejecutar el Script SQL

### Método 1: Copiar y Pegar
1. Abre la consola de MySQL en Railway
2. Copia todo el contenido del archivo `setup-railway-mysql.sql`
3. Pégalo en la consola
4. Presiona **Enter** para ejecutar

### Método 2: Ejecutar por Partes
Si prefieres ejecutar por partes, puedes dividir el script:

#### Parte 1: Crear Tablas
```sql
-- Ejecutar solo la sección de creación de tablas
USE railway;
-- (copiar todas las CREATE TABLE...)
```

#### Parte 2: Insertar Datos
```sql
-- Ejecutar solo la sección de inserción de datos
-- (copiar todas las INSERT INTO...)
```

#### Parte 3: Crear Índices
```sql
-- Ejecutar solo la sección de índices
-- (copiar todas las CREATE INDEX...)
```

## Paso 3: Verificar la Ejecución

Después de ejecutar el script, deberías ver mensajes como:
```
Query OK, 1 row affected
Query OK, 3 rows affected
```

Y al final, las consultas de verificación mostrarán:
```
+-------------------+----------+
| info              | cantidad |
+-------------------+----------+
| Roles creados:    |        3 |
| Usuarios creados: |        3 |
| Campos creados:   |        3 |
| Lotes creados:    |        4 |
| Cultivos creados: |        3 |
| Insumos creados:  |        5 |
| Maquinaria creada:|        4 |
| Labores creadas:  |        3 |
+-------------------+----------+
```

## Paso 4: Verificar Usuarios Creados

Ejecuta esta consulta para verificar los usuarios:
```sql
SELECT u.username, u.email, GROUP_CONCAT(r.name) as roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
GROUP BY u.id, u.username, u.email;
```

Deberías ver:
```
+----------+------------------------+------------------+
| username | email                  | roles            |
+----------+------------------------+------------------+
| admin    | admin@agrocloud.com   | ADMIN            |
| tecnico  | tecnico@agrocloud.com | TECNICO          |
| productor| productor@agrocloud.com| PRODUCTOR        |
+----------+------------------------+------------------+
```

## Credenciales de Acceso

Una vez ejecutado el script, puedes acceder al sistema con:

### Usuario Administrador
- **Username**: `admin`
- **Password**: `admin123`

### Usuario Técnico
- **Username**: `tecnico`
- **Password**: `tecnico123`

### Usuario Productor
- **Username**: `productor`
- **Password**: `productor123`

## Solución de Problemas

### Error: "Table already exists"
- No te preocupes, el script usa `CREATE TABLE IF NOT EXISTS`
- Los datos se insertarán correctamente

### Error: "Access denied"
- Verifica que estés conectado como usuario `root`
- Confirma que la base de datos sea `railway`

### Error: "Connection failed"
- Verifica que el servicio MySQL esté activo en Railway
- Confirma las credenciales de conexión

## Próximos Pasos

1. **Ejecutar el script SQL** en Railway
2. **Verificar que las tablas se crearon** correctamente
3. **Hacer commit y push** de los cambios del código
4. **Probar el healthcheck** de Railway
5. **Acceder al sistema** con las credenciales proporcionadas
