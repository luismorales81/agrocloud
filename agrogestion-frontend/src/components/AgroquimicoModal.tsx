import React, { useState, useEffect } from 'react';
import { insumosService } from '../services/apiServices';

interface Agroquimico {
  id?: number;
  insumoId?: number;
  principioActivo: string;
  concentracion: string;
  claseQuimica: string;
  modoAccion: string;
  periodoCarenciaDias: number;
  dosisMinimaPorHa: number;
  dosisMaximaPorHa: number;
  unidadDosis: string;
  observaciones: string;
}

interface Insumo {
  id: number;
  nombre: string;
  descripcion: string;
  unidadMedida: string;
  stockActual: number;
  stockMinimo: number;
  precioUnitario: number;
}

interface DosisTipoAplicacion {
  tipoAplicacion: string;
  dosisRecomendada: number;
  dosisMinima: number;
  dosisMaxima: number;
  unidadMedida: string;
  descripcion: string;
  condicionesAplicacion: string;
}

interface AgroquimicoModalProps {
  agroquimico: Agroquimico | null;
  onClose: () => void;
  onSave: (data: any) => void;
}

const AgroquimicoModal: React.FC<AgroquimicoModalProps> = ({ agroquimico, onClose, onSave }) => {
  const [formData, setFormData] = useState<Agroquimico>({
    principioActivo: '',
    concentracion: '',
    claseQuimica: '',
    modoAccion: '',
    periodoCarenciaDias: 0,
    dosisMinimaPorHa: 0,
    dosisMaximaPorHa: 0,
    unidadDosis: '',
    observaciones: ''
  });

  const [insumos, setInsumos] = useState<Insumo[]>([]);
  const [dosisPorTipo, setDosisPorTipo] = useState<DosisTipoAplicacion[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    cargarInsumos();
    if (agroquimico) {
      setFormData(agroquimico);
    }
  }, [agroquimico]);

  const cargarInsumos = async () => {
    try {
      const data = await insumosService.listar();
      setInsumos(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Error al cargar insumos:', error);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name.includes('Dias') || name.includes('PorHa') ? Number(value) : value
    }));
  };

  const handleDosisChange = (index: number, field: string, value: string | number) => {
    const newDosis = [...dosisPorTipo];
    newDosis[index] = {
      ...newDosis[index],
      [field]: value
    };
    setDosisPorTipo(newDosis);
  };

  const agregarDosis = () => {
    setDosisPorTipo([...dosisPorTipo, {
      tipoAplicacion: 'FOLIAR',
      dosisRecomendada: 0,
      dosisMinima: 0,
      dosisMaxima: 0,
      unidadMedida: 'L',
      descripcion: '',
      condicionesAplicacion: ''
    }]);
  };

  const eliminarDosis = (index: number) => {
    const newDosis = dosisPorTipo.filter((_, i) => i !== index);
    setDosisPorTipo(newDosis);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const data = {
        ...formData,
        dosisPorTipoAplicacion: dosisPorTipo
      };
      await onSave(data);
    } catch (error) {
      console.error('Error al guardar:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
      <div className="relative top-20 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-1/2 shadow-lg rounded-md bg-white">
        <div className="mt-3">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-medium text-gray-900">
              {agroquimico ? 'Editar Agroquímico' : 'Nuevo Agroquímico'}
            </h3>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600"
            >
              ✕
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Información del insumo */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Insumo</label>
                <select
                  name="insumoId"
                  value={formData.insumoId || ''}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  required
                >
                  <option value="">Seleccionar insumo</option>
                  {insumos.map(insumo => (
                    <option key={insumo.id} value={insumo.id}>
                      {insumo.nombre} - Stock: {insumo.stockActual} {insumo.unidadMedida}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Información del agroquímico */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Principio Activo</label>
                <input
                  type="text"
                  name="principioActivo"
                  value={formData.principioActivo}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Concentración</label>
                <input
                  type="text"
                  name="concentracion"
                  value={formData.concentracion}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  required
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Clase Química</label>
                <input
                  type="text"
                  name="claseQuimica"
                  value={formData.claseQuimica}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Modo de Acción</label>
                <input
                  type="text"
                  name="modoAccion"
                  value={formData.modoAccion}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Período de Carencia (días)</label>
                <input
                  type="number"
                  name="periodoCarenciaDias"
                  value={formData.periodoCarenciaDias}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Dosis Mínima (por ha)</label>
                <input
                  type="number"
                  step="0.01"
                  name="dosisMinimaPorHa"
                  value={formData.dosisMinimaPorHa}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Dosis Máxima (por ha)</label>
                <input
                  type="number"
                  step="0.01"
                  name="dosisMaximaPorHa"
                  value={formData.dosisMaximaPorHa}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">Unidad de Dosis</label>
              <select
                name="unidadDosis"
                value={formData.unidadDosis}
                onChange={handleInputChange}
                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="L">Litros (L)</option>
                <option value="kg">Kilogramos (kg)</option>
                <option value="g">Gramos (g)</option>
                <option value="ml">Mililitros (ml)</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">Observaciones</label>
              <textarea
                name="observaciones"
                value={formData.observaciones}
                onChange={handleInputChange}
                rows={3}
                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
              />
            </div>

            {/* Dosis por tipo de aplicación */}
            <div>
              <div className="flex justify-between items-center mb-2">
                <label className="block text-sm font-medium text-gray-700">Dosis por Tipo de Aplicación</label>
                <button
                  type="button"
                  onClick={agregarDosis}
                  className="bg-green-500 hover:bg-green-700 text-white text-sm font-bold py-1 px-3 rounded"
                >
                  + Agregar Dosis
                </button>
              </div>
              
              {dosisPorTipo.map((dosis, index) => (
                <div key={index} className="border rounded-md p-4 mb-4 bg-gray-50">
                  <div className="flex justify-between items-center mb-2">
                    <h4 className="font-medium">Dosis {index + 1}</h4>
                    <button
                      type="button"
                      onClick={() => eliminarDosis(index)}
                      className="text-red-600 hover:text-red-800"
                    >
                      Eliminar
                    </button>
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700">Tipo de Aplicación</label>
                      <select
                        value={dosis.tipoAplicacion}
                        onChange={(e) => handleDosisChange(index, 'tipoAplicacion', e.target.value)}
                        className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                      >
                        <option value="FOLIAR">Foliar</option>
                        <option value="TERRESTRE">Terrestre</option>
                        <option value="AEREA">Aérea</option>
                        <option value="PRECISION">Precisión</option>
                      </select>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700">Unidad de Medida</label>
                      <select
                        value={dosis.unidadMedida}
                        onChange={(e) => handleDosisChange(index, 'unidadMedida', e.target.value)}
                        className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                      >
                        <option value="L">Litros (L)</option>
                        <option value="kg">Kilogramos (kg)</option>
                        <option value="g">Gramos (g)</option>
                        <option value="ml">Mililitros (ml)</option>
                      </select>
                    </div>
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mt-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700">Dosis Recomendada</label>
                      <input
                        type="number"
                        step="0.01"
                        value={dosis.dosisRecomendada}
                        onChange={(e) => handleDosisChange(index, 'dosisRecomendada', Number(e.target.value))}
                        className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700">Dosis Mínima</label>
                      <input
                        type="number"
                        step="0.01"
                        value={dosis.dosisMinima}
                        onChange={(e) => handleDosisChange(index, 'dosisMinima', Number(e.target.value))}
                        className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700">Dosis Máxima</label>
                      <input
                        type="number"
                        step="0.01"
                        value={dosis.dosisMaxima}
                        onChange={(e) => handleDosisChange(index, 'dosisMaxima', Number(e.target.value))}
                        className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                      />
                    </div>
                  </div>
                  
                  <div className="mt-4">
                    <label className="block text-sm font-medium text-gray-700">Descripción</label>
                    <input
                      type="text"
                      value={dosis.descripcion}
                      onChange={(e) => handleDosisChange(index, 'descripcion', e.target.value)}
                      className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                    />
                  </div>
                  
                  <div className="mt-4">
                    <label className="block text-sm font-medium text-gray-700">Condiciones de Aplicación</label>
                    <textarea
                      value={dosis.condicionesAplicacion}
                      onChange={(e) => handleDosisChange(index, 'condicionesAplicacion', e.target.value)}
                      rows={2}
                      className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                    />
                  </div>
                </div>
              ))}
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
                className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
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

export default AgroquimicoModal;






