import React, { useState, useEffect, useRef, useMemo } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useCurrencyContext } from '../contexts/CurrencyContext';
import { offlineService } from '../services/OfflineService';
import { laboresService, dosisAgroquimicosService } from '../services/apiServices';
import type { Labor, Lote, Insumo, Maquinaria, MaquinariaAsignada, InsumoUsado, LaborMaquinaria, LaborManoObra } from '../types/labores.types';
import { mapEstadoToBackend, mapEstadoFromBackend, mapTipoLaborToBackend, mapTipoLaborFromBackend, ESTADOS_LABOR, TODOS_LOS_TIPOS_LABOR } from '../utils/laboresUtils';
import { useLaboresPermisos } from '../hooks/useLaboresPermisos';
import { useLaboresData } from '../hooks/useLaboresData';
import useContextoOperativo from '../hooks/useContextoOperativo';
import PermissionGate from './PermissionGate';
import { Icon } from '../core/components/Icon';
import { Autocomplete, AutocompleteOption } from './ui/Autocomplete';
import { LaborDetalleCostosModal } from './labores/LaborDetalleCostosModal';
import ProgresoEstadoLotePanel from './ProgresoEstadoLotePanel';

const LaboresManagement: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const abrirLaborIdProcesado = useRef<number | null>(null);
  const { formatCurrency } = useCurrencyContext();
  const { campanaId } = useContextoOperativo();
  const { puedeModificarLabor, puedeAnularLabor, puedeEliminarLabor } = useLaboresPermisos();
  const { labores, setLabores, lotes, insumos, setInsumos, maquinaria, cultivos, loading, setLoading, totalElementos, totalPaginas: totalPaginasServidor, loadData, cargarLabores } = useLaboresData();

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
  /** Texto de búsqueda aplicado al filtro tras debounce (evita filtrar/ordenar en cada tecla). */
  const [busquedaDebounced, setBusquedaDebounced] = useState('');
  const [filterEstado, setFilterEstado] = useState('todos');
  const [filterLote, setFilterLote] = useState<number | 'todos'>('todos');
  const [formData, setFormData] = useState<Labor>({
    tipo: '',
    fecha: '',
    observaciones: '',
    lote_id: 0,
    lote_nombre: '',
    cultivo_id: undefined,
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

  // Modal anular labor (solo para labores completadas o en progreso; requiere admin/jefe)
  const [showModalAnular, setShowModalAnular] = useState(false);
  const [laborAnularId, setLaborAnularId] = useState<number | null>(null);
  const [justificacionAnular, setJustificacionAnular] = useState('');
  const [restaurarInsumosAnular, setRestaurarInsumosAnular] = useState(true);

  // Tipos de labor disponibles (se filtrarán según el estado del lote)
  const [tiposLaborDisponibles, setTiposLaborDisponibles] = useState<string[]>([]);
  const [loteEstado, setLoteEstado] = useState<string>('');
  
  // Estados para mensaje de configuración faltante
  const [mensajeConfiguracion, setMensajeConfiguracion] = useState<string>('');
  const [requiereConfiguracion, setRequiereConfiguracion] = useState<boolean>(false);
  /** true cuando el tipo no tiene config pero se muestran tareas de plantilla (advertencia, no bloqueo) */
  const [usandoTareasPlantilla, setUsandoTareasPlantilla] = useState<boolean>(false);

  const todosLosTiposLabor = TODOS_LOS_TIPOS_LABOR;
  const estadosLabor = ESTADOS_LABOR;

  // Función para cargar tareas disponibles según el lote (usa configuración de estados)
  const cargarTareasDisponibles = async (loteId: number | null, estadoLote?: string) => {
    if (!loteId) {
      console.log('❌ No hay lote seleccionado');
      setTiposLaborDisponibles([]);
      setMensajeConfiguracion('');
      setRequiereConfiguracion(false);
      return;
    }

    try {
      console.log('🔍 Cargando tareas disponibles para lote:', loteId);
      // Intentar usar el nuevo endpoint que usa configuración
      const data = await laboresService.obtenerTareasDisponiblesPorLote(loteId);
      
      console.log('✅ Tareas recibidas del backend:', data);
      
      // Verificar si requiere configuración (tipo sin esquema de estados/tareas)
      if (data.requiereConfiguracion) {
        setRequiereConfiguracion(true);
        setMensajeConfiguracion(
          data.mensaje || 'El cultivo asignado no tiene un esquema de estados, transiciones y tareas configurado.'
        );
        // Si aun así el backend envió tareas (plantilla), mostrarlas para no bloquear al usuario
        const tareasFallback = data.tareas && data.tareas.length > 0 ? data.tareas.map((t: string) => t.toLowerCase()) : [];
        if (tareasFallback.length > 0) {
          setTiposLaborDisponibles(tareasFallback);
          setUsandoTareasPlantilla(true);
          setMensajeConfiguracion(
            'El tipo de cultivo no tiene configuración específica. Se muestran tareas de plantilla. Configure en Cultivos → Configuración → Configuración del Módulo para definir estados y tareas de este tipo.'
          );
          console.log('⚠️ Usando tareas de plantilla; tipo sin configuración:', data.mensaje);
        } else {
          setTiposLaborDisponibles([]);
          setUsandoTareasPlantilla(false);
          console.log('⚠️ Requiere configuración:', data.mensaje);
        }
      } else {
        setRequiereConfiguracion(false);
        setUsandoTareasPlantilla(false);
        setMensajeConfiguracion('');
        
        // Si hay tareas detalladas (desde configuración), usarlas
        if (data.tareasDetalladas && data.tareasDetalladas.length > 0) {
          const tiposLabor = data.tareasDetalladas.map((t: any) => t.tipoLabor.toLowerCase());
          setTiposLaborDisponibles(tiposLabor);
          console.log('✅ Usando tareas desde configuración:', tiposLabor);
        } else if (data.tareas && data.tareas.length > 0) {
          // Fallback a tareas simples
          setTiposLaborDisponibles(data.tareas.map((t: string) => t.toLowerCase()));
          console.log('✅ Usando tareas simples:', data.tareas);
        } else {
          // No hay tareas para este estado pero el tipo de cultivo sí tiene configuración: mensaje informativo, no bloqueante
          setTiposLaborDisponibles([]);
          setMensajeConfiguracion(data.mensaje || 'No hay tareas configuradas para el estado actual del lote.');
          setRequiereConfiguracion(data.tieneConfiguracion === true ? false : true);
          console.log('⚠️ No se encontraron tareas para este estado');
        }
      }
      
      // Actualizar estado del lote si viene en la respuesta
      if (data.estadoConfigurado) {
        setLoteEstado(data.estadoConfigurado.nombre);
      } else if (data.estadoEnum) {
        setLoteEstado(data.estadoEnum);
      } else if (estadoLote) {
        setLoteEstado(estadoLote);
      }
    } catch (error) {
      console.error('❌ Error cargando tareas disponibles por lote:', error);
      setRequiereConfiguracion(true);
      setMensajeConfiguracion('No se pudieron cargar las tareas para este lote. Verifique la configuración de estados.');
      setTiposLaborDisponibles([]);
    }
  };

  useEffect(() => {
    loadData();
    // Las tareas se cargan al seleccionar un lote (según estado y configuración)
    setTiposLaborDisponibles([]);
  }, [campanaId]);

  useEffect(() => {
    const temporizador = window.setTimeout(() => setBusquedaDebounced(searchTerm), 300);
    return () => window.clearTimeout(temporizador);
  }, [searchTerm]);
  
  // Efecto separado para preseleccionar lote cuando se cargan los lotes
  useEffect(() => {
    // Verificar si hay un lote preseleccionado desde LotesManagement
    const lotePreseleccionado = localStorage.getItem('lotePreseleccionadoParaLabor');
    if (lotePreseleccionado && lotes.length > 0) {
      try {
        const lote = JSON.parse(lotePreseleccionado);
        if (lote.id && lotes.find(l => l.id === lote.id)) {
          handleLoteChange(lote.id);
          // Limpiar el localStorage después de usarlo
          localStorage.removeItem('lotePreseleccionadoParaLabor');
        }
      } catch (error) {
        console.error('Error al parsear lote preseleccionado:', error);
        localStorage.removeItem('lotePreseleccionadoParaLabor');
      }
    }
  }, [lotes]);

  // Abrir detalle de labor al llegar desde el calendario (state.abrirLaborId)
  useEffect(() => {
    const abrirLaborId = (location.state as { abrirLaborId?: number })?.abrirLaborId;
    if (loading || abrirLaborId == null || abrirLaborIdProcesado.current === abrirLaborId) return;

    abrirLaborIdProcesado.current = abrirLaborId;
    let cancelled = false;

    laboresService.obtener(abrirLaborId)
      .then((d: any) => {
        if (cancelled) return;
        const laborMapeada: Labor = {
          id: d.id,
          tipo: d.tipo || 'OTROS',
          fecha: d.fechaInicio || '',
          fecha_fin: d.fechaFin || undefined,
          observaciones: d.observaciones || d.descripcion || '',
          lote_id: d.loteId ?? 0,
          lote_nombre: d.loteNombre || '',
          estado: mapEstadoFromBackend(d.estado || 'PLANIFICADA'),
          responsable: d.responsable || '',
          horas_trabajo: d.horasTrabajo ?? 0,
          costo_total: d.costoTotal ?? 0,
          insumos_usados: (d.insumosUsados || []).map((ins: any) => ({
            insumo_id: ins.idInsumo ?? ins.insumo_id,
            insumo_nombre: ins.insumoNombre ?? ins.insumo_nombre ?? '',
            cantidad_usada: ins.cantidadUsada ?? ins.cantidad_usada ?? 0,
            cantidad_planificada: ins.cantidadPlanificada ?? ins.cantidad_planificada ?? 0,
            unidad_medida: ins.unidadMedida ?? ins.unidad_medida ?? '',
            costo_unitario: ins.costoUnitario ?? ins.costo_unitario ?? 0,
            costo_total: ins.costoTotal ?? ins.costo_total ?? 0
          })),
          maquinaria_asignada: [],
          maquinarias: (d.maquinarias || []).map((m: any) => ({
            id_labor_maquinaria: m.idLaborMaquinaria ?? m.id_labor_maquinaria ?? 0,
            id_labor: d.id,
            descripcion: m.descripcion ?? m.maquinariaNombre ?? '',
            proveedor: m.proveedor,
            costo: m.costo ?? 0,
            observaciones: m.observaciones
          })),
          mano_obra: (d.manoObra || []).map((mo: any) => ({
            id_labor_mano_obra: mo.idLaborManoObra ?? mo.id_labor_mano_obra ?? 0,
            id_labor: d.id,
            descripcion: mo.descripcion ?? '',
            cantidad_personas: mo.cantidadPersonas ?? mo.cantidad_personas ?? 1,
            proveedor: mo.proveedor,
            costo_total: mo.costoTotal ?? mo.costo_total ?? 0,
            horas_trabajo: mo.horasTrabajo ?? mo.horas_trabajo,
            observaciones: mo.observaciones
          }))
        };
        handleOpenEditModal(laborMapeada);
        navigate('/cultivos/labores', { replace: true, state: {} });
      })
      .catch((err) => {
        if (!cancelled) {
          console.error('Error al cargar labor desde calendario:', err);
          abrirLaborIdProcesado.current = null;
        }
      });

    return () => { cancelled = true; };
  }, [loading, location.state]);

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
      cultivo_id: undefined,
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
    // Cargar siempre desde el backend para mostrar los datos actualizados (evita datos en caché/lista desactualizada)
    let laborParaForm = labor;
    if (labor.id) {
      try {
        const d = await laboresService.obtener(labor.id);
        laborParaForm = {
          id: d.id,
          tipo: (d.tipo || '').toLowerCase(),
          fecha: d.fechaInicio || '',
          fecha_fin: d.fechaFin || '',
          observaciones: d.observaciones || '',
          lote_id: d.loteId ?? 0,
          lote_nombre: d.loteNombre || '',
          estado: mapEstadoFromBackend(d.estado || 'PLANIFICADA'),
          responsable: d.responsable || '',
          horas_trabajo: d.horasTrabajo ?? 0,
          costo_total: d.costoTotal ?? 0,
          costo_insumos: d.costoInsumos ?? 0,
          costo_maquinaria: d.costoMaquinaria ?? 0,
          costo_mano_obra: d.costoManoObra ?? 0,
          fecha_realizacion: d.fechaRealizacion || undefined,
          insumos_usados: (d.insumosUsados || []).map((ins: any) => ({
            insumo_id: ins.idInsumo ?? ins.insumo_id,
            insumo_nombre: ins.insumoNombre ?? ins.insumo_nombre ?? '',
            cantidad_usada: ins.cantidadUsada ?? ins.cantidad_usada ?? 0,
            cantidad_planificada: ins.cantidadPlanificada ?? ins.cantidad_planificada ?? 0,
            unidad_medida: ins.unidadMedida ?? ins.unidad_medida ?? '',
            costo_unitario: ins.costoUnitario ?? ins.costo_unitario ?? 0,
            costo_total: ins.costoTotal ?? ins.costo_total ?? 0
          })),
          maquinaria_asignada: (d.maquinarias || []).map((m: any) => ({
            maquinaria_id: m.idLaborMaquinaria ?? m.id_labor_maquinaria ?? 0,
            maquinaria_nombre: m.descripcion ?? m.maquinariaNombre ?? '',
            costo_total: m.costo ?? 0,
            proveedor: m.proveedor
          })),
          maquinarias: (d.maquinarias || []).map((m: any) => ({
            id_labor_maquinaria: m.idLaborMaquinaria ?? m.id_labor_maquinaria ?? 0,
            id_labor: d.id,
            descripcion: m.descripcion ?? m.maquinariaNombre ?? '',
            proveedor: m.proveedor,
            costo: m.costo ?? 0,
            observaciones: m.observaciones
          })),
          mano_obra: (d.manoObra || []).map((mo: any) => ({
            id_labor_mano_obra: mo.idLaborManoObra ?? mo.id_labor_mano_obra ?? 0,
            id_labor: d.id,
            descripcion: mo.descripcion ?? '',
            cantidad_personas: mo.cantidadPersonas ?? mo.cantidad_personas ?? 1,
            proveedor: mo.proveedor,
            costo_total: mo.costoTotal ?? mo.costo_total ?? 0,
            horas_trabajo: mo.horasTrabajo ?? mo.horas_trabajo,
            observaciones: mo.observaciones
          }))
        };
      } catch (err) {
        console.warn('No se pudo cargar labor por ID, usando datos del listado:', err);
      }
    }

    const laborMapeada = {
      ...laborParaForm,
      tipo: mapTipoLaborFromBackend(laborParaForm.tipo)
    };
    setFormData(laborMapeada);

    const lote = lotes.find(l => l.id === laborParaForm.lote_id);
    if (lote && lote.id) {
      await cargarTareasDisponibles(lote.id, lote.estado);
    } else {
      setLoteEstado('');
      setTiposLaborDisponibles([]);
    }

    const insumosMapeados = (laborParaForm.insumos_usados || []).map((insumo: any) => ({
      insumo_id: insumo.insumo_id || insumo.id,
      insumo_nombre: insumo.insumo_nombre || insumo.nombre,
      cantidad_usada: insumo.cantidad_usada || insumo.cantidad,
      cantidad_planificada: insumo.cantidad_planificada || insumo.cantidad,
      unidad_medida: insumo.unidad_medida || insumo.unidad,
      costo_unitario: insumo.costo_unitario || insumo.precio_unitario,
      costo_total: insumo.costo_total ?? (insumo.cantidad * insumo.precio_unitario)
    }));
    setSelectedInsumos(insumosMapeados);

    const maquinariasMapeadas = (laborParaForm.maquinarias || []).map((maq: any) => ({
      maquinaria_id: maq.id_labor_maquinaria,
      maquinaria_nombre: maq.descripcion,
      descripcion: maq.descripcion,
      proveedor: maq.proveedor,
      costo_total: maq.costo ?? 0,
      horas_uso: 0,
      kilometros_recorridos: 0
    }));
    const manoObraMapeada = (laborParaForm.mano_obra || []).map((mo: any) => ({
      id_labor_mano_obra: mo.idLaborManoObra ?? mo.id_labor_mano_obra,
      id_labor: mo.idLabor ?? mo.id_labor,
      descripcion: mo.descripcion,
      cantidad_personas: mo.cantidadPersonas ?? mo.cantidad_personas,
      proveedor: mo.proveedor,
      costo_total: mo.costoTotal ?? mo.costo_total ?? 0,
      horas_trabajo: mo.horasTrabajo ?? mo.horas_trabajo,
      observaciones: mo.observaciones
    }));
    setSelectedMaquinaria(maquinariasMapeadas);
    setSelectedManoObra(manoObraMapeada);
    setEditingLabor(laborParaForm);
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
      [field]: value,
      ...(field === 'tipo' ? { cultivo_id: value === 'siembra' ? prev.cultivo_id : undefined } : {})
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
      tipo: '', // Resetear tipo de labor cuando cambia el lote
      cultivo_id: undefined
    }));
    
    // Cargar tareas disponibles según el lote (usa configuración de estados)
    if (lote && lote.id) {
      console.log('✅ Lote seleccionado, cargando tareas para lote:', lote.id);
      await cargarTareasDisponibles(lote.id, lote.estado);
    } else {
      console.log('❌ Lote NO seleccionado, usando todas las tareas');
      setLoteEstado('');
      setTiposLaborDisponibles([...todosLosTiposLabor]);
    }
  };

  const saveLabor = async () => {
    if (!formData.tipo || !formData.fecha || !formData.lote_id || !formData.responsable) {
      alert('Por favor complete todos los campos obligatorios');
      return;
    }
    if (formData.tipo === 'siembra' && !formData.cultivo_id) {
      alert('Para una labor de Siembra debe seleccionar el cultivo a sembrar.');
      return;
    }

    try {
      setLoading(true);

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
      
      const laborCompleta: Record<string, unknown> = {
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
        // Cultivo a sembrar: el backend lo usa para asignar tipo de cultivo al lote
        ...(formData.tipo === 'siembra' && formData.cultivo_id ? { cultivoId: formData.cultivo_id } : {}),
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
      // Invalidar caché y recargar listado antes de cerrar para que al reabrir se vean los cambios
      offlineService.remove('labores');
      await loadData();
      handleCloseModal();
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

        await laboresService.eliminar(id);
        
        // Invalidar caché de labores para forzar recarga fresca
        offlineService.remove('labores');
        // Eliminar del estado local solo si la API confirma la eliminación
        setLabores(prev => prev.filter(l => l.id !== id));
        alert('Labor eliminada exitosamente');
      } catch (error: any) {
        console.error('Error al eliminar la labor:', error);
        const mensajeBackend = error?.response?.data?.error;
        const mensaje = mensajeBackend
          ? mensajeBackend
          : 'Error de conexión al eliminar la labor. Por favor, verifica tu conexión e intenta nuevamente.';
        alert(mensaje);
      } finally {
        setLoading(false);
      }
    }
  };

  const openModalAnular = (labor: Labor) => {
    setLaborAnularId(labor.id!);
    setJustificacionAnular('');
    setRestaurarInsumosAnular(true);
    setShowModalAnular(true);
  };

  const closeModalAnular = () => {
    setShowModalAnular(false);
    setLaborAnularId(null);
    setJustificacionAnular('');
    setRestaurarInsumosAnular(true);
  };

  const confirmarAnular = async () => {
    if (laborAnularId == null) return;
    const justificacion = justificacionAnular.trim();
    if (!justificacion) {
      alert('La justificación es obligatoria para anular una labor.');
      return;
    }
    try {
      setLoading(true);
      await laboresService.anular(laborAnularId, {
        justificacion,
        restaurarInsumos: restaurarInsumosAnular
      });
      offlineService.remove('labores');
      setLabores(prev => prev.filter(l => l.id !== laborAnularId));
      alert('Labor anulada exitosamente.');
      closeModalAnular();
    } catch (error: any) {
      console.error('Error al anular la labor:', error);
      const mensaje = error?.response?.data?.error || 'Error al anular la labor. Intente de nuevo.';
      alert(mensaje);
    } finally {
      setLoading(false);
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

  const resumenLabores = useMemo(() => {
    let costoTotal = 0;
    let enProgreso = 0;
    let completadas = 0;
    for (const l of labores) {
      costoTotal += l.costo_total || 0;
      if (l.estado === 'en_progreso') enProgreso++;
      if (l.estado === 'completada') completadas++;
    }
    return {
      total: labores.length,
      enProgreso,
      completadas,
      costoTotal
    };
  }, [labores]);

  const filteredLabores = labores;

  const { laboresPaginadas, totalPaginas, totalListado } = useMemo(() => ({
    laboresPaginadas: filteredLabores,
    totalPaginas: totalPaginasServidor,
    totalListado: totalElementos,
  }), [filteredLabores, totalPaginasServidor, totalElementos]);

  const hayFiltrosAplicados =
    busquedaDebounced.trim() !== '' || filterEstado !== 'todos' || filterLote !== 'todos';

  const filtrosLabores = useMemo(() => ({
    loteId: filterLote === 'todos' ? undefined : filterLote,
    estado: filterEstado,
    busqueda: busquedaDebounced.trim() || undefined,
  }), [filterLote, filterEstado, busquedaDebounced]);

  // Carga paginada con filtros en servidor
  useEffect(() => {
    cargarLabores(paginaActual - 1, elementosPorPagina, filtrosLabores);
  }, [paginaActual, elementosPorPagina, filtrosLabores, cargarLabores]);

  // Resetear paginación cuando cambien los filtros (búsqueda tras debounce)
  useEffect(() => {
    setPaginaActual(1);
  }, [busquedaDebounced, filterEstado, filterLote]);

  /** Si el total de páginas encoge, evitar quedar en una página vacía. */
  useEffect(() => {
    setPaginaActual(p => (p > totalPaginasServidor ? Math.max(1, totalPaginasServidor) : p));
  }, [totalPaginasServidor]);

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
          fontSize: isMobile ? '20px' : '24px',
          display: 'flex',
          alignItems: 'center',
          gap: '8px'
        }}>
          <Icon name="Wrench" size={24} /> Gestión de Labores
        </h1>
        <p style={{ 
          margin: '0', 
          opacity: '0.9',
          fontSize: isMobile ? '14px' : '16px'
        }}>
          Administra las labores agrícolas y su consumo de insumos. Las tareas del calendario se derivan de estas labores.
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
          <Icon name="BarChart" size={20} style={{ marginRight: '8px', verticalAlign: 'middle' }} /> Resumen de Labores
        </h3>
        <div style={{ 
          display: 'grid', 
          gridTemplateColumns: isMobile ? 'repeat(2, 1fr)' : 'repeat(auto-fit, minmax(200px, 1fr))', 
          gap: isMobile ? '10px' : '15px' 
        }}>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#f59e0b' }}>
              {resumenLabores.total}
            </div>
            <div style={{ fontSize: '14px', color: '#6b7280' }}>Total Labores</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#3b82f6' }}>
              {resumenLabores.enProgreso}
            </div>
            <div style={{ fontSize: '14px', color: '#6b7280' }}>En Progreso</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#10b981' }}>
              {resumenLabores.completadas}
            </div>
            <div style={{ fontSize: '14px', color: '#6b7280' }}>Completadas</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#ef4444' }}>
              {formatCurrency(resumenLabores.costoTotal)}
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
            <option value="vencidas">Vencidas</option>
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
              <Icon name="Plus" size={16} style={{ marginRight: '5px', verticalAlign: 'middle' }} /> {isMobile ? 'Nueva' : 'Nueva Labor'}
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
          <Icon name="Wrench" size={18} style={{ marginRight: '8px', verticalAlign: 'middle' }} /> Labores Registradas ({totalListado})
        </div>
        
        {loading ? (
          <div style={{ padding: '40px', textAlign: 'center', color: '#666' }}>
            <Icon name="Loader" size={20} style={{ marginRight: '8px', verticalAlign: 'middle' }} /> Cargando labores...
          </div>
        ) : filteredLabores.length === 0 ? (
          <div style={{ padding: '40px', textAlign: 'center', color: '#666' }}>
            {hayFiltrosAplicados ? 'No se encontraron labores con los filtros aplicados' : 'No hay labores registradas'}
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
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Fecha planificada</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Fecha realización</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Estado</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Responsable</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Insumos</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Maquinaria</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Mano de Obra</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Total</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {laboresPaginadas.map((labor: Labor) => (
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
                      {labor.fecha ? new Date(labor.fecha).toLocaleDateString('es-ES') : '—'}
                    </td>
                    <td style={{ padding: '12px', color: labor.fecha_realizacion ? '#047857' : '#9ca3af' }}>
                      {labor.fecha_realizacion ? new Date(labor.fecha_realizacion).toLocaleDateString('es-ES') : '—'}
                    </td>
                    <td style={{ padding: '12px' }}>
                      <span style={{
                        padding: '4px 8px',
                        borderRadius: '12px',
                        fontSize: '12px',
                        fontWeight: 'bold',
                        background: labor.overdue ? '#dc262620' : '#10b98120',
                        color: labor.overdue ? '#dc2626' : '#10b981'
                      }}>
                        {labor.estado.charAt(0).toUpperCase() + labor.estado.slice(1)}
                      </span>
                      {labor.overdue && (
                        <span style={{
                          marginLeft: '6px',
                          padding: '2px 6px',
                          borderRadius: '8px',
                          fontSize: '11px',
                          fontWeight: '600',
                          background: '#dc2626',
                          color: '#fff'
                        }}>
                          Vencida
                        </span>
                      )}
                    </td>
                    <td style={{ padding: '12px' }}>
                      {labor.responsable}
                    </td>
                    <td style={{ padding: '12px' }}>
                      <span style={{ fontWeight: 'bold', color: '#6b7280' }}>
                        {formatCurrency(labor.costo_insumos || 0)}
                      </span>
                      {labor.insumos_usados && labor.insumos_usados.length > 0 && (
                        <div style={{ fontSize: '10px', color: '#6b7280', marginTop: '2px' }}>
                          {labor.insumos_usados.length} insumo(s)
                        </div>
                      )}
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
                {formatCurrency((labor.costo_insumos || 0) + (labor.costo_maquinaria || 0) + (labor.costo_mano_obra || 0))}
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
                            <Icon name="Pencil" size={14} />
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
                          <Icon name="DollarSign" size={14} />
                        </button>
                        {puedeEliminarLabor(labor) && (
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
                            title="Eliminar labor (solo planificadas, canceladas o anuladas)"
                          >
                            <Icon name="Trash2" size={14} />
                          </button>
                        )}
                        {puedeAnularLabor(labor) && (
                          <button
                            onClick={() => openModalAnular(labor)}
                            style={{
                              padding: '4px 8px',
                              background: '#b45309',
                              color: 'white',
                              border: 'none',
                              borderRadius: '4px',
                              cursor: 'pointer',
                              fontSize: '12px'
                            }}
                            title="Anular labor (completada o en progreso)"
                          >
                            <Icon name="XCircle" size={14} />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Paginación */}
        {totalPaginas > 1 && (
              <div style={{ 
                display: 'flex', 
                justifyContent: 'space-between', 
                alignItems: 'center', 
                padding: '20px',
                borderTop: '1px solid #e5e7eb',
                background: '#f8f9fa'
              }}>
                <div style={{ fontSize: '14px', color: '#6b7280' }}>
                  Mostrando {((paginaActual - 1) * elementosPorPagina) + 1} - {Math.min(paginaActual * elementosPorPagina, totalListado)} de {totalListado} labores
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
                    <Icon name="ChevronsLeft" size={14} style={{ marginRight: '4px', verticalAlign: 'middle' }} /> Primera
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
                    <Icon name="ChevronLeft" size={14} style={{ marginRight: '4px', verticalAlign: 'middle' }} /> Anterior
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
                    Siguiente <Icon name="ChevronRight" size={14} style={{ marginLeft: '4px', verticalAlign: 'middle' }} />
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
                    Última <Icon name="ChevronsRight" size={14} style={{ marginLeft: '4px', verticalAlign: 'middle' }} />
                  </button>
                </div>
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
              <h2 style={{ margin: 0, color: '#374151', display: 'flex', alignItems: 'center', gap: '8px' }}>
                {editingLabor ? (
                  <>
                    <Icon name="Pencil" size={20} /> Editar Labor
                  </>
                ) : (
                  <>
                    <Icon name="Plus" size={20} /> Nueva Labor
                  </>
                )}
              </h2>
              <button
                onClick={handleCloseModal}
                style={{
                  background: 'none',
                  border: 'none',
                  fontSize: '24px',
                  cursor: 'pointer',
                  color: '#6b7280',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}
              >
                <Icon name="X" size={20} />
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
                      <strong><Icon name="MapPin" size={14} style={{ marginRight: '4px', verticalAlign: 'middle' }} /> Estado del lote:</strong> {loteEstado.replace(/_/g, ' ')}
                      <br />
                      <em><Icon name="CheckCircle" size={14} style={{ marginRight: '4px', verticalAlign: 'middle' }} /> Solo se mostrarán las labores apropiadas para este estado</em>
                    </div>
                  )}
                  <ProgresoEstadoLotePanel loteId={formData.lote_id > 0 ? formData.lote_id : null} />
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
                      <Icon name="Lock" size={14} style={{ marginRight: '4px', verticalAlign: 'middle' }} /> El tipo de labor no se puede cambiar al editar
                    </div>
                  )}
                  {tiposLaborDisponibles.length > 0 && formData.lote_id && editingLabor === null && (
                    <div style={{
                      marginTop: '6px',
                      fontSize: '11px',
                      color: '#059669',
                      fontWeight: 'bold'
                    }}>
                      <Icon name="Check" size={14} style={{ marginRight: '4px', verticalAlign: 'middle' }} /> {tiposLaborDisponibles.length} labor(es) disponible(s) para este lote
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
                  
                  {/* Mensaje cuando falta configuración: no mostrar al editar (el tipo ya está fijado) */}
                  {requiereConfiguracion && formData.lote_id && editingLabor === null && (
                    <div style={{
                      marginTop: '10px',
                      padding: '12px',
                      backgroundColor: usandoTareasPlantilla ? '#fef3c7' : '#fee2e2',
                      border: `1px solid ${usandoTareasPlantilla ? '#f59e0b' : '#ef4444'}`,
                      borderRadius: '8px',
                      fontSize: '13px',
                      color: usandoTareasPlantilla ? '#92400e' : '#991b1b',
                      lineHeight: '1.6'
                    }}>
                      <div style={{ display: 'flex', alignItems: 'flex-start', gap: '8px' }}>
                        <Icon name="AlertCircle" size={18} style={{ marginTop: '2px', flexShrink: 0 }} />
                        <div>
                          <strong style={{ display: 'block', marginBottom: '4px' }}>
                            {usandoTareasPlantilla ? 'ℹ️ Tareas de plantilla' : '⚠️ Configuración Requerida'}
                          </strong>
                          <div>{mensajeConfiguracion}</div>
                          {!usandoTareasPlantilla && (
                            <div style={{ marginTop: '8px', fontSize: '12px', fontStyle: 'italic' }}>
                              Configure el esquema de estados, transiciones y tareas para este tipo de cultivo en la sección de Configuración de Estados.
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  )}

                  {/* Cultivo a sembrar: obligatorio cuando el tipo de labor es Siembra */}
                  {formData.tipo === 'siembra' && (
                    <div style={{ marginTop: '16px' }}>
                      <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#374151' }}>
                        Cultivo a sembrar *
                      </label>
                      <select
                        value={formData.cultivo_id ?? ''}
                        onChange={(e) => handleInputChange('cultivo_id', e.target.value ? Number(e.target.value) : undefined)}
                        required
                        disabled={editingLabor !== null}
                        style={{
                          width: '100%',
                          padding: '10px',
                          border: '1px solid #d1d5db',
                          borderRadius: '8px',
                          fontSize: '14px',
                          backgroundColor: editingLabor !== null ? '#f3f4f6' : 'white'
                        }}
                      >
                        <option value="">Seleccionar cultivo</option>
                        {cultivos.map(c => (
                          <option key={c.id} value={c.id}>
                            {c.nombre} {c.tipo ? `(${c.tipo})` : ''}
                          </option>
                        ))}
                      </select>
                      <div style={{ marginTop: '6px', fontSize: '12px', color: '#6b7280' }}>
                        Al guardar, el lote quedará asignado a este cultivo y tipo de cultivo para las próximas labores.
                      </div>
                      <div style={{ marginTop: '8px', padding: '8px', backgroundColor: '#fef3c7', borderRadius: '6px', fontSize: '11px', color: '#92400e' }}>
                        Si el tipo de cultivo (ej. Soja, Maíz) no tiene configuración de estados y tareas en Configuración del Módulo, la siembra se guardará igual; en las próximas labores se usarán tareas de plantilla hasta que configure ese tipo.
                      </div>
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
                    <label style={{ fontWeight: 'bold', color: '#374151', display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <Icon name="FlaskConical" size={18} /> Insumos Utilizados
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
                        fontSize: '12px',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '4px'
                      }}
                    >
                      <Icon name="Plus" size={14} /> Agregar
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
                            <Icon name="X" size={12} />
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
                    <label style={{ fontWeight: 'bold', color: '#374151', display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <Icon name="Tractor" size={18} /> Maquinaria Utilizada
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
                          fontSize: '12px',
                          display: 'flex',
                          alignItems: 'center',
                          gap: '4px'
                        }}
                      >
                        <Icon name="Home" size={14} /> Propia
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
                          fontSize: '12px',
                          display: 'flex',
                          alignItems: 'center',
                          gap: '4px'
                        }}
                      >
                        <Icon name="Building" size={14} /> Alquilada
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
                            <Icon name="X" size={12} />
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
                    <label style={{ fontWeight: 'bold', color: '#374151', display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <Icon name="Users" size={18} /> Mano de Obra
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
                        fontSize: '12px',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '4px'
                      }}
                    >
                      <Icon name="Plus" size={14} /> Agregar
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
                            <Icon name="X" size={12} />
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
                  <h4 style={{ margin: '0 0 10px 0', color: '#374151', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Icon name="DollarSign" size={18} /> Resumen de Costos
                  </h4>
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
          padding: '16px'
        }}>
          <div style={{
            background: 'white',
            borderRadius: '12px',
            padding: '28px',
            width: '100%',
            maxWidth: '640px',
            maxHeight: 'calc(100vh - 32px)',
            minHeight: '520px',
            overflowY: 'auto',
            overflowX: 'hidden'
          }}>
            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: '24px',
              borderBottom: '1px solid #e5e7eb',
              paddingBottom: '20px',
              paddingTop: '4px'
            }}>
              <h3 style={{ margin: 0, color: '#374151', fontSize: '18px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Icon name="FlaskConical" size={22} /> Seleccionar Insumos
              </h3>
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
                  color: '#6b7280',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}
              >
                <Icon name="X" size={20} />
              </button>
            </div>

            <div style={{ display: 'grid', gap: '20px' }}>
              {/* Selección de insumo - Autocomplete con búsqueda para listas largas */}
              <div style={{ minWidth: '100%' }}>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold', color: '#374151', fontSize: '15px' }}>
                  Seleccionar Insumo
                </label>
                <Autocomplete<Insumo>
                  options={insumos.map(insumo => {
                    const cantidadYaUsada = selectedInsumos
                      .filter(i => i.insumo_id === insumo.id)
                      .reduce((sum, i) => sum + i.cantidad_usada, 0);
                    const stockDisponible = insumo.stock_actual - cantidadYaUsada;
                    return {
                      value: insumo.id,
                      label: `${insumo.nombre} - Stock: ${stockDisponible} ${insumo.unidad_medida}`,
                      data: insumo
                    };
                  })}
                  value={selectedInsumoId || undefined}
                  onChange={async (value, option) => {
                    const insumoId = value ? Number(value) : 0;
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
                        const esAgroquimico = insumo.tipo === 'HERBICIDA' || insumo.tipo === 'FUNGICIDA' || 
                                             insumo.tipo === 'INSECTICIDA' || insumo.tipo === 'FERTILIZANTE';
                        
                        if (esAgroquimico) {
                          try {
                            const dosisData = await dosisAgroquimicosService.obtenerPorInsumo(insumoId);
                            if (dosisData && Array.isArray(dosisData) && dosisData.length > 0) {
                              setDosisDisponibles(dosisData);
                              if (dosisData.length === 1) {
                                setDosisSeleccionada(dosisData[0].id);
                                calcularCantidadDesdeDosis(dosisData[0], insumo);
                              }
                            } else {
                              setDosisDisponibles([]);
                              setDosisCalculada(null);
                            }
                          } catch {
                            setDosisDisponibles([]);
                            setDosisCalculada(null);
                          }
                        }
                      }
                    }
                  }}
                  placeholder="Buscar insumo por nombre..."
                  emptyMessage="No se encontraron insumos"
                  maxHeight={280}
                  style={{ minHeight: '56px' }}
                  inputStyle={{
                    minHeight: '56px',
                    fontSize: '16px',
                    padding: '14px 2.75rem 14px 16px',
                    lineHeight: '1.5',
                  }}
                />
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
                          <label style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '8px', fontWeight: 'bold', fontSize: '13px', color: '#374151' }}>
                            <Icon name="Target" size={16} /> (Opcional) Seleccione la aplicación para cálculo automático:
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
                          <div style={{ fontWeight: 'bold', marginBottom: '5px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <Icon name="Lightbulb" size={16} /> Cálculo automático:
                          </div>
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
                          <Icon name="Info" size={16} style={{ marginRight: '6px', verticalAlign: 'middle' }} /> Este agroquímico no tiene dosis configuradas. Puede ingresar la cantidad manualmente como insumo normal.
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
              <h3 style={{ margin: 0, color: '#374151', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Icon name="Tractor" size={20} /> Seleccionar Maquinaria
              </h3>
              <button
                onClick={() => setShowMaquinariaModal(false)}
                style={{
                  background: 'none',
                  border: 'none',
                  fontSize: '24px',
                  cursor: 'pointer',
                  color: '#6b7280',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}
              >
                <Icon name="X" size={20} />
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
        <LaborDetalleCostosModal
          labor={laborSeleccionada}
          onClose={() => setShowDetallesCostos(false)}
          formatCurrency={formatCurrency}
        />
      )}

      {/* Modal Anular labor (completada o en progreso) */}
      {showModalAnular && (
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
            padding: '24px',
            borderRadius: '8px',
            width: '100%',
            maxWidth: '440px',
            boxShadow: '0 4px 12px rgba(0,0,0,0.15)'
          }}>
            <h3 style={{ margin: '0 0 16px 0', color: '#374151', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Icon name="XCircle" size={22} style={{ color: '#b45309' }} />
              Anular labor
            </h3>
            <p style={{ margin: '0 0 16px 0', color: '#6b7280', fontSize: '14px' }}>
              Esta labor está completada o en progreso. Para anularla debe indicar una justificación. Opcionalmente puede restaurar los insumos al inventario.
            </p>
            <label style={{ display: 'block', marginBottom: '6px', fontWeight: 600, fontSize: '14px', color: '#374151' }}>
              Justificación (obligatoria)
            </label>
            <textarea
              value={justificacionAnular}
              onChange={(e) => setJustificacionAnular(e.target.value)}
              placeholder="Ej.: Error en datos, labor duplicada..."
              rows={3}
              maxLength={1000}
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #d1d5db',
                borderRadius: '6px',
                fontSize: '14px',
                resize: 'vertical',
                marginBottom: '12px',
                boxSizing: 'border-box'
              }}
            />
            <label style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '20px', cursor: 'pointer', fontSize: '14px', color: '#374151' }}>
              <input
                type="checkbox"
                checked={restaurarInsumosAnular}
                onChange={(e) => setRestaurarInsumosAnular(e.target.checked)}
              />
              Restaurar insumos al inventario
            </label>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
              <button
                type="button"
                onClick={closeModalAnular}
                style={{
                  padding: '8px 16px',
                  background: '#e5e7eb',
                  color: '#374151',
                  border: 'none',
                  borderRadius: '6px',
                  cursor: 'pointer',
                  fontSize: '14px'
                }}
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={confirmarAnular}
                disabled={!justificacionAnular.trim()}
                style={{
                  padding: '8px 16px',
                  background: justificacionAnular.trim() ? '#b45309' : '#9ca3af',
                  color: 'white',
                  border: 'none',
                  borderRadius: '6px',
                  cursor: justificacionAnular.trim() ? 'pointer' : 'not-allowed',
                  fontSize: '14px'
                }}
              >
                Anular labor
              </button>
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
              <h2 style={{ margin: 0, color: '#374151', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Icon name="Home" size={20} /> Agregar Maquinaria Propia
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
              <h2 style={{ margin: 0, color: '#374151', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Icon name="Building" size={20} /> Agregar Maquinaria Alquilada
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
              <h2 style={{ margin: 0, color: '#374151', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Icon name="Users" size={20} /> Agregar Mano de Obra
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
