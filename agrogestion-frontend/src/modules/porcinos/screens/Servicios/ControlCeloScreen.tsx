import React, { useState, useEffect } from 'react';
import { Icon, SemanticIcon } from '../../../../components/icons';
import { useParams, useNavigate } from 'react-router-dom';
import { serviciosService } from '../../services/serviciosService';
import { madresService } from '../../services/madresService';
import type { Servicio, Madre } from '../../types';

type MetodoControl = 'PALPACION' | 'ECO' | 'OBSERVACION';

const ControlCeloScreen: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [servicio, setServicio] = useState<Servicio | null>(null);
  const [madre, setMadre] = useState<Madre | null>(null);
  const [preñada, setPreñada] = useState<boolean | null>(null);
  const [metodo, setMetodo] = useState<MetodoControl>('PALPACION');
  const [fechaControl, setFechaControl] = useState('');
  const [observaciones, setObservaciones] = useState('');
  const [loading, setLoading] = useState(false);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  /** Tras registrar "No preñada", mostrar opción de nuevo intento con la misma madre */
  const [mostrarNuevoIntento, setMostrarNuevoIntento] = useState(false);
  const [madreIdParaNuevoIntento, setMadreIdParaNuevoIntento] = useState<number | null>(null);

  useEffect(() => {
    if (id) {
      cargarDatos();
    }
  }, [id]);

  const cargarDatos = async () => {
    setCargando(true);
    setError(null);
    try {
      // Obtener todos los servicios y buscar el que necesitamos
      // (ya que no existe endpoint GET /{id} en el backend)
      const servicios = await serviciosService.listar();
      const servicioEncontrado = servicios.find((s: Servicio) => s.id === parseInt(id!));
      
      if (!servicioEncontrado) {
        setError('Servicio no encontrado');
        setCargando(false);
        return;
      }

      setServicio(servicioEncontrado);
      
      // Calcular fecha de control (fecha servicio + 21 días por defecto)
      const fechaServicio = new Date(servicioEncontrado.fechaServicio);
      const fechaControlCalculada = new Date(fechaServicio);
      fechaControlCalculada.setDate(fechaControlCalculada.getDate() + 21);
      setFechaControl(fechaControlCalculada.toISOString().split('T')[0]);

      // Cargar información de la madre
      if (servicioEncontrado.madreId) {
        try {
          const madreData = await madresService.obtener(servicioEncontrado.madreId);
          setMadre(madreData);
        } catch (error) {
          console.error('Error cargando madre:', error);
          // No es crítico, seguimos sin la info de la madre
        }
      }
    } catch (error: any) {
      console.error('Error:', error);
      setError(error.response?.data?.message || 'Error al cargar los datos del servicio');
    } finally {
      setCargando(false);
    }
  };

  const calcularDiasDesdeServicio = () => {
    if (!servicio) return 0;
    const fechaServicio = new Date(servicio.fechaServicio);
    const hoy = new Date();
    const diff = Math.floor((hoy.getTime() - fechaServicio.getTime()) / (1000 * 60 * 60 * 24));
    return diff;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    
    if (preñada === null || !id) {
      setError('Debe seleccionar un resultado');
      return;
    }

    if (!fechaControl) {
      setError('Debe ingresar la fecha del control');
      return;
    }

    setLoading(true);
    try {
      // El backend crea automáticamente la gestación si preñada es true
      // No es necesario crear la gestación manualmente desde el frontend
      await serviciosService.controlCelo(parseInt(id), { 
        servicioId: parseInt(id), 
        preñada, 
        observaciones 
      });
      if (preñada) {
        navigate('/porcinos/servicios');
      } else {
        setMostrarNuevoIntento(true);
        setMadreIdParaNuevoIntento(servicio?.madreId ?? null);
      }
    } catch (error: any) {
      console.error('Error:', error);
      setError(error.response?.data?.message || 'Error al registrar el control de celo');
    } finally {
      setLoading(false);
    }
  };

  if (cargando) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <SemanticIcon semanticName="pending" size={32} />
        <p>Cargando información del servicio...</p>
      </div>
    );
  }

  if (!servicio) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <SemanticIcon semanticName="error" size={32} />
        <p>Servicio no encontrado</p>
        <button onClick={() => navigate('/porcinos/servicios')} style={{ marginTop: '1rem', padding: '0.5rem 1rem', backgroundColor: '#6b7280', color: 'white', border: 'none', borderRadius: '0.375rem', cursor: 'pointer' }}>
          Volver
        </button>
      </div>
    );
  }

  // Tras registrar "No preñada", ofrecer nuevo intento con la misma madre
  if (mostrarNuevoIntento) {
    return (
      <div style={{ maxWidth: '32rem', margin: '2rem auto', padding: '1.5rem' }}>
        <div style={{
          background: '#f0fdf4',
          border: '1px solid #86efac',
          borderRadius: '0.5rem',
          padding: '1.5rem',
          textAlign: 'center'
        }}>
          <p style={{ color: '#166534', marginBottom: '0.75rem', fontWeight: '500' }}>
            Control registrado. La madre no quedó preñada.
          </p>
          <p style={{ color: '#15803d', fontSize: '0.875rem', marginBottom: '1.25rem' }}>
            ¿Desea cargar un nuevo servicio (próximo intento) para esta madre? El Intento # se calculará automáticamente.
          </p>
          <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'center', flexWrap: 'wrap' }}>
            {madreIdParaNuevoIntento != null && (
              <button
                type="button"
                onClick={() => navigate(`/porcinos/servicios/nuevo?madreId=${madreIdParaNuevoIntento}`)}
                style={{
                  padding: '0.75rem 1.25rem',
                  backgroundColor: '#3b82f6',
                  color: 'white',
                  border: 'none',
                  borderRadius: '0.375rem',
                  cursor: 'pointer',
                  fontWeight: '500',
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '0.5rem'
                }}
              >
                <Icon name="Plus" size={18} />
                Nuevo intento
              </button>
            )}
            <button
              type="button"
              onClick={() => navigate('/porcinos/servicios')}
              style={{
                padding: '0.75rem 1.25rem',
                backgroundColor: '#6b7280',
                color: 'white',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: 'pointer',
                fontWeight: '500'
              }}
            >
              Volver a lista de Servicios
            </button>
          </div>
        </div>
      </div>
    );
  }

  const diasDesdeServicio = calcularDiasDesdeServicio();

  return (
    <div style={{ padding: '2rem', maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ marginBottom: '2rem' }}>
        <button
          onClick={() => navigate('/porcinos/servicios')}
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
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Icon name="Calendar" size={32} />
          Control de Celo
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

      {/* Información del Servicio */}
      <div style={{
        backgroundColor: '#eff6ff',
        border: '1px solid #bfdbfe',
        borderRadius: '0.5rem',
        padding: '1.5rem',
        marginBottom: '1.5rem'
      }}>
        <h2 style={{ fontSize: '1rem', fontWeight: '600', marginBottom: '1rem', color: '#1e40af' }}>
          <Icon name="Info" size={18} style={{ marginRight: '0.5rem' }} />
          Información del Servicio
        </h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', fontSize: '0.875rem' }}>
          <div>
            <span style={{ color: '#6b7280', fontWeight: '500' }}>Madre:</span>
            <span style={{ marginLeft: '0.5rem', fontWeight: '600', color: '#1f2937' }}>
              {madre?.identificacion || `ID: ${servicio.madreId}`}
            </span>
          </div>
          <div>
            <span style={{ color: '#6b7280', fontWeight: '500' }}>Tipo:</span>
            <span style={{ marginLeft: '0.5rem', fontWeight: '600', color: '#1f2937' }}>
              {servicio.tipo === 'MONTA_NATURAL' ? 'Monta Natural' : 'Inseminación Artificial'}
            </span>
          </div>
          <div>
            <span style={{ color: '#6b7280', fontWeight: '500' }}>Fecha Servicio:</span>
            <span style={{ marginLeft: '0.5rem', fontWeight: '600', color: '#1f2937' }}>
              {new Date(servicio.fechaServicio).toLocaleDateString('es-ES')}
            </span>
          </div>
          <div>
            <span style={{ color: '#6b7280', fontWeight: '500' }}>Intento #:</span>
            <span style={{ marginLeft: '0.5rem', fontWeight: '600', color: '#1f2937' }}>
              {servicio.numeroIntento}
            </span>
          </div>
          {servicio.machoNombre && (
            <div>
              <span style={{ color: '#6b7280', fontWeight: '500' }}>Padrillo:</span>
              <span style={{ marginLeft: '0.5rem', fontWeight: '600', color: '#1f2937' }}>
                {servicio.machoNombre}
              </span>
            </div>
          )}
          <div>
            <span style={{ color: '#6b7280', fontWeight: '500' }}>Días desde servicio:</span>
            <span style={{ marginLeft: '0.5rem', fontWeight: '600', color: diasDesdeServicio >= 21 ? '#10b981' : '#f59e0b' }}>
              {diasDesdeServicio} días
            </span>
          </div>
        </div>
      </div>

      <form onSubmit={handleSubmit} style={{
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)'
      }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
              Fecha del Control *
            </label>
            <input
              type="date"
              value={fechaControl}
              onChange={(e) => setFechaControl(e.target.value)}
              required
              style={{
                width: '100%',
                padding: '0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem'
              }}
            />
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginTop: '0.25rem' }}>
              Fecha esperada: {servicio.fechaControlCelo ? new Date(servicio.fechaControlCelo).toLocaleDateString('es-ES') : 'No disponible'}
            </p>
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
              Método de Control *
            </label>
            <select
              value={metodo}
              onChange={(e) => setMetodo(e.target.value as MetodoControl)}
              required
              style={{
                width: '100%',
                padding: '0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem'
              }}
            >
              <option value="PALPACION">Palpación</option>
              <option value="ECO">Ecografía</option>
              <option value="OBSERVACION">Observación</option>
            </select>
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
              Resultado del Control *
            </label>
            <div style={{ display: 'flex', gap: '1rem' }}>
              <button
                type="button"
                onClick={() => setPreñada(true)}
                style={{
                  flex: 1,
                  padding: '1rem',
                  backgroundColor: preñada === true ? '#10b981' : '#f3f4f6',
                  color: preñada === true ? 'white' : '#374151',
                  border: preñada === true ? '2px solid #10b981' : '2px solid #e5e7eb',
                  borderRadius: '0.375rem',
                  cursor: 'pointer',
                  fontSize: '0.875rem',
                  fontWeight: '500',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '0.5rem',
                  transition: 'all 0.2s'
                }}
              >
                <Icon name="CheckCircle" size={20} />
                Preñada
              </button>
              <button
                type="button"
                onClick={() => setPreñada(false)}
                style={{
                  flex: 1,
                  padding: '1rem',
                  backgroundColor: preñada === false ? '#ef4444' : '#f3f4f6',
                  color: preñada === false ? 'white' : '#374151',
                  border: preñada === false ? '2px solid #ef4444' : '2px solid #e5e7eb',
                  borderRadius: '0.375rem',
                  cursor: 'pointer',
                  fontSize: '0.875rem',
                  fontWeight: '500',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '0.5rem',
                  transition: 'all 0.2s'
                }}
              >
                <Icon name="XCircle" size={20} />
                No Preñada
              </button>
            </div>
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
              Observaciones
            </label>
            <textarea
              value={observaciones}
              onChange={(e) => setObservaciones(e.target.value)}
              rows={4}
              style={{
                width: '100%',
                padding: '0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem',
                fontFamily: 'inherit'
              }}
              placeholder="Observaciones adicionales sobre el control de celo..."
            />
          </div>

          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
            <button
              type="button"
              onClick={() => navigate('/porcinos/servicios')}
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
              disabled={preñada === null || loading || !fechaControl}
              style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: loading || preñada === null || !fechaControl ? '#9ca3af' : '#3b82f6',
                color: 'white',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: loading || preñada === null || !fechaControl ? 'not-allowed' : 'pointer',
                fontWeight: '500'
              }}
            >
              {loading ? 'Guardando...' : 'Guardar Control'}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
};

export default ControlCeloScreen;

