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
  progreso?: number;
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

interface Lote {
  id: number;
  nombre: string;
  superficie: number;
  cultivo: string;
}

const LaboresManagement: React.FC = () => {
  const { formatCurrency } = useCurrency();
  const [labores, setLabores] = useState<Labor[]>([]);
  const [lotes, setLotes] = useState<Lote[]>([]);
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

  // Estados para manejar insumos y maquinaria en el formulario
  const [selectedInsumos, setSelectedInsumos] = useState<InsumoUsado[]>([]);
  const [selectedMaquinaria, setSelectedMaquinaria] = useState<MaquinariaAsignada[]>([]);
  const [showInsumosModal, setShowInsumosModal] = useState(false);
  const [showMaquinariaModal, setShowMaquinariaModal] = useState(false);
  
  // Estados para los modales de selección individual
  const [selectedInsumoId, setSelectedInsumoId] = useState<number>(0);
  const [selectedMaquinariaId, setSelectedMaquinariaId] = useState<number>(0);
  const [insumoCantidad, setInsumoCantidad] = useState<number>(0);
  const [maquinariaHoras, setMaquinariaHoras] = useState<number>(0);
  const [maquinariaKilometros, setMaquinariaKilometros] = useState<number>(0);

  const tiposLabor = [
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

  // Cargar datos mock
  const loadData = async () => {
    setLoading(true);
    
    setTimeout(() => {
      const mockLotes: Lote[] = [
        { id: 1, nombre: 'Lote A1', superficie: 25.5, cultivo: 'Soja' },
        { id: 2, nombre: 'Lote A2', superficie: 30.25, cultivo: 'Maíz' },
        { id: 3, nombre: 'Lote B1', superficie: 40.0, cultivo: 'Trigo' }
      ];

      const mockInsumos: Insumo[] = [
        { id: 1, nombre: 'Semilla Soja DM 53i54', tipo: 'semilla', stock_actual: 2500, unidad_medida: 'kg', precio_unitario: 8500 },
        { id: 2, nombre: 'Fertilizante Urea 46%', tipo: 'fertilizante', stock_actual: 8000, unidad_medida: 'kg', precio_unitario: 450 },
        { id: 3, nombre: 'Glifosato 48%', tipo: 'herbicida', stock_actual: 150, unidad_medida: 'L', precio_unitario: 2800 },
        { id: 4, nombre: 'Aceite de Motor 15W40', tipo: 'lubricante', stock_actual: 80, unidad_medida: 'L', precio_unitario: 1200 }
      ];

      const mockMaquinaria: Maquinaria[] = [
        { id: 1, nombre: 'Tractor John Deere 5075E', tipo: 'tractor', estado: 'activo', kilometros_uso: 12500.50, costo_por_hora: 45.00 },
        { id: 2, nombre: 'Cosechadora New Holland CR8.90', tipo: 'cosechadora', estado: 'activo', kilometros_uso: 8500.25, costo_por_hora: 120.00 },
        { id: 3, nombre: 'Pulverizadora Jacto 2000', tipo: 'pulverizadora', estado: 'activo', kilometros_uso: 3200.75, costo_por_hora: 35.00 },
        { id: 4, nombre: 'Sembradora de Precisión', tipo: 'sembradora', estado: 'activo', kilometros_uso: 1800.00, costo_por_hora: 25.00 }
      ];

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
      responsable: '',
      progreso: 0
    });
    setSelectedInsumos([]);
    setSelectedMaquinaria([]);
    setSelectedInsumoId(0);
    setSelectedMaquinariaId(0);
    setInsumoCantidad(0);
    setMaquinariaHoras(0);
    setMaquinariaKilometros(0);
    setEditingLabor(null);
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

  const handleOpenEditModal = (labor: Labor) => {
    setFormData(labor);
    setSelectedInsumos(labor.insumos_usados);
    setSelectedMaquinaria(labor.maquinaria_asignada);
    setEditingLabor(labor);
    setShowForm(true);
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

  const handleLoteChange = (loteId: number) => {
    const lote = lotes.find(l => l.id === loteId);
    setFormData(prev => ({
      ...prev,
      lote_id: loteId,
      lote_nombre: lote?.nombre || ''
    }));
  };

  const saveLabor = () => {
    if (!formData.tipo || !formData.fecha || !formData.lote_id || !formData.responsable) {
      alert('Por favor complete todos los campos obligatorios');
      return;
    }

    // Crear labor con insumos y maquinaria
    const laborCompleta = {
      ...formData,
      insumos_usados: selectedInsumos,
      maquinaria_asignada: selectedMaquinaria,
      costo_total: calcularCostoTotal()
    };

    if (editingLabor) {
      // Editar labor existente
      setLabores(prev => prev.map(l => l.id === editingLabor.id ? { ...laborCompleta, id: l.id } : l));
      alert('Labor actualizada exitosamente');
    } else {
      // Crear nueva labor
      const newId = Math.max(...labores.map(l => l.id || 0)) + 1;
      const newLabor = { ...laborCompleta, id: newId };
      setLabores(prev => [...prev, newLabor]);
      
      // Actualizar stock de insumos y uso de maquinaria
      actualizarStockInsumos();
      actualizarUsoMaquinaria();
      
      alert('Labor creada exitosamente');
    }

    handleCloseModal();
  };

  const deleteLabor = (id: number) => {
    if (window.confirm('¿Está seguro de que desea eliminar esta labor?')) {
      setLabores(prev => prev.filter(l => l.id !== id));
      alert('Labor eliminada exitosamente');
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
          ? { ...m, horas_uso: horas, kilometros_recorridos: kilometros, costo_total: horas * maq.costo_por_hora }
          : m
      ));
    } else {
      // Agregar nueva maquinaria
      const nuevaMaquinaria: MaquinariaAsignada = {
        maquinaria_id: maq.id,
        maquinaria_nombre: maq.nombre,
        horas_uso: horas,
        kilometros_recorridos: kilometros,
        costo_total: horas * maq.costo_por_hora
      };
      setSelectedMaquinaria(prev => [...prev, nuevaMaquinaria]);
    }
  };

  const removeMaquinaria = (maquinariaId: number) => {
    setSelectedMaquinaria(prev => prev.filter(m => m.maquinaria_id !== maquinariaId));
  };

  const updateMaquinariaUso = (maquinariaId: number, horas: number, kilometros: number) => {
    const maq = maquinaria.find(m => m.id === maquinariaId);
    if (maq) {
      setSelectedMaquinaria(prev => prev.map(m => 
        m.maquinaria_id === maquinariaId 
          ? { ...m, horas_uso: horas, kilometros_recorridos: kilometros, costo_total: horas * maq.costo_por_hora }
          : m
      ));
    }
  };

  // Calcular costo total de la labor
  const calcularCostoTotal = () => {
    const costoInsumos = selectedInsumos.reduce((sum, insumo) => sum + insumo.costo_total, 0);
    const costoMaquinaria = selectedMaquinaria.reduce((sum, maq) => sum + maq.costo_total, 0);
    return costoInsumos + costoMaquinaria;
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
    selectedMaquinaria.forEach(maqUsada => {
      const maq = maquinaria.find(m => m.id === maqUsada.maquinaria_id);
      if (maq) {
        const nuevosKilometros = maq.kilometros_uso + maqUsada.kilometros_recorridos;
        setMaquinaria(prev => prev.map(m => 
          m.id === maq.id ? { ...m, kilometros_uso: nuevosKilometros } : m
        ));
      }
    });
  };

  // Filtrar labores
  const filteredLabores = labores.filter(labor => {
    const matchesSearch = labor.tipo.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         labor.lote_nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         labor.responsable.toLowerCase().includes(searchTerm.toLowerCase());
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
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Costo</th>
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
                      {new Date(labor.fecha).toLocaleDateString('es-ES')}
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
                      <span style={{ fontWeight: 'bold', color: '#10b981' }}>
                        {formatCurrency(labor.costo_total || 0)}
                      </span>
                    </td>
                    <td style={{ padding: '12px' }}>
                      <div style={{ display: 'flex', gap: '5px' }}>
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
                        >
                          ✏️
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
                {/* Tipo de labor */}
                <div>
                  <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#374151' }}>
                    Tipo de Labor *
                  </label>
                  <select
                    value={formData.tipo}
                    onChange={(e) => handleInputChange('tipo', e.target.value)}
                    required
                    style={{
                      width: '100%',
                      padding: '10px',
                      border: '1px solid #d1d5db',
                      borderRadius: '8px',
                      fontSize: '14px'
                    }}
                  >
                    <option value="">Seleccionar tipo de labor</option>
                    {tiposLabor.map(tipo => (
                      <option key={tipo} value={tipo}>
                        {tipo.charAt(0).toUpperCase() + tipo.slice(1)}
                      </option>
                    ))}
                  </select>
                </div>

                {/* Lote */}
                <div>
                  <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#374151' }}>
                    Lote *
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

                {/* Progreso */}
                <div>
                  <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', color: '#374151' }}>
                    Progreso (%)
                  </label>
                  <input
                    type="number"
                    min="0"
                    max="100"
                    value={formData.progreso}
                    onChange={(e) => handleInputChange('progreso', Number(e.target.value))}
                    style={{
                      width: '100%',
                      padding: '10px',
                      border: '1px solid #d1d5db',
                      borderRadius: '8px',
                      fontSize: '14px'
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
                      {selectedInsumos.map((insumo) => (
                        <div key={insumo.insumo_id} style={{
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
                    <button
                      type="button"
                      onClick={handleOpenMaquinariaModal}
                      style={{
                        padding: '5px 10px',
                        background: '#3b82f6',
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
                  
                  {selectedMaquinaria.length > 0 ? (
                    <div style={{ 
                      border: '1px solid #e5e7eb', 
                      borderRadius: '8px', 
                      padding: '10px',
                      maxHeight: '150px',
                      overflowY: 'auto'
                    }}>
                      {selectedMaquinaria.map((maq) => (
                        <div key={maq.maquinaria_id} style={{
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
                              {maq.horas_uso}h - {maq.kilometros_recorridos}km - {formatCurrency(maq.costo_total)}
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
    </div>
  );
};

export default LaboresManagement;
