import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { madresService } from '../../services/madresService';
import type { Madre } from '../../types';
import { Icon, SemanticIcon } from '../../../../components/icons';

const MadreDetailScreen: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [madre, setMadre] = useState<Madre | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) {
      cargarMadre();
    }
  }, [id]);

  const cargarMadre = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const data = await madresService.obtener(parseInt(id));
      setMadre(data);
    } catch (error) {
      console.error('Error cargando madre:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div style={{ padding: '2rem', textAlign: 'center' }}><SemanticIcon semanticName="pending" size={32} /> Cargando...</div>;
  }

  if (!madre) {
    return <div style={{ padding: '2rem', textAlign: 'center' }}><SemanticIcon semanticName="error" size={32} /> Madre no encontrada</div>;
  }

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937' }}>
          <Icon name="PiggyBank" size={32} style={{ marginRight: '0.5rem' }} /> {madre.identificacion}
        </h1>
        <button onClick={() => navigate('/porcinos/madres')} style={{
          padding: '0.5rem 1rem',
          backgroundColor: '#6b7280',
          color: 'white',
          border: 'none',
          borderRadius: '0.375rem',
          cursor: 'pointer'
        }}>
          ← Volver
        </button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
        <div style={{ backgroundColor: 'white', padding: '1.5rem', borderRadius: '0.5rem', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
          <h3 style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.5rem' }}>Estado Actual</h3>
          <p style={{ fontSize: '1.25rem', fontWeight: 'bold' }}>{madre.estadoActual}</p>
        </div>
        <div style={{ backgroundColor: 'white', padding: '1.5rem', borderRadius: '0.5rem', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
          <h3 style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.5rem' }}>Fecha Nacimiento</h3>
          <p style={{ fontSize: '1.25rem', fontWeight: 'bold' }}>{new Date(madre.fechaNacimiento).toLocaleDateString('es-ES')}</p>
        </div>
        <div style={{ backgroundColor: 'white', padding: '1.5rem', borderRadius: '0.5rem', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
          <h3 style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.5rem' }}>Cantidad Tetas</h3>
          <p style={{ fontSize: '1.25rem', fontWeight: 'bold' }}>{madre.cantidadTetas}</p>
        </div>
      </div>

      <div style={{ display: 'flex', gap: '1rem', marginBottom: '2rem' }}>
        <button onClick={() => navigate(`/porcinos/servicios/nuevo?madreId=${id}`)} style={{
          padding: '0.75rem 1.5rem',
          backgroundColor: '#8b5cf6',
          color: 'white',
          border: 'none',
          borderRadius: '0.375rem',
          cursor: 'pointer'
        }}>
          <Icon name="Plus" size={18} style={{ marginRight: '0.5rem' }} /> Registrar Servicio
        </button>
        <button onClick={() => navigate(`/porcinos/partos/nuevo?madreId=${id}`)} style={{
          padding: '0.75rem 1.5rem',
          backgroundColor: '#ec4899',
          color: 'white',
          border: 'none',
          borderRadius: '0.375rem',
          cursor: 'pointer'
        }}>
          <Icon name="Plus" size={18} style={{ marginRight: '0.5rem' }} /> Registrar Parto
        </button>
        <button onClick={() => navigate(`/porcinos/madres/${id}/historial`)} style={{
          padding: '0.75rem 1.5rem',
          backgroundColor: '#3b82f6',
          color: 'white',
          border: 'none',
          borderRadius: '0.375rem',
          cursor: 'pointer'
        }}>
          📜 Ver Historial
        </button>
      </div>
    </div>
  );
};

export default MadreDetailScreen;

