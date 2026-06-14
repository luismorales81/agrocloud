import React, { useState, useEffect } from 'react';
import { Icon, SemanticIcon } from '../../../../components/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { desteteService } from '../../services/desteteService';
import { partosService } from '../../services/partosService';
import type { Destete, Parto } from '../../types';

const DesteteDetailScreen: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const [destete, setDestete] = useState<Destete | null>(null);
  const [parto, setParto] = useState<Parto | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) {
      cargarDatos();
    }
  }, [id]);

  const cargarDatos = async () => {
    setLoading(true);
    try {
      const desteteData = await desteteService.obtener(parseInt(id!));
      setDestete(desteteData);
      
      // Cargar información del parto
      if (desteteData.partoId) {
        try {
          const partoData = await partosService.obtener(desteteData.partoId);
          setParto(partoData);
        } catch (error) {
          console.error('Error al cargar parto:', error);
        }
      }
    } catch (error) {
      console.error('Error:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <SemanticIcon semanticName="pending" size={32} />
        <p>Cargando destete...</p>
      </div>
    );
  }

  if (!destete) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <p>Destete no encontrado</p>
        <button onClick={() => navigate('/porcinos/destetes')}>Volver</button>
      </div>
    );
  }

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ marginBottom: '2rem' }}>
        <button
          onClick={() => navigate('/porcinos/destetes')}
          style={{
            padding: '0.5rem 1rem',
            backgroundColor: '#6b7280',
            color: 'white',
            border: 'none',
            borderRadius: '0.375rem',
            cursor: 'pointer',
            marginBottom: '1rem',
            fontSize: '0.875rem'
          }}
        >
          ← Volver
        </button>
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937' }}>
          <Icon name="Baby" size={32} style={{ marginRight: '0.5rem' }} /> Detalle de Destete
        </h1>
      </div>

      {/* Información Principal */}
      <div style={{
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        marginBottom: '1.5rem'
      }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Fecha Destete</p>
            <p style={{ fontSize: '1rem', fontWeight: '500' }}>
              {destete.fechaDestete ? new Date(destete.fechaDestete).toLocaleDateString('es-ES') : '-'}
            </p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Cantidad Destetados</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#10b981' }}>
              {destete.cantidadDestetados}
            </p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Peso Promedio (kg)</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#3b82f6' }}>
              {destete.pesoPromedioDestete?.toFixed(2) || '0.00'}
            </p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Días de Lactancia</p>
            <p style={{ fontSize: '1rem', fontWeight: '500' }}>
              {destete.diasLactancia || '-'}
            </p>
          </div>
        </div>

        {parto && (
          <div style={{
            padding: '1rem',
            backgroundColor: '#f9fafb',
            borderRadius: '0.375rem',
            marginTop: '1.5rem'
          }}>
            <h3 style={{ fontSize: '0.875rem', fontWeight: '600', marginBottom: '0.75rem', color: '#1f2937' }}>
              Información del Parto
            </h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
              <div>
                <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Fecha Parto</p>
                <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>
                  {new Date(parto.fechaInicio).toLocaleDateString('es-ES')}
                </p>
              </div>
              <div>
                <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Madre</p>
                <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>
                  {parto.madreIdentificacion || `ID: ${parto.madreId}`}
                </p>
              </div>
              <div>
                <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Nacidos Vivos</p>
                <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>
                  {parto.nacidosVivos}
                </p>
              </div>
            </div>
          </div>
        )}

        {destete.observaciones && (
          <div style={{ marginTop: '1.5rem' }}>
            <h3 style={{ fontSize: '0.875rem', fontWeight: '600', marginBottom: '0.5rem', color: '#1f2937' }}>
              Observaciones
            </h3>
            <p style={{ fontSize: '0.875rem', color: '#4b5563', whiteSpace: 'pre-wrap' }}>
              {destete.observaciones}
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default DesteteDetailScreen;

