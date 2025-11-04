# 📚 Guía de Uso de Servicios API Centralizados

## 🎯 Objetivo

Centralizar todas las llamadas a la API en servicios reutilizables para:
- ✅ Mantener consistencia en las URLs
- ✅ Facilitar el mantenimiento
- ✅ Evitar errores de duplicación de `/api/`
- ✅ Mejorar la experiencia de desarrollo
- ✅ Facilitar el testing

## 📁 Estructura de Archivos

```
src/services/
├── api.ts                    # Configuración base de Axios e interceptores
├── apiEndpoints.ts           # Definición centralizada de todos los endpoints
├── apiServices.ts            # Servicios con métodos para cada entidad
└── README_API_SERVICES.md    # Esta documentación
```

## 🚀 Uso Rápido

### ❌ Antes (Incorrecto - Evitar)
```typescript
// ❌ Llamada directa con riesgo de duplicación /api/api/
const response = await api.get(`/api/v1/cultivos/${id}`);
```

### ✅ Después (Correcto - Recomendado)
```typescript
// ✅ Usar servicios centralizados
import { cultivosService } from '../services/apiServices';

const cultivo = await cultivosService.obtener(id);
```

## 📖 Ejemplos de Uso por Servicio

### 🔐 Autenticación
```typescript
import { authService } from '../services/apiServices';

// Login
const user = await authService.login(email, password);

// Verificar autenticación
if (authService.isAuthenticated()) {
  // Usuario autenticado
}
```

### 👥 Usuarios
```typescript
import { usuariosService } from '../services/apiServices';

// Listar usuarios
const usuarios = await usuariosService.listar();

// Crear usuario
const nuevoUsuario = await usuariosService.crear(usuarioData);

// Actualizar usuario
await usuariosService.actualizar(id, usuarioData);

// Cambiar estado
await usuariosService.cambiarEstado(id, 'ACTIVO');
```

### 🌱 Cultivos
```typescript
import { cultivosService } from '../services/apiServices';

// Listar cultivos
const cultivos = await cultivosService.listar();

// Crear cultivo
const nuevoCultivo = await cultivosService.crear(cultivoData);

// Actualizar cultivo
await cultivosService.actualizar(id, cultivoData);

// Eliminar cultivo
await cultivosService.eliminar(id);
```

### 📦 Lotes
```typescript
import { lotesService } from '../services/apiServices';

// Listar lotes
const lotes = await lotesService.listar();

// Sembrar lote
await lotesService.sembrar(loteId, siembraData);

// Cosechar lote
await lotesService.cosechar(loteId, cosechaData);

// Obtener info de cosecha
const infoCosecha = await lotesService.obtenerInfoCosecha(loteId);
```

### 🧪 Insumos
```typescript
import { insumosService } from '../services/apiServices';

// Listar insumos
const insumos = await insumosService.listar();

// Crear insumo
await insumosService.crear(insumoData);

// Actualizar insumo
await insumosService.actualizar(id, insumoData);
```

### 🧪 Dosis de Agroquímicos
```typescript
import { dosisAgroquimicosService } from '../services/apiServices';

// Obtener dosis por insumo
const dosis = await dosisAgroquimicosService.obtenerPorInsumo(insumoId);

// Crear dosis
await dosisAgroquimicosService.crear(dosisData);
```

### ⚙️ Labores
```typescript
import { laboresService } from '../services/apiServices';

// Listar labores
const labores = await laboresService.listar();

// Crear labor
await laboresService.crear(laborData);

// Anular labor
await laboresService.anular(laborId, {
  justificacion: 'Error en la aplicación',
  restaurarInsumos: true
});
```

### 💰 Finanzas
```typescript
import { ingresosService, egresosService } from '../services/apiServices';

// Ingresos
const ingresos = await ingresosService.listar();
await ingresosService.crear(ingresoData);

// Egresos
const egresos = await egresosService.listar();
await egresosService.crear(egresoData);
```

## 🔧 Si Necesitas una URL Específica

Si necesitas usar una URL directamente (no recomendado), usa los endpoints definidos:

```typescript
import { API_ENDPOINTS } from '../services/apiEndpoints';
import api from '../services/api';

// Usar endpoint definido
const response = await api.get(API_ENDPOINTS.CULTIVOS.OBTENER(id));

// ✅ Esto es seguro y consistente
```

## 📝 Migración Gradual

Si ya tienes código existente, puedes migrarlo gradualmente:

1. **Identifica** las llamadas API directas en tu componente
2. **Reemplaza** con los servicios centralizados
3. **Prueba** que todo funcione correctamente
4. **Repite** para el siguiente componente

### Ejemplo de Migración

```typescript
// ❌ Antes
const [cultivos, setCultivos] = useState([]);
useEffect(() => {
  api.get('/v1/cultivos').then(res => setCultivos(res.data));
}, []);

// ✅ Después
const [cultivos, setCultivos] = useState([]);
useEffect(() => {
  cultivosService.listar().then(setCultivos);
}, []);
```

## 🎨 Estructura de Servicios

Cada servicio sigue el mismo patrón:

```typescript
export const ejemploService = {
  async listar() { ... },
  async obtener(id: number) { ... },
  async crear(data: any) { ... },
  async actualizar(id: number, data: any) { ... },
  async eliminar(id: number) { ... },
  // Métodos específicos adicionales...
};
```

## ⚠️ Importante

1. **NUNCA** incluyas `/api/` en las rutas, el interceptor lo agrega automáticamente
2. **SIEMPRE** usa los servicios centralizados para nuevas funciones
3. **MIGRA** gradualmente el código existente cuando tengas tiempo
4. **CONSULTA** `apiEndpoints.ts` si necesitas una URL específica

## 🐛 Troubleshooting

### Error: "No static resource api/api/..."
- **Causa**: Estás usando una URL que incluye `/api/` manualmente
- **Solución**: Usa los servicios centralizados o `API_ENDPOINTS`

### Error: "Endpoint not found"
- **Causa**: El endpoint no está definido en `apiEndpoints.ts`
- **Solución**: Agrégalo a `apiEndpoints.ts` y crea el método correspondiente en `apiServices.ts`

## 📚 Servicios Disponibles

- ✅ `authService` - Autenticación
- ✅ `usuariosService` - Administración de usuarios
- ✅ `cultivosService` - Gestión de cultivos
- ✅ `lotesService` - Gestión de lotes
- ✅ `insumosService` - Gestión de insumos
- ✅ `dosisAgroquimicosService` - Dosis de agroquímicos
- ✅ `laboresService` - Gestión de labores
- ✅ `camposService` - Gestión de campos
- ✅ `maquinariaService` - Gestión de maquinaria
- ✅ `ingresosService` - Gestión de ingresos
- ✅ `egresosService` - Gestión de egresos
- ✅ `rolesService` - Gestión de roles

## 🚀 Próximos Pasos

1. Usa estos servicios en nuevos componentes
2. Migra componentes existentes gradualmente
3. Reporta cualquier endpoint faltante
4. Sugiere mejoras a la estructura




