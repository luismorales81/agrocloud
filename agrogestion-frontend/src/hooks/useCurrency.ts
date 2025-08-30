import { useState, useEffect } from 'react';

export const useCurrency = () => {
  const [selectedCurrency, setSelectedCurrency] = useState<'ARS' | 'USD' | 'EUR'>(
    (localStorage.getItem('selectedCurrency') as 'ARS' | 'USD' | 'EUR') || 'ARS'
  );

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
    
    // Disparar evento personalizado para notificar cambios
    window.dispatchEvent(new CustomEvent('currencyChanged', { detail: newCurrency }));
  };

  // Escuchar cambios de moneda desde otros componentes
  useEffect(() => {
    const handleCurrencyChange = (event: CustomEvent) => {
      setSelectedCurrency(event.detail);
    };

    window.addEventListener('currencyChanged', handleCurrencyChange as EventListener);
    
    return () => {
      window.removeEventListener('currencyChanged', handleCurrencyChange as EventListener);
    };
  }, []);

  return {
    selectedCurrency,
    formatCurrency,
    changeCurrency
  };
};
