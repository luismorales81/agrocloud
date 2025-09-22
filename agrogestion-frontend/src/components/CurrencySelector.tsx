import React, { useEffect, useState } from 'react';
import { useCurrencyContext } from '../contexts/CurrencyContext';
import { currencyService } from '../services/CurrencyService';

const CurrencySelector: React.FC = () => {
  const { selectedCurrency, exchangeType, changeCurrency, changeExchangeType } = useCurrencyContext();
  const [rates, setRates] = useState<{ oficial: number; blue: number } | null>(null);

  useEffect(() => {
    const loadRates = async () => {
      try {
        await currencyService.updateCurrencyRates();
        const realRates = currencyService.getRealDolarRates();
        setRates(realRates);
      } catch (error) {
        console.error('❌ [CurrencySelector] Error cargando tasas:', error);
      }
    };
    
    loadRates();
  }, []);

  const handleCurrencyChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const value = event.target.value;
    if (value === 'ARS') {
      changeCurrency('ARS');
    } else if (value === 'oficial' || value === 'blue') {
      changeCurrency('USD');
      changeExchangeType(value);
    }
    
    // Forzar actualización inmediata
    setTimeout(() => {
      window.dispatchEvent(new Event('currencyUpdate'));
    }, 100);
  };

  const getDisplayText = () => {
    if (selectedCurrency === 'ARS') {
      return 'ARS (Pesos)';
    }
    
    if (rates) {
      const rate = exchangeType === 'oficial' ? rates.oficial : rates.blue;
      return `USD ${exchangeType === 'oficial' ? 'Oficial' : 'Blue'} (${rate.toFixed(2)})`;
    }
    
    return `USD ${exchangeType === 'oficial' ? 'Oficial' : 'Blue'}`;
  };

  return (
    <div style={{
      position: 'fixed',
      top: '2rem',
      right: '12rem', // Ajustado para estar más a la izquierda del botón de cerrar sesión
      zIndex: 1000,
      backgroundColor: 'white',
      borderRadius: '0.375rem',
      boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
      border: '1px solid #e5e7eb',
      padding: '0.5rem'
    }}>
      <select
        value={selectedCurrency === 'ARS' ? 'ARS' : exchangeType}
        onChange={handleCurrencyChange}
        style={{
          border: 'none',
          outline: 'none',
          fontSize: '0.875rem',
          fontWeight: '500',
          color: '#374151',
          backgroundColor: 'transparent',
          cursor: 'pointer',
          minWidth: '140px'
        }}
      >
        <option value="ARS">ARS (Pesos)</option>
        <option value="oficial">
          USD Oficial {rates ? `(${rates.oficial.toFixed(2)})` : ''}
        </option>
        <option value="blue">
          USD Blue {rates ? `(${rates.blue.toFixed(2)})` : ''}
        </option>
      </select>
    </div>
  );
};

export default CurrencySelector;
