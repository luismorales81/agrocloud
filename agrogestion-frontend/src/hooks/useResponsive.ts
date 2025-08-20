import { useState, useEffect } from 'react';

interface ResponsiveConfig {
  isMobile: boolean;
  isTablet: boolean;
  isDesktop: boolean;
  isLandscape: boolean;
  isPortrait: boolean;
  screenWidth: number;
  screenHeight: number;
  orientation: 'landscape' | 'portrait';
  deviceType: 'mobile' | 'tablet' | 'desktop';
}

// Breakpoints
const BREAKPOINTS = {
  mobile: 768,
  tablet: 1024,
  desktop: 1025
};

export const useResponsive = (): ResponsiveConfig => {
  const [responsive, setResponsive] = useState<ResponsiveConfig>({
    isMobile: false,
    isTablet: false,
    isDesktop: false,
    isLandscape: false,
    isPortrait: false,
    screenWidth: 0,
    screenHeight: 0,
    orientation: 'portrait',
    deviceType: 'desktop'
  });

  useEffect(() => {
    const updateResponsive = () => {
      const width = window.innerWidth;
      const height = window.innerHeight;
      const isLandscape = width > height;
      const isPortrait = height > width;

      const isMobile = width <= BREAKPOINTS.mobile;
      const isTablet = width > BREAKPOINTS.mobile && width <= BREAKPOINTS.tablet;
      const isDesktop = width > BREAKPOINTS.tablet;

      let deviceType: 'mobile' | 'tablet' | 'desktop' = 'desktop';
      if (isMobile) deviceType = 'mobile';
      else if (isTablet) deviceType = 'tablet';

      setResponsive({
        isMobile,
        isTablet,
        isDesktop,
        isLandscape,
        isPortrait,
        screenWidth: width,
        screenHeight: height,
        orientation: isLandscape ? 'landscape' : 'portrait',
        deviceType
      });
    };

    // Actualizar al montar
    updateResponsive();

    // Escuchar cambios de tamaño
    window.addEventListener('resize', updateResponsive);
    window.addEventListener('orientationchange', updateResponsive);

    return () => {
      window.removeEventListener('resize', updateResponsive);
      window.removeEventListener('orientationchange', updateResponsive);
    };
  }, []);

  return responsive;
};

// Hook para detectar si el dispositivo soporta touch
export const useTouchSupport = (): boolean => {
  const [isTouchSupported, setIsTouchSupported] = useState(false);

  useEffect(() => {
    setIsTouchSupported('ontouchstart' in window || navigator.maxTouchPoints > 0);
  }, []);

  return isTouchSupported;
};

// Hook para detectar si el dispositivo está en modo standalone (PWA instalada)
export const useStandalone = (): boolean => {
  const [isStandalone, setIsStandalone] = useState(false);

  useEffect(() => {
    const checkStandalone = () => {
      return (
        window.matchMedia('(display-mode: standalone)').matches ||
        (window.navigator as any).standalone === true ||
        document.referrer.includes('android-app://')
      );
    };

    setIsStandalone(checkStandalone());
  }, []);

  return isStandalone;
};

// Hook para detectar conexión a internet
export const useOnlineStatus = (): boolean => {
  const [isOnline, setIsOnline] = useState(navigator.onLine);

  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  return isOnline;
};

// Hook para detectar si el dispositivo está en modo oscuro
export const useDarkMode = (): boolean => {
  const [isDarkMode, setIsDarkMode] = useState(false);

  useEffect(() => {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    setIsDarkMode(mediaQuery.matches);

    const handleChange = (e: MediaQueryListEvent) => {
      setIsDarkMode(e.matches);
    };

    mediaQuery.addEventListener('change', handleChange);
    return () => mediaQuery.removeEventListener('change', handleChange);
  }, []);

  return isDarkMode;
};

// Hook para detectar si el dispositivo tiene notch
export const useNotch = (): boolean => {
  const [hasNotch, setHasNotch] = useState(false);

  useEffect(() => {
    const checkNotch = () => {
      // Detectar notch en iOS
      const isIOS = /iPad|iPhone|iPod/.test(navigator.userAgent);
      if (isIOS) {
        const hasNotchIOS = window.screen.height >= 812; // iPhone X y posteriores
        setHasNotch(hasNotchIOS);
        return;
      }

      // Detectar notch en Android
      const hasNotchAndroid = (window as any).chrome?.webstore || 
                             (window as any).chrome?.runtime ||
                             navigator.userAgent.includes('Android');
      
      if (hasNotchAndroid) {
        // Verificar si hay notch basado en el viewport
        const viewport = window.visualViewport;
        if (viewport) {
          const hasNotchAndroid = viewport.height < window.innerHeight;
          setHasNotch(hasNotchAndroid);
        }
      }
    };

    checkNotch();
    window.addEventListener('resize', checkNotch);
    return () => window.removeEventListener('resize', checkNotch);
  }, []);

  return hasNotch;
};

// Hook para detectar si el dispositivo está en modo de ahorro de datos
export const useSaveData = (): boolean => {
  const [saveData, setSaveData] = useState(false);

  useEffect(() => {
    const checkSaveData = () => {
      const connection = (navigator as any).connection;
      if (connection) {
        setSaveData(connection.saveData);
      }
    };

    checkSaveData();
  }, []);

  return saveData;
};

// Hook para detectar si el dispositivo está en modo de batería baja
export const useLowBattery = (): boolean => {
  const [isLowBattery, setIsLowBattery] = useState(false);

  useEffect(() => {
    const checkBattery = async () => {
      if ('getBattery' in navigator) {
        try {
          const battery = await (navigator as any).getBattery();
          setIsLowBattery(battery.level < 0.2 && !battery.charging);

          battery.addEventListener('levelchange', () => {
            setIsLowBattery(battery.level < 0.2 && !battery.charging);
          });

          battery.addEventListener('chargingchange', () => {
            setIsLowBattery(battery.level < 0.2 && !battery.charging);
          });
        } catch (error) {
          console.log('No se pudo acceder a la información de la batería');
        }
      }
    };

    checkBattery();
  }, []);

  return isLowBattery;
};

// Hook para detectar si el dispositivo está en modo de memoria baja
export const useLowMemory = (): boolean => {
  const [isLowMemory, setIsLowMemory] = useState(false);

  useEffect(() => {
    const checkMemory = () => {
      if ('deviceMemory' in navigator) {
        const memory = (navigator as any).deviceMemory;
        setIsLowMemory(memory < 4); // Menos de 4GB
      }
    };

    checkMemory();
  }, []);

  return isLowMemory;
};

// Hook para detectar si el dispositivo está en modo de conexión lenta
export const useSlowConnection = (): boolean => {
  const [isSlowConnection, setIsSlowConnection] = useState(false);

  useEffect(() => {
    const checkConnection = () => {
      const connection = (navigator as any).connection;
      if (connection) {
        const isSlow = connection.effectiveType === 'slow-2g' || 
                      connection.effectiveType === '2g' ||
                      connection.downlink < 1;
        setIsSlowConnection(isSlow);
      }
    };

    checkConnection();
  }, []);

  return isSlowConnection;
};

// Hook combinado para todas las características del dispositivo
export const useDeviceCapabilities = () => {
  const responsive = useResponsive();
  const isTouchSupported = useTouchSupport();
  const isStandalone = useStandalone();
  const isOnline = useOnlineStatus();
  const isDarkMode = useDarkMode();
  const hasNotch = useNotch();
  const saveData = useSaveData();
  const isLowBattery = useLowBattery();
  const isLowMemory = useLowMemory();
  const isSlowConnection = useSlowConnection();

  return {
    ...responsive,
    isTouchSupported,
    isStandalone,
    isOnline,
    isDarkMode,
    hasNotch,
    saveData,
    isLowBattery,
    isLowMemory,
    isSlowConnection
  };
};
