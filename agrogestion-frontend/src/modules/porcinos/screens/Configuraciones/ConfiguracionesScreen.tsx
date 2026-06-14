import React, { useState, useEffect } from 'react';
import {
  configuracionService,
  CLAVE_CONFIRMACION_CALENDARIO_SOLO_CON_RACION_REAL,
} from '../../services/configuracionService';
import { catalogosService } from '../../services/catalogosService';
import { parametrosService } from '../../services/parametrosService';
import { SemanticIcon, Icon } from '../../../../components/icons';
import type {
  ConfiguracionPorcino,
  RazaPorcino,
  TipoAlimentoPorcino,
  TipoServicioPorcino,
  CausaMortalidadPorcino,
  MotivoBajaPorcino,
  EsquemaSanitarioPorcino,
  TipoParto,
  UbicacionInterna,
  ParametrosEstablecimientoPorcino,
  ParametrosProductivosPorcino,
  DatosEconomicosPorcino,
} from '../../types';

type TabType = 
  | 'parametros-establecimiento'
  | 'parametros-productivos'
  | 'datos-economicos'
  | 'razas'
  | 'tipos-servicio'
  | 'causas-mortalidad'
  | 'motivos-baja'
  | 'esquemas-sanitarios'
  | 'tipos-parto'
  | 'ubicaciones-internas'
  | 'configuraciones-generales';

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

const ConfiguracionesScreen: React.FC = () => {
  const [tabActiva, setTabActiva] = useState<TabType>('parametros-establecimiento');
  const [loading, setLoading] = useState(false);
  const { mensaje, mostrarMensajeExito, mostrarMensajeError, ocultarMensaje } = useMensajes();

  const tabs = [
    { id: 'parametros-establecimiento' as TabType, nombre: 'Establecimiento', icono: 'Building2' },
    { id: 'parametros-productivos' as TabType, nombre: 'Productivos', icono: 'BarChart' },
    { id: 'datos-economicos' as TabType, nombre: 'Económicos', icono: 'DollarSign' },
    { id: 'razas' as TabType, nombre: 'Razas', icono: 'PiggyBank' },
    { id: 'tipos-servicio' as TabType, nombre: 'Servicios', icono: 'Heart' },
    { id: 'causas-mortalidad' as TabType, nombre: 'Mortalidad', icono: 'Skull' },
    { id: 'motivos-baja' as TabType, nombre: 'Motivos Baja', icono: 'Clipboard' },
    { id: 'esquemas-sanitarios' as TabType, nombre: 'Sanitarios', icono: 'Syringe' },
    { id: 'tipos-parto' as TabType, nombre: 'Tipos Parto', icono: 'Baby' },
    { id: 'ubicaciones-internas' as TabType, nombre: 'Ubicaciones', icono: 'MapPin' },
    { id: 'configuraciones-generales' as TabType, nombre: 'Generales', icono: 'Settings' },
  ];

  return (
    <div style={{ padding: '2rem', maxWidth: '1400px', margin: '0 auto' }}>
      <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937', marginBottom: '2rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
        <SemanticIcon semanticName="settings" size={32} />
        Configuraciones del Módulo Porcinos
      </h1>

      {/* Mensajes de éxito/error */}
      {mensaje && (
        <div
          style={{
            position: 'fixed',
            top: '20px',
            right: '20px',
            padding: '1rem 1.5rem',
            backgroundColor: mensaje.tipo === 'exito' ? '#10b981' : '#ef4444',
            color: 'white',
            borderRadius: '0.5rem',
            boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
            zIndex: 10000,
            animation: 'slideIn 0.3s ease',
            maxWidth: '400px'
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <SemanticIcon semanticName={mensaje.tipo === 'exito' ? 'success' : 'error'} size={20} />
            <span>{mensaje.texto}</span>
            <button
              onClick={ocultarMensaje}
              style={{
                marginLeft: 'auto',
                background: 'transparent',
                border: 'none',
                color: 'white',
                cursor: 'pointer',
                fontSize: '1.25rem',
                padding: '0 0.5rem'
              }}
            >
              ×
            </button>
          </div>
        </div>
      )}

      {/* Pestañas */}
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
            onClick={() => setTabActiva(tab.id)}
            style={{
              padding: '0.75rem 1.5rem',
              backgroundColor: tabActiva === tab.id ? '#3b82f6' : 'white',
              color: tabActiva === tab.id ? 'white' : '#1f2937',
              border: `1px solid ${tabActiva === tab.id ? '#3b82f6' : '#d1d5db'}`,
              borderRadius: '0.5rem 0.5rem 0 0',
              cursor: 'pointer',
              fontSize: '0.875rem',
              fontWeight: '500',
              transition: 'all 0.2s',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem'
            }}
            onMouseEnter={(e) => {
              if (tabActiva !== tab.id) {
                e.currentTarget.style.backgroundColor = '#f3f4f6';
              }
            }}
            onMouseLeave={(e) => {
              if (tabActiva !== tab.id) {
                e.currentTarget.style.backgroundColor = 'white';
              }
            }}
          >
            <Icon name={tab.icono as any} size={18} style={{ marginRight: '0.5rem' }} />
            <span>{tab.nombre}</span>
          </button>
        ))}
      </div>

      {/* Contenido de las pestañas */}
      <div style={{
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        minHeight: '400px'
      }}>
        {loading ? (
          <div style={{ textAlign: 'center', padding: '3rem' }}>
            <SemanticIcon semanticName="pending" size={32} />
            <p>Cargando...</p>
          </div>
        ) : (
          <>
            {tabActiva === 'parametros-establecimiento' && (
              <ParametrosEstablecimientoTab />
            )}
            {tabActiva === 'parametros-productivos' && (
              <ParametrosProductivosTab />
            )}
            {tabActiva === 'datos-economicos' && (
              <DatosEconomicosTab />
            )}
            {tabActiva === 'razas' && (
              <RazasTab mostrarMensajeExito={mostrarMensajeExito} mostrarMensajeError={mostrarMensajeError} />
            )}
            {tabActiva === 'tipos-servicio' && (
              <TiposServicioTab />
            )}
            {tabActiva === 'causas-mortalidad' && (
              <CausasMortalidadTab />
            )}
            {tabActiva === 'motivos-baja' && (
              <MotivosBajaTab />
            )}
            {tabActiva === 'esquemas-sanitarios' && (
              <EsquemasSanitariosTab />
            )}
            {tabActiva === 'tipos-parto' && (
              <TiposPartoTab />
            )}
            {tabActiva === 'ubicaciones-internas' && (
              <UbicacionesInternasTab />
            )}
            {tabActiva === 'configuraciones-generales' && (
              <ConfiguracionesGeneralesTab mostrarMensajeExito={mostrarMensajeExito} mostrarMensajeError={mostrarMensajeError} />
            )}
          </>
        )}
      </div>
    </div>
  );
};

// ============================================================================
// COMPONENTES DE PESTAÑAS
// ============================================================================

const ParametrosEstablecimientoTab: React.FC = () => {
  const [parametros, setParametros] = useState<ParametrosEstablecimientoPorcino | null>(null);
  const [editando, setEditando] = useState(false);
  const [formData, setFormData] = useState<Partial<ParametrosEstablecimientoPorcino>>({});

  useEffect(() => {
    cargarParametros();
  }, []);

  const cargarParametros = async () => {
    try {
      const data = await parametrosService.obtenerParametrosEstablecimiento();
      setParametros(data);
      if (data) {
        setFormData(data);
      } else {
        setFormData({
          unidadManejo: 'LOTES',
          nombreEstablecimiento: '',
        });
      }
    } catch (error) {
      console.error('Error al cargar parámetros:', error);
    }
  };

  const handleGuardar = async () => {
    try {
      await parametrosService.guardarParametrosEstablecimiento(formData);
      await cargarParametros();
      setEditando(false);
      alert('Parámetros guardados correctamente');
    } catch (error) {
      console.error('Error al guardar:', error);
      alert('Error al guardar los parámetros');
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937' }}>
          Parámetros del Establecimiento
        </h2>
        {!editando ? (
          <button
            onClick={() => setEditando(true)}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: '#3b82f6',
              color: 'white',
              border: 'none',
              borderRadius: '0.375rem',
              cursor: 'pointer'
            }}
          >
            <Icon name="Pencil" size={16} style={{ marginRight: '0.25rem' }} /> Editar
          </button>
        ) : (
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button
              onClick={handleGuardar}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: '#10b981',
                color: 'white',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: 'pointer'
              }}
            >
              <Icon name="Save" size={16} style={{ marginRight: '0.25rem' }} /> Guardar
            </button>
            <button
              onClick={() => {
                setEditando(false);
                cargarParametros();
              }}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: '#6b7280',
                color: 'white',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: 'pointer'
              }}
            >
              <Icon name="X" size={16} style={{ marginRight: '0.25rem' }} /> Cancelar
            </button>
          </div>
        )}
      </div>

      <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))' }}>
        <CampoFormulario
          label="Nombre del Establecimiento *"
          value={formData.nombreEstablecimiento || ''}
          onChange={(val) => setFormData({ ...formData, nombreEstablecimiento: val })}
          editando={editando}
          tipo="text"
        />
        <CampoFormulario
          label="Provincia"
          value={formData.provincia || ''}
          onChange={(val) => setFormData({ ...formData, provincia: val })}
          editando={editando}
          tipo="text"
        />
        <CampoFormulario
          label="Localidad"
          value={formData.localidad || ''}
          onChange={(val) => setFormData({ ...formData, localidad: val })}
          editando={editando}
          tipo="text"
        />
        <CampoFormulario
          label="Razón Social"
          value={formData.razonSocial || ''}
          onChange={(val) => setFormData({ ...formData, razonSocial: val })}
          editando={editando}
          tipo="text"
        />
        <CampoFormulario
          label="Unidad de Manejo *"
          value={formData.unidadManejo || 'LOTES'}
          onChange={(val) => setFormData({ ...formData, unidadManejo: val as any })}
          editando={editando}
          tipo="select"
          opciones={[
            { valor: 'LOTES', etiqueta: 'Lotes' },
            { valor: 'GRUPOS', etiqueta: 'Grupos' },
            { valor: 'ANIMALES_INDIVIDUALES', etiqueta: 'Animales Individuales' },
          ]}
        />
        <CampoFormulario
          label="Máximo de Madres"
          value={formData.maximoMadres?.toString() || ''}
          onChange={(val) => setFormData({ ...formData, maximoMadres: parseInt(val) || undefined })}
          editando={editando}
          tipo="number"
        />
        <CampoFormulario
          label="Máximo de Padrillos"
          value={formData.maximoPadrillos?.toString() || ''}
          onChange={(val) => setFormData({ ...formData, maximoPadrillos: parseInt(val) || undefined })}
          editando={editando}
          tipo="number"
        />
        <CampoFormulario
          label="Máxima Capacidad Recría/Engorde"
          value={formData.maximaCapacidadRecriaEngorde?.toString() || ''}
          onChange={(val) => setFormData({ ...formData, maximaCapacidadRecriaEngorde: parseInt(val) || undefined })}
          editando={editando}
          tipo="number"
        />
      </div>
    </div>
  );
};

// Componente auxiliar para campos de formulario
const CampoFormulario: React.FC<{
  label: string;
  value: string;
  onChange: (val: string) => void;
  editando: boolean;
  tipo: 'text' | 'number' | 'select';
  opciones?: { valor: string; etiqueta: string }[];
}> = ({ label, value, onChange, editando, tipo, opciones }) => {
  return (
    <div>
      <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#374151' }}>
        {label}
      </label>
      {editando ? (
        tipo === 'select' && opciones ? (
          <select
            value={value}
            onChange={(e) => onChange(e.target.value)}
            style={{
              width: '100%',
              padding: '0.5rem',
              border: '1px solid #d1d5db',
              borderRadius: '0.375rem',
              fontSize: '0.875rem'
            }}
          >
            {opciones.map(op => (
              <option key={op.valor} value={op.valor}>{op.etiqueta}</option>
            ))}
          </select>
        ) : (
          <input
            type={tipo}
            value={value}
            onChange={(e) => onChange(e.target.value)}
            style={{
              width: '100%',
              padding: '0.5rem',
              border: '1px solid #d1d5db',
              borderRadius: '0.375rem',
              fontSize: '0.875rem'
            }}
          />
        )
      ) : (
        <div style={{
          padding: '0.5rem',
          backgroundColor: '#f9fafb',
          borderRadius: '0.375rem',
          fontSize: '0.875rem',
          color: value ? '#1f2937' : '#9ca3af'
        }}>
          {value || 'No configurado'}
        </div>
      )}
    </div>
  );
};

// ============================================================================
// PARÁMETROS PRODUCTIVOS
// ============================================================================

// Valores por defecto del sistema (usados en cálculos cuando no hay configuración)
const VALORES_POR_DEFECTO_PRODUCTIVOS: ParametrosProductivosPorcino = {
  // Ciclo de producción
  diasPromedioGestacion: 115,
  diasLactancia: 21,
  diasRecriaAntesEngorde: 60,
  diasEngorde: 120,
  // Parámetros reproductivos
  diasToleranciaVencimientoGestacion: 5,
  diasControlCelo: 21,
  diasEntreCelos: 21,
  diasPasajeMaternidad: 7,
  // Servicios
  cantidadMaximaServiciosPadrilloDia: 3,
  tiempoEsperaEntreServiciosHoras: 12,
  // Alertas
  diasAntelacionAlertarPartos: 7,
  diasAntelacionAlertarEcografias: 3,
  diasAntelacionAlertarDestetes: 2,
  diasAntelacionAlertarPasajeMaternidad: 5,
  diasAntelacionAlertarRevisionesSanitarias: 1,
  // Umbrales
  umbralMortalidadLactanciaPorcentaje: 10.0,
  umbralMortalidadRecriaPorcentaje: 5.0,
  porcentajeMinimoPrenezAntesAdvertencia: 85.0,
  // Pesos estándar (kg)
  pesoPromedioNacimiento: 1.5,
  pesoDesteteObjetivo: 7.0,
  pesoVentaObjetivo: 110.0,
  // Índices productivos objetivo
  lechonesVivosPartoObjetivo: 12.0,
  lechonesDestetadosObjetivo: 11.0,
  partosMadreAnioObjetivo: 2.4,
};

const ParametrosProductivosTab: React.FC = () => {
  const [parametros, setParametros] = useState<ParametrosProductivosPorcino | null>(null);
  const [editando, setEditando] = useState(false);
  const [formData, setFormData] = useState<Partial<ParametrosProductivosPorcino>>(VALORES_POR_DEFECTO_PRODUCTIVOS);
  const [cargado, setCargado] = useState(false);

  useEffect(() => {
    cargarParametros();
  }, []);

  const cargarParametros = async () => {
    try {
      const data = await parametrosService.obtenerParametrosProductivos();
      setParametros(data);
      // Si hay datos del servidor, usarlos; si no, usar valores por defecto
      if (data) {
        setFormData({ ...VALORES_POR_DEFECTO_PRODUCTIVOS, ...data });
      } else {
        setFormData(VALORES_POR_DEFECTO_PRODUCTIVOS);
      }
      setCargado(true);
    } catch (error) {
      console.error('Error al cargar parámetros:', error);
      // En caso de error, mostrar valores por defecto
      setFormData(VALORES_POR_DEFECTO_PRODUCTIVOS);
      setCargado(true);
    }
  };

  const handleGuardar = async () => {
    try {
      await parametrosService.guardarParametrosProductivos(formData);
      await cargarParametros();
      setEditando(false);
      alert('Parámetros guardados correctamente');
    } catch (error: any) {
      console.error('Error al guardar:', error);
      const mensaje = error.response?.data?.message || error.message || 'Error al guardar los parámetros';
      alert(mensaje);
    }
  };

  if (!cargado) {
    return <div style={{ textAlign: 'center', padding: '2rem' }}>⏳ Cargando parámetros...</div>;
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937' }}>
          Parámetros Productivos
        </h2>
        {!editando ? (
          <button onClick={() => setEditando(true)} style={botonEstilo('#3b82f6')}>
            <Icon name="Pencil" size={16} style={{ marginRight: '0.25rem' }} /> Editar
          </button>
        ) : (
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button onClick={handleGuardar} style={botonEstilo('#10b981')}>💾 Guardar</button>
            <button onClick={() => { setEditando(false); cargarParametros(); }} style={botonEstilo('#6b7280')}>✕ Cancelar</button>
          </div>
        )}
      </div>

      {!parametros && (
        <div style={{ 
          backgroundColor: '#fef3c7', 
          border: '1px solid #f59e0b', 
          borderRadius: '0.5rem', 
          padding: '1rem', 
          marginBottom: '1.5rem',
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem'
        }}>
          <span>⚠️</span>
          <span>Estos son valores por defecto del sistema. Haga clic en "Editar" y "Guardar" para personalizarlos para su establecimiento.</span>
        </div>
      )}

      {/* Ciclo de Producción */}
      <div style={{ marginBottom: '2rem' }}>
        <h3 style={{ fontSize: '1.25rem', fontWeight: '600', color: '#374151', marginBottom: '1rem', borderBottom: '2px solid #e5e7eb', paddingBottom: '0.5rem' }}>
          🔄 Ciclo de Producción
        </h3>
        <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))' }}>
          <CampoFormulario label="Días Promedio Gestación" value={formData.diasPromedioGestacion?.toString() || ''} onChange={(val) => setFormData({ ...formData, diasPromedioGestacion: parseInt(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="Días Lactancia" value={formData.diasLactancia?.toString() || ''} onChange={(val) => setFormData({ ...formData, diasLactancia: parseInt(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="Días Recría Antes Engorde" value={formData.diasRecriaAntesEngorde?.toString() || ''} onChange={(val) => setFormData({ ...formData, diasRecriaAntesEngorde: parseInt(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="Días Engorde" value={formData.diasEngorde?.toString() || ''} onChange={(val) => setFormData({ ...formData, diasEngorde: parseInt(val) || undefined })} editando={editando} tipo="number" />
        </div>
      </div>

      {/* Parámetros Reproductivos */}
      <div style={{ marginBottom: '2rem' }}>
        <h3 style={{ fontSize: '1.25rem', fontWeight: '600', color: '#374151', marginBottom: '1rem', borderBottom: '2px solid #e5e7eb', paddingBottom: '0.5rem' }}>
          🐷 Parámetros Reproductivos
        </h3>
        <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))' }}>
          <CampoFormulario label="Días Tolerancia Vencimiento Gestación" value={formData.diasToleranciaVencimientoGestacion?.toString() || ''} onChange={(val) => setFormData({ ...formData, diasToleranciaVencimientoGestacion: parseInt(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="Días Control de Celo" value={formData.diasControlCelo?.toString() || ''} onChange={(val) => setFormData({ ...formData, diasControlCelo: parseInt(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="Días Entre Celos" value={formData.diasEntreCelos?.toString() || ''} onChange={(val) => setFormData({ ...formData, diasEntreCelos: parseInt(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="Días Pasaje a Maternidad (antes parto)" value={formData.diasPasajeMaternidad?.toString() || ''} onChange={(val) => setFormData({ ...formData, diasPasajeMaternidad: parseInt(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="Máx. Servicios Padrillo/Día" value={formData.cantidadMaximaServiciosPadrilloDia?.toString() || ''} onChange={(val) => setFormData({ ...formData, cantidadMaximaServiciosPadrilloDia: parseInt(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="Tiempo Espera Entre Servicios (horas)" value={formData.tiempoEsperaEntreServiciosHoras?.toString() || ''} onChange={(val) => setFormData({ ...formData, tiempoEsperaEntreServiciosHoras: parseInt(val) || undefined })} editando={editando} tipo="number" />
        </div>
      </div>

      {/* Pesos Estándar */}
      <div style={{ marginBottom: '2rem' }}>
        <h3 style={{ fontSize: '1.25rem', fontWeight: '600', color: '#374151', marginBottom: '1rem', borderBottom: '2px solid #e5e7eb', paddingBottom: '0.5rem' }}>
          ⚖️ Pesos Estándar (kg)
        </h3>
        <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))' }}>
          <CampoFormulario label="Peso Promedio Nacimiento (kg)" value={formData.pesoPromedioNacimiento?.toString() || ''} onChange={(val) => setFormData({ ...formData, pesoPromedioNacimiento: parseFloat(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="Peso Destete Objetivo (kg)" value={formData.pesoDesteteObjetivo?.toString() || ''} onChange={(val) => setFormData({ ...formData, pesoDesteteObjetivo: parseFloat(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="Peso Venta Objetivo (kg)" value={formData.pesoVentaObjetivo?.toString() || ''} onChange={(val) => setFormData({ ...formData, pesoVentaObjetivo: parseFloat(val) || undefined })} editando={editando} tipo="number" />
        </div>
      </div>

      {/* Índices Productivos Objetivo */}
      <div style={{ marginBottom: '2rem' }}>
        <h3 style={{ fontSize: '1.25rem', fontWeight: '600', color: '#374151', marginBottom: '1rem', borderBottom: '2px solid #e5e7eb', paddingBottom: '0.5rem' }}>
          📈 Índices Productivos Objetivo
        </h3>
        <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))' }}>
          <CampoFormulario label="Lechones Vivos/Parto Objetivo" value={formData.lechonesVivosPartoObjetivo?.toString() || ''} onChange={(val) => setFormData({ ...formData, lechonesVivosPartoObjetivo: parseFloat(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="Lechones Destetados Objetivo" value={formData.lechonesDestetadosObjetivo?.toString() || ''} onChange={(val) => setFormData({ ...formData, lechonesDestetadosObjetivo: parseFloat(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="Partos/Madre/Año Objetivo" value={formData.partosMadreAnioObjetivo?.toString() || ''} onChange={(val) => setFormData({ ...formData, partosMadreAnioObjetivo: parseFloat(val) || undefined })} editando={editando} tipo="number" />
        </div>
      </div>

      {/* Alertas y Recordatorios */}
      <div style={{ marginBottom: '2rem' }}>
        <h3 style={{ fontSize: '1.25rem', fontWeight: '600', color: '#374151', marginBottom: '1rem', borderBottom: '2px solid #e5e7eb', paddingBottom: '0.5rem' }}>
          🔔 Alertas y Recordatorios
        </h3>
        <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))' }}>
          <CampoFormulario label="Días Antelación - Partos" value={formData.diasAntelacionAlertarPartos?.toString() || ''} onChange={(val) => setFormData({ ...formData, diasAntelacionAlertarPartos: parseInt(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="Días Antelación - Ecografías" value={formData.diasAntelacionAlertarEcografias?.toString() || ''} onChange={(val) => setFormData({ ...formData, diasAntelacionAlertarEcografias: parseInt(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="Días Antelación - Destetes" value={formData.diasAntelacionAlertarDestetes?.toString() || ''} onChange={(val) => setFormData({ ...formData, diasAntelacionAlertarDestetes: parseInt(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="Días Antelación - Pasaje Maternidad" value={formData.diasAntelacionAlertarPasajeMaternidad?.toString() || ''} onChange={(val) => setFormData({ ...formData, diasAntelacionAlertarPasajeMaternidad: parseInt(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="Días Antelación - Revisiones Sanitarias" value={formData.diasAntelacionAlertarRevisionesSanitarias?.toString() || ''} onChange={(val) => setFormData({ ...formData, diasAntelacionAlertarRevisionesSanitarias: parseInt(val) || undefined })} editando={editando} tipo="number" />
        </div>
      </div>

      {/* Umbrales de Control */}
      <div>
        <h3 style={{ fontSize: '1.25rem', fontWeight: '600', color: '#374151', marginBottom: '1rem', borderBottom: '2px solid #e5e7eb', paddingBottom: '0.5rem' }}>
          ⚠️ Umbrales de Control
        </h3>
        <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))' }}>
          <CampoFormulario label="Umbral Mortalidad Lactancia (%)" value={formData.umbralMortalidadLactanciaPorcentaje?.toString() || ''} onChange={(val) => setFormData({ ...formData, umbralMortalidadLactanciaPorcentaje: parseFloat(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="Umbral Mortalidad Recría (%)" value={formData.umbralMortalidadRecriaPorcentaje?.toString() || ''} onChange={(val) => setFormData({ ...formData, umbralMortalidadRecriaPorcentaje: parseFloat(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="% Mínimo Preñez (Advertencia)" value={formData.porcentajeMinimoPrenezAntesAdvertencia?.toString() || ''} onChange={(val) => setFormData({ ...formData, porcentajeMinimoPrenezAntesAdvertencia: parseFloat(val) || undefined })} editando={editando} tipo="number" />
        </div>
      </div>
    </div>
  );
};

// ============================================================================
// DATOS ECONÓMICOS
// ============================================================================

// Valores por defecto económicos del sistema
const VALORES_POR_DEFECTO_ECONOMICOS: DatosEconomicosPorcino = {
  costoMadreGestacionDia: 15.0,
  costoMadreLactanciaDia: 25.0,
  costoLechon: 50.0,
  costoEngordeDia: 8.0,
  costoManoObraDia: 200.0,
  precioVentaCerdoTerminadoKg: 180.0,
  porcentajeMermaTransporte: 2.0,
  kgMaizPorRacionEngorde: 2.5,
  porcentajeMezclaAlimentoPropioBalanceado: 60.0,
  indiceConversionObjetivo: 3.2,
  metodoImputacionCostoCultivo: 'PROMEDIO_PONDERADO',
};

const DatosEconomicosTab: React.FC = () => {
  const [datos, setDatos] = useState<DatosEconomicosPorcino | null>(null);
  const [editando, setEditando] = useState(false);
  const [formData, setFormData] = useState<Partial<DatosEconomicosPorcino>>(VALORES_POR_DEFECTO_ECONOMICOS);
  const [cargado, setCargado] = useState(false);

  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    try {
      const data = await parametrosService.obtenerDatosEconomicos();
      setDatos(data);
      // Si hay datos del servidor, usarlos; si no, usar valores por defecto
      if (data) {
        setFormData({ ...VALORES_POR_DEFECTO_ECONOMICOS, ...data });
      } else {
        setFormData(VALORES_POR_DEFECTO_ECONOMICOS);
      }
      setCargado(true);
    } catch (error) {
      console.error('Error al cargar datos:', error);
      setFormData(VALORES_POR_DEFECTO_ECONOMICOS);
      setCargado(true);
    }
  };

  const handleGuardar = async () => {
    try {
      await parametrosService.guardarDatosEconomicos(formData);
      await cargarDatos();
      setEditando(false);
      alert('Datos guardados correctamente');
    } catch (error: any) {
      console.error('Error al guardar:', error);
      const mensaje = error.response?.data?.message || error.message || 'Error al guardar los datos';
      alert(mensaje);
    }
  };

  if (!cargado) {
    return <div style={{ textAlign: 'center', padding: '2rem' }}>⏳ Cargando datos económicos...</div>;
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937' }}>
          Datos Económicos
        </h2>
        {!editando ? (
          <button onClick={() => setEditando(true)} style={botonEstilo('#3b82f6')}>✏️ Editar</button>
        ) : (
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button onClick={handleGuardar} style={botonEstilo('#10b981')}>💾 Guardar</button>
            <button onClick={() => { setEditando(false); cargarDatos(); }} style={botonEstilo('#6b7280')}>✕ Cancelar</button>
          </div>
        )}
      </div>

      {!datos && (
        <div style={{ 
          backgroundColor: '#fef3c7', 
          border: '1px solid #f59e0b', 
          borderRadius: '0.5rem', 
          padding: '1rem', 
          marginBottom: '1.5rem',
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem'
        }}>
          <span>⚠️</span>
          <span>Estos son valores de referencia. Haga clic en "Editar" y "Guardar" para personalizarlos según los costos reales de su establecimiento.</span>
        </div>
      )}

      <div style={{ marginBottom: '2rem' }}>
        <h3 style={{ fontSize: '1.25rem', fontWeight: '600', color: '#374151', marginBottom: '1rem', borderBottom: '2px solid #e5e7eb', paddingBottom: '0.5rem' }}>
          💰 Costos por Categoría ($/día o $/unidad)
        </h3>
        <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))' }}>
          <CampoFormulario label="Costo Madre Gestación/Día ($)" value={formData.costoMadreGestacionDia?.toString() || ''} onChange={(val) => setFormData({ ...formData, costoMadreGestacionDia: parseFloat(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="Costo Madre Lactancia/Día ($)" value={formData.costoMadreLactanciaDia?.toString() || ''} onChange={(val) => setFormData({ ...formData, costoMadreLactanciaDia: parseFloat(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="Costo Lechón ($)" value={formData.costoLechon?.toString() || ''} onChange={(val) => setFormData({ ...formData, costoLechon: parseFloat(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="Costo Engorde/Día ($)" value={formData.costoEngordeDia?.toString() || ''} onChange={(val) => setFormData({ ...formData, costoEngordeDia: parseFloat(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="Costo Mano de Obra/Día ($)" value={formData.costoManoObraDia?.toString() || ''} onChange={(val) => setFormData({ ...formData, costoManoObraDia: parseFloat(val) || undefined })} editando={editando} tipo="number" />
        </div>
      </div>

      <div style={{ marginBottom: '2rem' }}>
        <h3 style={{ fontSize: '1.25rem', fontWeight: '600', color: '#374151', marginBottom: '1rem', borderBottom: '2px solid #e5e7eb', paddingBottom: '0.5rem' }}>
          📊 Ventas y Mermas
        </h3>
        <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))' }}>
          <CampoFormulario label="Precio Venta Cerdo Terminado ($/kg)" value={formData.precioVentaCerdoTerminadoKg?.toString() || ''} onChange={(val) => setFormData({ ...formData, precioVentaCerdoTerminadoKg: parseFloat(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="% Merma en Transporte" value={formData.porcentajeMermaTransporte?.toString() || ''} onChange={(val) => setFormData({ ...formData, porcentajeMermaTransporte: parseFloat(val) || undefined })} editando={editando} tipo="number" />
        </div>
      </div>

      <div>
        <h3 style={{ fontSize: '1.25rem', fontWeight: '600', color: '#374151', marginBottom: '1rem', borderBottom: '2px solid #e5e7eb', paddingBottom: '0.5rem' }}>
          🌾 Integración con Cultivos
        </h3>
        <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))' }}>
          <CampoFormulario label="Kg Maíz por Ración Engorde" value={formData.kgMaizPorRacionEngorde?.toString() || ''} onChange={(val) => setFormData({ ...formData, kgMaizPorRacionEngorde: parseFloat(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="% Mezcla Alimento Propio/Balanceado" value={formData.porcentajeMezclaAlimentoPropioBalanceado?.toString() || ''} onChange={(val) => setFormData({ ...formData, porcentajeMezclaAlimentoPropioBalanceado: parseFloat(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="Índice Conversión Objetivo" value={formData.indiceConversionObjetivo?.toString() || ''} onChange={(val) => setFormData({ ...formData, indiceConversionObjetivo: parseFloat(val) || undefined })} editando={editando} tipo="number" />
          <CampoFormulario label="Método Imputación Costo Cultivo" value={formData.metodoImputacionCostoCultivo || 'PROMEDIO_PONDERADO'} onChange={(val) => setFormData({ ...formData, metodoImputacionCostoCultivo: val as any })} editando={editando} tipo="select" opciones={[
            { valor: 'PROMEDIO_PONDERADO', etiqueta: 'Promedio Ponderado' },
            { valor: 'PRECIO_MERCADO', etiqueta: 'Precio de Mercado' },
            { valor: 'PRECIO_MANUAL', etiqueta: 'Precio Manual' },
          ]} />
          <CampoFormulario label="Precio Manual Cultivo ($/kg)" value={formData.precioManualCultivoKg?.toString() || ''} onChange={(val) => setFormData({ ...formData, precioManualCultivoKg: parseFloat(val) || undefined })} editando={editando} tipo="number" />
        </div>
      </div>
    </div>
  );
};

// ============================================================================
// RAZAS
// ============================================================================

const RazasTab: React.FC<{ mostrarMensajeExito: (texto: string) => void; mostrarMensajeError: (texto: string) => void }> = ({ mostrarMensajeExito, mostrarMensajeError }) => {
  const [razas, setRazas] = useState<RazaPorcino[]>([]);
  const [mostrarFormulario, setMostrarFormulario] = useState(false);
  const [editando, setEditando] = useState<RazaPorcino | null>(null);
  const [formData, setFormData] = useState<Partial<RazaPorcino>>({ tipo: 'MADRE' });

  useEffect(() => {
    cargarRazas();
  }, []);

  const cargarRazas = async () => {
    try {
      const data = await catalogosService.listarRazas();
      setRazas(data);
    } catch (error) {
      console.error('Error al cargar razas:', error);
    }
  };

  const handleGuardar = async () => {
    try {
      if (!formData.nombre || formData.nombre.trim() === '') {
        alert('El nombre es obligatorio');
        return;
      }
      
      if (editando?.id) {
        await catalogosService.guardarRaza({ ...editando, ...formData });
      } else {
        await catalogosService.guardarRaza(formData);
      }
      await cargarRazas();
      setMostrarFormulario(false);
      setEditando(null);
      setFormData({ tipo: 'MADRE' });
      mostrarMensajeExito('Raza guardada correctamente');
    } catch (error: any) {
      console.error('Error al guardar:', error);
      const mensaje = error.response?.data?.message || error.message || 'Error al guardar la raza';
      mostrarMensajeError(mensaje);
    }
  };

  const handleEliminar = async (id: number) => {
    if (!confirm('¿Está seguro de eliminar esta raza?')) return;
    try {
      await catalogosService.eliminarRaza(id);
      await cargarRazas();
      mostrarMensajeExito('Raza eliminada correctamente');
    } catch (error: any) {
      console.error('Error al eliminar:', error);
      const mensaje = error.response?.data?.message || error.message || 'Error al eliminar la raza';
      mostrarMensajeError(mensaje);
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937' }}>Razas</h2>
        <button onClick={() => { setMostrarFormulario(true); setEditando(null); setFormData({ tipo: 'MADRE' }); }} style={botonEstilo('#10b981')}>
          <Icon name="Plus" size={16} style={{ marginRight: '0.25rem' }} /> Agregar Raza
        </button>
      </div>

      {mostrarFormulario && (
        <div style={{ marginBottom: '2rem', padding: '1.5rem', backgroundColor: '#f9fafb', borderRadius: '0.5rem' }}>
          <h3 style={{ marginBottom: '1rem', fontSize: '1.25rem', fontWeight: '600' }}>
            {editando ? 'Editar' : 'Nueva'} Raza
          </h3>
          <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))' }}>
            <CampoFormulario label="Nombre *" value={formData.nombre || ''} onChange={(val) => setFormData({ ...formData, nombre: val })} editando={true} tipo="text" />
            <CampoFormulario label="Tipo *" value={formData.tipo || 'MADRE'} onChange={(val) => setFormData({ ...formData, tipo: val as any })} editando={true} tipo="select" opciones={[
              { valor: 'MADRE', etiqueta: 'Madre' },
              { valor: 'PADRILLO', etiqueta: 'Padrillo' },
              { valor: 'HIBRIDO', etiqueta: 'Híbrido' },
            ]} />
            <CampoFormulario label="Descripción" value={formData.descripcion || ''} onChange={(val) => setFormData({ ...formData, descripcion: val })} editando={true} tipo="text" />
          </div>
          <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
            <button onClick={handleGuardar} style={botonEstilo('#10b981')}>💾 Guardar</button>
            <button onClick={() => { setMostrarFormulario(false); setEditando(null); setFormData({ tipo: 'MADRE' }); }} style={botonEstilo('#6b7280')}>✕ Cancelar</button>
          </div>
        </div>
      )}

      <TablaCatalogos
        datos={razas}
        columnas={[
          { clave: 'nombre', etiqueta: 'Nombre' },
          { clave: 'tipo', etiqueta: 'Tipo' },
          { clave: 'descripcion', etiqueta: 'Descripción' },
        ]}
        onEditar={(item) => { setEditando(item); setFormData(item); setMostrarFormulario(true); }}
        onEliminar={(item) => item.id && handleEliminar(item.id)}
        filtros={[
          {
            clave: 'tipo',
            etiqueta: 'Tipo',
            opciones: [
              { valor: 'MADRE', etiqueta: 'Madre' },
              { valor: 'PADRILLO', etiqueta: 'Padrillo' },
              { valor: 'HIBRIDO', etiqueta: 'Híbrido' },
            ]
          }
        ]}
      />
    </div>
  );
};

// Componente reutilizable para tablas de catálogos con búsqueda y filtros
const TablaCatalogos: React.FC<{
  datos: any[];
  columnas: { clave: string; etiqueta: string }[];
  onEditar: (item: any) => void;
  onEliminar: (item: any) => void;
  filtros?: { clave: string; etiqueta: string; opciones: { valor: string; etiqueta: string }[] }[];
}> = ({ datos, columnas, onEditar, onEliminar, filtros }) => {
  const [busqueda, setBusqueda] = useState('');
  const [filtrosActivos, setFiltrosActivos] = useState<Record<string, string>>({});

  // Filtrar datos
  const datosFiltrados = datos.filter(item => {
    // Búsqueda por texto
    if (busqueda) {
      const textoBusqueda = busqueda.toLowerCase();
      const coincide = columnas.some(col => {
        const valor = item[col.clave];
        return valor && valor.toString().toLowerCase().includes(textoBusqueda);
      });
      if (!coincide) return false;
    }
    
    // Filtros por columna
    if (filtros) {
      for (const filtro of filtros) {
        const valorFiltro = filtrosActivos[filtro.clave];
        if (valorFiltro && valorFiltro !== 'TODOS' && item[filtro.clave] !== valorFiltro) {
          return false;
        }
      }
    }
    
    return true;
  });

  if (datos.length === 0) {
    return <div style={{ textAlign: 'center', padding: '2rem', color: '#6b7280' }}>No hay datos disponibles</div>;
  }

  return (
    <div>
      {/* Barra de búsqueda y filtros */}
      <div style={{ marginBottom: '1rem', display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'center' }}>
        <input
          type="text"
          placeholder="🔍 Buscar..."
          value={busqueda}
          onChange={(e) => setBusqueda(e.target.value)}
          style={{
            flex: 1,
            minWidth: '200px',
            padding: '0.5rem',
            border: '1px solid #d1d5db',
            borderRadius: '0.375rem',
            fontSize: '0.875rem'
          }}
        />
        {filtros && filtros.map(filtro => (
          <select
            key={filtro.clave}
            value={filtrosActivos[filtro.clave] || 'TODOS'}
            onChange={(e) => setFiltrosActivos({ ...filtrosActivos, [filtro.clave]: e.target.value })}
            style={{
              padding: '0.5rem',
              border: '1px solid #d1d5db',
              borderRadius: '0.375rem',
              fontSize: '0.875rem'
            }}
          >
            <option value="TODOS">Todos - {filtro.etiqueta}</option>
            {filtro.opciones.map(op => (
              <option key={op.valor} value={op.valor}>{op.etiqueta}</option>
            ))}
          </select>
        ))}
        {busqueda || Object.values(filtrosActivos).some(v => v && v !== 'TODOS') ? (
          <button
            onClick={() => { setBusqueda(''); setFiltrosActivos({}); }}
            style={{ ...botonEstilo('#6b7280'), padding: '0.5rem 1rem', fontSize: '0.875rem' }}
          >
            <Icon name="Trash2" size={16} style={{ marginRight: '0.25rem' }} /> Limpiar
          </button>
        ) : null}
      </div>

      {/* Contador de resultados */}
      <div style={{ marginBottom: '0.5rem', fontSize: '0.875rem', color: '#6b7280' }}>
        Mostrando {datosFiltrados.length} de {datos.length} registros
      </div>

      {/* Tabla */}
      <div style={{ overflowX: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ backgroundColor: '#f3f4f6' }}>
              {columnas.map(col => (
                <th key={col.clave} style={{ padding: '0.75rem', textAlign: 'left', borderBottom: '2px solid #e5e7eb', fontSize: '0.875rem', fontWeight: '600' }}>
                  {col.etiqueta}
                </th>
              ))}
              <th style={{ padding: '0.75rem', textAlign: 'left', borderBottom: '2px solid #e5e7eb', fontSize: '0.875rem', fontWeight: '600' }}>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {datosFiltrados.length === 0 ? (
              <tr>
                <td colSpan={columnas.length + 1} style={{ padding: '2rem', textAlign: 'center', color: '#6b7280' }}>
                  No se encontraron resultados
                </td>
              </tr>
            ) : (
              datosFiltrados.map((item, idx) => (
                <tr key={item.id || idx} style={{ borderBottom: '1px solid #e5e7eb' }}>
                  {columnas.map(col => (
                    <td key={col.clave} style={{ padding: '0.75rem', fontSize: '0.875rem' }}>
                      {item[col.clave] || '-'}
                    </td>
                  ))}
                  <td style={{ padding: '0.75rem' }}>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <button onClick={() => onEditar(item)} style={{ ...botonEstilo('#3b82f6'), padding: '0.25rem 0.5rem', fontSize: '0.75rem', display: 'flex', alignItems: 'center' }}><Icon name="Pencil" size={14} /></button>
                      <button onClick={() => onEliminar(item)} style={{ ...botonEstilo('#ef4444'), padding: '0.25rem 0.5rem', fontSize: '0.75rem', display: 'flex', alignItems: 'center' }}><Icon name="Trash2" size={14} /></button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

// Helper para estilos de botones
const botonEstilo = (color: string) => ({
  padding: '0.5rem 1rem',
  backgroundColor: color,
  color: 'white',
  border: 'none',
  borderRadius: '0.375rem',
  cursor: 'pointer',
  fontSize: '0.875rem',
  fontWeight: '500',
});

// Implementaciones similares para los demás catálogos (TiposAlimentoTab, TiposServicioTab, etc.)
// Por brevedad, usaré el mismo patrón para todos

const TiposAlimentoTab: React.FC = () => {
  const [tipos, setTipos] = useState<TipoAlimentoPorcino[]>([]);
  const [mostrarFormulario, setMostrarFormulario] = useState(false);
  const [editando, setEditando] = useState<TipoAlimentoPorcino | null>(null);
  const [formData, setFormData] = useState<Partial<TipoAlimentoPorcino>>({ categoria: 'BALANCEADO' });

  useEffect(() => {
    cargarTipos();
  }, []);

  const cargarTipos = async () => {
    try {
      const data = await catalogosService.listarTiposAlimento();
      setTipos(data);
    } catch (error) {
      console.error('Error al cargar tipos:', error);
    }
  };

  const handleGuardar = async () => {
    try {
      await catalogosService.guardarTipoAlimento(formData);
      await cargarTipos();
      setMostrarFormulario(false);
      setEditando(null);
      setFormData({ categoria: 'BALANCEADO' });
      alert('Tipo de alimento guardado correctamente');
    } catch (error) {
      console.error('Error al guardar:', error);
      alert('Error al guardar');
    }
  };

  const handleEliminar = async (id: number) => {
    if (!confirm('¿Está seguro de eliminar?')) return;
    try {
      await catalogosService.eliminarTipoAlimento(id);
      await cargarTipos();
    } catch (error) {
      console.error('Error al eliminar:', error);
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937' }}>Tipos de Alimento</h2>
        <button onClick={() => { setMostrarFormulario(true); setEditando(null); setFormData({ categoria: 'BALANCEADO' }); }} style={botonEstilo('#10b981')}>
          <Icon name="Plus" size={16} style={{ marginRight: '0.25rem' }} /> Agregar
        </button>
      </div>

      {mostrarFormulario && (
        <div style={{ marginBottom: '2rem', padding: '1.5rem', backgroundColor: '#f9fafb', borderRadius: '0.5rem' }}>
          <h3 style={{ marginBottom: '1rem' }}>{editando ? 'Editar' : 'Nuevo'} Tipo de Alimento</h3>
          <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))' }}>
            <CampoFormulario label="Nombre *" value={formData.nombre || ''} onChange={(val) => setFormData({ ...formData, nombre: val })} editando={true} tipo="text" />
            <CampoFormulario 
              label="Categoría *" 
              value={formData.categoria || 'BALANCEADO'} 
              onChange={(val) => setFormData({ ...formData, categoria: val as any })} 
              editando={true} 
              tipo="select" 
              opciones={[
                { valor: 'BALANCEADO', etiqueta: 'Balanceado' },
                { valor: 'GRANO_PROPIO', etiqueta: 'Grano Propio' },
                { valor: 'OTRO', etiqueta: 'Otro' },
                // NOTA: Las categorías RACION_* fueron eliminadas porque están solapadas con InsumoCompuesto
                // Las recetas (raciones) ahora se gestionan en InsumoCompuesto (tipo RACION) que se asocia a etapas
              ]} 
            />
            <CampoFormulario label="% Proteína" value={formData.porcentajeProteina?.toString() || ''} onChange={(val) => setFormData({ ...formData, porcentajeProteina: parseFloat(val) || undefined })} editando={true} tipo="number" />
            <CampoFormulario label="Precio (kg)" value={formData.precioKg?.toString() || ''} onChange={(val) => setFormData({ ...formData, precioKg: parseFloat(val) || undefined })} editando={true} tipo="number" />
            <CampoFormulario label="Unidad Medida" value={formData.unidadMedida || 'kg'} onChange={(val) => setFormData({ ...formData, unidadMedida: val })} editando={true} tipo="text" />
            <CampoFormulario label="Descripción" value={formData.descripcion || ''} onChange={(val) => setFormData({ ...formData, descripcion: val })} editando={true} tipo="text" />
          </div>
          <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
            <button onClick={handleGuardar} style={botonEstilo('#10b981')}>💾 Guardar</button>
            <button onClick={() => { setMostrarFormulario(false); setEditando(null); setFormData({ categoria: 'BALANCEADO' }); }} style={botonEstilo('#6b7280')}>✕ Cancelar</button>
          </div>
        </div>
      )}

      <TablaCatalogos
        datos={tipos}
        columnas={[
          { clave: 'nombre', etiqueta: 'Nombre' },
          { clave: 'categoria', etiqueta: 'Categoría' },
          { clave: 'porcentajeProteina', etiqueta: '% Proteína' },
          { clave: 'precioKg', etiqueta: 'Precio/kg' },
        ]}
        onEditar={(item) => { setEditando(item); setFormData(item); setMostrarFormulario(true); }}
        onEliminar={(item) => item.id && handleEliminar(item.id)}
      />
    </div>
  );
};

// Implementaciones simplificadas para los demás (mismo patrón)
const TiposServicioTab: React.FC = () => <CatalogoTab titulo="Tipos de Servicio" servicio={catalogosService.listarTiposServicio} guardar={catalogosService.guardarTipoServicio} eliminar={catalogosService.eliminarTipoServicio} campos={[
  { clave: 'nombre', etiqueta: 'Nombre', tipo: 'text' },
  { clave: 'tipo', etiqueta: 'Tipo', tipo: 'select', opciones: [
    { valor: 'MONTA_NATURAL_DIRECTA', etiqueta: 'Monta Natural Directa' },
    { valor: 'IA_POSCERVICAL', etiqueta: 'IA Poscervical' },
    { valor: 'IA_TRADICIONAL', etiqueta: 'IA Tradicional' },
    { valor: 'SERVICIO_REPETIDO', etiqueta: 'Servicio Repetido' },
  ]},
]} />;

const CausasMortalidadTab: React.FC = () => <CatalogoTab titulo="Causas de Mortalidad" servicio={catalogosService.listarCausasMortalidad} guardar={catalogosService.guardarCausaMortalidad} eliminar={catalogosService.eliminarCausaMortalidad} campos={[
  { clave: 'nombre', etiqueta: 'Nombre', tipo: 'text' },
  { clave: 'etapa', etiqueta: 'Etapa', tipo: 'select', opciones: [
    { valor: 'LACTANCIA', etiqueta: 'Lactancia' },
    { valor: 'RECRIA', etiqueta: 'Recría' },
    { valor: 'ENGORDE', etiqueta: 'Engorde' },
    { valor: 'GESTACION', etiqueta: 'Gestación' },
    { valor: 'GENERAL', etiqueta: 'General' },
  ]},
]} />;

const MotivosBajaTab: React.FC = () => <CatalogoTab titulo="Motivos de Baja" servicio={catalogosService.listarMotivosBaja} guardar={catalogosService.guardarMotivoBaja} eliminar={catalogosService.eliminarMotivoBaja} campos={[
  { clave: 'nombre', etiqueta: 'Nombre', tipo: 'text' },
  { clave: 'tipo', etiqueta: 'Tipo', tipo: 'select', opciones: [
    { valor: 'VENTA', etiqueta: 'Venta' },
    { valor: 'MUERTE', etiqueta: 'Muerte' },
    { valor: 'REEMPLAZO', etiqueta: 'Reemplazo' },
    { valor: 'PROBLEMAS_SANITARIOS', etiqueta: 'Problemas Sanitarios' },
    { valor: 'PROBLEMAS_REPRODUCTIVOS', etiqueta: 'Problemas Reproductivos' },
  ]},
]} />;

const EsquemasSanitariosTab: React.FC = () => {
  const [esquemas, setEsquemas] = useState<EsquemaSanitarioPorcino[]>([]);
  const [mostrarFormulario, setMostrarFormulario] = useState(false);
  const [formData, setFormData] = useState<Partial<EsquemaSanitarioPorcino>>({ tipo: 'VACUNA' });

  useEffect(() => {
    cargarEsquemas();
  }, []);

  const cargarEsquemas = async () => {
    try {
      const data = await catalogosService.listarEsquemasSanitarios();
      setEsquemas(data);
    } catch (error) {
      console.error('Error:', error);
    }
  };

  const handleGuardar = async () => {
    try {
      await catalogosService.guardarEsquemaSanitario(formData);
      await cargarEsquemas();
      setMostrarFormulario(false);
      setFormData({ tipo: 'VACUNA' });
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>Esquemas Sanitarios</h2>
        <button onClick={() => setMostrarFormulario(true)} style={botonEstilo('#10b981')}>➕ Agregar</button>
      </div>
      {mostrarFormulario && (
        <div style={{ marginBottom: '2rem', padding: '1.5rem', backgroundColor: '#f9fafb', borderRadius: '0.5rem' }}>
          <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))' }}>
            <CampoFormulario label="Nombre *" value={formData.nombre || ''} onChange={(val) => setFormData({ ...formData, nombre: val })} editando={true} tipo="text" />
            <CampoFormulario label="Tipo *" value={formData.tipo || 'VACUNA'} onChange={(val) => setFormData({ ...formData, tipo: val as any })} editando={true} tipo="select" opciones={[
              { valor: 'VACUNA', etiqueta: 'Vacuna' },
              { valor: 'DESPARASITACION', etiqueta: 'Desparasitación' },
              { valor: 'ANTIBIOTICO', etiqueta: 'Antibiótico' },
              { valor: 'OTRO', etiqueta: 'Otro' },
            ]} />
            <CampoFormulario label="Producto" value={formData.producto || ''} onChange={(val) => setFormData({ ...formData, producto: val })} editando={true} tipo="text" />
            <CampoFormulario label="Dosis" value={formData.dosis || ''} onChange={(val) => setFormData({ ...formData, dosis: val })} editando={true} tipo="text" />
            <CampoFormulario label="Frecuencia (días)" value={formData.frecuenciaDias?.toString() || ''} onChange={(val) => setFormData({ ...formData, frecuenciaDias: parseInt(val) || undefined })} editando={true} tipo="number" />
            <CampoFormulario label="Aplicable a" value={formData.aplicableA || ''} onChange={(val) => setFormData({ ...formData, aplicableA: val })} editando={true} tipo="text" />
          </div>
          <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
            <button onClick={handleGuardar} style={botonEstilo('#10b981')}>💾 Guardar</button>
            <button onClick={() => { setMostrarFormulario(false); setFormData({ tipo: 'VACUNA' }); }} style={botonEstilo('#6b7280')}>✕ Cancelar</button>
          </div>
        </div>
      )}
      <TablaCatalogos datos={esquemas} columnas={[
        { clave: 'nombre', etiqueta: 'Nombre' },
        { clave: 'tipo', etiqueta: 'Tipo' },
        { clave: 'producto', etiqueta: 'Producto' },
        { clave: 'frecuenciaDias', etiqueta: 'Frecuencia (días)' },
      ]} onEditar={() => {}} onEliminar={(item) => item.id && catalogosService.eliminarEsquemaSanitario(item.id)} />
    </div>
  );
};

// Componente genérico para catálogos simples (simplificado)
const CatalogoTab: React.FC<{
  titulo: string;
  servicio: () => Promise<any[]>;
  guardar: (data: any) => Promise<any>;
  eliminar: (id: number) => Promise<void>;
  campos: any[];
}> = ({ titulo, servicio, guardar, eliminar, campos }) => {
  const [datos, setDatos] = useState<any[]>([]);
  const [mostrarFormulario, setMostrarFormulario] = useState(false);
  const [formData, setFormData] = useState<any>({});

  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    try {
      const data = await servicio();
      setDatos(data);
    } catch (error) {
      console.error('Error:', error);
    }
  };

  const handleGuardar = async () => {
    try {
      await guardar(formData);
      await cargarDatos();
      setMostrarFormulario(false);
      setFormData({});
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>{titulo}</h2>
        <button onClick={() => setMostrarFormulario(true)} style={botonEstilo('#10b981')}>➕ Agregar</button>
      </div>
      {mostrarFormulario && (
        <div style={{ marginBottom: '2rem', padding: '1.5rem', backgroundColor: '#f9fafb', borderRadius: '0.5rem' }}>
          <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))' }}>
            {campos.map(campo => (
              <CampoFormulario
                key={campo.clave}
                label={campo.etiqueta}
                value={formData[campo.clave] || ''}
                onChange={(val) => setFormData({ ...formData, [campo.clave]: val })}
                editando={true}
                tipo={campo.tipo}
                opciones={campo.opciones}
              />
            ))}
          </div>
          <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
            <button onClick={handleGuardar} style={botonEstilo('#10b981')}>💾 Guardar</button>
            <button onClick={() => { setMostrarFormulario(false); setFormData({}); }} style={botonEstilo('#6b7280')}>✕ Cancelar</button>
          </div>
        </div>
      )}
      <TablaCatalogos
        datos={datos}
        columnas={campos.map(c => ({ clave: c.clave, etiqueta: c.etiqueta }))}
        onEditar={(item) => { setFormData(item); setMostrarFormulario(true); }}
        onEliminar={(item) => item.id && eliminar(item.id)}
      />
    </div>
  );
};

const TiposPartoTab: React.FC = () => {
  const [tipos, setTipos] = useState<TipoParto[]>([]);
  const [mostrarFormulario, setMostrarFormulario] = useState(false);
  const [formData, setFormData] = useState<Partial<TipoParto>>({ requiereIntervencion: false });

  useEffect(() => {
    cargarTipos();
  }, []);

  const cargarTipos = async () => {
    try {
      const data = await catalogosService.listarTiposParto();
      setTipos(data);
    } catch (error) {
      console.error('Error:', error);
    }
  };

  const handleGuardar = async () => {
    try {
      await catalogosService.guardarTipoParto(formData);
      await cargarTipos();
      setMostrarFormulario(false);
      setFormData({ requiereIntervencion: false });
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>Tipos de Parto</h2>
        <button onClick={() => setMostrarFormulario(true)} style={botonEstilo('#10b981')}>➕ Agregar</button>
      </div>
      {mostrarFormulario && (
        <div style={{ marginBottom: '2rem', padding: '1.5rem', backgroundColor: '#f9fafb', borderRadius: '0.5rem' }}>
          <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))' }}>
            <CampoFormulario label="Nombre *" value={formData.nombre || ''} onChange={(val) => setFormData({ ...formData, nombre: val })} editando={true} tipo="text" />
            <CampoFormulario label="Descripción" value={formData.descripcion || ''} onChange={(val) => setFormData({ ...formData, descripcion: val })} editando={true} tipo="text" />
            <CampoFormulario 
              label="Requiere Intervención" 
              value={formData.requiereIntervencion ? 'true' : 'false'} 
              onChange={(val) => setFormData({ ...formData, requiereIntervencion: val === 'true' })} 
              editando={true} 
              tipo="select" 
              opciones={[
                { valor: 'false', etiqueta: 'No' },
                { valor: 'true', etiqueta: 'Sí' },
              ]} 
            />
          </div>
          <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
            <button onClick={handleGuardar} style={botonEstilo('#10b981')}>💾 Guardar</button>
            <button onClick={() => { setMostrarFormulario(false); setFormData({ requiereIntervencion: false }); }} style={botonEstilo('#6b7280')}>✕ Cancelar</button>
          </div>
        </div>
      )}
      <TablaCatalogos datos={tipos} columnas={[
        { clave: 'nombre', etiqueta: 'Nombre' },
        { clave: 'descripcion', etiqueta: 'Descripción' },
        { clave: 'requiereIntervencion', etiqueta: 'Requiere Intervención' },
      ]} onEditar={(item) => { setFormData(item); setMostrarFormulario(true); }} onEliminar={(item) => item.id && catalogosService.eliminarTipoParto(item.id)} />
    </div>
  );
};

const UbicacionesInternasTab: React.FC = () => {
  const [ubicaciones, setUbicaciones] = useState<UbicacionInterna[]>([]);
  const [galpones, setGalpones] = useState<UbicacionInterna[]>([]);
  const [salas, setSalas] = useState<UbicacionInterna[]>([]);
  const [mostrarFormulario, setMostrarFormulario] = useState(false);
  const [formData, setFormData] = useState<Partial<UbicacionInterna>>({ nivel: 'GALPON', tipoUbicacion: 'GENERAL' });

  useEffect(() => {
    cargarUbicaciones();
  }, []);

  const cargarUbicaciones = async () => {
    try {
      const data = await catalogosService.listarUbicacionesInternas();
      // Filtrar solo ubicaciones activas
      const activas = data.filter(u => u.activo !== false);
      setUbicaciones(activas);
      setGalpones(activas.filter(u => u.nivel === 'GALPON'));
      setSalas(activas.filter(u => u.nivel === 'SALA'));
    } catch (error) {
      console.error('Error:', error);
    }
  };

  const handleGuardar = async () => {
    try {
      // Validar campos obligatorios
      if (!formData.nombre || formData.nombre.trim() === '') {
        alert('El nombre es obligatorio');
        return;
      }
      
      if (formData.nivel !== 'GALPON' && !formData.ubicacionPadreId) {
        alert('Debe seleccionar una ubicación padre');
        return;
      }
      
      // Preparar datos para enviar: si hay ubicacionPadreId, crear objeto padre
      const datosParaEnviar: any = {
        nombre: formData.nombre,
        nivel: formData.nivel,
        tipoUbicacion: formData.tipoUbicacion || 'GENERAL',
        codigo: formData.codigo || null,
        capacidadMaxima: formData.capacidadMaxima || null,
        descripcion: formData.descripcion || null,
      };
      
      // Si es edición, incluir el ID
      if (formData.id) {
        datosParaEnviar.id = formData.id;
      }
      
      // Si hay ubicacionPadreId, crear objeto padre
      if (datosParaEnviar.ubicacionPadreId || formData.ubicacionPadreId) {
        datosParaEnviar.ubicacionPadre = { 
          id: datosParaEnviar.ubicacionPadreId || formData.ubicacionPadreId 
        };
      }
      
      await catalogosService.guardarUbicacionInterna(datosParaEnviar);
      // Recargar ubicaciones para actualizar el estado
      await cargarUbicaciones();
      setMostrarFormulario(false);
      // Limpiar el formulario completamente
      setFormData({ nivel: 'GALPON', tipoUbicacion: 'GENERAL', ubicacionPadreId: undefined });
    } catch (error: any) {
      console.error('Error:', error);
      const mensaje = error?.response?.data?.message || error?.message || 'Error al guardar la ubicación';
      alert(`Error: ${mensaje}`);
    }
  };

  const obtenerUbicacionesHijas = (padreId: number) => {
    return ubicaciones.filter(u => {
      // Solo mostrar ubicaciones activas
      if (u.activo === false) return false;
      
      // Manejar tanto si viene como ID directo o como objeto
      if (u.ubicacionPadreId) {
        return u.ubicacionPadreId === padreId;
      }
      // Si viene como objeto anidado
      if (u.ubicacionesHijas && Array.isArray(u.ubicacionesHijas)) {
        return u.ubicacionesHijas.some(h => h.id === padreId);
      }
      return false;
    });
  };

  const renderizarJerarquia = (padre: UbicacionInterna, nivel: number = 0) => {
    const hijas = obtenerUbicacionesHijas(padre.id!);
    return (
      <div key={padre.id} style={{ marginLeft: `${nivel * 2}rem`, marginBottom: '0.5rem' }}>
        <div style={{ 
          padding: '0.75rem', 
          backgroundColor: nivel === 0 ? '#dbeafe' : nivel === 1 ? '#e0e7ff' : '#f3e8ff',
          borderRadius: '0.375rem',
          border: '1px solid #d1d5db',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          <div>
            <strong>{padre.nombre}</strong> 
            {padre.codigo && <span style={{ marginLeft: '0.5rem', color: '#6b7280' }}>({padre.codigo})</span>}
            <div style={{ fontSize: '0.875rem', color: '#6b7280', marginTop: '0.25rem' }}>
              {padre.nivel} {padre.tipoUbicacion && `- ${padre.tipoUbicacion}`}
              {padre.capacidadMaxima && ` - Capacidad: ${padre.capacidadMaxima}`}
            </div>
          </div>
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button 
              onClick={() => { 
                setFormData({ 
                  ...formData, 
                  nivel: padre.nivel === 'GALPON' ? 'SALA' : 'CORRAL',
                  ubicacionPadreId: padre.id 
                }); 
                setMostrarFormulario(true); 
              }} 
              style={botonEstilo('#3b82f6')}
            >
              <Icon name="Plus" size={16} style={{ marginRight: '0.25rem' }} /> Agregar {padre.nivel === 'GALPON' ? 'Sala' : 'Corral'}
            </button>
            {padre.id && (
              <button 
                onClick={async () => {
                  if (confirm(`¿Está seguro de que desea eliminar "${padre.nombre}"?`)) {
                    try {
                      await catalogosService.eliminarUbicacionInterna(padre.id!);
                      // Recargar ubicaciones para actualizar el estado
                      await cargarUbicaciones();
                      // Si estaba editando esta ubicación, cerrar el formulario
                      if (formData.id === padre.id) {
                        setMostrarFormulario(false);
                        setFormData({ nivel: 'GALPON', tipoUbicacion: 'GENERAL' });
                      }
                    } catch (error: any) {
                      const mensaje = error?.response?.data?.message || error?.message || 'Error al eliminar la ubicación';
                      alert(`Error: ${mensaje}`);
                    }
                  }
                }} 
                style={botonEstilo('#ef4444')}
              >
                🗑️
              </button>
            )}
          </div>
        </div>
        {hijas.map(hija => renderizarJerarquia(hija, nivel + 1))}
      </div>
    );
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>Ubicaciones Internas</h2>
        <button onClick={() => { setFormData({ nivel: 'GALPON', tipoUbicacion: 'GENERAL' }); setMostrarFormulario(true); }} style={botonEstilo('#10b981')}>➕ Agregar Galpón</button>
      </div>
      {mostrarFormulario && (
        <div style={{ marginBottom: '2rem', padding: '1.5rem', backgroundColor: '#f9fafb', borderRadius: '0.5rem' }}>
          <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))' }}>
            <CampoFormulario label="Nombre *" value={formData.nombre || ''} onChange={(val) => setFormData({ ...formData, nombre: val })} editando={true} tipo="text" />
            <CampoFormulario label="Código" value={formData.codigo || ''} onChange={(val) => setFormData({ ...formData, codigo: val })} editando={true} tipo="text" />
            <CampoFormulario 
              label="Nivel *" 
              value={formData.nivel || 'GALPON'} 
              onChange={(val) => setFormData({ ...formData, nivel: val as any })} 
              editando={true} 
              tipo="select" 
              opciones={[
                { valor: 'GALPON', etiqueta: 'Galpón' },
                { valor: 'SALA', etiqueta: 'Sala' },
                { valor: 'CORRAL', etiqueta: 'Corral' },
              ]} 
            />
            {formData.nivel !== 'GALPON' && (
              <CampoFormulario 
                label="Ubicación Padre *" 
                value={formData.ubicacionPadreId?.toString() || ''} 
                onChange={(val) => setFormData({ ...formData, ubicacionPadreId: parseInt(val) || undefined })} 
                editando={true} 
                tipo="select" 
                opciones={
                  formData.nivel === 'SALA' 
                    ? galpones
                        .filter(g => g.activo !== false)
                        .map(g => ({ valor: g.id!.toString(), etiqueta: `${g.nombre}${g.codigo ? ` (${g.codigo})` : ''}` }))
                    : salas
                        .filter(s => s.activo !== false)
                        .map(s => ({ valor: s.id!.toString(), etiqueta: `${s.nombre}${s.codigo ? ` (${s.codigo})` : ''} - ${s.tipoUbicacion || 'GENERAL'}` }))
                }
              />
            )}
            <CampoFormulario 
              label="Tipo de Ubicación" 
              value={formData.tipoUbicacion || 'GENERAL'} 
              onChange={(val) => setFormData({ ...formData, tipoUbicacion: val as any })} 
              editando={true} 
              tipo="select" 
              opciones={[
                { valor: 'GENERAL', etiqueta: 'General' },
                { valor: 'MATERNIDAD', etiqueta: 'Maternidad' },
                { valor: 'GESTACION', etiqueta: 'Gestación' },
                { valor: 'RECRIA', etiqueta: 'Recría' },
                { valor: 'ENGORDE', etiqueta: 'Engorde' },
                { valor: 'AISLAMIENTO', etiqueta: 'Aislamiento' },
              ]} 
            />
            <CampoFormulario label="Capacidad Máxima" value={formData.capacidadMaxima?.toString() || ''} onChange={(val) => setFormData({ ...formData, capacidadMaxima: parseInt(val) || undefined })} editando={true} tipo="number" />
            <CampoFormulario label="Descripción" value={formData.descripcion || ''} onChange={(val) => setFormData({ ...formData, descripcion: val })} editando={true} tipo="text" />
          </div>
          <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
            <button onClick={handleGuardar} style={botonEstilo('#10b981')}>💾 Guardar</button>
            <button onClick={() => { setMostrarFormulario(false); setFormData({ nivel: 'GALPON', tipoUbicacion: 'GENERAL' }); }} style={botonEstilo('#6b7280')}>✕ Cancelar</button>
          </div>
        </div>
      )}
      <div style={{ marginTop: '1.5rem' }}>
        <h3 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '1rem' }}>Estructura Jerárquica</h3>
        {galpones.length === 0 ? (
          <p style={{ color: '#6b7280', fontStyle: 'italic' }}>No hay ubicaciones registradas. Agrega un galpón para comenzar.</p>
        ) : (
          galpones.map(galpon => renderizarJerarquia(galpon))
        )}
      </div>
    </div>
  );
};

const ConfiguracionesGeneralesTab: React.FC<{ mostrarMensajeExito: (texto: string) => void; mostrarMensajeError: (texto: string) => void }> = ({ mostrarMensajeExito, mostrarMensajeError }) => {
  const [configuraciones, setConfiguraciones] = useState<ConfiguracionPorcino[]>([]);
  const [exigirKgRealCalendario, setExigirKgRealCalendario] = useState(false);
  const [guardandoPoliticaCalendario, setGuardandoPoliticaCalendario] = useState(false);
  const [loading, setLoading] = useState(true);
  const [mostrarFormulario, setMostrarFormulario] = useState(false);
  const [formData, setFormData] = useState<Partial<ConfiguracionPorcino>>({
    tipo: 'TEXTO',
    categoria: 'GENERAL'
  });

  useEffect(() => {
    cargarConfiguraciones();
  }, []);

  const cargarConfiguraciones = async (opciones?: { sinPantallaCarga?: boolean }) => {
    if (!opciones?.sinPantallaCarga) {
      setLoading(true);
    }
    try {
      const data = await configuracionService.listar();
      setConfiguraciones(data);
      const politica = data.find((c: ConfiguracionPorcino) => c.clave === CLAVE_CONFIRMACION_CALENDARIO_SOLO_CON_RACION_REAL);
      const activa =
        politica != null &&
        (String(politica.valor).trim().toLowerCase() === 'true' || String(politica.valor).trim() === '1');
      setExigirKgRealCalendario(activa);
    } catch (error) {
      console.error('Error al cargar configuraciones:', error);
      mostrarMensajeError('Error al cargar configuraciones');
    } finally {
      if (!opciones?.sinPantallaCarga) {
        setLoading(false);
      }
    }
  };

  const handleCambiarPoliticaCalendario = async (activo: boolean) => {
    setGuardandoPoliticaCalendario(true);
    try {
      await configuracionService.guardarPoliticaConfirmacionCalendarioSoloConKgReal(activo);
      setExigirKgRealCalendario(activo);
      await cargarConfiguraciones({ sinPantallaCarga: true });
      mostrarMensajeExito('Política del calendario de alimentación actualizada');
    } catch (error) {
      console.error('Error al guardar política del calendario:', error);
      mostrarMensajeError('No se pudo guardar la política del calendario');
    } finally {
      setGuardandoPoliticaCalendario(false);
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

  const handleEliminar = async (id: number) => {
    if (!confirm('¿Está seguro de que desea eliminar esta configuración?')) {
      return;
    }

    try {
      // TODO: Implementar método eliminar en el servicio si es necesario
      // Por ahora, solo mostramos un mensaje
      mostrarMensajeError('Función de eliminación pendiente de implementar');
    } catch (error) {
      console.error('Error al eliminar configuración:', error);
      mostrarMensajeError('Error al eliminar la configuración');
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

  const configuracionesSinPoliticaCalendario = configuraciones.filter(
    (c) => c.clave !== CLAVE_CONFIRMACION_CALENDARIO_SOLO_CON_RACION_REAL
  );

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937' }}>
          ⚙️ Configuraciones Generales
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

      <div
        style={{
          marginBottom: '1.5rem',
          padding: '1.25rem',
          borderRadius: '0.5rem',
          border: '1px solid #e5e7eb',
          backgroundColor: '#fffbeb',
        }}
      >
        <h3 style={{ margin: '0 0 0.5rem', fontSize: '1.1rem', fontWeight: 700, color: '#92400e' }}>
          <Icon name="Calendar" size={20} style={{ marginRight: '0.35rem', verticalAlign: 'text-bottom' }} />
          Calendario de alimentación
        </h3>
        <p style={{ margin: '0 0 0.75rem', fontSize: '0.875rem', color: '#78350f' }}>
          Controla si se puede confirmar un día con consumos aún en modo estimado (sin kg real de ración cargado).
        </p>
        <label
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            cursor: guardandoPoliticaCalendario ? 'wait' : 'pointer',
            fontWeight: 500,
            color: '#1f2937',
          }}
        >
          <input
            type="checkbox"
            checked={exigirKgRealCalendario}
            disabled={guardandoPoliticaCalendario}
            onChange={(e) => void handleCambiarPoliticaCalendario(e.target.checked)}
          />
          Exigir kg real en todos los consumos antes de confirmar el día
        </label>
        <p style={{ margin: '0.5rem 0 0', fontSize: '0.75rem', color: '#78716c' }}>
          Clave interna:{' '}
          <code style={{ background: '#fef3c7', padding: '0.1rem 0.25rem' }}>
            {CLAVE_CONFIRMACION_CALENDARIO_SOLO_CON_RACION_REAL}
          </code>
        </p>
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
                placeholder="Ej.: DIAS_CACHORRA, CONFIRMAR_CALENDARIO_SOLO_CON_REAL (true/false). No usar claves de Parámetros Productivos."
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
              <input
                type="text"
                value={formData.categoria || 'GENERAL'}
                onChange={(e) => setFormData({ ...formData, categoria: e.target.value })}
                placeholder="GENERAL, ETAPAS, REPRODUCCION, etc."
                style={{
                  width: '100%',
                  padding: '0.5rem',
                  border: '1px solid #d1d5db',
                  borderRadius: '0.375rem'
                }}
              />
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

      {configuracionesSinPoliticaCalendario.length === 0 ? (
        <div style={{
          padding: '3rem',
          textAlign: 'center',
          color: '#6b7280',
          border: '2px dashed #d1d5db',
          borderRadius: '0.5rem'
        }}>
          <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>⚙️</div>
          <p style={{ fontSize: '1.125rem', marginBottom: '0.5rem' }}>No hay otras configuraciones manuales</p>
          <p style={{ fontSize: '0.875rem' }}>La política del calendario se gestiona arriba. Podés agregar más claves con «Nueva configuración».</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gap: '1rem', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))' }}>
          {configuracionesSinPoliticaCalendario.map(config => (
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

export default ConfiguracionesScreen;

// Estilos CSS para animaciones
const estilosGlobales = `
  @keyframes slideIn {
    from {
      transform: translateX(100%);
      opacity: 0;
    }
    to {
      transform: translateX(0);
      opacity: 1;
    }
  }
`;

// Inyectar estilos si no existen
if (typeof document !== 'undefined' && !document.getElementById('configuraciones-estilos')) {
  const style = document.createElement('style');
  style.id = 'configuraciones-estilos';
  style.textContent = estilosGlobales;
  document.head.appendChild(style);
}
