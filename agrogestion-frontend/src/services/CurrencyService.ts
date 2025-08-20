interface CurrencyRate {
  success: boolean;
  query: {
    from: string;
    to: string;
    amount: number;
  };
  info: {
    rate: number;
    timestamp: number;
  };
  date: string;
  result: number;
}

interface CurrencyConfig {
  currency: 'ARS' | 'USD';
  rate: number;
  lastUpdate: string;
}

class CurrencyService {
  private readonly API_URL = 'https://api.exchangerate.host/convert';
  private readonly STORAGE_KEY = 'currencyConfig';
  private readonly RATE_STORAGE_KEY = 'exchangeRate';

  async getExchangeRate(from: string = 'ARS', to: string = 'USD', amount: number = 1): Promise<CurrencyRate> {
    try {
      const url = `${this.API_URL}?from=${from}&to=${to}&amount=${amount}`;
      const response = await fetch(url);
      
      if (!response.ok) {
        throw new Error('Error al obtener cotización de cambio');
      }
      
      const data = await response.json();
      
      if (!data.success) {
        throw new Error(data.error?.info || 'Error en la respuesta del servicio');
      }
      
      return data;
    } catch (error) {
      console.error('Error obteniendo cotización:', error);
      throw error;
    }
  }

  async getCurrentRate(): Promise<number> {
    try {
      const rateData = await this.getExchangeRate('ARS', 'USD', 1);
      return rateData.info.rate;
    } catch (error) {
      console.error('Error obteniendo tasa actual:', error);
      // Tasa de respaldo en caso de error
      return 1000; // Tasa aproximada ARS/USD
    }
  }

  async updateCurrencyRates(): Promise<void> {
    try {
      const currentRate = await this.getCurrentRate();
      
      // Guardar la tasa actual
      const rateData = {
        rate: currentRate,
        lastUpdate: new Date().toISOString(),
        from: 'ARS',
        to: 'USD'
      };
      
      localStorage.setItem(this.RATE_STORAGE_KEY, JSON.stringify(rateData));
      
      // Actualizar configuración
      const config = this.getCurrencyConfig();
      config.lastUpdate = rateData.lastUpdate;
      this.setCurrencyConfig(config);
      
      console.log('Cotización actualizada:', currentRate);
    } catch (error) {
      console.error('Error actualizando cotización:', error);
    }
  }

  getCurrentRateFromStorage(): number {
    const stored = localStorage.getItem(this.RATE_STORAGE_KEY);
    if (stored) {
      const data = JSON.parse(stored);
      return data.rate;
    }
    return 1000; // Tasa por defecto
  }

  getCurrencyConfig(): CurrencyConfig {
    const stored = localStorage.getItem(this.STORAGE_KEY);
    if (stored) {
      return JSON.parse(stored);
    }
    
    // Configuración por defecto
    return {
      currency: 'ARS',
      rate: 1,
      lastUpdate: new Date().toISOString()
    };
  }

  setCurrencyConfig(config: CurrencyConfig): void {
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(config));
  }

  formatCurrency(amount: number, currency: 'ARS' | 'USD' = 'ARS'): string {
    const config = this.getCurrencyConfig();
    const currentRate = this.getCurrentRateFromStorage();
    
    let displayAmount = amount;
    let displayCurrency = currency;
    
    // Si la moneda configurada es diferente a la solicitada, convertir
    if (config.currency !== currency) {
      if (config.currency === 'USD' && currency === 'ARS') {
        // Convertir de ARS a USD
        displayAmount = amount / currentRate;
        displayCurrency = 'USD';
      } else if (config.currency === 'ARS' && currency === 'USD') {
        // Convertir de USD a ARS
        displayAmount = amount * currentRate;
        displayCurrency = 'ARS';
      }
    }
    
    const formatter = new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: displayCurrency,
      minimumFractionDigits: 0,
      maximumFractionDigits: 2
    });
    
    return formatter.format(displayAmount);
  }

  convertAmount(amount: number, fromCurrency: 'ARS' | 'USD', toCurrency: 'ARS' | 'USD'): number {
    if (fromCurrency === toCurrency) {
      return amount;
    }
    
    const currentRate = this.getCurrentRateFromStorage();
    
    if (fromCurrency === 'ARS' && toCurrency === 'USD') {
      return amount / currentRate;
    } else if (fromCurrency === 'USD' && toCurrency === 'ARS') {
      return amount * currentRate;
    }
    
    return amount;
  }

  // Método para obtener información de la cotización actual
  getRateInfo(): { rate: number; lastUpdate: string; from: string; to: string } | null {
    const stored = localStorage.getItem(this.RATE_STORAGE_KEY);
    return stored ? JSON.parse(stored) : null;
  }

  // Método para formatear la tasa de cambio
  formatRate(rate: number): string {
    return `1 USD = ${rate.toLocaleString('es-AR', { 
      minimumFractionDigits: 2, 
      maximumFractionDigits: 2 
    })} ARS`;
  }
}

export const currencyService = new CurrencyService();
export default currencyService;
