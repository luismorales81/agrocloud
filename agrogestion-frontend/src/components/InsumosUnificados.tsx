import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useEmpresa } from '../contexts/EmpresaContext';
import { insumosService } from '../services/apiServices';
import PermissionGate from './PermissionGate';
import InsumoWizard from './InsumoWizard';

interface InsumoUnificado {
  id: number;
  nombre: string;
  tipo: string;
  descripcion: string;
  unidadMedida: string;
  precioUnitario: number;
  stockActual: number;
  stockMinimo: number;
  proveedor: string;
  fechaVencimiento: string;
  activo: boolean;
  
  // Campos específicos de agroquímicos
  principioActivo?: string;
  concentracion?: string;
  claseQuimica?: string;
  categoriaToxicologica?: string;
  periodoCarenciaDias?: number;
  dosisMinimaPorHa?: number;
  dosisMaximaPorHa?: number;
  unidadDosis?: string;
  
  // Metadatos para identificación
  esAgroquimico: boolean;
  tienePropiedadesAgroquimicas: boolean;
}

const InsumosUnificados: React.FC = () => {
  const { user } = useAuth();
  const { rolUsuario } = useEmpresa();
  
  const [insumos, setInsumos] = useState<InsumoUnificado[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Estados para filtros
  const [filtroTipo, setFiltroTipo] = useState<'todos' | 'general' | 'agroquimico'>('todos');
  const [busqueda, setBusqueda] = useState('');
  const [ordenarPor, setOrdenarPor] = useState<'nombre' | 'tipo' | 'stock' | 'precio'>('nombre');
  
  // Estados para el wizard
  const [showWizard, setShowWizard] = useState(false);
  const [wizardMode, setWizardMode] = useState<'create' | 'edit'>('create');
  const [editingInsumo, setEditingInsumo] = useState<InsumoUnificado | null>(null);
  
  // Estados para paginación
  const [paginaActual, setPaginaActual] = useState(1);
  const [elementosPorPagina] = useState(10);

  useEffect(() => {
    cargarInsumos();
  }, []);

  const cargarInsumos = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Cargar TODOS los insumos de la tabla insumos
      const insumosData = await insumosService.listar();
      
      // Procesar todos los insumos y diferenciar por tipo
      const todosLosInsumos = (Array.isArray(insumosData) ? insumosData : []).map((insumo: any) => {
        // Determinar si es agroquímico basado en el tipo
        const tiposAgroquimicos = ['HERBICIDA', 'FUNGICIDA', 'INSECTICIDA'];
        const esAgroquimico = tiposAgroquimicos.includes(insumo.tipo);
        
        return {
          ...insumo,
          esAgroquimico: esAgroquimico,
          tienePropiedadesAgroquimicas: esAgroquimico
        };
      });
      
      // Log para depuración
      console.log('🔧 [InsumosUnificados] Todos los insumos:', todosLosInsumos);
      
      setInsumos(todosLosInsumos);
      
    } catch (err) {
      setError('Error al cargar los insumos. Verifique que el backend esté ejecutándose.');
      console.error('Error:', err);
    } finally {
      setLoading(false);
    }
  };

  // Funciones para el wizard
  const handleCreate = () => {
    setEditingInsumo(null);
    setWizardMode('create');
    setShowWizard(true);
  };

  const handleEdit = (insumo: InsumoUnificado) => {
    setEditingInsumo(insumo);
    setWizardMode('edit');
    setShowWizard(true);
  };

  const handleWizardClose = () => {
    setShowWizard(false);
    setEditingInsumo(null);
  };

  const handleWizardSave = () => {
    cargarInsumos();
    setShowWizard(false);
    setEditingInsumo(null);
  };

  // Filtrar insumos
  const insumosFiltrados = insumos.filter(insumo => {
    const coincideBusqueda = insumo.nombre.toLowerCase().includes(busqueda.toLowerCase()) ||
                            insumo.descripcion?.toLowerCase().includes(busqueda.toLowerCase());
    
    const coincideTipo = filtroTipo === 'todos' ||
                        (filtroTipo === 'general' && !insumo.esAgroquimico) ||
                        (filtroTipo === 'agroquimico' && insumo.esAgroquimico);
    
    return coincideBusqueda && coincideTipo;
  });

  // Ordenar insumos
  const insumosOrdenados = [...insumosFiltrados].sort((a, b) => {
    switch (ordenarPor) {
      case 'nombre':
        return a.nombre.localeCompare(b.nombre);
      case 'tipo':
        return a.tipo.localeCompare(b.tipo);
      case 'stock':
        return b.stockActual - a.stockActual;
      case 'precio':
        return b.precioUnitario - a.precioUnitario;
      default:
        return 0;
    }
  });

  // Paginación
  const totalPaginas = Math.ceil(insumosOrdenados.length / elementosPorPagina);
  const inicio = (paginaActual - 1) * elementosPorPagina;
  const fin = inicio + elementosPorPagina;
  const insumosPaginados = insumosOrdenados.slice(inicio, fin);

  // Obtener icono según el tipo
  const getIcono = (insumo: InsumoUnificado) => {
    if (insumo.esAgroquimico) {
      switch (insumo.tipo) {
        case 'HERBICIDA': return '🌿';
        case 'FUNGICIDA': return '🍄';
        case 'INSECTICIDA': return '🐛';
        case 'FERTILIZANTE': return '🌱';
        default: return '🧪';
      }
    } else {
      switch (insumo.tipo) {
        case 'FERTILIZANTE': return '🌱';
        case 'SEMILLA': return '🌾';
        case 'HERRAMIENTA': return '🔧';
        case 'COMBUSTIBLE': return '⛽';
        default: return '📦';
      }
    }
  };

  // Obtener color según el tipo
  const getColor = (insumo: InsumoUnificado) => {
    if (insumo.esAgroquimico) {
      return 'border-purple-200 bg-purple-50';
    } else {
      return 'border-blue-200 bg-blue-50';
    }
  };

  // Obtener etiqueta de tipo
  const getEtiquetaTipo = (insumo: InsumoUnificado) => {
    if (insumo.esAgroquimico) {
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
          🧪 Agroquímico
        </span>
      );
    } else {
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
          📦 Insumo General
        </span>
      );
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <PermissionGate permission="canViewInsumos">
      <div className="p-6">
        {/* Header */}
        <div className="mb-6">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            Gestión Unificada de Insumos
          </h1>
          <p className="text-gray-600">
            Administra insumos generales y agroquímicos en un solo lugar
          </p>
        </div>

        

        {/* Controles */}
        <div className="mb-6 flex flex-wrap gap-4 items-center">
          <PermissionGate permission="canCreateInsumos">
            <button
              onClick={handleCreate}
              className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-lg transition-colors"
            >
              ➕ Nuevo Insumo/Agroquímico
            </button>
          </PermissionGate>

          {/* Filtros */}
          <div className="flex flex-wrap gap-4 items-center">
            <select
              value={filtroTipo}
              onChange={(e) => setFiltroTipo(e.target.value as any)}
              className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="todos">Todos los tipos</option>
              <option value="general">Solo Insumos Generales</option>
              <option value="agroquimico">Solo Agroquímicos</option>
            </select>

            <input
              type="text"
              placeholder="Buscar insumos..."
              value={busqueda}
              onChange={(e) => setBusqueda(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />

            <select
              value={ordenarPor}
              onChange={(e) => setOrdenarPor(e.target.value as any)}
              className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="nombre">Ordenar por Nombre</option>
              <option value="tipo">Ordenar por Tipo</option>
              <option value="stock">Ordenar por Stock</option>
              <option value="precio">Ordenar por Precio</option>
            </select>
          </div>
        </div>

        {/* Estadísticas */}
        <div className="mb-6 grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="bg-white p-4 rounded-lg shadow">
            <div className="flex items-center">
              <div className="text-2xl mr-3">📦</div>
              <div>
                <p className="text-sm text-gray-600">Total Insumos</p>
                <p className="text-2xl font-bold text-gray-900">{insumos.length}</p>
              </div>
            </div>
          </div>
          
          <div className="bg-white p-4 rounded-lg shadow">
            <div className="flex items-center">
              <div className="text-2xl mr-3">🧪</div>
              <div>
                <p className="text-sm text-gray-600">Agroquímicos</p>
                <p className="text-2xl font-bold text-purple-600">
                  {insumos.filter(i => i.esAgroquimico).length}
                </p>
              </div>
            </div>
          </div>
          
          <div className="bg-white p-4 rounded-lg shadow">
            <div className="flex items-center">
              <div className="text-2xl mr-3">📦</div>
              <div>
                <p className="text-sm text-gray-600">Insumos Generales</p>
                <p className="text-2xl font-bold text-blue-600">
                  {insumos.filter(i => !i.esAgroquimico).length}
                </p>
              </div>
            </div>
          </div>
          
          <div className="bg-white p-4 rounded-lg shadow">
            <div className="flex items-center">
              <div className="text-2xl mr-3">⚠️</div>
              <div>
                <p className="text-sm text-gray-600">Stock Bajo</p>
                <p className="text-2xl font-bold text-red-600">
                  {insumos.filter(i => i.stockActual <= i.stockMinimo).length}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Lista de insumos */}
        <div className="bg-white shadow overflow-hidden sm:rounded-md">
          {insumosPaginados.length === 0 ? (
            <div className="px-6 py-8 text-center text-gray-500">
              <p>No se encontraron insumos con los filtros aplicados.</p>
            </div>
          ) : (
            <ul className="divide-y divide-gray-200">
              {insumosPaginados.map((insumo) => (
                <li key={insumo.id} className={`px-6 py-4 hover:bg-gray-50 ${getColor(insumo)}`}>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-4">
                      <div className="text-2xl">
                        {getIcono(insumo)}
                      </div>
                      
                      <div className="flex-1">
                        <div className="flex items-center space-x-2 mb-1">
                          <h3 className="text-lg font-medium text-gray-900">
                            {insumo.nombre}
                          </h3>
                          {getEtiquetaTipo(insumo)}
                        </div>
                        
                        <p className="text-sm text-gray-600 mb-2">
                          {insumo.descripcion}
                        </p>
                        
                        <div className="flex flex-wrap gap-4 text-sm text-gray-500">
                          <span>Stock: {insumo.stockActual} {insumo.unidadMedida}</span>
                          <span>Precio: ${insumo.precioUnitario}</span>
                          <span>Tipo: {insumo.tipo}</span>
                          
                          {insumo.esAgroquimico && insumo.principioActivo && (
                            <span>Principio: {insumo.principioActivo}</span>
                          )}
                          
                          {insumo.esAgroquimico && insumo.concentracion && (
                            <span>Concentración: {insumo.concentracion}</span>
                          )}
                        </div>
                      </div>
                    </div>
                    
                    <div className="flex items-center space-x-2">
                      <button
                        onClick={() => handleEdit(insumo)}
                        className="text-blue-600 hover:text-blue-800 text-sm font-medium"
                      >
                        Editar
                      </button>
                      
                      {insumo.stockActual <= insumo.stockMinimo && (
                        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
                          Stock Bajo
                        </span>
                      )}
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>

        {/* Paginación */}
        {totalPaginas > 1 && (
          <div className="mt-6 flex items-center justify-between">
            <div className="text-sm text-gray-700">
              Mostrando {inicio + 1} a {Math.min(fin, insumosOrdenados.length)} de {insumosOrdenados.length} insumos
            </div>
            
            <div className="flex space-x-2">
              <button
                onClick={() => setPaginaActual(1)}
                disabled={paginaActual === 1}
                className="px-3 py-1 border border-gray-300 rounded-md text-sm disabled:bg-gray-100 disabled:cursor-not-allowed"
              >
                Primera
              </button>
              
              <button
                onClick={() => setPaginaActual(paginaActual - 1)}
                disabled={paginaActual === 1}
                className="px-3 py-1 border border-gray-300 rounded-md text-sm disabled:bg-gray-100 disabled:cursor-not-allowed"
              >
                Anterior
              </button>
              
              <span className="px-3 py-1 text-sm">
                Página {paginaActual} de {totalPaginas}
              </span>
              
              <button
                onClick={() => setPaginaActual(paginaActual + 1)}
                disabled={paginaActual === totalPaginas}
                className="px-3 py-1 border border-gray-300 rounded-md text-sm disabled:bg-gray-100 disabled:cursor-not-allowed"
              >
                Siguiente
              </button>
              
              <button
                onClick={() => setPaginaActual(totalPaginas)}
                disabled={paginaActual === totalPaginas}
                className="px-3 py-1 border border-gray-300 rounded-md text-sm disabled:bg-gray-100 disabled:cursor-not-allowed"
              >
                Última
              </button>
            </div>
          </div>
        )}

        {/* Wizard */}
        <InsumoWizard
          isOpen={showWizard}
          onClose={handleWizardClose}
          insumoEditando={editingInsumo}
          onGuardar={handleWizardSave}
        />
      </div>
    </PermissionGate>
  );
};

export default InsumosUnificados;
