import React, { useState, useEffect } from 'react';
import HistorialCosechasModal from './HistorialCosechasModal';
import api from '../services/api';
import humidityService from '../services/humidityService';

interface Cosecha {
  id?: number;
  lote_id: number;
  cultivo_id: number;
  superficie_ha: number;
  fecha_siembra: string;
  densidad_siembra: number;
  variedad_semilla: string;
  fertilizante_nitrogeno: number;
  fertilizante_fosforo: number;
  fertilizante_potasio: number;
  otros_insumos: string;
  fecha_cosecha: string;
  cantidad_cosechada: number;
  unidad_cosecha: 'kg' | 'tn' | 'qq';
  rinde_real: number;
  rinde_esperado: number;
  diferencia_rinde: number;
  porcentaje_cumplimiento: number;
  clima_favorable: boolean;
  plagas_enfermedades: boolean;
  riego_suficiente: boolean;
  observaciones: string;
}

interface Lote {
  id: number;
  nombre: string;
  superficie: number;
  cultivo: string;
  campo_id: number;
  fechaSiembra?: string;
  cultivoActual?: string;
  campo?: {
    id: number;
    nombre: string;
    ubicacion: string;
  };
}

interface Cultivo {
  id: number;
  nombre: string;
  variedad: string;
  rendimiento_esperado: number;
  unidad_rendimiento: string;
}



// Función para mapear unidad_rendimiento del cultivo a unidad_cosecha
const mapearUnidadRendimientoACosecha = (unidadRendimiento: string): 'kg' | 'tn' | 'qq' => {
  if (!unidadRendimiento) return 'kg';
  
  const unidad = unidadRendimiento.toLowerCase();
  
  // Mapear diferentes formatos de unidad_rendimiento a unidad_cosecha
  if (unidad.includes('tn') || unidad.includes('tonelada')) {
    return 'tn';
  } else if (unidad.includes('qq') || unidad.includes('quintal')) {
    return 'qq';
  } else if (unidad.includes('kg') || unidad.includes('kilogramo')) {
    return 'kg';
  }
  
  // Por defecto, si no se puede determinar, usar kg
  return 'kg';
};

const CosechasManagement: React.FC = () => {
  const [cosechas, setCosechas] = useState<Cosecha[]>([]);
  const [lotes, setLotes] = useState<Lote[]>([]);
  const [cultivos, setCultivos] = useState<Cultivo[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(false);
  const [showHistorialModal, setShowHistorialModal] = useState(false);
  const [selectedLoteForHistorial, setSelectedLoteForHistorial] = useState<{id: number, nombre: string} | null>(null);
  const [showForcedReleaseModal, setShowForcedReleaseModal] = useState(false);
  const [selectedLoteForForcedRelease, setSelectedLoteForForcedRelease] = useState<{id: number, nombre: string} | null>(null);
  const [forcedReleaseJustification, setForcedReleaseJustification] = useState('');


  // Función para calcular rendimiento real en la unidad del cultivo
  const calcularRendimientoReal = (cantidad: number, unidadCantidad: string, superficie: number, unidadCultivo: string): number => {
    console.log('🔍 calcularRendimientoReal:', { cantidad, unidadCantidad, superficie, unidadCultivo });
    
    if (superficie <= 0) {
      console.log('⚠️ Superficie <= 0, retornando 0');
      return 0;
    }
    
    // Convertir cantidad a la unidad del cultivo
    let cantidadEnUnidadCultivo = cantidad;
    
    // Convertir cantidad cosechada a kg primero
    let cantidadEnKg = cantidad;
    if (unidadCantidad === 'tn') {
      cantidadEnKg = cantidad * 1000; // tn a kg
    } else if (unidadCantidad === 'qq') {
      cantidadEnKg = cantidad * 46; // qq a kg
    }
    
    // Convertir de kg a la unidad del cultivo
    if (unidadCultivo.toLowerCase().includes('tn') || unidadCultivo.toLowerCase().includes('tonelada')) {
      cantidadEnUnidadCultivo = cantidadEnKg / 1000; // kg a tn
    } else if (unidadCultivo.toLowerCase().includes('qq') || unidadCultivo.toLowerCase().includes('quintal')) {
      cantidadEnUnidadCultivo = cantidadEnKg / 46; // kg a qq
    } else {
      // Si es kg/ha, mantener en kg
      cantidadEnUnidadCultivo = cantidadEnKg;
    }
    
    const resultado = cantidadEnUnidadCultivo / superficie;
    console.log('📊 Resultado cálculo:', { cantidadEnKg, cantidadEnUnidadCultivo, superficie, resultado });
    
    return resultado;
  };



  // Función para liberar lote para nueva siembra
  const liberarLoteParaNuevaSiembra = async (loteId: number, forzado: boolean = false, justificacion: string = '') => {
    try {
      const url = forzado 
        ? `/historial-cosechas/lote/${loteId}/liberar-forzado`
        : `/historial-cosechas/lote/${loteId}/liberar`;
      
      const response = forzado
        ? await api.put(url, { justificacion })
        : await api.put(url);
        
      alert(response.data);
    } catch (error: any) {
      console.error('Error liberando lote:', error);
      if (error.response?.data) {
        const errorMessage = error.response.data;
        
        // Si es error de período mínimo, mostrar opción de liberación forzada
        if (errorMessage.includes('muy recientes') || errorMessage.includes('menos de')) {
          const lote = lotes.find(l => l.id === loteId);
          if (lote) {
            setSelectedLoteForForcedRelease({ id: loteId, nombre: lote.nombre });
            setShowForcedReleaseModal(true);
            return;
          }
        }
        
        alert(`Error: ${errorMessage}`);
      } else {
        alert('Error al liberar el lote. Verifica la conexión con el servidor.');
      }
    }
  };
  const [formData, setFormData] = useState<Cosecha>({
    lote_id: 0,
    cultivo_id: 0,
    superficie_ha: 0,
    fecha_siembra: '',
    densidad_siembra: 0,
    variedad_semilla: '',
    fertilizante_nitrogeno: 0,
    fertilizante_fosforo: 0,
    fertilizante_potasio: 0,
    otros_insumos: '',
    fecha_cosecha: new Date().toISOString().split('T')[0], // Fecha de hoy por defecto
    cantidad_cosechada: 0,
    unidad_cosecha: 'kg',
    rinde_real: 0,
    rinde_esperado: 0,
    diferencia_rinde: 0,
    porcentaje_cumplimiento: 0,
    clima_favorable: true,
    plagas_enfermedades: false,
    riego_suficiente: true,
    observaciones: ''
  });

  // Cargar datos desde la API real
  const loadData = async () => {
    setLoading(true);
    try {
      // Obtener token del localStorage
      const token = localStorage.getItem('token');
      if (!token) {
        console.error('No hay token de autenticación');
        alert('No hay token de autenticación. Por favor, inicia sesión nuevamente.');
        return;
      }

      // Cargar lotes
      const lotesResponse = await api.get('/api/v1/lotes');
      const lotesData = lotesResponse.data;
        const lotesMapeados: Lote[] = lotesData
          .filter((lote: any) => {
            // Solo incluir lotes que tienen cultivos sembrados (no disponibles)
            return lote.cultivoActual && lote.cultivoActual.trim() !== '' && 
                   lote.estado && lote.estado.toLowerCase() !== 'disponible';
          })
          .map((lote: any) => ({
          id: lote.id,
          nombre: lote.nombre,
          superficie: lote.areaHectareas || 0,
          cultivo: lote.cultivoActual || '',
            campo_id: lote.campoId || 0,
            fechaSiembra: lote.fechaSiembra || '',
            cultivoActual: lote.cultivoActual || '',
            campo: lote.campo ? {
              id: lote.campo.id,
              nombre: lote.campo.nombre,
              ubicacion: lote.campo.ubicacion
            } : undefined
        }));
        setLotes(lotesMapeados);

      // Cargar cultivos
      const cultivosResponse = await api.get('/api/v1/cultivos');
      const cultivosData = cultivosResponse.data;
        const cultivosMapeados: Cultivo[] = cultivosData.map((cultivo: any) => ({
          id: cultivo.id,
          nombre: cultivo.nombre,
          variedad: cultivo.variedad,
          rendimiento_esperado: cultivo.rendimientoEsperado || 0,
          unidad_rendimiento: cultivo.unidadRendimiento || 'kg/ha'
        }));
        setCultivos(cultivosMapeados);

      // Cargar cosechas usando el servicio api que tiene el interceptor configurado
      const cosechasResponse = await api.get('/api/v1/cosechas');
      const cosechasData = cosechasResponse.data;
        console.log('Cosechas recibidas del backend:', cosechasData);
        
        const cosechasMapeadas: Cosecha[] = cosechasData.map((cosecha: any) => ({
          id: cosecha.id,
          lote_id: cosecha.loteId || 0,
          cultivo_id: cosecha.cultivoId || 0,
          superficie_ha: 0, // Se calculará desde el lote
          fecha_siembra: '', // Se obtendrá del lote
          densidad_siembra: 0,
          variedad_semilla: '', // Se obtendrá del cultivo
          fertilizante_nitrogeno: 0,
          fertilizante_fosforo: 0,
          fertilizante_potasio: 0,
          otros_insumos: '',
          fecha_cosecha: cosecha.fecha || '',
          cantidad_cosechada: cosecha.cantidadToneladas ? cosecha.cantidadToneladas * 1000 : 0, // Convertir de tn a kg
          unidad_cosecha: 'kg',
          rinde_real: 0, // Se calculará
          rinde_esperado: 0, // Se obtendrá del cultivo
          diferencia_rinde: 0,
          porcentaje_cumplimiento: 0,
          clima_favorable: true,
          plagas_enfermedades: cosecha.plagasEnfermedades || false,
          riego_suficiente: cosecha.riegoSuficiente || true,
          observaciones: cosecha.observaciones || ''
        }));
        
        // Enriquecer los datos con información de lotes y cultivos
        const cosechasEnriquecidas = cosechasMapeadas.map(cosecha => {
          const lote = lotesMapeados.find(l => l.id === cosecha.lote_id);
          const cultivo = cultivosMapeados.find(c => c.id === cosecha.cultivo_id);
          
          const superficie = lote?.superficie || 0;
          const rindeEsperado = cultivo?.rendimiento_esperado || 0;
          
          // Calcular rendimiento real en la unidad del cultivo
          const rindeReal = calcularRendimientoReal(
            cosecha.cantidad_cosechada, 
            cosecha.unidad_cosecha, 
            superficie, 
            cultivo?.unidad_rendimiento || 'kg/ha'
          );
          
          return {
            ...cosecha,
            superficie_ha: superficie,
            fecha_siembra: lote?.fechaSiembra || '',
            variedad_semilla: cultivo?.variedad || '',
            rinde_esperado: rindeEsperado,
            rinde_real: rindeReal,
            diferencia_rinde: rindeReal - rindeEsperado,
            porcentaje_cumplimiento: rindeEsperado > 0 ? (rindeReal / rindeEsperado) * 100 : 0
          };
        });
        
        console.log('Cosechas enriquecidas:', cosechasEnriquecidas);
        setCosechas(cosechasEnriquecidas);

    } catch (error) {
      console.error('Error cargando datos:', error);
      alert('Error al cargar los datos. Verifica la conexión con el servidor.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      // Obtener token del localStorage
      const token = localStorage.getItem('token');
      if (!token) {
        console.error('No hay token de autenticación');
        alert('No hay token de autenticación. Por favor, inicia sesión nuevamente.');
        return;
      }

      // Función para convertir cualquier unidad a toneladas
      const convertirAToneladas = (cantidad: number, unidad: 'kg' | 'tn' | 'qq'): number => {
        switch (unidad) {
          case 'kg':
            return cantidad / 1000; // kg a toneladas
          case 'tn':
            return cantidad; // ya está en toneladas
          case 'qq':
            return (cantidad * 46) / 1000; // quintales a kg, luego a toneladas (1 qq = 46 kg)
          default:
            return cantidad / 1000; // por defecto asumir kg
        }
      };

      // Mapear datos del frontend al formato esperado por el backend
      const cosechaData = {
        cultivoId: formData.cultivo_id,
        loteId: formData.lote_id,
        fecha: formData.fecha_cosecha,
        cantidadToneladas: convertirAToneladas(formData.cantidad_cosechada, formData.unidad_cosecha),
        precioPorTonelada: null, // Valor opcional
        costoTotal: null, // Valor opcional
        observaciones: formData.observaciones
      };

      console.log('Datos del formulario:', formData);
      console.log('Datos a enviar al backend:', cosechaData);

      // Validar campos requeridos
      if (!formData.cultivo_id || formData.cultivo_id === 0) {
        alert('Por favor selecciona un cultivo');
        setLoading(false);
        return;
      }

      if (!formData.lote_id || formData.lote_id === 0) {
        alert('Por favor selecciona un lote');
        setLoading(false);
        return;
      }

      if (!formData.fecha_cosecha) {
        alert('Por favor ingresa la fecha de cosecha');
        setLoading(false);
        return;
      }

      if (!formData.cantidad_cosechada || formData.cantidad_cosechada <= 0) {
        alert('Por favor ingresa una cantidad cosechada válida');
        setLoading(false);
        return;
      }

      const response = await api.post('/cosechas', cosechaData);
      const savedCosecha = response.data;
      console.log('Cosecha guardada:', savedCosecha);
      
      // Preguntar si desea liberar el lote para nueva siembra
      const loteSeleccionado = lotes.find(l => l.id === formData.lote_id);
      if (loteSeleccionado) {
        const liberarLote = confirm(
          `¿Deseas marcar el lote "${loteSeleccionado.nombre}" como disponible para una nueva siembra?\n\n` +
          `Esto permitirá que el lote pueda ser utilizado para un nuevo cultivo.`
        );
        
        if (liberarLote) {
          await liberarLoteParaNuevaSiembra(formData.lote_id);
        }
      }
      
      // Recargar los datos desde la API
      await loadData();
      
      setShowForm(false);
      setFormData({
        lote_id: 0,
        cultivo_id: 0,
        superficie_ha: 0,
        fecha_siembra: '',
        densidad_siembra: 0,
        variedad_semilla: '',
        fertilizante_nitrogeno: 0,
        fertilizante_fosforo: 0,
        fertilizante_potasio: 0,
        otros_insumos: '',
        fecha_cosecha: new Date().toISOString().split('T')[0],
        cantidad_cosechada: 0,
        unidad_cosecha: 'kg',
        rinde_real: 0,
        rinde_esperado: 0,
        diferencia_rinde: 0,
        porcentaje_cumplimiento: 0,
        clima_favorable: true,
        plagas_enfermedades: false,
        riego_suficiente: true,
        observaciones: ''
      });
      
      alert('Cosecha creada exitosamente');
    } catch (error) {
      console.error('Error guardando cosecha:', error);
      alert('Error al guardar la cosecha. Verifica la conexión con el servidor.');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value, type } = e.target;
    
    if (type === 'checkbox') {
      const checked = (e.target as HTMLInputElement).checked;
      setFormData(prev => ({ ...prev, [name]: checked }));
    } else if (type === 'number') {
      setFormData(prev => ({ ...prev, [name]: parseFloat(value) || 0 }));
    } else {
      setFormData(prev => ({ ...prev, [name]: value }));
    }

    // Auto-completar campos cuando se selecciona un lote
    if (name === 'lote_id' && value) {
      const loteId = parseInt(value);
      const loteSeleccionado = lotes.find(l => l.id === loteId);
      
      if (loteSeleccionado) {
        // Buscar el cultivo correspondiente al lote
        const cultivoCorrespondiente = cultivos.find(c => c.nombre === loteSeleccionado.cultivo);
        
        setFormData(prev => ({
          ...prev,
          lote_id: loteId,
          superficie_ha: loteSeleccionado.superficie,
          cultivo_id: cultivoCorrespondiente?.id || 0,
          variedad_semilla: cultivoCorrespondiente?.variedad || '',
          rinde_esperado: cultivoCorrespondiente?.rendimiento_esperado || 0,
        // Cargar fecha de siembra del lote si está disponible
        fecha_siembra: loteSeleccionado.fechaSiembra || prev.fecha_siembra,
          // Establecer unidad de cosecha basada en la unidad de rendimiento del cultivo
          unidad_cosecha: cultivoCorrespondiente ? mapearUnidadRendimientoACosecha(cultivoCorrespondiente.unidad_rendimiento) : prev.unidad_cosecha
        }));
        
      }
    }

    // Auto-completar campos cuando se selecciona un cultivo
    if (name === 'cultivo_id' && value) {
      const cultivoId = parseInt(value);
      const cultivoSeleccionado = cultivos.find(c => c.id === cultivoId);
      
      if (cultivoSeleccionado) {
        setFormData(prev => ({
          ...prev,
          cultivo_id: cultivoId,
          variedad_semilla: cultivoSeleccionado.variedad,
          rinde_esperado: cultivoSeleccionado.rendimiento_esperado,
          // Establecer unidad de cosecha basada en la unidad de rendimiento del cultivo
          unidad_cosecha: mapearUnidadRendimientoACosecha(cultivoSeleccionado.unidad_rendimiento),
        }));
      }
    }

    // Calcular rendimiento real automáticamente cuando cambia la cantidad cosechada
    if (name === 'cantidad_cosechada' && formData.superficie_ha > 0) {
      const cantidad = parseFloat(value) || 0;
      const cultivo = cultivos.find(c => c.id === formData.cultivo_id);
      const unidadCultivo = cultivo?.unidad_rendimiento || 'kg/ha';
      
      const rindeReal = calcularRendimientoReal(
        cantidad, 
        formData.unidad_cosecha, 
        formData.superficie_ha, 
        unidadCultivo
      );
      
      setFormData(prev => ({ ...prev, rinde_real: rindeReal }));
    }

    // Recalcular rendimiento real cuando cambia la unidad de cosecha
    if (name === 'unidad_cosecha' && formData.cantidad_cosechada > 0 && formData.superficie_ha > 0) {
      const cantidad = formData.cantidad_cosechada;
      const cultivo = cultivos.find(c => c.id === formData.cultivo_id);
      const unidadCultivo = cultivo?.unidad_rendimiento || 'kg/ha';
      
      const rindeReal = calcularRendimientoReal(
        cantidad, 
        value, 
        formData.superficie_ha, 
        unidadCultivo
      );
      
      setFormData(prev => ({ ...prev, rinde_real: rindeReal }));
    }
  };

  const filteredCosechas = cosechas.filter(cosecha => {
    const lote = lotes.find(l => l.id === cosecha.lote_id);
    const cultivo = cultivos.find(c => c.id === cosecha.cultivo_id);
    return (
      lote?.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
      cultivo?.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
      cosecha.variedad_semilla.toLowerCase().includes(searchTerm.toLowerCase())
    );
  });

  const getLoteName = (loteId: number) => {
    const lote = lotes.find(l => l.id === loteId);
    return lote ? lote.nombre : 'Lote no encontrado';
  };

  const getCultivoName = (cultivoId: number) => {
    const cultivo = cultivos.find(c => c.id === cultivoId);
    return cultivo ? cultivo.nombre : 'Cultivo no encontrado';
  };

  const handleVerHistorial = (loteId: number) => {
    const lote = lotes.find(l => l.id === loteId);
    if (lote) {
      setSelectedLoteForHistorial({ id: loteId, nombre: lote.nombre });
      setShowHistorialModal(true);
    }
  };

  const handleForcedRelease = async () => {
    if (!selectedLoteForForcedRelease || !forcedReleaseJustification.trim()) {
      alert('Por favor, proporciona una justificación para la liberación forzada.');
      return;
    }

    try {
      await liberarLoteParaNuevaSiembra(selectedLoteForForcedRelease.id, true, forcedReleaseJustification);
      setShowForcedReleaseModal(false);
      setForcedReleaseJustification('');
      setSelectedLoteForForcedRelease(null);
    } catch (error) {
      console.error('Error en liberación forzada:', error);
    }
  };

  const cancelForcedRelease = () => {
    setShowForcedReleaseModal(false);
    setForcedReleaseJustification('');
    setSelectedLoteForForcedRelease(null);
  };

  return (
    <div style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ margin: 0, color: '#1f2937' }}>🌾 Gestión de Cosechas</h1>
        <button
          onClick={() => {
            // Resetear formulario con fecha de hoy
            setFormData({
              lote_id: 0,
              cultivo_id: 0,
              superficie_ha: 0,
              fecha_siembra: '',
              densidad_siembra: 0,
              variedad_semilla: '',
              fertilizante_nitrogeno: 0,
              fertilizante_fosforo: 0,
              fertilizante_potasio: 0,
              otros_insumos: '',
              fecha_cosecha: new Date().toISOString().split('T')[0],
              cantidad_cosechada: 0,
              unidad_cosecha: 'kg',
              rinde_real: 0,
              rinde_esperado: 0,
              diferencia_rinde: 0,
              porcentaje_cumplimiento: 0,
              clima_favorable: true,
              plagas_enfermedades: false,
              riego_suficiente: true,
              observaciones: ''
            });
            setShowForm(true);
          }}
          style={{
            backgroundColor: '#10b981',
            color: 'white',
            border: 'none',
            padding: '0.75rem 1.5rem',
            borderRadius: '0.5rem',
            cursor: 'pointer',
            fontSize: '1rem',
            fontWeight: '500'
          }}
        >
          + Nueva Cosecha
        </button>
      </div>

      {/* Filtros */}
      <div style={{ marginBottom: '2rem' }}>
        <input
          type="text"
          placeholder="Buscar por lote, cultivo o variedad..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{
            width: '100%',
            padding: '0.75rem',
            border: '1px solid #d1d5db',
            borderRadius: '0.5rem',
            fontSize: '1rem'
          }}
        />
      </div>

      {/* Lista de cosechas */}
      <div style={{ display: 'grid', gap: '1rem' }}>
        {filteredCosechas.map((cosecha) => (
          <div
            key={cosecha.id}
            style={{
              backgroundColor: 'white',
              padding: '1.5rem',
              borderRadius: '0.5rem',
              boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
              border: '1px solid #e5e7eb'
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', marginBottom: '1rem' }}>
              <div>
                <h3 style={{ margin: 0, color: '#1f2937', fontSize: '1.25rem' }}>
                  {getLoteName(cosecha.lote_id)} - {getCultivoName(cosecha.cultivo_id)}
                </h3>
                <p style={{ margin: '0.5rem 0 0 0', color: '#6b7280' }}>
                  Variedad: {cosecha.variedad_semilla} | Superficie: {cosecha.superficie_ha} ha
                </p>
              </div>
              <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
                <button
                  onClick={() => handleVerHistorial(cosecha.lote_id)}
                  style={{
                    backgroundColor: '#3b82f6',
                    color: 'white',
                    border: 'none',
                    padding: '0.5rem 1rem',
                    borderRadius: '0.25rem',
                    cursor: 'pointer',
                    fontSize: '0.875rem',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.25rem'
                  }}
                >
                  📊 Historial
                </button>
              <div style={{ textAlign: 'right' }}>
                <div style={{ 
                  fontSize: '1.5rem', 
                  fontWeight: 'bold',
                  color: cosecha.porcentaje_cumplimiento >= 100 ? '#10b981' : '#f59e0b'
                }}>
                  {cosecha.porcentaje_cumplimiento.toFixed(1)}%
                </div>
                <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                  Cumplimiento
                  </div>
                </div>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginBottom: '1rem' }}>
              <div>
                <strong>Fecha de Cosecha:</strong> {new Date(cosecha.fecha_cosecha).toLocaleDateString()}
              </div>
              <div>
                <strong>Cantidad Cosechada:</strong> {cosecha.cantidad_cosechada.toLocaleString()} {cosecha.unidad_cosecha}
              </div>
              <div>
                <strong>Rendimiento Real:</strong> {cosecha.rinde_real.toLocaleString()} {(() => {
                  const cultivo = cultivos.find(c => c.id === cosecha.cultivo_id);
                  return cultivo?.unidad_rendimiento || 'kg/ha';
                })()}
              </div>
              <div>
                <strong>Rendimiento Esperado:</strong> {cosecha.rinde_esperado.toLocaleString()} {(() => {
                  const cultivo = cultivos.find(c => c.id === cosecha.cultivo_id);
                  return cultivo?.unidad_rendimiento || 'kg/ha';
                })()}
              </div>
              <div>
                <strong>Diferencia:</strong> 
                <span style={{ color: cosecha.diferencia_rinde >= 0 ? '#10b981' : '#ef4444' }}>
                  {cosecha.diferencia_rinde >= 0 ? '+' : ''}{cosecha.diferencia_rinde.toLocaleString()} {(() => {
                    const cultivo = cultivos.find(c => c.id === cosecha.cultivo_id);
                    return cultivo?.unidad_rendimiento || 'kg/ha';
                  })()}
                </span>
              </div>
            </div>

            {cosecha.observaciones && (
              <div style={{ marginTop: '1rem', padding: '1rem', backgroundColor: '#f9fafb', borderRadius: '0.5rem' }}>
                <strong>Observaciones:</strong> {cosecha.observaciones}
              </div>
            )}
          </div>
        ))}
      </div>

      {filteredCosechas.length === 0 && (
        <div style={{ textAlign: 'center', padding: '3rem', color: '#6b7280' }}>
          <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>🌾</div>
          <h3>No hay cosechas registradas</h3>
          <p>Comienza agregando una nueva cosecha para hacer seguimiento de tus rendimientos.</p>
        </div>
      )}

      {/* Modal de formulario */}
      {showForm && (
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
            maxWidth: '600px',
            maxHeight: '90vh',
            overflowY: 'auto'
          }}>
            <h2 style={{ margin: '0 0 1.5rem 0', color: '#1f2937' }}>Nueva Cosecha</h2>
            
            <form onSubmit={handleSubmit}>
              {/* Sección 1: Información del Lote y Cultivo */}
              <div style={{ marginBottom: '2rem' }}>
                <h3 style={{ color: '#1f2937', marginBottom: '1rem', borderBottom: '2px solid #e5e7eb', paddingBottom: '0.5rem' }}>
                  📍 Información del Lote y Cultivo
                </h3>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1rem' }}>
                  <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600', color: '#374151' }}>
                      Lote *
                    </label>
                    <select
                      name="lote_id"
                      value={formData.lote_id}
                      onChange={handleInputChange}
                      required
                      style={{
                        width: '100%',
                        padding: '0.75rem',
                        border: '2px solid #d1d5db',
                        borderRadius: '0.5rem',
                        fontSize: '1rem',
                        backgroundColor: '#f9fafb'
                      }}
                    >
                      <option value={0}>Seleccionar lote</option>
                      {lotes.map(lote => (
                        <option key={lote.id} value={lote.id}>
                          {lote.nombre} - {lote.cultivo} ({lote.superficie} ha)
                        </option>
                      ))}
                    </select>
                    {lotes.length === 0 && (
                      <div style={{ 
                        marginTop: '0.5rem', 
                        padding: '0.75rem', 
                        backgroundColor: '#fef3c7', 
                        border: '1px solid #f59e0b', 
                        borderRadius: '0.375rem',
                        fontSize: '0.875rem',
                        color: '#92400e'
                      }}>
                        ℹ️ Solo se muestran lotes con cultivos sembrados. Los lotes disponibles (sin sembrar) no aparecen en esta lista.
                      </div>
                    )}
                  </div>

                  <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600', color: '#374151' }}>
                      Cultivo *
                    </label>
                    <select
                      name="cultivo_id"
                      value={formData.cultivo_id}
                      onChange={handleInputChange}
                      required
                      style={{
                        width: '100%',
                        padding: '0.75rem',
                        border: '2px solid #d1d5db',
                        borderRadius: '0.5rem',
                        fontSize: '1rem',
                        backgroundColor: '#f9fafb'
                      }}
                    >
                      <option value={0}>Seleccionar cultivo</option>
                      {cultivos.map(cultivo => (
                        <option key={cultivo.id} value={cultivo.id}>
                          {cultivo.nombre} - {cultivo.variedad}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600', color: '#374151' }}>
                      Superficie (ha) *
                    </label>
                    <input
                      type="number"
                      name="superficie_ha"
                      value={formData.superficie_ha}
                      onChange={handleInputChange}
                      required
                      step="0.01"
                      style={{
                        width: '100%',
                        padding: '0.75rem',
                        border: '2px solid #d1d5db',
                        borderRadius: '0.5rem',
                        fontSize: '1rem',
                        backgroundColor: '#f9fafb'
                      }}
                    />
                  </div>

                  <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600', color: '#374151' }}>
                      Variedad de Semilla *
                    </label>
                    <input
                      type="text"
                      name="variedad_semilla"
                      value={formData.variedad_semilla}
                      onChange={handleInputChange}
                      required
                      style={{
                        width: '100%',
                        padding: '0.75rem',
                        border: '2px solid #d1d5db',
                        borderRadius: '0.5rem',
                        fontSize: '1rem',
                        backgroundColor: '#f9fafb'
                      }}
                    />
                  </div>
                </div>
              </div>

              {/* Sección 2: Fechas */}
              <div style={{ marginBottom: '2rem' }}>
                <h3 style={{ color: '#1f2937', marginBottom: '1rem', borderBottom: '2px solid #e5e7eb', paddingBottom: '0.5rem' }}>
                  📅 Fechas
                </h3>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1rem' }}>
                  <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600', color: '#374151' }}>
                      Fecha de Siembra *
                    </label>
                    <input
                      type="date"
                      name="fecha_siembra"
                      value={formData.fecha_siembra}
                      onChange={handleInputChange}
                      required
                      style={{
                        width: '100%',
                        padding: '0.75rem',
                        border: '2px solid #d1d5db',
                        borderRadius: '0.5rem',
                        fontSize: '1rem',
                        backgroundColor: '#f9fafb'
                      }}
                    />
                  </div>

                  <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600', color: '#374151' }}>
                      Fecha de Cosecha * (Hoy)
                    </label>
                    <input
                      type="date"
                      name="fecha_cosecha"
                      value={formData.fecha_cosecha}
                      onChange={handleInputChange}
                      required
                      style={{
                        width: '100%',
                        padding: '0.75rem',
                        border: '2px solid #10b981',
                        borderRadius: '0.5rem',
                        fontSize: '1rem',
                        backgroundColor: '#f0fdf4'
                      }}
                    />
                  </div>
                </div>
              </div>

              {/* Sección 3: Rendimiento y Cosecha */}
              <div style={{ marginBottom: '2rem' }}>
                <h3 style={{ color: '#1f2937', marginBottom: '1rem', borderBottom: '2px solid #e5e7eb', paddingBottom: '0.5rem' }}>
                  🌾 Rendimiento y Cosecha
                </h3>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1rem' }}>
                  <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600', color: '#374151' }}>
                      Cantidad Cosechada *
                    </label>
                    <input
                      type="number"
                      name="cantidad_cosechada"
                      step="0.01"
                      min="0"
                      value={formData.cantidad_cosechada === 0 ? '' : formData.cantidad_cosechada}
                      onChange={(e) => {
                        const value = parseFloat(e.target.value) || 0;
                        setFormData(prev => {
                          const newData = { ...prev, cantidad_cosechada: value };
                          
                          // Recalcular rendimiento real si tenemos superficie
                          if (newData.superficie_ha > 0) {
                            const cultivo = cultivos.find(c => c.id === newData.cultivo_id);
                            const unidadCultivo = cultivo?.unidad_rendimiento || 'kg/ha';
                            
                            const rindeReal = calcularRendimientoReal(
                              value, 
                              newData.unidad_cosecha, 
                              newData.superficie_ha, 
                              unidadCultivo
                            );
                            
                            newData.rinde_real = rindeReal;
                          }
                          
                          return newData;
                        });
                      }}
                      placeholder="Ej: 15000.50"
                      required
                      style={{
                        width: '100%',
                        padding: '0.75rem',
                        border: '2px solid #3b82f6',
                        borderRadius: '0.5rem',
                        fontSize: '1rem',
                        backgroundColor: '#eff6ff'
                      }}
                    />
                  </div>

                  <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600', color: '#374151' }}>
                      Unidad
                    </label>
                    <select
                      name="unidad_cosecha"
                      value={formData.unidad_cosecha}
                      onChange={handleInputChange}
                      style={{
                        width: '100%',
                        padding: '0.75rem',
                        border: '2px solid #d1d5db',
                        borderRadius: '0.5rem',
                        fontSize: '1rem',
                        backgroundColor: '#f9fafb'
                      }}
                    >
                      <option value="kg">Kilogramos (kg)</option>
                      <option value="tn">Toneladas (tn)</option>
                      <option value="qq">Quintales (qq)</option>
                    </select>
                  </div>

                  <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600', color: '#374151' }}>
                      Rendimiento Esperado ({formData.cultivo_id ? (() => {
                        const cultivo = cultivos.find(c => c.id === formData.cultivo_id);
                        return cultivo?.unidad_rendimiento || 'kg/ha';
                      })() : 'kg/ha'})
                    </label>
                    <input
                      type="number"
                      name="rinde_esperado"
                      value={formData.rinde_esperado}
                      onChange={handleInputChange}
                      step="0.01"
                      style={{
                        width: '100%',
                        padding: '0.75rem',
                        border: '2px solid #d1d5db',
                        borderRadius: '0.5rem',
                        fontSize: '1rem',
                        backgroundColor: '#f9fafb'
                      }}
                    />
                  </div>

                  <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600', color: '#374151' }}>
                      Rendimiento Real ({formData.cultivo_id ? (() => {
                        const cultivo = cultivos.find(c => c.id === formData.cultivo_id);
                        return cultivo?.unidad_rendimiento || 'kg/ha';
                      })() : 'kg/ha'}) - Calculado automáticamente
                    </label>
                    <input
                      type="number"
                      name="rinde_real"
                      value={formData.rinde_real.toFixed(2)}
                      readOnly
                      style={{
                        width: '100%',
                        padding: '0.75rem',
                        border: '2px solid #10b981',
                        borderRadius: '0.5rem',
                        fontSize: '1rem',
                        backgroundColor: '#f0fdf4',
                        color: '#065f46',
                        fontWeight: '600'
                      }}
                    />
                  </div>

                </div>
              </div>

              {/* Sección 4: Resumen de Rendimiento */}
              {formData.cantidad_cosechada > 0 && formData.superficie_ha > 0 && (() => {
                const cultivoSeleccionado = cultivos.find(c => c.id === formData.cultivo_id);
                const cultivoNombre = cultivoSeleccionado?.nombre || '';
                
                return (
                <div style={{ marginBottom: '2rem', padding: '1.5rem', backgroundColor: '#f0fdf4', borderRadius: '0.5rem', border: '2px solid #10b981' }}>
                  <h3 style={{ color: '#065f46', marginBottom: '1rem', borderBottom: '2px solid #10b981', paddingBottom: '0.5rem' }}>
                    📊 Resumen de Rendimiento
                  </h3>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
                    <div style={{ textAlign: 'center' }}>
                      <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#065f46' }}>
                          {formData.rinde_real.toFixed(3)} {(() => {
                            const cultivo = cultivos.find(c => c.id === formData.cultivo_id);
                            return cultivo?.unidad_rendimiento || 'kg/ha';
                          })()}
                      </div>
                      <div style={{ fontSize: '0.875rem', color: '#047857' }}>Rendimiento Real</div>
                    </div>
                    <div style={{ textAlign: 'center' }}>
                      <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937' }}>
                          {formData.rinde_esperado.toFixed(3)} {(() => {
                            const cultivo = cultivos.find(c => c.id === formData.cultivo_id);
                            return cultivo?.unidad_rendimiento || 'kg/ha';
                          })()}
                      </div>
                      <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>Rendimiento Esperado</div>
                    </div>
                    <div style={{ textAlign: 'center' }}>
                      <div style={{ 
                        fontSize: '1.5rem', 
                        fontWeight: 'bold',
                          color: (() => {
                            // Convertir rendimiento esperado a kg/ha para comparación
                            const cultivo = cultivos.find(c => c.id === formData.cultivo_id);
                            let rindeEsperadoEnKg = formData.rinde_esperado;
                            
                            if (cultivo?.unidad_rendimiento) {
                              const unidad = cultivo.unidad_rendimiento.toLowerCase();
                              if (unidad.includes('tn') || unidad.includes('tonelada')) {
                                rindeEsperadoEnKg = formData.rinde_esperado * 1000; // tn/ha a kg/ha
                              } else if (unidad.includes('qq') || unidad.includes('quintal')) {
                                rindeEsperadoEnKg = formData.rinde_esperado * 46; // qq/ha a kg/ha
                              }
                            }
                            
                            return rendimientoCorregido >= rindeEsperadoEnKg ? '#10b981' : '#f59e0b';
                          })()
                        }}>
                          {(() => {
                            // Usar las mismas unidades para la comparación
                            return formData.rinde_esperado > 0 ? ((rendimientoCorregido / formData.rinde_esperado) * 100).toFixed(1) : '0';
                          })()}%
                      </div>
                      <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>Cumplimiento</div>
                        <div style={{ fontSize: '0.75rem', color: '#6b7280', marginTop: '0.25rem' }}>
(Real)
                        </div>
                    </div>
                    <div style={{ textAlign: 'center' }}>
                      <div style={{ 
                        fontSize: '1.5rem', 
                        fontWeight: 'bold',
                          color: (() => {
                            // Convertir rendimiento esperado a kg/ha para comparación
                            const cultivo = cultivos.find(c => c.id === formData.cultivo_id);
                            let rindeEsperadoEnKg = formData.rinde_esperado;
                            
                            if (cultivo?.unidad_rendimiento) {
                              const unidad = cultivo.unidad_rendimiento.toLowerCase();
                              if (unidad.includes('tn') || unidad.includes('tonelada')) {
                                rindeEsperadoEnKg = formData.rinde_esperado * 1000; // tn/ha a kg/ha
                              } else if (unidad.includes('qq') || unidad.includes('quintal')) {
                                rindeEsperadoEnKg = formData.rinde_esperado * 46; // qq/ha a kg/ha
                              }
                            }
                            
                            return (rendimientoCorregido - formData.rinde_esperado) >= 0 ? '#10b981' : '#ef4444';
                          })()
                        }}>
                          {(() => {
                            // Calcular diferencia en las mismas unidades
                            const diferencia = rendimientoCorregido - formData.rinde_esperado;
                            return (diferencia >= 0 ? '+' : '') + diferencia.toFixed(3);
                          })()} {(() => {
                            const cultivo = cultivos.find(c => c.id === formData.cultivo_id);
                            return cultivo?.unidad_rendimiento || 'kg/ha';
                          })()}
                      </div>
                      <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>Diferencia</div>
                        <div style={{ fontSize: '0.75rem', color: '#6b7280', marginTop: '0.25rem' }}>
(Real)
                    </div>
                  </div>
                    </div>
                  </div>
                );
              })()}

              {/* Sección 5: Información del Cultivo */}
              {formData.cultivo_id > 0 && (
                <div style={{ marginBottom: '1.5rem', padding: '1rem', backgroundColor: '#f8fafc', borderRadius: '0.5rem', border: '1px solid #e2e8f0' }}>
                  <h3 style={{ color: '#1f2937', marginBottom: '1rem', borderBottom: '2px solid #e5e7eb', paddingBottom: '0.5rem' }}>
                    ℹ️ Información del Cultivo
                  </h3>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
                    {(() => {
                      const cultivoSeleccionado = cultivos.find(c => c.id === formData.cultivo_id);
                      const cultivoNombre = cultivoSeleccionado?.nombre || '';
                      // Función de humedad eliminada - ahora se enfoca en cantidad esperada vs obtenida
                      
                      return (
                        <>
                          {cultivoSeleccionado?.unidad_rendimiento && (
                            <div style={{ textAlign: 'center' }}>
                              <div style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.25rem' }}>Unidad de Rendimiento</div>
                              <div style={{ fontSize: '1rem', fontWeight: '600', color: '#1f2937' }}>{cultivoSeleccionado.unidad_rendimiento}</div>
                </div>
              )}
                          {formData.cantidad_cosechada > 0 && formData.superficie_ha > 0 && (() => {
                            
                            return (
                              <div style={{ textAlign: 'center' }}>
                                <div style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.25rem' }}>Rendimiento Final</div>
                                <div style={{ fontSize: '1rem', fontWeight: '600', color: '#3b82f6' }}>
                                  {formData.rinde_real.toFixed(2)} {(() => {
                                    const cultivo = cultivos.find(c => c.id === formData.cultivo_id);
                                    return cultivo?.unidad_rendimiento || 'kg/ha';
                                  })()}
                                </div>
                              </div>
                            );
                          })()}
                        </>
                      );
                    })()}
                  </div>
                </div>
              )}

              {/* Sección 6: Observaciones */}
              <div style={{ marginBottom: '1.5rem' }}>
                <h3 style={{ color: '#1f2937', marginBottom: '1rem', borderBottom: '2px solid #e5e7eb', paddingBottom: '0.5rem' }}>
                  📝 Observaciones
                </h3>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600', color: '#374151' }}>
                  Observaciones adicionales
                </label>
                <textarea
                  name="observaciones"
                  value={formData.observaciones}
                  onChange={handleInputChange}
                  rows={3}
                  placeholder="Ingresa observaciones sobre la cosecha, condiciones climáticas, plagas, etc."
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '2px solid #d1d5db',
                    borderRadius: '0.5rem',
                    fontSize: '1rem',
                    resize: 'vertical',
                    backgroundColor: '#f9fafb'
                  }}
                />
              </div>

              <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
                <button
                  type="button"
                  onClick={() => setShowForm(false)}
                  style={{
                    backgroundColor: '#6b7280',
                    color: 'white',
                    border: 'none',
                    padding: '0.75rem 1.5rem',
                    borderRadius: '0.5rem',
                    cursor: 'pointer',
                    fontSize: '1rem'
                  }}
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  style={{
                    backgroundColor: loading ? '#9ca3af' : '#10b981',
                    color: 'white',
                    border: 'none',
                    padding: '0.75rem 1.5rem',
                    borderRadius: '0.5rem',
                    cursor: loading ? 'not-allowed' : 'pointer',
                    fontSize: '1rem'
                  }}
                >
                  {loading ? 'Guardando...' : 'Guardar Cosecha'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal de Historial de Cosechas */}
      {selectedLoteForHistorial && (
        <HistorialCosechasModal
          loteId={selectedLoteForHistorial.id}
          loteNombre={selectedLoteForHistorial.nombre}
          isOpen={showHistorialModal}
          onClose={() => {
            setShowHistorialModal(false);
            setSelectedLoteForHistorial(null);
          }}
        />
      )}

      {/* Modal de Liberación Forzada */}
      {showForcedReleaseModal && selectedLoteForForcedRelease && (
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
            maxWidth: '500px',
            width: '90%',
            maxHeight: '80vh',
            overflow: 'auto'
          }}>
            <h2 style={{ marginTop: 0, color: '#dc2626', marginBottom: '1rem' }}>
              ⚠️ Liberación Forzada de Lote
            </h2>
            
            <div style={{ marginBottom: '1rem' }}>
              <p style={{ marginBottom: '1rem', color: '#374151' }}>
                <strong>Lote:</strong> {selectedLoteForForcedRelease.nombre}
              </p>
              <p style={{ marginBottom: '1rem', color: '#6b7280' }}>
                Este lote tiene cosechas muy recientes y normalmente requeriría un período de descanso. 
                La liberación forzada puede afectar la salud del suelo.
              </p>
            </div>

            <div style={{ marginBottom: '1.5rem' }}>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600', color: '#374151' }}>
                Justificación para la liberación forzada *
              </label>
              <textarea
                value={forcedReleaseJustification}
                onChange={(e) => setForcedReleaseJustification(e.target.value)}
                placeholder="Explica por qué es necesario liberar este lote antes del período de descanso recomendado..."
                required
                style={{
                  width: '100%',
                  minHeight: '100px',
                  padding: '0.75rem',
                  border: '2px solid #dc2626',
                  borderRadius: '0.5rem',
                  fontSize: '1rem',
                  resize: 'vertical'
                }}
              />
            </div>

            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
              <button
                onClick={cancelForcedRelease}
                style={{
                  backgroundColor: '#6b7280',
                  color: 'white',
                  border: 'none',
                  padding: '0.75rem 1.5rem',
                  borderRadius: '0.5rem',
                  cursor: 'pointer',
                  fontSize: '1rem'
                }}
              >
                Cancelar
              </button>
              <button
                onClick={handleForcedRelease}
                style={{
                  backgroundColor: '#dc2626',
                  color: 'white',
                  border: 'none',
                  padding: '0.75rem 1.5rem',
                  borderRadius: '0.5rem',
                  cursor: 'pointer',
                  fontSize: '1rem'
                }}
              >
                Liberar Forzosamente
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CosechasManagement;