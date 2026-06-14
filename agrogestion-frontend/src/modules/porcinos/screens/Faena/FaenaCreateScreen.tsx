import React, { useState, useEffect } from 'react';
import { Icon } from '../../../../components/icons';
import { useParams, useNavigate } from 'react-router-dom';
import { ventaPorcinoService } from '../../services/ventaPorcinoService';
import { recriaService } from '../../services/recriaService';
import type { VentaPorcino, Recria } from '../../types';

const FaenaCreateScreen: React.FC = () => {
  const { recriaId } = useParams<{ recriaId: string }>();
  const navigate = useNavigate();
  const [recria, setRecria] = useState<Recria | null>(null);
  const [formData, setFormData] = useState<Partial<VentaPorcino>>({
    tipo: 'FAENA',
    recriaId: recriaId ? parseInt(recriaId) : undefined,
    fecha: new Date().toISOString().split('T')[0],
    fechaEnvio: new Date().toISOString().split('T')[0],
    pesoEnvio: 0,
    cantidad: 0,
    pesoPromedio: 0,
    observaciones: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (recriaId) {
      cargarRecria();
    }
  }, [recriaId]);

  const cargarRecria = async () => {
    try {
      const data = await recriaService.obtener(parseInt(recriaId!));
      setRecria(data);
      const pesoTotal = data.pesoPromedio * data.cantidadAnimales;
      setFormData(prev => ({
        ...prev,
        recriaId: parseInt(recriaId!),
        cantidad: data.cantidadAnimales,
        pesoPromedio: data.pesoPromedio,
        pesoEnvio: pesoTotal,
      }));
    } catch (error) {
      console.error('Error al cargar recría:', error);
    }
  };

  const calcularRendimiento = () => {
    if (formData.pesoEnvio && formData.pesoFaena) {
      return (formData.pesoFaena / formData.pesoEnvio) * 100;
    }
    return null;
  };

  const calcularIngresoTotal = () => {
    if (formData.pesoEnvio && formData.precioKg) {
      return formData.pesoEnvio * formData.precioKg;
    }
    return null;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!formData.fechaEnvio || !formData.pesoEnvio || !formData.cantidad) {
      setError('Debe completar todos los campos obligatorios');
      return;
    }

    const rendimiento = calcularRendimiento();
    const ingresoTotal = calcularIngresoTotal();

    setLoading(true);
    try {
      await ventaPorcinoService.crear({
        tipo: 'FAENA',
        recriaId: parseInt(recriaId!),
        fecha: formData.fechaEnvio || formData.fecha || new Date().toISOString().split('T')[0],
        cantidad: formData.cantidad,
        pesoPromedio: formData.pesoPromedio || (formData.pesoEnvio! / formData.cantidad!),
        precioKg: formData.precioKg,
        ingresoTotal: ingresoTotal || 0,
        fechaEnvio: formData.fechaEnvio,
        fechaFaena: formData.fechaFaena,
        pesoEnvio: formData.pesoEnvio,
        pesoFaena: formData.pesoFaena,
        rendimiento: rendimiento || undefined,
        observaciones: formData.observaciones,
      });
      navigate('/porcinos/faena');
    } catch (error: any) {
      setError(error.response?.data?.mensaje || error.response?.data?.message || 'Error al registrar la faena');
      console.error('Error:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '2rem', maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ marginBottom: '2rem' }}>
        <button
          onClick={() => navigate('/porcinos/faena')}
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
          🔪 Registrar Faena
        </h1>
        {recria && (
          <p style={{ fontSize: '0.875rem', color: '#6b7280', marginTop: '0.5rem' }}>
            Recría: {recria.loteNombre || `Lote ${recria.loteId}`} - {recria.cantidadAnimales} animales
          </p>
        )}
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

      <form onSubmit={handleSubmit} style={{
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)'
      }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1rem' }}>
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
                Fecha de Envío *
              </label>
              <input
                type="date"
                value={formData.fechaEnvio || ''}
                onChange={(e) => setFormData({...formData, fechaEnvio: e.target.value})}
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
                Fecha de Faena
              </label>
              <input
                type="date"
                value={formData.fechaFaena || ''}
                onChange={(e) => setFormData({...formData, fechaFaena: e.target.value})}
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

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1rem' }}>
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
                Peso de Envío (kg) *
              </label>
              <input
                type="number"
                step="0.01"
                min="0"
                value={formData.pesoEnvio || ''}
                onChange={(e) => setFormData({...formData, pesoEnvio: parseFloat(e.target.value) || 0})}
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
                Peso de Faena (kg)
              </label>
              <input
                type="number"
                step="0.01"
                min="0"
                value={formData.pesoFaena || ''}
                onChange={(e) => setFormData({...formData, pesoFaena: parseFloat(e.target.value) || undefined})}
                style={{
                  width: '100%',
                  padding: '0.75rem',
                  border: '1px solid #d1d5db',
                  borderRadius: '0.375rem',
                  fontSize: '0.875rem'
                }}
              />
              {formData.pesoFaena && formData.pesoEnvio && (
                <p style={{ fontSize: '0.75rem', color: '#6b7280', marginTop: '0.25rem' }}>
                  Rendimiento: {calcularRendimiento()?.toFixed(2)}%
                </p>
              )}
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1rem' }}>
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
                Precio por kg ($)
              </label>
              <input
                type="number"
                step="0.01"
                min="0"
                value={formData.precioKg || ''}
                onChange={(e) => setFormData({...formData, precioKg: parseFloat(e.target.value) || undefined})}
                style={{
                  width: '100%',
                  padding: '0.75rem',
                  border: '1px solid #d1d5db',
                  borderRadius: '0.375rem',
                  fontSize: '0.875rem'
                }}
              />
              {formData.precioKg && formData.pesoEnvio && (
                <p style={{ fontSize: '0.75rem', color: '#10b981', marginTop: '0.25rem', fontWeight: '500' }}>
                  Ingreso Total: ${calcularIngresoTotal()?.toLocaleString('es-AR')}
                </p>
              )}
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
                Cantidad de Animales *
              </label>
              <input
                type="number"
                min="1"
                value={formData.cantidad || ''}
                onChange={(e) => setFormData({...formData, cantidad: parseInt(e.target.value) || 0})}
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
              onClick={() => navigate('/porcinos/faena')}
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
              {loading ? 'Guardando...' : 'Registrar Faena'}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
};

export default FaenaCreateScreen;







