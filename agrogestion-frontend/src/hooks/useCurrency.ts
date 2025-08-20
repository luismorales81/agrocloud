import { useState, useEffect } from 'react';
import currencyService from '../services/CurrencyService';

export const useCurrency = () => {
  const [currencyConfig, setCurrencyConfig] = useState(currencyService.getCurrencyConfig());
  const [rateInfo, setRateInfo] = useState(currencyService.getRateInfo());

  // Escuchar cambios en la configuración de moneda
  useEffect(() => {
    const handleStorageChange = () => {
      setCurrencyConfig(currencyService.getCurrencyConfig());
      setRateInfo(currencyService.getRateInfo());
    };

    window.addEventListener('storage', handleStorageChange);
    
    // También escuchar cambios locales
    const interval = setInterval(() => {
      const currentConfig = currencyService.getCurrencyConfig();
      const currentRateInfo = currencyService.getRateInfo();
      
      if (JSON.stringify(currentConfig) !== JSON.stringify(currencyConfig)) {
        setCurrencyConfig(currentConfig);
      }
      
      if (JSON.stringify(currentRateInfo) !== JSON.stringify(rateInfo)) {
        setRateInfo(currentRateInfo);
      }
    }, 1000);

    return () => {
      window.removeEventListener('storage', handleStorageChange);
      clearInterval(interval);
    };
  }, [currencyConfig, rateInfo]);

  const formatCurrency = (amount: number, originalCurrency: 'ARS' | 'USD' = 'ARS'): string => {
    return currencyService.formatCurrency(amount, originalCurrency);
  };

  const convertAmount = (amount: number, fromCurrency: 'ARS' | 'USD', toCurrency: 'ARS' | 'USD'): number => {
    return currencyService.convertAmount(amount, fromCurrency, toCurrency);
  };

  const getCurrentCurrency = (): 'ARS' | 'USD' => {
    return currencyConfig.currency;
  };

  const getCurrentRate = (): number => {
    return currencyService.getCurrentRateFromStorage();
  };

  return {
    currencyConfig,
    rateInfo,
    formatCurrency,
    convertAmount,
    getCurrentCurrency,
    getCurrentRate
  };
};
