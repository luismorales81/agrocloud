import { useCallback, useEffect, useState } from 'react';
import {
  chatIaService,
  ChatIaEstado,
  ChatIaMensajeRespuesta,
  MensajeChatIa,
} from '../services/chatIaService';

const CLAVE_SESION = 'chat-ia-mensajes-sesion';

function cargarMensajesSesion(): MensajeChatIa[] {
  try {
    const raw = sessionStorage.getItem(CLAVE_SESION);
    if (!raw) return [];
    return JSON.parse(raw) as MensajeChatIa[];
  } catch {
    return [];
  }
}

function guardarMensajesSesion(mensajes: MensajeChatIa[]) {
  sessionStorage.setItem(CLAVE_SESION, JSON.stringify(mensajes));
}

export function useChatIa(moduloActivo?: string) {
  const [estado, setEstado] = useState<ChatIaEstado | null>(null);
  const [mensajes, setMensajes] = useState<MensajeChatIa[]>(cargarMensajesSesion);
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const recargarEstado = useCallback(async () => {
    try {
      const data = await chatIaService.obtenerEstado();
      setEstado(data);
    } catch {
      try {
        const config = await chatIaService.obtenerConfiguracion();
        setEstado({
          habilitado: config.claveConfigurada && config.activo,
          modelo: config.modelo || 'gemini-flash-latest',
          claveConfigurada: config.claveConfigurada,
          claveInvalida: config.claveInvalida,
        });
      } catch {
        setEstado({
          habilitado: false,
          modelo: 'gemini-flash-latest',
          claveConfigurada: false,
          claveInvalida: false,
        });
      }
    }
  }, []);

  useEffect(() => {
    recargarEstado();
  }, [recargarEstado]);

  useEffect(() => {
    guardarMensajesSesion(mensajes);
  }, [mensajes]);

  const enviarMensaje = useCallback(
    async (texto: string): Promise<ChatIaMensajeRespuesta | null> => {
      if (!texto.trim()) return null;
      const mensajeUsuario: MensajeChatIa = { rol: 'usuario', contenido: texto.trim() };
      const historialPrevio = [...mensajes];
      setMensajes((prev) => [...prev, mensajeUsuario]);
      setCargando(true);
      setError(null);
      try {
        const respuesta = await chatIaService.enviarMensaje(
          texto.trim(),
          historialPrevio,
          moduloActivo
        );
        setMensajes((prev) => [
          ...prev,
          {
            rol: 'asistente',
            contenido: respuesta.respuesta,
            herramientasUsadas: respuesta.herramientasUsadas,
          },
        ]);
        if (!respuesta.iaDisponible && respuesta.error) {
          setError(respuesta.error);
          await recargarEstado();
        }
        return respuesta;
      } catch (e: unknown) {
        const msg = e instanceof Error ? e.message : 'Error al enviar el mensaje';
        setError(msg);
        setMensajes((prev) => [
          ...prev,
          {
            rol: 'asistente',
            contenido: 'No se pudo completar la consulta. ' + msg,
          },
        ]);
        return null;
      } finally {
        setCargando(false);
      }
    },
    [mensajes, moduloActivo, recargarEstado]
  );

  const limpiarChat = useCallback(() => {
    setMensajes([]);
    sessionStorage.removeItem(CLAVE_SESION);
  }, []);

  return {
    estado,
    mensajes,
    cargando,
    error,
    enviarMensaje,
    limpiarChat,
    recargarEstado,
  };
}
