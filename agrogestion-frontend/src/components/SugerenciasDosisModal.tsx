import React, { useState, useEffect } from 'react';
import api, { agroquimicoIntegradoService } from '../services/api';

interface InsumoAgroquimico {
  id: number;
  nombre: string;
  principioActivo?: string;
  concentracion?: string;
  claseQuimica?: string;
  stockActual: number;
  unidadMedida: string;
}

interface CondicionesAmbientales {
  temperatura: number;
  humedad: number;
  velocidadViento: number;
  direccionViento?: string;
  presionAtmosferica?: number;
  observaciones?: string;
}

interface DosisSugerida {
  agroquimicoId: number;
  nombreAgroquimico: string;
  tipoAplicacion: string;
  dosisSugeridaPorHa: number;
  cantidadTotalSugerida: number;
  superficieHa: number;
  stockDisponible: number;
  stockSuficiente: boolean;
  unidadMedida: string;
  condicionesAplicacion: string;
  factorAjuste: number;
  nivelRiesgo: string;
  observaciones?: string;
}

interface SugerenciasDosisModalProps {
  agroquimico: InsumoAgroquimico;
  condiciones: CondicionesAmbientales | null;
  onClose: () => void;
  onSugerenciasObtenidas: (sugerencias: DosisSugerida[]) => void;
}

const SugerenciasDosisModal: React.FC<SugerenciasDosisModalProps> = ({
  agroquimico,
  condiciones,
  onClose,
  onSugerenciasObtenidas
}) => {
  const [sugerencias, setSugerencias] = useState<DosisSugerida[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [superficieHa, setSuperficieHa] = useState<number>(1);
  const [tipoAplicacion, setTipoAplicacion] = useState<string>('FOLIAR');

  const tiposAplicacion = [
    { value: 'FOLIAR', label: 'Foliar' },
    { value: 'TERRESTRE', label: 'Terrestre' },
    { value: 'AEREA', label: 'Aérea' },
    { value: 'PRECISION', label: 'Precisión' }
  ];

  const obtenerSugerencias = async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await agroquimicoIntegradoService.sugerirDosis({
        agroquimicoId: agroquimico.id,
        tipoAplicacion: tipoAplicacion,
        superficieHa: superficieHa,
        condicionesAmbientales: condiciones
      });

      const sugerencia: DosisSugerida = response;
      setSugerencias([sugerencia]);
      onSugerenciasObtenidas([sugerencia]);

    } catch (err) {
      setError('Error al obtener sugerencias de dosis');
      console.error('Error:', err);
    } finally {
      setLoading(false);
    }
  };

  const getNivelRiesgoColor = (nivel: string) => {
    switch (nivel) {
      case 'BAJO': return 'text-green-600 bg-green-100';
      case 'MEDIO': return 'text-yellow-600 bg-yellow-100';
      case 'ALTO': return 'text-red-600 bg-red-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  const getStockSuficienteColor = (suficiente: boolean) => {
    return suficiente ? 'text-green-600' : 'text-red-600';
  };

  return (
    <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
      <div className="relative top-10 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-2/3 shadow-lg rounded-md bg-white">
        <div className="mt-3">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-medium text-gray-900">
              💡 Sugerencias de Dosis - {agroquimico.nombre}
            </h3>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 text-2xl"
            >
              ✕
            </button>
          </div>

          {/* Información del agroquímico */}
          <div className="mb-6 p-4 bg-blue-50 rounded-lg">
            <h4 className="font-semibold text-blue-900 mb-2">Información del Agroquímico</h4>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
              <div>
                <span className="font-medium">Nombre:</span> {agroquimico.nombre}
              </div>
              {agroquimico.principioActivo && (
                <div>
                  <span className="font-medium">Principio Activo:</span> {agroquimico.principioActivo}
                </div>
              )}
              {agroquimico.concentracion && (
                <div>
                  <span className="font-medium">Concentración:</span> {agroquimico.concentracion}
                </div>
              )}
              {agroquimico.claseQuimica && (
                <div>
                  <span className="font-medium">Clase Química:</span> {agroquimico.claseQuimica}
                </div>
              )}
              <div>
                <span className="font-medium">Stock Disponible:</span> {agroquimico.stockActual} {agroquimico.unidadMedida}
              </div>
            </div>
          </div>

          {/* Parámetros de consulta */}
          <div className="mb-6 p-4 bg-gray-50 rounded-lg">
            <h4 className="font-semibold text-gray-900 mb-4">Parámetros de Consulta</h4>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Tipo de Aplicación
                </label>
                <select
                  value={tipoAplicacion}
                  onChange={(e) => setTipoAplicacion(e.target.value)}
                  className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  {tiposAplicacion.map(tipo => (
                    <option key={tipo.value} value={tipo.value}>
                      {tipo.label}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Superficie (hectáreas)
                </label>
                <input
                  type="number"
                  step="0.1"
                  min="0.1"
                  value={superficieHa}
                  onChange={(e) => setSuperficieHa(Number(e.target.value))}
                  className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
            </div>
          </div>

          {/* Condiciones ambientales */}
          {condiciones && (
            <div className="mb-6 p-4 bg-green-50 rounded-lg">
              <h4 className="font-semibold text-green-900 mb-2">Condiciones Ambientales</h4>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                <div>
                  <span className="font-medium">Temperatura:</span> {condiciones.temperatura}°C
                </div>
                <div>
                  <span className="font-medium">Humedad:</span> {condiciones.humedad}%
                </div>
                <div>
                  <span className="font-medium">Viento:</span> {condiciones.velocidadViento} km/h
                </div>
              </div>
            </div>
          )}

          {/* Botón para obtener sugerencias */}
          <div className="mb-6">
            <button
              onClick={obtenerSugerencias}
              disabled={loading}
              className="w-full bg-blue-500 hover:bg-blue-700 text-white font-bold py-3 px-4 rounded disabled:opacity-50"
            >
              {loading ? '🔄 Obteniendo Sugerencias...' : '💡 Obtener Sugerencias de Dosis'}
            </button>
          </div>

          {/* Mostrar sugerencias */}
          {sugerencias.length > 0 && (
            <div className="space-y-4">
              <h4 className="font-semibold text-gray-900">Sugerencias Obtenidas</h4>
              {sugerencias.map((sugerencia, index) => (
                <div key={index} className="p-4 border rounded-lg bg-yellow-50">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                    <div>
                      <span className="font-medium">Dosis Sugerida:</span>
                      <span className="ml-2 text-lg font-bold text-blue-600">
                        {sugerencia.dosisSugeridaPorHa} {sugerencia.unidadMedida}/ha
                      </span>
                    </div>
                    <div>
                      <span className="font-medium">Cantidad Total:</span>
                      <span className="ml-2 text-lg font-bold text-green-600">
                        {sugerencia.cantidadTotalSugerida} {sugerencia.unidadMedida}
                      </span>
                    </div>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                    <div>
                      <span className="font-medium">Stock Disponible:</span>
                      <span className={`ml-2 font-bold ${getStockSuficienteColor(sugerencia.stockSuficiente)}`}>
                        {sugerencia.stockDisponible} {sugerencia.unidadMedida}
                        {sugerencia.stockSuficiente ? ' ✅' : ' ❌'}
                      </span>
                    </div>
                    <div>
                      <span className="font-medium">Nivel de Riesgo:</span>
                      <span className={`ml-2 px-2 py-1 rounded text-sm font-medium ${getNivelRiesgoColor(sugerencia.nivelRiesgo)}`}>
                        {sugerencia.nivelRiesgo}
                      </span>
                    </div>
                  </div>

                  <div className="mb-4">
                    <span className="font-medium">Condiciones de Aplicación:</span>
                    <p className="mt-1 text-sm text-gray-600">{sugerencia.condicionesAplicacion}</p>
                  </div>

                  {sugerencia.factorAjuste !== 1 && (
                    <div className="mb-4">
                      <span className="font-medium">Factor de Ajuste Aplicado:</span>
                      <span className="ml-2 text-sm text-gray-600">
                        {sugerencia.factorAjuste > 1 ? '+' : ''}
                        {((sugerencia.factorAjuste - 1) * 100).toFixed(1)}%
                      </span>
                    </div>
                  )}

                  {sugerencia.observaciones && (
                    <div className="mb-4">
                      <span className="font-medium">Observaciones:</span>
                      <p className="mt-1 text-sm text-gray-600">{sugerencia.observaciones}</p>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}

          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
              {error}
            </div>
          )}

          {/* Botones de acción */}
          <div className="flex justify-end space-x-3 pt-4">
            <button
              onClick={onClose}
              className="bg-gray-300 hover:bg-gray-400 text-gray-800 font-bold py-2 px-4 rounded"
            >
              Cerrar
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SugerenciasDosisModal;
