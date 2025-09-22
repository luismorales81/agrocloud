import { useEffect, useState } from 'react';

export const useCurrencyUpdate = () => {
  const [updateTrigger, setUpdateTrigger] = useState(0);

  useEffect(() => {
    const handleCurrencyUpdate = () => {
      setUpdateTrigger(prev => prev + 1);
    };

    const handleRatesUpdated = () => {
      setUpdateTrigger(prev => prev + 1);
    };

    // Escuchar eventos de actualización de moneda
    window.addEventListener('currencyUpdate', handleCurrencyUpdate);
    window.addEventListener('ratesUpdated', handleRatesUpdated);
    window.addEventListener('currencyChanged', handleCurrencyUpdate);
    window.addEventListener('exchangeTypeChanged', handleCurrencyUpdate);

    return () => {
      window.removeEventListener('currencyUpdate', handleCurrencyUpdate);
      window.removeEventListener('ratesUpdated', handleRatesUpdated);
      window.removeEventListener('currencyChanged', handleCurrencyUpdate);
      window.removeEventListener('exchangeTypeChanged', handleCurrencyUpdate);
    };
  }, []);

  return updateTrigger;
};
