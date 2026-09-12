import api from './api';

export interface ClimaActualDto {
  temperature: number;
  humidity: number;
  windSpeed: number;
  precipitation: number;
  weatherCode: number;
  weatherDescription: string;
  icon: string;
}

export interface PronosticoDiaDto {
  date: string;
  dayOfWeek: string;
  maxTemperature: number;
  minTemperature: number;
  precipitation: number;
  weatherCode: number;
  weatherDescription: string;
  icon: string;
  agriculturalAdvice?: string;
}

export interface ClimaRespuestaDto {
  current: ClimaActualDto;
  forecast: PronosticoDiaDto[];
  location: string;
  lastUpdated?: string;
}

export const climaService = {
  async obtenerPorCoordenadas(latitude: number, longitude: number): Promise<ClimaRespuestaDto> {
    const { data } = await api.get<ClimaRespuestaDto>('/v1/weather/coordinates', {
      params: { latitude, longitude },
    });
    return data;
  },
};
