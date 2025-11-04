import React, { useState, useEffect } from 'react';
import api, { agroquimicoIntegradoService } from '../services/api';

interface CondicionesAmbientales {
  temperatura: number;
  humedad: number;
  velocidadViento: number;
  direccionViento?: string;
  presionAtmosferica?: number;
  observaciones?: string;
}

interface CondicionesAmbientalesModalProps {
  condiciones: CondicionesAmbientales | null;
  onClose: () => void;
  onCondicionesGuardadas: (condiciones: CondicionesAmbientales) => void;
}

const CondicionesAmbientalesModal: React.FC<CondicionesAmbientalesModalProps> = ({
  condiciones,
  onClose,
  onCondicionesGuardadas
}) => {
  const [formData, setFormData] = useState<CondicionesAmbientales>({
    temperatura: 20,
    humedad: 65,
    velocidadViento: 5,
    direccionViento: 'N',
    presionAtmosferica: 1013,
    observaciones: ''
  });

  const [condicionesAdecuadas, setCondicionesAdecuadas] = useState<boolean>(true);
  const [resumenCondiciones, setResumenCondiciones] = useState<string>('');

  useEffect(() => {
    if (condiciones) {
      setFormData(condiciones);
    }
  }, [condiciones]);

  useEffect(() => {
    // Evaluar si las condiciones son adecuadas
    const tempAdecuada = formData.temperatura >= 5 && formData.temperatura <= 35;
    const humedadAdecuada = formData.humedad >= 30 && formData.humedad <= 90;
    const vientoAdecuado = formData.velocidadViento <= 15;
    
    const adecuadas = tempAdecuada && humedadAdecuada && vientoAdecuado;
    setCondicionesAdecuadas(adecuadas);

    // Generar resumen
    const resumen = `Temp: ${formData.temperatura}°C, Humedad: ${formData.humedad}%, Viento: ${formData.velocidadViento} km/h${formData.direccionViento ? ` ${formData.direccionViento}` : ''}`;
    setResumenCondiciones(resumen);
  }, [formData]);

  const handleInputChange = (field: keyof CondicionesAmbientales, value: number | string) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const obtenerCondicionesRecomendadas = async () => {
    try {
      const response = await agroquimicoIntegradoService.getCondicionesRecomendadas();
      setFormData(response);
    } catch (err) {
      console.error('Error al obtener condiciones recomendadas:', err);
    }
  };

  const getCondicionesColor = () => {
    if (condicionesAdecuadas) return 'text-green-600 bg-green-100';
    return 'text-red-600 bg-red-100';
  };

  const getCondicionesIcon = () => {
    if (condicionesAdecuadas) return '✅';
    return '⚠️';
  };

  const getRecomendaciones = () => {
    const recomendaciones = [];
    
    if (formData.temperatura < 5 || formData.temperatura > 35) {
      recomendaciones.push('Temperatura fuera del rango recomendado (5-35°C)');
    }
    
    if (formData.humedad < 30 || formData.humedad > 90) {
      recomendaciones.push('Humedad fuera del rango recomendado (30-90%)');
    }
    
    if (formData.velocidadViento > 15) {
      recomendaciones.push('Velocidad del viento muy alta (>15 km/h)');
    }
    
    return recomendaciones;
  };

  const handleGuardar = () => {
    onCondicionesGuardadas(formData);
  };

  return (
    <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
      <div className="relative top-10 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-1/2 shadow-lg rounded-md bg-white">
        <div className="mt-3">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-medium text-gray-900">
              🌡️ Condiciones Ambientales
            </h3>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600 text-2xl"
            >
              ✕
            </button>
          </div>

          {/* Resumen de condiciones */}
          <div className={`mb-6 p-4 rounded-lg ${getCondicionesColor()}`}>
            <div className="flex items-center justify-between">
              <div>
                <h4 className="font-semibold">
                  {getCondicionesIcon()} Estado de las Condiciones
                </h4>
                <p className="text-sm mt-1">{resumenCondiciones}</p>
              </div>
              <div className="text-right">
                <div className="font-bold">
                  {condicionesAdecuadas ? 'Adecuadas' : 'No Adecuadas'}
                </div>
              </div>
            </div>
          </div>

          {/* Formulario de condiciones */}
          <div className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Temperatura (°C)
                </label>
                <input
                  type="number"
                  step="0.1"
                  value={formData.temperatura}
                  onChange={(e) => handleInputChange('temperatura', Number(e.target.value))}
                  className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <p className="text-xs text-gray-500 mt-1">Rango recomendado: 5-35°C</p>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Humedad (%)
                </label>
                <input
                  type="number"
                  step="0.1"
                  min="0"
                  max="100"
                  value={formData.humedad}
                  onChange={(e) => handleInputChange('humedad', Number(e.target.value))}
                  className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <p className="text-xs text-gray-500 mt-1">Rango recomendado: 30-90%</p>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Velocidad del Viento (km/h)
                </label>
                <input
                  type="number"
                  step="0.1"
                  min="0"
                  value={formData.velocidadViento}
                  onChange={(e) => handleInputChange('velocidadViento', Number(e.target.value))}
                  className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <p className="text-xs text-gray-500 mt-1">Máximo recomendado: 15 km/h</p>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Dirección del Viento
                </label>
                <select
                  value={formData.direccionViento || ''}
                  onChange={(e) => handleInputChange('direccionViento', e.target.value)}
                  className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">Seleccionar</option>
                  <option value="N">Norte (N)</option>
                  <option value="NE">Noreste (NE)</option>
                  <option value="E">Este (E)</option>
                  <option value="SE">Sureste (SE)</option>
                  <option value="S">Sur (S)</option>
                  <option value="SW">Suroeste (SW)</option>
                  <option value="W">Oeste (W)</option>
                  <option value="NW">Noroeste (NW)</option>
                </select>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Presión Atmosférica (hPa)
                </label>
                <input
                  type="number"
                  step="0.1"
                  value={formData.presionAtmosferica || 1013}
                  onChange={(e) => handleInputChange('presionAtmosferica', Number(e.target.value))}
                  className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <p className="text-xs text-gray-500 mt-1">Rango normal: 800-1100 hPa</p>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Observaciones
              </label>
              <textarea
                value={formData.observaciones || ''}
                onChange={(e) => handleInputChange('observaciones', e.target.value)}
                placeholder="Observaciones adicionales sobre las condiciones ambientales..."
                rows={3}
                className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          {/* Recomendaciones */}
          {!condicionesAdecuadas && (
            <div className="mt-4 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
              <h4 className="font-semibold text-yellow-800 mb-2">⚠️ Recomendaciones</h4>
              <ul className="text-sm text-yellow-700 space-y-1">
                {getRecomendaciones().map((recomendacion, index) => (
                  <li key={index}>• {recomendacion}</li>
                ))}
              </ul>
            </div>
          )}

          {/* Botones de acción */}
          <div className="flex justify-between pt-6">
            <button
              onClick={obtenerCondicionesRecomendadas}
              className="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded"
            >
              🌡️ Condiciones Recomendadas
            </button>
            <div className="flex space-x-3">
              <button
                onClick={onClose}
                className="bg-gray-300 hover:bg-gray-400 text-gray-800 font-bold py-2 px-4 rounded"
              >
                Cancelar
              </button>
              <button
                onClick={handleGuardar}
                className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
              >
                💾 Guardar Condiciones
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CondicionesAmbientalesModal;
