import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { Card, CardHeader, CardTitle, CardContent } from './ui/Card';
import { Button } from './ui/Button';
import Badge from './ui/Badge';
import VentaGranoModal from './VentaGranoModal';
import { useCurrencyContext } from '../contexts/CurrencyContext';

interface InventarioGrano {
  id: number;
  cosechaId: number;
  cultivoId: number;
  cultivoNombre: string;
  loteId: number;
  loteNombre: string;
  cantidadInicial: number;
  cantidadDisponible: number;
  unidadMedida: string;
  costoProduccionTotal: number;
  costoUnitario: number;
  fechaIngreso: string;
  estado: string;
  variedad?: string;
  calidad?: string;
  ubicacionAlmacenamiento?: string;
  observaciones?: string;
  valorTotal: number;
  cantidadVendida: number;
  porcentajeDisponible: number;
}

const InventarioGranosManagement: React.FC = () => {
  const [inventario, setInventario] = useState<InventarioGrano[]>([]);
  const [inventarioFiltrado, setInventarioFiltrado] = useState<InventarioGrano[]>([]);
  const [loading, setLoading] = useState(false);
  const [filtro, setFiltro] = useState<'todos' | 'disponible' | 'agotado'>('disponible');
  const [searchTerm, setSearchTerm] = useState('');
  const [showVentaModal, setShowVentaModal] = useState(false);
  const [inventarioSeleccionado, setInventarioSeleccionado] = useState<InventarioGrano | null>(null);
  const { formatCurrency } = useCurrencyContext();
  
  // Estados para paginación
  const [paginaActual, setPaginaActual] = useState(1);
  const [elementosPorPagina] = useState(10);

  useEffect(() => {
    cargarInventario();
  }, []);

  useEffect(() => {
    aplicarFiltros();
  }, [inventario, filtro, searchTerm]);

  // Función para obtener inventario paginado
  const obtenerInventarioPaginado = () => {
    const totalPaginas = Math.ceil(inventarioFiltrado.length / elementosPorPagina);
    const inicio = (paginaActual - 1) * elementosPorPagina;
    const fin = inicio + elementosPorPagina;
    const inventarioPaginado = inventarioFiltrado.slice(inicio, fin);
    
    return { inventarioPaginado, totalPaginas };
  };

  // Resetear paginación cuando cambien los filtros
  useEffect(() => {
    setPaginaActual(1);
  }, [filtro, searchTerm]);

  const cargarInventario = async () => {
    try {
      setLoading(true);
      const response = await api.get('/v1/inventario-granos');
      setInventario(response.data);
    } catch (error) {
      console.error('Error cargando inventario:', error);
      alert('Error al cargar el inventario');
    } finally {
      setLoading(false);
    }
  };

  const aplicarFiltros = () => {
    let resultado = [...inventario];

    // Filtro por estado
    if (filtro === 'disponible') {
      resultado = resultado.filter(i => i.estado === 'DISPONIBLE' && i.cantidadDisponible > 0);
    } else if (filtro === 'agotado') {
      resultado = resultado.filter(i => i.estado === 'AGOTADO' || i.cantidadDisponible <= 0);
    }

    // Búsqueda por texto
    if (searchTerm) {
      const termLower = searchTerm.toLowerCase();
      resultado = resultado.filter(i =>
        i.cultivoNombre.toLowerCase().includes(termLower) ||
        i.loteNombre.toLowerCase().includes(termLower) ||
        (i.variedad && i.variedad.toLowerCase().includes(termLower))
      );
    }

    setInventarioFiltrado(resultado);
  };

  const abrirModalVenta = (item: InventarioGrano) => {
    setInventarioSeleccionado(item);
    setShowVentaModal(true);
  };

  const cerrarModalVenta = () => {
    setShowVentaModal(false);
    setInventarioSeleccionado(null);
  };

  const handleVentaExitosa = () => {
    cargarInventario();
    cerrarModalVenta();
  };

  const formatearFecha = (fecha: string) => {
    if (!fecha) return 'N/A';
    const date = new Date(fecha);
    return date.toLocaleDateString('es-ES');
  };

  const obtenerColorEstado = (estado: string): "green" | "yellow" | "red" | "blue" | "gray" => {
    switch (estado) {
      case 'DISPONIBLE': return 'green';
      case 'RESERVADO': return 'yellow';
      case 'AGOTADO': return 'gray';
      default: return 'blue';
    }
  };

  // Calcular totales
  const totalStockValor = inventarioFiltrado.reduce((sum, i) => sum + (i.valorTotal || 0), 0);
  const totalCantidad = inventarioFiltrado.reduce((sum, i) => sum + i.cantidadDisponible, 0);
  const itemsDisponibles = inventarioFiltrado.filter(i => i.cantidadDisponible > 0).length;

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-900">📦 Inventario de Granos</h1>
        <Button onClick={cargarInventario} className="bg-blue-600 hover:bg-blue-700">
          🔄 Actualizar
        </Button>
      </div>

      {/* Resumen del Inventario */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <Card>
          <CardContent className="pt-6">
            <div className="text-center">
              <p className="text-3xl font-bold text-blue-600">{inventarioFiltrado.length}</p>
              <p className="text-sm text-gray-600">Total Registros</p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="pt-6">
            <div className="text-center">
              <p className="text-3xl font-bold text-green-600">{itemsDisponibles}</p>
              <p className="text-sm text-gray-600">Disponibles</p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="pt-6">
            <div className="text-center">
              <p className="text-3xl font-bold text-purple-600">{totalCantidad.toFixed(2)}</p>
              <p className="text-sm text-gray-600">Total Stock</p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="pt-6">
            <div className="text-center">
              <p className="text-3xl font-bold text-orange-600">{formatCurrency(totalStockValor)}</p>
              <p className="text-sm text-gray-600">Valor Inventario</p>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Filtros */}
      <Card className="mb-6">
        <CardContent className="pt-6">
          <div className="flex flex-col md:flex-row gap-4">
            <div className="flex-1">
              <input
                type="text"
                placeholder="🔍 Buscar por cultivo, lote o variedad..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full px-4 py-2 border rounded-lg"
              />
            </div>
            <div className="flex gap-2">
              <Button
                onClick={() => setFiltro('disponible')}
                className={filtro === 'disponible' ? 'bg-green-600' : 'bg-gray-400'}
              >
                Disponible
              </Button>
              <Button
                onClick={() => setFiltro('todos')}
                className={filtro === 'todos' ? 'bg-blue-600' : 'bg-gray-400'}
              >
                Todos
              </Button>
              <Button
                onClick={() => setFiltro('agotado')}
                className={filtro === 'agotado' ? 'bg-gray-600' : 'bg-gray-400'}
              >
                Agotado
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Lista de Inventario */}
      <Card>
        <CardHeader>
          <CardTitle>📦 Stock de Granos</CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="text-center py-8">
              <p>⏳ Cargando inventario...</p>
            </div>
          ) : inventarioFiltrado.length === 0 ? (
            <div className="text-center py-8">
              <p className="text-gray-500">No hay registros de inventario</p>
              <p className="text-sm text-gray-400 mt-2">
                El inventario se crea automáticamente cuando cosechas un lote
              </p>
            </div>
          ) : (() => {
            const { inventarioPaginado, totalPaginas } = obtenerInventarioPaginado();
            
            if (inventarioFiltrado.length === 0) {
              return (
                <div className="text-center py-8">
                  <p className="text-gray-500">No hay registros de inventario</p>
                  <p className="text-sm text-gray-400 mt-2">
                    El inventario se crea automáticamente cuando cosechas un lote
                  </p>
                </div>
              );
            }

            return (
              <>
                <div className="space-y-4">
                  {inventarioPaginado.map((item) => (
                <div
                  key={item.id}
                  className="flex items-center justify-between p-4 border rounded-lg hover:bg-gray-50"
                >
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <h3 className="font-semibold text-lg">{item.cultivoNombre}</h3>
                      {item.variedad && (
                        <span className="text-sm text-gray-500">({item.variedad})</span>
                      )}
                      <Badge color={obtenerColorEstado(item.estado)}>
                        {item.estado}
                      </Badge>
                    </div>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-2 text-sm text-gray-600">
                      <p>📍 Lote: <span className="font-medium">{item.loteNombre}</span></p>
                      <p>📅 Cosechado: <span className="font-medium">{formatearFecha(item.fechaIngreso)}</span></p>
                      <p>📦 Cantidad Inicial: <span className="font-medium">{item.cantidadInicial} {item.unidadMedida}</span></p>
                      <p>✅ Disponible: <span className="font-medium text-green-600">{item.cantidadDisponible} {item.unidadMedida}</span></p>
                      <p>📊 Vendido: <span className="font-medium">{item.cantidadVendida} {item.unidadMedida}</span> ({(100 - item.porcentajeDisponible).toFixed(1)}%)</p>
                      <p>💰 Costo Unitario: <span className="font-medium">{formatCurrency(item.costoUnitario)}/{item.unidadMedida}</span></p>
                    </div>

                    {item.ubicacionAlmacenamiento && (
                      <p className="text-sm text-gray-500 mt-2">
                        🏪 Ubicación: {item.ubicacionAlmacenamiento}
                      </p>
                    )}
                  </div>

                  <div className="text-right space-y-2 ml-4">
                    <div>
                      <p className="text-2xl font-bold text-purple-600">
                        {item.cantidadDisponible.toFixed(2)}
                      </p>
                      <p className="text-sm text-gray-500">{item.unidadMedida}</p>
                    </div>
                    <div className="text-sm text-gray-600">
                      <p>Valor: <span className="font-semibold">{formatCurrency(item.valorTotal)}</span></p>
                    </div>
                    {item.cantidadDisponible > 0 && (
                      <Button
                        onClick={() => abrirModalVenta(item)}
                        className="bg-green-600 hover:bg-green-700 text-sm w-full"
                      >
                        💵 Vender
                      </Button>
                    )}
                  </div>
                  </div>
                ))}
                </div>

                {/* Paginación */}
                {totalPaginas > 1 && (
                  <div className="flex items-center justify-between mt-6 pt-4 border-t">
                    <div className="flex items-center gap-2">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setPaginaActual(1)}
                        disabled={paginaActual === 1}
                      >
                        ⏮️ Primera
                      </Button>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setPaginaActual(paginaActual - 1)}
                        disabled={paginaActual === 1}
                      >
                        ⬅️ Anterior
                      </Button>
                    </div>

                    <div className="flex items-center gap-2">
                      <span className="text-sm text-gray-600">
                        Página {paginaActual} de {totalPaginas}
                      </span>
                    </div>

                    <div className="flex items-center gap-2">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setPaginaActual(paginaActual + 1)}
                        disabled={paginaActual === totalPaginas}
                      >
                        Siguiente ➡️
                      </Button>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setPaginaActual(totalPaginas)}
                        disabled={paginaActual === totalPaginas}
                      >
                        Última ⏭️
                      </Button>
                    </div>
                  </div>
                )}
              </>
            );
          })()}
        </CardContent>
      </Card>

      {/* Modal de Venta */}
      {showVentaModal && inventarioSeleccionado && (
        <VentaGranoModal
          inventario={inventarioSeleccionado}
          onClose={cerrarModalVenta}
          onSuccess={handleVentaExitosa}
        />
      )}
    </div>
  );
};

export default InventarioGranosManagement;

