import React, { useState, useEffect, useMemo } from 'react';
import { Icon } from '../../../../components/icons';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { serviciosService } from '../../services/serviciosService';
import { madresService } from '../../services/madresService';
import { padrillosService } from '../../services/padrillosService';
import { Autocomplete, AutocompleteOption } from '../../../../components/ui/Autocomplete';
import type { ServicioCreateDTO, TipoServicio, OrigenSemen, Madre, Padrillo } from '../../types';

const ServicioCreateScreen: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const madreIdParam = searchParams.get('madreId');
  
  const [madres, setMadres] = useState<Madre[]>([]);
  const [padrillos, setPadrillos] = useState<Padrillo[]>([]);
  const [formData, setFormData] = useState<ServicioCreateDTO>({
    madreId: madreIdParam ? parseInt(madreIdParam) : 0,
    tipo: 'MONTA_NATURAL',
    fechaServicio: new Date().toISOString().split('T')[0],
    observaciones: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    cargarDatos();
  }, []);

  useEffect(() => {
    // Limpiar campos de macho cuando cambia el tipo de servicio
    if (formData.tipo === 'MONTA_NATURAL') {
      setFormData(prev => ({ ...prev, origenSemen: undefined, machoNombre: undefined }));
    } else if (formData.tipo === 'IA') {
      setFormData(prev => ({ ...prev, machoId: undefined }));
      if (!formData.origenSemen) {
        setFormData(prev => ({ ...prev, origenSemen: 'INTERNO' }));
      }
    }
  }, [formData.tipo]);

  useEffect(() => {
    // Limpiar campos de macho cuando cambia el origen del semen
    if (formData.origenSemen === 'INTERNO') {
      setFormData(prev => ({ ...prev, machoNombre: undefined }));
    } else if (formData.origenSemen === 'EXTERNO') {
      setFormData(prev => ({ ...prev, machoId: undefined }));
    }
  }, [formData.origenSemen]);

  const cargarDatos = async () => {
    try {
      const [madresData, padrillosData] = await Promise.all([
        madresService.listar(),
        padrillosService.listar(),
      ]);
      setMadres(madresData.filter((m: Madre) => m.estadoActual !== 'DESCARTE'));
      setPadrillos(padrillosData);
    } catch (error) {
      console.error('Error al cargar datos:', error);
    }
  };

  const calcularFechaControlCelo = (fechaServicio: string): string => {
    const fecha = new Date(fechaServicio);
    fecha.setDate(fecha.getDate() + 21);
    return fecha.toISOString().split('T')[0];
  };

  // Convertir madres a opciones para el Autocomplete
  const opcionesMadres: AutocompleteOption<Madre>[] = useMemo(() => {
    return madres.map(madre => ({
      value: madre.id!,
      label: `${madre.identificacion} - ${madre.estadoActual}`,
      data: madre,
    }));
  }, [madres]);

  // Convertir padrillos a opciones para el Autocomplete
  const opcionesPadrillos: AutocompleteOption<Padrillo>[] = useMemo(() => {
    return padrillos.map(padrillo => ({
      value: padrillo.id!,
      label: padrillo.identificacion,
      data: padrillo,
    }));
  }, [padrillos]);

  // Manejar selección de madre
  const handleMadreChange = (valor: string | number | undefined) => {
    const madreId =
      valor === undefined || valor === ''
        ? 0
        : typeof valor === 'string'
          ? Number(valor)
          : valor;
    setFormData({...formData, madreId: madreId || 0});
  };

  const handlePadrilloChange = (valor: string | number | undefined) => {
    const padrilloId =
      valor === undefined || valor === ''
        ? undefined
        : typeof valor === 'string'
          ? Number(valor)
          : valor;
    setFormData({...formData, machoId: padrilloId});
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    
    if (!formData.madreId || formData.madreId === 0) {
      setError('Debe seleccionar una madre');
      return;
    }

    setLoading(true);
    try {
      await serviciosService.crear(formData);
      navigate('/porcinos/servicios');
    } catch (error: any) {
      setError(error.response?.data?.message || 'Error al crear el servicio');
      console.error('Error:', error);
    } finally {
      setLoading(false);
    }
  };

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
          <Icon name="Plus" size={32} />
          Nuevo Servicio
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

      <form onSubmit={handleSubmit} style={{
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)'
      }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div>
            <Autocomplete
              options={opcionesMadres}
              value={formData.madreId || undefined}
              onChange={handleMadreChange}
              placeholder="Buscar madre por identificación..."
              label="Madre"
              required
              emptyMessage="No se encontraron madres"
              maxHeight={250}
            />
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
              Tipo de Servicio *
            </label>
            <select
              value={formData.tipo}
              onChange={(e) => setFormData({...formData, tipo: e.target.value as TipoServicio})}
              required
              style={{
                width: '100%',
                padding: '0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem'
              }}
            >
              <option value="MONTA_NATURAL">Monta Natural</option>
              <option value="IA">Inseminación Artificial</option>
            </select>
          </div>

          {formData.tipo === 'MONTA_NATURAL' && (
            <div>
              <Autocomplete
                options={opcionesPadrillos}
                value={formData.machoId || undefined}
                onChange={handlePadrilloChange}
                placeholder="Buscar padrillo por identificación..."
                label="Padrillo (opcional)"
                emptyMessage="No se encontraron padrillos"
                maxHeight={250}
              />
            </div>
          )}

          {formData.tipo === 'IA' && (
            <>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
                  Origen del Semen *
                </label>
                <select
                  value={formData.origenSemen || 'INTERNO'}
                  onChange={(e) => setFormData({...formData, origenSemen: e.target.value as OrigenSemen})}
                  required
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem'
                  }}
                >
                  <option value="INTERNO">Interno</option>
                  <option value="EXTERNO">Externo</option>
                </select>
              </div>

              {formData.origenSemen === 'INTERNO' && (
                <div>
                  <Autocomplete
                    options={opcionesPadrillos}
                    value={formData.machoId || undefined}
                    onChange={handlePadrilloChange}
                    placeholder="Buscar padrillo por identificación..."
                    label="Padrillo"
                    required
                    emptyMessage="No se encontraron padrillos"
                    maxHeight={250}
                  />
                </div>
              )}

              {formData.origenSemen === 'EXTERNO' && (
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
                    Información del Macho Externo *
                  </label>
                  <input
                    type="text"
                    value={formData.machoNombre || ''}
                    onChange={(e) => setFormData({...formData, machoNombre: e.target.value})}
                    required
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.375rem',
                      fontSize: '0.875rem'
                    }}
                    placeholder="Ingrese el nombre o identificación del macho externo"
                  />
                </div>
              )}
            </>
          )}

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
              Fecha del Servicio *
            </label>
            <input
              type="date"
              value={formData.fechaServicio}
              onChange={(e) => {
                const fecha = e.target.value;
                setFormData({...formData, fechaServicio: fecha});
              }}
              required
              style={{
                width: '100%',
                padding: '0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem'
              }}
            />
            {formData.fechaServicio && (
              <p style={{ marginTop: '0.5rem', fontSize: '0.75rem', color: '#6b7280' }}>
                Fecha de control de celo: {new Date(calcularFechaControlCelo(formData.fechaServicio)).toLocaleDateString('es-ES')}
              </p>
            )}
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
              {loading ? 'Guardando...' : 'Guardar Servicio'}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
};

export default ServicioCreateScreen;
