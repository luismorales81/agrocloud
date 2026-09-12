import api from './api';

export interface ChatIaEstado {
  habilitado: boolean;
  modelo: string;
  claveConfigurada: boolean;
  claveInvalida: boolean;
  modelosDisponibles?: string[];
}

export interface ChatIaConfiguracion {
  modelo: string;
  activo: boolean;
  claveConfigurada: boolean;
  claveEnmascarada?: string;
  claveInvalida: boolean;
}

export interface MensajeChatIa {
  rol: 'usuario' | 'asistente';
  contenido: string;
  herramientasUsadas?: string[];
}

export interface ChatIaMensajeRespuesta {
  respuesta: string;
  herramientasUsadas: string[];
  iaDisponible: boolean;
  error?: string;
}

export const chatIaService = {
  async obtenerEstado(): Promise<ChatIaEstado> {
    const { data } = await api.get<ChatIaEstado>('/chat-ia/estado');
    return data;
  },

  async obtenerConfiguracion(): Promise<ChatIaConfiguracion> {
    const { data } = await api.get<ChatIaConfiguracion>('/chat-ia/configuracion');
    return data;
  },

  async guardarConfiguracion(claveApi: string, modelo: string): Promise<ChatIaConfiguracion> {
    const { data } = await api.put<ChatIaConfiguracion>('/chat-ia/configuracion', {
      claveApi: claveApi || undefined,
      modelo,
    });
    return data;
  },

  async eliminarConfiguracion(): Promise<void> {
    await api.delete('/chat-ia/configuracion');
  },

  async enviarMensaje(
    mensaje: string,
    historial: MensajeChatIa[],
    moduloActivo?: string
  ): Promise<ChatIaMensajeRespuesta> {
    const { data } = await api.post<ChatIaMensajeRespuesta>('/chat-ia/mensaje', {
      mensaje,
      historial: historial.map((m) => ({
        rol: m.rol,
        contenido: m.contenido,
      })),
      moduloActivo,
    });
    return data;
  },
};
