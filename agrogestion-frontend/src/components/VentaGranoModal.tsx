import React, { useState } from 'react';
import api from '../services/api';
import Button from './ui/Button';
import { useCurrencyContext } from '../contexts/CurrencyContext';

interface InventarioGrano {
  id: number;
  cultivoNombre: string;
  loteNombre: string;
  cantidadDisponible: number;
  unidadMedida: string;
  costoUnitario: number;
}

interface VentaGranoModalProps {
  inventario: InventarioGrano;
  onClose: () => void;
  onSuccess: () => void;
}

const VentaGranoModal: React.FC<VentaGranoModalProps> = ({ inventario, onClose, onSuccess }) => {
  const { formatCurrency } = useCurrencyContext();
  const [loading, setLoading] = useState(false);
  const [errorCantidad, setErrorCantidad] = useState('');
  const [formData, setFormData] = useState({
    cantidadVender: '',
    precioVentaUnitario: '',
    fechaVenta: new Date().toISOString().split('T')[0],
    clienteComprador: '',
    observaciones: ''
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    
    // Validar cantidad máxima para el campo cantidadVender
    if (name === 'cantidadVender') {
      const cantidadIngresada = parseFloat(value);
      if (value !== '' && cantidadIngresada > inventario.cantidadDisponible) {
        // Mostrar error y no permitir el valor
        setErrorCantidad(`⚠️ No puede vender más de ${inventario.cantidadDisponible} ${inventario.unidadMedida}`);
        return; // No actualizar el estado
      } else {
        setErrorCantidad(''); // Limpiar error si el valor es válido
      }
    }
    
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const calcularMontoTotal = () => {
    const cantidad = parseFloat(formData.cantidadVender) || 0;
    const precio = parseFloat(formData.precioVentaUnitario) || 0;
    return cantidad * precio;
  };

  const calcularMargen = () => {
    const montoVenta = calcularMontoTotal();
    const cantidad = parseFloat(formData.cantidadVender) || 0;
    const costoTotal = cantidad * inventario.costoUnitario;
    return montoVenta - costoTotal;
  };

  const calcularRentabilidad = () => {
    const cantidad = parseFloat(formData.cantidadVender) || 0;
    const costoTotal = cantidad * inventario.costoUnitario;
    
    // Si el costo es 0, no se puede calcular rentabilidad
    if (costoTotal === 0) {
      return null; // Retorna null para indicar que no hay costo
    }
    
    const margen = calcularMargen();
    return (margen / costoTotal) * 100;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Validaciones
    const cantidad = parseFloat(formData.cantidadVender);
    const precio = parseFloat(formData.precioVentaUnitario);

    if (!cantidad || cantidad <= 0) {
      alert('⚠️ Ingrese una cantidad válida');
      return;
    }

    if (cantidad > inventario.cantidadDisponible) {
      setErrorCantidad(`⚠️ No puede vender más de ${inventario.cantidadDisponible} ${inventario.unidadMedida}`);
      alert(`⚠️ Cantidad insuficiente. Disponible: ${inventario.cantidadDisponible} ${inventario.unidadMedida}`);
      return;
    }

    if (!precio || precio <= 0) {
      alert('⚠️ Ingrese un precio válido');
      return;
    }

    // Si hay error de cantidad, no permitir envío
    if (errorCantidad) {
      return;
    }

    try {
      setLoading(true);
      
      const requestData = {
        inventarioId: inventario.id,
        cantidadVender: cantidad,
        precioVentaUnitario: precio,
        fechaVenta: formData.fechaVenta,
        clienteComprador: formData.clienteComprador || null,
        observaciones: formData.observaciones || null
      };

      const response = await api.post('/api/v1/inventario-granos/vender', requestData);

      if (response.data.success) {
        alert(`✅ Venta procesada exitosamente\n\n` +
              `Cantidad: ${cantidad} ${inventario.unidadMedida}\n` +
              `Monto Total: ${formatCurrency(calcularMontoTotal())}\n` +
              `Margen: ${formatCurrency(calcularMargen())}\n\n` +
              `Se creó un ingreso en Finanzas automáticamente`);
        onSuccess();
      } else {
        alert('❌ ' + response.data.message);
      }
    } catch (error: any) {
      console.error('Error procesando venta:', error);
      const mensaje = error.response?.data?.message || 'Error al procesar la venta';
      alert('❌ ' + mensaje);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-semibold">💵 Venta de Grano</h2>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700 text-2xl"
          >
            ×
          </button>
        </div>

        {/* Información del Inventario */}
        <div className="bg-blue-50 p-4 rounded-lg mb-6">
          <h3 className="font-semibold text-blue-900 mb-2">📦 Información del Stock</h3>
          <div className="grid grid-cols-2 gap-2 text-sm">
            <p><span className="text-gray-600">Cultivo:</span> <span className="font-medium">{inventario.cultivoNombre}</span></p>
            <p><span className="text-gray-600">Lote:</span> <span className="font-medium">{inventario.loteNombre}</span></p>
            <p><span className="text-gray-600">Disponible:</span> <span className="font-medium text-green-600">{inventario.cantidadDisponible} {inventario.unidadMedida}</span></p>
            <p><span className="text-gray-600">Costo Producción:</span> <span className="font-medium">{formatCurrency(inventario.costoUnitario)}/{inventario.unidadMedida}</span></p>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Cantidad a Vender */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Cantidad a Vender *
            </label>
            <div className="flex gap-2">
              <input
                type="number"
                name="cantidadVender"
                value={formData.cantidadVender}
                onChange={handleChange}
                step="0.01"
                max={inventario.cantidadDisponible}
                placeholder="0.00"
                required
                className={`flex-1 px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 ${
                  errorCantidad 
                    ? 'border-red-500 focus:ring-red-500' 
                    : 'focus:ring-blue-500'
                }`}
              />
              <span className="flex items-center text-gray-600">{inventario.unidadMedida}</span>
            </div>
            {errorCantidad ? (
              <p className="text-xs text-red-600 font-medium mt-1">
                {errorCantidad}
              </p>
            ) : (
              <p className="text-xs text-gray-500 mt-1">
                Máximo: {inventario.cantidadDisponible} {inventario.unidadMedida}
              </p>
            )}
          </div>

          {/* Precio de Venta */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Precio de Venta *
            </label>
            <div className="flex gap-2">
              <input
                type="number"
                name="precioVentaUnitario"
                value={formData.precioVentaUnitario}
                onChange={handleChange}
                step="0.01"
                placeholder="0.00"
                required
                className="flex-1 px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <span className="flex items-center text-gray-600">por {inventario.unidadMedida}</span>
            </div>
          </div>

          {/* Fecha de Venta */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Fecha de Venta *
            </label>
            <input
              type="date"
              name="fechaVenta"
              value={formData.fechaVenta}
              onChange={handleChange}
              required
              className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* Cliente */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Cliente/Comprador
            </label>
            <input
              type="text"
              name="clienteComprador"
              value={formData.clienteComprador}
              onChange={handleChange}
              placeholder="Nombre del cliente o acopio"
              className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* Observaciones */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Observaciones
            </label>
            <textarea
              name="observaciones"
              value={formData.observaciones}
              onChange={handleChange}
              rows={3}
              className="w-full px-3 py-2 border rounded-lg"
              placeholder="Notas adicionales sobre la venta..."
            />
          </div>

          {/* Resumen de la Venta */}
          {formData.cantidadVender && formData.precioVentaUnitario && (
            <div className="bg-green-50 p-4 rounded-lg">
              <h3 className="font-semibold text-green-900 mb-2">💰 Resumen de la Venta</h3>
              <div className="space-y-1 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-600">Cantidad:</span>
                  <span className="font-medium">{parseFloat(formData.cantidadVender).toFixed(2)} {inventario.unidadMedida}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Precio Unitario:</span>
                  <span className="font-medium">{formatCurrency(parseFloat(formData.precioVentaUnitario))}</span>
                </div>
                <div className="flex justify-between pt-2 border-t">
                  <span className="text-gray-600">Monto Total Venta:</span>
                  <span className="font-bold text-green-700">{formatCurrency(calcularMontoTotal())}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Costo Producción:</span>
                  <span className="font-medium text-red-600">
                    {formatCurrency(parseFloat(formData.cantidadVender) * inventario.costoUnitario)}
                  </span>
                </div>
                <div className="flex justify-between pt-2 border-t">
                  <span className="font-semibold">Margen Bruto:</span>
                  <span className={`font-bold ${calcularMargen() >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                    {formatCurrency(calcularMargen())}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="font-semibold">Rentabilidad:</span>
                  <span className={`font-bold ${calcularMargen() >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                    {(() => {
                      const rentabilidad = calcularRentabilidad();
                      if (rentabilidad === null) {
                        return 'Sin costo base';
                      }
                      return `${rentabilidad.toFixed(1)}%`;
                    })()}
                  </span>
                </div>
                {inventario.costoUnitario === 0 && (
                  <div className="mt-2 pt-2 border-t">
                    <p className="text-xs text-amber-700 bg-amber-50 p-2 rounded">
                      ℹ️ El costo de producción es $0. Esto puede ocurrir cuando no se registraron costos en la cosecha o cuando los datos de producción aún no están completos.
                    </p>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Botones */}
          <div className="flex gap-2 pt-4">
            <Button
              type="button"
              onClick={onClose}
              className="flex-1 bg-gray-500 hover:bg-gray-600"
            >
              Cancelar
            </Button>
            <Button
              type="submit"
              disabled={loading || !!errorCantidad}
              className="flex-1 bg-green-600 hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? '⏳ Procesando...' : '💵 Confirmar Venta'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default VentaGranoModal;

