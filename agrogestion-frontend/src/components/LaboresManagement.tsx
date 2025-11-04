import React, { useState, useEffect } from 'react';
import { useCurrencyContext } from '../contexts/CurrencyContext';
import { useAuth } from '../contexts/AuthContext';
import { useEmpresa } from '../contexts/EmpresaContext';
import { offlineService } from '../services/OfflineService';
import { lotesService, insumosService, maquinariaService, laboresService, dosisAgroquimicosService } from '../services/apiServices';
import PermissionGate from './PermissionGate';

interface Insumo {
  id: number;
  nombre: string;
  tipo: string;
  stock_actual: number;
  unidad_medida: string;
  precio_unitario: number;
}

interface Maquinaria {
  id: number;
  nombre: string;
  tipo: string;
  estado: string;
  kilometros_uso: number;
  costo_por_hora: number;
}

interface Labor {
  id?: number;
  tipo: string;
  fecha: string;
  fecha_fin?: string;
  observaciones: string;
  lote_id: number;
  lote_nombre: string;
  estado: 'planificada' | 'en_progreso' | 'completada' | 'interrumpida' | 'cancelada';
  insumos_usados: InsumoUsado[];
  maquinaria_asignada: MaquinariaAsignada[];
  responsable: string;
  horas_trabajo?: number;
  costo_total?: number;
  // Nuevos campos para costos detallados
  costo_base?: number;
  costo_maquinaria?: number;
  costo_mano_obra?: number;
  maquinarias?: LaborMaquinaria[];
  mano_obra?: LaborManoObra[];
}

interface MaquinariaAsignada {
  maquinaria_id: number;
  maquinaria_nombre: string;
  costo_total: number;
  proveedor?: string;
}

interface InsumoUsado {
  insumo_id: number;
  insumo_nombre: string;
  cantidad_usada: number;
  cantidad_planificada: number;
  unidad_medida: string;
  costo_unitario: number;
  costo_total: number;
}

// Nuevas interfaces para costos detallados
interface LaborMaquinaria {
  id_labor_maquinaria: number;
  id_labor: number;
  descripcion: string;
  proveedor?: string;
  costo: number;
  observaciones?: string;
}

interface LaborManoObra {
  id_labor_mano_obra: number;
  id_labor: number;
  descripcion: string;
  cantidad_personas: number;
  proveedor?: string;
  costo_total: number;
  horas_trabajo?: number;
  observaciones?: string;
}

interface Lote {
  id: number;
  nombre: string;
  superficie: number;
  cultivo: string;
  estado?: string;  // Estado del lote (DISPONIBLE, SEMBRADO, etc.)
}

const LaboresManagement: React.FC = () => {
  const { formatCurrency } = useCurrencyContext();
  const { user } = useAuth();
  const empresaContext = useEmpresa();
  const esOperario = empresaContext?.esOperario() || false;
  const esAdministrador = empresaContext?.esAdministrador() || false;
  const esJefeCampo = empresaContext?.esJefeCampo() || false;
  const esConsultorExterno = empresaContext?.esConsultorExterno() || false;
  
  // Función para verificar si el usuario puede editar/eliminar una labor
  const puedeModificarLabor = (labor: any): boolean => {
    // CONSULTOR_EXTERNO es solo lectura, no puede modificar nada
    if (esConsultorExterno) {
      return false;
    }
    
    // ADMIN y JEFE_CAMPO pueden modificar cualquier labor
    if (esAdministrador || esJefeCampo) {
      return true;
    }
    
    // OPERARIO solo puede modificar sus propias labores
    if (esOperario) {
      // Comparar el responsable de la labor con el nombre del usuario
      const nombreUsuario = user?.name || '';
      const responsableLabor = labor.responsable || '';
      return nombreUsuario.toLowerCase() === responsableLabor.toLowerCase();
    }
    
    // Otros roles no pueden modificar
    return false;
  };
  
  // Función para mapear estados del frontend al backend
  const mapEstadoToBackend = (estado: string) => {
    const estadoMap: { [key: string]: string } = {
      'planificada': 'PLANIFICADA',
      'en_progreso': 'EN_PROGRESO',
      'completada': 'COMPLETADA',
      'cancelada': 'CANCELADA'
    };
    return estadoMap[estado] || 'PLANIFICADA';
  };

  // Función para mapear estados del backend al frontend
  const mapEstadoFromBackend = (estado: string) => {
    const estadoMap: { [key: string]: string } = {
      'PLANIFICADA': 'planificada',
      'EN_PROGRESO': 'en_progreso',
      'COMPLETADA': 'completada',
      'CANCELADA': 'cancelada'
    };
    return estadoMap[estado] || 'planificada';
  };

  // Función para mapear tipos de labor del frontend al backend
  const mapTipoLaborToBackend = (tipo: string) => {
    const tipoMap: { [key: string]: string } = {
      'siembra': 'SIEMBRA',
      'fertilizacion': 'FERTILIZACION',
      'riego': 'RIEGO',
      'cosecha': 'COSECHA',
      'mantenimiento': 'MANTENIMIENTO',
      'poda': 'PODA',
      'control_plagas': 'CONTROL_PLAGAS',
      'control_malezas': 'CONTROL_MALEZAS',
      'desmalezado': 'CONTROL_MALEZAS', // Mapear desmalezado a CONTROL_MALEZAS
      'aplicacion_herbicida': 'CONTROL_MALEZAS', // Mapear aplicación herbicida a CONTROL_MALEZAS
      'aplicacion_insecticida': 'CONTROL_PLAGAS', // Mapear aplicación insecticida a CONTROL_PLAGAS
      'pulverizacion': 'MANTENIMIENTO', // Mapear pulverización a MANTENIMIENTO
      'arado': 'MANTENIMIENTO', // Mapear arado a MANTENIMIENTO
      'rastra': 'MANTENIMIENTO', // Mapear rastra a MANTENIMIENTO
      'monitoreo': 'ANALISIS_SUELO', // Mapear monitoreo a ANALISIS_SUELO
      'otro': 'OTROS', // Mapear otro a OTROS
      'analisis_suelo': 'ANALISIS_SUELO',
      'otros': 'OTROS'
    };
    return tipoMap[tipo.toLowerCase()] || 'OTROS';
  };

  // Función para mapear tipos de labor del backend al frontend
  const mapTipoLaborFromBackend = (tipo: string) => {
    const tipoMap: { [key: string]: string } = {
      'SIEMBRA': 'siembra',
      'FERTILIZACION': 'fertilizacion',
      'RIEGO': 'riego',
      'COSECHA': 'cosecha',
      'MANTENIMIENTO': 'mantenimiento',
      'PODA': 'poda',
      'CONTROL_PLAGAS': 'control_plagas',
      'CONTROL_MALEZAS': 'control_malezas',
      'ANALISIS_SUELO': 'analisis_suelo',
      'OTROS': 'otros'
    };
    return tipoMap[tipo] || 'otros';
  };
  
  const [labores, setLabores] = useState<Labor[]>([]);
  const [lotes, setLotes] = useState<Lote[]>([]);
  const [insumos, setInsumos] = useState<Insumo[]>([]);
  const [maquinaria, setMaquinaria] = useState<Maquinaria[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [editingLabor, setEditingLabor] = useState<Labor | null>(null);
  const [showDetallesCostos, setShowDetallesCostos] = useState(false);
  const [laborSeleccionada, setLaborSeleccionada] = useState<Labor | null>(null);
  
  // Estados para paginación
  const [paginaActual, setPaginaActual] = useState(1);
  const [elementosPorPagina] = useState(10);
  
  // Estados para formularios de maquinaria y mano de obra
  const [showFormMaquinaria, setShowFormMaquinaria] = useState(false);
  const [showFormMaquinariaAlquilada, setShowFormMaquinariaAlquilada] = useState(false);
  const [showFormManoObra, setShowFormManoObra] = useState(false);
  const [laborParaMaquinaria, setLaborParaMaquinaria] = useState<Labor | null>(null);
  const [laborParaManoObra, setLaborParaManoObra] = useState<Labor | null>(null);
  
  // Formulario de maquinaria
  const [formMaquinaria, setFormMaquinaria] = useState({
    descripcion: '',
    proveedor: '',
    horas_uso: '',
    costo: '',
    kilometros_recorridos: '',
    observaciones: ''
  });
  
  // Formulario de mano de obra
  const [formManoObra, setFormManoObra] = useState({
    descripcion: '',
    cantidad_personas: 1,
    proveedor: '',
    costo_total: '',
    horas_trabajo: '',
    observaciones: ''
  });

  // Formulario de maquinaria alquilada
  const [formMaquinariaAlquilada, setFormMaquinariaAlquilada] = useState({
    descripcion: '',
    proveedor: '',
    costo: '',
    observaciones: ''
  });
  const [searchTerm, setSearchTerm] = useState('');
  const [filterEstado, setFilterEstado] = useState('todos');
  const [filterLote, setFilterLote] = useState<number | 'todos'>('todos');
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState<Labor>({
    tipo: '',
    fecha: '',
    observaciones: '',
    lote_id: 0,
    lote_nombre: '',
    estado: 'planificada',
    insumos_usados: [],
    maquinaria_asignada: [],
    responsable: ''
  });

  // Estados para manejar insumos y maquinaria en el formulario
  const [selectedInsumos, setSelectedInsumos] = useState<InsumoUsado[]>([]);
  const [selectedMaquinaria, setSelectedMaquinaria] = useState<MaquinariaAsignada[]>([]);
  const [selectedManoObra, setSelectedManoObra] = useState<LaborManoObra[]>([]);
  const [showInsumosModal, setShowInsumosModal] = useState(false);
  const [showMaquinariaModal, setShowMaquinariaModal] = useState(false);
  
  // Estados para los modales de selección individual
  const [selectedInsumoId, setSelectedInsumoId] = useState<number>(0);
  const [selectedMaquinariaId, setSelectedMaquinariaId] = useState<number>(0);
  const [insumoCantidad, setInsumoCantidad] = useState<number>(0);
  const [maquinariaHoras, setMaquinariaHoras] = useState<number>(0);
  const [maquinariaKilometros, setMaquinariaKilometros] = useState<number>(0);
  
  // Estado para mensaje informativo cuando se selecciona un tipo de labor con agroquímicos
  const [mensajeAgroquimicos, setMensajeAgroquimicos] = useState<string>('');
  
  // Estado para información de dosis calculada en el modal de insumos
  const [dosisCalculada, setDosisCalculada] = useState<{
    dosisRecomendadaPorHa: number;
    cantidadNecesaria: number;
    hectareas: number;
    mensaje: string;
  } | null>(null);
  
  // Estado para almacenar las dosis disponibles del agroquímico seleccionado
  const [dosisDisponibles, setDosisDisponibles] = useState<any[]>([]);
  const [dosisSeleccionada, setDosisSeleccionada] = useState<number | null>(null);

  // Tipos de labor disponibles (se filtrarán según el estado del lote)
  const [tiposLaborDisponibles, setTiposLaborDisponibles] = useState<string[]>([]);
  const [loteEstado, setLoteEstado] = useState<string>('');
  
  // Todos los tipos de labor posibles
  const todosLosTiposLabor = [
    'siembra', 'fertilizacion', 'cosecha', 'riego', 'pulverizacion',
    'arado', 'rastra', 'desmalezado', 'aplicacion_herbicida',
    'aplicacion_insecticida', 'monitoreo', 'otro'
  ];

  const estadosLabor = [
    { value: 'planificada', label: 'Planificada', color: '#6b7280' },
    { value: 'en_progreso', label: 'En Progreso', color: '#3b82f6' },
    { value: 'completada', label: 'Completada', color: '#10b981' },
    { value: 'interrumpida', label: 'Interrumpida', color: '#f59e0b' },
    { value: 'cancelada', label: 'Cancelada', color: '#ef4444' }
  ];

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
      try {
        const lotesData = await lotesService.listar();
        const lotesMapeados: Lote[] = (Array.isArray(lotesData) ? lotesData : []).map((lote: any) => ({
          id: lote.id,
          nombre: lote.nombre,
          superficie: lote.areaHectareas || 0,
          cultivo: lote.cultivoActual || '',
          estado: lote.estado // ← AGREGADO: campo estado del lote
        }));
        setLotes(lotesMapeados);
      } catch (error) {
        console.error('Error cargando lotes:', error);
      }

      // Cargar insumos
      try {
        const insumosData = await insumosService.listar();
        const insumosMapeados: Insumo[] = (Array.isArray(insumosData) ? insumosData : []).map((insumo: any) => ({
          id: insumo.id,
          nombre: insumo.nombre,
          tipo: insumo.tipo,
          stock_actual: insumo.stockActual || 0,
          unidad_medida: insumo.unidadMedida || '',
          precio_unitario: insumo.precioUnitario || 0
        }));
        setInsumos(insumosMapeados);
      } catch (error) {
        console.error('Error cargando insumos:', error);
      }

      // Cargar maquinaria
      try {
        const maquinariaData = await maquinariaService.listar();
        const maquinariaMapeada: Maquinaria[] = (Array.isArray(maquinariaData) ? maquinariaData : []).map((maq: any) => ({
          id: maq.id,
          nombre: maq.nombre,
          tipo: maq.tipo,
          estado: maq.estado,
          kilometros_uso: maq.kilometrosUso || 0,
          costo_por_hora: maq.costoPorHora || 0
        }));
        setMaquinaria(maquinariaMapeada);
      } catch (error) {
        console.error('Error cargando maquinaria:', error);
      }

      // Cargar labores usando servicio offline
      try {
        const laboresData = await offlineService.getLabores();
        if (laboresData) {
          // Filtrar solo labores activas (no eliminadas)
          const laboresActivas = laboresData.filter((labor: any) => labor.activo !== false);
          const laboresMapeadas: Labor[] = laboresActivas.map((labor: any) => ({
            id: labor.id,
            tipo: labor.tipo || '',
            fecha: labor.fechaInicio || '',
            fecha_fin: labor.fechaFin || '',
            observaciones: labor.observaciones || '',
            lote_id: labor.loteId || 0,
            lote_nombre: labor.loteNombre || '',
            estado: mapEstadoFromBackend(labor.estado || 'PLANIFICADA'),
            maquinaria_asignada: labor.maquinariaAsignada || [],
            responsable: labor.responsable || '',
            horas_trabajo: labor.horasTrabajo || 0,
            costo_total: labor.costoTotal || 0,
            // Nuevos campos para costos detallados - mapeo correcto del backend
            costo_base: labor.costoBase || labor.costoInsumos || 0, // Usar costoInsumos si costoBase no está disponible
            costo_maquinaria: labor.costoMaquinaria || 0,
            costo_mano_obra: labor.costoManoObra || 0,
            maquinarias: labor.maquinarias || [],
            mano_obra: (labor.manoObra || []).map((mo: any) => ({
              id_labor_mano_obra: mo.idLaborManoObra || mo.id_labor_mano_obra,
              id_labor: mo.idLabor || mo.id_labor,
              descripcion: mo.descripcion,
              cantidad_personas: mo.cantidadPersonas || mo.cantidad_personas || 1, // Asegurar que al menos sea 1
              proveedor: mo.proveedor,
              costo_total: mo.costoTotal || mo.costo_total || 0,
              horas_trabajo: mo.horasTrabajo || mo.horas_trabajo,
              observaciones: mo.observaciones
            })),
            insumos_usados: (labor.insumosUsados || []).map((ins: any) => ({
              insumo_id: ins.idInsumo || ins.insumo_id || ins.id_insumo,
              insumo_nombre: ins.insumoNombre || ins.insumo_nombre || ins.nombre || 'Insumo sin nombre',
              cantidad_usada: ins.cantidadUsada || ins.cantidad_usada || ins.cantidad || 0,
              cantidad_planificada: ins.cantidadPlanificada || ins.cantidad_planificada || ins.cantidad_usada || 0,
              unidad_medida: ins.unidadMedida || ins.unidad_medida || ins.unidad || '',
              costo_unitario: ins.costoUnitario || ins.costo_unitario || ins.precio_unitario || 0,
              costo_total: ins.costoTotal || ins.costo_total || (ins.cantidadUsada || ins.cantidad_usada || ins.cantidad || 0) * (ins.costoUnitario || ins.costo_unitario || ins.precio_unitario || 0)
            }))
          }));
          setLabores(laboresMapeadas);
        }
      } catch (error) {
        console.error('Error cargando labores:', error);
      }

    } catch (error) {
      console.error('Error cargando datos:', error);
      alert('Error al cargar los datos. Verifica la conexión con el servidor.');
    } finally {
      setLoading(false);
    }
  };

  // Función para cargar tareas disponibles según el estado del lote
  const cargarTareasDisponibles = async (estadoLote: string) => {
    if (!estadoLote) {
      console.log('❌ No hay estado de lote, usando todas las tareas');
      setTiposLaborDisponibles(todosLosTiposLabor);
      return;
    }

    try {
      console.log('🔍 Cargando tareas disponibles para estado:', estadoLote);
      const data = await laboresService.obtenerTareasDisponibles(estadoLote);
      
      console.log('✅ Tareas recibidas del backend:', data.tareas);
      setTiposLaborDisponibles(data.tareas || []);
    } catch (error) {
      console.error('❌ Error cargando tareas disponibles:', error);
      // Si falla, usar todas las tareas
      setTiposLaborDisponibles(todosLosTiposLabor);
    }
  };

  useEffect(() => {
    loadData();
    // Inicializar con todas las tareas disponibles
    setTiposLaborDisponibles(todosLosTiposLabor);
  }, []);

  // Detectar si es móvil
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth <= 768);
    };
    
    checkMobile();
    window.addEventListener('resize', checkMobile);
    
    return () => window.removeEventListener('resize', checkMobile);
  }, []);

  // Funciones para manejar el formulario
  const resetForm = () => {
    setFormData({
      tipo: '',
      fecha: '',
      observaciones: '',
      lote_id: 0,
      lote_nombre: '',
      estado: 'planificada',
      insumos_usados: [],
      maquinaria_asignada: [],
      responsable: ''
    });
    setSelectedInsumos([]);
    setSelectedMaquinaria([]);
    setSelectedManoObra([]);
    setSelectedInsumoId(0);
    setSelectedMaquinariaId(0);
    setInsumoCantidad(0);
    setMaquinariaHoras(0);
    setMaquinariaKilometros(0);
    setEditingLabor(null);
    setMensajeAgroquimicos('');
    
    // Limpiar formularios de maquinaria
    setFormMaquinaria({
      descripcion: '',
      proveedor: '',
      horas_uso: '',
      costo: '',
      kilometros_recorridos: '',
      observaciones: ''
    });
    setFormMaquinariaAlquilada({
      descripcion: '',
      proveedor: '',
      costo: '',
      observaciones: ''
    });
  };

  const handleOpenAddModal = () => {
    resetForm();
    setShowForm(true);
  };

  const handleOpenInsumosModal = () => {
    setSelectedInsumoId(0);
    setInsumoCantidad(0);
    setDosisCalculada(null);
    setDosisDisponibles([]);
    setDosisSeleccionada(null);
    setShowInsumosModal(true);
  };
  
  // Función para calcular cantidad desde una dosis seleccionada
  const calcularCantidadDesdeDosis = (dosis: any, insumo: Insumo) => {
    console.log('📊 Calculando cantidad desde dosis:', dosis);
    const lote = lotes.find(l => l.id === formData.lote_id);
    
    // Manejar tanto camelCase como snake_case
    const dosisRecomendadaPorHa = dosis.dosisRecomendadaPorHa || dosis.dosis_recomendada_por_ha || 0;
    const tipoAplicacion = dosis.tipoAplicacion || dosis.tipo_aplicacion || '';
    const formaAplicacion = dosis.formaAplicacion || dosis.forma_aplicacion || '';
    const unidad = dosis.unidad || '';
    
    console.log('📊 Datos extraídos de dosis:');
    console.log('  - dosisRecomendadaPorHa:', dosisRecomendadaPorHa);
    console.log('  - tipoAplicacion:', tipoAplicacion);
    console.log('  - formaAplicacion:', formaAplicacion);
    console.log('  - unidad:', unidad);
    
    if (lote && lote.superficie > 0 && dosisRecomendadaPorHa > 0) {
      const cantidadNecesaria = lote.superficie * dosisRecomendadaPorHa;
      console.log('📊 Cálculo automático:');
      console.log('  - Hectáreas:', lote.superficie);
      console.log('  - Dosis recomendada por ha:', dosisRecomendadaPorHa);
      console.log('  - Cantidad necesaria:', cantidadNecesaria);
      
      // Prellenar la cantidad
      setInsumoCantidad(cantidadNecesaria);
      
      // Formatear tipo y forma de aplicación para el mensaje
      const tipoAplicacionFormateado = tipoAplicacion ? tipoAplicacion.replace(/_/g, ' ').toLowerCase() : '';
      const formaAplicacionFormateado = formaAplicacion ? formaAplicacion.replace(/_/g, ' ').toLowerCase() : '';
      const unidadFormateado = unidad ? unidad.replace(/_/g, ' ') : '';
      
      // Mostrar mensaje informativo
      setDosisCalculada({
        dosisRecomendadaPorHa,
        cantidadNecesaria,
        hectareas: lote.superficie,
        mensaje: `Cantidad calculada: ${cantidadNecesaria.toFixed(2)} ${insumo.unidad_medida} (${lote.superficie} ha × ${dosisRecomendadaPorHa} ${unidadFormateado}/ha)${tipoAplicacionFormateado ? ` - Tipo: ${tipoAplicacionFormateado}` : ''}${formaAplicacionFormateado ? `, Forma: ${formaAplicacionFormateado}` : ''}`
      });
    } else {
      console.error('❌ Error en cálculo: lote o dosis inválida');
      console.error('  - Lote:', lote);
      console.error('  - Superficie del lote:', lote?.superficie);
      console.error('  - Dosis recomendada:', dosisRecomendadaPorHa);
    }
  };

  const handleOpenMaquinariaModal = () => {
    setSelectedMaquinariaId(0);
    setMaquinariaHoras(0);
    setMaquinariaKilometros(0);
    setShowMaquinariaModal(true);
  };

  const handleOpenEditModal = async (labor: Labor) => {
    // Mapear el tipo de labor del backend al frontend
    const laborMapeada = {
      ...labor,
      tipo: mapTipoLaborFromBackend(labor.tipo)
    };
    
    setFormData(laborMapeada);
    
    // Cargar el estado del lote y las tareas disponibles
    const lote = lotes.find(l => l.id === labor.lote_id);
    if (lote && lote.estado) {
      console.log('🔄 [handleOpenEditModal] Cargando tareas para lote en estado:', lote.estado);
      setLoteEstado(lote.estado);
      await cargarTareasDisponibles(lote.estado);
    } else {
      console.log('⚠️ [handleOpenEditModal] Lote sin estado, limpiando tareas');
      setLoteEstado('');
      setTiposLaborDisponibles([]);
    }
    
    // Mapear insumos del formato del backend al formato del frontend
    const insumosMapeados = (labor.insumos_usados || []).map((insumo: any) => ({
      insumo_id: insumo.insumo_id || insumo.id,
      insumo_nombre: insumo.insumo_nombre || insumo.nombre,
      cantidad_usada: insumo.cantidad_usada || insumo.cantidad,
      cantidad_planificada: insumo.cantidad_planificada || insumo.cantidad,
      unidad_medida: insumo.unidad_medida || insumo.unidad,
      costo_unitario: insumo.costo_unitario || insumo.precio_unitario,
      costo_total: insumo.costo_total || (insumo.cantidad * insumo.precio_unitario)
    }));
    
    setSelectedInsumos(insumosMapeados);
    
    // Mapear maquinarias del formato del backend al formato del frontend
    const maquinariasMapeadas = (labor.maquinarias || []).map((maq: any) => ({
      maquinaria_id: maq.id_labor_maquinaria,
      maquinaria_nombre: maq.descripcion, // El frontend espera maquinaria_nombre
      descripcion: maq.descripcion,
      proveedor: maq.proveedor,
      costo_total: maq.costo, // Mapear costo a costo_total
      horas_uso: 0, // No se almacena en el backend
      kilometros_recorridos: 0 // No se almacena en el backend
    }));
    
    // Mapear mano de obra del formato del backend al formato del frontend
    const manoObraMapeada = (labor.mano_obra || []).map((mo: any) => ({
      id_labor_mano_obra: mo.idLaborManoObra || mo.id_labor_mano_obra,
      id_labor: mo.idLabor || mo.id_labor,
      descripcion: mo.descripcion,
      cantidad_personas: mo.cantidadPersonas || mo.cantidad_personas,
      proveedor: mo.proveedor,
      costo_total: mo.costoTotal || mo.costo_total || 0,
      horas_trabajo: mo.horasTrabajo || mo.horas_trabajo,
      observaciones: mo.observaciones
    }));
    
    setSelectedMaquinaria(maquinariasMapeadas);
    setSelectedManoObra(manoObraMapeada);
    setEditingLabor(labor);
    setShowForm(true);
  };

  const handleVerDetallesCostos = (labor: Labor) => {
    setLaborSeleccionada(labor);
    setShowDetallesCostos(true);
  };

  // Funciones para manejar formularios de maquinaria
  const handleAgregarMaquinaria = (labor: Labor) => {
    setLaborParaMaquinaria(labor);
    setFormMaquinaria({
      descripcion: '',
      proveedor: '',
      horas_uso: '',
      costo: '',
      kilometros_recorridos: '',
      observaciones: ''
    });
    setShowFormMaquinaria(true);
  };

  const handleGuardarMaquinaria = () => {
    if (!formMaquinaria.descripcion || !formMaquinaria.horas_uso || !formMaquinaria.costo) {
      alert('Por favor complete la descripción, horas de uso y verifique que el costo se haya calculado');
      return;
    }

    const nuevaMaquinaria: LaborMaquinaria = {
      id_labor_maquinaria: Date.now(), // ID temporal
      id_labor: 0, // Se asignará cuando se guarde la labor
      descripcion: formMaquinaria.descripcion,
      proveedor: undefined, // Maquinaria propia no tiene proveedor
      costo: parseFloat(formMaquinaria.costo) || 0,
      observaciones: formMaquinaria.observaciones || undefined
    };

    // Agregar a la lista de maquinaria seleccionada
    const maquinariaAsignada: MaquinariaAsignada = {
      maquinaria_id: nuevaMaquinaria.id_labor_maquinaria,
      maquinaria_nombre: nuevaMaquinaria.descripcion,
      costo_total: nuevaMaquinaria.costo
    };

    setSelectedMaquinaria(prev => [...prev, maquinariaAsignada]);
    
    // Limpiar formulario
    setFormMaquinaria({
      descripcion: '',
      proveedor: '',
      horas_uso: '',
      costo: '',
      kilometros_recorridos: '',
      observaciones: ''
    });
    
    setShowFormMaquinaria(false);
  };

  const handleGuardarMaquinariaAlquilada = () => {
    if (!formMaquinariaAlquilada.descripcion || !formMaquinariaAlquilada.costo || !formMaquinariaAlquilada.proveedor) {
      alert('Por favor complete la descripción, proveedor y costo');
      return;
    }

    const nuevaMaquinaria: LaborMaquinaria = {
      id_labor_maquinaria: Date.now(), // ID temporal
      id_labor: 0, // Se asignará cuando se guarde la labor
      descripcion: formMaquinariaAlquilada.descripcion,
      proveedor: formMaquinariaAlquilada.proveedor,
      costo: parseFloat(formMaquinariaAlquilada.costo) || 0,
      observaciones: formMaquinariaAlquilada.observaciones || undefined
    };

    // Agregar a la lista de maquinaria seleccionada
    const maquinariaAsignada: MaquinariaAsignada = {
      maquinaria_id: nuevaMaquinaria.id_labor_maquinaria,
      maquinaria_nombre: `${nuevaMaquinaria.descripcion} (${nuevaMaquinaria.proveedor})`,
      costo_total: nuevaMaquinaria.costo
    };

    setSelectedMaquinaria(prev => [...prev, maquinariaAsignada]);
    
    // Limpiar formulario
    setFormMaquinariaAlquilada({
      descripcion: '',
      proveedor: '',
      costo: '',
      observaciones: ''
    });
    
    setShowFormMaquinariaAlquilada(false);
  };

  // Funciones para manejar formularios de mano de obra
  const handleAgregarManoObra = () => {
    setFormManoObra({
      descripcion: '',
      cantidad_personas: 1,
      proveedor: '',
      costo_total: '',
      horas_trabajo: '',
      observaciones: ''
    });
    setShowFormManoObra(true);
  };

  const handleGuardarManoObra = () => {
    if (!formManoObra.descripcion || !formManoObra.costo_total) {
      alert('Por favor complete la descripción y el costo total');
      return;
    }

    const nuevaManoObra: LaborManoObra = {
      id_labor_mano_obra: Date.now(), // ID temporal
      id_labor: 0, // Se asignará cuando se guarde la labor
      descripcion: formManoObra.descripcion,
      cantidad_personas: formManoObra.cantidad_personas,
      proveedor: formManoObra.proveedor || undefined,
      costo_total: parseFloat(formManoObra.costo_total) || 0,
      horas_trabajo: formManoObra.horas_trabajo ? parseFloat(formManoObra.horas_trabajo) : undefined,
      observaciones: formManoObra.observaciones || undefined
    };

    addManoObra(nuevaManoObra);
    
    // Limpiar formulario
    setFormManoObra({
      descripcion: '',
      cantidad_personas: 1,
      proveedor: '',
      costo_total: '',
      horas_trabajo: '',
      observaciones: ''
    });
    
    setShowFormManoObra(false);
  };

  const handleCloseModal = () => {
    setShowForm(false);
    resetForm();
  };

  const handleInputChange = (field: keyof Labor, value: any) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
    
    // Si se cambia el tipo de labor, verificar si involucra agroquímicos
    if (field === 'tipo' && value) {
      console.log('📋 Tipo de labor seleccionado:', value);
      
      // Tipos de labor que típicamente involucran agroquímicos
      const tiposConAgroquimicos = [
        'fertilizacion',
        'pulverizacion',
        'aplicacion_herbicida',
        'aplicacion_insecticida',
        'control_plagas',
        'control_malezas'
      ];
      
      if (tiposConAgroquimicos.includes(value.toLowerCase())) {
        console.log('🦠 Tipo de labor que puede involucrar agroquímicos:', value);
        setMensajeAgroquimicos(
          '💡 Este tipo de labor puede involucrar agroquímicos. Al agregar insumos, el sistema calculará automáticamente las dosis recomendadas basadas en las hectáreas del lote y validará que el stock sea suficiente.'
        );
      } else {
        setMensajeAgroquimicos('');
      }
    }
  };

  const handleLoteChange = async (loteId: number) => {
    console.log('🔄 handleLoteChange llamado con loteId:', loteId);
    const lote = lotes.find(l => l.id === loteId);
    console.log('📦 Lote encontrado:', lote);
    console.log('📍 Estado del lote:', lote?.estado);
    
    setFormData(prev => ({
      ...prev,
      lote_id: loteId,
      lote_nombre: lote?.nombre || '',
      tipo: '' // Resetear tipo de labor cuando cambia el lote
    }));
    
    // Cargar tareas disponibles según el estado del lote
    if (lote && lote.estado) {
      console.log('✅ Lote tiene estado, cargando tareas para:', lote.estado);
      setLoteEstado(lote.estado);
      await cargarTareasDisponibles(lote.estado);
    } else {
      console.log('❌ Lote NO tiene estado, usando todas las tareas');
      setLoteEstado('');
      setTiposLaborDisponibles(todosLosTiposLabor);
    }
  };

  const saveLabor = async () => {
    if (!formData.tipo || !formData.fecha || !formData.lote_id || !formData.responsable) {
      alert('Por favor complete todos los campos obligatorios');
      return;
    }

    try {
      setLoading(true);
      
      const token = localStorage.getItem('token');
      if (!token) {
        alert('Error de autenticación. Por favor, inicia sesión nuevamente.');
        return;
      }

      // Crear labor con insumos, maquinaria y mano de obra
      console.log('Tipo original:', formData.tipo);
      console.log('Tipo mapeado:', mapTipoLaborToBackend(formData.tipo));
      
      // Transformar maquinaria al formato que espera el backend
      const maquinariaTransformada = selectedMaquinaria.map(maq => {
        // Determinar si es propia o alquilada basado en si tiene proveedor
        const esAlquilada = maq.proveedor && maq.proveedor.trim() !== '';
        return {
          descripcion: maq.maquinaria_nombre,
          proveedor: esAlquilada ? maq.proveedor : null,
          costo: maq.costo_total,
          observaciones: null
        };
      });
      
      // Transformar mano de obra al formato que espera el backend
      const manoObraTransformada = selectedManoObra.map(mo => ({
        descripcion: mo.descripcion || 'Mano de obra',
        cantidad_personas: mo.cantidad_personas || 1,
        proveedor: mo.proveedor || null,
        costo_total: mo.costo_total || 0,
        horas_trabajo: mo.horas_trabajo || null,
        observaciones: mo.observaciones || null
      }));
      
      const laborCompleta = {
        // Mapear campos del frontend al backend
        tipoLabor: mapTipoLaborToBackend(formData.tipo), // Mapear tipo al formato del backend
        descripcion: formData.observaciones || '',
        fechaInicio: formData.fecha, // Frontend: fecha -> Backend: fechaInicio
        fechaFin: formData.fecha_fin || null,
        estado: mapEstadoToBackend(formData.estado), // Mapear estado al formato del backend
        responsable: formData.responsable || '',
        horasTrabajo: formData.horas_trabajo || 0,
        costoTotal: calcularCostoTotal(),
        lote: formData.lote_id ? { id: formData.lote_id } : null,
        // Campos adicionales del frontend - transformados al formato correcto
        insumosUsados: selectedInsumos,
        maquinariaAsignada: maquinariaTransformada,
        manoObra: manoObraTransformada
      };

      console.log('Datos a enviar al backend:', laborCompleta);
      console.log('Maquinaria seleccionada:', selectedMaquinaria);
      console.log('Maquinaria transformada:', maquinariaTransformada);
      console.log('Mano de obra seleccionada:', selectedManoObra);
      console.log('Mano de obra transformada:', manoObraTransformada);

      if (editingLabor) {
        // Editar labor existente
        await laboresService.actualizar(editingLabor.id!, laborCompleta);
      } else {
        // Crear nueva labor
        await laboresService.crear(laborCompleta);
      }

      alert(editingLabor ? 'Labor actualizada exitosamente' : 'Labor creada exitosamente');
      // Invalidar caché de labores para forzar recarga fresca
      offlineService.remove('labores');
      handleCloseModal();
      loadData(); // Recargar datos desde el backend
    } catch (error) {
      console.error('Error:', error);
      alert('Error al guardar la labor');
    } finally {
      setLoading(false);
    }
  };

  const deleteLabor = async (id: number) => {
    if (window.confirm('¿Está seguro de que desea eliminar esta labor?')) {
      try {
        setLoading(true);
        
        const token = localStorage.getItem('token');
        if (!token) {
          alert('Error de autenticación. Por favor, inicia sesión nuevamente.');
          return;
        }

        await laboresService.eliminar(id);
        
        // Invalidar caché de labores para forzar recarga fresca
        offlineService.remove('labores');
        // Eliminar del estado local solo si la API confirma la eliminación
        setLabores(prev => prev.filter(l => l.id !== id));
        alert('Labor eliminada exitosamente');
      } catch (error) {
        console.error('Error de conexión al eliminar la labor:', error);
        alert('Error de conexión al eliminar la labor. Por favor, verifica tu conexión e intenta nuevamente.');
      } finally {
        setLoading(false);
      }
    }
  };

  // Funciones para manejar insumos
  const addInsumo = (insumo: Insumo, cantidad: number) => {
    // Verificar stock disponible
    const stockDisponible = insumo.stock_actual;
    const cantidadYaUsada = selectedInsumos
      .filter(i => i.insumo_id === insumo.id)
      .reduce((sum, i) => sum + i.cantidad_usada, 0);
    
    const stockRealDisponible = stockDisponible - cantidadYaUsada;
    
    if (cantidad > stockRealDisponible) {
      alert(`No hay suficiente stock disponible. Stock disponible: ${stockRealDisponible} ${insumo.unidad_medida}`);
      return false;
    }
    
    const insumoExistente = selectedInsumos.find(i => i.insumo_id === insumo.id);
    
    if (insumoExistente) {
      // Actualizar cantidad si ya existe
      setSelectedInsumos(prev => prev.map(i => 
        i.insumo_id === insumo.id 
          ? { ...i, cantidad_usada: cantidad, costo_total: cantidad * i.costo_unitario }
          : i
      ));
    } else {
      // Agregar nuevo insumo
      const nuevoInsumo: InsumoUsado = {
        insumo_id: insumo.id,
        insumo_nombre: insumo.nombre,
        cantidad_usada: cantidad,
        cantidad_planificada: cantidad,
        unidad_medida: insumo.unidad_medida,
        costo_unitario: insumo.precio_unitario,
        costo_total: cantidad * insumo.precio_unitario
      };
      setSelectedInsumos(prev => [...prev, nuevoInsumo]);
    }
    return true;
  };

  const removeInsumo = (insumoId: number) => {
    setSelectedInsumos(prev => prev.filter(i => i.insumo_id !== insumoId));
  };

  const updateInsumoCantidad = (insumoId: number, cantidad: number) => {
    setSelectedInsumos(prev => prev.map(i => 
      i.insumo_id === insumoId 
        ? { ...i, cantidad_usada: cantidad, costo_total: cantidad * i.costo_unitario }
        : i
    ));
  };

  // Funciones para manejar maquinaria
  const addMaquinaria = (maq: Maquinaria, horas: number, kilometros: number) => {
    const maqExistente = selectedMaquinaria.find(m => m.maquinaria_id === maq.id);
    
    if (maqExistente) {
      // Actualizar si ya existe
      setSelectedMaquinaria(prev => prev.map(m => 
        m.maquinaria_id === maq.id 
          ? { ...m, costo_total: horas * maq.costo_por_hora }
          : m
      ));
    } else {
      // Agregar nueva maquinaria
      const nuevaMaquinaria: MaquinariaAsignada = {
        maquinaria_id: maq.id,
        maquinaria_nombre: maq.nombre,
        costo_total: horas * maq.costo_por_hora
      };
      setSelectedMaquinaria(prev => [...prev, nuevaMaquinaria]);
    }
  };

  const removeMaquinaria = (maquinariaId: number) => {
    setSelectedMaquinaria(prev => prev.filter(m => m.maquinaria_id !== maquinariaId));
  };

  // Funciones para manejar mano de obra en el formulario
  const addManoObra = (manoObra: LaborManoObra) => {
    setSelectedManoObra(prev => [...prev, manoObra]);
  };

  const removeManoObra = (index: number) => {
    setSelectedManoObra(prev => prev.filter((_, i) => i !== index));
  };

  const updateMaquinariaUso = (maquinariaId: number, horas: number, kilometros: number) => {
    const maq = maquinaria.find(m => m.id === maquinariaId);
    if (maq) {
      setSelectedMaquinaria(prev => prev.map(m => 
        m.maquinaria_id === maquinariaId 
          ? { ...m, costo_total: horas * maq.costo_por_hora }
          : m
      ));
    }
  };

  // Calcular costo total de la labor
  const calcularCostoTotal = () => {
    const costoInsumos = selectedInsumos.reduce((sum, insumo) => sum + insumo.costo_total, 0);
    const costoMaquinaria = selectedMaquinaria.reduce((sum, maq) => sum + maq.costo_total, 0);
    const costoManoObra = selectedManoObra.reduce((sum, mo) => sum + mo.costo_total, 0);
    return costoInsumos + costoMaquinaria + costoManoObra;
  };

  // Actualizar stock de insumos
  const actualizarStockInsumos = () => {
    selectedInsumos.forEach(insumoUsado => {
      const insumo = insumos.find(i => i.id === insumoUsado.insumo_id);
      if (insumo) {
        const nuevoStock = insumo.stock_actual - insumoUsado.cantidad_usada;
        setInsumos(prev => prev.map(i => 
          i.id === insumo.id ? { ...i, stock_actual: Math.max(0, nuevoStock) } : i
        ));
      }
    });
  };

  // Actualizar uso de maquinaria
  const actualizarUsoMaquinaria = () => {
    // Actualizar uso de maquinaria (sin kilómetros)
    selectedMaquinaria.forEach(maqUsada => {
      const maq = maquinaria.find(m => m.id === maqUsada.maquinaria_id);
      if (maq) {
        // Solo actualizar horas de uso si es necesario
        // Los kilómetros ya no se manejan
      }
    });
  };

  // Filtrar labores
  const filteredLabores = labores
    .filter(labor => {
      const matchesSearch = (labor.tipo || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
                           (labor.lote_nombre || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
                           (labor.responsable || '').toLowerCase().includes(searchTerm.toLowerCase());
      const matchesEstado = filterEstado === 'todos' || labor.estado === filterEstado;
      const matchesLote = filterLote === 'todos' || labor.lote_id === filterLote;
      return matchesSearch && matchesEstado && matchesLote;
    })
    .sort((a, b) => {
      // Ordenar por fecha (más nuevo a más viejo)
      const fechaA = new Date(a.fecha || 0).getTime();
      const fechaB = new Date(b.fecha || 0).getTime();
      return fechaB - fechaA; // Orden descendente (más nuevo primero)
    });

  // Función para obtener labores paginadas
  const obtenerLaboresPaginadas = () => {
    const totalPaginas = Math.ceil(filteredLabores.length / elementosPorPagina);
    const inicio = (paginaActual - 1) * elementosPorPagina;
    const fin = inicio + elementosPorPagina;
    const laboresPaginadas = filteredLabores.slice(inicio, fin);
    
    return { laboresPaginadas, totalPaginas };
  };

  // Resetear paginación cuando cambien los filtros
  useEffect(() => {
    setPaginaActual(1);
  }, [searchTerm, filterEstado, filterLote]);

  return (
    <div style={{ 
      padding: isMobile ? '10px' : '20px', 
      fontFamily: 'Arial, sans-serif' 
    }}>
      <div style={{ 
        background: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)', 
        color: 'white', 
        padding: isMobile ? '15px' : '20px', 
        borderRadius: '10px', 
        marginBottom: isMobile ? '15px' : '20px' 
      }}>
        <h1 style={{ 
          margin: '0 0 10px 0', 
          fontSize: isMobile ? '20px' : '24px' 
        }}>
          🔧 Gestión de Labores
        </h1>
        <p style={{ 
          margin: '0', 
          opacity: '0.9',
          fontSize: isMobile ? '14px' : '16px'
        }}>
          Administra las labores agrícolas y su consumo de insumos
        </p>
      </div>

      <div style={{ 
        background: '#f3f4f6', 
        padding: isMobile ? '15px' : '20px', 
        borderRadius: '10px', 
        marginBottom: isMobile ? '15px' : '20px',
        border: '1px solid #e5e7eb'
      }}>
        <h3 style={{ 
          margin: '0 0 15px 0', 
          color: '#374151',
          fontSize: isMobile ? '16px' : '18px'
        }}>
          📊 Resumen de Labores
        </h3>
        <div style={{ 
          display: 'grid', 
          gridTemplateColumns: isMobile ? 'repeat(2, 1fr)' : 'repeat(auto-fit, minmax(200px, 1fr))', 
          gap: isMobile ? '10px' : '15px' 
        }}>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#f59e0b' }}>
              {labores.length}
            </div>
            <div style={{ fontSize: '14px', color: '#6b7280' }}>Total Labores</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#3b82f6' }}>
              {labores.filter((l: Labor) => l.estado === 'en_progreso').length}
            </div>
            <div style={{ fontSize: '14px', color: '#6b7280' }}>En Progreso</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#10b981' }}>
              {labores.filter((l: Labor) => l.estado === 'completada').length}
            </div>
            <div style={{ fontSize: '14px', color: '#6b7280' }}>Completadas</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#ef4444' }}>
              {formatCurrency(labores.reduce((sum: number, l: Labor) => sum + (l.costo_total || 0), 0))}
            </div>
            <div style={{ fontSize: '14px', color: '#6b7280' }}>Costo Total</div>
          </div>
        </div>
      </div>

      {/* Filtros y búsqueda */}
      <div style={{ 
        background: 'white', 
        padding: isMobile ? '15px' : '20px', 
        borderRadius: '10px', 
        marginBottom: isMobile ? '15px' : '20px',
        boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
      }}>
        <div style={{ 
          display: 'flex', 
          gap: isMobile ? '10px' : '15px', 
          flexWrap: 'wrap',
          alignItems: 'center'
        }}>
          <input
            type="text"
            placeholder="Buscar labores..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{
              flex: 1,
              minWidth: '200px',
              padding: '10px',
              border: '1px solid #d1d5db',
              borderRadius: '8px',
              fontSize: '14px'
            }}
          />
          <select
            value={filterEstado}
            onChange={(e) => setFilterEstado(e.target.value)}
            style={{
              padding: '10px',
              border: '1px solid #d1d5db',
              borderRadius: '8px',
              fontSize: '14px',
              minWidth: '150px'
            }}
          >
            <option value="todos">Todos los estados</option>
            {estadosLabor.map(estado => (
              <option key={estado.value} value={estado.value}>
                {estado.label}
              </option>
            ))}
          </select>
          <select
            value={filterLote === 'todos' ? 'todos' : filterLote.toString()}
            onChange={(e) => setFilterLote(e.target.value === 'todos' ? 'todos' : Number(e.target.value))}
            style={{
              padding: '10px',
              border: '1px solid #d1d5db',
              borderRadius: '8px',
              fontSize: '14px',
              minWidth: '180px'
            }}
          >
            <option value="todos">Todos los lotes</option>
            {lotes.map(lote => (
              <option key={lote.id} value={lote.id}>
                {lote.nombre} {lote.cultivo ? `(${lote.cultivo})` : ''}
              </option>
            ))}
          </select>
          <PermissionGate permission="canCreateLabores">
            <button
              onClick={handleOpenAddModal}
              style={{
                background: '#f59e0b',
                color: 'white',
                border: 'none',
                padding: '10px 20px',
                borderRadius: '8px',
                cursor: 'pointer',
                fontSize: '14px',
                fontWeight: 'bold'
              }}
            >
              ➕ {isMobile ? 'Nueva' : 'Nueva Labor'}
            </button>
          </PermissionGate>
        </div>
      </div>

      <div style={{ 
        background: 'white', 
        borderRadius: '10px', 
        overflow: 'hidden',
        boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
      }}>
        <div style={{ 
          background: '#f8f9fa', 
          padding: '15px', 
          borderBottom: '1px solid #dee2e6',
          fontWeight: 'bold'
        }}>
          🔧 Labores Registradas ({filteredLabores.length})
        </div>
        
        {loading ? (
          <div style={{ padding: '40px', textAlign: 'center', color: '#666' }}>
            🔄 Cargando labores...
          </div>
        ) : filteredLabores.length === 0 ? (
          <div style={{ padding: '40px', textAlign: 'center', color: '#666' }}>
            {searchTerm || filterEstado !== 'todos' ? 'No se encontraron labores con los filtros aplicados' : 'No hay labores registradas'}
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ 
              width: '100%', 
              borderCollapse: 'collapse',
              fontSize: '14px'
            }}>
              <thead>
                <tr style={{ background: '#f8f9fa' }}>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Labor</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Lote</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Fecha</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Estado</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Responsable</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Costo Base</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Maquinaria</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Mano de Obra</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Total</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {(() => {
                  const { laboresPaginadas, totalPaginas } = obtenerLaboresPaginadas();
                  
                  if (filteredLabores.length === 0) {
                    return (
                      <tr>
                        <td colSpan={8} style={{ padding: '2rem', textAlign: 'center', color: '#6b7280' }}>
                          {searchTerm || filterEstado !== 'todos' ? 'No se encontraron labores que coincidan con los filtros' : 'No hay labores registradas'}
                        </td>
                      </tr>
                    );
                  }

                  return laboresPaginadas.map((labor: Labor) => (
                  <tr key={labor.id} style={{ borderBottom: '1px solid #f1f3f4' }}>
                    <td style={{ padding: '12px' }}>
                      <div>
                        <strong>{labor.tipo.charAt(0).toUpperCase() + labor.tipo.slice(1)}</strong>
                        <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '2px' }}>
                          {labor.observaciones.substring(0, 50)}...
                        </div>
                      </div>
                    </td>
                    <td style={{ padding: '12px' }}>
                      <span style={{ fontWeight: 'bold' }}>{labor.lote_nombre}</span>
                    </td>
                    <td style={{ padding: '12px' }}>
                      {labor.fecha ? new Date(labor.fecha).toLocaleDateString('es-ES') : 'Sin fecha'}
                    </td>
                    <td style={{ padding: '12px' }}>
                      <span style={{
                        padding: '4px 8px',
                        borderRadius: '12px',
                        fontSize: '12px',
                        fontWeight: 'bold',
                        background: '#10b98120',
                        color: '#10b981'
                      }}>
                        {labor.estado.charAt(0).toUpperCase() + labor.estado.slice(1)}
                      </span>
                    </td>
                    <td style={{ padding: '12px' }}>
                      {labor.responsable}
                    </td>
                    <td style={{ padding: '12px' }}>
                      <span style={{ fontWeight: 'bold', color: '#6b7280' }}>
                        {formatCurrency(labor.costo_base || 0)}
                      </span>
                    </td>
                    <td style={{ padding: '12px' }}>
                      <span style={{ fontWeight: 'bold', color: '#3b82f6' }}>
                        {formatCurrency(labor.costo_maquinaria || 0)}
                      </span>
                      {labor.maquinarias && labor.maquinarias.length > 0 && (
                        <div style={{ fontSize: '10px', color: '#6b7280', marginTop: '2px' }}>
                          {labor.maquinarias.length} maquinaria(s)
                        </div>
                      )}
                    </td>
                    <td style={{ padding: '12px' }}>
                      <span style={{ fontWeight: 'bold', color: '#f59e0b' }}>
                        {formatCurrency(labor.costo_mano_obra || 0)}
                      </span>
                      {labor.mano_obra && labor.mano_obra.length > 0 && (
                        <div style={{ fontSize: '10px', color: '#6b7280', marginTop: '2px' }}>
                          {labor.mano_obra.length} registro(s)
                        </div>
                      )}
                    </td>
                    <td style={{ padding: '12px' }}>
                      <span style={{ fontWeight: 'bold', color: '#10b981', fontSize: '16px' }}>
                        {formatCurrency((labor.costo_base || 0) + (labor.costo_maquinaria || 0) + (labor.costo_mano_obra || 0))}
                      </span>
                    </td>
                    <td style={{ padding: '12px' }}>
                      <div style={{ display: 'flex', gap: '5px', flexWrap: 'wrap' }}>
                        {puedeModificarLabor(labor) && (
                          <button
                            onClick={() => handleOpenEditModal(labor)}
                            style={{
                              padding: '4px 8px',
                              background: '#3b82f6',
                              color: 'white',
                              border: 'none',
                              borderRadius: '4px',
                              cursor: 'pointer',
                              fontSize: '12px'
                            }}
                            title="Editar labor"
                          >
                            ✏️
                          </button>
                        )}
                        <button
                          onClick={() => handleVerDetallesCostos(labor)}
                          style={{
                            padding: '4px 8px',
                            background: '#10b981',
                            color: 'white',
                            border: 'none',
                            borderRadius: '4px',
                            cursor: 'pointer',
                            fontSize: '12px'
                          }}
                          title="Ver detalles de costos"
                        >
                          💰
                        </button>
                        {puedeModificarLabor(labor) && (
                          <button
                            onClick={() => deleteLabor(labor.id!)}
                            style={{
                              padding: '4px 8px',
                              background: '#ef4444',
                              color: 'white',
                              border: 'none',
                              borderRadius: '4px',
                              cursor: 'pointer',
                              fontSize: '12px'
                            }}
                            title="Eliminar labor"
                          >
                            🗑️
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ));
                })()}
              </tbody>
            </table>
          </div>
        )}

        {/* Paginación */}
        {(() => {
          const { totalPaginas } = obtenerLaboresPaginadas();
          
          if (totalPaginas > 1) {
            return (
              <div style={{ 
                display: 'flex', 
                justifyContent: 'space-between', 
                alignItems: 'center', 
                padding: '20px',
                borderTop: '1px solid #e5e7eb',
                background: '#f8f9fa'
              }}>
                <div style={{ fontSize: '14px', color: '#6b7280' }}>
                  Mostrando {((paginaActual - 1) * elementosPorPagina) + 1} - {Math.min(paginaActual * elementosPorPagina, filteredLabores.length)} de {filteredLabores.length} labores
                </div>
                
                <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                  <button
                    onClick={() => setPaginaActual(1)}
                    disabled={paginaActual === 1}
                    style={{
                      padding: '8px 12px',
                      border: '1px solid #d1d5db',
                      borderRadius: '6px',
                      background: paginaActual === 1 ? '#f3f4f6' : 'white',
                      color: paginaActual === 1 ? '#9ca3af' : '#374151',
                      cursor: paginaActual === 1 ? 'not-allowed' : 'pointer',
                      fontSize: '14px'
                    }}
                  >
                    ⏮️ Primera
                  </button>
                  
                  <button
                    onClick={() => setPaginaActual(paginaActual - 1)}
                    disabled={paginaActual === 1}
                    style={{
                      padding: '8px 12px',
                      border: '1px solid #d1d5db',
                      borderRadius: '6px',
                      background: paginaActual === 1 ? '#f3f4f6' : 'white',
                      color: paginaActual === 1 ? '#9ca3af' : '#374151',
                      cursor: paginaActual === 1 ? 'not-allowed' : 'pointer',
                      fontSize: '14px'
                    }}
                  >
                    ⬅️ Anterior
                  </button>
                  
                  <span style={{ 
                    padding: '8px 12px', 
                    fontSize: '14px',
                    color: '#374151',
                    fontWeight: 'bold'
                  }}>
                    Página {paginaActual} de {totalPaginas}
                  </span>
                  
                  <button
                    onClick={() => setPaginaActual(paginaActual + 1)}
                    disabled={paginaActual === totalPaginas}
                    style={{
                      padding: '8px 12px',
                      border: '1px solid #d1d5db',
                      borderRadius: '6px',
                      background: paginaActual === totalPaginas ? '#f3f4f6' : 'white',
                      color: paginaActual === totalPaginas ? '#9ca3af' : '#374151',
                      cursor: paginaActual === totalPaginas ? 'not-allowed' : 'pointer',
                      fontSize: '14px'
                    }}
                  >
                    Siguiente ➡️
                  </button>
                  
                  <button
                    onClick={() => setPaginaActual(totalPaginas)}
                    disabled={paginaActual === totalPaginas}
                    style={{
                      padding: '8px 12px',
                      border: '1px solid #d1d5db',
                      borderRadius: '6px',
                      background: paginaActual === totalPaginas ? '#f3f4f6' : 'white',
                      color: paginaActual === totalPaginas ? '#9ca3af' : '#374151',
                      cursor: paginaActual === totalPaginas ? 'not-allowed' : 'pointer',
                      fontSize: '14px'
                    }}
                  >
                    Última ⏭️
                  </button>
                </div>
              </div>
            );
          }
          return null;
        })()}
      </div>

      {/* Modal para crear/editar labor */}
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
          zIndex: 1000,
          padding: '20px'
        }}>
          <div style={{
            background: 'white',
            borderRadius: '10px',
            padding: '20px',
            width: '100%',
            maxWidth: '600px',
            maxHeight: '90vh',
            overflowY: 'auto'
          }}>
            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: '20px',
              borderBottom: '1px solid #e5e7eb',
              paddingBottom: '15px'
            }}>
              <h2 style={{ margin: 0, color: '#374151' }}>
                {editingLabor ? '✏️ Editar Labor' : '➕ Nueva Labor'}
              </h2>
              <button
                onClick={handleCloseModal}
                style={{
                  background: 'none',
                  border: 'none',
                  fontSize: '24px',
                  cursor: 'pointer',
                  color: '#6b7280'
                }}
              >
                ✕
              </button>
            </div>

            <form onSubmit={(e) => { e.preventDefault(); saveLabor(); }}>
              <div style={{ display: 'grid', gap: '15px' }}>
                {/* Lote - PRIMERO para determinar las labores disponibles */}
                <div>
                  <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#374151' }}>
                    Lote * <span style={{ fontSize: '12px', color: '#6b7280', fontWeight: 'normal' }}>(Seleccione primero el lote)</span>
                  </label>
                  <select
                    value={formData.lote_id}
                    onChange={(e) => handleLoteChange(Number(e.target.value))}
                    required
                    style={{
                      width: '100%',
                      padding: '10px',
                      border: '1px solid #d1d5db',
                      borderRadius: '8px',
                      fontSize: '14px'
                    }}
                  >
                    <option value={0}>Seleccionar lote</option>
                    {lotes.map(lote => (
                      <option key={lote.id} value={lote.id}>
                        {lote.nombre} - {lote.cultivo} ({lote.superficie} ha)
                      </option>
                    ))}
                  </select>
                  {loteEstado && (
                    <div style={{ 
                      marginTop: '8px', 
                      padding: '8px', 
                      background: '#f0f9ff', 
                      border: '1px solid #bfdbfe', 
                      borderRadius: '6px',
                      fontSize: '12px',
                      color: '#1e40af'
                    }}>
                      <strong>📍 Estado del lote:</strong> {loteEstado.replace(/_/g, ' ')}
                      <br />
                      <em>✅ Solo se mostrarán las labores apropiadas para este estado</em>
                    </div>
                  )}
                </div>

                {/* Tipo de labor - SEGUNDO, filtrado por el estado del lote */}
                <div>
                  <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#374151' }}>
                    Tipo de Labor *
                  </label>
                  <select
                    value={formData.tipo}
                    onChange={(e) => handleInputChange('tipo', e.target.value)}
                    required
                    disabled={!formData.lote_id || editingLabor !== null}
                    style={{
                      width: '100%',
                      padding: '10px',
                      border: '1px solid #d1d5db',
                      borderRadius: '8px',
                      fontSize: '14px',
                      opacity: (!formData.lote_id || editingLabor !== null) ? 0.5 : 1,
                      cursor: (!formData.lote_id || editingLabor !== null) ? 'not-allowed' : 'pointer',
                      backgroundColor: editingLabor !== null ? '#f3f4f6' : 'white'
                    }}
                  >
                    <option value="">{!formData.lote_id ? 'Primero seleccione un lote' : 'Seleccionar tipo de labor'}</option>
                    {tiposLaborDisponibles.map(tipo => (
                      <option key={tipo} value={tipo}>
                        {tipo.charAt(0).toUpperCase() + tipo.slice(1).replace(/_/g, ' ')}
                      </option>
                    ))}
                  </select>
                  {editingLabor !== null && (
                    <div style={{
                      marginTop: '6px',
                      fontSize: '11px',
                      color: '#6b7280',
                      fontWeight: 'bold'
                    }}>
                      🔒 El tipo de labor no se puede cambiar al editar
                    </div>
                  )}
                  {tiposLaborDisponibles.length > 0 && formData.lote_id && editingLabor === null && (
                    <div style={{
                      marginTop: '6px',
                      fontSize: '11px',
                      color: '#059669',
                      fontWeight: 'bold'
                    }}>
                      ✓ {tiposLaborDisponibles.length} labor(es) disponible(s) para este lote
                    </div>
                  )}
                  
                  {/* Mensaje informativo para labores que involucran agroquímicos */}
                  {mensajeAgroquimicos && formData.tipo && (
                    <div style={{
                      marginTop: '10px',
                      padding: '10px',
                      backgroundColor: '#fef3c7',
                      border: '1px solid #fbbf24',
                      borderRadius: '8px',
                      fontSize: '12px',
                      color: '#92400e',
                      lineHeight: '1.5'
                    }}>
                      {mensajeAgroquimicos}
                    </div>
                  )}
                </div>

                {/* Fecha */}
                <div>
                  <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#374151' }}>
                    Fecha *
                  </label>
                  <input
                    type="date"
                    value={formData.fecha}
                    onChange={(e) => handleInputChange('fecha', e.target.value)}
                    required
                    style={{
                      width: '100%',
                      padding: '10px',
                      border: '1px solid #d1d5db',
                      borderRadius: '8px',
                      fontSize: '14px'
                    }}
                  />
                </div>

                {/* Estado */}
                <div>
                  <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#374151' }}>
                    Estado
                  </label>
                  <select
                    value={formData.estado}
                    onChange={(e) => handleInputChange('estado', e.target.value)}
                    style={{
                      width: '100%',
                      padding: '10px',
                      border: '1px solid #d1d5db',
                      borderRadius: '8px',
                      fontSize: '14px'
                    }}
                  >
                    {estadosLabor.map(estado => (
                      <option key={estado.value} value={estado.value}>
                        {estado.label}
                      </option>
                    ))}
                  </select>
                </div>

                {/* Responsable */}
                <div>
                  <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#374151' }}>
                    Responsable *
                  </label>
                  <input
                    type="text"
                    value={formData.responsable}
                    onChange={(e) => handleInputChange('responsable', e.target.value)}
                    placeholder="Nombre del responsable"
                    required
                    style={{
                      width: '100%',
                      padding: '10px',
                      border: '1px solid #d1d5db',
                      borderRadius: '8px',
                      fontSize: '14px'
                    }}
                  />
                </div>

                {/* Observaciones */}
                <div>
                  <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#374151' }}>
                    Observaciones
                  </label>
                  <textarea
                    value={formData.observaciones}
                    onChange={(e) => handleInputChange('observaciones', e.target.value)}
                    placeholder="Descripción de la labor..."
                    rows={3}
                    style={{
                      width: '100%',
                      padding: '10px',
                      border: '1px solid #d1d5db',
                      borderRadius: '8px',
                      fontSize: '14px',
                      resize: 'vertical'
                    }}
                  />
                </div>


                {/* Sección de Insumos */}
                <div>
                  <div style={{ 
                    display: 'flex', 
                    justifyContent: 'space-between', 
                    alignItems: 'center',
                    marginBottom: '10px'
                  }}>
                    <label style={{ fontWeight: 'bold', color: '#374151' }}>
                      🧪 Insumos Utilizados
                    </label>
                    <button
                      type="button"
                      onClick={handleOpenInsumosModal}
                      style={{
                        padding: '5px 10px',
                        background: '#10b981',
                        color: 'white',
                        border: 'none',
                        borderRadius: '5px',
                        cursor: 'pointer',
                        fontSize: '12px'
                      }}
                    >
                      ➕ Agregar
                    </button>
                  </div>
                  
                  {selectedInsumos.length > 0 ? (
                    <div style={{ 
                      border: '1px solid #e5e7eb', 
                      borderRadius: '8px', 
                      padding: '10px',
                      maxHeight: '150px',
                      overflowY: 'auto'
                    }}>
                      {selectedInsumos.map((insumo, index) => (
                        <div key={insumo.insumo_id || `insumo-${index}`} style={{
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'center',
                          padding: '5px 0',
                          borderBottom: '1px solid #f3f4f6'
                        }}>
                          <div>
                            <div style={{ fontWeight: 'bold', fontSize: '13px' }}>
                              {insumo.insumo_nombre}
                            </div>
                            <div style={{ fontSize: '12px', color: '#6b7280' }}>
                              {insumo.cantidad_usada} {insumo.unidad_medida} - {formatCurrency(insumo.costo_total)}
                            </div>
                          </div>
                          <button
                            type="button"
                            onClick={() => removeInsumo(insumo.insumo_id)}
                            style={{
                              padding: '2px 6px',
                              background: '#ef4444',
                              color: 'white',
                              border: 'none',
                              borderRadius: '3px',
                              cursor: 'pointer',
                              fontSize: '10px'
                            }}
                          >
                            ✕
                          </button>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div style={{ 
                      border: '1px solid #e5e7eb', 
                      borderRadius: '8px', 
                      padding: '20px',
                      textAlign: 'center',
                      color: '#6b7280',
                      fontSize: '14px'
                    }}>
                      No hay insumos seleccionados
                    </div>
                  )}
                </div>

                {/* Sección de Maquinaria */}
                <div>
                  <div style={{ 
                    display: 'flex', 
                    justifyContent: 'space-between', 
                    alignItems: 'center',
                    marginBottom: '10px'
                  }}>
                    <label style={{ fontWeight: 'bold', color: '#374151' }}>
                      🚜 Maquinaria Utilizada
                    </label>
                    <div style={{ display: 'flex', gap: '5px' }}>
                      <button
                        type="button"
                        onClick={() => setShowFormMaquinaria(true)}
                        style={{
                          padding: '5px 10px',
                          background: '#10b981',
                          color: 'white',
                          border: 'none',
                          borderRadius: '5px',
                          cursor: 'pointer',
                          fontSize: '12px'
                        }}
                      >
                        🏠 Propia
                      </button>
                      <button
                        type="button"
                        onClick={() => setShowFormMaquinariaAlquilada(true)}
                        style={{
                          padding: '5px 10px',
                          background: '#f59e0b',
                          color: 'white',
                          border: 'none',
                          borderRadius: '5px',
                          cursor: 'pointer',
                          fontSize: '12px'
                        }}
                      >
                        🏢 Alquilada
                      </button>
                    </div>
                  </div>
                  
                  {selectedMaquinaria.length > 0 ? (
                    <div style={{ 
                      border: '1px solid #e5e7eb', 
                      borderRadius: '8px', 
                      padding: '10px',
                      maxHeight: '150px',
                      overflowY: 'auto'
                    }}>
                      {selectedMaquinaria.map((maq, index) => (
                        <div key={maq.maquinaria_id || `maquinaria-${index}`} style={{
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'center',
                          padding: '5px 0',
                          borderBottom: '1px solid #f3f4f6'
                        }}>
                          <div>
                            <div style={{ fontWeight: 'bold', fontSize: '13px' }}>
                              {maq.maquinaria_nombre}
                            </div>
                            <div style={{ fontSize: '12px', color: '#6b7280' }}>
                              {formatCurrency(maq.costo_total)}
                            </div>
                          </div>
                          <button
                            type="button"
                            onClick={() => removeMaquinaria(maq.maquinaria_id)}
                            style={{
                              padding: '2px 6px',
                              background: '#ef4444',
                              color: 'white',
                              border: 'none',
                              borderRadius: '3px',
                              cursor: 'pointer',
                              fontSize: '10px'
                            }}
                          >
                            ✕
                          </button>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div style={{ 
                      border: '1px solid #e5e7eb', 
                      borderRadius: '8px', 
                      padding: '20px',
                      textAlign: 'center',
                      color: '#6b7280',
                      fontSize: '14px'
                    }}>
                      No hay maquinaria seleccionada
                    </div>
                  )}
                </div>

                {/* Sección de Mano de Obra */}
                <div>
                  <div style={{ 
                    display: 'flex', 
                    justifyContent: 'space-between', 
                    alignItems: 'center',
                    marginBottom: '10px'
                  }}>
                    <label style={{ fontWeight: 'bold', color: '#374151' }}>
                      👥 Mano de Obra
                    </label>
                    <button
                      type="button"
                      onClick={() => setShowFormManoObra(true)}
                      style={{
                        padding: '5px 10px',
                        background: '#f59e0b',
                        color: 'white',
                        border: 'none',
                        borderRadius: '5px',
                        cursor: 'pointer',
                        fontSize: '12px'
                      }}
                    >
                      ➕ Agregar
                    </button>
                  </div>
                  
                  {selectedManoObra.length > 0 ? (
                    <div style={{ 
                      border: '1px solid #e5e7eb', 
                      borderRadius: '8px', 
                      padding: '10px',
                      maxHeight: '150px',
                      overflowY: 'auto'
                    }}>
                      {selectedManoObra.map((mo, index) => (
                        <div key={mo.id_labor_mano_obra || `mano-obra-${index}`} style={{
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'center',
                          padding: '5px 0',
                          borderBottom: '1px solid #f3f4f6'
                        }}>
                          <div>
                            <div style={{ fontWeight: 'bold', fontSize: '13px' }}>
                              {mo.descripcion}
                            </div>
                            <div style={{ fontSize: '12px', color: '#6b7280' }}>
                              {mo.cantidad_personas} persona(s) {mo.proveedor && `- ${mo.proveedor}`}
                              {mo.horas_trabajo && ` - ${mo.horas_trabajo}h`}
                            </div>
                            <div style={{ fontSize: '12px', color: '#f59e0b', fontWeight: 'bold' }}>
                              {formatCurrency(mo.costo_total)}
                            </div>
                          </div>
                          <button
                            type="button"
                            onClick={() => removeManoObra(index)}
                            style={{
                              padding: '2px 6px',
                              background: '#ef4444',
                              color: 'white',
                              border: 'none',
                              borderRadius: '3px',
                              cursor: 'pointer',
                              fontSize: '10px'
                            }}
                          >
                            ✕
                          </button>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div style={{ 
                      border: '1px solid #e5e7eb', 
                      borderRadius: '8px', 
                      padding: '20px',
                      textAlign: 'center',
                      color: '#6b7280',
                      fontSize: '14px'
                    }}>
                      No hay mano de obra agregada
                    </div>
                  )}
                </div>

                {/* Resumen de Costos */}
                <div style={{
                  background: '#f8f9fa',
                  padding: '15px',
                  borderRadius: '8px',
                  border: '1px solid #e5e7eb'
                }}>
                  <h4 style={{ margin: '0 0 10px 0', color: '#374151' }}>💰 Resumen de Costos</h4>
                  <div style={{ display: 'grid', gap: '5px', fontSize: '14px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span>Insumos:</span>
                      <span>{formatCurrency(selectedInsumos.reduce((sum, i) => sum + i.costo_total, 0))}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span>Maquinaria:</span>
                      <span>{formatCurrency(selectedMaquinaria.reduce((sum, m) => sum + m.costo_total, 0))}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span>Mano de Obra:</span>
                      <span>{formatCurrency(selectedManoObra.reduce((sum, mo) => sum + mo.costo_total, 0))}</span>
                    </div>
                    <div style={{ 
                      display: 'flex', 
                      justifyContent: 'space-between', 
                      fontWeight: 'bold',
                      borderTop: '1px solid #d1d5db',
                      paddingTop: '5px',
                      marginTop: '5px'
                    }}>
                      <span>Total:</span>
                      <span style={{ color: '#10b981' }}>{formatCurrency(calcularCostoTotal())}</span>
                    </div>
                  </div>
                </div>

                {/* Botones */}
                <div style={{
                  display: 'flex',
                  gap: '10px',
                  justifyContent: 'flex-end',
                  marginTop: '20px',
                  borderTop: '1px solid #e5e7eb',
                  paddingTop: '15px'
                }}>
                  <button
                    type="button"
                    onClick={handleCloseModal}
                    style={{
                      padding: '10px 20px',
                      background: '#6b7280',
                      color: 'white',
                      border: 'none',
                      borderRadius: '8px',
                      cursor: 'pointer',
                      fontSize: '14px'
                    }}
                  >
                    Cancelar
                  </button>
                  <button
                    type="submit"
                    style={{
                      padding: '10px 20px',
                      background: '#f59e0b',
                      color: 'white',
                      border: 'none',
                      borderRadius: '8px',
                      cursor: 'pointer',
                      fontSize: '14px',
                      fontWeight: 'bold'
                    }}
                  >
                    {editingLabor ? 'Actualizar' : 'Crear'} Labor
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal para seleccionar insumos */}
      {showInsumosModal && (
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
          zIndex: 1001,
          padding: '20px'
        }}>
          <div style={{
            background: 'white',
            borderRadius: '10px',
            padding: '20px',
            width: '100%',
            maxWidth: '500px',
            maxHeight: '80vh',
            overflowY: 'auto'
          }}>
            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: '20px',
              borderBottom: '1px solid #e5e7eb',
              paddingBottom: '15px'
            }}>
              <h3 style={{ margin: 0, color: '#374151' }}>🧪 Seleccionar Insumos</h3>
              <button
                onClick={() => {
                  setShowInsumosModal(false);
                  setDosisCalculada(null);
                  setDosisDisponibles([]);
                  setDosisSeleccionada(null);
                }}
                style={{
                  background: 'none',
                  border: 'none',
                  fontSize: '24px',
                  cursor: 'pointer',
                  color: '#6b7280'
                }}
              >
                ✕
              </button>
            </div>

            <div style={{ display: 'grid', gap: '15px' }}>
              {/* Selección de insumo */}
              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#374151' }}>
                  Seleccionar Insumo
                </label>
                <select
                  value={selectedInsumoId}
                  onChange={async (e) => {
                    const insumoId = Number(e.target.value);
                    setSelectedInsumoId(insumoId);
                    setInsumoCantidad(0);
                    setDosisCalculada(null);
                    setDosisDisponibles([]);
                    setDosisSeleccionada(null);
                    
                    // Si hay insumo seleccionado, tipo de labor con agroquímicos y lote, buscar dosis disponibles
                    if (insumoId > 0 && formData.tipo && formData.lote_id) {
                      const tiposConAgroquimicos = [
                        'fertilizacion',
                        'pulverizacion',
                        'aplicacion_herbicida',
                        'aplicacion_insecticida',
                        'control_plagas',
                        'control_malezas'
                      ];
                      
                      const insumo = insumos.find(i => i.id === insumoId);
                      if (insumo && tiposConAgroquimicos.includes(formData.tipo.toLowerCase())) {
                        // Verificar si el insumo es agroquímico
                        const esAgroquimico = insumo.tipo === 'HERBICIDA' || insumo.tipo === 'FUNGICIDA' || 
                                             insumo.tipo === 'INSECTICIDA' || insumo.tipo === 'FERTILIZANTE';
                        
                        if (esAgroquimico) {
                          console.log('🦠 Detectado agroquímico:', insumo.nombre);
                          console.log('🔍 Buscando dosis para insumo ID:', insumoId);
                          
                          try {
                            // Buscar todas las dosis recomendadas del insumo
                            console.log('📡 Llamando a API: /dosis-agroquimicos/insumo/' + insumoId);
                            const dosisData = await dosisAgroquimicosService.obtenerPorInsumo(insumoId);
                            console.log('✅ Respuesta recibida del servidor:', dosisData);
                            console.log('📦 Response data type:', typeof dosisData);
                            console.log('📦 Response data length:', Array.isArray(dosisData) ? dosisData.length : 'no es array');
                            
                            if (dosisData && Array.isArray(dosisData) && dosisData.length > 0) {
                              console.log('📋 Dosis disponibles encontradas:', dosisData.length);
                              console.log('📋 Dosis disponibles (datos completos):', JSON.stringify(dosisData, null, 2));
                              setDosisDisponibles(dosisData);
                              
                              // Si hay solo una dosis, seleccionarla automáticamente
                              if (dosisData.length === 1) {
                                console.log('✅ Solo hay una dosis, seleccionando automáticamente');
                                setDosisSeleccionada(dosisData[0].id);
                                calcularCantidadDesdeDosis(dosisData[0], insumo);
                              }
                            } else {
                              // No hay dosis configuradas - el usuario puede usar el insumo normalmente
                              console.log('⚠️ No se encontraron dosis. dosisData:', dosisData);
                              setDosisDisponibles([]);
                              setDosisCalculada(null);
                              console.log('ℹ️ No hay dosis configuradas para este insumo. El usuario puede usarlo como insumo normal.');
                            }
                          } catch (error: any) {
                            console.error('❌ Error al buscar dosis recomendada:', error);
                            console.error('❌ Error completo:', JSON.stringify(error, null, 2));
                            if (error.response) {
                              console.error('❌ Error response status:', error.response.status);
                              console.error('❌ Error response data:', error.response.data);
                            }
                            // Si hay error, permitir usar el insumo normalmente
                            setDosisDisponibles([]);
                            setDosisCalculada(null);
                            console.log('ℹ️ Error al obtener dosis. El usuario puede usar el insumo normalmente.');
                          }
                        }
                      }
                    }
                  }}
                  style={{
                    width: '100%',
                    padding: '10px',
                    border: '1px solid #d1d5db',
                    borderRadius: '8px',
                    fontSize: '14px'
                  }}
                >
                  <option value={0}>Seleccionar un insumo</option>
                  {insumos.map(insumo => {
                    const cantidadYaUsada = selectedInsumos
                      .filter(i => i.insumo_id === insumo.id)
                      .reduce((sum, i) => sum + i.cantidad_usada, 0);
                    const stockDisponible = insumo.stock_actual - cantidadYaUsada;
                    
                    return (
                      <option key={insumo.id} value={insumo.id}>
                        {insumo.nombre} - Stock disponible: {stockDisponible} {insumo.unidad_medida}
                      </option>
                    );
                  })}
                </select>
              </div>

              {/* Información del insumo seleccionado */}
              {selectedInsumoId > 0 && (() => {
                const insumo = insumos.find(i => i.id === selectedInsumoId);
                if (!insumo) return null;
                
                const cantidadYaUsada = selectedInsumos
                  .filter(i => i.insumo_id === insumo.id)
                  .reduce((sum, i) => sum + i.cantidad_usada, 0);
                const stockDisponible = insumo.stock_actual - cantidadYaUsada;
                const insumoYaSeleccionado = selectedInsumos.find(i => i.insumo_id === insumo.id);

                return (
                  <div style={{
                    border: '1px solid #e5e7eb',
                    borderRadius: '8px',
                    padding: '15px',
                    background: '#f9fafb'
                  }}>
                    <div style={{ marginBottom: '15px' }}>
                      <div style={{ fontWeight: 'bold', fontSize: '14px', marginBottom: '5px' }}>
                        {insumo.nombre}
                      </div>
                      <div style={{ fontSize: '12px', color: '#6b7280', marginBottom: '5px' }}>
                        Precio: {formatCurrency(insumo.precio_unitario)}/{insumo.unidad_medida}
                      </div>
                      <div style={{ fontSize: '12px', color: stockDisponible > 0 ? '#10b981' : '#ef4444' }}>
                        Stock disponible: {stockDisponible} {insumo.unidad_medida}
                        {cantidadYaUsada > 0 && (
                          <span style={{ color: '#6b7280' }}> (ya usado: {cantidadYaUsada} {insumo.unidad_medida})</span>
                        )}
                      </div>
                      
                      {/* Selector de dosis si hay dosis disponibles (opcional) */}
                      {dosisDisponibles.length > 0 && (
                        <div style={{ marginTop: '15px' }}>
                          <label style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold', fontSize: '13px', color: '#374151' }}>
                            🎯 (Opcional) Seleccione la aplicación para cálculo automático:
                          </label>
                          <select
                            value={dosisSeleccionada || ''}
                            onChange={(e) => {
                              const dosisId = Number(e.target.value);
                              setDosisSeleccionada(dosisId);
                              if (dosisId > 0) {
                                const dosis = dosisDisponibles.find(d => d.id === dosisId);
                                if (dosis) {
                                  calcularCantidadDesdeDosis(dosis, insumo);
                                }
                              } else {
                                // Si el usuario selecciona "ninguna", limpiar cálculo
                                setDosisCalculada(null);
                                setInsumoCantidad(0);
                              }
                            }}
                            style={{
                              width: '100%',
                              padding: '10px',
                              border: '1px solid #d1d5db',
                              borderRadius: '8px',
                              fontSize: '14px',
                              backgroundColor: 'white'
                            }}
                          >
                            <option value="">-- No usar cálculo automático --</option>
                            {dosisDisponibles.map((dosis: any) => {
                              console.log('🔍 Mapeando dosis:', dosis);
                              // Manejar tanto camelCase como snake_case
                              const tipoAplicacion = dosis.tipoAplicacion || dosis.tipo_aplicacion || '';
                              const formaAplicacion = dosis.formaAplicacion || dosis.forma_aplicacion || '';
                              const unidad = dosis.unidad || '';
                              const dosisRecomendada = dosis.dosisRecomendadaPorHa || dosis.dosis_recomendada_por_ha || 0;
                              const tipoAplicacionFormateado = tipoAplicacion ? tipoAplicacion.replace(/_/g, ' ') : 'Sin tipo';
                              const formaAplicacionFormateado = formaAplicacion ? formaAplicacion.replace(/_/g, ' ') : 'Sin forma';
                              const unidadFormateado = unidad ? unidad.replace(/_/g, ' ') : '';
                              return (
                                <option key={dosis.id || dosis.dosis_id || `dosis-${Math.random()}`} value={dosis.id || dosis.dosis_id}>
                                  {tipoAplicacionFormateado} - {formaAplicacionFormateado} ({dosisRecomendada} {unidadFormateado}/ha)
                                </option>
                              );
                            })}
                          </select>
                        </div>
                      )}
                      
                      {/* Mensaje informativo sobre dosis calculada */}
                      {dosisCalculada && dosisCalculada.cantidadNecesaria > 0 && (
                        <div style={{
                          marginTop: '10px',
                          padding: '10px',
                          backgroundColor: '#dbeafe',
                          border: '1px solid #3b82f6',
                          borderRadius: '8px',
                          fontSize: '12px',
                          color: '#1e40af',
                          lineHeight: '1.5'
                        }}>
                          <div style={{ fontWeight: 'bold', marginBottom: '5px' }}>💡 Cálculo automático:</div>
                          <div>{dosisCalculada.mensaje}</div>
                          <div style={{ marginTop: '5px', fontSize: '11px', fontStyle: 'italic' }}>
                            Puede modificar la cantidad manualmente si lo desea.
                          </div>
                        </div>
                      )}
                      
                      {/* Mensaje informativo si no hay dosis configurada (no es error) */}
                      {dosisDisponibles.length === 0 && insumo.tipo && (insumo.tipo === 'HERBICIDA' || insumo.tipo === 'FUNGICIDA' || insumo.tipo === 'INSECTICIDA' || insumo.tipo === 'FERTILIZANTE') && (
                        <div style={{
                          marginTop: '10px',
                          padding: '10px',
                          backgroundColor: '#f0f9ff',
                          border: '1px solid #93c5fd',
                          borderRadius: '8px',
                          fontSize: '12px',
                          color: '#1e40af',
                          lineHeight: '1.5'
                        }}>
                          ℹ️ Este agroquímico no tiene dosis configuradas. Puede ingresar la cantidad manualmente como insumo normal.
                        </div>
                      )}
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <input
                        type="number"
                        min="0"
                        max={stockDisponible}
                        value={insumoCantidad}
                        onChange={(e) => setInsumoCantidad(Number(e.target.value))}
                        placeholder="Cantidad"
                        style={{
                          width: '100px',
                          padding: '8px',
                          border: '1px solid #d1d5db',
                          borderRadius: '4px',
                          fontSize: '14px'
                        }}
                      />
                      <span style={{ fontSize: '12px', color: '#6b7280' }}>{insumo.unidad_medida}</span>
                      <button
                        onClick={() => {
                          if (insumoCantidad > 0 && insumoCantidad <= stockDisponible) {
                            const success = addInsumo(insumo, insumoCantidad);
                            if (success) {
                              setShowInsumosModal(false);
                              setSelectedInsumoId(0);
                              setInsumoCantidad(0);
                              setDosisCalculada(null);
                              setDosisDisponibles([]);
                              setDosisSeleccionada(null);
                            }
                          } else if (insumoCantidad > stockDisponible) {
                            alert(`No puede usar más de ${stockDisponible} ${insumo.unidad_medida}`);
                          }
                        }}
                        disabled={insumoCantidad <= 0 || insumoCantidad > stockDisponible}
                        style={{
                          padding: '8px 16px',
                          background: (insumoCantidad > 0 && insumoCantidad <= stockDisponible) ? '#10b981' : '#6b7280',
                          color: 'white',
                          border: 'none',
                          borderRadius: '4px',
                          cursor: (insumoCantidad > 0 && insumoCantidad <= stockDisponible) ? 'pointer' : 'not-allowed',
                          fontSize: '12px'
                        }}
                      >
                        {insumoYaSeleccionado ? 'Actualizar' : 'Agregar'}
                      </button>
                    </div>
                  </div>
                );
              })()}
            </div>
          </div>
        </div>
      )}

      {/* Modal para seleccionar maquinaria */}
      {showMaquinariaModal && (
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
          zIndex: 1001,
          padding: '20px'
        }}>
          <div style={{
            background: 'white',
            borderRadius: '10px',
            padding: '20px',
            width: '100%',
            maxWidth: '500px',
            maxHeight: '80vh',
            overflowY: 'auto'
          }}>
            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: '20px',
              borderBottom: '1px solid #e5e7eb',
              paddingBottom: '15px'
            }}>
              <h3 style={{ margin: 0, color: '#374151' }}>🚜 Seleccionar Maquinaria</h3>
              <button
                onClick={() => setShowMaquinariaModal(false)}
                style={{
                  background: 'none',
                  border: 'none',
                  fontSize: '24px',
                  cursor: 'pointer',
                  color: '#6b7280'
                }}
              >
                ✕
              </button>
            </div>

            <div style={{ display: 'grid', gap: '15px' }}>
              {/* Selección de maquinaria */}
              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#374151' }}>
                  Seleccionar Maquinaria
                </label>
                <select
                  value={selectedMaquinariaId}
                  onChange={(e) => {
                    const maquinariaId = Number(e.target.value);
                    setSelectedMaquinariaId(maquinariaId);
                    setMaquinariaHoras(0);
                    setMaquinariaKilometros(0);
                  }}
                  style={{
                    width: '100%',
                    padding: '10px',
                    border: '1px solid #d1d5db',
                    borderRadius: '8px',
                    fontSize: '14px'
                  }}
                >
                  <option value={0}>Seleccionar una máquina</option>
                  {maquinaria.map(maq => (
                    <option key={maq.id} value={maq.id}>
                      {maq.nombre} - Estado: {maq.estado}
                    </option>
                  ))}
                </select>
              </div>

              {/* Información de la maquinaria seleccionada */}
              {selectedMaquinariaId > 0 && (() => {
                const maq = maquinaria.find(m => m.id === selectedMaquinariaId);
                if (!maq) return null;
                
                const maqYaSeleccionada = selectedMaquinaria.find(m => m.maquinaria_id === maq.id);

                return (
                  <div style={{
                    border: '1px solid #e5e7eb',
                    borderRadius: '8px',
                    padding: '15px',
                    background: '#f9fafb'
                  }}>
                    <div style={{ marginBottom: '15px' }}>
                      <div style={{ fontWeight: 'bold', fontSize: '14px', marginBottom: '5px' }}>
                        {maq.nombre}
                      </div>
                      <div style={{ fontSize: '12px', color: '#6b7280', marginBottom: '5px' }}>
                        Estado: {maq.estado} | Costo/hora: {formatCurrency(maq.costo_por_hora)}
                      </div>
                      <div style={{ fontSize: '12px', color: '#6b7280' }}>
                        Kilómetros actuales: {maq.kilometros_uso} km
                      </div>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px', marginBottom: '15px' }}>
                      <div>
                        <label style={{ fontSize: '12px', color: '#6b7280', display: 'block', marginBottom: '5px' }}>
                          Horas de uso:
                        </label>
                        <input
                          type="number"
                          min="0"
                          value={maquinariaHoras}
                          onChange={(e) => setMaquinariaHoras(Number(e.target.value))}
                          placeholder="0"
                          style={{
                            width: '100%',
                            padding: '8px',
                            border: '1px solid #d1d5db',
                            borderRadius: '4px',
                            fontSize: '14px'
                          }}
                        />
                      </div>
                      <div>
                        <label style={{ fontSize: '12px', color: '#6b7280', display: 'block', marginBottom: '5px' }}>
                          Kilómetros:
                        </label>
                        <input
                          type="number"
                          min="0"
                          value={maquinariaKilometros}
                          onChange={(e) => setMaquinariaKilometros(Number(e.target.value))}
                          placeholder="0"
                          style={{
                            width: '100%',
                            padding: '8px',
                            border: '1px solid #d1d5db',
                            borderRadius: '4px',
                            fontSize: '14px'
                          }}
                        />
                      </div>
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div style={{ fontSize: '12px', color: '#6b7280' }}>
                        Costo estimado: {formatCurrency(maquinariaHoras * maq.costo_por_hora)}
                      </div>
                      <button
                        onClick={() => {
                          if (maquinariaHoras > 0 || maquinariaKilometros > 0) {
                            addMaquinaria(maq, maquinariaHoras, maquinariaKilometros);
                            setShowMaquinariaModal(false);
                            setSelectedMaquinariaId(0);
                            setMaquinariaHoras(0);
                            setMaquinariaKilometros(0);
                          }
                        }}
                        disabled={maquinariaHoras <= 0 && maquinariaKilometros <= 0}
                        style={{
                          padding: '8px 16px',
                          background: (maquinariaHoras > 0 || maquinariaKilometros > 0) ? '#3b82f6' : '#6b7280',
                          color: 'white',
                          border: 'none',
                          borderRadius: '4px',
                          cursor: (maquinariaHoras > 0 || maquinariaKilometros > 0) ? 'pointer' : 'not-allowed',
                          fontSize: '12px'
                        }}
                      >
                        {maqYaSeleccionada ? 'Actualizar' : 'Agregar'}
                      </button>
                    </div>
                  </div>
                );
              })()}
            </div>
          </div>
        </div>
      )}

      {/* Modal de Detalles de Costos */}
      {showDetallesCostos && laborSeleccionada && (
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
            borderRadius: '8px',
            padding: '20px',
            maxWidth: '800px',
            width: '90%',
            maxHeight: '80vh',
            overflowY: 'auto'
          }}>
            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: '20px',
              borderBottom: '1px solid #e5e7eb',
              paddingBottom: '10px'
            }}>
              <h2 style={{ margin: 0, color: '#374151' }}>
                💰 Detalles de Costos - {laborSeleccionada.tipo}
              </h2>
              <button
                onClick={() => setShowDetallesCostos(false)}
                style={{
                  background: 'none',
                  border: 'none',
                  fontSize: '24px',
                  cursor: 'pointer',
                  color: '#6b7280'
                }}
              >
                ×
              </button>
            </div>

            {/* Resumen de Costos */}
            {(() => {
              // Calcular costo de insumos sumando los insumos_usados
              const costoInsumos = laborSeleccionada.insumos_usados && laborSeleccionada.insumos_usados.length > 0
                ? laborSeleccionada.insumos_usados.reduce((sum: number, ins: any) => {
                    const costo = ins.costoTotal || ins.costo_total || 
                                  (ins.costoUnitario || ins.costo_unitario || 0) * (ins.cantidadUsada || ins.cantidad_usada || ins.cantidad || 0);
                    return sum + (costo || 0);
                  }, 0)
                : (laborSeleccionada.costo_base || 0);
              
              const costoMaquinaria = laborSeleccionada.costo_maquinaria || 0;
              const costoManoObra = laborSeleccionada.costo_mano_obra || 0;
              const total = costoInsumos + costoMaquinaria + costoManoObra;
              
              return (
                <div style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
                  gap: '15px',
                  marginBottom: '30px'
                }}>
                  <div style={{
                    padding: '15px',
                    backgroundColor: '#e0e7ff',
                    borderRadius: '8px',
                    textAlign: 'center',
                    border: '2px solid #6366f1'
                  }}>
                    <div style={{ fontSize: '14px', color: '#4338ca', marginBottom: '5px', fontWeight: '600' }}>
                      🧪 Insumos
                    </div>
                    <div style={{ fontSize: '20px', fontWeight: 'bold', color: '#4338ca' }}>
                      {formatCurrency(costoInsumos)}
                    </div>
                  </div>
                  <div style={{
                    padding: '15px',
                    backgroundColor: '#dbeafe',
                    borderRadius: '8px',
                    textAlign: 'center',
                    border: '2px solid #3b82f6'
                  }}>
                    <div style={{ fontSize: '14px', color: '#1d4ed8', marginBottom: '5px', fontWeight: '600' }}>
                      🔧 Maquinaria
                    </div>
                    <div style={{ fontSize: '20px', fontWeight: 'bold', color: '#1d4ed8' }}>
                      {formatCurrency(costoMaquinaria)}
                    </div>
                  </div>
                  <div style={{
                    padding: '15px',
                    backgroundColor: '#fef3c7',
                    borderRadius: '8px',
                    textAlign: 'center',
                    border: '2px solid #f59e0b'
                  }}>
                    <div style={{ fontSize: '14px', color: '#d97706', marginBottom: '5px', fontWeight: '600' }}>
                      👥 Mano de Obra
                    </div>
                    <div style={{ fontSize: '20px', fontWeight: 'bold', color: '#d97706' }}>
                      {formatCurrency(costoManoObra)}
                    </div>
                  </div>
                  <div style={{
                    padding: '15px',
                    backgroundColor: '#d1fae5',
                    borderRadius: '8px',
                    textAlign: 'center',
                    border: '2px solid #10b981',
                    gridColumn: 'span 1'
                  }}>
                    <div style={{ fontSize: '14px', color: '#059669', marginBottom: '5px', fontWeight: '600' }}>
                      💰 Total
                    </div>
                    <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#059669' }}>
                      {formatCurrency(total)}
                    </div>
                  </div>
                </div>
              );
            })()}

            {/* Detalles de Maquinaria */}
            {laborSeleccionada.maquinarias && laborSeleccionada.maquinarias.length > 0 && (
              <div style={{ marginBottom: '30px' }}>
                <h3 style={{ color: '#374151', marginBottom: '15px' }}>🔧 Maquinaria Utilizada</h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                  {laborSeleccionada.maquinarias.map((maq, index) => (
                    <div key={maq.id_labor_maquinaria || `maquinaria-${index}`} style={{
                      padding: '15px',
                      backgroundColor: '#f8fafc',
                      borderRadius: '8px',
                      border: '1px solid #e2e8f0'
                    }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                          <div style={{ fontWeight: 'bold', color: '#374151' }}>{maq.descripcion}</div>
                          <div style={{ fontSize: '14px', color: '#6b7280' }}>
                            Tipo: {maq.tipo} {maq.proveedor && `- ${maq.proveedor}`}
                          </div>
                        </div>
                        <div style={{ fontSize: '18px', fontWeight: 'bold', color: '#1d4ed8' }}>
                          {formatCurrency(maq.costo)}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Detalles de Mano de Obra */}
            {laborSeleccionada.mano_obra && laborSeleccionada.mano_obra.length > 0 && (
              <div style={{ marginBottom: '30px' }}>
                <h3 style={{ color: '#374151', marginBottom: '15px' }}>👥 Mano de Obra</h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                  {laborSeleccionada.mano_obra.map((mo, index) => {
                    // Normalizar datos de mano de obra
                    const cantidadPersonas = mo.cantidadPersonas || mo.cantidad_personas || 1;
                    const costoTotal = mo.costoTotal || mo.costo_total || 0;
                    const horasTrabajo = mo.horasTrabajo || mo.horas_trabajo;
                    
                    return (
                      <div key={mo.id_labor_mano_obra || mo.idLaborManoObra || `mano-obra-detail-${index}`} style={{
                        padding: '15px',
                        backgroundColor: '#f8fafc',
                        borderRadius: '8px',
                        border: '1px solid #e2e8f0'
                      }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start' }}>
                          <div style={{ flex: 1 }}>
                            <div style={{ fontWeight: 'bold', color: '#374151', marginBottom: '4px' }}>
                              {mo.descripcion}
                            </div>
                            <div style={{ fontSize: '14px', color: '#6b7280', marginBottom: '2px' }}>
                              👥 Personas: <strong>{cantidadPersonas}</strong>
                            </div>
                            {mo.proveedor && (
                              <div style={{ fontSize: '14px', color: '#6b7280', marginBottom: '2px' }}>
                                🏢 Proveedor: {mo.proveedor}
                              </div>
                            )}
                            {horasTrabajo && (
                              <div style={{ fontSize: '14px', color: '#6b7280' }}>
                                ⏱️ Horas: {horasTrabajo}
                              </div>
                            )}
                            {mo.observaciones && (
                              <div style={{ fontSize: '12px', color: '#9ca3af', marginTop: '4px', fontStyle: 'italic' }}>
                                {mo.observaciones}
                              </div>
                            )}
                          </div>
                          <div style={{ fontSize: '18px', fontWeight: 'bold', color: '#d97706', textAlign: 'right', minWidth: '120px' }}>
                            {formatCurrency(costoTotal)}
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Detalles de Insumos Utilizados */}
            {laborSeleccionada.insumos_usados && laborSeleccionada.insumos_usados.length > 0 && (
              <div style={{ marginBottom: '30px' }}>
                <h3 style={{ color: '#374151', marginBottom: '15px' }}>🧪 Insumos Utilizados</h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                  {laborSeleccionada.insumos_usados.map((insumo, index) => {
                    // Normalizar datos de insumo
                    const cantidadUsada = insumo.cantidadUsada || insumo.cantidad_usada || insumo.cantidad || 0;
                    const costoUnitario = insumo.costoUnitario || insumo.costo_unitario || insumo.precio_unitario || 0;
                    const costoTotal = insumo.costoTotal || insumo.costo_total || (cantidadUsada * costoUnitario);
                    const unidadMedida = insumo.unidadMedida || insumo.unidad_medida || insumo.unidad || '';
                    const insumoNombre = insumo.insumoNombre || insumo.insumo_nombre || insumo.nombre || 'Insumo sin nombre';
                    
                    return (
                      <div key={insumo.insumo_id || insumo.idInsumo || insumo.id || `insumo-detail-${index}`} style={{
                        padding: '15px',
                        backgroundColor: '#f8fafc',
                        borderRadius: '8px',
                        border: '1px solid #e2e8f0'
                      }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start' }}>
                          <div style={{ flex: 1 }}>
                            <div style={{ fontWeight: 'bold', color: '#374151', marginBottom: '4px' }}>
                              {insumoNombre}
                            </div>
                            <div style={{ fontSize: '14px', color: '#6b7280', marginBottom: '2px' }}>
                              📦 Cantidad: <strong>{cantidadUsada} {unidadMedida}</strong>
                            </div>
                            <div style={{ fontSize: '14px', color: '#6b7280', marginBottom: '2px' }}>
                              💰 Precio Unitario: {formatCurrency(costoUnitario)}
                            </div>
                            {insumo.observaciones && (
                              <div style={{ fontSize: '12px', color: '#9ca3af', marginTop: '4px', fontStyle: 'italic' }}>
                                {insumo.observaciones}
                              </div>
                            )}
                          </div>
                          <div style={{ fontSize: '18px', fontWeight: 'bold', color: '#059669', textAlign: 'right', minWidth: '120px' }}>
                            {formatCurrency(costoTotal)}
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Información de la Labor */}
            <div style={{
              padding: '15px',
              backgroundColor: '#f9fafb',
              borderRadius: '8px',
              border: '1px solid #e5e7eb'
            }}>
              <h4 style={{ color: '#374151', marginBottom: '10px' }}>Información de la Labor</h4>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '10px' }}>
                <div><strong>Lote:</strong> {laborSeleccionada.lote_nombre}</div>
                <div><strong>Fecha:</strong> {new Date(laborSeleccionada.fecha).toLocaleDateString('es-ES')}</div>
                <div><strong>Estado:</strong> {laborSeleccionada.estado}</div>
                <div><strong>Responsable:</strong> {laborSeleccionada.responsable}</div>
              </div>
              {laborSeleccionada.observaciones && (
                <div style={{ marginTop: '10px' }}>
                  <strong>Observaciones:</strong> {laborSeleccionada.observaciones}
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Modal para agregar maquinaria propia */}
      {showFormMaquinaria && (
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
            padding: '30px',
            borderRadius: '10px',
            width: '500px',
            maxHeight: '80vh',
            overflowY: 'auto'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <h2 style={{ margin: 0, color: '#374151' }}>
                🏠 Agregar Maquinaria Propia
              </h2>
              <button
                onClick={() => setShowFormMaquinaria(false)}
                style={{
                  background: 'none',
                  border: 'none',
                  fontSize: '24px',
                  cursor: 'pointer',
                  color: '#6b7280'
                }}
              >
                ×
              </button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Maquinaria de la Empresa:</label>
                <select
                  value={formMaquinaria.descripcion}
                  onChange={(e) => setFormMaquinaria({...formMaquinaria, descripcion: e.target.value})}
                  style={{ width: '100%', padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px' }}
                >
                  <option value="">Seleccionar maquinaria</option>
                  {maquinaria.map(maq => (
                    <option key={maq.id} value={maq.nombre}>
                      {maq.nombre} - {maq.tipo} ({formatCurrency(maq.costo_por_hora)}/h)
                    </option>
                  ))}
                </select>
              </div>


              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Horas de Uso:</label>
                <input
                  type="number"
                  step="0.1"
                  value={formMaquinaria.horas_uso}
                  onChange={(e) => {
                    const horas = e.target.value;
                    const maqSeleccionada = maquinaria.find(m => m.nombre === formMaquinaria.descripcion);
                    const costoCalculado = (parseFloat(horas) || 0) * (maqSeleccionada?.costo_por_hora || 0);
                    setFormMaquinaria({
                      ...formMaquinaria, 
                      horas_uso: horas,
                      costo: costoCalculado.toString()
                    });
                  }}
                  placeholder="0.0"
                  style={{ width: '100%', padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Costo Total:</label>
                <input
                  type="number"
                  step="0.01"
                  value={formMaquinaria.costo}
                  readOnly
                  style={{ 
                    width: '100%', 
                    padding: '8px', 
                    border: '1px solid #d1d5db', 
                    borderRadius: '4px',
                    backgroundColor: '#f9fafb',
                    color: '#374151'
                  }}
                />
                <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '2px' }}>
                  Se calcula automáticamente: {formMaquinaria.horas_uso ? parseFloat(formMaquinaria.horas_uso) : 0} horas × {maquinaria.find(m => m.nombre === formMaquinaria.descripcion)?.costo_por_hora || 0} = {formatCurrency((parseFloat(formMaquinaria.horas_uso) || 0) * (maquinaria.find(m => m.nombre === formMaquinaria.descripcion)?.costo_por_hora || 0))}
                </div>
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Kilómetros Recorridos:</label>
                <input
                  type="number"
                  step="0.1"
                  value={formMaquinaria.kilometros_recorridos}
                  onChange={(e) => setFormMaquinaria({...formMaquinaria, kilometros_recorridos: e.target.value})}
                  placeholder="0.0"
                  style={{ width: '100%', padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Observaciones:</label>
                <textarea
                  value={formMaquinaria.observaciones}
                  onChange={(e) => setFormMaquinaria({...formMaquinaria, observaciones: e.target.value})}
                  placeholder="Observaciones adicionales..."
                  rows={3}
                  style={{ width: '100%', padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px', resize: 'vertical' }}
                />
              </div>

              <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end', marginTop: '20px' }}>
                <button
                  onClick={() => setShowFormMaquinaria(false)}
                  style={{
                    padding: '10px 20px',
                    backgroundColor: '#6b7280',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer'
                  }}
                >
                  Cancelar
                </button>
                <button
                  onClick={handleGuardarMaquinaria}
                  style={{
                    padding: '10px 20px',
                    backgroundColor: '#3b82f6',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer'
                  }}
                >
                  Guardar Maquinaria
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal para agregar maquinaria alquilada */}
      {showFormMaquinariaAlquilada && (
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
            padding: '30px',
            borderRadius: '10px',
            width: '500px',
            maxHeight: '80vh',
            overflowY: 'auto'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <h2 style={{ margin: 0, color: '#374151' }}>
                🏢 Agregar Maquinaria Alquilada
              </h2>
              <button
                onClick={() => setShowFormMaquinariaAlquilada(false)}
                style={{
                  background: 'none',
                  border: 'none',
                  fontSize: '24px',
                  cursor: 'pointer',
                  color: '#6b7280'
                }}
              >
                ×
              </button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Descripción:</label>
                <input
                  type="text"
                  value={formMaquinariaAlquilada.descripcion}
                  onChange={(e) => setFormMaquinariaAlquilada({...formMaquinariaAlquilada, descripcion: e.target.value})}
                  placeholder="Ej: Fertilizadora Amazone"
                  style={{ width: '100%', padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Proveedor:</label>
                <input
                  type="text"
                  value={formMaquinariaAlquilada.proveedor}
                  onChange={(e) => setFormMaquinariaAlquilada({...formMaquinariaAlquilada, proveedor: e.target.value})}
                  placeholder="Ej: AgroServicios SRL"
                  style={{ width: '100%', padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Costo:</label>
                <input
                  type="number"
                  step="0.01"
                  value={formMaquinariaAlquilada.costo}
                  onChange={(e) => setFormMaquinariaAlquilada({...formMaquinariaAlquilada, costo: e.target.value})}
                  placeholder="0.00"
                  style={{ width: '100%', padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px' }}
                />
              </div>



              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Observaciones:</label>
                <textarea
                  value={formMaquinariaAlquilada.observaciones}
                  onChange={(e) => setFormMaquinariaAlquilada({...formMaquinariaAlquilada, observaciones: e.target.value})}
                  placeholder="Observaciones adicionales..."
                  rows={3}
                  style={{ width: '100%', padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px', resize: 'vertical' }}
                />
              </div>

              <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end', marginTop: '20px' }}>
                <button
                  onClick={() => setShowFormMaquinariaAlquilada(false)}
                  style={{
                    padding: '10px 20px',
                    backgroundColor: '#6b7280',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer'
                  }}
                >
                  Cancelar
                </button>
                <button
                  onClick={handleGuardarMaquinariaAlquilada}
                  style={{
                    padding: '10px 20px',
                    backgroundColor: '#f59e0b',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer'
                  }}
                >
                  Guardar Maquinaria Alquilada
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal para agregar mano de obra */}
      {showFormManoObra && (
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
            padding: '30px',
            borderRadius: '10px',
            width: '500px',
            maxHeight: '80vh',
            overflowY: 'auto'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <h2 style={{ margin: 0, color: '#374151' }}>
                👥 Agregar Mano de Obra
              </h2>
              <button
                onClick={() => setShowFormManoObra(false)}
                style={{
                  background: 'none',
                  border: 'none',
                  fontSize: '24px',
                  cursor: 'pointer',
                  color: '#6b7280'
                }}
              >
                ×
              </button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Descripción:</label>
                <input
                  type="text"
                  value={formManoObra.descripcion}
                  onChange={(e) => setFormManoObra({...formManoObra, descripcion: e.target.value})}
                  placeholder="Ej: Operador de tractor"
                  style={{ width: '100%', padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Cantidad de Personas:</label>
                <input
                  type="number"
                  min="1"
                  value={formManoObra.cantidad_personas}
                  onChange={(e) => setFormManoObra({...formManoObra, cantidad_personas: parseInt(e.target.value) || 1})}
                  style={{ width: '100%', padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Proveedor (opcional):</label>
                <input
                  type="text"
                  value={formManoObra.proveedor}
                  onChange={(e) => setFormManoObra({...formManoObra, proveedor: e.target.value})}
                  placeholder="Ej: Trabajadores Rurales"
                  style={{ width: '100%', padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Costo Total:</label>
                <input
                  type="number"
                  step="0.01"
                  value={formManoObra.costo_total}
                  onChange={(e) => setFormManoObra({...formManoObra, costo_total: e.target.value})}
                  placeholder="0.00"
                  style={{ width: '100%', padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Horas de Trabajo:</label>
                <input
                  type="number"
                  step="0.1"
                  value={formManoObra.horas_trabajo}
                  onChange={(e) => setFormManoObra({...formManoObra, horas_trabajo: e.target.value})}
                  placeholder="0.0"
                  style={{ width: '100%', padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px' }}
                />
              </div>


              <div>
                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Observaciones:</label>
                <textarea
                  value={formManoObra.observaciones}
                  onChange={(e) => setFormManoObra({...formManoObra, observaciones: e.target.value})}
                  placeholder="Observaciones adicionales..."
                  rows={3}
                  style={{ width: '100%', padding: '8px', border: '1px solid #d1d5db', borderRadius: '4px', resize: 'vertical' }}
                />
              </div>

              <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end', marginTop: '20px' }}>
                <button
                  onClick={() => setShowFormManoObra(false)}
                  style={{
                    padding: '10px 20px',
                    backgroundColor: '#6b7280',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer'
                  }}
                >
                  Cancelar
                </button>
                <button
                  onClick={handleGuardarManoObra}
                  style={{
                    padding: '10px 20px',
                    backgroundColor: '#f59e0b',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: 'pointer'
                  }}
                >
                  Guardar Mano de Obra
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default LaboresManagement;
