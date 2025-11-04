import React, { useState, useEffect } from 'react';

interface DosisAgroquimico {
  id?: number;
  agroquimicoId: number;
  tipoAplicacion: string;
  dosisRecomendada: number;
  dosisMinima: number;
  dosisMaxima: number;
  unidadMedida: string;
  descripcion: string;
  condicionesAplicacion: string;
}

interface Agroquimico {
  id: number;
  nombreInsumo: string;
  principioActivo: string;
  concentracion: string;
}

interface DosisModalProps {
  dosis: DosisAgroquimico | null;
  agroquimicos: Agroquimico[];
  onClose: () => void;
  onSave: (data: any) => void;
}

const DosisModal: React.FC<DosisModalProps> = ({ dosis, agroquimicos, onClose, onSave }) => {
  const [formData, setFormData] = useState<DosisAgroquimico>({
    agroquimicoId: 0,
    tipoAplicacion: 'FOLIAR',
    dosisRecomendada: 0,
    dosisMinima: 0,
    dosisMaxima: 0,
    unidadMedida: 'L',
    descripcion: '',
    condicionesAplicacion: ''
  });

  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (dosis) {
      setFormData(dosis);
    }
  }, [dosis]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name.includes('dosis') || name.includes('Id') ? Number(value) : value
    }));
  };

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

  return (
    <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
      <div className="relative top-20 mx-auto p-5 border w-11/12 md:w-2/3 lg:w-1/2 shadow-lg rounded-md bg-white">
        <div className="mt-3">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-medium text-gray-900">
              {dosis ? 'Editar Dosis' : 'Nueva Dosis'}
            </h3>
            <button
              onClick={onClose}
              className="text-gray-400 hover:text-gray-600"
            >
              ✕
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Agroquímico</label>
              <select
                name="agroquimicoId"
                value={formData.agroquimicoId}
                onChange={handleInputChange}
                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                required
              >
                <option value={0}>Seleccionar agroquímico</option>
                {agroquimicos.map(agroquimico => (
                  <option key={agroquimico.id} value={agroquimico.id}>
                    {agroquimico.nombreInsumo} - {agroquimico.principioActivo} {agroquimico.concentracion}
                  </option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Tipo de Aplicación</label>
                <select
                  name="tipoAplicacion"
                  value={formData.tipoAplicacion}
                  onChange={handleInputChange}
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

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Dosis Recomendada</label>
                <input
                  type="number"
                  step="0.01"
                  name="dosisRecomendada"
                  value={formData.dosisRecomendada}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Dosis Mínima</label>
                <input
                  type="number"
                  step="0.01"
                  name="dosisMinima"
                  value={formData.dosisMinima}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Dosis Máxima</label>
                <input
                  type="number"
                  step="0.01"
                  name="dosisMaxima"
                  value={formData.dosisMaxima}
                  onChange={handleInputChange}
                  className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">Descripción</label>
              <input
                type="text"
                name="descripcion"
                value={formData.descripcion}
                onChange={handleInputChange}
                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                placeholder="Descripción de la dosis y su uso"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">Condiciones de Aplicación</label>
              <textarea
                name="condicionesAplicacion"
                value={formData.condicionesAplicacion}
                onChange={handleInputChange}
                rows={3}
                className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                placeholder="Condiciones climáticas, momento de aplicación, etc."
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
                className="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
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

export default DosisModal;









