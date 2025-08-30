// CurrencyService simplificado para debugging
export interface CurrencyConfig {
  primaryCurrency: 'ARS' | 'USD' | 'EUR';
  autoUpdate: boolean;
  updateInterval: number;
}

class SimpleCurrencyService {
  private config: CurrencyConfig = {
    primaryCurrency: 'ARS',
    autoUpdate: true,
    updateInterval: 30
  };

  private lastUpdate: Date = new Date();
  private rates = new Map<string, number>([
    ['USD-ARS', 850],
    ['ARS-USD', 1/850],
    ['EUR-ARS', 920],
    ['ARS-EUR', 1/920]
  ]);

  public getConfig(): CurrencyConfig {
    return { ...this.config };
  }

  public getCurrencyConfig(): CurrencyConfig {
    return { ...this.config };
  }

  public setCurrencyConfig(config: CurrencyConfig) {
    this.config = { ...config };
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
    const rate = this.rates.get(`${fromCurrency}-${toCurrency}`);
    return rate ? amount * rate : amount;
  }

  public convertAndFormat(amount: number, fromCurrency: string, toCurrency: string, locale: string = 'es-AR'): string {
    const converted = this.convert(amount, fromCurrency, toCurrency);
    return this.formatCurrency(converted, toCurrency, locale);
  }

  public updateConfig(newConfig: Partial<CurrencyConfig>) {
    this.config = { ...this.config, ...newConfig };
  }

  public async updateCurrencyRates(): Promise<void> {
    console.log('Actualizando tasas de cambio...');
    this.lastUpdate = new Date();
    return Promise.resolve();
  }

  public getDefaultRateInfo(): { rate: number; lastUpdate: string; from: string; to: string } | null {
    return {
      rate: 850,
      lastUpdate: this.lastUpdate.toISOString(),
      from: 'USD',
      to: 'ARS'
    };
  }

  public formatRate(rate: number): string {
    return `1 USD = ${rate.toFixed(2)} ARS`;
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
    const rate = this.rates.get(`${fromCurrency}-${toCurrency}`);
    return rate ? {
      rate,
      lastUpdate: this.lastUpdate.toISOString(),
      from: fromCurrency,
      to: toCurrency
    } : null;
  }
}

export const currencyService = new SimpleCurrencyService();
export default currencyService;
