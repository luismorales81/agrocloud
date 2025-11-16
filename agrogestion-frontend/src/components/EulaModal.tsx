import React, { useState, useEffect } from 'react';
import { eulaService } from '../services/apiServices';

interface EulaModalProps {
  email: string;
  onAceptar: () => void;
  onCancelar?: () => void;
}

const EulaModal: React.FC<EulaModalProps> = ({ email, onAceptar, onCancelar }) => {
  const [aceptado, setAceptado] = useState(false);
  const [cargando, setCargando] = useState(false);
  const [textoEula, setTextoEula] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    cargarTextoEula();
  }, []);

  const cargarTextoEula = async () => {
    try {
      const data = await eulaService.obtenerTexto();
      setTextoEula(data.texto || '');
    } catch (err: any) {
      // Solo loggear si es un error real (no 401/403 esperados)
      if (err.response?.status !== 401 && err.response?.status !== 403) {
        console.error('Error cargando texto EULA:', err);
      }
      setTextoEula('Error al cargar el texto del EULA. Por favor, intente nuevamente.');
    }
  };

  const handleAceptar = async () => {
    if (!aceptado) {
      setError('Debe aceptar los términos y condiciones para continuar');
      return;
    }

    setCargando(true);
    setError('');

    try {
      console.log('📄 [EulaModal] Aceptando EULA para:', email);
      const resultado = await eulaService.aceptarEula(email, true);
      console.log('✅ [EulaModal] EULA aceptado exitosamente:', resultado);
      onAceptar();
    } catch (err: any) {
      console.error('❌ [EulaModal] Error aceptando EULA:', err);
      console.error('❌ [EulaModal] Error response:', err.response);
      console.error('❌ [EulaModal] Error data:', err.response?.data);
      setError(err.response?.data?.message || err.response?.data?.error || 'Error al aceptar el EULA. Por favor, intente nuevamente.');
    } finally {
      setCargando(false);
    }
  };

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0, 0, 0, 0.7)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 10000,
      padding: '20px'
    }}>
      <div style={{
        backgroundColor: 'white',
        borderRadius: '15px',
        maxWidth: '800px',
        width: '100%',
        maxHeight: '90vh',
        display: 'flex',
        flexDirection: 'column',
        boxShadow: '0 20px 60px rgba(0,0,0,0.3)'
      }}>
        {/* Header */}
        <div style={{
          padding: '25px',
          borderBottom: '2px solid #e0e0e0',
          backgroundColor: '#f8f9fa'
        }}>
          <h2 style={{
            margin: 0,
            color: '#333',
            fontSize: '24px',
            fontWeight: 'bold'
          }}>
            📄 Acuerdo de Licencia de Usuario Final (EULA)
          </h2>
          <p style={{
            margin: '10px 0 0 0',
            color: '#666',
            fontSize: '14px'
          }}>
            Versión 1.0 - Agrocloud
          </p>
        </div>

        {/* Contenido */}
        <div style={{
          padding: '25px',
          overflowY: 'auto',
          flex: 1,
          backgroundColor: '#fff'
        }}>
          <div style={{
            backgroundColor: '#f8f9fa',
            padding: '20px',
            borderRadius: '8px',
            marginBottom: '20px',
            border: '1px solid #e0e0e0',
            maxHeight: '400px',
            overflowY: 'auto',
            fontSize: '14px',
            lineHeight: '1.6',
            color: '#333',
            whiteSpace: 'pre-wrap',
            fontFamily: 'system-ui, -apple-system, sans-serif'
          }}>
            {textoEula || 'Cargando términos y condiciones...'}
          </div>

          {/* Checkbox de aceptación */}
          <div style={{
            padding: '20px',
            backgroundColor: '#fff3cd',
            borderRadius: '8px',
            border: '2px solid #ffc107',
            marginBottom: '20px'
          }}>
            <label style={{
              display: 'flex',
              alignItems: 'flex-start',
              cursor: 'pointer',
              fontSize: '15px',
              fontWeight: '500',
              color: '#333'
            }}>
              <input
                type="checkbox"
                checked={aceptado}
                onChange={(e) => {
                  setAceptado(e.target.checked);
                  setError('');
                }}
                style={{
                  marginRight: '12px',
                  marginTop: '3px',
                  width: '20px',
                  height: '20px',
                  cursor: 'pointer',
                  flexShrink: 0
                }}
              />
              <span>
                <strong>Declaro haber leído, comprendido y aceptado íntegramente</strong> el presente 
                Acuerdo de Licencia de Usuario Final (EULA). Acepto que mi dirección IP, fecha y hora 
                de aceptación quedarán registradas como evidencia de mi consentimiento.
              </span>
            </label>
          </div>

          {error && (
            <div style={{
              padding: '12px',
              backgroundColor: '#f8d7da',
              color: '#721c24',
              borderRadius: '6px',
              marginBottom: '15px',
              border: '1px solid #f5c6cb',
              fontSize: '14px'
            }}>
              ⚠️ {error}
            </div>
          )}

          <div style={{
            fontSize: '12px',
            color: '#666',
            fontStyle: 'italic',
            marginTop: '15px',
            padding: '10px',
            backgroundColor: '#f8f9fa',
            borderRadius: '6px'
          }}>
            <strong>Nota legal:</strong> Esta aceptación tiene validez legal según el Código Civil y 
            Comercial de la República Argentina (arts. 1100-1103 sobre contratos electrónicos). 
            Al aceptar, usted está celebrando un contrato vinculante.
          </div>
        </div>

        {/* Footer con botones */}
        <div style={{
          padding: '20px 25px',
          borderTop: '2px solid #e0e0e0',
          backgroundColor: '#f8f9fa',
          display: 'flex',
          justifyContent: 'flex-end',
          gap: '12px'
        }}>
          {onCancelar && (
            <button
              onClick={onCancelar}
              disabled={cargando}
              style={{
                padding: '12px 24px',
                backgroundColor: '#6c757d',
                color: 'white',
                border: 'none',
                borderRadius: '8px',
                cursor: cargando ? 'not-allowed' : 'pointer',
                fontSize: '15px',
                fontWeight: '500',
                opacity: cargando ? 0.6 : 1
              }}
            >
              Cancelar
            </button>
          )}
          <button
            onClick={handleAceptar}
            disabled={!aceptado || cargando}
            style={{
              padding: '12px 32px',
              backgroundColor: aceptado && !cargando ? '#28a745' : '#6c757d',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              cursor: aceptado && !cargando ? 'pointer' : 'not-allowed',
              fontSize: '15px',
              fontWeight: '600',
              opacity: aceptado && !cargando ? 1 : 0.6,
              transition: 'all 0.3s ease'
            }}
          >
            {cargando ? 'Procesando...' : 'Aceptar y Continuar'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default EulaModal;

