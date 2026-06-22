import React, { useState, useEffect } from 'react';
import { useLocation, useSearchParams } from 'react-router-dom';
import { SemanticIcon, Icon } from './icons';
import ConfiguracionEstadosScreen from './ConfiguracionEstadosScreen';
import AdminUsuarios from './AdminUsuarios';
import ConfiguracionesScreen from '../modules/porcinos/screens/Configuraciones/ConfiguracionesScreen';
import GestionCampanasScreen from './GestionCampanasScreen';
import { configuracionService } from '../modules/porcinos/services/configuracionService';
import type { ConfiguracionPorcino } from '../modules/porcinos/types';
import useConceptoTemporalModulo from '../core/hooks/useConceptoTemporalModulo';

type TabPrincipal = 'modulo' | 'periodos' | 'usuarios' | 'general';

const TABS_VALIDOS: TabPrincipal[] = ['modulo', 'periodos', 'usuarios', 'general'];

// Hook personalizado para manejar mensajes
const useMensajes = () => {
  const [mensaje, setMensaje] = useState<{ tipo: 'exito' | 'error'; texto: string } | null>(null);

  const mostrarMensajeExito = (texto: string) => {
    setMensaje({ tipo: 'exito', texto });
    setTimeout(() => setMensaje(null), 3000);
  };

  const mostrarMensajeError = (texto: string) => {
    setMensaje({ tipo: 'error', texto });
    setTimeout(() => setMensaje(null), 5000);
  };

  const ocultarMensaje = () => {
    setMensaje(null);
  };

  return { mensaje, mostrarMensajeExito, mostrarMensajeError, ocultarMensaje };
};

// Componente para Configuración General (común para todos los módulos)
const ConfiguracionGeneralTab: React.FC = () => {
  const [configuraciones, setConfiguraciones] = useState<ConfiguracionPorcino[]>([]);
  const [loading, setLoading] = useState(true);
  const [mostrarFormulario, setMostrarFormulario] = useState(false);
  const [formData, setFormData] = useState<Partial<ConfiguracionPorcino>>({
    tipo: 'TEXTO',
    categoria: 'GENERAL'
  });
  const { mostrarMensajeExito, mostrarMensajeError } = useMensajes();

  useEffect(() => {
    cargarConfiguraciones();
  }, []);

  const cargarConfiguraciones = async () => {
    setLoading(true);
    try {
      const data = await configuracionService.listar();
      // Filtrar solo configuraciones generales del sistema (excluir categorías específicas de porcinos)
      const categoriasEspecificasPorcinos = ['ETAPAS', 'REPRODUCCION', 'ALIMENTACION', 'SANIDAD', 'PRODUCCION'];
      const configuracionesGenerales = data.filter((config: ConfiguracionPorcino) => 
        config.categoria === 'GENERAL' || 
        config.categoria === 'SISTEMA' ||
        (!categoriasEspecificasPorcinos.includes(config.categoria || ''))
      );
      setConfiguraciones(configuracionesGenerales);
    } catch (error) {
      console.error('Error al cargar configuraciones:', error);
      mostrarMensajeError('Error al cargar configuraciones');
    } finally {
      setLoading(false);
    }
  };

  const handleGuardar = async () => {
    if (!formData.clave || !formData.valor) {
      mostrarMensajeError('La clave y el valor son obligatorios');
      return;
    }

    try {
      await configuracionService.guardar(formData);
      await cargarConfiguraciones();
      setMostrarFormulario(false);
      setFormData({ tipo: 'TEXTO', categoria: 'GENERAL' });
      mostrarMensajeExito('Configuración guardada exitosamente');
    } catch (error) {
      console.error('Error al guardar configuración:', error);
      mostrarMensajeError('Error al guardar la configuración');
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '3rem' }}>
        <div style={{ fontSize: '2rem', marginBottom: '1rem' }}>⏳</div>
        <p>Cargando configuraciones...</p>
      </div>
    );
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937' }}>
          ⚙️ Configuraciones Generales del Sistema
        </h2>
        <button
          onClick={() => setMostrarFormulario(true)}
          style={{
            padding: '0.5rem 1rem',
            backgroundColor: '#3b82f6',
            color: 'white',
            border: 'none',
            borderRadius: '0.375rem',
            cursor: 'pointer',
            fontWeight: '500'
          }}
        >
          ➕ Nueva Configuración
        </button>
      </div>

      {mostrarFormulario && (
        <div style={{
          padding: '1.5rem',
          border: '1px solid #e5e7eb',
          borderRadius: '0.5rem',
          marginBottom: '1.5rem',
          backgroundColor: '#f9fafb'
        }}>
          <h3 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '1rem' }}>
            {formData.id ? 'Editar' : 'Nueva'} Configuración
          </h3>
          <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: '1fr 1fr' }}>
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>
                Clave *
              </label>
              <input
                type="text"
                value={formData.clave || ''}
                onChange={(e) => setFormData({ ...formData, clave: e.target.value.toUpperCase().replace(/\s/g, '_') })}
                placeholder="EJ: CONFIG_GENERAL"
                style={{
                  width: '100%',
                  padding: '0.5rem',
                  border: '1px solid #d1d5db',
                  borderRadius: '0.375rem'
                }}
                disabled={!!formData.id}
              />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>
                Valor *
              </label>
              <input
                type="text"
                value={formData.valor || ''}
                onChange={(e) => setFormData({ ...formData, valor: e.target.value })}
                placeholder="Valor de la configuración"
                style={{
                  width: '100%',
                  padding: '0.5rem',
                  border: '1px solid #d1d5db',
                  borderRadius: '0.375rem'
                }}
              />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>
                Tipo
              </label>
              <select
                value={formData.tipo || 'TEXTO'}
                onChange={(e) => setFormData({ ...formData, tipo: e.target.value as any })}
                style={{
                  width: '100%',
                  padding: '0.5rem',
                  border: '1px solid #d1d5db',
                  borderRadius: '0.375rem'
                }}
              >
                <option value="TEXTO">Texto</option>
                <option value="NUMERO">Número</option>
                <option value="BOOLEAN">Booleano</option>
                <option value="FECHA">Fecha</option>
              </select>
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>
                Categoría
              </label>
              <select
                value={formData.categoria || 'GENERAL'}
                onChange={(e) => setFormData({ ...formData, categoria: e.target.value })}
                style={{
                  width: '100%',
                  padding: '0.5rem',
                  border: '1px solid #d1d5db',
                  borderRadius: '0.375rem'
                }}
              >
                <option value="GENERAL">GENERAL</option>
                <option value="SISTEMA">SISTEMA</option>
              </select>
            </div>
            <div style={{ gridColumn: '1 / -1' }}>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>
                Descripción
              </label>
              <textarea
                value={formData.descripcion || ''}
                onChange={(e) => setFormData({ ...formData, descripcion: e.target.value })}
                placeholder="Descripción de la configuración"
                rows={3}
                style={{
                  width: '100%',
                  padding: '0.5rem',
                  border: '1px solid #d1d5db',
                  borderRadius: '0.375rem',
                  fontFamily: 'inherit'
                }}
              />
            </div>
          </div>
          <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
            <button
              onClick={handleGuardar}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: '#10b981',
                color: 'white',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: 'pointer',
                fontWeight: '500'
              }}
            >
              <Icon name="Save" size={16} style={{ marginRight: '0.25rem' }} /> Guardar
            </button>
            <button
              onClick={() => {
                setMostrarFormulario(false);
                setFormData({ tipo: 'TEXTO', categoria: 'GENERAL' });
              }}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: '#6b7280',
                color: 'white',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: 'pointer',
                fontWeight: '500'
              }}
            >
              <Icon name="X" size={16} style={{ marginRight: '0.25rem' }} /> Cancelar
            </button>
          </div>
        </div>
      )}

      {configuraciones.length === 0 ? (
        <div style={{
          padding: '3rem',
          textAlign: 'center',
          color: '#6b7280',
          border: '2px dashed #d1d5db',
          borderRadius: '0.5rem'
        }}>
          <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>⚙️</div>
          <p style={{ fontSize: '1.125rem', marginBottom: '0.5rem' }}>No hay configuraciones</p>
          <p style={{ fontSize: '0.875rem' }}>Crea una nueva configuración para comenzar</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))' }}>
          {configuraciones.map(config => (
            <div
              key={config.id}
              style={{
                padding: '1rem',
                border: '1px solid #e5e7eb',
                borderRadius: '0.5rem',
                backgroundColor: 'white',
                position: 'relative'
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', marginBottom: '0.5rem' }}>
                <div style={{ fontWeight: 'bold', color: '#1f2937', fontSize: '1rem' }}>
                  {config.clave}
                </div>
                <button
                  onClick={() => {
                    setFormData(config);
                    setMostrarFormulario(true);
                  }}
                  style={{
                    background: 'transparent',
                    border: 'none',
                    cursor: 'pointer',
                    fontSize: '0.875rem',
                    color: '#3b82f6'
                  }}
                  title="Editar"
                >
                  ✏️
                </button>
              </div>
              {config.descripcion && (
                <div style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.5rem' }}>
                  {config.descripcion}
                </div>
              )}
              <div style={{ marginTop: '0.5rem', padding: '0.5rem', backgroundColor: '#f3f4f6', borderRadius: '0.25rem' }}>
                <div style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>
                  Valor ({config.tipo}):
                </div>
                <div style={{ fontWeight: 'bold', color: '#1f2937' }}>{config.valor}</div>
              </div>
              {config.categoria && (
                <div style={{ marginTop: '0.5rem', fontSize: '0.75rem', color: '#9ca3af' }}>
                  Categoría: {config.categoria}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

const ConfiguracionUnificadaScreen: React.FC = () => {
  const location = useLocation();
  const [searchParams, setSearchParams] = useSearchParams();
  const [tabPrincipal, setTabPrincipal] = useState<TabPrincipal>('modulo');
  const { concepto } = useConceptoTemporalModulo();

  // Detectar el módulo desde la ruta
  const esModuloCultivos = location.pathname.startsWith('/cultivos');
  const esModuloPorcinos = location.pathname.startsWith('/porcinos');
  const nombreModulo = esModuloCultivos ? 'Cultivos' : esModuloPorcinos ? 'Porcinos' : 'Sistema';

  useEffect(() => {
    const tabParam = searchParams.get('tab');
    if (tabParam && TABS_VALIDOS.includes(tabParam as TabPrincipal)) {
      setTabPrincipal(tabParam as TabPrincipal);
    }
  }, [searchParams]);

  const cambiarTab = (tab: TabPrincipal) => {
    setTabPrincipal(tab);
    const params = new URLSearchParams(searchParams);
    if (tab === 'modulo') {
      params.delete('tab');
    } else {
      params.set('tab', tab);
    }
    setSearchParams(params, { replace: true });
  };

  const tabs = [
    { id: 'modulo' as TabPrincipal, nombre: 'Estados y tareas', icono: 'Settings' },
    {
      id: 'periodos' as TabPrincipal,
      nombre: concepto.etiquetaPeriodo === 'Campaña agrícola' ? 'Campañas' : 'Períodos',
      icono: 'CalendarDays',
    },
    { id: 'usuarios' as TabPrincipal, nombre: 'Usuarios', icono: 'Users' },
    { id: 'general' as TabPrincipal, nombre: 'General del sistema', icono: 'Cog' },
  ];

  return (
    <div style={{ padding: '2rem', maxWidth: '1400px', margin: '0 auto' }}>
      <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937', marginBottom: '2rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
        <SemanticIcon semanticName="settings" size={32} />
        Configuración - Módulo {nombreModulo}
      </h1>

      {/* Tabs principales */}
      <div style={{
        display: 'flex',
        gap: '0.5rem',
        marginBottom: '2rem',
        flexWrap: 'wrap',
        borderBottom: '2px solid #e5e7eb',
        paddingBottom: '1rem'
      }}>
        {tabs.map(tab => (
          <button
            key={tab.id}
            onClick={() => cambiarTab(tab.id)}
            style={{
              padding: '0.75rem 1.5rem',
              backgroundColor: tabPrincipal === tab.id ? '#3b82f6' : 'white',
              color: tabPrincipal === tab.id ? 'white' : '#6b7280',
              border: 'none',
              borderBottom: tabPrincipal === tab.id ? '3px solid #3b82f6' : '3px solid transparent',
              cursor: 'pointer',
              fontWeight: '600',
              borderRadius: '0.5rem 0.5rem 0 0',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
              transition: 'all 0.2s'
            }}
          >
            <Icon name={tab.icono} size={18} />
            {tab.nombre}
          </button>
        ))}
      </div>

      {/* Contenido de los tabs */}
      <div style={{
        padding: '1.5rem',
        backgroundColor: '#f9fafb',
        borderRadius: '0.5rem',
        minHeight: '400px'
      }}>
        {tabPrincipal === 'modulo' && (
          <>
            {esModuloCultivos && <ConfiguracionEstadosScreen />}
            {esModuloPorcinos && <ConfiguracionesScreen />}
            {!esModuloCultivos && !esModuloPorcinos && (
              <div style={{ textAlign: 'center', padding: '3rem' }}>
                <p>Módulo no reconocido</p>
              </div>
            )}
          </>
        )}

        {tabPrincipal === 'periodos' && (
          <GestionCampanasScreen incrustado />
        )}

        {tabPrincipal === 'usuarios' && (
          <AdminUsuarios />
        )}

        {tabPrincipal === 'general' && (
          <ConfiguracionGeneralTab />
        )}
      </div>
    </div>
  );
};

export default ConfiguracionUnificadaScreen;
