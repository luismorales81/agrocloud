# Sistema de Iconos - Lucide React

Este sistema proporciona una interfaz unificada para usar iconos en todo el proyecto usando Lucide React.

## Instalación

Lucide React ya está instalado en el proyecto. No se requiere configuración adicional.

## Uso Básico

### Importar el componente Icon

```tsx
import { Icon } from '../components/icons';

// Uso básico
<Icon name="Settings" size={24} color="#000" />
```

### Usar nombres semánticos

```tsx
import { SemanticIcon } from '../components/icons';

// Usar nombres semánticos en lugar de nombres técnicos
<SemanticIcon semanticName="settings" size={24} />
<SemanticIcon semanticName="pig" size={24} />
<SemanticIcon semanticName="harvest" size={24} />
```

### Usar el hook useIcon

```tsx
import { useIcon, Icon } from '../components/icons';

const MyComponent = () => {
  const iconName = useIcon('settings');
  return <Icon name={iconName} size={24} />;
};
```

## Migración desde Emojis

Para migrar emojis a iconos de Lucide, usa el mapeo de utilidades:

```tsx
import { emojiToIcon } from '../utils/iconMapper';
import { Icon } from '../components/icons';

// Antes (con emoji)
<span>⚙️</span>

// Después (con Lucide)
<Icon name={emojiToIcon('⚙️')} size={24} />
```

## Nombres Semánticos Disponibles

### Navegación y UI
- `home`, `menu`, `close`, `check`, `plus`, `minus`, `edit`, `delete`, `save`, `cancel`
- `search`, `filter`, `settings`, `user`, `users`, `logout`, `login`

### Agricultura
- `plant`, `seed`, `field`, `lot`, `crop`, `harvest`, `tractor`
- `irrigation`, `fertilizer`, `pesticide`, `weather`, `sun`, `rain`, `wind`

### Porcinos
- `pig`, `piggyBank`, `farm`, `barn`, `feed`, `syringe`, `calendar`, `clock`

### Estados
- `active`, `inactive`, `pending`, `completed`, `error`, `warning`, `info`, `success`

### Operaciones
- `add`, `remove`, `edit`, `delete`, `view`, `download`, `upload`, `export`, `import`, `print`, `share`

### Finanzas
- `money`, `chart`, `graph`, `report`, `invoice`

### Inventario
- `inventory`, `stock`, `warehouse`, `product`

## Props del Componente Icon

```tsx
interface IconProps {
  name: IconName;           // Nombre del icono de Lucide
  size?: number | string;   // Tamaño (default: 24)
  color?: string;           // Color del icono
  className?: string;       // Clases CSS adicionales
  strokeWidth?: number;     // Grosor del trazo (default: 2)
  style?: React.CSSProperties; // Estilos inline
}
```

## Ejemplos Completos

### Botón con Icono

```tsx
import { SemanticIcon } from '../components/icons';

<button>
  <SemanticIcon semanticName="plus" size={20} />
  Agregar
</button>
```

### Lista con Iconos

```tsx
import { Icon, IconMap } from '../components/icons';

const items = [
  { name: 'Configuración', icon: IconMap.settings },
  { name: 'Usuarios', icon: IconMap.users },
  { name: 'Reportes', icon: IconMap.report },
];

{items.map(item => (
  <div key={item.name}>
    <Icon name={item.icon} size={20} />
    {item.name}
  </div>
))}
```

### Migración Gradual

Puedes mantener emojis y migrar gradualmente:

```tsx
import { Icon, emojiToIcon } from '../components/icons';
import { emojiToIcon } from '../utils/iconMapper';

// Opción 1: Usar emoji directamente (temporal)
<span>⚙️</span>

// Opción 2: Convertir emoji a icono
<Icon name={emojiToIcon('⚙️')} size={24} />

// Opción 3: Usar nombre semántico (recomendado)
<SemanticIcon semanticName="settings" size={24} />
```

## Catálogo de Iconos

Para ver todos los iconos disponibles de Lucide, visita:
https://lucide.dev/icons/

## Notas

- Todos los iconos de Lucide están disponibles usando el nombre exacto del icono
- Los nombres semánticos facilitan la migración y hacen el código más legible
- El sistema es compatible con los emojis existentes durante la migración
- Los iconos se renderizan como SVG, por lo que son escalables y personalizables










