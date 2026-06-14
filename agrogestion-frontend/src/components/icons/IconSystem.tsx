/**
 * Sistema centralizado de iconos usando Lucide React
 * Proporciona una interfaz unificada para usar iconos en todo el sistema
 */

import React from 'react';
import * as LucideIcons from 'lucide-react';

// Tipo para los nombres de iconos disponibles
export type IconName = keyof typeof LucideIcons;

interface IconProps {
  /** Acepta nombre literal de Lucide o cadena dinámica (p. ej. desde API). */
  name: IconName | string;
  size?: number | string;
  color?: string;
  className?: string;
  strokeWidth?: number;
  style?: React.CSSProperties;
}

/**
 * Componente de icono unificado
 * Usa Lucide Icons internamente pero proporciona una interfaz consistente
 */
export const Icon: React.FC<IconProps> = ({
  name,
  size = 24,
  color,
  className,
  strokeWidth = 2,
  style,
}) => {
  const IconComponent = LucideIcons[name as IconName] as React.ComponentType<{
    size?: number | string;
    color?: string;
    className?: string;
    strokeWidth?: number;
    style?: React.CSSProperties;
  }>;

  if (!IconComponent) {
    console.warn(`Icono "${name}" no encontrado en Lucide Icons`);
    return <span>❓</span>;
  }

  return (
    <IconComponent
      size={size}
      color={color}
      className={className}
      strokeWidth={strokeWidth}
      style={style}
    />
  );
};

/**
 * Mapeo de iconos comunes del sistema para facilitar la migración
 * Mapea nombres semánticos a iconos de Lucide
 */
export const IconMap: Record<string, IconName> = {
  // Navegación y UI
  home: 'Home',
  menu: 'Menu',
  close: 'X',
  check: 'Check',
  plus: 'Plus',
  minus: 'Minus',
  edit: 'Pencil',
  delete: 'Trash2',
  save: 'Save',
  cancel: 'XCircle',
  search: 'Search',
  filter: 'Filter',
  settings: 'Settings',
  user: 'User',
  users: 'Users',
  logout: 'LogOut',
  login: 'LogIn',
  
  // Agricultura y Cultivos
  plant: 'Sprout',
  seed: 'Circle',
  field: 'Map',
  lot: 'MapPin',
  crop: 'Wheat',
  harvest: 'Scissors',
  tractor: 'Tractor',
  irrigation: 'Droplet',
  fertilizer: 'Flower',
  pesticide: 'Bug',
  weather: 'Cloud',
  sun: 'Sun',
  rain: 'CloudRain',
  wind: 'Wind',
  
  // Porcinos
  pig: 'Circle', // Lucide no tiene icono de cerdo, usar Circle como placeholder
  piggyBank: 'PiggyBank',
  farm: 'Building2',
  barn: 'Warehouse',
  feed: 'Package',
  syringe: 'Syringe',
  calendar: 'Calendar',
  clock: 'Clock',
  
  // Estados y Acciones
  active: 'CheckCircle',
  inactive: 'XCircle',
  pending: 'Clock',
  completed: 'CheckCircle2',
  error: 'AlertCircle',
  warning: 'AlertTriangle',
  info: 'Info',
  success: 'CheckCircle',
  
  // Operaciones
  add: 'Plus',
  remove: 'Minus',
  view: 'Eye',
  download: 'Download',
  upload: 'Upload',
  export: 'FileDown',
  import: 'FileUp',
  print: 'Printer',
  share: 'Share2',
  
  // Finanzas
  money: 'DollarSign',
  chart: 'BarChart',
  graph: 'LineChart',
  report: 'FileText',
  invoice: 'Receipt',
  
  // Inventario
  inventory: 'Package',
  stock: 'Boxes',
  warehouse: 'Warehouse',
  product: 'Package',
  
  // Configuración
  config: 'Settings',
  tools: 'Wrench',
  help: 'HelpCircle',
  
  // Comunicación
  email: 'Mail',
  phone: 'Phone',
  message: 'MessageSquare',
  notification: 'Bell',
  
  // Otros
  arrowRight: 'ArrowRight',
  arrowLeft: 'ArrowLeft',
  arrowUp: 'ArrowUp',
  arrowDown: 'ArrowDown',
  chevronRight: 'ChevronRight',
  chevronLeft: 'ChevronLeft',
  chevronUp: 'ChevronUp',
  chevronDown: 'ChevronDown',
  more: 'MoreVertical',
  dots: 'MoreHorizontal',
};

/**
 * Hook para obtener un icono por nombre semántico
 */
export const useIcon = (semanticName: string): IconName => {
  return IconMap[semanticName] || 'Circle';
};

/**
 * Componente de icono semántico
 * Permite usar nombres semánticos en lugar de nombres técnicos de Lucide
 */
export const SemanticIcon: React.FC<Omit<IconProps, 'name'> & { semanticName: string }> = ({
  semanticName,
  ...props
}) => {
  const iconName = useIcon(semanticName);
  return <Icon name={iconName} {...props} />;
};

/**
 * Exportar todos los iconos de Lucide para uso directo si es necesario
 */
export { LucideIcons };


