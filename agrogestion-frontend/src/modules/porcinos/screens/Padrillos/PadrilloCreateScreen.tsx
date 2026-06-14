import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Icon } from '../../../../components/icons';
import { padrillosService } from '../../services/padrillosService';
import type { Padrillo } from '../../types';

const PadrilloCreateScreen: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const [formData, setFormData] = useState<Partial<Padrillo>>({
    identificacion: '',
    fechaNacimiento: new Date().toISOString().split('T')[0],
    origen: 'EXTERNA',
    fechaIngresoGranja: new Date().toISOString().split('T')[0],
    observaciones: '',
  });
  const [loading, setLoading] = useState(false);
  const [loadingData, setLoadingData] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const isEditing = !!id;

  useEffect(() => {
    if (id) {
      cargarPadrillo();
    }
  }, [id]);

  const cargarPadrillo = async () => {
    setLoadingData(true);
    try {
      const data = await padrillosService.obtener(parseInt(id!));
      setFormData({
        identificacion: data.identificacion || '',
        fechaNacimiento: data.fechaNacimiento ? new Date(data.fechaNacimiento).toISOString().split('T')[0] : '',
        origen: data.origen || 'EXTERNA',
        fechaIngresoGranja: data.fechaIngresoGranja ? new Date(data.fechaIngresoGranja).toISOString().split('T')[0] : '',
        fechaBaja: data.fechaBaja ? new Date(data.fechaBaja).toISOString().split('T')[0] : undefined,
        motivoBaja: data.motivoBaja || '',
        observaciones: data.observaciones || '',
      });
    } catch (error: any) {
      setError(error.response?.data?.message || 'Error al cargar el padrillo');
      console.error('Error:', error);
    } finally {
      setLoadingData(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!formData.identificacion || !formData.fechaNacimiento || !formData.fechaIngresoGranja) {
      setError('Debe completar todos los campos obligatorios');
      return;
    }

    setLoading(true);
    try {
      if (isEditing) {
        await padrillosService.actualizar(parseInt(id!), formData);
      } else {
        await padrillosService.crear(formData);
      }
      navigate('/porcinos/padrillos');
    } catch (error: any) {
      setError(error.response?.data?.message || `Error al ${isEditing ? 'actualizar' : 'crear'} el padrillo`);
      console.error('Error:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '2rem', maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ marginBottom: '2rem' }}>
        <button
          onClick={() => navigate('/porcinos/padrillos')}
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
          <Icon name={isEditing ? "Edit" : "Plus"} size={32} style={{ marginRight: '0.5rem' }} /> 
          {isEditing ? 'Editar Padrillo' : 'Nuevo Padrillo'}
        </h1>
      </div>

      {error && (
        <div style={{
          padding: '1rem',
          backgroundColor: '#fee2e2',
          border: '1px solid #fecaca',
          borderRadius: '0.375rem',
          marginBottom: '1.5rem',
          color: '#991b1b'
        }}>
          {error}
        </div>
      )}

      {loadingData ? (
        <div style={{ textAlign: 'center', padding: '2rem' }}>
          <p>Cargando datos...</p>
        </div>
      ) : (
        <form onSubmit={handleSubmit} style={{
          backgroundColor: 'white',
          padding: '2rem',
          borderRadius: '0.5rem',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)'
        }}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
                Identificación *
              </label>
              <input
                type="text"
                value={formData.identificacion || ''}
                onChange={(e) => setFormData({...formData, identificacion: e.target.value})}
                required
                style={{
                  width: '100%',
                  padding: '0.75rem',
                  border: '1px solid #d1d5db',
                  borderRadius: '0.375rem',
                  fontSize: '0.875rem'
                }}
                placeholder="Ej: PAD-001"
              />
            </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1rem' }}>
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
                Fecha de Nacimiento *
              </label>
              <input
                type="date"
                value={formData.fechaNacimiento || ''}
                onChange={(e) => setFormData({...formData, fechaNacimiento: e.target.value})}
                required
                style={{
                  width: '100%',
                  padding: '0.75rem',
                  border: '1px solid #d1d5db',
                  borderRadius: '0.375rem',
                  fontSize: '0.875rem'
                }}
              />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
                Fecha Ingreso Granja *
              </label>
              <input
                type="date"
                value={formData.fechaIngresoGranja || ''}
                onChange={(e) => setFormData({...formData, fechaIngresoGranja: e.target.value})}
                required
                style={{
                  width: '100%',
                  padding: '0.75rem',
                  border: '1px solid #d1d5db',
                  borderRadius: '0.375rem',
                  fontSize: '0.875rem'
                }}
              />
            </div>
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
              Origen *
            </label>
            <select
              value={formData.origen || 'EXTERNA'}
              onChange={(e) => setFormData({...formData, origen: e.target.value as 'EXTERNA' | 'INTERNA'})}
              required
              style={{
                width: '100%',
                padding: '0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem'
              }}
            >
              <option value="EXTERNA">Externa</option>
              <option value="INTERNA">Interna</option>
            </select>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1rem' }}>
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
                Fecha de Baja
              </label>
              <input
                type="date"
                value={formData.fechaBaja || ''}
                onChange={(e) => setFormData({...formData, fechaBaja: e.target.value || undefined})}
                style={{
                  width: '100%',
                  padding: '0.75rem',
                  border: '1px solid #d1d5db',
                  borderRadius: '0.375rem',
                  fontSize: '0.875rem'
                }}
              />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
                Motivo de Baja
              </label>
              <input
                type="text"
                value={formData.motivoBaja || ''}
                onChange={(e) => setFormData({...formData, motivoBaja: e.target.value || undefined})}
                style={{
                  width: '100%',
                  padding: '0.75rem',
                  border: '1px solid #d1d5db',
                  borderRadius: '0.375rem',
                  fontSize: '0.875rem'
                }}
                placeholder="Motivo de baja..."
              />
            </div>
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
              Observaciones
            </label>
              <textarea
                value={formData.observaciones || ''}
                onChange={(e) => setFormData({...formData, observaciones: e.target.value})}
                rows={4}
                style={{
                  width: '100%',
                  padding: '0.75rem',
                  border: '1px solid #d1d5db',
                  borderRadius: '0.375rem',
                  fontSize: '0.875rem',
                  fontFamily: 'inherit'
                }}
                placeholder="Observaciones adicionales..."
              />
            </div>

          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
            <button
              type="button"
              onClick={() => navigate('/porcinos/padrillos')}
              style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: '#6b7280',
                color: 'white',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: 'pointer',
                fontWeight: '500'
              }}
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={loading}
              style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: loading ? '#9ca3af' : '#3b82f6',
                color: 'white',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: loading ? 'not-allowed' : 'pointer',
                fontWeight: '500'
              }}
            >
              {loading ? 'Guardando...' : isEditing ? 'Actualizar Padrillo' : 'Guardar Padrillo'}
            </button>
          </div>
        </div>
      </form>
      )}
    </div>
  );
};

export default PadrilloCreateScreen;







