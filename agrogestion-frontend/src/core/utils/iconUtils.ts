import * as LucideIcons from 'lucide-react';
import type { IconName } from '../components/Icon';

/**
 * Lista de todos los iconos disponibles de Lucide
 * Útil para autocompletado y validación
 */
export const availableIcons: IconName[] = Object.keys(LucideIcons).filter(
  (key) => typeof LucideIcons[key as IconName] === 'function'
) as IconName[];

/**
 * Busca iconos por nombre (búsqueda parcial)
 */
export const searchIcons = (query: string): IconName[] => {
  const lowerQuery = query.toLowerCase();
  return availableIcons.filter((iconName) =>
    iconName.toLowerCase().includes(lowerQuery)
  );
};

/**
 * Categorías de iconos comunes para facilitar la selección
 */
export const iconCategories = {
  agricultura: [
    'Wheat',
    'Sprout',
    'TreePine',
    'TreeDeciduous',
    'Flower',
    'Carrot',
    'Apple',
    'Tractor',
    'Farm',
  ] as IconName[],
  animales: [
    'PiggyBank',
    'Dog',
    'Cat',
    'Bird',
    'Fish',
    'Heart',
    'Baby',
  ] as IconName[],
  acciones: [
    'Plus',
    'Edit',
    'Trash2',
    'Save',
    'X',
    'Check',
    'Search',
    'Filter',
    'Download',
    'Upload',
  ] as IconName[],
  navegacion: [
    'Home',
    'Settings',
    'User',
    'Users',
    'Menu',
    'ArrowLeft',
    'ArrowRight',
    'ChevronDown',
    'ChevronUp',
  ] as IconName[],
  estado: [
    'CheckCircle',
    'XCircle',
    'AlertCircle',
    'AlertTriangle',
    'Info',
    'Clock',
    'Calendar',
  ] as IconName[],
  finanzas: [
    'DollarSign',
    'TrendingUp',
    'TrendingDown',
    'PiggyBank',
    'CreditCard',
    'Receipt',
  ] as IconName[],
  configuracion: [
    'Settings',
    'Cog',
    'Wrench',
    'Sliders',
    'Tool',
  ] as IconName[],
};

/**
 * Obtiene iconos sugeridos basados en una palabra clave
 */
export const getSuggestedIcons = (keyword: string): IconName[] => {
  const lowerKeyword = keyword.toLowerCase();
  const suggestions: IconName[] = [];

  // Buscar en categorías
  Object.values(iconCategories).forEach((category) => {
    category.forEach((icon) => {
      if (icon.toLowerCase().includes(lowerKeyword)) {
        suggestions.push(icon);
      }
    });
  });

  // Buscar en todos los iconos
  availableIcons.forEach((icon) => {
    if (icon.toLowerCase().includes(lowerKeyword) && !suggestions.includes(icon)) {
      suggestions.push(icon);
    }
  });

  return suggestions.slice(0, 10); // Limitar a 10 sugerencias
};

