import api from './api';

export interface WeatherCurrent {
  temperature: number;
  humidity: number;
  windSpeed: number;
  precipitation: number;
  weatherCode: number;
  weatherDescription: string;
  icon: string;
}

export interface WeatherForecast {
  date: string;
  dayOfWeek: string;
  maxTemperature: number;
  minTemperature: number;
  precipitation: number;
  weatherCode: number;
  weatherDescription: string;
  icon: string;
  agriculturalAdvice: string;
}

export interface WeatherData {
  current: WeatherCurrent;
  forecast: WeatherForecast[];
  location: string;
  lastUpdated: string;
}

class WeatherService {
  private cache = new Map<string, { data: WeatherData; timestamp: number }>();
  private readonly CACHE_DURATION = 30 * 60 * 1000; // 30 minutos en milisegundos

  /**
   * Obtiene datos meteorológicos por coordenadas con cache
   */
  async getWeatherByCoordinates(latitude: number, longitude: number): Promise<WeatherData> {
    const cacheKey = `${latitude.toFixed(4)}_${longitude.toFixed(4)}`;
    const now = Date.now();
    
    // Verificar si hay datos en cache y si aún son válidos
    const cached = this.cache.get(cacheKey);
    if (cached && (now - cached.timestamp) < this.CACHE_DURATION) {
      return cached.data;
    }

    try {
      console.log(`🌤️ [WeatherService] Obteniendo clima REAL para lat: ${latitude}, lon: ${longitude}`);
      
      // Llamar al endpoint real de Open-Meteo
      const response = await api.get(`/v1/weather-simple/coordinates?latitude=${latitude}&longitude=${longitude}`);
      console.log('🌤️ [WeatherService] Datos meteorológicos REALES obtenidos:', response.data);
      
      // Guardar en cache
      this.cache.set(cacheKey, { data: response.data, timestamp: now });
      console.log(`💾 [WeatherService] Datos guardados en cache para lat: ${latitude}, lon: ${longitude}`);
      
      return response.data;
      
    } catch (error) {
      console.error('❌ [WeatherService] Error obteniendo clima real:', error);
      
      // Fallback: intentar con el endpoint simple
      try {
        console.log('🔄 [WeatherService] Intentando con endpoint simple...');
        const fallbackResponse = await api.get(`/v1/weather-simple/coordinates-simple?latitude=${latitude}&longitude=${longitude}`);
        console.log('🌤️ [WeatherService] Datos meteorológicos obtenidos con fallback:', fallbackResponse.data);
        
        // Guardar en cache también el fallback
        this.cache.set(cacheKey, { data: fallbackResponse.data, timestamp: now });
        console.log(`💾 [WeatherService] Datos de fallback guardados en cache para lat: ${latitude}, lon: ${longitude}`);
        
        return fallbackResponse.data;
      } catch (fallbackError) {
        console.error('❌ [WeatherService] Error en fallback también:', fallbackError);
        throw new Error('Error obteniendo datos meteorológicos reales');
      }
    }
  }

  /**
   * Limpia el cache expirado
   */
  clearExpiredCache(): void {
    const now = Date.now();
    for (const [key, value] of this.cache.entries()) {
      if ((now - value.timestamp) >= this.CACHE_DURATION) {
        this.cache.delete(key);
        console.log(`🗑️ [WeatherService] Cache expirado eliminado para key: ${key}`);
      }
    }
  }

  /**
   * Limpia todo el cache
   */
  clearAllCache(): void {
    this.cache.clear();
    console.log('🗑️ [WeatherService] Todo el cache eliminado');
  }

  /**
   * Obtiene información del cache
   */
  getCacheInfo(): { size: number; keys: string[]; oldestEntry?: number } {
    const keys = Array.from(this.cache.keys());
    let oldestEntry: number | undefined;
    
    if (keys.length > 0) {
      oldestEntry = Math.min(...Array.from(this.cache.values()).map(v => v.timestamp));
    }
    
    return {
      size: this.cache.size,
      keys,
      oldestEntry
    };
  }

  /**
   * Obtiene datos meteorológicos por ID de campo con cache
   */
  async getWeatherByField(fieldId: number): Promise<WeatherData> {
    const cacheKey = `field_${fieldId}`;
    const now = Date.now();
    
    // Verificar si hay datos en cache y si aún son válidos
    const cached = this.cache.get(cacheKey);
    if (cached && (now - cached.timestamp) < this.CACHE_DURATION) {
      console.log(`🔄 [WeatherService] Usando datos en cache para campo ID: ${fieldId}`);
      return cached.data;
    }

    try {
      console.log(`🌤️ [WeatherService] Obteniendo clima para campo ID: ${fieldId}`);
      
      const response = await api.get(`/v1/weather-simple/field/${fieldId}`);
      
      console.log('🌤️ [WeatherService] Datos meteorológicos obtenidos:', response.data);
      
      // Guardar en cache
      this.cache.set(cacheKey, { data: response.data, timestamp: now });
      console.log(`💾 [WeatherService] Datos guardados en cache para campo ID: ${fieldId}`);
      
      return response.data;
      
    } catch (error) {
      console.error('❌ [WeatherService] Error obteniendo clima para campo:', error);
      throw new Error('Error obteniendo datos meteorológicos del campo');
    }
  }

  /**
   * Verifica el estado del servicio meteorológico
   */
  async checkHealth(): Promise<boolean> {
    try {
      const response = await api.get('/v1/weather-simple/health');
      return response.status === 200;
    } catch (error) {
      console.error('❌ [WeatherService] Error verificando salud del servicio:', error);
      return false;
    }
  }

  /**
   * Obtiene consejos agrícolas basados en las condiciones meteorológicas
   */
  getAgriculturalAdvice(current: WeatherCurrent): string {
    const temp = current.temperature;
    const humidity = current.humidity;
    const windSpeed = current.windSpeed;
    const precipitation = current.precipitation;

    if (temp < 5) {
      return '❄️ Temperatura muy baja - Evitar labores sensibles al frío';
    }
    if (temp < 10) {
      return '❄️ Temperatura baja - Considerar retrasar labores sensibles';
    }
    if (temp > 35) {
      return '🌡️ Temperatura muy alta - Evitar labores en horas pico';
    }
    if (temp > 30) {
      return '🌡️ Temperatura alta - Programar labores temprano o tarde';
    }
    if (humidity > 85) {
      return '💧 Humedad muy alta - Riesgo de enfermedades fúngicas';
    }
    if (humidity > 75) {
      return '💧 Humedad alta - Monitorear condiciones sanitarias';
    }
    if (windSpeed > 25) {
      return '💨 Viento muy fuerte - No recomendable para pulverizaciones';
    }
    if (windSpeed > 15) {
      return '💨 Viento fuerte - Cuidado con aplicaciones fitosanitarias';
    }
    if (precipitation > 5) {
      return '🌧️ Lluvia intensa - Evitar labores de campo';
    }
    if (precipitation > 0) {
      return '🌧️ Lluvia - Considerar retrasar labores sensibles';
    }

    return '✅ Condiciones favorables para labores agrícolas';
  }

  /**
   * Obtiene el color de fondo basado en la temperatura
   */
  getTemperatureColor(temperature: number): string {
    if (temperature < 5) return '#e3f2fd'; // Azul claro - frío
    if (temperature < 15) return '#f3e5f5'; // Púrpura claro - fresco
    if (temperature < 25) return '#e8f5e8'; // Verde claro - templado
    if (temperature < 30) return '#fff3e0'; // Naranja claro - cálido
    return '#ffebee'; // Rojo claro - caluroso
  }

  /**
   * Formatea la velocidad del viento
   */
  formatWindSpeed(windSpeed: number): string {
    if (windSpeed < 5) return 'Calma';
    if (windSpeed < 10) return 'Ligero';
    if (windSpeed < 15) return 'Moderado';
    if (windSpeed < 20) return 'Fuerte';
    return 'Muy fuerte';
  }

  /**
   * Formatea la precipitación
   */
  formatPrecipitation(precipitation: number): string {
    if (precipitation === 0) return 'Sin lluvia';
    if (precipitation < 1) return 'Llovizna';
    if (precipitation < 5) return 'Lluvia ligera';
    if (precipitation < 10) return 'Lluvia moderada';
    return 'Lluvia intensa';
  }
}

export default new WeatherService();
