import { useMemo } from 'react';
import { getIcon, type IconName } from '../components/Icon';
import { emojiToIconMap } from '../components/Icon';

/**
 * Hook para obtener un componente de icono
 * 
 * @example
 * const HomeIcon = useIcon('Home');
 * return <HomeIcon size={24} color="#333" />;
 */
export const useIcon = (name: IconName) => {
  return useMemo(() => getIcon(name), [name]);
};

/**
 * Hook para convertir un emoji a un icono de Lucide
 * 
 * @example
 * const Icon = useIconFromEmoji('📊');
 * return <Icon size={24} />;
 */
export const useIconFromEmoji = (emoji: string) => {
  return useMemo(() => {
    const iconName = emojiToIconMap[emoji];
    if (iconName) {
      return getIcon(iconName);
    }
    return null;
  }, [emoji]);
};

