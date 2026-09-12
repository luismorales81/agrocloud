import React, { useRef, useEffect, useState } from 'react';
import { useModule } from '../../core/hooks/useModule';
import { useChatIa } from '../../hooks/useChatIa';
import PantallaConfiguracionIa from './PantallaConfiguracionIa';

const ChatIaWidget: React.FC = () => {
  const { currentModule } = useModule();
  const { estado, mensajes, cargando, enviarMensaje, limpiarChat, recargarEstado } = useChatIa(
    currentModule || undefined
  );
  const [abierto, setAbierto] = useState(false);
  const [configAbierta, setConfigAbierta] = useState(false);
  const [texto, setTexto] = useState('');
  const finListaRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (abierto && finListaRef.current) {
      finListaRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [mensajes, abierto, cargando]);

  useEffect(() => {
    const abrirConfig = () => setConfigAbierta(true);
    const abrirChat = () => setAbierto(true);
    window.addEventListener('abrir-config-chat-ia', abrirConfig);
    window.addEventListener('abrir-chat-ia', abrirChat);
    return () => {
      window.removeEventListener('abrir-config-chat-ia', abrirConfig);
      window.removeEventListener('abrir-chat-ia', abrirChat);
    };
  }, []);

  const handleEnviar = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!texto.trim() || cargando) return;
    const t = texto;
    setTexto('');
    await enviarMensaje(t);
  };

  const puedeUsarChat = estado?.claveConfigurada === true;

  return (
    <>
      {/* Botón flotante */}
      <button
        type="button"
        onClick={() => setAbierto((v) => !v)}
        title="Asistente IA"
        style={{
          position: 'fixed',
          bottom: '1.25rem',
          right: '1.25rem',
          zIndex: 1050,
          width: '3.5rem',
          height: '3.5rem',
          borderRadius: '50%',
          border: 'none',
          backgroundColor: puedeUsarChat ? '#2563eb' : '#6b7280',
          color: 'white',
          fontSize: '1.5rem',
          cursor: 'pointer',
          boxShadow: puedeUsarChat
            ? '0 4px 14px rgba(37, 99, 235, 0.45)'
            : '0 4px 14px rgba(107, 114, 128, 0.35)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        ✦
      </button>

      {/* Panel de chat */}
      {abierto && (
        <div
          style={{
            position: 'fixed',
            bottom: '5.5rem',
            right: '1.25rem',
            width: 'min(400px, calc(100vw - 2rem))',
            height: 'min(520px, calc(100vh - 8rem))',
            zIndex: 1050,
            backgroundColor: 'white',
            borderRadius: '0.75rem',
            boxShadow: '0 12px 40px rgba(0,0,0,0.18)',
            display: 'flex',
            flexDirection: 'column',
            overflow: 'hidden',
            border: '1px solid #e5e7eb',
          }}
        >
          <div
            style={{
              padding: '0.75rem 1rem',
              backgroundColor: '#1e40af',
              color: 'white',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
            }}
          >
            <div>
              <div style={{ fontWeight: 600, fontSize: '0.95rem' }}>Asistente AgroGestion</div>
              <div style={{ fontSize: '0.7rem', opacity: 0.85 }}>
                {estado?.modelo || 'gemini-flash-latest'} · solo datos productivos
              </div>
            </div>
            <div style={{ display: 'flex', gap: '0.25rem' }}>
              <button
                type="button"
                onClick={() => setConfigAbierta(true)}
                title="Configuración"
                style={{
                  background: 'transparent',
                  border: 'none',
                  color: 'white',
                  cursor: 'pointer',
                  fontSize: '1rem',
                  padding: '0.25rem',
                }}
              >
                ⚙
              </button>
              <button
                type="button"
                onClick={limpiarChat}
                title="Limpiar conversación"
                style={{
                  background: 'transparent',
                  border: 'none',
                  color: 'white',
                  cursor: 'pointer',
                  fontSize: '0.8rem',
                  padding: '0.25rem 0.5rem',
                }}
              >
                Limpiar
              </button>
              <button
                type="button"
                onClick={() => setAbierto(false)}
                style={{
                  background: 'transparent',
                  border: 'none',
                  color: 'white',
                  cursor: 'pointer',
                  fontSize: '1.25rem',
                  lineHeight: 1,
                }}
              >
                ×
              </button>
            </div>
          </div>

          {estado?.claveInvalida && (
            <div
              style={{
                padding: '0.5rem 0.75rem',
                backgroundColor: '#fef2f2',
                color: '#b91c1c',
                fontSize: '0.8rem',
              }}
            >
              Tu clave API parece inválida.{' '}
              <button
                type="button"
                onClick={() => setConfigAbierta(true)}
                style={{
                  background: 'none',
                  border: 'none',
                  color: '#1d4ed8',
                  cursor: 'pointer',
                  textDecoration: 'underline',
                }}
              >
                Actualizala
              </button>
            </div>
          )}

          {!puedeUsarChat ? (
            <div
              style={{
                flex: 1,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                padding: '1.5rem',
                backgroundColor: '#f9fafb',
                textAlign: 'center',
              }}
            >
              <p style={{ color: '#374151', fontSize: '0.9rem', margin: '0 0 0.75rem' }}>
                Para usar el asistente necesitás configurar tu clave API de Gemini.
              </p>
              <button
                type="button"
                onClick={() => setConfigAbierta(true)}
                style={{
                  padding: '0.5rem 1rem',
                  backgroundColor: '#2563eb',
                  color: 'white',
                  border: 'none',
                  borderRadius: '0.375rem',
                  cursor: 'pointer',
                  fontSize: '0.875rem',
                }}
              >
                Ir a configuración
              </button>
            </div>
          ) : (
            <>
          <div
            style={{
              flex: 1,
              overflowY: 'auto',
              padding: '0.75rem',
              backgroundColor: '#f9fafb',
            }}
          >
            {mensajes.length === 0 && (
              <p style={{ color: '#6b7280', fontSize: '0.875rem', margin: 0 }}>
                Preguntá sobre insumos, campos, lotes, labores, maquinaria, finanzas, inventario y datos de los
                módulos que tenés activos.
              </p>
            )}
            {mensajes.map((m, i) => (
              <div
                key={i}
                style={{
                  marginBottom: '0.75rem',
                  display: 'flex',
                  justifyContent: m.rol === 'usuario' ? 'flex-end' : 'flex-start',
                }}
              >
                <div
                  style={{
                    maxWidth: '85%',
                    padding: '0.5rem 0.75rem',
                    borderRadius: '0.625rem',
                    backgroundColor: m.rol === 'usuario' ? '#2563eb' : 'white',
                    color: m.rol === 'usuario' ? 'white' : '#111827',
                    fontSize: '0.875rem',
                    border: m.rol === 'asistente' ? '1px solid #e5e7eb' : 'none',
                    whiteSpace: 'pre-wrap',
                  }}
                >
                  {m.contenido}
                  {m.herramientasUsadas && m.herramientasUsadas.length > 0 && (
                    <div
                      style={{
                        marginTop: '0.35rem',
                        fontSize: '0.65rem',
                        opacity: 0.7,
                        borderTop: '1px solid #e5e7eb',
                        paddingTop: '0.25rem',
                      }}
                    >
                      Fuente: {m.herramientasUsadas.join(', ')}
                    </div>
                  )}
                </div>
              </div>
            ))}
            {cargando && (
              <p style={{ color: '#6b7280', fontSize: '0.8rem', fontStyle: 'italic' }}>
                Consultando datos...
              </p>
            )}
            <div ref={finListaRef} />
          </div>

          <form
            onSubmit={handleEnviar}
            style={{
              padding: '0.75rem',
              borderTop: '1px solid #e5e7eb',
              display: 'flex',
              gap: '0.5rem',
            }}
          >
            <input
              type="text"
              value={texto}
              onChange={(e) => setTexto(e.target.value)}
              placeholder="Escribí tu consulta..."
              disabled={cargando}
              style={{
                flex: 1,
                padding: '0.5rem 0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.5rem',
                fontSize: '0.875rem',
              }}
            />
            <button
              type="submit"
              disabled={cargando || !texto.trim()}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: '#2563eb',
                color: 'white',
                border: 'none',
                borderRadius: '0.5rem',
                cursor: cargando ? 'wait' : 'pointer',
                opacity: cargando || !texto.trim() ? 0.6 : 1,
              }}
            >
              Enviar
            </button>
          </form>
            </>
          )}
        </div>
      )}

      <PantallaConfiguracionIa
        abierto={configAbierta}
        onCerrar={() => setConfigAbierta(false)}
        onGuardado={() => {
          recargarEstado();
        }}
      />
    </>
  );
};

export default ChatIaWidget;
