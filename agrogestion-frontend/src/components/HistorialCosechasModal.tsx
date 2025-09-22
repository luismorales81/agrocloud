import React, { useState, useEffect } from 'react';

interface HistorialCosecha {
  id: number;
  lote: {
    id: number;
    nombre: string;
  };
  cultivo: {
    id: number;
    nombre: string;
    variedad: string;
  };
  fechaSiembra: string;
  fechaCosecha: string;
  superficieHectareas: number;
  cantidadCosechada: number;
  unidadCosecha: string;
  rendimientoReal: number;
  rendimientoEsperado: number;
  cantidadEsperada: number;
  variedadSemilla: string;
  observaciones: string;
  estadoSuelo: string;
  requiereDescanso: boolean;
  diasDescansoRecomendados: number;
  fechaCreacion: string;
}

interface HistorialCosechasModalProps {
  loteId: number;
  loteNombre: string;
  isOpen: boolean;
  onClose: () => void;
}

const HistorialCosechasModal: React.FC<HistorialCosechasModalProps> = ({
  loteId,
  loteNombre,
  isOpen,
  onClose
}) => {
  const [historial, setHistorial] = useState<HistorialCosecha[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Cargar historial cuando se abre el modal
  useEffect(() => {
    if (isOpen && loteId > 0) {
      cargarHistorial();
    }
  }, [isOpen, loteId]);

  const cargarHistorial = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        setError('No hay token de autenticación');
        return;
      }

      const response = await fetch(`http://localhost:8080/api/historial-cosechas/lote/${loteId}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        const data = await response.json();
        setHistorial(data);
      } else {
        setError('Error al cargar el historial');
      }
    } catch (error) {
      console.error('Error cargando historial:', error);
      setError('Error de conexión');
    } finally {
      setLoading(false);
    }
  };

  const getEstadoSueloColor = (estado: string): string => {
    switch (estado) {
      case 'BUENO': return '#10b981';
      case 'DESCANSANDO': return '#f59e0b';
      case 'AGOTADO': return '#ef4444';
      default: return '#6b7280';
    }
  };

  const getEstadoSueloIcon = (estado: string): string => {
    switch (estado) {
      case 'BUENO': return '✅';
      case 'DESCANSANDO': return '⏳';
      case 'AGOTADO': return '⚠️';
      default: return '❓';
    }
  };

  const calcularDiasCiclo = (fechaSiembra: string, fechaCosecha: string): number => {
    const inicio = new Date(fechaSiembra);
    const fin = new Date(fechaCosecha);
    const diffTime = Math.abs(fin.getTime() - inicio.getTime());
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  };

  if (!isOpen) return null;

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0, 0, 0, 0.5)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 1000
    }}>
      <div style={{
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '0.5rem',
        width: '90%',
        maxWidth: '1000px',
        maxHeight: '90vh',
        overflowY: 'auto'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
          <h2 style={{ margin: 0, color: '#1f2937' }}>
            📊 Historial de Cosechas - {loteNombre}
          </h2>
          <button
            onClick={onClose}
            style={{
              backgroundColor: '#6b7280',
              color: 'white',
              border: 'none',
              padding: '0.5rem 1rem',
              borderRadius: '0.25rem',
              cursor: 'pointer',
              fontSize: '1rem'
            }}
          >
            ✕ Cerrar
          </button>
        </div>

        {loading && (
          <div style={{ textAlign: 'center', padding: '2rem' }}>
            <div style={{ fontSize: '1.5rem', marginBottom: '1rem' }}>⏳</div>
            <p>Cargando historial...</p>
          </div>
        )}

        {error && (
          <div style={{ 
            textAlign: 'center', 
            padding: '2rem',
            backgroundColor: '#fef2f2',
            border: '1px solid #fecaca',
            borderRadius: '0.5rem',
            color: '#dc2626'
          }}>
            <div style={{ fontSize: '1.5rem', marginBottom: '1rem' }}>❌</div>
            <p>{error}</p>
            <button
              onClick={cargarHistorial}
              style={{
                backgroundColor: '#dc2626',
                color: 'white',
                border: 'none',
                padding: '0.5rem 1rem',
                borderRadius: '0.25rem',
                cursor: 'pointer',
                marginTop: '1rem'
              }}
            >
              🔄 Reintentar
            </button>
          </div>
        )}

        {!loading && !error && historial.length === 0 && (
          <div style={{ textAlign: 'center', padding: '3rem', color: '#6b7280' }}>
            <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>📋</div>
            <h3>No hay historial de cosechas</h3>
            <p>Este lote aún no tiene cosechas registradas.</p>
          </div>
        )}

        {!loading && !error && historial.length > 0 && (
          <div style={{ display: 'grid', gap: '1rem' }}>
            {historial.map((cosecha, index) => (
              <div
                key={cosecha.id}
                style={{
                  backgroundColor: '#f8fafc',
                  padding: '1.5rem',
                  borderRadius: '0.5rem',
                  border: '1px solid #e2e8f0',
                  borderLeft: `4px solid ${getEstadoSueloColor(cosecha.estadoSuelo)}`
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', marginBottom: '1rem' }}>
                  <div>
                    <h3 style={{ margin: 0, color: '#1f2937', fontSize: '1.25rem' }}>
                      {cosecha.cultivo.nombre} - {cosecha.cultivo.variedad}
                    </h3>
                    <p style={{ margin: '0.5rem 0 0 0', color: '#6b7280' }}>
                      Variedad: {cosecha.variedadSemilla} | Superficie: {cosecha.superficieHectareas} ha
                    </p>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ 
                      fontSize: '1.5rem', 
                      fontWeight: 'bold',
                      color: cosecha.rendimientoReal >= cosecha.rendimientoEsperado ? '#10b981' : '#f59e0b'
                    }}>
                      {cosecha.rendimientoEsperado > 0 ? 
                        ((cosecha.rendimientoReal / cosecha.rendimientoEsperado) * 100).toFixed(1) : '0'}%
                    </div>
                    <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                      Cumplimiento
                    </div>
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginBottom: '1rem' }}>
                  <div>
                    <strong>Fecha de Siembra:</strong> {new Date(cosecha.fechaSiembra).toLocaleDateString()}
                  </div>
                  <div>
                    <strong>Fecha de Cosecha:</strong> {new Date(cosecha.fechaCosecha).toLocaleDateString()}
                  </div>
                  <div>
                    <strong>Duración del Ciclo:</strong> {calcularDiasCiclo(cosecha.fechaSiembra, cosecha.fechaCosecha)} días
                  </div>
                  <div>
                    <strong>Cantidad Cosechada:</strong> {cosecha.cantidadCosechada.toLocaleString()} {cosecha.unidadCosecha}
                  </div>
                  <div>
                    <strong>Rendimiento Real:</strong> {cosecha.rendimientoReal.toLocaleString()} kg/ha
                  </div>
                  <div>
                    <strong>Rendimiento Esperado:</strong> {cosecha.rendimientoEsperado.toLocaleString()} kg/ha
                  </div>
                  <div>
                    <strong>Cantidad Esperada:</strong> {cosecha.cantidadEsperada?.toLocaleString()} {cosecha.unidadCosecha}
                  </div>
                  <div>
                    <strong>Estado del Suelo:</strong> 
                    <span style={{ 
                      color: getEstadoSueloColor(cosecha.estadoSuelo),
                      fontWeight: 'bold',
                      marginLeft: '0.5rem'
                    }}>
                      {getEstadoSueloIcon(cosecha.estadoSuelo)} {cosecha.estadoSuelo}
                    </span>
                  </div>
                </div>

                {cosecha.requiereDescanso && (
                  <div style={{ 
                    marginTop: '1rem', 
                    padding: '1rem', 
                    backgroundColor: '#fef3c7', 
                    borderRadius: '0.5rem',
                    border: '1px solid #f59e0b'
                  }}>
                    <strong>⏳ Período de Descanso Recomendado:</strong> {cosecha.diasDescansoRecomendados} días
                  </div>
                )}

                {cosecha.observaciones && (
                  <div style={{ marginTop: '1rem', padding: '1rem', backgroundColor: '#f9fafb', borderRadius: '0.5rem' }}>
                    <strong>Observaciones:</strong> {cosecha.observaciones}
                  </div>
                )}

                <div style={{ 
                  marginTop: '1rem', 
                  padding: '0.5rem', 
                  fontSize: '0.875rem', 
                  color: '#6b7280',
                  borderTop: '1px solid #e5e7eb'
                }}>
                  Registrado el {new Date(cosecha.fechaCreacion).toLocaleString()}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default HistorialCosechasModal;
