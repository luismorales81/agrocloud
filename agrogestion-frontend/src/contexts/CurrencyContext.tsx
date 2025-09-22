import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { currencyService } from '../services/CurrencyService';

interface CurrencyContextType {
  selectedCurrency: 'ARS' | 'USD' | 'EUR';
  exchangeType: 'oficial' | 'blue';
  formatCurrency: (amount: number) => string;
  convertCurrency: (amount: number, fromCurrency: string, toCurrency: string) => number;
  changeCurrency: (currency: 'ARS' | 'USD' | 'EUR') => void;
  changeExchangeType: (type: 'oficial' | 'blue') => void;
  updateRates: () => Promise<void>;
  rateInfo: any;
  loading: boolean;
}

const CurrencyContext = createContext<CurrencyContextType | undefined>(undefined);

export const useCurrencyContext = () => {
  const context = useContext(CurrencyContext);
  if (context === undefined) {
    throw new Error('useCurrencyContext must be used within a CurrencyProvider');
  }
  return context;
};

interface CurrencyProviderProps {
  children: ReactNode;
}

export const CurrencyProvider: React.FC<CurrencyProviderProps> = ({ children }) => {
  const [selectedCurrency, setSelectedCurrency] = useState<'ARS' | 'USD' | 'EUR'>(
    (localStorage.getItem('selectedCurrency') as 'ARS' | 'USD' | 'EUR') || 'ARS'
  );
  
  const [exchangeType, setExchangeType] = useState<'oficial' | 'blue'>(
    (localStorage.getItem('exchangeType') as 'oficial' | 'blue') || 'oficial'
  );
  
  const [rateInfo, setRateInfo] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  // Cargar configuración inicial
  useEffect(() => {
    currencyService.loadSavedConfig();
    loadInitialRates();
  }, []);

  const loadInitialRates = async () => {
    try {
      setLoading(true);
      await currencyService.updateCurrencyRates();
      const info = currencyService.getDefaultRateInfo();
      setRateInfo(info);
    } catch (error) {
      console.error('Error cargando tasas iniciales:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount: number) => {
    // Validar que amount sea un número válido
    if (isNaN(amount) || amount === null || amount === undefined) {
      return '$ 0,00';
    }
    
    // Si la moneda seleccionada es USD, convertir desde ARS
    if (selectedCurrency === 'USD') {
      const convertedAmount = currencyService.convert(amount, 'ARS', 'USD');
      return new Intl.NumberFormat('es-AR', {
        style: 'currency',
        currency: 'USD',
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      }).format(convertedAmount);
    }
    
    // Si es ARS, mostrar directamente
    return new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: 'ARS',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(amount);
  };

  const convertCurrency = (amount: number, fromCurrency: string, toCurrency: string) => {
    return currencyService.convert(amount, fromCurrency, toCurrency);
  };

  const changeCurrency = (currency: 'ARS' | 'USD' | 'EUR') => {
    setSelectedCurrency(currency);
    localStorage.setItem('selectedCurrency', currency);
    currencyService.updateConfig({ primaryCurrency: currency });
    
    // Disparar evento para notificar cambios
    window.dispatchEvent(new CustomEvent('currencyChanged', { detail: currency }));
    
    // Forzar re-renderizado de todos los componentes
    window.dispatchEvent(new Event('currencyUpdate'));
  };

  const changeExchangeType = (type: 'oficial' | 'blue') => {
    setExchangeType(type);
    localStorage.setItem('exchangeType', type);
    currencyService.setExchangeType(type);
    
    // Disparar evento para notificar cambios
    window.dispatchEvent(new CustomEvent('exchangeTypeChanged', { detail: type }));
    
    // Forzar re-renderizado de todos los componentes
    window.dispatchEvent(new Event('currencyUpdate'));
  };

  const updateRates = async () => {
    try {
      setLoading(true);
      await currencyService.updateCurrencyRates();
      const info = currencyService.getDefaultRateInfo();
      setRateInfo(info);
      
      // Disparar evento para notificar actualización de tasas
      window.dispatchEvent(new Event('ratesUpdated'));
    } catch (error) {
      console.error('Error actualizando tasas:', error);
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const value: CurrencyContextType = {
    selectedCurrency,
    exchangeType,
    formatCurrency,
    convertCurrency,
    changeCurrency,
    changeExchangeType,
    updateRates,
    rateInfo,
    loading
  };

  return (
    <CurrencyContext.Provider value={value}>
      {children}
    </CurrencyContext.Provider>
  );
};
