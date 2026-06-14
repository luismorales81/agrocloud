/**
 * Pantalla de Inventario para el módulo de Porcinos
 * Muestra insumos compartidos (vacunas, medicamentos, materiales, etc.)
 * y permite preparar recetas con descuento automático de ingredientes
 */
import React, { useState, useEffect } from 'react';
import { useCurrencyContext } from '../../../../contexts/CurrencyContext';
import { useAuth } from '../../../../contexts/AuthContext';
import { useEmpresa } from '../../../../contexts/EmpresaContext';
import { insumosService } from '../../../../services/apiServices';
import { insumosCompuestosService } from '../../../../services/insumosCompuestosService';
import PermissionGate from '../../../../components/PermissionGate';
import InsumoWizard from '../../../../components/InsumoWizard';
import { Icon } from '../../../../components/icons';
import type { InsumoCompuesto, CalcularPreparacionRecetaResponse } from '../../types';

interface Insumo {
  id?: number;
  nombre: string;
  tipo: string;
  descripcion: string;
  unidadMedida: string;
  precioUnitario: number;
  stockActual: number;
  stockMinimo: number;
  proveedor: string;
  fechaVencimiento: string;
  estado: string;
}

const InventarioPorcinosScreen: React.FC = () => {
  const { formatCurrency } = useCurrencyContext();
  const { user } = useAuth();
  const { rolUsuario } = useEmpresa();
  const [insumos, setInsumos] = useState<Insumo[]>([]);
  const [recetas, setRecetas] = useState<InsumoCompuesto[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterType, setFilterType] = useState('todos');
  const [mostrarPrepararReceta, setMostrarPrepararReceta] = useState(false);
  const [recetaSeleccionada, setRecetaSeleccionada] = useState<InsumoCompuesto | null>(null);
  const [cantidadPreparar, setCantidadPreparar] = useState<string>('');
  const [calculoPreparacion, setCalculoPreparacion] = useState<CalcularPreparacionRecetaResponse | null>(null);
  const [mostrarCalculo, setMostrarCalculo] = useState(false);
  
  // Estados para paginación
  const [paginaActual, setPaginaActual] = useState(1);
  const [itemsPorPagina] = useState(10);

  // Estados para el wizard de insumos
  const [showWizard, setShowWizard] = useState(false);
  const [wizardMode, setWizardMode] = useState<'create' | 'edit'>('create');
  const [editingInsumo, setEditingInsumo] = useState<Insumo | null>(null);

  // Categorías relevantes para porcinos (sin agroquímicos)
  const categoriasPorcinos = [
    'Vacunas',
    'Medicamentos',
    'Vitaminas',
    'Minerales',
    'Materiales',
    'Otros'
  ];

  // Unidades de medida
  const unidadesMedida = [
    'Kg',
    'Litro',
    'Bolsa',
    'unidad',
    'ml',
    'dosis',
    'metro',
    'caja'
  ];

  const mapearTipoACategoria = (tipo: string): string => {
    const mapeo: { [key: string]: string } = {
      'VACUNA': 'Vacunas',
      'MEDICAMENTO': 'Medicamentos',
      'VITAMINA': 'Vitaminas',
      'MINERAL': 'Minerales',
      'HERRAMIENTA': 'Materiales',
      'OTROS': 'Otros'
    };
    return mapeo[tipo] || 'Otros';
  };

  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    setLoading(true);
    try {
      const [insumosData, recetasData] = await Promise.all([
        insumosService.listar(),
        insumosCompuestosService.listar(),
      ]);

      const insumosMapeados = (Array.isArray(insumosData) ? insumosData : []).map((insumo: any) => ({
        id: insumo.id,
        nombre: insumo.nombre || 'Sin nombre',
        tipo: insumo.tipo || 'OTROS',
        descripcion: insumo.descripcion || '',
        unidadMedida: insumo.unidadMedida || insumo.unidad_medida || 'unidad',
        precioUnitario: insumo.precioUnitario || insumo.precio_unitario || 0,
        stockActual: insumo.stockActual || insumo.stock_actual || 0,
        stockMinimo: insumo.stockMinimo || insumo.stock_minimo || 0,
        proveedor: insumo.proveedor || '',
        fechaVencimiento: insumo.fechaVencimiento || insumo.fecha_vencimiento || '',
        estado: insumo.estado || 'activo',
      }));

      setInsumos(insumosMapeados);
      setRecetas(recetasData || []);
    } catch (error) {
      console.error('Error cargando datos:', error);
    } finally {
      setLoading(false);
    }
  };

  const prepararReceta = async () => {
    if (!recetaSeleccionada || !cantidadPreparar || parseFloat(cantidadPreparar) <= 0) {
      alert('Por favor ingrese una cantidad válida');
      return;
    }

    try {
      setLoading(true);
      await insumosCompuestosService.prepararReceta(
        recetaSeleccionada.id!,
        parseFloat(cantidadPreparar)
      );

      alert('Receta preparada exitosamente. Los ingredientes han sido descontados del inventario.');
      setMostrarPrepararReceta(false);
      setRecetaSeleccionada(null);
      setCantidadPreparar('');
      cargarDatos(); // Recargar para actualizar stocks
    } catch (error: any) {
      console.error('Error preparando receta:', error);
      
      // Extraer mensaje del error
      let mensajeError = 'Error al preparar la receta';
      
      if (error.response?.data) {
        // Si el backend devuelve un objeto con mensaje
        if (error.response.data.mensaje) {
          mensajeError = error.response.data.mensaje;
        } else if (typeof error.response.data === 'string') {
          // Si el backend devuelve directamente un string
          mensajeError = error.response.data;
        } else if (error.response.data.message) {
          mensajeError = error.response.data.message;
        }
      } else if (error.message) {
        mensajeError = error.message;
      }
      
      // Si es un error de stock insuficiente, agregar sugerencia
      if (mensajeError.toLowerCase().includes('stock insuficiente') || 
          mensajeError.toLowerCase().includes('insuficiente')) {
        mensajeError += '\n\n💡 Sugerencia: Use el botón "Calcular" para verificar el stock disponible antes de preparar.';
      }
      
      alert(mensajeError);
    } finally {
      setLoading(false);
    }
  };

  const abrirPrepararReceta = (receta: InsumoCompuesto) => {
    setRecetaSeleccionada(receta);
    setCantidadPreparar('1');
    setCalculoPreparacion(null);
    setMostrarCalculo(false);
    setMostrarPrepararReceta(true);
  };

  const calcularPreparacion = async () => {
    if (!recetaSeleccionada || !cantidadPreparar || parseFloat(cantidadPreparar) <= 0) {
      alert('Por favor ingrese una cantidad válida para calcular');
      return;
    }

    try {
      setLoading(true);
      const calculo = await insumosCompuestosService.calcularPreparacion(
        recetaSeleccionada.id!,
        parseFloat(cantidadPreparar)
      );
      setCalculoPreparacion(calculo);
      setMostrarCalculo(true);
    } catch (error: any) {
      console.error('Error calculando preparación:', error);
      alert(error.response?.data?.mensaje || 'Error al calcular la preparación');
    } finally {
      setLoading(false);
    }
  };

  // Filtrar insumos
  const filteredInsumos = insumos.filter(insumo => {
    const matchesSearch = insumo.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         insumo.descripcion.toLowerCase().includes(searchTerm.toLowerCase());
    
    const categoria = mapearTipoACategoria(insumo.tipo);
    const matchesType = filterType === 'todos' || categoria === filterType;
    
    return matchesSearch && matchesType;
  });

  // Calcular paginación
  const totalPaginas = Math.ceil(filteredInsumos.length / itemsPorPagina);
  const inicio = (paginaActual - 1) * itemsPorPagina;
  const fin = inicio + itemsPorPagina;
  const insumosPaginados = filteredInsumos.slice(inicio, fin);

  // Resetear a página 1 cuando cambian los filtros
  useEffect(() => {
    setPaginaActual(1);
  }, [searchTerm, filterType]);

  // Estadísticas
  const estadisticas = {
    totalInsumos: insumos.length,
    stockBajo: insumos.filter(i => i.stockActual <= i.stockMinimo).length,
    valorTotal: insumos.reduce((sum, i) => sum + (i.precioUnitario * i.stockActual), 0)
  };

  const getStockStatusColor = (insumo: Insumo) => {
    if (insumo.stockActual <= insumo.stockMinimo) return '#ef4444';
    if (insumo.stockActual <= insumo.stockMinimo * 1.5) return '#f59e0b';
    return '#10b981';
  };

  const getStockStatusLabel = (insumo: Insumo) => {
    if (insumo.stockActual <= insumo.stockMinimo) return 'Crítico';
    if (insumo.stockActual <= insumo.stockMinimo * 1.5) return 'Bajo';
    return 'Normal';
  };

  // Verificar si el usuario puede modificar insumos
  const puedeModificarInsumos = () => {
    if (!rolUsuario) return false;
    return rolUsuario !== 'OPERARIO' &&
           rolUsuario !== 'INVITADO' &&
           rolUsuario !== 'CONSULTOR_EXTERNO' &&
           rolUsuario !== 'LECTURA';
  };

  // Funciones para el wizard de insumos
  const handleCreateInsumo = () => {
    setEditingInsumo(null);
    setWizardMode('create');
    setShowWizard(true);
  };

  const handleEditInsumo = (insumo: Insumo) => {
    setEditingInsumo(insumo);
    setWizardMode('edit');
    setShowWizard(true);
  };

  const handleWizardClose = () => {
    setShowWizard(false);
    setEditingInsumo(null);
  };

  const handleWizardSave = () => {
    cargarDatos(); // Recargar todos los insumos después de guardar
    setShowWizard(false);
    setEditingInsumo(null);
  };

  // Eliminar insumo
  const deleteInsumo = async (id: number) => {
    if (!window.confirm('¿Está seguro de que desea eliminar este insumo?')) return;
    try {
      setLoading(true);
      await insumosService.eliminar(id);
      alert('Insumo eliminado exitosamente');
      cargarDatos();
    } catch (error) {
      console.error('Error al eliminar insumo:', error);
      alert('Error al eliminar el insumo. Verifique que no esté en uso.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
      <div style={{ 
        background: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)', 
        color: 'white', 
        padding: '20px', 
        borderRadius: '10px', 
        marginBottom: '20px' 
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <h1 style={{ margin: '0 0 10px 0', fontSize: '24px' }}>📦 Inventario de Insumos</h1>
            <p style={{ margin: '0', opacity: '0.9' }}>
              Gestión de inventario compartido (vacunas, medicamentos, materiales)
            </p>
          </div>
          <PermissionGate permission="canCreateInsumos">
            <button
              onClick={handleCreateInsumo}
              style={{
                background: '#10b981',
                color: 'white',
                border: 'none',
                padding: '12px 24px',
                borderRadius: '8px',
                cursor: 'pointer',
                fontSize: '14px',
                fontWeight: 'bold',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}
            >
              <Icon name="Plus" size={18} />
              Nuevo Insumo
            </button>
          </PermissionGate>
        </div>
      </div>

      {/* Estadísticas */}
      <div style={{ 
        background: '#f3f4f6', 
        padding: '20px', 
        borderRadius: '10px', 
        marginBottom: '20px',
        border: '1px solid #e5e7eb'
      }}>
        <h3 style={{ margin: '0 0 15px 0', color: '#374151' }}>📊 Resumen de Inventario</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#f59e0b' }}>
              {estadisticas.totalInsumos}
            </div>
            <div style={{ fontSize: '14px', color: '#6b7280' }}>Total Insumos</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#ef4444' }}>
              {estadisticas.stockBajo}
            </div>
            <div style={{ fontSize: '14px', color: '#6b7280' }}>Stock Crítico</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#10b981' }}>
              {formatCurrency(estadisticas.valorTotal)}
            </div>
            <div style={{ fontSize: '14px', color: '#6b7280' }}>Valor Total</div>
          </div>
        </div>
      </div>

      {/* Modal preparar receta */}
      {mostrarPrepararReceta && recetaSeleccionada && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(0,0,0,0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{
            background: 'white',
            padding: '30px',
            borderRadius: '10px',
            maxWidth: '800px',
            width: '90%',
            maxHeight: '90vh',
            overflowY: 'auto'
          }}>
            <h2 style={{ margin: '0 0 20px 0' }}>Preparar Receta: {recetaSeleccionada.nombre}</h2>
            
            <div style={{ marginBottom: '20px' }}>
              <label style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
                Cantidad a preparar ({recetaSeleccionada.unidadMedida || 'kg'}):
              </label>
              <div style={{ display: 'flex', gap: '10px' }}>
                <input
                  type="number"
                  step="0.01"
                  value={cantidadPreparar}
                  onChange={(e) => setCantidadPreparar(e.target.value)}
                  style={{
                    flex: 1,
                    padding: '10px',
                    border: '1px solid #ddd',
                    borderRadius: '5px',
                    fontSize: '14px'
                  }}
                  placeholder="Ej: 100"
                />
                <button
                  onClick={calcularPreparacion}
                  disabled={loading || !cantidadPreparar}
                  style={{
                    background: '#3b82f6',
                    color: 'white',
                    border: 'none',
                    padding: '10px 20px',
                    borderRadius: '5px',
                    cursor: loading || !cantidadPreparar ? 'not-allowed' : 'pointer',
                    opacity: loading || !cantidadPreparar ? 0.6 : 1,
                    whiteSpace: 'nowrap'
                  }}
                >
                  Calcular
                </button>
              </div>
            </div>

            {calculoPreparacion && (
              <div style={{ marginBottom: '20px', padding: '15px', background: '#f9fafb', borderRadius: '8px' }}>
                <h3 style={{ margin: '0 0 15px 0', fontSize: '16px', fontWeight: 'bold' }}>
                  Resultado del Cálculo
                </h3>
                
                {calculoPreparacion.maximoPreparableConStock !== null && (
                  <div style={{ 
                    marginBottom: '15px', 
                    padding: '12px', 
                    background: calculoPreparacion.stockSuficienteGlobal ? '#d1fae5' : '#fee2e2',
                    borderRadius: '5px',
                    border: `1px solid ${calculoPreparacion.stockSuficienteGlobal ? '#10b981' : '#ef4444'}`
                  }}>
                    <div style={{ fontWeight: 'bold', marginBottom: '5px' }}>
                      {calculoPreparacion.stockSuficienteGlobal ? '✅ Stock suficiente' : '⚠️ Stock insuficiente'}
                    </div>
                    <div style={{ fontSize: '14px' }}>
                      Máximo preparable con stock actual: <strong>{calculoPreparacion.maximoPreparableConStock.toFixed(2)} {calculoPreparacion.unidadMedida}</strong>
                    </div>
                    {calculoPreparacion.mensaje && (
                      <div style={{ fontSize: '13px', marginTop: '5px', color: '#6b7280' }}>
                        {calculoPreparacion.mensaje}
                      </div>
                    )}
                  </div>
                )}

                <div style={{ overflowX: 'auto' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px' }}>
                    <thead>
                      <tr style={{ background: '#e5e7eb', borderBottom: '2px solid #d1d5db' }}>
                        <th style={{ padding: '10px', textAlign: 'left', fontWeight: 'bold' }}>Componente</th>
                        <th style={{ padding: '10px', textAlign: 'right', fontWeight: 'bold' }}>Por kg receta</th>
                        <th style={{ padding: '10px', textAlign: 'right', fontWeight: 'bold' }}>Necesario</th>
                        <th style={{ padding: '10px', textAlign: 'right', fontWeight: 'bold' }}>Stock</th>
                        <th style={{ padding: '10px', textAlign: 'right', fontWeight: 'bold' }}>Máx. preparable</th>
                        <th style={{ padding: '10px', textAlign: 'center', fontWeight: 'bold' }}>Estado</th>
                      </tr>
                    </thead>
                    <tbody>
                      {calculoPreparacion.componentes.map((comp, idx) => (
                        <tr key={idx} style={{ borderBottom: '1px solid #e5e7eb' }}>
                          <td style={{ padding: '10px' }}>{comp.nombreComponente}</td>
                          <td style={{ padding: '10px', textAlign: 'right' }}>
                            {comp.cantidadPorKgReceta.toFixed(4)} {comp.unidadMedida}
                          </td>
                          <td style={{ padding: '10px', textAlign: 'right' }}>
                            {comp.cantidadNecesaria.toFixed(2)} {comp.unidadMedida}
                          </td>
                          <td style={{ padding: '10px', textAlign: 'right' }}>
                            {comp.stockDisponible.toFixed(2)} {comp.unidadMedida}
                          </td>
                          <td style={{ padding: '10px', textAlign: 'right' }}>
                            {comp.maximoPreparable !== null ? `${comp.maximoPreparable.toFixed(2)} ${calculoPreparacion.unidadMedida}` : 'N/A'}
                          </td>
                          <td style={{ padding: '10px', textAlign: 'center' }}>
                            <span style={{
                              padding: '4px 8px',
                              borderRadius: '4px',
                              fontSize: '11px',
                              fontWeight: 'bold',
                              background: comp.stockSuficiente ? '#d1fae5' : '#fee2e2',
                              color: comp.stockSuficiente ? '#065f46' : '#991b1b'
                            }}>
                              {comp.stockSuficiente ? '✓' : '✗'}
                            </span>
                            {comp.mensajeStock && (
                              <div style={{ fontSize: '11px', color: '#6b7280', marginTop: '4px' }}>
                                {comp.mensajeStock}
                              </div>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end', marginTop: '20px' }}>
              <button
                onClick={() => {
                  setMostrarPrepararReceta(false);
                  setRecetaSeleccionada(null);
                  setCantidadPreparar('');
                  setCalculoPreparacion(null);
                  setMostrarCalculo(false);
                }}
                style={{
                  background: '#6b7280',
                  color: 'white',
                  border: 'none',
                  padding: '10px 20px',
                  borderRadius: '5px',
                  cursor: 'pointer'
                }}
              >
                Cancelar
              </button>
              <button
                onClick={prepararReceta}
                disabled={loading}
                style={{
                  background: '#f59e0b',
                  color: 'white',
                  border: 'none',
                  padding: '10px 20px',
                  borderRadius: '5px',
                  cursor: loading ? 'not-allowed' : 'pointer',
                  opacity: loading ? 0.6 : 1
                }}
              >
                {loading ? 'Preparando...' : 'Preparar'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Filtros */}
      <div style={{ marginBottom: '20px', display: 'flex', gap: '15px', flexWrap: 'wrap', alignItems: 'center' }}>
        <div style={{ flex: '1', minWidth: '250px' }}>
          <input
            type="text"
            placeholder="🔍 Buscar insumos..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{
              width: '100%',
              padding: '10px',
              border: '1px solid #ddd',
              borderRadius: '5px',
              fontSize: '14px'
            }}
          />
        </div>
        <div>
          <select
            value={filterType}
            onChange={(e) => setFilterType(e.target.value)}
            style={{
              padding: '10px',
              border: '1px solid #ddd',
              borderRadius: '5px',
              fontSize: '14px'
            }}
          >
            <option value="todos">Todas las categorías</option>
            {categoriasPorcinos.map(categoria => (
              <option key={categoria} value={categoria}>{categoria}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Tabla de insumos */}
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
          fontWeight: 'bold',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          <span>📦 Insumos ({filteredInsumos.length})</span>
          {filteredInsumos.length > 0 && (
            <span style={{ fontSize: '14px', fontWeight: 'normal', color: '#6b7280' }}>
              Mostrando {inicio + 1}-{Math.min(fin, filteredInsumos.length)} de {filteredInsumos.length}
            </span>
          )}
        </div>
        
        {loading ? (
          <div style={{ padding: '40px', textAlign: 'center', color: '#666' }}>
            🔄 Cargando inventario...
          </div>
        ) : filteredInsumos.length === 0 ? (
          <div style={{ padding: '40px', textAlign: 'center', color: '#666' }}>
            No hay insumos registrados
          </div>
        ) : (
          <>
            <div style={{ overflowX: 'auto' }}>
              <table style={{ 
                width: '100%', 
                borderCollapse: 'collapse',
                fontSize: '14px'
              }}>
                <thead>
                  <tr style={{ background: '#f8f9fa' }}>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Insumo</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Categoría</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Stock</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Precio</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Valor Total</th>
                    {puedeModificarInsumos() && (
                      <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Acciones</th>
                    )}
                  </tr>
                </thead>
                <tbody>
                  {insumosPaginados.map(insumo => (
                  <tr key={insumo.id} style={{ borderBottom: '1px solid #f1f3f4' }}>
                    <td style={{ padding: '12px' }}>
                      <div>
                        <strong>{insumo.nombre}</strong>
                        {insumo.descripcion && (
                          <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '2px' }}>
                            {insumo.descripcion.substring(0, 50)}
                          </div>
                        )}
                      </div>
                    </td>
                    <td style={{ padding: '12px' }}>
                      <span style={{
                        padding: '4px 8px',
                        borderRadius: '12px',
                        fontSize: '12px',
                        fontWeight: 'bold',
                        background: '#e0e7ff',
                        color: '#3730a3'
                      }}>
                        {mapearTipoACategoria(insumo.tipo)}
                      </span>
                    </td>
                    <td style={{ padding: '12px' }}>
                      <div>
                        <span style={{ fontWeight: 'bold' }}>
                          {insumo.stockActual} {insumo.unidadMedida}
                        </span>
                        <div style={{ fontSize: '12px', color: '#6b7280' }}>
                          Mín: {insumo.stockMinimo}
                        </div>
                        <span style={{
                          padding: '2px 6px',
                          borderRadius: '8px',
                          fontSize: '10px',
                          fontWeight: 'bold',
                          background: getStockStatusColor(insumo) + '20',
                          color: getStockStatusColor(insumo)
                        }}>
                          {getStockStatusLabel(insumo)}
                        </span>
                      </div>
                    </td>
                    <td style={{ padding: '12px' }}>
                      <span style={{ fontWeight: 'bold', color: '#10b981' }}>
                        {formatCurrency(insumo.precioUnitario || 0)}
                      </span>
                    </td>
                    <td style={{ padding: '12px' }}>
                      <span style={{ fontWeight: 'bold' }}>
                        {formatCurrency((insumo.precioUnitario || 0) * insumo.stockActual)}
                      </span>
                    </td>
                    {puedeModificarInsumos() && (
                      <td style={{ padding: '12px' }}>
                        <div style={{ display: 'flex', gap: '8px' }}>
                          <button
                            onClick={() => handleEditInsumo(insumo)}
                            style={{
                              background: '#3b82f6',
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
                          <button
                            onClick={() => deleteInsumo(insumo.id!)}
                            style={{
                              background: '#ef4444',
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
                        </div>
                      </td>
                    )}
                  </tr>
                  ))}
                </tbody>
              </table>
            </div>
            
            {/* Paginación */}
            {totalPaginas > 1 && (
              <div style={{
                padding: '15px',
                borderTop: '1px solid #dee2e6',
                background: '#f8f9fa',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                flexWrap: 'wrap',
                gap: '10px'
              }}>
                <div style={{ fontSize: '14px', color: '#6b7280' }}>
                  Página {paginaActual} de {totalPaginas}
                </div>
                <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                  <button
                    onClick={() => setPaginaActual(1)}
                    disabled={paginaActual === 1}
                    style={{
                      padding: '6px 12px',
                      border: '1px solid #d1d5db',
                      borderRadius: '5px',
                      background: paginaActual === 1 ? '#f3f4f6' : 'white',
                      color: paginaActual === 1 ? '#9ca3af' : '#374151',
                      cursor: paginaActual === 1 ? 'not-allowed' : 'pointer',
                      fontSize: '13px'
                    }}
                  >
                    Primera
                  </button>
                  <button
                    onClick={() => setPaginaActual(p => Math.max(1, p - 1))}
                    disabled={paginaActual === 1}
                    style={{
                      padding: '6px 12px',
                      border: '1px solid #d1d5db',
                      borderRadius: '5px',
                      background: paginaActual === 1 ? '#f3f4f6' : 'white',
                      color: paginaActual === 1 ? '#9ca3af' : '#374151',
                      cursor: paginaActual === 1 ? 'not-allowed' : 'pointer',
                      fontSize: '13px'
                    }}
                  >
                    Anterior
                  </button>
                  
                  {/* Números de página */}
                  <div style={{ display: 'flex', gap: '4px' }}>
                    {(() => {
                      const maxBotones = Math.min(5, totalPaginas);
                      const paginas: number[] = [];
                      
                      if (totalPaginas <= 5) {
                        // Mostrar todas las páginas si hay 5 o menos
                        for (let i = 1; i <= totalPaginas; i++) {
                          paginas.push(i);
                        }
                      } else if (paginaActual <= 3) {
                        // Mostrar primeras 5 páginas
                        for (let i = 1; i <= 5; i++) {
                          paginas.push(i);
                        }
                      } else if (paginaActual >= totalPaginas - 2) {
                        // Mostrar últimas 5 páginas
                        for (let i = totalPaginas - 4; i <= totalPaginas; i++) {
                          paginas.push(i);
                        }
                      } else {
                        // Mostrar página actual y 2 a cada lado
                        for (let i = paginaActual - 2; i <= paginaActual + 2; i++) {
                          paginas.push(i);
                        }
                      }
                      
                      return paginas.map(paginaNumero => (
                        <button
                          key={paginaNumero}
                          onClick={() => setPaginaActual(paginaNumero)}
                          style={{
                            padding: '6px 12px',
                            border: '1px solid #d1d5db',
                            borderRadius: '5px',
                            background: paginaActual === paginaNumero ? '#f59e0b' : 'white',
                            color: paginaActual === paginaNumero ? 'white' : '#374151',
                            cursor: 'pointer',
                            fontSize: '13px',
                            fontWeight: paginaActual === paginaNumero ? 'bold' : 'normal',
                            minWidth: '36px'
                          }}
                        >
                          {paginaNumero}
                        </button>
                      ));
                    })()}
                  </div>
                  
                  <button
                    onClick={() => setPaginaActual(p => Math.min(totalPaginas, p + 1))}
                    disabled={paginaActual === totalPaginas}
                    style={{
                      padding: '6px 12px',
                      border: '1px solid #d1d5db',
                      borderRadius: '5px',
                      background: paginaActual === totalPaginas ? '#f3f4f6' : 'white',
                      color: paginaActual === totalPaginas ? '#9ca3af' : '#374151',
                      cursor: paginaActual === totalPaginas ? 'not-allowed' : 'pointer',
                      fontSize: '13px'
                    }}
                  >
                    Siguiente
                  </button>
                  <button
                    onClick={() => setPaginaActual(totalPaginas)}
                    disabled={paginaActual === totalPaginas}
                    style={{
                      padding: '6px 12px',
                      border: '1px solid #d1d5db',
                      borderRadius: '5px',
                      background: paginaActual === totalPaginas ? '#f3f4f6' : 'white',
                      color: paginaActual === totalPaginas ? '#9ca3af' : '#374151',
                      cursor: paginaActual === totalPaginas ? 'not-allowed' : 'pointer',
                      fontSize: '13px'
                    }}
                  >
                    Última
                  </button>
                </div>
              </div>
            )}
          </>
        )}
      </div>

      {/* Recetas - Movida al final */}
      {recetas.length > 0 && (
        <div style={{ 
          background: '#f9fafb', 
          padding: '20px', 
          borderRadius: '10px', 
          marginTop: '20px',
          border: '1px solid #e5e7eb'
        }}>
          <h3 style={{ margin: '0 0 15px 0', color: '#374151' }}>🍽️ Preparar Recetas</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '15px' }}>
            {recetas.map(receta => (
              <div key={receta.id} style={{
                background: 'white',
                padding: '15px',
                borderRadius: '8px',
                border: '1px solid #d1d5db'
              }}>
                <div style={{ fontWeight: 'bold', marginBottom: '8px' }}>{receta.nombre}</div>
                <div style={{ fontSize: '12px', color: '#6b7280', marginBottom: '10px' }}>
                  {receta.descripcion || 'Sin descripción'}
                </div>
                <button
                  onClick={() => abrirPrepararReceta(receta)}
                  style={{
                    width: '100%',
                    background: '#f59e0b',
                    color: 'white',
                    border: 'none',
                    padding: '8px 16px',
                    borderRadius: '6px',
                    cursor: 'pointer',
                    fontSize: '14px',
                    fontWeight: 'bold'
                  }}
                >
                  Preparar Receta
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Wizard de Insumos */}
      <InsumoWizard
        isOpen={showWizard}
        onClose={handleWizardClose}
        insumoEditando={editingInsumo ? {
          id: editingInsumo.id,
          nombre: editingInsumo.nombre,
          tipo: editingInsumo.tipo,
          descripcion: editingInsumo.descripcion,
          unidadMedida: editingInsumo.unidadMedida,
          precioUnitario: editingInsumo.precioUnitario,
          stockActual: editingInsumo.stockActual,
          stockMinimo: editingInsumo.stockMinimo,
          proveedor: editingInsumo.proveedor,
          fechaVencimiento: editingInsumo.fechaVencimiento,
          activo: editingInsumo.estado === 'activo',
        } : undefined}
        onGuardar={handleWizardSave}
      />
    </div>
  );
};

export default InventarioPorcinosScreen;

