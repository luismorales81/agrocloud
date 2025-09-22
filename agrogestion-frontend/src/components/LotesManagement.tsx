import React, { useState, useEffect } from 'react';
import { useCurrencyContext } from '../contexts/CurrencyContext';

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
  tipoSuelo?: string;
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
  const { formatCurrency } = useCurrencyContext();
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
      estado: 'activo',
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
      estado: 'activo',
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

  // Datos simulados de lotes
  const lotesSimulados: Lote[] = [
    {
      id: 1,
      nombre: 'Lote A1',
      superficie: 25.5,
      cultivo: 'Soja',
      campo_id: 1,
      estado: 'activo',
      descripcion: 'Lote de soja de primera',
      tipoSuelo: 'Franco Limoso'
    },
    {
      id: 2,
      nombre: 'Lote A2',
      superficie: 30.25,
      cultivo: 'Maíz',
      campo_id: 1,
      estado: 'activo',
      descripcion: 'Lote de maíz tardío',
      tipoSuelo: 'Franco Arenoso'
    },
    {
      id: 3,
      nombre: 'Lote B1',
      superficie: 40.0,
      cultivo: 'Trigo',
      campo_id: 2,
      estado: 'activo',
      descripcion: 'Lote de trigo de invierno',
      tipoSuelo: 'Arcilloso'
    }
  ];

  const [formData, setFormData] = useState<Lote>({
    nombre: '',
    superficie: '',
    cultivo: '',
    campo_id: 0,
    estado: 'activo',
    descripcion: '',
    tipoSuelo: 'Franco Limoso',
    fechaSiembra: '',
    fechaCosechaEsperada: ''
  });

  // Datos simulados de labores
  const laboresSimuladas: Labor[] = [
    {
      id: 1,
      tipo: 'siembra',
      fecha: '2024-11-15',
      fecha_fin: '2024-11-15',
      observaciones: 'Siembra de soja con densidad de 25 plantas/m²',
      lote_id: 1,
      lote_nombre: 'Lote A1',
      estado: 'completada',
      responsable: 'Juan Pérez',
      horas_trabajo: 8,
      costo_total: 4250560,
      progreso: 100,
      insumos_usados: [
        { insumo_nombre: 'Semilla Soja DM 53i54', cantidad_usada: 500, unidad_medida: 'kg', costo_total: 4250000 }
      ],
      maquinaria_asignada: [
        { maquinaria_nombre: 'Tractor John Deere 5075E', horas_uso: 8, costo_total: 360 },
        { maquinaria_nombre: 'Sembradora de Precisión', horas_uso: 8, costo_total: 200 }
      ]
    },
    {
      id: 2,
      tipo: 'fertilizacion',
      fecha: '2024-12-05',
      fecha_fin: '2024-12-05',
      observaciones: 'Aplicación de fertilizante NPK 15-15-15',
      lote_id: 1,
      lote_nombre: 'Lote A1',
      estado: 'completada',
      responsable: 'María González',
      horas_trabajo: 6,
      costo_total: 1250000,
      progreso: 100,
      insumos_usados: [
        { insumo_nombre: 'Fertilizante NPK 15-15-15', cantidad_usada: 250, unidad_medida: 'kg', costo_total: 1250000 }
      ],
      maquinaria_asignada: [
        { maquinaria_nombre: 'Tractor John Deere 5075E', horas_uso: 6, costo_total: 270 }
      ]
    },
    {
      id: 3,
      tipo: 'pulverizacion',
      fecha: '2024-12-20',
      fecha_fin: '2024-12-20',
      observaciones: 'Control de malezas con glifosato',
      lote_id: 1,
      lote_nombre: 'Lote A1',
      estado: 'completada',
      responsable: 'Carlos Rodríguez',
      horas_trabajo: 4,
      costo_total: 850000,
      progreso: 100,
      insumos_usados: [
        { insumo_nombre: 'Glifosato 48%', cantidad_usada: 30, unidad_medida: 'L', costo_total: 840000 }
      ],
      maquinaria_asignada: [
        { maquinaria_nombre: 'Pulverizadora Jacto 2000', horas_uso: 4, costo_total: 140 }
      ]
    },
    {
      id: 4,
      tipo: 'siembra',
      fecha: '2024-11-20',
      fecha_fin: '2024-11-20',
      observaciones: 'Siembra de maíz tardío',
      lote_id: 2,
      lote_nombre: 'Lote A2',
      estado: 'completada',
      responsable: 'Juan Pérez',
      horas_trabajo: 10,
      costo_total: 3800000,
      progreso: 100,
      insumos_usados: [
        { insumo_nombre: 'Semilla Maíz DK 7210', cantidad_usada: 600, unidad_medida: 'kg', costo_total: 3600000 }
      ],
      maquinaria_asignada: [
        { maquinaria_nombre: 'Tractor John Deere 5075E', horas_uso: 10, costo_total: 450 },
        { maquinaria_nombre: 'Sembradora de Precisión', horas_uso: 10, costo_total: 250 }
      ]
    },
    {
      id: 5,
      tipo: 'cosecha',
      fecha: '2024-03-15',
      fecha_fin: '2024-03-15',
      observaciones: 'Cosecha de soja con rendimiento de 3.2 tn/ha',
      lote_id: 1,
      lote_nombre: 'Lote A1',
      estado: 'completada',
      responsable: 'Roberto Silva',
      horas_trabajo: 12,
      costo_total: 2800000,
      progreso: 100,
      insumos_usados: [],
      maquinaria_asignada: [
        { maquinaria_nombre: 'Cosechadora New Holland CR8.90', horas_uso: 12, costo_total: 1440 }
      ]
    }
  ];

  // Estado para cultivos
  const [cultivos, setCultivos] = useState<string[]>([]);

  useEffect(() => {
    cargarDatos();
  }, []);

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

      const response = await fetch('http://localhost:8080/api/campos', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      
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

      const response = await fetch('http://localhost:8080/api/lotes', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      
      // Mapear los datos de la API al formato del frontend
      const lotesMapeados: Lote[] = data.map((lote: any) => ({
        id: lote.id,
        nombre: lote.nombre,
        superficie: lote.areaHectareas || 0,
        cultivo: lote.cultivoActual || '',
        campo_id: lote.campo?.id || lote.campoId || 0,
        estado: lote.estado?.toLowerCase() || 'activo',
        descripcion: lote.descripcion || '',
        tipoSuelo: lote.tipoSuelo || 'Franco Limoso',
        fechaSiembra: lote.fechaSiembra || '',
        fechaCosechaEsperada: lote.fechaCosechaEsperada || ''
      }));

      setLotes(lotesMapeados);
    } catch (error) {
      console.error('Error cargando lotes:', error);
      setError('Error al cargar los lotes. Usando datos de ejemplo.');
      // Fallback a datos simulados
      setLotes(lotesSimulados);
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

      const response = await fetch('http://localhost:8080/api/labores', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      
      // Mapear los datos de la API al formato del frontend
      const laboresMapeadas: Labor[] = data.map((labor: any) => ({
        id: labor.id,
        nombre: labor.nombre,
        tipo: labor.tipo || '',
        fecha: labor.fechaInicio || '',
        lote_id: labor.loteId || 0,
        lote_nombre: labor.lote?.nombre || '',
        responsable: labor.responsable || '',
        horas_trabajo: labor.horasTrabajo || 0,
        costo_total: labor.costoTotal || 0,
        descripcion: labor.descripcion || '',
        estado: labor.estado || 'planificada',
        observaciones: labor.observaciones || ''
      }));

      setLabores(laboresMapeadas);
    } catch (error) {
      console.error('Error cargando labores:', error);
      setError('Error al cargar las labores. Usando datos de ejemplo.');
      // Fallback a datos simulados
      setLabores(laboresSimuladas);
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

      const response = await fetch('http://localhost:8080/api/cultivos', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      });

      if (response.ok) {
        const data = await response.json();
        // Extraer nombres únicos de cultivos
        const nombresCultivos = [...new Set(data.map((cultivo: any) => cultivo.nombre))];
        setCultivos(nombresCultivos);
      } else {
        console.warn('Error cargando cultivos, usando lista básica');
        setCultivos(['Soja', 'Maíz', 'Trigo', 'Girasol', 'Sorgo', 'Cebada', 'Avena', 'Arroz']);
      }
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
      .reduce((sum, lote) => sum + lote.superficie, 0);

    const superficieTotal = superficieLotesExistentes + superficie;
    
    return superficieTotal <= campo.superficie;
  };

  const handleAgregarLote = () => {
    setFormData({
      nombre: '',
      superficie: '',
      cultivo: '',
      campo_id: 0,
      estado: 'activo',
      descripcion: '',
      tipoSuelo: 'Franco Limoso',
      fechaSiembra: '',
      fechaCosechaEsperada: ''
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
    if (!formData.nombre || !formData.cultivo || formData.campo_id === 0) {
      alert('Por favor complete todos los campos obligatorios');
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
        .reduce((sum, lote) => sum + lote.superficie, 0);
      
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
        estado: formData.estado.toUpperCase(),
        tipoSuelo: formData.tipoSuelo,
        cultivoActual: formData.cultivo,
        fechaSiembra: formData.fechaSiembra || null,
        fechaCosechaEsperada: formData.fechaCosechaEsperada || null,
        activo: true,
        campo: { id: formData.campo_id }
      };

      let response;
      if (isEditing && selectedLote) {
        // Editar lote existente
        response = await fetch(`http://localhost:8080/api/lotes/${selectedLote.id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify(loteData)
        });
      } else {
        // Crear nuevo lote
        response = await fetch('http://localhost:8080/api/lotes', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify(loteData)
        });
      }

      if (response.ok) {
        alert(isEditing ? 'Lote actualizado exitosamente' : 'Lote creado exitosamente');
        closeModal();
        
        // Recargar lotes del backend
        await cargarLotes();
      } else {
        const errorData = await response.text();
        console.error('Error del servidor:', errorData);
        alert('Error al guardar el lote. Por favor, inténtalo de nuevo.');
      }
    } catch (error) {
      console.error('Error de conexión:', error);
      alert('Error de conexión. Por favor, verifica tu conexión e intenta nuevamente.');
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

        const response = await fetch(`http://localhost:8080/api/lotes/${id}`, {
          method: 'DELETE',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok || response.status === 204) {
          // Eliminar del estado local solo si la API confirma la eliminación
          setLotes(prev => prev.filter(lote => lote.id !== id));
          alert('Lote eliminado exitosamente');
        } else {
          console.error('Error al eliminar el lote:', response.status, response.statusText);
          alert('Error al eliminar el lote. Por favor, inténtalo de nuevo.');
        }
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
      estado: 'activo',
      descripcion: '',
      tipoSuelo: 'Franco Limoso',
      fechaSiembra: '',
      fechaCosechaEsperada: ''
    });
    setValidationError('');
  };

  const getCampoName = (campoId: number) => {
    const campo = campos.find(c => c.id === campoId);
    return campo ? campo.nombre : 'Campo no encontrado';
  };

  const getEstadoTraducido = (estado: string) => {
    const traducciones: { [key: string]: string } = {
      'en_cultivo': 'En Cultivo',
      'disponible': 'Disponible',
      'activo': 'Activo',
      'inactivo': 'Inactivo'
    };
    return traducciones[estado] || estado;
  };

  const getSuperficieDisponible = (campoId: number) => {
    const campo = campos.find(c => c.id === campoId);
    if (!campo) return 0;

    const superficieLotesExistentes = lotes
      .filter(lote => lote.campo_id === campoId && lote.id !== selectedLote?.id)
      .reduce((sum, lote) => sum + lote.superficie, 0);

    return campo.superficie - superficieLotesExistentes;
  };

  // Filtrar lotes
  const filteredLotes = lotes.filter(lote => {
    const matchesSearch = lote.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         lote.cultivo.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCultivo = selectedCultivo === '' || lote.cultivo === selectedCultivo;
    return matchesSearch && matchesCultivo;
  });

  // Estadísticas
  const totalSuperficie = lotes.reduce((sum, lote) => sum + lote.superficie, 0);
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
        <h1 style={{ margin: '0 0 10px 0', fontSize: '24px' }}>🏞️ Gestión de Lotes</h1>
        <p style={{ margin: '0', opacity: '0.9' }}>
          Divide campos en lotes y asigna cultivos específicos
        </p>
      </div>

      {/* Botones de acción */}
      <div style={{ marginBottom: '20px', display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
        <button
          onClick={handleAgregarLote}
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
          ➕ Agregar Lote
        </button>
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
          <h3 style={{ margin: '0', color: '#495057' }}>📊 Lista de Lotes ({filteredLotes.length})</h3>
        </div>

        {filteredLotes.length === 0 ? (
          <div style={{ padding: '40px', textAlign: 'center', color: '#666' }}>
            No se encontraron lotes
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
                {filteredLotes.map(lote => (
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
                      <span style={{
                        background: lote.estado === 'activo' ? '#e8f5e8' : 
                                   lote.estado === 'inactivo' ? '#ffebee' : '#fff3e0',
                        color: lote.estado === 'activo' ? '#2e7d32' : 
                               lote.estado === 'inactivo' ? '#c62828' : '#e65100',
                        padding: '4px 8px',
                        borderRadius: '12px',
                        fontSize: '12px',
                        fontWeight: 'bold'
                      }}>
                        {getEstadoTraducido(lote.estado)}
                      </span>
                    </td>
                    <td style={{ padding: '12px' }}>
                      <div style={{ display: 'flex', gap: '8px' }}>
                        <button
                          onClick={() => handleVerHistorial(lote)}
                          style={{
                            background: '#9c27b0',
                            color: 'white',
                            border: 'none',
                            padding: '6px 12px',
                            borderRadius: '4px',
                            cursor: 'pointer',
                            fontSize: '12px'
                          }}
                        >
                          📋 Historial
                        </button>
                        <button
                          onClick={() => handleEditar(lote)}
                          style={{
                            background: '#2196F3',
                            color: 'white',
                            border: 'none',
                            padding: '6px 12px',
                            borderRadius: '4px',
                            cursor: 'pointer',
                            fontSize: '12px'
                          }}
                        >
                          ✏️ Editar
                        </button>
                        <button
                          onClick={() => handleEliminar(lote.id!)}
                          style={{
                            background: '#f44336',
                            color: 'white',
                            border: 'none',
                            padding: '6px 12px',
                            borderRadius: '4px',
                            cursor: 'pointer',
                            fontSize: '12px'
                          }}
                        >
                          🗑️ Eliminar
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

      {/* Estadísticas */}
      <div style={{ 
        background: '#e8f5e8', 
        padding: '15px', 
        borderRadius: '8px' 
      }}>
        <h4 style={{ margin: '0 0 10px 0', color: '#2e7d32' }}>📈 Estadísticas</h4>
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
              {isEditing ? '✏️ Editar Lote' : '➕ Agregar Nuevo Lote'}
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
                      📊 Información del Campo
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

                {/* Cultivo */}
                <div>
                  <label style={{ 
                    display: 'block', 
                    marginBottom: '0.5rem',
                    fontWeight: '500',
                    color: '#374151'
                  }}>
                    Cultivo *
                  </label>
                  <select
                    value={formData.cultivo}
                    onChange={(e) => {
                      handleInputChange('cultivo', e.target.value);
                      // Recalcular fecha de cosecha si ya hay fecha de siembra
                      if (formData.fechaSiembra && e.target.value) {
                        const fechaCosecha = calcularFechaCosecha(formData.fechaSiembra, e.target.value);
                        handleInputChange('fechaCosechaEsperada', fechaCosecha);
                      }
                    }}
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.375rem',
                      fontSize: '1rem'
                    }}
                    required
                  >
                    <option value="">Seleccionar cultivo</option>
                    {cultivos.map(cultivo => (
                      <option key={cultivo} value={cultivo}>{cultivo}</option>
                    ))}
                  </select>
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

                {/* Estado */}
                <div>
                  <label style={{ 
                    display: 'block', 
                    marginBottom: '0.5rem',
                    fontWeight: '500',
                    color: '#374151'
                  }}>
                    Estado *
                  </label>
                  <select
                    value={formData.estado}
                    onChange={(e) => handleInputChange('estado', e.target.value)}
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.375rem',
                      fontSize: '1rem'
                    }}
                    required
                  >
                    <option value="activo">Activo</option>
                    <option value="inactivo">Inactivo</option>
                    <option value="en_mantenimiento">En Mantenimiento</option>
                  </select>
                </div>

                {/* Descripción */}
                <div>
                  <label style={{ 
                    display: 'block', 
                    marginBottom: '0.5rem',
                    fontWeight: '500',
                    color: '#374151'
                  }}>
                    Descripción
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

                {/* Fecha de Siembra */}
                <div>
                  <label style={{ 
                    display: 'block', 
                    marginBottom: '0.5rem',
                    fontWeight: '500',
                    color: '#374151'
                  }}>
                    Fecha de Siembra
                  </label>
                  <input
                    type="date"
                    value={formData.fechaSiembra}
                    onChange={(e) => {
                      handleInputChange('fechaSiembra', e.target.value);
                      // Calcular fecha de cosecha esperada automáticamente
                      if (e.target.value && formData.cultivo) {
                        const fechaCosecha = calcularFechaCosecha(e.target.value, formData.cultivo);
                        handleInputChange('fechaCosechaEsperada', fechaCosecha);
                      }
                    }}
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.375rem',
                      fontSize: '1rem'
                    }}
                  />
                </div>

                {/* Fecha de Cosecha Esperada */}
                <div>
                  <label style={{ 
                    display: 'block', 
                    marginBottom: '0.5rem',
                    fontWeight: '500',
                    color: '#374151'
                  }}>
                    Fecha de Cosecha Esperada
                  </label>
                  <input
                    type="date"
                    value={formData.fechaCosechaEsperada}
                    onChange={(e) => handleInputChange('fechaCosechaEsperada', e.target.value)}
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.375rem',
                      fontSize: '1rem',
                      backgroundColor: '#f9fafb'
                    }}
                    readOnly
                  />
                  <small style={{ color: '#6b7280', fontSize: '0.75rem' }}>
                    Se calcula automáticamente según el cultivo seleccionado
                  </small>
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
                    ⚠️ {validationError}
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
                📋 Historial de Labores - {selectedLoteForHistorial.nombre}
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
                ✕
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
                  <strong>Estado:</strong> {getEstadoTraducido(selectedLoteForHistorial.estado)}
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
                    <div style={{ fontSize: '48px', marginBottom: '1rem' }}>📝</div>
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
                            <div><strong>Progreso:</strong> {labor.progreso || 0}%</div>
                          </div>

                          {/* Insumos utilizados */}
                          {labor.insumos_usados && labor.insumos_usados.length > 0 && (
                            <div style={{ marginBottom: '0.75rem' }}>
                              <strong style={{ fontSize: '14px', color: '#374151' }}>🧪 Insumos:</strong>
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
                              <strong style={{ fontSize: '14px', color: '#374151' }}>🚜 Maquinaria:</strong>
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
                    <h4 style={{ margin: '0 0 0.5rem 0', color: '#2e7d32' }}>💰 Resumen de Costos</h4>
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
    </div>
  );
};

export default LotesManagement;
