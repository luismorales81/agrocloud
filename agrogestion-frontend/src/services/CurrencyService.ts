// CurrencyService con integración de DolarAPI
export interface CurrencyConfig {
  primaryCurrency: 'ARS' | 'USD' | 'EUR';
  autoUpdate: boolean;
  updateInterval: number;
  exchangeType: 'oficial' | 'blue';
}

export interface DolarApiResponse {
  oficial: {
    value_avg: number;
    value_sell: number;
    value_buy: number;
  };
  blue: {
    value_avg: number;
    value_sell: number;
    value_buy: number;
  };
  last_update: string;
}

// Interfaz para la respuesta real de DolarAPI
export interface DolarApiRealResponse {
  moneda: string;
  casa: string;
  nombre: string;
  compra: number;
  venta: number;
  fechaActualizacion: string;
}

class CurrencyService {
  private config: CurrencyConfig = {
    primaryCurrency: 'ARS',
    autoUpdate: true,
    updateInterval: 30,
    exchangeType: 'oficial'
  };

  private lastUpdate: Date = new Date();
  private rates = new Map<string, number>([
    ['USD-ARS', 850],
    ['ARS-USD', 1/850],
    ['EUR-ARS', 920],
    ['ARS-EUR', 1/920]
  ]);

  private dolarApiData: DolarApiResponse | null = null;
  private realDolarRates: { oficial: number; blue: number } | null = null;

  public getConfig(): CurrencyConfig {
    return { ...this.config };
  }

  public getCurrencyConfig(): CurrencyConfig {
    return { ...this.config };
  }

  public setCurrencyConfig(config: CurrencyConfig) {
    this.config = { ...config };
    localStorage.setItem('currencyConfig', JSON.stringify(config));
  }

  public formatCurrency(amount: number, currency: string, locale: string = 'es-AR'): string {
    return new Intl.NumberFormat(locale, {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(amount);
  }

  public convert(amount: number, fromCurrency: string, toCurrency: string): number {
    if (fromCurrency === toCurrency) return amount;
    
    // Si estamos convirtiendo USD a ARS, usar DolarAPI real
    if (fromCurrency === 'USD' && toCurrency === 'ARS' && this.realDolarRates) {
      const rate = this.config.exchangeType === 'oficial' 
        ? this.realDolarRates.oficial 
        : this.realDolarRates.blue;
      const result = amount * rate;
      return result;
    }
    
    // Si estamos convirtiendo ARS a USD, usar DolarAPI real
    if (fromCurrency === 'ARS' && toCurrency === 'USD' && this.realDolarRates) {
      const rate = this.config.exchangeType === 'oficial' 
        ? this.realDolarRates.oficial 
        : this.realDolarRates.blue;
      const result = amount / rate;
      return result;
    }
    
    // Para otras conversiones, usar rates hardcodeados
    const rate = this.rates.get(`${fromCurrency}-${toCurrency}`);
    const result = rate ? amount * rate : amount;
    return result;
  }

  public convertAndFormat(amount: number, fromCurrency: string, toCurrency: string, locale: string = 'es-AR'): string {
    const converted = this.convert(amount, fromCurrency, toCurrency);
    return this.formatCurrency(converted, toCurrency, locale);
  }

  public updateConfig(newConfig: Partial<CurrencyConfig>) {
    this.config = { ...this.config, ...newConfig };
    localStorage.setItem('currencyConfig', JSON.stringify(this.config));
  }

  public async updateCurrencyRates(): Promise<void> {
    try {
      
      const response = await fetch('https://dolarapi.com/v1/dolares');
      if (!response.ok) {
        throw new Error(`Error HTTP: ${response.status}`);
      }
      
      const data: DolarApiRealResponse[] = await response.json();
      
      // Extraer valores oficiales y blue de la respuesta real
      let oficialRate = 850; // valor por defecto
      let blueRate = 1200; // valor por defecto
      
      data.forEach(item => {
        
        if (item.casa === 'oficial') {
          oficialRate = (item.compra + item.venta) / 2;
        }
        if (item.casa === 'blue') {
          blueRate = (item.compra + item.venta) / 2;
        }
      });
      
      // Guardar las tasas reales
      this.realDolarRates = {
        oficial: oficialRate,
        blue: blueRate
      };
      
      this.lastUpdate = new Date();
      
      // Actualizar rates con los nuevos valores
      this.rates.set('USD-ARS', oficialRate);
      this.rates.set('ARS-USD', 1 / oficialRate);
      this.rates.set('USD-ARS-BLUE', blueRate);
      this.rates.set('ARS-USD-BLUE', 1 / blueRate);
      
      
    } catch (error) {
      console.error('❌ [CurrencyService] Error actualizando tasas de cambio:', error);
      // Usar valores por defecto si falla la API
      this.realDolarRates = {
        oficial: 850,
        blue: 1200
      };
      throw error;
    }
  }

  public getDefaultRateInfo(): { rate: number; lastUpdate: string; from: string; to: string; exchangeType: string } | null {
    if (!this.realDolarRates) {
      return {
        rate: 850,
        lastUpdate: this.lastUpdate.toISOString(),
        from: 'USD',
        to: 'ARS',
        exchangeType: this.config.exchangeType
      };
    }

    const rate = this.config.exchangeType === 'oficial' 
      ? this.realDolarRates.oficial 
      : this.realDolarRates.blue;


    return {
      rate,
      lastUpdate: this.lastUpdate.toISOString(),
      from: 'USD',
      to: 'ARS',
      exchangeType: this.config.exchangeType
    };
  }

  public formatRate(rate: number): string {
    const exchangeType = this.config.exchangeType === 'oficial' ? 'Oficial' : 'Blue';
    return `1 USD = ${rate.toFixed(2)} ARS (${exchangeType})`;
  }

  public getLastUpdate(): Date | null {
    return this.lastUpdate;
  }

  public isUpToDate(): boolean {
    const now = new Date();
    const diff = now.getTime() - this.lastUpdate.getTime();
    return diff < 30 * 60 * 1000; // 30 minutos
  }

  public async forceUpdate(): Promise<void> {
    return this.updateCurrencyRates();
  }

  public getAllRates(): Map<string, number> {
    return new Map(this.rates);
  }

  public getSupportedCurrencies(): string[] {
    return ['ARS', 'USD', 'EUR'];
  }

  public getRateInfo(fromCurrency: string, toCurrency: string) {
    if (fromCurrency === 'USD' && toCurrency === 'ARS' && this.realDolarRates) {
      const rate = this.config.exchangeType === 'oficial' 
        ? this.realDolarRates.oficial 
        : this.realDolarRates.blue;
      
      return {
        rate,
        lastUpdate: this.lastUpdate.toISOString(),
        from: fromCurrency,
        to: toCurrency,
        exchangeType: this.config.exchangeType
      };
    }
    
    const rate = this.rates.get(`${fromCurrency}-${toCurrency}`);
    return rate ? {
      rate,
      lastUpdate: this.lastUpdate.toISOString(),
      from: fromCurrency,
      to: toCurrency,
      exchangeType: this.config.exchangeType
    } : null;
  }

  public getDolarApiData(): DolarApiResponse | null {
    return this.dolarApiData;
  }

  public getRealDolarRates(): { oficial: number; blue: number } | null {
    return this.realDolarRates;
  }

  public setExchangeType(type: 'oficial' | 'blue') {
    this.config.exchangeType = type;
    localStorage.setItem('currencyConfig', JSON.stringify(this.config));
  }

  public getExchangeType(): 'oficial' | 'blue' {
    return this.config.exchangeType;
  }

  // Cargar configuración guardada al inicializar
  public loadSavedConfig() {
    const saved = localStorage.getItem('currencyConfig');
    if (saved) {
      try {
        this.config = { ...this.config, ...JSON.parse(saved) };
      } catch (error) {
        console.error('❌ [CurrencyService] Error cargando configuración de moneda:', error);
      }
    }
  }
}

export const currencyService = new CurrencyService();
export default currencyService;
