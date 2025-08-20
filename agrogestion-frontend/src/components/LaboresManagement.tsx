import React, { useState, useEffect } from 'react';
import { useCurrency } from '../hooks/useCurrency';

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
  progreso?: number; // 0-100
}

interface MaquinariaAsignada {
  maquinaria_id: number;
  maquinaria_nombre: string;
  horas_uso: number;
  kilometros_recorridos: number;
  costo_total: number;
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

const LaboresManagement: React.FC = () => {
  const { formatCurrency } = useCurrency();
  const [labores, setLabores] = useState<Labor[]>([]);
  const [lotes, setLotes] = useState<any[]>([]);
  const [insumos, setInsumos] = useState<Insumo[]>([]);
  const [maquinaria, setMaquinaria] = useState<Maquinaria[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [editingLabor, setEditingLabor] = useState<Labor | null>(null);
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
    responsable: '',
    progreso: 0
  });

  const tiposLabor = [
    'siembra',
    'fertilizacion',
    'cosecha',
    'riego',
    'pulverizacion',
    'arado',
    'rastra',
    'desmalezado',
    'aplicacion_herbicida',
    'aplicacion_insecticida',
    'monitoreo',
    'otro'
  ];

  const estadosLabor = [
    { value: 'planificada', label: 'Planificada', color: '#6b7280' },
    { value: 'en_progreso', label: 'En Progreso', color: '#3b82f6' },
    { value: 'completada', label: 'Completada', color: '#10b981' },
    { value: 'interrumpida', label: 'Interrumpida', color: '#f59e0b' },
    { value: 'cancelada', label: 'Cancelada', color: '#ef4444' }
  ];

  // Cargar datos mock
  const loadData = async () => {
    setLoading(true);
    
    setTimeout(() => {
      // Mock lotes
      const mockLotes = [
        { id: 1, nombre: 'Lote A1', superficie: 25.5, cultivo: 'Soja' },
        { id: 2, nombre: 'Lote A2', superficie: 30.25, cultivo: 'Maíz' },
        { id: 3, nombre: 'Lote B1', superficie: 40.0, cultivo: 'Trigo' }
      ];

      // Mock insumos
      const mockInsumos: Insumo[] = [
        { id: 1, nombre: 'Semilla Soja DM 53i54', tipo: 'semilla', stock_actual: 2500, unidad_medida: 'kg', precio_unitario: 8500 },
        { id: 2, nombre: 'Fertilizante Urea 46%', tipo: 'fertilizante', stock_actual: 8000, unidad_medida: 'kg', precio_unitario: 450 },
        { id: 3, nombre: 'Glifosato 48%', tipo: 'herbicida', stock_actual: 150, unidad_medida: 'L', precio_unitario: 2800 },
        { id: 4, nombre: 'Aceite de Motor 15W40', tipo: 'lubricante', stock_actual: 80, unidad_medida: 'L', precio_unitario: 1200 }
      ];

      // Mock maquinaria
      const mockMaquinaria: Maquinaria[] = [
        { id: 1, nombre: 'Tractor John Deere 5075E', tipo: 'tractor', estado: 'activo', kilometros_uso: 12500.50, costo_por_hora: 45.00 },
        { id: 2, nombre: 'Cosechadora New Holland CR8.90', tipo: 'cosechadora', estado: 'activo', kilometros_uso: 8500.25, costo_por_hora: 120.00 },
        { id: 3, nombre: 'Pulverizadora Jacto 2000', tipo: 'pulverizadora', estado: 'activo', kilometros_uso: 3200.75, costo_por_hora: 35.00 },
        { id: 4, nombre: 'Sembradora de Precisión', tipo: 'sembradora', estado: 'activo', kilometros_uso: 1800.00, costo_por_hora: 25.00 }
      ];

      // Mock labores
      const mockLabores: Labor[] = [
        {
          id: 1,
          tipo: 'siembra',
          fecha: '2024-11-15',
          fecha_fin: '2024-11-15',
          observaciones: 'Siembra de soja con densidad de 25 plantas/m²',
          lote_id: 1,
          lote_nombre: 'Lote A1',
          estado: 'completada',
          insumos_usados: [
            { insumo_id: 1, insumo_nombre: 'Semilla Soja DM 53i54', cantidad_usada: 500, cantidad_planificada: 500, unidad_medida: 'kg', costo_unitario: 8500, costo_total: 4250000 }
          ],
          maquinaria_asignada: [
            { maquinaria_id: 1, maquinaria_nombre: 'Tractor John Deere 5075E', horas_uso: 8, kilometros_recorridos: 45.5, costo_total: 360 },
            { maquinaria_id: 4, maquinaria_nombre: 'Sembradora de Precisión', horas_uso: 8, kilometros_recorridos: 45.5, costo_total: 200 }
          ],
          responsable: 'Juan Pérez',
          horas_trabajo: 8,
          costo_total: 4250560,
          progreso: 100
        },
        {
          id: 2,
          tipo: 'fertilizacion',
          fecha: '2024-11-20',
          fecha_fin: '2024-11-20',
          observaciones: 'Aplicación de fertilizante nitrogenado',
          lote_id: 2,
          lote_nombre: 'Lote A2',
          estado: 'completada',
          insumos_usados: [
            { insumo_id: 2, insumo_nombre: 'Fertilizante Urea 46%', cantidad_usada: 1200, cantidad_planificada: 1200, unidad_medida: 'kg', costo_unitario: 450, costo_total: 540000 }
          ],
          maquinaria_asignada: [
            { maquinaria_id: 1, maquinaria_nombre: 'Tractor John Deere 5075E', horas_uso: 6, kilometros_recorridos: 32.0, costo_total: 270 }
          ],
          responsable: 'María González',
          horas_trabajo: 6,
          costo_total: 540270,
          progreso: 100
        },
        {
          id: 3,
          tipo: 'pulverizacion',
          fecha: '2024-11-25',
          observaciones: 'Control de malezas con glifosato',
          lote_id: 1,
          lote_nombre: 'Lote A1',
          estado: 'en_progreso',
          insumos_usados: [
            { insumo_id: 3, insumo_nombre: 'Glifosato 48%', cantidad_usada: 25, cantidad_planificada: 50, unidad_medida: 'L', costo_unitario: 2800, costo_total: 70000 }
          ],
          maquinaria_asignada: [
            { maquinaria_id: 3, maquinaria_nombre: 'Pulverizadora Jacto 2000', horas_uso: 3, kilometros_recorridos: 18.5, costo_total: 105 }
          ],
          responsable: 'Carlos López',
          horas_trabajo: 3,
          costo_total: 70105,
          progreso: 50
        }
      ];

      setLotes(mockLotes);
      setInsumos(mockInsumos);
      setMaquinaria(mockMaquinaria);
      setLabores(mockLabores);
      setLoading(false);
    }, 1000);
  };

  useEffect(() => {
    loadData();
  }, []);

  // Agregar insumo a la labor
  const addInsumoToLabor = () => {
    const insumoSelect = document.getElementById('insumo-select') as HTMLSelectElement;
    const cantidadInput = document.getElementById('cantidad-insumo') as HTMLInputElement;
    
    if (insumoSelect && cantidadInput) {
      const insumoId = parseInt(insumoSelect.value);
      const cantidad = parseFloat(cantidadInput.value);
      
      if (insumoId && cantidad > 0) {
        const insumo = insumos.find(i => i.id === insumoId);
        if (insumo) {
          // Validar stock disponible
          if (cantidad > insumo.stock_actual) {
            alert(`❌ Error: La cantidad solicitada (${cantidad} ${insumo.unidad_medida}) excede el stock disponible (${insumo.stock_actual} ${insumo.unidad_medida}) para ${insumo.nombre}`);
            return;
          }

          // Verificar si el insumo ya está agregado
          const insumoYaAgregado = formData.insumos_usados.find(i => i.insumo_id === insumo.id);
          if (insumoYaAgregado) {
            alert(`❌ Error: El insumo "${insumo.nombre}" ya está agregado a esta labor`);
            return;
          }

          const nuevoInsumo: InsumoUsado = {
            insumo_id: insumo.id,
            insumo_nombre: insumo.nombre,
            cantidad_usada: 0,
            cantidad_planificada: cantidad,
            unidad_medida: insumo.unidad_medida,
            costo_unitario: insumo.precio_unitario,
            costo_total: cantidad * insumo.precio_unitario
          };

          setFormData(prev => ({
            ...prev,
            insumos_usados: [...prev.insumos_usados, nuevoInsumo]
          }));

          // Limpiar campos
          insumoSelect.value = '';
          cantidadInput.value = '';
          
          alert(`✅ Insumo "${insumo.nombre}" agregado correctamente`);
        }
      } else {
        alert('❌ Error: Por favor seleccione un insumo y especifique una cantidad válida');
      }
    }
  };

  // Remover insumo de la labor
  const removeInsumoFromLabor = (index: number) => {
    setFormData(prev => ({
      ...prev,
      insumos_usados: prev.insumos_usados.filter((_, i) => i !== index)
    }));
  };

  // Actualizar cantidad usada de insumo
  const updateInsumoUsado = (index: number, cantidadUsada: number) => {
    setFormData(prev => {
      const nuevosInsumos = [...prev.insumos_usados];
      const insumo = nuevosInsumos[index];
      
      // Validar que la cantidad usada no exceda la planificada
      if (cantidadUsada > insumo.cantidad_planificada) {
        alert(`❌ Error: La cantidad usada (${cantidadUsada} ${insumo.unidad_medida}) no puede exceder la cantidad planificada (${insumo.cantidad_planificada} ${insumo.unidad_medida})`);
        return prev;
      }
      
      nuevosInsumos[index] = {
        ...insumo,
        cantidad_usada: cantidadUsada,
        costo_total: cantidadUsada * insumo.costo_unitario
      };
      return { ...prev, insumos_usados: nuevosInsumos };
    });
  };

  // Agregar maquinaria a la labor
  const addMaquinariaToLabor = () => {
    const maquinariaSelect = document.getElementById('maquinaria-select') as HTMLSelectElement;
    const horasInput = document.getElementById('horas-maquinaria') as HTMLInputElement;
    const kilometrosInput = document.getElementById('kilometros-maquinaria') as HTMLInputElement;
    
    if (maquinariaSelect && horasInput && kilometrosInput) {
      const maquinariaId = parseInt(maquinariaSelect.value);
      const horas = parseFloat(horasInput.value);
      const kilometros = parseFloat(kilometrosInput.value);
      
      if (maquinariaId && (horas > 0 || kilometros > 0)) {
        const maq = maquinaria.find(m => m.id === maquinariaId);
        if (maq) {
          // Verificar si la maquinaria ya está agregada
          const maquinariaYaAgregada = formData.maquinaria_asignada.find(m => m.maquinaria_id === maq.id);
          if (maquinariaYaAgregada) {
            alert(`❌ Error: La maquinaria "${maq.nombre}" ya está asignada a esta labor`);
            return;
          }

          const nuevaMaquinaria: MaquinariaAsignada = {
            maquinaria_id: maq.id,
            maquinaria_nombre: maq.nombre,
            horas_uso: horas,
            kilometros_recorridos: kilometros,
            costo_total: horas * maq.costo_por_hora
          };

          setFormData(prev => ({
            ...prev,
            maquinaria_asignada: [...prev.maquinaria_asignada, nuevaMaquinaria]
          }));

          // Limpiar campos
          maquinariaSelect.value = '';
          horasInput.value = '';
          kilometrosInput.value = '';
          
          alert(`✅ Maquinaria "${maq.nombre}" asignada correctamente`);
        }
      } else {
        alert('❌ Error: Por favor seleccione una maquinaria y especifique horas o kilómetros válidos');
      }
    }
  };

  // Remover maquinaria de la labor
  const removeMaquinariaFromLabor = (index: number) => {
    setFormData(prev => ({
      ...prev,
      maquinaria_asignada: prev.maquinaria_asignada.filter((_, i) => i !== index)
    }));
  };

  // Guardar labor
  const saveLabor = async () => {
    try {
      setLoading(true);
      
      if (!formData.tipo || !formData.fecha || !formData.lote_id) {
        alert('Por favor complete todos los campos obligatorios');
        return;
      }

      if (editingLabor) {
        // Actualizar labor existente
        const updatedLabores = labores.map(labor => 
          labor.id === editingLabor.id ? { ...formData, id: labor.id } : labor
        );
        setLabores(updatedLabores);
        
        // Actualizar stock de insumos si la labor se completó o interrumpió
        if (formData.estado === 'completada' || formData.estado === 'interrumpida') {
          updateInsumosStock(formData.insumos_usados);
        }
        
        alert('Labor actualizada exitosamente');
      } else {
        // Crear nueva labor
        const newLabor: Labor = {
          ...formData,
          id: labores.length + 1
        };
        setLabores(prev => [...prev, newLabor]);
        alert('Labor creada exitosamente');
      }

      resetForm();
    } catch (error) {
      console.error('Error guardando labor:', error);
      alert('Error al guardar la labor');
    } finally {
      setLoading(false);
    }
  };

  // Actualizar stock de insumos
  const updateInsumosStock = (insumosUsados: InsumoUsado[]) => {
    setInsumos(prev => prev.map(insumo => {
      const insumoUsado = insumosUsados.find(iu => iu.insumo_id === insumo.id);
      if (insumoUsado) {
        return {
          ...insumo,
          stock_actual: insumo.stock_actual - insumoUsado.cantidad_usada
        };
      }
      return insumo;
    }));
  };

  // Cambiar estado de labor
  const changeLaborEstado = (laborId: number, nuevoEstado: string) => {
    setLabores(prev => prev.map(labor => {
      if (labor.id === laborId) {
        const laborActualizada = { ...labor, estado: nuevoEstado as any };
        
        // Si se completa o interrumpe, actualizar stock
        if (nuevoEstado === 'completada' || nuevoEstado === 'interrumpida') {
          updateInsumosStock(labor.insumos_usados);
        }
        
        return laborActualizada;
      }
      return labor;
    }));
  };

  // Editar labor
  const editLabor = (labor: Labor) => {
    setEditingLabor(labor);
    setFormData(labor);
    setShowForm(true);
  };

  // Eliminar labor
  const deleteLabor = (id: number) => {
    if (confirm('¿Está seguro de que desea eliminar esta labor?')) {
      setLabores(prev => prev.filter(labor => labor.id !== id));
      alert('Labor eliminada exitosamente');
    }
  };

  // Resetear formulario
  const resetForm = () => {
    setFormData({
      tipo: '',
      fecha: '',
      observaciones: '',
      lote_id: 0,
      lote_nombre: '',
      estado: 'planificada',
      insumos_usados: [],
      responsable: '',
      progreso: 0
    });
    setEditingLabor(null);
    setShowForm(false);
  };

  // Filtrar labores
  const filteredLabores = labores.filter(labor => {
    const matchesSearch = labor.tipo.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         labor.observaciones.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         labor.lote_nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         labor.responsable.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesEstado = filterEstado === 'todos' || labor.estado === filterEstado;
    
    return matchesSearch && matchesEstado;
  });

  // Obtener estadísticas
  const estadisticas = {
    totalLabores: labores.length,
    enProgreso: labores.filter(l => l.estado === 'en_progreso').length,
    completadas: labores.filter(l => l.estado === 'completada').length,
    pendientes: labores.filter(l => l.estado === 'planificada').length,
    costoTotal: labores.reduce((sum, l) => sum + (l.costo_total || 0), 0)
  };

  // Obtener color de estado
  const getEstadoColor = (estado: string) => {
    const estadoObj = estadosLabor.find(e => e.value === estado);
    return estadoObj ? estadoObj.color : '#6b7280';
  };

  // Obtener etiqueta de estado
  const getEstadoLabel = (estado: string) => {
    const estadoObj = estadosLabor.find(e => e.value === estado);
    return estadoObj ? estadoObj.label : estado;
  };

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

      {/* Estadísticas */}
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
              {estadisticas.totalLabores}
            </div>
            <div style={{ fontSize: '14px', color: '#6b7280' }}>Total Labores</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#3b82f6' }}>
              {estadisticas.enProgreso}
            </div>
            <div style={{ fontSize: '14px', color: '#6b7280' }}>En Progreso</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#10b981' }}>
              {estadisticas.completadas}
            </div>
            <div style={{ fontSize: '14px', color: '#6b7280' }}>Completadas</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#ef4444' }}>
              {formatCurrency(estadisticas.costoTotal)}
            </div>
            <div style={{ fontSize: '14px', color: '#6b7280' }}>Costo Total</div>
          </div>
        </div>
      </div>

      {/* Botones de acción */}
      <div style={{ 
        marginBottom: isMobile ? '15px' : '20px', 
        display: 'flex', 
        gap: isMobile ? '8px' : '10px', 
        flexWrap: 'wrap' 
      }}>
        <button
          onClick={() => setShowForm(true)}
          style={{
            background: '#f59e0b',
            color: 'white',
            border: 'none',
            padding: isMobile ? '10px 16px' : '12px 24px',
            borderRadius: '8px',
            cursor: 'pointer',
            fontSize: isMobile ? '13px' : '14px',
            fontWeight: 'bold'
          }}
        >
          ➕ {isMobile ? 'Nueva' : 'Nueva Labor'}
        </button>
      </div>

      {/* Filtros */}
      <div style={{ 
        marginBottom: isMobile ? '15px' : '20px', 
        display: 'flex', 
        gap: isMobile ? '10px' : '15px', 
        flexWrap: 'wrap', 
        alignItems: 'center',
        flexDirection: isMobile ? 'column' : 'row'
      }}>
        <div style={{ 
          flex: isMobile ? 'none' : '1', 
          minWidth: isMobile ? '100%' : '250px',
          width: isMobile ? '100%' : 'auto'
        }}>
          <input
            type="text"
            placeholder="🔍 Buscar labores..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{
              width: '100%',
              padding: isMobile ? '8px' : '10px',
              border: '1px solid #ddd',
              borderRadius: '5px',
              fontSize: isMobile ? '13px' : '14px'
            }}
          />
        </div>
        <div style={{ width: isMobile ? '100%' : 'auto' }}>
          <select
            value={filterEstado}
            onChange={(e) => setFilterEstado(e.target.value)}
            style={{
              width: isMobile ? '100%' : 'auto',
              padding: isMobile ? '8px' : '10px',
              border: '1px solid #ddd',
              borderRadius: '5px',
              fontSize: isMobile ? '13px' : '14px'
            }}
          >
            <option value="todos">Todos los estados</option>
            {estadosLabor.map(estado => (
              <option key={estado.value} value={estado.value}>
                {estado.label}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Formulario */}
      {showForm && (
        <div style={{ 
          background: '#f9fafb', 
          padding: '20px', 
          borderRadius: '10px', 
          marginBottom: '20px',
          border: '1px solid #e5e7eb'
        }}>
          <h3 style={{ margin: '0 0 15px 0', color: '#374151' }}>
            {editingLabor ? '✏️ Editar Labor' : '📝 Nueva Labor'}
          </h3>
          
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '15px' }}>
            {/* Tipo de labor */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Tipo de Labor *:</label>
              <select
                value={formData.tipo}
                onChange={(e) => setFormData(prev => ({ ...prev, tipo: e.target.value }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              >
                <option value="">Seleccionar tipo</option>
                {tiposLabor.map(tipo => (
                  <option key={tipo} value={tipo}>
                    {tipo.charAt(0).toUpperCase() + tipo.slice(1)}
                  </option>
                ))}
              </select>
            </div>

            {/* Lote */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Lote *:</label>
              <select
                value={formData.lote_id}
                onChange={(e) => {
                  const loteId = parseInt(e.target.value);
                  const lote = lotes.find(l => l.id === loteId);
                  setFormData(prev => ({ 
                    ...prev, 
                    lote_id: loteId,
                    lote_nombre: lote ? lote.nombre : ''
                  }));
                }}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              >
                <option value="0">Seleccionar lote</option>
                {lotes.map(lote => (
                  <option key={lote.id} value={lote.id}>
                    {lote.nombre} - {lote.cultivo}
                  </option>
                ))}
              </select>
            </div>

            {/* Fecha */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Fecha *:</label>
              <input
                type="date"
                value={formData.fecha}
                onChange={(e) => setFormData(prev => ({ ...prev, fecha: e.target.value }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              />
            </div>

            {/* Estado */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Estado:</label>
              <select
                value={formData.estado}
                onChange={(e) => setFormData(prev => ({ ...prev, estado: e.target.value as any }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
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
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Responsable:</label>
              <input
                type="text"
                value={formData.responsable}
                onChange={(e) => setFormData(prev => ({ ...prev, responsable: e.target.value }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
                placeholder="Nombre del responsable"
              />
            </div>

            {/* Progreso */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Progreso (%):</label>
              <input
                type="number"
                min="0"
                max="100"
                value={formData.progreso}
                onChange={(e) => setFormData(prev => ({ ...prev, progreso: parseInt(e.target.value) || 0 }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              />
            </div>
          </div>

          {/* Gestión de Insumos */}
          <div style={{ marginTop: '20px' }}>
                                    <h4 style={{ margin: '0 0 15px 0', color: '#374151' }}>🧪 Insumos a Utilizar</h4>
                        <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '15px' }}>
                          Seleccione los insumos que se utilizarán en esta labor. El sistema validará el stock disponible.
                        </p>
            
            {/* Agregar insumo */}
            <div style={{ display: 'flex', gap: '10px', marginBottom: '15px', flexWrap: 'wrap' }}>
                                        <select
                            id="insumo-select"
                            style={{
                              padding: '10px',
                              border: '1px solid #ddd',
                              borderRadius: '5px',
                              fontSize: '14px',
                              minWidth: '200px'
                            }}
                          >
                            <option value="">Seleccionar insumo</option>
                            {insumos.map(insumo => (
                              <option key={insumo.id} value={insumo.id}>
                                {insumo.nombre} (Stock: {insumo.stock_actual} {insumo.unidad_medida})
                                {insumo.stock_actual < insumo.stock_minimo ? ' ⚠️ Stock bajo' : ''}
                              </option>
                            ))}
                          </select>
              <input
                id="cantidad-insumo"
                type="number"
                step="0.01"
                placeholder="Cantidad"
                style={{
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px',
                  width: '120px'
                }}
              />
              <button
                onClick={addInsumoToLabor}
                style={{
                  background: '#10b981',
                  color: 'white',
                  border: 'none',
                  padding: '10px 15px',
                  borderRadius: '5px',
                  cursor: 'pointer',
                  fontSize: '14px'
                }}
              >
                ➕ Agregar
              </button>
            </div>

            {/* Lista de insumos */}
            {formData.insumos_usados.length > 0 && (
              <div style={{ 
                background: 'white', 
                padding: '15px', 
                borderRadius: '5px',
                border: '1px solid #e5e7eb'
              }}>
                <h5 style={{ margin: '0 0 10px 0', color: '#374151' }}>Insumos Seleccionados:</h5>
                {formData.insumos_usados.map((insumo, index) => (
                  <div key={index} style={{ 
                    display: 'flex', 
                    alignItems: 'center', 
                    gap: '10px', 
                    padding: '10px',
                    border: '1px solid #f3f4f6',
                    borderRadius: '5px',
                    marginBottom: '10px'
                  }}>
                    <div style={{ flex: '1' }}>
                      <strong>{insumo.insumo_nombre}</strong>
                      <div style={{ fontSize: '12px', color: '#6b7280' }}>
                        Planificado: {insumo.cantidad_planificada} {insumo.unidad_medida}
                        {insumo.cantidad_usada > 0 && (
                          <span style={{ color: '#10b981', marginLeft: '10px' }}>
                            • Usado: {insumo.cantidad_usada} {insumo.unidad_medida}
                          </span>
                        )}
                      </div>
                    </div>
                    <input
                      type="number"
                      step="0.01"
                      placeholder="Usado"
                      value={insumo.cantidad_usada}
                      onChange={(e) => updateInsumoUsado(index, parseFloat(e.target.value) || 0)}
                      style={{
                        padding: '8px',
                        border: '1px solid #ddd',
                        borderRadius: '3px',
                        fontSize: '14px',
                        width: '100px'
                      }}
                    />
                    <span style={{ fontSize: '12px', color: '#6b7280', minWidth: '60px' }}>
                      {insumo.unidad_medida}
                    </span>
                    <span style={{ fontSize: '12px', color: '#10b981', minWidth: '80px' }}>
                      {formatCurrency(insumo.costo_total)}
                    </span>
                    <button
                      onClick={() => removeInsumoFromLabor(index)}
                      style={{
                        background: '#ef4444',
                        color: 'white',
                        border: 'none',
                        padding: '5px 10px',
                        borderRadius: '3px',
                        cursor: 'pointer',
                        fontSize: '12px'
                      }}
                    >
                      ❌
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Sección de Maquinaria */}
          <div style={{ marginTop: '15px' }}>
            <h4 style={{ margin: '0 0 10px 0', color: '#374151' }}>🚜 Maquinaria Asignada</h4>
            
            {/* Formulario para agregar maquinaria */}
            <div style={{ 
              background: '#f9fafb', 
              padding: '15px', 
              borderRadius: '5px',
              border: '1px solid #e5e7eb'
            }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr auto', gap: '10px', alignItems: 'end' }}>
                <div>
                  <label style={{ display: 'block', marginBottom: '5px', fontSize: '12px', fontWeight: 'bold' }}>
                    Maquinaria:
                  </label>
                  <select
                    id="maquinaria-select"
                    style={{
                      width: '100%',
                      padding: '8px',
                      border: '1px solid #ddd',
                      borderRadius: '3px',
                      fontSize: '14px'
                    }}
                  >
                    <option value="">Seleccionar maquinaria...</option>
                    {maquinaria.filter(m => m.estado === 'activo').map(maq => (
                      <option key={maq.id} value={maq.id}>
                        {maq.nombre} (${maq.costo_por_hora}/h)
                      </option>
                    ))}
                  </select>
                </div>
                
                <div>
                  <label style={{ display: 'block', marginBottom: '5px', fontSize: '12px', fontWeight: 'bold' }}>
                    Horas de Uso:
                  </label>
                  <input
                    id="horas-maquinaria"
                    type="number"
                    step="0.1"
                    placeholder="0.0"
                    style={{
                      width: '100%',
                      padding: '8px',
                      border: '1px solid #ddd',
                      borderRadius: '3px',
                      fontSize: '14px'
                    }}
                  />
                </div>
                
                <div>
                  <label style={{ display: 'block', marginBottom: '5px', fontSize: '12px', fontWeight: 'bold' }}>
                    Kilómetros:
                  </label>
                  <input
                    id="kilometros-maquinaria"
                    type="number"
                    step="0.1"
                    placeholder="0.0"
                    style={{
                      width: '100%',
                      padding: '8px',
                      border: '1px solid #ddd',
                      borderRadius: '3px',
                      fontSize: '14px'
                    }}
                  />
                </div>
                
                <button
                  onClick={addMaquinariaToLabor}
                  style={{
                    background: '#10b981',
                    color: 'white',
                    border: 'none',
                    padding: '8px 15px',
                    borderRadius: '3px',
                    cursor: 'pointer',
                    fontSize: '14px',
                    fontWeight: 'bold'
                  }}
                >
                  ➕ Agregar
                </button>
              </div>
            </div>

            {/* Lista de maquinaria asignada */}
            {formData.maquinaria_asignada.length > 0 && (
              <div style={{ 
                background: 'white', 
                padding: '15px', 
                borderRadius: '5px',
                border: '1px solid #e5e7eb',
                marginTop: '10px'
              }}>
                <h5 style={{ margin: '0 0 10px 0', color: '#374151' }}>Maquinaria Seleccionada:</h5>
                {formData.maquinaria_asignada.map((maq, index) => (
                  <div key={index} style={{ 
                    display: 'flex', 
                    alignItems: 'center', 
                    gap: '10px', 
                    padding: '10px',
                    border: '1px solid #f3f4f6',
                    borderRadius: '5px',
                    marginBottom: '10px'
                  }}>
                    <div style={{ flex: '1' }}>
                      <strong>{maq.maquinaria_nombre}</strong>
                      <div style={{ fontSize: '12px', color: '#6b7280' }}>
                        Horas: {maq.horas_uso}h • Kilómetros: {maq.kilometros_recorridos}km
                      </div>
                    </div>
                    <span style={{ fontSize: '12px', color: '#10b981', minWidth: '80px' }}>
                      {formatCurrency(maq.costo_total)}
                    </span>
                    <button
                      onClick={() => removeMaquinariaFromLabor(index)}
                      style={{
                        background: '#ef4444',
                        color: 'white',
                        border: 'none',
                        padding: '5px 10px',
                        borderRadius: '3px',
                        cursor: 'pointer',
                        fontSize: '12px'
                      }}
                    >
                      ❌
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Observaciones */}
          <div style={{ marginTop: '15px' }}>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Observaciones:</label>
            <textarea
              value={formData.observaciones}
              onChange={(e) => setFormData(prev => ({ ...prev, observaciones: e.target.value }))}
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #ddd',
                borderRadius: '5px',
                fontSize: '14px',
                minHeight: '80px',
                resize: 'vertical'
              }}
              placeholder="Detalles de la labor..."
            />
          </div>

          {/* Botones */}
          <div style={{ display: 'flex', gap: '10px', marginTop: '15px' }}>
            <button
              onClick={saveLabor}
              disabled={loading}
              style={{
                background: '#f59e0b',
                color: 'white',
                border: 'none',
                padding: '12px 24px',
                borderRadius: '8px',
                cursor: loading ? 'not-allowed' : 'pointer',
                fontSize: '14px',
                fontWeight: 'bold',
                opacity: loading ? 0.6 : 1
              }}
            >
              {loading ? '💾 Guardando...' : (editingLabor ? '💾 Actualizar' : '💾 Guardar')}
            </button>
            <button
              onClick={resetForm}
              style={{
                background: '#6b7280',
                color: 'white',
                border: 'none',
                padding: '12px 24px',
                borderRadius: '8px',
                cursor: 'pointer',
                fontSize: '14px'
              }}
            >
              ❌ Cancelar
            </button>
          </div>
        </div>
      )}

      {/* Tabla de labores */}
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
            {searchTerm || filterEstado !== 'todos' ? 'No se encontraron labores que coincidan con los filtros' : 'No hay labores registradas'}
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
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Insumos</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Maquinaria</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Costo</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {filteredLabores.map(labor => (
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
                      {new Date(labor.fecha).toLocaleDateString('es-ES')}
                    </td>
                    <td style={{ padding: '12px' }}>
                      <span style={{
                        padding: '4px 8px',
                        borderRadius: '12px',
                        fontSize: '12px',
                        fontWeight: 'bold',
                        background: getEstadoColor(labor.estado) + '20',
                        color: getEstadoColor(labor.estado)
                      }}>
                        {getEstadoLabel(labor.estado)}
                      </span>
                      {labor.progreso !== undefined && labor.progreso > 0 && (
                        <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '2px' }}>
                          {labor.progreso}% completado
                        </div>
                      )}
                    </td>
                    <td style={{ padding: '12px' }}>
                      {labor.responsable}
                    </td>
                    <td style={{ padding: '12px' }}>
                      <div style={{ fontSize: '12px' }}>
                        {labor.insumos_usados.length} insumo(s)
                      </div>
                      {labor.insumos_usados.slice(0, 2).map((insumo, index) => (
                        <div key={index} style={{ fontSize: '11px', color: '#6b7280' }}>
                          {insumo.insumo_nombre}: {insumo.cantidad_usada}/{insumo.cantidad_planificada} {insumo.unidad_medida}
                        </div>
                      ))}
                      {labor.insumos_usados.length > 2 && (
                        <div style={{ fontSize: '11px', color: '#6b7280' }}>
                          +{labor.insumos_usados.length - 2} más
                        </div>
                      )}
                    </td>
                    <td style={{ padding: '12px' }}>
                      <div style={{ fontSize: '12px' }}>
                        {labor.maquinaria_asignada.length} máquina(s)
                      </div>
                      {labor.maquinaria_asignada.slice(0, 2).map((maq, index) => (
                        <div key={index} style={{ fontSize: '11px', color: '#6b7280' }}>
                          {maq.maquinaria_nombre}: {maq.horas_uso}h
                        </div>
                      ))}
                      {labor.maquinaria_asignada.length > 2 && (
                        <div style={{ fontSize: '11px', color: '#6b7280' }}>
                          +{labor.maquinaria_asignada.length - 2} más
                        </div>
                      )}
                    </td>
                    <td style={{ padding: '12px' }}>
                      <span style={{ fontWeight: 'bold', color: '#10b981' }}>
                        {formatCurrency(labor.costo_total || 0)}
                      </span>
                    </td>
                    <td style={{ padding: '12px' }}>
                      <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                        <button
                          onClick={() => editLabor(labor)}
                          style={{
                            background: '#3b82f6',
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
                        <select
                          value={labor.estado}
                          onChange={(e) => changeLaborEstado(labor.id!, e.target.value)}
                          style={{
                            padding: '4px 8px',
                            border: '1px solid #ddd',
                            borderRadius: '4px',
                            fontSize: '11px',
                            background: 'white'
                          }}
                        >
                          {estadosLabor.map(estado => (
                            <option key={estado.value} value={estado.value}>
                              {estado.label}
                            </option>
                          ))}
                        </select>
                        <button
                          onClick={() => deleteLabor(labor.id!)}
                          style={{
                            background: '#ef4444',
                            color: 'white',
                            border: 'none',
                            padding: '6px 12px',
                            borderRadius: '4px',
                            cursor: 'pointer',
                            fontSize: '12px'
                          }}
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
    </div>
  );
};

export default LaboresManagement;
