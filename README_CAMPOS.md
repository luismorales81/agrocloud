# Módulo de Gestión de Campos Agrícolas

## 🚀 Características

- ✅ **Formulario de alta** con nombre y superficie del campo
- ✅ **Integración con Google Maps** para delimitar polígonos
- ✅ **Cálculo automático de superficie** basado en el polígono dibujado
- ✅ **Listado de campos** con buscador
- ✅ **Visualización en mapa** de campos existentes
- ✅ **Almacenamiento en MySQL** con validaciones
- ✅ **Interfaz responsive** y moderna

## 📋 Requisitos Previos

### 1. Google Maps API Key
Necesitas una API Key de Google Maps con las siguientes APIs habilitadas:
- Maps JavaScript API
- Drawing Library
- Geometry Library

### 2. Base de Datos MySQL
Ejecutar el script `campos_table.sql` para crear la tabla y estructura necesaria.

## 🔧 Configuración

### Paso 1: Configurar Google Maps API Key

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un proyecto o selecciona uno existente
3. Habilita las APIs:
   - Maps JavaScript API
   - Drawing Library
   - Geometry Library
4. Crea una API Key
5. Reemplaza `YOUR_GOOGLE_MAPS_API_KEY` en el archivo `FieldsManagement.tsx`

```typescript
// En FieldsManagement.tsx, línea ~120
script.src = `https://maps.googleapis.com/maps/api/js?key=TU_API_KEY_AQUI&libraries=drawing,geometry`;
```

### Paso 2: Configurar Base de Datos

Ejecuta el script SQL en tu base de datos MySQL:

```bash
mysql -u agrogestion -pagrogestion agrogestion < campos_table.sql
```

### Paso 3: Configurar Backend (Opcional)

Si quieres conectar con un backend real, modifica las funciones en `FieldsManagement.tsx`:

```typescript
// Ejemplo de función para cargar campos desde API
const loadFields = async () => {
  try {
    const response = await fetch('/api/campos');
    const data = await response.json();
    setFields(data);
  } catch (error) {
    console.error('Error cargando campos:', error);
  }
};

// Ejemplo de función para guardar campo
const saveField = async () => {
  try {
    const response = await fetch('/api/campos', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(formData)
    });
    const newField = await response.json();
    setFields(prev => [...prev, newField]);
  } catch (error) {
    console.error('Error guardando campo:', error);
  }
};
```

## 🎯 Uso del Módulo

### 1. Crear un Nuevo Campo

1. Haz clic en **"+ Nuevo Campo"**
2. Completa el **nombre del campo**
3. Haz clic en **"Abrir Mapa para Delimitar"**
4. **Dibuja el polígono** en el mapa:
   - Haz clic para crear vértices
   - Haz doble clic para cerrar el polígono
   - La superficie se calcula automáticamente
5. Haz clic en **"Guardar Campo"**

### 2. Ver Campos Existentes

1. Usa el **buscador** para filtrar campos por nombre
2. Haz clic en **"Ver en Mapa"** para visualizar un campo específico
3. Haz clic en **"Eliminar"** para borrar un campo

### 3. Funcionalidades del Mapa

- **Modo satélite** por defecto para mejor visualización
- **Herramientas de dibujo** integradas
- **Cálculo automático de superficie** en hectáreas
- **Visualización de campos existentes** en rojo
- **Centrado automático** en el campo seleccionado

## 📊 Estructura de la Base de Datos

```sql
CREATE TABLE campos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    superficie DECIMAL(10,2) NOT NULL,
    poligono TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Campos:
- **id**: Identificador único autoincremental
- **nombre**: Nombre del campo agrícola
- **superficie**: Superficie en hectáreas
- **poligono**: Polígono en formato GeoJSON
- **created_at**: Fecha de creación
- **updated_at**: Fecha de última actualización

## 🔍 Funcionalidades Avanzadas

### Búsqueda y Filtrado
- Búsqueda en tiempo real por nombre
- Contador de campos encontrados
- Interfaz limpia y responsive

### Validaciones
- Superficie mayor a 0
- Polígono obligatorio
- Nombre requerido
- Validaciones en frontend y backend

### Estadísticas
- Vista SQL para estadísticas de campos
- Procedimiento almacenado para búsquedas
- Triggers para validaciones automáticas

## 🛠️ Personalización

### Cambiar Colores del Mapa
```typescript
polygonOptions: {
  fillColor: '#4CAF50',    // Color de relleno
  fillOpacity: 0.3,        // Opacidad del relleno
  strokeWeight: 2,         // Grosor del borde
  strokeColor: '#4CAF50',  // Color del borde
}
```

### Cambiar Ubicación por Defecto
```typescript
center: { lat: -34.6118, lng: -58.3960 }, // Buenos Aires
zoom: 15,                                  // Nivel de zoom
```

### Agregar Más Campos al Formulario
```typescript
interface Field {
  id?: number;
  nombre: string;
  superficie: number;
  poligono: string;
  // Agregar nuevos campos aquí
  tipo_suelo?: string;
  propietario?: string;
}
```

## 🚨 Solución de Problemas

### Google Maps no carga
- Verifica que la API Key sea válida
- Asegúrate de que las APIs estén habilitadas
- Revisa la consola del navegador para errores

### Error al guardar campo
- Verifica la conexión a la base de datos
- Revisa que todos los campos estén completos
- Consulta los logs del servidor

### Polígono no se dibuja
- Asegúrate de hacer clic para crear vértices
- Haz doble clic para cerrar el polígono
- Verifica que las librerías de Google Maps estén cargadas

## 📝 Notas Importantes

1. **API Key de Google Maps**: Es obligatoria para el funcionamiento del módulo
2. **Formato GeoJSON**: Los polígonos se almacenan en formato GeoJSON estándar
3. **Superficie**: Se calcula automáticamente en hectáreas
4. **Responsive**: El módulo funciona en dispositivos móviles y desktop
5. **Validaciones**: Tanto en frontend como en backend para mayor seguridad

## 🔗 Enlaces Útiles

- [Google Maps JavaScript API](https://developers.google.com/maps/documentation/javascript)
- [GeoJSON Specification](https://geojson.org/)
- [MySQL Spatial Data Types](https://dev.mysql.com/doc/refman/8.0/en/spatial-types.html)
