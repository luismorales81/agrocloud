import { useState, useEffect } from 'react';
import { currencyService } from '../services/CurrencyService';

export const useCurrency = () => {
  const [selectedCurrency, setSelectedCurrency] = useState<'ARS' | 'USD' | 'EUR'>(
    (localStorage.getItem('selectedCurrency') as 'ARS' | 'USD' | 'EUR') || 'ARS'
  );
  
  const [exchangeType, setExchangeType] = useState<'oficial' | 'blue'>(
    (localStorage.getItem('exchangeType') as 'oficial' | 'blue') || 'oficial'
  );

  // Cargar configuración guardada al inicializar
  useEffect(() => {
    currencyService.loadSavedConfig();
  }, []);

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: selectedCurrency,
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(amount);
  };

  const changeCurrency = (newCurrency: 'ARS' | 'USD' | 'EUR') => {
    setSelectedCurrency(newCurrency);
    localStorage.setItem('selectedCurrency', newCurrency);
    currencyService.updateConfig({ primaryCurrency: newCurrency });
    
    // Disparar evento personalizado para notificar cambios
    window.dispatchEvent(new CustomEvent('currencyChanged', { detail: newCurrency }));
  };

  const changeExchangeType = (newType: 'oficial' | 'blue') => {
    setExchangeType(newType);
    localStorage.setItem('exchangeType', newType);
    currencyService.setExchangeType(newType);
    
    // Disparar evento personalizado para notificar cambios
    window.dispatchEvent(new CustomEvent('exchangeTypeChanged', { detail: newType }));
  };

  // Escuchar cambios de moneda desde otros componentes
  useEffect(() => {
    const handleCurrencyChange = (event: CustomEvent) => {
      setSelectedCurrency(event.detail);
    };

    const handleExchangeTypeChange = (event: CustomEvent) => {
      setExchangeType(event.detail);
    };

    window.addEventListener('currencyChanged', handleCurrencyChange as EventListener);
    window.addEventListener('exchangeTypeChanged', handleExchangeTypeChange as EventListener);
    
    return () => {
      window.removeEventListener('currencyChanged', handleCurrencyChange as EventListener);
      window.removeEventListener('exchangeTypeChanged', handleExchangeTypeChange as EventListener);
    };
  }, []);

  return {
    selectedCurrency,
    exchangeType,
    formatCurrency,
    changeCurrency,
    changeExchangeType,
    currencyService
  };
};
