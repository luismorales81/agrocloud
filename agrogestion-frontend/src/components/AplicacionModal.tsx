import React, { useState, useEffect } from 'react';
import { laboresService, agroquimicosIntegradosService } from '../services/apiServices';

interface AplicacionAgroquimico {
  id?: number;
  laborId: number;
  agroquimicoId: number;
  tipoAplicacion: string;
  superficieAplicadaHa: number;
  dosisAplicadaPorHa: number;
  cantidadTotalAplicada: number;
  dosisRecomendadaPorHa: number;
  unidadMedida: string;
  condicionesClimaticas: string;
  equipoAplicacion: string;
  operador: string;
  observaciones: string;
  fechaAplicacion: string;
}

interface Agroquimico {
  id: number;
  nombreInsumo: string;
  principioActivo: string;
  concentracion: string;
  stockActual: number;
  unidadMedidaInsumo: string;
}

interface Labor {
  id: number;
  tipoLabor: string;
  fechaInicio: string;
  loteId: number;
  loteNombre: string;
  loteSuperficieHa: number;
}

interface AplicacionModalProps {
  aplicacion: AplicacionAgroquimico | null;
  agroquimicos: Agroquimico[];
  onClose: () => void;
  onSave: (data: any) => void;
}

const AplicacionModal: React.FC<AplicacionModalProps> = ({ aplicacion, agroquimicos, onClose, onSave }) => {
  const [formData, setFormData] = useState<AplicacionAgroquimico>({
    laborId: 0,
    agroquimicoId: 0,
    tipoAplicacion: 'FOLIAR',
    superficieAplicadaHa: 0,
    dosisAplicadaPorHa: 0,
    cantidadTotalAplicada: 0,
    dosisRecomendadaPorHa: 0,
    unidadMedida: 'L',
    condicionesClimaticas: '',
    equipoAplicacion: '',
    operador: '',
    observaciones: '',
    fechaAplicacion: new Date().toISOString().split('T')[0]
  });

  const [labores, setLabores] = useState<Labor[]>([]);
  const [dosisRecomendada, setDosisRecomendada] = useState<number>(0);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    cargarLabores();
    if (aplicacion) {
      setFormData(aplicacion);
    }
  }, [aplicacion]);

  const cargarLabores = async () => {
    try {
      const laboresData = await laboresService.listar();
      setLabores(Array.isArray(laboresData) ? laboresData : []);
    } catch (error) {
      console.error('Error al cargar labores:', error);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name.includes('Ha') || name.includes('Id') ? Number(value) : value
    }));
  };

  const handleAgroquimicoChange = async (agroquimicoId: number) => {
    setFormData(prev => ({ ...prev, agroquimicoId }));
    
    if (agroquimicoId && formData.tipoAplicacion) {
      try {
        const dosisData = await agroquimicosIntegradosService.obtenerDosisRecomendada(
          agroquimicoId,
          formData.tipoAplicacion
        );
        setDosisRecomendada(dosisData.dosisRecomendada || 0);
        setFormData(prev => ({ 
          ...prev, 
          dosisRecomendadaPorHa: dosisData.dosisRecomendada || 0 
        }));
      } catch (error) {
        console.error('Error al obtener dosis recomendada:', error);
      }
    }
  };

  const handleTipoAplicacionChange = async (tipoAplicacion: string) => {
    setFormData(prev => ({ ...prev, tipoAplicacion }));
    
    if (formData.agroquimicoId && tipoAplicacion) {
      try {
        const dosisData = await agroquimicosIntegradosService.obtenerDosisRecomendada(
          formData.agroquimicoId,
          tipoAplicacion
        );
        setDosisRecomendada(dosisData.dosisRecomendada || 0);
        setFormData(prev => ({ 
          ...prev, 
          dosisRecomendadaPorHa: dosisData.dosisRecomendada || 0 
        }));
      } catch (error) {
        console.error('Error al obtener dosis recomendada:', error);
      }
    }
  };

  const calcularCantidadTotal = () => {
    const cantidad = formData.superficieAplicadaHa * formData.dosisAplicadaPorHa;
    setFormData(prev => ({ ...prev, cantidadTotalAplicada: cantidad }));
  };

  useEffect(() => {
    calcularCantidadTotal();
  }, [formData.superficieAplicadaHa, formData.dosisAplicadaPorHa]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      await onSave(formData);
    } catch (error) {
      console.error('Error al guardar:', error);
    } finally {
      setLoading(false);
    }
  };

  const calcularDesviacion = () => {
    if (formData.dosisRecomendadaPorHa > 0) {
      const desviacion = ((formData.dosisAplicadaPorHa - formData.dosisRecomendadaPorHa) / formData.dosisRecomendadaPorHa) * 100;
      return desviacion.toFixed(2);
    }
    return 0;
  };

  const getNivelDesviacion = (desviacion: number) => {
    const absDesviacion = Math.abs(desviacion);
    if (absDesviacion <= 5) return 'Óptima';
    if (absDesviacion <= 10) return 'Aceptable';
    if (absDesviacion <= 20) return 'Alta';
    return 'Crítica';
  };

  const desviacion = calcularDesviacion();
  const nivelDesviacion = getNivelDesviacion(Number(desviacion));

  return (
    <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
      <div className="relative top-10 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-2/3 shadow-lg rounded-md bg-white">
        <div className="mt-3">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-medium text-gray-900">
              {aplicacion ? 'Editar Aplicación' : 'Nueva Aplicación'}
            </h3>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600"
            >
              ✕
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Labor</label>
                <select
                  name="laborId"
                  value={formData.laborId}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  required
                >
                  <option value={0}>Seleccionar labor</option>
                  {labores.map(labor => (
                    <option key={labor.id} value={labor.id}>
                      {labor.tipoLabor} - {labor.loteNombre} ({labor.loteSuperficieHa} ha)
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Agroquímico</label>
                <select
                  name="agroquimicoId"
                  value={formData.agroquimicoId}
                  onChange={(e) => handleAgroquimicoChange(Number(e.target.value))}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  required
                >
                  <option value={0}>Seleccionar agroquímico</option>
                  {agroquimicos.map(agroquimico => (
                    <option key={agroquimico.id} value={agroquimico.id}>
                      {agroquimico.nombreInsumo} - Stock: {agroquimico.stockActual} {agroquimico.unidadMedidaInsumo}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Tipo de Aplicación</label>
                <select
                  name="tipoAplicacion"
                  value={formData.tipoAplicacion}
                  onChange={(e) => handleTipoAplicacionChange(e.target.value)}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  required
                >
                  <option value="FOLIAR">Foliar</option>
                  <option value="TERRESTRE">Terrestre</option>
                  <option value="AEREA">Aérea</option>
                  <option value="PRECISION">Precisión</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Fecha de Aplicación</label>
                <input
                  type="date"
                  name="fechaAplicacion"
                  value={formData.fechaAplicacion}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  required
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Superficie Aplicada (ha)</label>
                <input
                  type="number"
                  step="0.01"
                  name="superficieAplicadaHa"
                  value={formData.superficieAplicadaHa}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Unidad de Medida</label>
                <select
                  name="unidadMedida"
                  value={formData.unidadMedida}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  required
                >
                  <option value="L">Litros (L)</option>
                  <option value="kg">Kilogramos (kg)</option>
                  <option value="g">Gramos (g)</option>
                  <option value="ml">Mililitros (ml)</option>
                </select>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Dosis Recomendada (por ha)</label>
                <input
                  type="number"
                  step="0.01"
                  name="dosisRecomendadaPorHa"
                  value={formData.dosisRecomendadaPorHa}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  readOnly
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Dosis Aplicada (por ha)</label>
                <input
                  type="number"
                  step="0.01"
                  name="dosisAplicadaPorHa"
                  value={formData.dosisAplicadaPorHa}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  required
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Cantidad Total Aplicada</label>
                <input
                  type="number"
                  step="0.01"
                  name="cantidadTotalAplicada"
                  value={formData.cantidadTotalAplicada}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  readOnly
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Desviación</label>
                <div className="mt-1 p-2 border rounded-md bg-gray-50">
                  <span className={`font-medium ${
                    Math.abs(Number(desviacion)) <= 5 ? 'text-green-600' :
                    Math.abs(Number(desviacion)) <= 10 ? 'text-yellow-600' :
                    Math.abs(Number(desviacion)) <= 20 ? 'text-orange-600' : 'text-red-600'
                  }`}>
                    {desviacion}% ({nivelDesviacion})
                  </span>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Condiciones Climáticas</label>
                <input
                  type="text"
                  name="condicionesClimaticas"
                  value={formData.condicionesClimaticas}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Temperatura, humedad, viento, etc."
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Equipo de Aplicación</label>
                <input
                  type="text"
                  name="equipoAplicacion"
                  value={formData.equipoAplicacion}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Pulverizadora, mochila, etc."
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">Operador</label>
              <input
                type="text"
                name="operador"
                value={formData.operador}
                onChange={handleInputChange}
                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                placeholder="Nombre del operador"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">Observaciones</label>
              <textarea
                name="observaciones"
                value={formData.observaciones}
                onChange={handleInputChange}
                rows={3}
                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                placeholder="Observaciones adicionales sobre la aplicación"
              />
            </div>

            <div className="flex justify-end space-x-3 pt-4">
              <button
                type="button"
                onClick={onClose}
                className="bg-gray-300 hover:bg-gray-400 text-gray-800 font-bold py-2 px-4 rounded"
              >
                Cancelar
              </button>
              <button
                type="submit"
                disabled={loading}
                className="bg-purple-500 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
              >
                {loading ? 'Guardando...' : 'Guardar'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default AplicacionModal;
