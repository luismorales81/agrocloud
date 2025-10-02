import React, { useState, useEffect } from 'react';
import { useCurrencyContext } from '../contexts/CurrencyContext';
import { offlineService } from '../services/offlineService';

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
      const lotesResponse = await fetch('http://localhost:8080/api/v1/lotes', {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (lotesResponse.ok) {
        const lotesData = await lotesResponse.json();
        const lotesMapeados: Lote[] = lotesData.map((lote: any) => ({
          id: lote.id,
          nombre: lote.nombre,
          superficie: lote.areaHectareas || 0,
          cultivo: lote.cultivoActual || '',
          estado: lote.estado // ← AGREGADO: campo estado del lote
        }));
        setLotes(lotesMapeados);
      }

      // Cargar insumos
      const insumosResponse = await fetch('http://localhost:8080/api/insumos', {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (insumosResponse.ok) {
        const insumosData = await insumosResponse.json();
        const insumosMapeados: Insumo[] = insumosData.map((insumo: any) => ({
          id: insumo.id,
          nombre: insumo.nombre,
          tipo: insumo.tipo,
          stock_actual: insumo.stockActual || 0,
          unidad_medida: insumo.unidadMedida || '',
          precio_unitario: insumo.precioUnitario || 0
        }));
        setInsumos(insumosMapeados);
      }

      // Cargar maquinaria
      const maquinariaResponse = await fetch('http://localhost:8080/api/maquinaria', {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      if (maquinariaResponse.ok) {
        const maquinariaData = await maquinariaResponse.json();
        const maquinariaMapeada: Maquinaria[] = maquinariaData.map((maq: any) => ({
          id: maq.id,
          nombre: maq.nombre,
          tipo: maq.tipo,
          estado: maq.estado,
          kilometros_uso: maq.kilometrosUso || 0,
          costo_por_hora: maq.costoPorHora || 0
        }));
        setMaquinaria(maquinariaMapeada);
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
            insumos_usados: labor.insumosUsados || [],
            maquinaria_asignada: labor.maquinariaAsignada || [],
            responsable: labor.responsable || '',
            horas_trabajo: labor.horasTrabajo || 0,
            costo_total: labor.costoTotal || 0,
            // Nuevos campos para costos detallados - mapeo correcto del backend
            costo_base: labor.costoBase || 0,
            costo_maquinaria: labor.costoMaquinaria || 0,
            costo_mano_obra: labor.costoManoObra || 0,
            maquinarias: labor.maquinarias || [],
            mano_obra: labor.manoObra || []
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
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:8080/api/labores/tareas-disponibles/${estadoLote}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

      console.log('📡 Response status:', response.status);
      
      if (response.ok) {
        const data = await response.json();
        console.log('✅ Tareas recibidas del backend:', data.tareas);
        setTiposLaborDisponibles(data.tareas || []);
      } else {
        console.error('❌ Error del servidor:', response.status);
        const errorText = await response.text();
        console.error('Error details:', errorText);
        // Si falla, usar todas las tareas
        setTiposLaborDisponibles(todosLosTiposLabor);
      }
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
    setShowInsumosModal(true);
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

      let response;
      if (editingLabor) {
        // Editar labor existente
        response = await fetch(`http://localhost:8080/api/labores/${editingLabor.id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify(laborCompleta)
        });
      } else {
        // Crear nueva labor
        response = await fetch('http://localhost:8080/api/labores', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify(laborCompleta)
        });
      }

      if (response.ok) {
        alert(editingLabor ? 'Labor actualizada exitosamente' : 'Labor creada exitosamente');
        // Invalidar caché de labores para forzar recarga fresca
        offlineService.remove('labores');
        handleCloseModal();
        loadData(); // Recargar datos desde el backend
      } else {
        let errorMessage = 'Error al guardar la labor';
        try {
          // Leer el cuerpo de la respuesta una sola vez
          const responseText = await response.text();
          try {
            // Intentar parsear como JSON
            const errorData = JSON.parse(responseText);
            errorMessage = errorData.message || errorData.error || errorMessage;
          } catch (jsonError) {
            // Si no es JSON válido, usar el texto directamente
            errorMessage = responseText || `Error ${response.status}: ${response.statusText}`;
          }
        } catch (textError) {
          // Si no se puede leer el texto, usar información básica
          errorMessage = `Error ${response.status}: ${response.statusText}`;
        }
        console.error('Error al guardar labor:', response.status, response.statusText, errorMessage);
        alert(`Error: ${errorMessage}`);
      }
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

        const response = await fetch(`http://localhost:8080/api/labores/${id}`, {
          method: 'DELETE',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok || response.status === 204) {
          // Invalidar caché de labores para forzar recarga fresca
          offlineService.remove('labores');
          // Eliminar del estado local solo si la API confirma la eliminación
          setLabores(prev => prev.filter(l => l.id !== id));
          alert('Labor eliminada exitosamente');
        } else {
          let errorMessage = 'Error al eliminar la labor';
          try {
            const responseText = await response.text();
            try {
              const errorData = JSON.parse(responseText);
              errorMessage = errorData.error || errorMessage;
            } catch (jsonError) {
              errorMessage = responseText || `Error ${response.status}: ${response.statusText}`;
            }
          } catch (textError) {
            errorMessage = `Error ${response.status}: ${response.statusText}`;
          }
          console.error('Error al eliminar la labor:', response.status, response.statusText, errorMessage);
          alert(`Error al eliminar la labor: ${errorMessage}`);
        }
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
  const filteredLabores = labores.filter(labor => {
    const matchesSearch = (labor.tipo || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
                         (labor.lote_nombre || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
                         (labor.responsable || '').toLowerCase().includes(searchTerm.toLowerCase());
    const matchesEstado = filterEstado === 'todos' || labor.estado === filterEstado;
    return matchesSearch && matchesEstado;
  });

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
                {filteredLabores.map((labor: Labor) => (
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
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
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
                onClick={() => setShowInsumosModal(false)}
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
                  onChange={(e) => {
                    const insumoId = Number(e.target.value);
                    setSelectedInsumoId(insumoId);
                    setInsumoCantidad(0);
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
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
              gap: '15px',
              marginBottom: '30px'
            }}>
              <div style={{
                padding: '15px',
                backgroundColor: '#f3f4f6',
                borderRadius: '8px',
                textAlign: 'center'
              }}>
                <div style={{ fontSize: '14px', color: '#6b7280', marginBottom: '5px' }}>Costo Base</div>
                <div style={{ fontSize: '20px', fontWeight: 'bold', color: '#6b7280' }}>
                  {formatCurrency(laborSeleccionada.costo_base || 0)}
                </div>
              </div>
              <div style={{
                padding: '15px',
                backgroundColor: '#dbeafe',
                borderRadius: '8px',
                textAlign: 'center'
              }}>
                <div style={{ fontSize: '14px', color: '#1d4ed8', marginBottom: '5px' }}>Maquinaria</div>
                <div style={{ fontSize: '20px', fontWeight: 'bold', color: '#1d4ed8' }}>
                  {formatCurrency(laborSeleccionada.costo_maquinaria || 0)}
                </div>
              </div>
              <div style={{
                padding: '15px',
                backgroundColor: '#fef3c7',
                borderRadius: '8px',
                textAlign: 'center'
              }}>
                <div style={{ fontSize: '14px', color: '#d97706', marginBottom: '5px' }}>Mano de Obra</div>
                <div style={{ fontSize: '20px', fontWeight: 'bold', color: '#d97706' }}>
                  {formatCurrency(laborSeleccionada.costo_mano_obra || 0)}
                </div>
              </div>
              <div style={{
                padding: '15px',
                backgroundColor: '#d1fae5',
                borderRadius: '8px',
                textAlign: 'center'
              }}>
                <div style={{ fontSize: '14px', color: '#059669', marginBottom: '5px' }}>Total</div>
                <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#059669' }}>
                  {formatCurrency((laborSeleccionada.costo_base || 0) + (laborSeleccionada.costo_maquinaria || 0) + (laborSeleccionada.costo_mano_obra || 0))}
                </div>
              </div>
            </div>

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
                  {laborSeleccionada.mano_obra.map((mo, index) => (
                    <div key={mo.id_labor_mano_obra || `mano-obra-detail-${index}`} style={{
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
                            👥 Personas: <strong>{mo.cantidad_personas || 0}</strong>
                          </div>
                          {mo.proveedor && (
                            <div style={{ fontSize: '14px', color: '#6b7280', marginBottom: '2px' }}>
                              🏢 Proveedor: {mo.proveedor}
                            </div>
                          )}
                          {mo.horas_trabajo && (
                            <div style={{ fontSize: '14px', color: '#6b7280' }}>
                              ⏱️ Horas: {mo.horas_trabajo}
                            </div>
                          )}
                          {mo.observaciones && (
                            <div style={{ fontSize: '12px', color: '#9ca3af', marginTop: '4px', fontStyle: 'italic' }}>
                              {mo.observaciones}
                            </div>
                          )}
                        </div>
                        <div style={{ fontSize: '18px', fontWeight: 'bold', color: '#d97706', textAlign: 'right', minWidth: '120px' }}>
                          {formatCurrency(mo.costo_total || 0)}
                        </div>
                      </div>
                    </div>
                  ))}
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
