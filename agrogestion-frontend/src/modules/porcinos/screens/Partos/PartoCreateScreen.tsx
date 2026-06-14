import React, { useState, useEffect, useMemo } from 'react';
import { Icon } from '../../../../components/icons';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { partosService } from '../../services/partosService';
import { madresService } from '../../services/madresService';
import { gestacionService } from '../../services/gestacionService';
import { Autocomplete, AutocompleteOption } from '../../../../components/ui/Autocomplete';
import type { PartoCreateDTO, Madre, Gestacion } from '../../types';

const PartoCreateScreen: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const gestacionIdParam = searchParams.get('gestacionId');
  const madreIdParam = searchParams.get('madreId');

  const [madres, setMadres] = useState<Madre[]>([]);
  const [gestacionesActivas, setGestacionesActivas] = useState<Gestacion[]>([]);
  const [formData, setFormData] = useState<PartoCreateDTO>({
    madreId: madreIdParam ? parseInt(madreIdParam) : 0,
    gestacionId: gestacionIdParam ? parseInt(gestacionIdParam) : undefined,
    fechaInicio: new Date().toISOString(),
    fechaFin: undefined,
    nacidosVivos: 0,
    nacidosMuertos: 0,
    momias: 0,
    totalNacidos: 0,
    pesoPromedioNacimiento: 0,
    observaciones: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    cargarDatos();
  }, [gestacionIdParam, madreIdParam]);

  useEffect(() => {
    calcularTotal();
  }, [formData.nacidosVivos, formData.nacidosMuertos, formData.momias]);

  const cargarDatos = async () => {
    try {
      const [madresData, gestacionesData] = await Promise.all([
        madresService.listar(),
        gestacionService.obtenerActivas(),
      ]);
      setMadres(madresData.filter((m: Madre) => m.estadoActual === 'GESTACION' || m.estadoActual === 'LACTANCIA'));
      setGestacionesActivas(gestacionesData);
      
      // Si hay gestacionId, buscar la madre asociada
      if (gestacionIdParam) {
        const gestacion = gestacionesData.find((g: Gestacion) => g.id === parseInt(gestacionIdParam));
        if (gestacion) {
          setFormData(prev => ({ ...prev, madreId: gestacion.madreId, gestacionId: gestacion.id }));
        }
      } else if (madreIdParam) {
        // Si hay madreId, establecerlo y buscar su gestación activa
        const madreId = parseInt(madreIdParam);
        const gestacion = gestacionesData.find((g: Gestacion) => g.madreId === madreId);
        setFormData(prev => ({ 
          ...prev, 
          madreId: madreId,
          gestacionId: gestacion?.id 
        }));
      }
    } catch (error) {
      console.error('Error al cargar datos:', error);
    }
  };

  const calcularTotal = () => {
    const total = (formData.nacidosVivos || 0) + (formData.nacidosMuertos || 0) + (formData.momias || 0);
    setFormData(prev => ({ ...prev, totalNacidos: total }));
  };

  // Convertir madres a opciones para el Autocomplete
  const opcionesMadres: AutocompleteOption<Madre>[] = useMemo(() => {
    return madres.map(madre => ({
      value: madre.id!,
      label: `${madre.identificacion} - ${madre.estadoActual}`,
      data: madre,
    }));
  }, [madres]);

  // Manejar selección de madre
  const handleMadreChange = (valor: string | number | undefined, option?: AutocompleteOption<Madre>) => {
    const madreId =
      valor === undefined || valor === ''
        ? undefined
        : typeof valor === 'string'
          ? Number(valor)
          : valor;
    const id = madreId || 0;
    const gestacion = id > 0 ? gestacionesActivas.find(g => g.madreId === id) : undefined;
    setFormData({
      ...formData,
      madreId: id,
      gestacionId: gestacion?.id,
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!formData.madreId || formData.madreId === 0) {
      setError('Debe seleccionar una madre');
      return;
    }

    if (formData.totalNacidos === 0) {
      setError('Debe registrar al menos un nacido (vivo, muerto o momia)');
      return;
    }

    setLoading(true);
    try {
      await partosService.crear(formData);
      navigate('/porcinos/partos');
    } catch (error: any) {
      setError(error.response?.data?.message || 'Error al crear el parto');
      console.error('Error:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '2rem', maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ marginBottom: '2rem' }}>
        <button
          onClick={() => navigate('/porcinos/partos')}
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
          <Icon name="Baby" size={32} />
          Registrar Parto
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
          <Autocomplete
            options={opcionesMadres}
            value={formData.madreId && formData.madreId > 0 ? formData.madreId : undefined}
            onChange={handleMadreChange}
            placeholder="Buscar madre por identificación o estado..."
            label="Madre"
            required
            emptyMessage="No se encontraron madres"
            maxHeight={250}
          />

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
              Fecha y Hora Inicio *
            </label>
            <input
              type="datetime-local"
              value={formData.fechaInicio ? new Date(formData.fechaInicio).toISOString().slice(0, 16) : ''}
              onChange={(e) => setFormData({...formData, fechaInicio: new Date(e.target.value).toISOString()})}
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
              Fecha y Hora Fin
            </label>
            <input
              type="datetime-local"
              value={formData.fechaFin ? new Date(formData.fechaFin).toISOString().slice(0, 16) : ''}
              onChange={(e) => setFormData({...formData, fechaFin: e.target.value ? new Date(e.target.value).toISOString() : undefined})}
              style={{
                width: '100%',
                padding: '0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem'
              }}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '1rem' }}>
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
                Nacidos Vivos *
              </label>
              <input
                type="number"
                min="0"
                value={formData.nacidosVivos || 0}
                onChange={(e) => setFormData({...formData, nacidosVivos: parseInt(e.target.value) || 0})}
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
                Nacidos Muertos *
              </label>
              <input
                type="number"
                min="0"
                value={formData.nacidosMuertos || 0}
                onChange={(e) => setFormData({...formData, nacidosMuertos: parseInt(e.target.value) || 0})}
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
                Momias *
              </label>
              <input
                type="number"
                min="0"
                value={formData.momias || 0}
                onChange={(e) => setFormData({...formData, momias: parseInt(e.target.value) || 0})}
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
              Total Nacidos
            </label>
            <input
              type="number"
              value={formData.totalNacidos}
              readOnly
              style={{
                width: '100%',
                padding: '0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem',
                backgroundColor: '#f9fafb',
                fontWeight: '500'
              }}
            />
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
              Peso Promedio al Nacer (kg)
            </label>
            <input
              type="number"
              step="0.01"
              min="0"
              value={formData.pesoPromedioNacimiento || ''}
              onChange={(e) => setFormData({...formData, pesoPromedioNacimiento: parseFloat(e.target.value) || undefined})}
              style={{
                width: '100%',
                padding: '0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem'
              }}
              placeholder="Opcional"
            />
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
              placeholder="Observaciones adicionales sobre el parto..."
            />
          </div>

          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
            <button
              type="button"
              onClick={() => navigate('/porcinos/partos')}
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
              {loading ? 'Guardando...' : 'Registrar Parto'}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
};

export default PartoCreateScreen;
