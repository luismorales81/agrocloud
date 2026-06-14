# Sistema Modular de AgroCloud

## 📋 Descripción

Este sistema permite que la aplicación AgroCloud soporte múltiples módulos (Cultivos, Porcinos, etc.) de forma escalable y mantenible. Cada módulo tiene su propio menú, rutas y componentes.

## 🏗️ Estructura

```
src/
├── modules/
│   ├── cultivos/
│   │   ├── screens/          # Pantallas del módulo
│   │   ├── components/       # Componentes específicos del módulo
│   │   ├── menu.ts          # Configuración del menú
│   │   ├── routes.ts        # Configuración de rutas
│   │   └── index.ts         # Exportación del módulo
│   ├── porcinos/
│   │   └── ... (misma estructura)
│   └── index.ts             # Registro centralizado de módulos
├── core/
│   ├── context/
│   │   └── ModuleContext.tsx  # Contexto global del módulo activo
│   ├── hooks/
│   │   └── useModule.ts      # Hook para acceder al módulo
│   └── types/
│       └── module.types.ts    # Tipos TypeScript
├── navigation/
│   └── AppNavigator.tsx      # Navegador que carga rutas dinámicamente
└── screens/
    └── ModuleSelectorScreen.tsx  # Pantalla de selección de módulos
```

## 🚀 Cómo Agregar un Nuevo Módulo

### Paso 1: Crear la estructura de carpetas

```bash
mkdir -p src/modules/nuevo-modulo/{screens,components}
```

### Paso 2: Crear el archivo `menu.ts`

```typescript
import { MenuItem } from '../../core/types/module.types';

export const nuevoModuloMenu: MenuItem[] = [
  {
    id: 'dashboard',
    nombre: 'Dashboard',
    icono: '📊',
    ruta: '/nuevo-modulo/dashboard',
  },
  {
    id: 'opcion1',
    nombre: 'Opción 1',
    icono: '🔧',
    ruta: '/nuevo-modulo/opcion1',
    permisos: ['canViewNuevoModulo'],
  },
];
```

### Paso 3: Crear el archivo `routes.ts`

```typescript
import { ModuleRoute } from '../../core/types/module.types';
import DashboardScreen from './screens/DashboardScreen';
import Opcion1Screen from './screens/Opcion1Screen';

export const nuevoModuloRoutes: ModuleRoute[] = [
  {
    path: '/nuevo-modulo/dashboard',
    name: 'Dashboard',
    component: DashboardScreen,
  },
  {
    path: '/nuevo-modulo/opcion1',
    name: 'Opción 1',
    component: Opcion1Screen,
    permisos: ['canViewNuevoModulo'],
  },
];
```

### Paso 4: Crear el archivo `index.ts`

```typescript
import { ModuleConfig } from '../../core/types/module.types';
import { nuevoModuloMenu } from './menu';
import { nuevoModuloRoutes } from './routes';

export const nuevoModuloModule: ModuleConfig = {
  id: 'nuevo-modulo',
  nombre: 'Nuevo Módulo',
  descripcion: 'Descripción del nuevo módulo',
  icono: '🔧',
  color: '#3b82f6',
  menu: nuevoModuloMenu,
  routes: nuevoModuloRoutes,
};
```

### Paso 5: Registrar el módulo en `modules/index.ts`

```typescript
import { nuevoModuloModule } from './nuevo-modulo';

export const availableModules: ModuleConfig[] = [
  cultivosModule,
  porcinosModule,
  nuevoModuloModule, // Agregar aquí
];
```

### Paso 6: Agregar el tipo en `core/types/module.types.ts`

```typescript
export type ModuleId = 'cultivos' | 'porcinos' | 'nuevo-modulo';
```

### Paso 7: Actualizar el backend

El backend debe exponer el módulo en el endpoint `/modules/available` con la siguiente estructura:

```json
{
  "id": "nuevo-modulo",
  "nombre": "Nuevo Módulo",
  "descripcion": "Descripción del nuevo módulo",
  "icono": "🔧",
  "color": "#3b82f6",
  "habilitado": true
}
```

## 🔧 Uso del Sistema

### Acceder al módulo activo

```typescript
import { useModule } from '../core/hooks/useModule';

const MiComponente = () => {
  const { currentModule, moduleInfo, availableModules } = useModule();
  
  return (
    <div>
      <p>Módulo actual: {currentModule}</p>
      <p>Nombre: {moduleInfo?.nombre}</p>
    </div>
  );
};
```

### Cambiar de módulo

```typescript
const { setCurrentModule } = useModule();
setCurrentModule('porcinos');
```

### Obtener menú del módulo

```typescript
import { getModuleMenu } from '../modules';

const menu = getModuleMenu('cultivos');
```

### Obtener rutas del módulo

```typescript
import { getModuleRoutes } from '../modules';

const routes = getModuleRoutes('cultivos');
```

## 🎯 Comportamiento

1. **Al iniciar la aplicación:**
   - Si el usuario tiene un solo módulo habilitado → entra directo a ese módulo
   - Si tiene múltiples módulos → muestra el selector de módulos

2. **Dentro de un módulo:**
   - El menú lateral cambia según el módulo activo
   - Las rutas se cargan dinámicamente
   - El usuario puede cambiar de módulo desde el sidebar

3. **Persistencia:**
   - El módulo seleccionado se guarda en `localStorage`
   - Se mantiene entre sesiones

## 📝 Notas

- Cada módulo es completamente independiente
- Los permisos se validan a nivel de ruta y menú
- El sistema es escalable: agregar un nuevo módulo no requiere modificar código existente
- Los módulos pueden compartir componentes comunes desde `src/components`

