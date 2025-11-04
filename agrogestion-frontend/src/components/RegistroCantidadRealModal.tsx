import React, { useState, useEffect } from 'react';
import api, { agroquimicoIntegradoService } from '../services/api';

interface AplicacionAgroquimico {
  id: number;
  laborId: number;
  agroquimicoId: number;
  nombreAgroquimico: string;
  tipoAplicacion: string;
  superficieAplicadaHa: number;
  dosisAplicadaPorHa: number;
  cantidadEsperada: number;
  cantidadRealUsada?: number;
  diferenciaCantidad?: number;
  porcentajeDesviacionCantidad?: number;
  motivoDesviacion?: string;
  unidadMedida: string;
}

interface CantidadRealRequest {
  aplicacionId: number;
  cantidadReal: number;
  motivoDesviacion?: string;
  observaciones?: string;
}

interface RegistroCantidadRealModalProps {
  onClose: () => void;
  onCantidadRegistrada: () => void;
}

const RegistroCantidadRealModal: React.FC<RegistroCantidadRealModalProps> = ({
  onClose,
  onCantidadRegistrada
}) => {
  const [aplicaciones, setAplicaciones] = useState<AplicacionAgroquimico[]>([]);
  const [cantidadesReales, setCantidadesReales] = useState<CantidadRealRequest[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    cargarAplicacionesPendientes();
  }, []);

  const cargarAplicacionesPendientes = async () => {
    try {
      setLoading(true);
      // Simular carga de aplicaciones pendientes
      // En un caso real, esto vendría de una API
      const aplicacionesSimuladas: AplicacionAgroquimico[] = [
        {
          id: 1,
          laborId: 1,
          agroquimicoId: 1,
          nombreAgroquimico: 'Glifosato 48%',
          tipoAplicacion: 'FOLIAR',
          superficieAplicadaHa: 5.0,
          dosisAplicadaPorHa: 2.0,
          cantidadEsperada: 10.0,
          unidadMedida: 'L'
        },
        {
          id: 2,
          laborId: 1,
          agroquimicoId: 2,
          nombreAgroquimico: 'Atrazina 50%',
          tipoAplicacion: 'TERRESTRE',
          superficieAplicadaHa: 5.0,
          dosisAplicadaPorHa: 1.5,
          cantidadEsperada: 7.5,
          unidadMedida: 'L'
        }
      ];
      
      setAplicaciones(aplicacionesSimuladas);
      
      // Inicializar cantidades reales con las esperadas
      const cantidadesIniciales = aplicacionesSimuladas.map(app => ({
        aplicacionId: app.id,
        cantidadReal: app.cantidadEsperada,
        motivoDesviacion: '',
        observaciones: ''
      }));
      setCantidadesReales(cantidadesIniciales);
      
    } catch (err) {
      setError('Error al cargar aplicaciones pendientes');
      console.error('Error:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCantidadRealChange = (aplicacionId: number, cantidadReal: number) => {
    setCantidadesReales(prev => prev.map(cr => 
      cr.aplicacionId === aplicacionId 
        ? { ...cr, cantidadReal }
        : cr
    ));
  };

  const handleMotivoChange = (aplicacionId: number, motivo: string) => {
    setCantidadesReales(prev => prev.map(cr => 
      cr.aplicacionId === aplicacionId 
        ? { ...cr, motivoDesviacion: motivo }
        : cr
    ));
  };

  const handleObservacionesChange = (aplicacionId: number, observaciones: string) => {
    setCantidadesReales(prev => prev.map(cr => 
      cr.aplicacionId === aplicacionId 
        ? { ...cr, observaciones }
        : cr
    ));
  };

  const calcularDiferencias = (aplicacion: AplicacionAgroquimico, cantidadReal: number) => {
    const diferencia = cantidadReal - aplicacion.cantidadEsperada;
    const porcentaje = aplicacion.cantidadEsperada > 0 
      ? (diferencia / aplicacion.cantidadEsperada) * 100 
      : 0;
    
    return { diferencia, porcentaje };
  };

  const esDesviacionSignificativa = (porcentaje: number) => {
    return Math.abs(porcentaje) > 10;
  };

  const getDesviacionColor = (porcentaje: number) => {
    if (Math.abs(porcentaje) <= 5) return 'text-green-600';
    if (Math.abs(porcentaje) <= 10) return 'text-yellow-600';
    return 'text-red-600';
  };

  const registrarCantidadesReales = async () => {
    try {
      setLoading(true);
      
      // Validar que todas las cantidades estén registradas
      const cantidadesFaltantes = cantidadesReales.filter(cr => cr.cantidadReal <= 0);
      if (cantidadesFaltantes.length > 0) {
        setError('Debe registrar una cantidad real mayor a 0 para todas las aplicaciones');
        return;
      }

      // Enviar datos al backend
      await agroquimicoIntegradoService.ejecutarLabor(1, {
        cantidadesReales: cantidadesReales,
        observacionesGenerales: '',
        confirmarEjecucion: true
      });

      alert('Cantidades reales registradas exitosamente');
      onCantidadRegistrada();
      
    } catch (err) {
      setError('Error al registrar cantidades reales');
      console.error('Error:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading && aplicaciones.length === 0) {
    return (
      <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
        <div className="relative top-20 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-2/3 shadow-lg rounded-md bg-white">
          <div className="flex justify-center items-center h-32">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
      <div className="relative top-10 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-2/3 shadow-lg rounded-md bg-white">
        <div className="mt-3">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-medium text-gray-900">
              📊 Registrar Cantidad Real Utilizada
            </h3>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 text-2xl"
            >
              ✕
            </button>
          </div>

          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
              {error}
            </div>
          )}

          <div className="space-y-6">
            {aplicaciones.map((aplicacion) => {
              const cantidadReal = cantidadesReales.find(cr => cr.aplicacionId === aplicacion.id)?.cantidadReal || aplicacion.cantidadEsperada;
              const { diferencia, porcentaje } = calcularDiferencias(aplicacion, cantidadReal);
              const desviacionSignificativa = esDesviacionSignificativa(porcentaje);
              
              return (
                <div key={aplicacion.id} className="p-4 border rounded-lg bg-gray-50">
                  <div className="mb-4">
                    <h4 className="font-semibold text-gray-900 mb-2">
                      {aplicacion.nombreAgroquimico}
                    </h4>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm text-gray-600">
                      <div>Tipo: {aplicacion.tipoAplicacion}</div>
                      <div>Superficie: {aplicacion.superficieAplicadaHa} ha</div>
                      <div>Dosis: {aplicacion.dosisAplicadaPorHa} {aplicacion.unidadMedida}/ha</div>
                    </div>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Cantidad Esperada
                      </label>
                      <div className="p-2 bg-blue-50 border border-blue-200 rounded text-blue-800 font-medium">
                        {aplicacion.cantidadEsperada} {aplicacion.unidadMedida}
                      </div>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Cantidad Real Utilizada
                      </label>
                      <input
                        type="number"
                        step="0.1"
                        min="0"
                        value={cantidadReal}
                        onChange={(e) => handleCantidadRealChange(aplicacion.id, Number(e.target.value))}
                        className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                  </div>

                  {/* Diferencia automática */}
                  {cantidadReal !== aplicacion.cantidadEsperada && (
                    <div className="mb-4 p-3 bg-yellow-50 border border-yellow-200 rounded">
                      <div className="flex justify-between items-center">
                        <span className="font-medium text-yellow-800">Diferencia:</span>
                        <span className={`font-bold ${getDesviacionColor(porcentaje)}`}>
                          {diferencia > 0 ? '+' : ''}{diferencia.toFixed(2)} {aplicacion.unidadMedida}
                          ({porcentaje > 0 ? '+' : ''}{porcentaje.toFixed(1)}%)
                        </span>
                      </div>
                    </div>
                  )}

                  {/* Motivo de desviación si hay diferencia significativa */}
                  {desviacionSignificativa && (
                    <div className="mb-4">
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Motivo de la Diferencia *
                      </label>
                      <textarea
                        value={cantidadesReales.find(cr => cr.aplicacionId === aplicacion.id)?.motivoDesviacion || ''}
                        onChange={(e) => handleMotivoChange(aplicacion.id, e.target.value)}
                        placeholder="Explica por qué hubo diferencia en la cantidad..."
                        rows={3}
                        className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                        required
                      />
                    </div>
                  )}

                  {/* Observaciones adicionales */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Observaciones Adicionales
                    </label>
                    <textarea
                      value={cantidadesReales.find(cr => cr.aplicacionId === aplicacion.id)?.observaciones || ''}
                      onChange={(e) => handleObservacionesChange(aplicacion.id, e.target.value)}
                      placeholder="Observaciones sobre la aplicación..."
                      rows={2}
                      className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                </div>
              );
            })}
          </div>

          {/* Botones de acción */}
          <div className="flex justify-end space-x-3 pt-6">
            <button
              onClick={onClose}
              className="bg-gray-300 hover:bg-gray-400 text-gray-800 font-bold py-2 px-4 rounded"
            >
              Cancelar
            </button>
            <button
              onClick={registrarCantidadesReales}
              disabled={loading}
              className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
            >
              {loading ? '🔄 Registrando...' : '📊 Registrar Cantidades Reales'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RegistroCantidadRealModal;
