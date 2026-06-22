import React, { useState, useEffect } from 'react';
import { useCurrencyContext } from '../contexts/CurrencyContext';
import { usePermissions } from '../hooks/usePermissions';
import useContextoOperativo from '../hooks/useContextoOperativo';
import AccionLoteModal from './AccionLoteModal';
import EstadoLoteDisplay from './EstadoLoteDisplay';
import ResetLoteModal from './ResetLoteModal';
import { camposService, lotesService, laboresService, cultivosService } from '../services/apiServices';
import type { LaborDetalladoDTO } from '../types/labor.types';
import PermissionGate from './PermissionGate';
import { Icon } from './icons';

interface Campo {
  id: number;
  nombre: string;
  superficie: number;
  ubicacion: string;
  estado: string;
  coordenadas: { lat: number; lng: number }[];
}

interface Lote {
  id?: number;
  nombre: string;
  superficie: number | '';
  cultivo: string;
  campo_id: number;
  estado: string;
  descripcion?: string;
  fechaSiembra?: string;
  fechaCosechaEsperada?: string;
  tipoSuelo?: string;
  estadoConfigurado?: {
    id: number;
    nombre: string;
    color: string;
    icono?: string;
  };
}

interface Labor {
  id: number;
  tipo: string;
  fecha: string;
  fecha_fin?: string;
  observaciones: string;
  lote_id: number;
  lote_nombre: string;
  estado: 'planificada' | 'en_progreso' | 'completada' | 'interrumpida' | 'cancelada';
  responsable: string;
  horas_trabajo?: number;
  costo_total?: number;
  progreso?: number;
  insumos_usados?: any[];
  maquinaria_asignada?: any[];
}

const LotesManagement: React.FC = () => {
  // Versión actualizada con modales simplificados - v2.0
  const { formatCurrency } = useCurrencyContext();
  const permissions = usePermissions();
  const { campanaId } = useContextoOperativo();
  const [lotes, setLotes] = useState<Lote[]>([]);
  const [campos, setCampos] = useState<Campo[]>([]);
  const [showAddModal, setShowAddModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [selectedLote, setSelectedLote] = useState<Lote | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCultivo, setSelectedCultivo] = useState('');
  const [validationError, setValidationError] = useState('');
  const [showHistorialModal, setShowHistorialModal] = useState(false);
  const [selectedLoteForHistorial, setSelectedLoteForHistorial] = useState<Lote | null>(null);
  const [labores, setLabores] = useState<Labor[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  // Estados para acciones especiales
  const [showAccionModal, setShowAccionModal] = useState(false);
  const [loteParaAccion, setLoteParaAccion] = useState<Lote | null>(null);
  
  const [tipoAccion, setTipoAccion] = useState<'abandonar' | 'forraje'>('abandonar');
  const [menuAbierto, setMenuAbierto] = useState<number | null>(null);
  
  // Estados para resetear
  const [showResetModal, setShowResetModal] = useState(false);
  const [loteParaReset, setLoteParaReset] = useState<Lote | null>(null);
  
  // Estados para paginación
  const [paginaActual, setPaginaActual] = useState(1);
  const [elementosPorPagina] = useState(10);

  // Función para calcular fecha de cosecha esperada según el cultivo
  const calcularFechaCosecha = (fechaSiembra: string, cultivo: string): string => {
    const fecha = new Date(fechaSiembra);
    let diasCrecimiento = 0;

    // Días de crecimiento según el cultivo (en días)
    const cultivosCrecimiento: { [key: string]: number } = {
      'Soja': 120,      // 4 meses
      'Maíz': 150,      // 5 meses
      'Trigo': 180,     // 6 meses
      'Cebada': 120,    // 4 meses
      'Alfalfa': 90,    // 3 meses (primer corte)
      'Girasol': 120,   // 4 meses
      'Sorgo': 120,     // 4 meses
      'Avena': 150,     // 5 meses
      'Centeno': 180,   // 6 meses
      'Colza': 180      // 6 meses
    };

    diasCrecimiento = cultivosCrecimiento[cultivo] || 120; // Default 4 meses

    // Agregar días de crecimiento
    fecha.setDate(fecha.getDate() + diasCrecimiento);

    // Formatear como YYYY-MM-DD
    return fecha.toISOString().split('T')[0];
  };

  // Datos simulados de campos
  const camposSimulados: Campo[] = [
    {
      id: 1,
      nombre: 'Campo Norte',
      superficie: 150.5,
      ubicacion: 'Ruta 9, Km 45',
      estado: 'disponible',
      coordenadas: [
        { lat: -34.6118, lng: -58.3960 },
        { lat: -34.6118, lng: -58.3950 },
        { lat: -34.6128, lng: -58.3950 },
        { lat: -34.6128, lng: -58.3960 }
      ]
    },
    {
      id: 2,
      nombre: 'Campo Sur',
      superficie: 89.3,
      ubicacion: 'Ruta 2, Km 78',
      estado: 'disponible',
      coordenadas: [
        { lat: -34.6138, lng: -58.3970 },
        { lat: -34.6138, lng: -58.3960 },
        { lat: -34.6148, lng: -58.3960 },
        { lat: -34.6148, lng: -58.3970 }
      ]
    },
    {
      id: 3,
      nombre: 'Campo Este',
      superficie: 120.0,
      ubicacion: 'Ruta 11, Km 32',
      estado: 'en_mantenimiento',
      coordenadas: [
        { lat: -34.6118, lng: -58.3940 },
        { lat: -34.6118, lng: -58.3930 },
        { lat: -34.6128, lng: -58.3930 },
        { lat: -34.6128, lng: -58.3940 }
      ]
    }
  ];


  const [formData, setFormData] = useState<Lote>({
    nombre: '',
    superficie: '',
    cultivo: '', // Se asigna al sembrar, no al crear
    campo_id: 0,
    estado: 'disponible', // Se enviará como DISPONIBLE al backend
    descripcion: '',
    tipoSuelo: 'Franco Limoso'
  });


  // Estado para cultivos
  const [cultivos, setCultivos] = useState<string[]>([]);

  useEffect(() => {
    cargarDatos();
  }, [campanaId]);
  
  // Cerrar menú dropdown al hacer clic fuera
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as HTMLElement;
      // Solo cerrar si el click NO fue en un botón del dropdown
      if (menuAbierto !== null && !target.closest('[data-testid*="cosechar-button"]')) {
        setMenuAbierto(null);
      }
    };
    
    if (menuAbierto !== null) {
      // Agregar con un pequeño delay para evitar que se cierre inmediatamente
      setTimeout(() => {
        document.addEventListener('click', handleClickOutside);
      }, 10);
      return () => document.removeEventListener('click', handleClickOutside);
    }
  }, [menuAbierto]);

  // Función para cargar datos del backend
  const cargarDatos = async () => {
    await Promise.all([
      cargarCampos(),
      cargarLotes(),
      cargarLabores(),
      cargarCultivos()
    ]);
  };

  // Función para formatear el tipo de labor
  const formatearTipoLabor = (tipo: string) => {
    if (!tipo) return '';
    
    // Mapeo de tipos de labor a nombres legibles
    const tiposLabor: { [key: string]: string } = {
      'SIEMBRA': 'Siembra',
      'FERTILIZACION': 'Fertilización',
      'RIEGO': 'Riego',
      'COSECHA': 'Cosecha',
      'MANTENIMIENTO': 'Mantenimiento',
      'PODA': 'Poda',
      'CONTROL_PLAGAS': 'Control de Plagas',
      'CONTROL_MALEZAS': 'Control de Malezas',
      'ANALISIS_SUELO': 'Análisis de Suelo',
      'OTROS': 'Otros'
    };
    
    // Si el tipo contiene información adicional (como "en V4"), extraer solo el tipo
    const tipoBase = tipo.split(' ')[0];
    
    // Retornar el nombre formateado o el tipo original si no está en el mapeo
    return tiposLabor[tipoBase] || tipoBase.charAt(0).toUpperCase() + tipoBase.slice(1).toLowerCase();
  };

  // Cargar campos del backend
  const cargarCampos = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const token = localStorage.getItem('token');
      if (!token) {
        setError('No hay token de autenticación. Por favor, inicia sesión nuevamente.');
        return;
      }

      const data = await camposService.listar();
      
      // Mapear los datos de la API al formato del frontend
      const camposMapeados: Campo[] = data.map((field: any) => {
        let coordenadas = [];
        if (field.coordenadas) {
          try {
            // Si ya es un array, usarlo directamente
            if (Array.isArray(field.coordenadas)) {
              coordenadas = field.coordenadas;
            } else if (typeof field.coordenadas === 'string') {
              // Si es string, intentar parsearlo como JSON
              coordenadas = JSON.parse(field.coordenadas);
            }
          } catch (error) {
            console.warn('Error parseando coordenadas del campo:', field.nombre, error);
            coordenadas = [];
          }
        }
        
        return {
          id: field.id,
          nombre: field.nombre,
          superficie: field.areaHectareas || 0,
          ubicacion: field.ubicacion || '',
          estado: field.estado?.toLowerCase() || 'activo',
          coordenadas: coordenadas
        };
      });

      console.log('Campos cargados desde API:', camposMapeados);
      setCampos(camposMapeados);
    } catch (error) {
      console.error('Error cargando campos:', error);
      console.log('Usando datos simulados como fallback');
      setError('Error al cargar los campos. Usando datos de ejemplo.');
      // Fallback a datos simulados
      setCampos(camposSimulados);
    }
  };

  // Cargar lotes del backend
  const cargarLotes = async () => {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        setError('No hay token de autenticación. Por favor, inicia sesión nuevamente.');
        return;
      }

      const data = await lotesService.listarCultivo();
      
      console.log('🔍 [LotesManagement] Lotes recibidos del backend:', data.length, data);
      
      // Mapear los datos de la API al formato del frontend
      const lotesMapeados: Lote[] = data.map((lote: any) => {
        const campoId = lote.campoId || lote.campo?.id || 0;
        console.log(`📍 [LotesManagement] Lote ${lote.nombre}: campoId=${campoId}, campo?.id=${lote.campo?.id}`);
        
        // Determinar el estado del lote
        const estadoLote = lote.estado || 'DISPONIBLE';
        
        // Manejar cultivoActual: puede venir como string, objeto o null
        // IMPORTANTE: Si el lote está DISPONIBLE, no debe mostrar cultivo aunque exista en BD
        let cultivoNombre = '';
        if (estadoLote !== 'DISPONIBLE') {
          // Solo obtener el cultivo si el lote NO está disponible
          if (lote.cultivoActual) {
            if (typeof lote.cultivoActual === 'string') {
              cultivoNombre = lote.cultivoActual;
            } else if (typeof lote.cultivoActual === 'object' && lote.cultivoActual.nombre) {
              cultivoNombre = lote.cultivoActual.nombre;
            }
          }
          // Si no hay cultivoActual pero hay cultivo (objeto), usar su nombre
          if (!cultivoNombre && lote.cultivo && typeof lote.cultivo === 'object' && lote.cultivo.nombre) {
            cultivoNombre = lote.cultivo.nombre;
          }
        }
        // Si el estado es DISPONIBLE, forzar cultivoNombre a vacío (ignorar cualquier valor en BD)
        
        return {
          id: lote.id,
          nombre: lote.nombre,
          superficie: lote.areaHectareas || 0,
          cultivo: cultivoNombre,
          campo_id: campoId,
          estado: estadoLote, // Mantener en MAYÚSCULAS como viene del backend
          descripcion: lote.descripcion || '',
          tipoSuelo: lote.tipoSuelo || 'Franco Limoso',
          estadoConfigurado: lote.estadoConfigurado
        };
      });

      console.log('✅ [LotesManagement] Lotes mapeados:', lotesMapeados.length, lotesMapeados);
      setLotes(lotesMapeados);
    } catch (error) {
      console.error('Error cargando lotes:', error);
      setError('Error al cargar los lotes. Por favor, verifica tu conexión.');
      setLotes([]);
    }
  };

  // Cargar labores del backend
  const cargarLabores = async () => {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        setError('No hay token de autenticación. Por favor, inicia sesión nuevamente.');
        return;
      }

      const data: LaborDetalladoDTO[] = await laboresService.listar();
      
      // Mapear los datos de la API al formato del frontend (contrato único: loteId, loteNombre)
      const laboresMapeadas: Labor[] = data.map((labor: LaborDetalladoDTO) => ({
        id: labor.id,
        nombre: labor.nombre,
        tipo: labor.tipo || '',
        fecha: labor.fechaInicio || '',
        lote_id: labor.loteId || 0,
        lote_nombre: labor.loteNombre || labor.lote?.nombre || '',
        responsable: labor.responsable || '',
        horas_trabajo: labor.horasTrabajo || 0,
        costo_total: labor.costoTotal || 0,
        descripcion: labor.descripcion || '',
        estado: (labor.estado || 'planificada') as Labor['estado'],
        observaciones: labor.observaciones || ''
      }));

      setLabores(laboresMapeadas);
    } catch (error) {
      console.error('Error cargando labores:', error);
      setError('Error al cargar las labores. Por favor, verifica tu conexión.');
      setLabores([]);
    } finally {
      setLoading(false);
    }
  };

  // Cargar cultivos del backend
  const cargarCultivos = async () => {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        console.warn('No hay token de autenticación para cargar cultivos');
        // Fallback a lista básica
        setCultivos(['Soja', 'Maíz', 'Trigo', 'Girasol', 'Sorgo', 'Cebada', 'Avena', 'Arroz']);
        return;
      }

      const cultivosData = await cultivosService.listar();
      // Extraer nombres únicos de cultivos
      const nombresCultivos = [...new Set(cultivosData.map((cultivo: { nombre: string }) => cultivo.nombre))] as string[];
      setCultivos(nombresCultivos);
    } catch (error) {
      console.error('Error cargando cultivos:', error);
      // Fallback a lista básica
      setCultivos(['Soja', 'Maíz', 'Trigo', 'Girasol', 'Sorgo', 'Cebada', 'Avena', 'Arroz']);
    }
  };

  const handleInputChange = (field: keyof Lote, value: any) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
    
    // Limpiar error de validación cuando cambia el campo o superficie
    if (field === 'campo_id' || field === 'superficie') {
      setValidationError('');
    }
  };

  const validateSuperficie = (campoId: number, superficie: number, excludeLoteId?: number): boolean => {
    const campo = campos.find(c => c.id === campoId);
    if (!campo) return false;

    // Calcular superficie total de lotes existentes en el campo (excluyendo el lote que se está editando)
    const superficieLotesExistentes = lotes
      .filter(lote => lote.campo_id === campoId && lote.id !== excludeLoteId)
      .reduce((sum, lote) => sum + Number(lote.superficie || 0), 0);

    const superficieTotal = superficieLotesExistentes + superficie;
    
    return superficieTotal <= campo.superficie;
  };

  const handleAgregarLote = () => {
    setFormData({
      nombre: '',
      superficie: '',
      cultivo: '',
      campo_id: 0,
      estado: 'disponible',
      descripcion: '',
      tipoSuelo: 'Franco Limoso',
    });
    setIsEditing(false);
    setShowAddModal(true);
    setValidationError('');
    // Scroll al modal
    setTimeout(() => {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }, 100);
  };

  const handleEditar = (lote: Lote) => {
    setSelectedLote(lote);
    setFormData({
      nombre: lote.nombre,
      superficie: lote.superficie,
      cultivo: lote.cultivo,
      campo_id: lote.campo_id,
      estado: lote.estado,
      descripcion: lote.descripcion || '',
      tipoSuelo: lote.tipoSuelo || 'Franco Limoso',
      fechaSiembra: lote.fechaSiembra || '',
      fechaCosechaEsperada: lote.fechaCosechaEsperada || ''
    });
    setIsEditing(true);
    setShowEditModal(true);
    setValidationError('');
    // Scroll al modal
    setTimeout(() => {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }, 100);
  };

  const handleSaveLote = async () => {
    if (!formData.nombre || formData.campo_id === 0) {
      alert('Por favor complete todos los campos obligatorios (Nombre y Campo)');
      return;
    }

    // Validar superficie
    if (formData.superficie === '' || formData.superficie <= 0) {
      alert('Por favor ingrese una superficie válida');
      return;
    }

    // Validar superficie contra el campo
    if (!validateSuperficie(formData.campo_id, formData.superficie as number, selectedLote?.id)) {
      const campo = campos.find(c => c.id === formData.campo_id);
      const superficieLotesExistentes = lotes
        .filter(lote => lote.campo_id === formData.campo_id && lote.id !== selectedLote?.id)
        .reduce((sum, lote) => sum + Number(lote.superficie || 0), 0);
      
      const superficieDisponible = (campo?.superficie || 0) - superficieLotesExistentes;
      
      setValidationError(
        `La superficie total de lotes (${superficieLotesExistentes + (formData.superficie as number)} ha) excede la superficie del campo (${campo?.superficie} ha). Superficie disponible: ${superficieDisponible.toFixed(2)} ha`
      );
      return;
    }

    // Guardar en el backend
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        alert('Error de autenticación. Por favor, inicia sesión nuevamente.');
        return;
      }

      const loteData = {
        nombre: formData.nombre,
        descripcion: formData.descripcion,
        areaHectareas: formData.superficie as number,
        estado: 'DISPONIBLE', // Los lotes nuevos siempre se crean como DISPONIBLE
        tipoSuelo: formData.tipoSuelo,
        cultivoActual: null, // Se asigna automáticamente al sembrar el lote
        fechaSiembra: null, // Se asigna automáticamente al sembrar el lote
        fechaCosechaEsperada: null, // Se calcula automáticamente al sembrar
        activo: true,
        campo: { id: formData.campo_id }
      };

      if (isEditing && selectedLote) {
        // Editar lote existente
        await lotesService.actualizar(selectedLote.id!, loteData);
      } else {
        // Crear nuevo lote
        await lotesService.crear(loteData);
      }

      alert(isEditing ? 'Lote actualizado exitosamente' : 'Lote creado exitosamente');
      closeModal();
      
      // Recargar lotes del backend
      await cargarLotes();
    } catch (error: any) {
      console.error('Error al guardar lote:', error);
      
      // Intentar extraer el mensaje de error del backend
      let errorMessage = 'Error de conexión. Por favor, verifica tu conexión e intenta nuevamente.';
      
      if (error.response?.data?.message) {
        // El backend devolvió un mensaje de error específico
        errorMessage = error.response.data.message;
      } else if (error.response?.data) {
        // El backend devolvió datos pero sin mensaje
        errorMessage = typeof error.response.data === 'string' 
          ? error.response.data 
          : 'Error al guardar el lote. Por favor, inténtalo de nuevo.';
      }
      
      alert(errorMessage);
    }
  };

  const handleEliminar = async (id: number) => {
    if (window.confirm('¿Está seguro de que desea eliminar este lote?')) {
      try {
        setLoading(true);
        
        const token = localStorage.getItem('token');
        if (!token) {
          alert('Error de autenticación. Por favor, inicia sesión nuevamente.');
          return;
        }

        await lotesService.eliminar(id);
        
        // Eliminar del estado local solo si la API confirma la eliminación
        setLotes(prev => prev.filter(lote => lote.id !== id));
        alert('Lote eliminado exitosamente');
      } catch (error) {
        console.error('Error de conexión al eliminar el lote:', error);
        alert('Error de conexión al eliminar el lote. Por favor, verifica tu conexión e intenta nuevamente.');
      } finally {
        setLoading(false);
      }
    }
  };

  const handleVerHistorial = (lote: Lote) => {
    setSelectedLoteForHistorial(lote);
    setShowHistorialModal(true);
  };
  
  // Función para abrir crear labor
  // Función para abrir modal de reset
  const handleResetearLote = (lote: Lote) => {
    setLoteParaReset(lote);
    setShowResetModal(true);
    setMenuAbierto(null); // Cerrar menú
  };
  
  const tieneAccionesEspeciales = (estado: string): boolean => {
    const estadoUpper = estado?.toUpperCase() || '';
    return estadoUpper === 'SEMBRADO' || estadoUpper === 'EN_CRECIMIENTO' || 
           estadoUpper === 'EN_FLORACION' || estadoUpper === 'EN_FRUTIFICACION';
  };
  
  const handleAccionEspecial = (lote: Lote, accion: 'abandonar' | 'forraje') => {
    setLoteParaAccion(lote);
    setTipoAccion(accion);
    setShowAccionModal(true);
    setMenuAbierto(null);
  };

  const getLaboresPorLote = (loteId: number): Labor[] => {
    return labores.filter(labor => labor.lote_id === loteId);
  };

  const getEstadoColor = (estado: string): string => {
    switch (estado) {
      case 'completada': return '#10b981';
      case 'en_progreso': return '#3b82f6';
      case 'planificada': return '#6b7280';
      case 'interrumpida': return '#f59e0b';
      case 'cancelada': return '#ef4444';
      default: return '#6b7280';
    }
  };

  const closeModal = () => {
    setShowAddModal(false);
    setShowEditModal(false);
    setSelectedLote(null);
    setFormData({
      nombre: '',
      superficie: '',
      cultivo: '',
      campo_id: 0,
      estado: 'disponible',
      descripcion: '',
      tipoSuelo: 'Franco Limoso',
    });
    setValidationError('');
  };

  const getCampoName = (campoId: number) => {
    const campo = campos.find(c => c.id === campoId);
    return campo ? campo.nombre : 'Campo no encontrado';
  };

  const getEstadoTraducido = (estado: string) => {
    const estadoUpper = estado?.toUpperCase() || '';
    const traducciones: { [key: string]: string } = {
      'DISPONIBLE': '🟢 Disponible',
      'PREPARADO': '🟢 Preparado',
      'EN_PREPARACION': '🔧 En Preparación',
      'SEMBRADO': '🌱 Sembrado',
      'EN_CRECIMIENTO': '🌿 En Crecimiento',
      'EN_FLORACION': '🌸 En Floración',
      'EN_FRUTIFICACION': '🍎 En Fructificación',
      'LISTO_PARA_COSECHA': '🌾 Listo para Cosecha',
      'EN_COSECHA': '🚜 En Cosecha',
      'COSECHADO': '✅ Cosechado',
      'EN_DESCANSO': '💤 En Descanso',
      'ENFERMO': '🚨 Enfermo',
      'ABANDONADO': '⚠️ Abandonado'
    };
    return traducciones[estadoUpper] || estado;
  };

  const getSuperficieDisponible = (campoId: number) => {
    const campo = campos.find(c => c.id === campoId);
    if (!campo) return 0;

    const superficieLotesExistentes = lotes
      .filter(lote => lote.campo_id === campoId && lote.id !== selectedLote?.id)
      .reduce((sum, lote) => sum + Number(lote.superficie || 0), 0);

    return campo.superficie - superficieLotesExistentes;
  };

  // Filtrar lotes
  const filteredLotes = lotes.filter(lote => {
    const matchesSearch = lote.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         lote.cultivo.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCultivo = selectedCultivo === '' || lote.cultivo === selectedCultivo;
    return matchesSearch && matchesCultivo;
  });
  
  // Log temporal para diagnóstico
  console.log('🔍 [LotesManagement] Total lotes:', lotes.length, 'Filtrados:', filteredLotes.length, 'Búsqueda:', searchTerm, 'Cultivo:', selectedCultivo);

  // Función para obtener lotes paginados
  const obtenerLotesPaginados = () => {
    const totalPaginas = Math.ceil(filteredLotes.length / elementosPorPagina);
    const inicio = (paginaActual - 1) * elementosPorPagina;
    const fin = inicio + elementosPorPagina;
    const lotesPaginados = filteredLotes.slice(inicio, fin);
    
    return { lotesPaginados, totalPaginas };
  };

  // Resetear paginación cuando cambien los filtros
  useEffect(() => {
    setPaginaActual(1);
  }, [searchTerm, selectedCultivo]);

  // Estadísticas
  const totalSuperficie = lotes.reduce((sum, lote) => sum + Number(lote.superficie || 0), 0);
  const cultivosUnicos = new Set(lotes.map(l => l.cultivo)).size;
  const camposConLotes = new Set(lotes.map(l => l.campo_id)).size;

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
      {/* Header */}
      <div style={{ 
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', 
        color: 'white', 
        padding: '20px', 
        borderRadius: '10px', 
        marginBottom: '20px' 
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
          <div>
            <h1 style={{ margin: '0 0 10px 0', fontSize: '24px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Icon name="Mountain" size={24} /> Gestión de Lotes
            </h1>
            <p style={{ margin: '0', opacity: '0.9' }}>
              Divide campos en lotes y asigna cultivos específicos
            </p>
          </div>
        </div>
      </div>

      {/* Botones de acción */}
      <div style={{ marginBottom: '20px', display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
        <PermissionGate permission="canCreateLotes">
          <button
            onClick={handleAgregarLote}
            data-testid="add-lote-button"
            style={{
              background: '#4CAF50',
              color: 'white',
              border: 'none',
              padding: '10px 20px',
              borderRadius: '5px',
              cursor: 'pointer',
              fontSize: '14px'
            }}
          >
            <Icon name="Plus" size={16} style={{ marginRight: '4px' }} /> Agregar Lote
          </button>
        </PermissionGate>
      </div>

      {/* Filtros */}
      <div style={{ 
        background: '#f5f5f5', 
        padding: '15px', 
        borderRadius: '8px', 
        marginBottom: '20px',
        display: 'flex',
        gap: '15px',
        flexWrap: 'wrap',
        alignItems: 'center'
      }}>
        <div>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Buscar:</label>
          <input
            type="text"
            placeholder="Buscar por nombre o cultivo..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{
              padding: '8px 12px',
              border: '1px solid #ddd',
              borderRadius: '4px',
              fontSize: '14px',
              width: '200px'
            }}
          />
        </div>
        <div>
          <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Cultivo:</label>
          <select
            value={selectedCultivo}
            onChange={(e) => setSelectedCultivo(e.target.value)}
            style={{
              padding: '8px 12px',
              border: '1px solid #ddd',
              borderRadius: '4px',
              fontSize: '14px',
              width: '150px'
            }}
          >
            <option value="">Todos los cultivos</option>
            {cultivos.map(cultivo => (
              <option key={cultivo} value={cultivo}>{cultivo}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Tabla de lotes */}
      <div style={{ 
        background: 'white', 
        borderRadius: '8px', 
        overflow: 'hidden',
        boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
        marginBottom: '20px'
      }}>
        <div style={{ 
          background: '#f8f9fa', 
          padding: '15px', 
          borderBottom: '1px solid #dee2e6' 
        }}>
          <h3 style={{ margin: '0', color: '#495057', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Icon name="BarChart" size={18} /> Lista de Lotes ({filteredLotes.length})
          </h3>
        </div>

        {(() => {
          const { lotesPaginados, totalPaginas } = obtenerLotesPaginados();
          
          if (filteredLotes.length === 0) {
            return (
              <div style={{ padding: '40px', textAlign: 'center', color: '#666' }}>
                {searchTerm || selectedCultivo ? 'No se encontraron lotes que coincidan con los filtros' : 'No hay lotes registrados'}
              </div>
            );
          }

          return (
            <>
              <div style={{ overflowX: 'auto' }}>
                <table style={{ 
                  width: '100%', 
                  borderCollapse: 'collapse',
                  fontSize: '14px'
                }}>
                  <thead>
                    <tr style={{ background: '#f8f9fa' }}>
                      <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Lote</th>
                      <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Campo</th>
                      <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Superficie</th>
                      <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Cultivo</th>
                      <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Tipo de Suelo</th>
                      <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Fecha Siembra</th>
                      <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Fecha Cosecha</th>
                      <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Estado</th>
                      <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {lotesPaginados.map(lote => (
                  <tr key={lote.id} style={{ borderBottom: '1px solid #f1f3f4' }}>
                    <td style={{ padding: '12px' }}>
                      <strong>{lote.nombre}</strong>
                      {lote.descripcion && (
                        <div style={{ fontSize: '12px', color: '#666', marginTop: '4px' }}>
                          {lote.descripcion}
                        </div>
                      )}
                    </td>
                    <td style={{ padding: '12px' }}>
                      {getCampoName(lote.campo_id)}
                    </td>
                    <td style={{ padding: '12px' }}>
                      {lote.superficie} ha
                    </td>
                    <td style={{ padding: '12px' }}>
                      {lote.cultivo ? (
                        <span style={{
                          background: '#e3f2fd',
                          color: '#1976d2',
                          padding: '4px 8px',
                          borderRadius: '12px',
                          fontSize: '12px',
                          fontWeight: 'bold'
                        }}>
                          {lote.cultivo}
                        </span>
                      ) : (
                        <span style={{
                          background: '#f3f4f6',
                          color: '#6b7280',
                          padding: '4px 8px',
                          borderRadius: '12px',
                          fontSize: '12px',
                          fontWeight: 'bold'
                        }}>
                          Sin cultivo
                        </span>
                      )}
                    </td>
                    <td style={{ padding: '12px' }}>
                      <span style={{
                        background: '#f3e5f5',
                        color: '#7b1fa2',
                        padding: '4px 8px',
                        borderRadius: '12px',
                        fontSize: '12px',
                        fontWeight: 'bold'
                      }}>
                        {lote.tipoSuelo || 'No especificado'}
                      </span>
                    </td>
                    <td style={{ padding: '12px' }}>
                      {lote.fechaSiembra ? (
                        <span style={{
                          background: '#e8f5e8',
                          color: '#2e7d32',
                          padding: '4px 8px',
                          borderRadius: '12px',
                          fontSize: '12px',
                          fontWeight: 'bold'
                        }}>
                          {new Date(lote.fechaSiembra).toLocaleDateString('es-ES')}
                        </span>
                      ) : (
                        <span style={{ color: '#999', fontSize: '12px' }}>No especificada</span>
                      )}
                    </td>
                    <td style={{ padding: '12px' }}>
                      {lote.fechaCosechaEsperada ? (
                        <span style={{
                          background: '#fff3e0',
                          color: '#e65100',
                          padding: '4px 8px',
                          borderRadius: '12px',
                          fontSize: '12px',
                          fontWeight: 'bold'
                        }}>
                          {new Date(lote.fechaCosechaEsperada).toLocaleDateString('es-ES')}
                        </span>
                      ) : (
                        <span style={{ color: '#999', fontSize: '12px' }}>No calculada</span>
                      )}
                    </td>
                    <td style={{ padding: '12px' }}>
                      <EstadoLoteDisplay 
                        estado={lote.estado} 
                        estadoConfigurado={lote.estadoConfigurado}
                      />
                    </td>
                    <td style={{ padding: '12px' }}>
                      <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', position: 'relative' }}>
                        {/* Botón de Historial */}
                        <button
                          onClick={() => handleVerHistorial(lote)}
                          data-testid={`lote-${lote.id}-historial-button`}
                          style={{
                            background: '#9c27b0',
                            color: 'white',
                            border: 'none',
                            padding: '6px 12px',
                            borderRadius: '4px',
                            cursor: 'pointer',
                            fontSize: '12px',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '4px'
                          }}
                        >
                          <Icon name="History" size={14} />
                          Historial
                        </button>
                        
                        {/* Menú de acciones adicionales */}
                        <div style={{ position: 'relative' }}>
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              setMenuAbierto(menuAbierto === lote.id ? null : lote.id!);
                            }}
                            style={{
                              background: '#6b7280',
                              color: 'white',
                              border: 'none',
                              padding: '6px 12px',
                              borderRadius: '4px',
                              cursor: 'pointer',
                              fontSize: '12px',
                              display: 'flex',
                              alignItems: 'center',
                              gap: '4px'
                            }}
                          >
                            <Icon name="MoreVertical" size={14} />
                            Más
                          </button>
                          
                          {/* Dropdown menu */}
                          {menuAbierto === lote.id && (
                            <div style={{
                              position: 'absolute',
                              top: '100%',
                              right: 0,
                              marginTop: '4px',
                              background: 'white',
                              border: '1px solid #ddd',
                              borderRadius: '4px',
                              boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
                              zIndex: 1000,
                              minWidth: '200px'
                            }}>
                              <button
                                onClick={(e) => { 
                                  e.stopPropagation();
                                  handleAccionEspecial(lote, 'forraje');
                                  setMenuAbierto(null);
                                }}
                                style={{
                                  width: '100%',
                                  padding: '10px 16px',
                                  border: 'none',
                                  background: 'white',
                                  cursor: 'pointer',
                                  textAlign: 'left',
                                  fontSize: '13px',
                                  borderBottom: '1px solid #f0f0f0',
                                  display: 'flex',
                                  alignItems: 'center',
                                  gap: '8px'
                                }}
                                onMouseEnter={(e) => e.currentTarget.style.background = '#f5f5f5'}
                                onMouseLeave={(e) => e.currentTarget.style.background = 'white'}
                              >
                                <Icon name="Circle" size={14} />
                                Convertir a Forraje
                              </button>
                              <button
                                onClick={(e) => { 
                                  e.stopPropagation();
                                  handleAccionEspecial(lote, 'abandonar');
                                  setMenuAbierto(null);
                                }}
                                style={{
                                  width: '100%',
                                  padding: '10px 16px',
                                  border: 'none',
                                  background: 'white',
                                  cursor: 'pointer',
                                  textAlign: 'left',
                                  fontSize: '13px',
                                  borderBottom: '1px solid #f0f0f0',
                                  color: '#f44336',
                                  display: 'flex',
                                  alignItems: 'center',
                                  gap: '8px'
                                }}
                                onMouseEnter={(e) => e.currentTarget.style.background = '#ffebee'}
                                onMouseLeave={(e) => e.currentTarget.style.background = 'white'}
                              >
                                <Icon name="AlertTriangle" size={14} />
                                Abandonar Cultivo
                              </button>
                              <button
                                onClick={(e) => { 
                                  e.stopPropagation();
                                  handleResetearLote(lote);
                                }}
                                style={{
                                  width: '100%',
                                  padding: '10px 16px',
                                  border: 'none',
                                  background: 'white',
                                  cursor: 'pointer',
                                  textAlign: 'left',
                                  fontSize: '13px',
                                  color: '#ef4444',
                                  display: 'flex',
                                  alignItems: 'center',
                                  gap: '8px'
                                }}
                                onMouseEnter={(e) => e.currentTarget.style.background = '#fee2e2'}
                                onMouseLeave={(e) => e.currentTarget.style.background = 'white'}
                              >
                                <Icon name="RefreshCw" size={14} />
                                Resetear a Inicial
                              </button>
                            </div>
                          )}
                        </div>
                        
                        {/* Botones de Editar y Eliminar */}
                        {permissions.canEditLotes && (
                          <button
                            onClick={() => handleEditar(lote)}
                            data-testid={`lote-${lote.id}-editar-button`}
                            style={{
                              background: '#2196F3',
                              color: 'white',
                              border: 'none',
                              padding: '6px 12px',
                              borderRadius: '4px',
                              cursor: 'pointer',
                              fontSize: '12px',
                              display: 'flex',
                              alignItems: 'center',
                              gap: '4px'
                            }}
                          >
                            <Icon name="Pencil" size={14} />
                            Editar
                          </button>
                        )}
                        {permissions.canDeleteLotes && (
                          <button
                            onClick={() => handleEliminar(lote.id!)}
                            data-testid={`lote-${lote.id}-eliminar-button`}
                            style={{
                              background: '#f44336',
                              color: 'white',
                              border: 'none',
                              padding: '6px 12px',
                              borderRadius: '4px',
                              cursor: 'pointer',
                              fontSize: '12px',
                              display: 'flex',
                              alignItems: 'center',
                              gap: '4px'
                            }}
                          >
                            <Icon name="Trash2" size={14} />
                            Eliminar
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                  ))}
                  </tbody>
                </table>
              </div>

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
                    Mostrando {((paginaActual - 1) * elementosPorPagina) + 1} - {Math.min(paginaActual * elementosPorPagina, filteredLotes.length)} de {filteredLotes.length} lotes
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
                      <Icon name="ChevronsLeft" size={14} style={{ marginRight: '4px' }} /> Primera
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
                      <Icon name="ChevronLeft" size={14} style={{ marginRight: '4px' }} /> Anterior
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
                      Siguiente <Icon name="ChevronRight" size={14} style={{ marginLeft: '4px' }} />
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
                      Última <Icon name="ChevronsRight" size={14} style={{ marginLeft: '4px' }} />
                    </button>
                  </div>
                </div>
              )}
            </>
          );
        })()}
      </div>

      {/* Estadísticas */}
      <div style={{ 
        background: '#e8f5e8', 
        padding: '15px', 
        borderRadius: '8px' 
      }}>
        <h4 style={{ margin: '0 0 10px 0', color: '#2e7d32', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Icon name="TrendingUp" size={18} /> Estadísticas
        </h4>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '15px' }}>
          <div>
            <strong>Total de Lotes:</strong> {lotes.length}
          </div>
          <div>
            <strong>Superficie Total:</strong> {totalSuperficie.toFixed(2)} ha
          </div>
          <div>
            <strong>Cultivos Diferentes:</strong> {cultivosUnicos}
          </div>
          <div>
            <strong>Campos con Lotes:</strong> {camposConLotes}
          </div>
        </div>
      </div>

      {/* Modal de Agregar/Editar Lote */}
      {(showAddModal || showEditModal) && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 1000
        }}>
          <div style={{
            background: 'white',
            borderRadius: '10px',
            padding: '2rem',
            maxWidth: '600px',
            width: '95%',
            maxHeight: '90vh',
            overflowY: 'auto'
          }}>
            <h2 style={{ margin: '0 0 1.5rem 0', color: '#1f2937' }}>
              <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Icon name={isEditing ? "Pencil" : "Plus"} size={20} />
                {isEditing ? 'Editar Lote' : 'Agregar Nuevo Lote'}
              </span>
            </h2>
            
            <form onSubmit={(e) => { e.preventDefault(); handleSaveLote(); }}>
              <div style={{ display: 'grid', gap: '1rem', marginBottom: '2rem' }}>
                {/* Campo */}
                <div>
                  <label style={{ 
                    display: 'block', 
                    marginBottom: '0.5rem',
                    fontWeight: '500',
                    color: '#374151'
                  }}>
                    Campo *
                  </label>
                  <select
                    value={formData.campo_id}
                    onChange={(e) => handleInputChange('campo_id', Number(e.target.value))}
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.375rem',
                      fontSize: '1rem'
                    }}
                    required
                  >
                    <option value={0}>Seleccionar campo</option>
                    {campos.map(campo => (
                      <option key={campo.id} value={campo.id}>
                        {campo.nombre} ({campo.superficie} ha)
                      </option>
                    ))}
                  </select>
                </div>

                {/* Información de superficie disponible */}
                {formData.campo_id > 0 && (
                  <div style={{
                    padding: '1rem',
                    backgroundColor: '#f0f9ff',
                    borderRadius: '0.5rem',
                    border: '1px solid #0ea5e9'
                  }}>
                    <h4 style={{ margin: '0 0 0.5rem 0', color: '#0c4a6e', fontSize: '1rem' }}>
                      <Icon name="BarChart" size={16} style={{ marginRight: '4px' }} /> Información del Campo
                    </h4>
                    <div style={{ fontSize: '0.875rem', color: '#0c4a6e' }}>
                      <p style={{ margin: '0.25rem 0' }}>
                        <strong>Superficie total del campo:</strong> {campos.find(c => c.id === formData.campo_id)?.superficie} ha
                      </p>
                      <p style={{ margin: '0.25rem 0' }}>
                        <strong>Superficie disponible:</strong> {getSuperficieDisponible(formData.campo_id).toFixed(2)} ha
                      </p>
                    </div>
                  </div>
                )}

                {/* Nombre del lote */}
                <div>
                  <label style={{ 
                    display: 'block', 
                    marginBottom: '0.5rem',
                    fontWeight: '500',
                    color: '#374151'
                  }}>
                    Nombre del Lote *
                  </label>
                  <input
                    type="text"
                    value={formData.nombre}
                    onChange={(e) => handleInputChange('nombre', e.target.value)}
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.375rem',
                      fontSize: '1rem'
                    }}
                    placeholder="Ej: Lote A1"
                    required
                  />
                </div>

                {/* Superficie */}
                <div>
                  <label style={{ 
                    display: 'block', 
                    marginBottom: '0.5rem',
                    fontWeight: '500',
                    color: '#374151'
                  }}>
                    Superficie (hectáreas) *
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    max={formData.campo_id > 0 ? getSuperficieDisponible(formData.campo_id) : undefined}
                    value={formData.superficie}
                    onChange={(e) => handleInputChange('superficie', e.target.value === '' ? '' : parseFloat(e.target.value) || '')}
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.375rem',
                      fontSize: '1rem'
                    }}
                    placeholder="Ej: 25.50"
                    required
                  />
                  {formData.campo_id > 0 && (
                    <p style={{ fontSize: '0.75rem', color: '#6b7280', marginTop: '0.25rem' }}>
                      Máximo: {getSuperficieDisponible(formData.campo_id).toFixed(2)} ha
                    </p>
                  )}
                </div>

                {/* Nota informativa */}
                <div style={{
                  background: '#e8f5e9',
                  border: '1px solid #4caf50',
                  borderRadius: '8px',
                  padding: '12px',
                  fontSize: '13px',
                  color: '#1b5e20'
                }}>
                  <Icon name="Lightbulb" size={14} style={{ marginRight: '4px', verticalAlign: 'middle' }} /> <strong>Nota:</strong> El lote se creará en estado <strong>DISPONIBLE</strong> para siembra. 
                  El cultivo se asignará cuando hagas clic en <strong><Icon name="Sprout" size={12} style={{ marginRight: '2px', verticalAlign: 'middle' }} /> Sembrar</strong>.
                </div>

                {/* Descripción */}
                <div>
                  <label style={{ 
                    display: 'block', 
                    marginBottom: '0.5rem',
                    fontWeight: '500',
                    color: '#374151'
                  }}>
                    Descripción (Opcional)
                  </label>
                  <textarea
                    value={formData.descripcion}
                    onChange={(e) => handleInputChange('descripcion', e.target.value)}
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.375rem',
                      fontSize: '1rem',
                      minHeight: '80px',
                      resize: 'vertical'
                    }}
                    placeholder="Descripción del lote, tipo de cultivo, etc."
                  />
                </div>

                {/* Tipo de Suelo */}
                <div>
                  <label style={{ 
                    display: 'block', 
                    marginBottom: '0.5rem',
                    fontWeight: '500',
                    color: '#374151'
                  }}>
                    Tipo de Suelo *
                  </label>
                  <select
                    value={formData.tipoSuelo}
                    onChange={(e) => handleInputChange('tipoSuelo', e.target.value)}
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.375rem',
                      fontSize: '1rem'
                    }}
                    required
                  >
                    <option value="Franco Limoso">Franco Limoso</option>
                    <option value="Franco Arenoso">Franco Arenoso</option>
                    <option value="Arcilloso">Arcilloso</option>
                    <option value="Arenoso">Arenoso</option>
                    <option value="Limoso">Limoso</option>
                    <option value="Franco Arcilloso">Franco Arcilloso</option>
                  </select>
                </div>

                {/* Error de validación */}
                {validationError && (
                  <div style={{
                    padding: '1rem',
                    backgroundColor: '#ffebee',
                    color: '#c62828',
                    borderRadius: '0.5rem',
                    border: '1px solid #ffcdd2',
                    fontSize: '0.875rem'
                  }}>
                    <Icon name="AlertTriangle" size={16} style={{ marginRight: '4px', verticalAlign: 'middle' }} /> {validationError}
                  </div>
                )}
              </div>

              {/* Botones de acción */}
              <div style={{
                display: 'flex',
                gap: '1rem',
                justifyContent: 'flex-end',
                paddingTop: '1rem',
                borderTop: '1px solid #e5e7eb'
              }}>
                <button
                  type="button"
                  onClick={closeModal}
                  style={{
                    padding: '0.75rem 1.5rem',
                    backgroundColor: '#6b7280',
                    color: 'white',
                    border: 'none',
                    borderRadius: '0.375rem',
                    cursor: 'pointer',
                    fontSize: '1rem'
                  }}
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  style={{
                    padding: '0.75rem 1.5rem',
                    backgroundColor: '#4CAF50',
                    color: 'white',
                    border: 'none',
                    borderRadius: '0.375rem',
                    cursor: 'pointer',
                    fontSize: '1rem'
                  }}
                >
                  {isEditing ? 'Guardar Cambios' : 'Crear Lote'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal de Historial de Labores */}
      {showHistorialModal && selectedLoteForHistorial && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 1000,
          padding: '20px'
        }}>
          <div style={{
            background: 'white',
            borderRadius: '10px',
            padding: '2rem',
            maxWidth: '900px',
            width: '95%',
            maxHeight: '90vh',
            overflowY: 'auto'
          }}>
            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: '1.5rem',
              borderBottom: '1px solid #e5e7eb',
              paddingBottom: '1rem'
            }}>
              <h2 style={{ margin: 0, color: '#1f2937' }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Icon name="Clipboard" size={20} /> Historial de Labores - {selectedLoteForHistorial.nombre}
                </span>
              </h2>
              <button
                onClick={() => setShowHistorialModal(false)}
                style={{
                  background: 'none',
                  border: 'none',
                  fontSize: '24px',
                  cursor: 'pointer',
                  color: '#6b7280'
                }}
              >
                <Icon name="X" size={24} />
              </button>
            </div>

            {/* Información del lote */}
            <div style={{
              background: '#f8f9fa',
              padding: '1rem',
              borderRadius: '8px',
              marginBottom: '1.5rem',
              border: '1px solid #e5e7eb'
            }}>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
                <div>
                  <strong>Campo:</strong> {getCampoName(selectedLoteForHistorial.campo_id)}
                </div>
                <div>
                  <strong>Superficie:</strong> {selectedLoteForHistorial.superficie} ha
                </div>
                <div>
                  <strong>Cultivo:</strong> {selectedLoteForHistorial.cultivo}
                </div>
                <div>
                  <strong>Estado:</strong>{' '}
                  <EstadoLoteDisplay 
                    estado={selectedLoteForHistorial.estado} 
                    estadoConfigurado={selectedLoteForHistorial.estadoConfigurado}
                  />
                </div>
              </div>
            </div>

            {/* Lista de labores */}
            {(() => {
              const laboresDelLote = getLaboresPorLote(selectedLoteForHistorial.id!);
              
              if (laboresDelLote.length === 0) {
                return (
                  <div style={{
                    padding: '3rem',
                    textAlign: 'center',
                    color: '#6b7280',
                    background: '#f9fafb',
                    borderRadius: '8px',
                    border: '1px solid #e5e7eb'
                  }}>
                    <div style={{ marginBottom: '1rem', display: 'flex', justifyContent: 'center' }}>
                      <Icon name="FileText" size={48} />
                    </div>
                    <h3 style={{ margin: '0 0 0.5rem 0', color: '#374151' }}>Sin labores registradas</h3>
                    <p style={{ margin: 0, fontSize: '14px' }}>
                      No se han registrado labores para este lote aún.
                    </p>
                  </div>
                );
              }

              return (
                <div>
                  <h3 style={{ margin: '0 0 1rem 0', color: '#374151' }}>
                    Labores Realizadas ({laboresDelLote.length})
                  </h3>
                  
                  <div style={{ display: 'grid', gap: '1rem' }}>
                    {laboresDelLote
                      .sort((a, b) => new Date(b.fecha).getTime() - new Date(a.fecha).getTime())
                      .map((labor) => (
                        <div key={labor.id} style={{
                          border: '1px solid #e5e7eb',
                          borderRadius: '8px',
                          padding: '1rem',
                          background: '#fafafa'
                        }}>
                          <div style={{
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'flex-start',
                            marginBottom: '0.5rem'
                          }}>
                            <div>
                              <h4 style={{ margin: '0 0 0.25rem 0', color: '#1f2937' }}>
                                {formatearTipoLabor(labor.tipo)}
                              </h4>
                              <p style={{ margin: '0 0 0.5rem 0', color: '#6b7280', fontSize: '14px' }}>
                                {labor.observaciones}
                              </p>
                            </div>
                            <div style={{ textAlign: 'right' }}>
                              <span style={{
                                padding: '4px 8px',
                                borderRadius: '12px',
                                fontSize: '12px',
                                fontWeight: 'bold',
                                background: `${getEstadoColor(labor.estado)}20`,
                                color: getEstadoColor(labor.estado)
                              }}>
                                {labor.estado.charAt(0).toUpperCase() + labor.estado.slice(1)}
                              </span>
                            </div>
                          </div>

                          <div style={{
                            display: 'grid',
                            gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
                            gap: '0.5rem',
                            marginBottom: '0.75rem',
                            fontSize: '14px'
                          }}>
                            <div><strong>Fecha:</strong> {new Date(labor.fecha).toLocaleDateString('es-ES')}</div>
                            <div><strong>Responsable:</strong> {labor.responsable}</div>
                            <div><strong>Horas:</strong> {labor.horas_trabajo || 0}h</div>
                            <div><strong>Costo:</strong> {formatCurrency(labor.costo_total || 0)}</div>
                          </div>

                          {/* Insumos utilizados */}
                          {labor.insumos_usados && labor.insumos_usados.length > 0 && (
                            <div style={{ marginBottom: '0.75rem' }}>
                              <strong style={{ fontSize: '14px', color: '#374151', display: 'flex', alignItems: 'center', gap: '4px' }}>
                                <Icon name="FlaskConical" size={14} /> Insumos:
                              </strong>
                              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginTop: '0.25rem' }}>
                                {labor.insumos_usados.map((insumo: any, index: number) => (
                                  <span key={index} style={{
                                    background: '#e3f2fd',
                                    color: '#1976d2',
                                    padding: '2px 6px',
                                    borderRadius: '4px',
                                    fontSize: '12px'
                                  }}>
                                    {insumo.insumo_nombre} ({insumo.cantidad_usada} {insumo.unidad_medida})
                                  </span>
                                ))}
                              </div>
                            </div>
                          )}

                          {/* Maquinaria utilizada */}
                          {labor.maquinaria_asignada && labor.maquinaria_asignada.length > 0 && (
                            <div>
                              <strong style={{ fontSize: '14px', color: '#374151', display: 'flex', alignItems: 'center', gap: '4px' }}>
                                <Icon name="Tractor" size={14} /> Maquinaria:
                              </strong>
                              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginTop: '0.25rem' }}>
                                {labor.maquinaria_asignada.map((maq: any, index: number) => (
                                  <span key={index} style={{
                                    background: '#f3e5f5',
                                    color: '#7b1fa2',
                                    padding: '2px 6px',
                                    borderRadius: '4px',
                                    fontSize: '12px'
                                  }}>
                                    {maq.maquinaria_nombre} ({maq.horas_uso}h)
                                  </span>
                                ))}
                              </div>
                            </div>
                          )}
                        </div>
                      ))}
                  </div>

                  {/* Resumen de costos */}
                  <div style={{
                    background: '#e8f5e8',
                    padding: '1rem',
                    borderRadius: '8px',
                    marginTop: '1.5rem',
                    border: '1px solid #c8e6c9'
                  }}>
                    <h4 style={{ margin: '0 0 0.5rem 0', color: '#2e7d32', display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <Icon name="DollarSign" size={18} /> Resumen de Costos
                    </h4>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '0.5rem', fontSize: '14px' }}>
                      <div><strong>Total de labores:</strong> {laboresDelLote.length}</div>
                      <div><strong>Costo total:</strong> {formatCurrency(laboresDelLote.reduce((sum, l) => sum + (l.costo_total || 0), 0))}</div>
                      <div><strong>Horas totales:</strong> {laboresDelLote.reduce((sum, l) => sum + (l.horas_trabajo || 0), 0)}h</div>
                      <div><strong>Última labor:</strong> {new Date(Math.max(...laboresDelLote.map(l => new Date(l.fecha).getTime()))).toLocaleDateString('es-ES')}</div>
                    </div>
                  </div>
                </div>
              );
            })()}
          </div>
        </div>
      )}
      
      {/* Modal de Reset */}
      {showResetModal && loteParaReset && (
        <ResetLoteModal
          lote={loteParaReset}
          onClose={() => {
            setShowResetModal(false);
            setLoteParaReset(null);
          }}
          onSuccess={() => {
            cargarDatos();
          }}
        />
      )}
      
      {/* Modal de Acciones Especiales */}
      {showAccionModal && loteParaAccion && (
        <AccionLoteModal
          lote={loteParaAccion}
          accion={tipoAccion}
          onClose={() => {
            setShowAccionModal(false);
            setLoteParaAccion(null);
          }}
          onSuccess={() => {
            cargarDatos(); // Recargar datos después de la acción
          }}
        />
      )}
    </div>
  );
};

export default LotesManagement;
