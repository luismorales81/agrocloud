/**
 * Utilidad para mapear emojis y nombres antiguos a iconos de Lucide
 * Facilita la migración gradual del sistema
 */

import { IconName } from '../components/icons';

/**
 * Mapeo de emojis comunes a iconos de Lucide
 */
export const emojiToIconMap: Record<string, IconName> = {
  '🏢': 'Building2',
  '📊': 'BarChart',
  '💰': 'DollarSign',
  '🐷': 'PiggyBank',
  '🌾': 'Wheat',
  '👶': 'Baby',
  '💀': 'Skull',
  '📋': 'Clipboard',
  '💉': 'Syringe',
  '🏠': 'Home',
  '📍': 'MapPin',
  '⚙️': 'Settings',
  '✅': 'Check',
  '❌': 'X',
  '➕': 'Plus',
  '✏️': 'Pencil',
  '🗑️': 'Trash2',
  '💾': 'Save',
  '✕': 'X',
  '🟢': 'Circle',
  '🟡': 'Circle',
  '🔵': 'Circle',
  '🌱': 'Sprout',
  '🌽': 'Wheat', // 'Corn' no existe en Lucide, usando 'Wheat' como alternativa
  '📦': 'Package',
  '🔔': 'Bell',
  '⚠️': 'AlertTriangle',
  '⏳': 'Clock',
  '🔍': 'Search',
  '📈': 'TrendingUp',
  '📉': 'TrendingDown',
  '🌧️': 'CloudRain',
  '☀️': 'Sun',
  '🌙': 'Moon',
  '⛅': 'Cloud',
  '☁️': 'Cloud',
  '🌦️': 'CloudRain',
  '⛈️': 'CloudLightning',
  '❄️': 'Snowflake',
  '🌫️': 'CloudFog',
  '🌤️': 'CloudSun',
  '⚒️': 'Wrench',
  '🚜': 'Tractor',
  '🔧': 'Wrench',
  '🌿': 'Leaf',
  '🍄': 'Circle',
  '🐛': 'Bug',
  '⛽': 'Fuel',
  '📅': 'Calendar',
  '📌': 'Pin',
  '👥': 'Users',
  '📚': 'Book',
  '💤': 'Moon',
  '🚨': 'AlertCircle',
  '🌸': 'Flower',
  '🍎': 'Apple',
};

/**
 * Convierte un emoji a un nombre de icono de Lucide
 */
export const emojiToIcon = (emoji: string): IconName => {
  return emojiToIconMap[emoji] || 'Circle';
};

/**
 * Convierte un nombre de estado o tipo a un icono de Lucide
 */
export const nameToIcon = (name: string): IconName => {
  const nameLower = name.toLowerCase();
  
  const nameMap: Record<string, IconName> = {
    // Estados
    'disponible': 'Circle',
    'preparado': 'Circle',
    'sembrado': 'Sprout',
    'emergencia': 'Sprout',
    'crecimiento': 'TrendingUp',
    'floracion': 'Flower',
    'fructificacion': 'Apple',
    'cosechado': 'CheckCircle',
    'descanso': 'Moon',
    
    // Tipos de labor
    'siembra': 'Sprout',
    'fertilizacion': 'Flower',
    'riego': 'Droplet',
    'cosecha': 'Scissors',
    'mantenimiento': 'Wrench',
    'poda': 'Scissors',
    'control_plagas': 'Bug',
    'control_malezas': 'Leaf',
    'analisis_suelo': 'FlaskConical',
    
    // Porcinos
    'raza': 'PiggyBank',
    'alimento': 'Wheat',
    'servicio': 'Heart',
    'mortalidad': 'Skull',
    'baja': 'XCircle',
    'sanitario': 'Syringe',
    'corral': 'Home',
    'parto': 'Baby',
    'ubicacion': 'MapPin',
    
    // General
    'configuracion': 'Settings',
    'establecimiento': 'Building2',
    'productivo': 'BarChart',
    'economico': 'DollarSign',
  };
  
  return nameMap[nameLower] || 'Circle';
};










