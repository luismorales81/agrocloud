import React, { useState, useEffect } from 'react';
import { lotesService, configuracionEstadosService } from '../services/apiServices';
import { Icon } from './icons';

interface ResetLoteModalProps {
  lote: {
    id?: number;
    nombre: string;
    estado: string;
    estadoConfigurado?: {
      id: number;
      nombre: string;
      color: string;
      icono?: string;
    };
  };
  onClose: () => void;
  onSuccess: () => void;
}

interface EstadoInicial {
  id: number;
  nombre: string;
  descripcion?: string;
  color: string;
  icono?: string;
}

const ResetLoteModal: React.FC<ResetLoteModalProps> = ({ lote, onClose, onSuccess }) => {
  const [estadosIniciales, setEstadosIniciales] = useState<EstadoInicial[]>([]);
  const [estadoInicialSeleccionado, setEstadoInicialSeleccionado] = useState<number | null>(null);
  const [motivo, setMotivo] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    cargarEstadosIniciales();
  }, []);

  const cargarEstadosIniciales = async () => {
    try {
      // Obtener tipos de cultivo para buscar estados iniciales
      const tiposCultivo = await configuracionEstadosService.obtenerTiposCultivo();
      
      if (tiposCultivo && tiposCultivo.length > 0) {
        // Obtener estados del primer tipo de cultivo (o se podría mejorar para detectar el tipo del lote)
        const tipoCultivoId = tiposCultivo[0].id;
        const estados = await configuracionEstadosService.obtenerEstados(tipoCultivoId);
        
        // Filtrar solo estados iniciales
        const iniciales = estados.filter((e: any) => e.esEstadoInicial);
        setEstadosIniciales(iniciales);
        
        // Seleccionar el primero por defecto
        if (iniciales.length > 0) {
          setEstadoInicialSeleccionado(iniciales[0].id);
        }
      }
    } catch (error) {
      console.error('Error cargando estados iniciales:', error);
      // Si falla, usar un estado por defecto
      setEstadosIniciales([{
        id: 0,
        nombre: 'Disponible',
        color: '#10b981',
        icono: '🟢'
      }]);
      setEstadoInicialSeleccionado(0);
    }
  };

  const handleResetear = async () => {
    if (!estadoInicialSeleccionado) {
      setError('Debe seleccionar un estado inicial');
      return;
    }

    if (!motivo || motivo.trim().length < 20) {
      setError('El motivo es obligatorio y debe tener al menos 20 caracteres');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      await lotesService.resetear(lote.id!, estadoInicialSeleccionado, motivo.trim());
      alert('✅ Lote reseteado exitosamente');
      onSuccess();
      onClose();
    } catch (error: any) {
      console.error('Error al resetear lote:', error);
      setError(error?.response?.data?.message || error?.message || 'Error al resetear el lote');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0,0,0,0.5)',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      zIndex: 2000
    }}>
      <div style={{
        background: 'white',
        borderRadius: '12px',
        padding: '24px',
        maxWidth: '600px',
        width: '95%',
        maxHeight: '90vh',
        overflowY: 'auto'
      }}>
        {/* Header */}
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '20px',
          borderBottom: '2px solid #ef4444',
          paddingBottom: '10px'
        }}>
          <h2 style={{ margin: 0, color: '#ef4444', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Icon name="RefreshCw" size={24} />
            Resetear Lote a Estado Inicial
          </h2>
          <button
            onClick={onClose}
            style={{
              background: 'none',
              border: 'none',
              fontSize: '24px',
              cursor: 'pointer',
              color: '#666'
            }}
          >
            ✕
          </button>
        </div>

        {/* Información del lote */}
        <div style={{
          padding: '16px',
          backgroundColor: '#f9fafb',
          borderRadius: '8px',
          marginBottom: '20px'
        }}>
          <div style={{ marginBottom: '8px' }}>
            <strong>Lote:</strong> {lote.nombre}
          </div>
          <div>
            <strong>Estado actual:</strong>{' '}
            {lote.estadoConfigurado ? (
              <span style={{
                backgroundColor: lote.estadoConfigurado.color,
                color: 'white',
                padding: '2px 8px',
                borderRadius: '4px',
                fontSize: '12px'
              }}>
                {lote.estadoConfigurado.icono} {lote.estadoConfigurado.nombre}
              </span>
            ) : (
              <span>{lote.estado}</span>
            )}
          </div>
        </div>

        {/* Advertencia */}
        <div style={{
          padding: '16px',
          backgroundColor: '#fef3c7',
          border: '1px solid #fbbf24',
          borderRadius: '8px',
          marginBottom: '20px'
        }}>
          <div style={{ display: 'flex', alignItems: 'start', gap: '8px' }}>
            <Icon name="AlertTriangle" size={20} style={{ color: '#f59e0b', flexShrink: 0 }} />
            <div>
              <strong style={{ color: '#92400e' }}>⚠️ Advertencia</strong>
              <p style={{ margin: '8px 0 0 0', color: '#78350f', fontSize: '14px' }}>
                Esta acción reseteará el lote a su estado inicial. Las siguientes consecuencias aplicarán:
              </p>
              <ul style={{ margin: '8px 0 0 0', paddingLeft: '20px', color: '#78350f', fontSize: '14px' }}>
                <li>Se limpiará el cultivo actual</li>
                <li>Se eliminarán las fechas de siembra y cosecha</li>
                <li>Las labores existentes se marcarán como históricas</li>
                <li>El cambio quedará registrado en el historial</li>
              </ul>
            </div>
          </div>
        </div>

        {/* Estado inicial */}
        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '8px', fontWeight: '500' }}>
            Estado Inicial *
          </label>
          <select
            value={estadoInicialSeleccionado || ''}
            onChange={(e) => setEstadoInicialSeleccionado(parseInt(e.target.value))}
            style={{
              width: '100%',
              padding: '10px',
              border: '1px solid #d1d5db',
              borderRadius: '8px',
              fontSize: '14px'
            }}
            disabled={loading}
          >
            <option value="">Seleccione un estado inicial</option>
            {estadosIniciales.map(estado => (
              <option key={estado.id} value={estado.id}>
                {estado.icono} {estado.nombre}
              </option>
            ))}
          </select>
        </div>

        {/* Motivo */}
        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '8px', fontWeight: '500' }}>
            Motivo del Reset * <span style={{ fontSize: '12px', color: '#6b7280' }}>(mínimo 20 caracteres)</span>
          </label>
          <textarea
            value={motivo}
            onChange={(e) => setMotivo(e.target.value)}
            placeholder="Ej: Cultivo fallido por sequía extrema. Necesitamos reiniciar el ciclo productivo."
            rows={4}
            style={{
              width: '100%',
              padding: '10px',
              border: '1px solid #d1d5db',
              borderRadius: '8px',
              fontSize: '14px',
              fontFamily: 'inherit',
              resize: 'vertical'
            }}
            disabled={loading}
          />
          <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '4px' }}>
            {motivo.length}/20 caracteres mínimos
          </div>
        </div>

        {/* Error */}
        {error && (
          <div style={{
            padding: '12px',
            backgroundColor: '#fee2e2',
            border: '1px solid #fca5a5',
            borderRadius: '8px',
            marginBottom: '20px',
            color: '#991b1b'
          }}>
            {error}
          </div>
        )}

        {/* Botones */}
        <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
          <button
            onClick={onClose}
            disabled={loading}
            style={{
              padding: '12px 24px',
              background: '#6b7280',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              cursor: loading ? 'not-allowed' : 'pointer',
              fontSize: '14px',
              fontWeight: '500',
              opacity: loading ? 0.5 : 1
            }}
          >
            Cancelar
          </button>
          <button
            onClick={handleResetear}
            disabled={loading || !estadoInicialSeleccionado || motivo.trim().length < 20}
            style={{
              padding: '12px 24px',
              background: loading || !estadoInicialSeleccionado || motivo.trim().length < 20 
                ? '#9ca3af' 
                : '#ef4444',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              cursor: loading || !estadoInicialSeleccionado || motivo.trim().length < 20 
                ? 'not-allowed' 
                : 'pointer',
              fontSize: '14px',
              fontWeight: '500',
              display: 'flex',
              alignItems: 'center',
              gap: '8px'
            }}
          >
            {loading ? (
              <>
                <Icon name="RefreshCcw" size={16} style={{ animation: 'spin 1s linear infinite' }} />
                Reseteando...
              </>
            ) : (
              <>
                <Icon name="RefreshCw" size={16} />
                Confirmar Reset
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ResetLoteModal;

