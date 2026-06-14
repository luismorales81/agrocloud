import React from 'react';
import * as LucideIcons from 'lucide-react';

/**
 * Tipo para los nombres de iconos disponibles de Lucide
 */
export type IconName = keyof typeof LucideIcons;

/**
 * Props del componente Icon
 */
export interface IconProps {
  name: IconName | string;
  size?: number | string;
  color?: string;
  className?: string;
  strokeWidth?: number;
  style?: React.CSSProperties;
}

/**
 * Componente Icon que renderiza iconos de Lucide React
 * 
 * @example
 * <Icon name="Home" size={24} color="#333" />
 * <Icon name="Settings" size={20} className="text-blue-500" />
 */
export const Icon: React.FC<IconProps> = ({
  name,
  size = 24,
  color,
  className,
  strokeWidth = 2,
  style,
}) => {
  const IconComponent = LucideIcons[name as IconName] as React.ComponentType<any>;

  if (!IconComponent) {
    console.warn(`Icono "${name}" no encontrado en Lucide Icons. Usando icono por defecto.`);
    const DefaultIcon = LucideIcons.AlertCircle;
    return (
      <DefaultIcon
        size={size}
        color={color}
        className={className}
        strokeWidth={strokeWidth}
        style={style}
      />
    );
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
 * Helper para obtener un componente de icono directamente
 */
export const getIcon = (name: IconName): React.ComponentType<any> => {
  const IconComponent = LucideIcons[name] as React.ComponentType<any>;
  return IconComponent || LucideIcons.AlertCircle;
};

/**
 * Mapeo de emojis comunes a nombres de iconos de Lucide
 * Útil para migrar de emojis a iconos de Lucide
 */
export const emojiToIconMap: Record<string, IconName> = {
  '📊': 'BarChart',
  '🐷': 'PiggyBank',
  '👶': 'Baby',
  '🤱': 'Heart',
  '🐖': 'PiggyBank',
  '🌾': 'Wheat',
  '💰': 'DollarSign',
  '🐗': 'PiggyBank',
  '💉': 'Syringe',
  '⚙️': 'Settings',
  '🔪': 'Scissors',
  '🏢': 'Building',
  '📋': 'Clipboard',
  '🔄': 'RefreshCw',
  '✅': 'Check',
  '🟢': 'Circle',
  '🟡': 'Circle',
  '🔵': 'Circle',
  '🌱': 'Sprout',
  '🌽': 'Wheat', // 'Corn' no existe en Lucide, usando 'Wheat' como alternativa
  '📦': 'Package',
  '📍': 'MapPin',
  '🏠': 'Home',
  '💀': 'Skull',
  '🌿': 'Leaf',
  '🚜': 'Tractor',
  '📈': 'TrendingUp',
  '👥': 'Users',
  '📚': 'Book',
  '🔐': 'Lock',
  '➕': 'Plus',
  '✏️': 'Edit',
  '🗑️': 'Trash2',
  '✕': 'X',
  '⏳': 'Clock',
  '⚠️': 'AlertTriangle',
  '❌': 'XCircle',
  '💾': 'Save',
  '🔍': 'Search',
  '📅': 'Calendar',
  '🌸': 'Flower',
  '🍎': 'Apple',
  '💤': 'Moon',
  '🚨': 'AlertCircle',
};

/**
 * Componente que convierte un emoji a un icono de Lucide
 */
export interface EmojiIconProps {
  emoji: string;
  size?: number | string;
  color?: string;
  className?: string;
  fallback?: React.ReactNode;
}

export const EmojiIcon: React.FC<EmojiIconProps> = ({
  emoji,
  size = 24,
  color,
  className,
  fallback,
}) => {
  const iconName = emojiToIconMap[emoji];
  
  if (iconName) {
    return (
      <Icon
        name={iconName}
        size={size}
        color={color}
        className={className}
      />
    );
  }

  // Si no hay mapeo, mostrar el emoji original o el fallback
  return (
    <span style={{ fontSize: size, display: 'inline-block' }}>
      {fallback || emoji}
    </span>
  );
};

export default Icon;

