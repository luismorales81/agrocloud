import React, { useState, useEffect } from 'react';
import { cultivosService, lotesService } from '../services/apiServices';

interface Lote {
  id?: number;
  nombre: string;
  superficie: number | '';
  cultivo: string;
  campo_id: number;
  estado: string;
}

interface Cultivo {
  id: number;
  nombre: string;
}

interface SiembraModalProps {
  lote: Lote;
  onClose: () => void;
  onSuccess: () => void;
}

const SiembraModal: React.FC<SiembraModalProps> = ({ lote, onClose, onSuccess }) => {
  const [cultivos, setCultivos] = useState<Cultivo[]>([]);
  const [formData, setFormData] = useState({
    cultivoId: '',
    fechaSiembra: new Date().toISOString().split('T')[0],
    densidadSiembra: '',
    observaciones: ''
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    cargarCultivos();
  }, []);

  const cargarCultivos = async () => {
    try {
      const data = await cultivosService.listar();
      setCultivos(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Error cargando cultivos:', error);
    }
  };

  const handleSembrar = async () => {
    if (!formData.cultivoId) {
      alert('Por favor seleccione un cultivo');
      return;
    }

    if (!formData.densidadSiembra || parseFloat(formData.densidadSiembra) <= 0) {
      alert('Por favor ingrese una densidad de siembra válida');
      return;
    }

    setLoading(true);

    try {
      const siembraData = {
        cultivoId: parseInt(formData.cultivoId),
        fechaSiembra: formData.fechaSiembra,
        densidadSiembra: parseFloat(formData.densidadSiembra),
        observaciones: formData.observaciones,
        insumos: [],
        maquinaria: [],
        manoObra: []
      };
      
      const data = await lotesService.sembrar(lote.id!, siembraData);
      alert(`✅ ${data.message || 'Lote sembrado exitosamente'}`);
      onSuccess();
      onClose();
    } catch (error) {
      console.error('Error al sembrar:', error);
      alert('❌ Error de conexión. Por favor, intente nuevamente.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0,0,0,0.5)',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      zIndex: 2000
    }}>
      <div style={{
        background: 'white',
        borderRadius: '12px',
        padding: '24px',
        maxWidth: '500px',
        width: '90%',
        maxHeight: '90vh',
        overflowY: 'auto'
      }}>
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '20px',
          borderBottom: '2px solid #4CAF50',
          paddingBottom: '10px'
        }}>
          <h2 style={{ margin: 0, color: '#2e7d32', display: 'flex', alignItems: 'center', gap: '8px' }}>
            🌱 Sembrar Lote
          </h2>
          <button
            onClick={onClose}
            style={{
              background: 'none',
              border: 'none',
              fontSize: '24px',
              cursor: 'pointer',
              color: '#666'
            }}
          >
            ✕
          </button>
        </div>

        <div style={{
          background: '#f0f9ff',
          padding: '12px',
          borderRadius: '8px',
          marginBottom: '20px',
          border: '1px solid #bfdbfe'
        }}>
          <div style={{ fontSize: '14px', color: '#1e40af' }}>
            <strong>Lote:</strong> {lote.nombre}
          </div>
          <div style={{ fontSize: '14px', color: '#1e40af' }}>
            <strong>Superficie:</strong> {lote.superficie} ha
          </div>
          <div style={{ fontSize: '14px', color: '#1e40af' }}>
            <strong>Estado actual:</strong> {lote.estado.replace(/_/g, ' ')}
          </div>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {/* Seleccionar Cultivo */}
          <div>
            <label style={{ display: 'block', marginBottom: '6px', fontWeight: 'bold', color: '#374151' }}>
              Cultivo a Sembrar *
            </label>
            <select
              value={formData.cultivoId}
              onChange={(e) => setFormData({ ...formData, cultivoId: e.target.value })}
              required
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #d1d5db',
                borderRadius: '8px',
                fontSize: '14px'
              }}
            >
              <option value="">Seleccionar cultivo</option>
              {cultivos.map(cultivo => (
                <option key={cultivo.id} value={cultivo.id}>
                  {cultivo.nombre}
                </option>
              ))}
            </select>
          </div>

          {/* Fecha de Siembra */}
          <div>
            <label style={{ display: 'block', marginBottom: '6px', fontWeight: 'bold', color: '#374151' }}>
              Fecha de Siembra *
            </label>
            <input
              type="date"
              value={formData.fechaSiembra}
              onChange={(e) => setFormData({ ...formData, fechaSiembra: e.target.value })}
              required
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #d1d5db',
                borderRadius: '8px',
                fontSize: '14px'
              }}
            />
          </div>

          {/* Densidad de Siembra */}
          <div>
            <label style={{ display: 'block', marginBottom: '6px', fontWeight: 'bold', color: '#374151' }}>
              Densidad de Siembra (plantas/ha) *
            </label>
            <input
              type="number"
              value={formData.densidadSiembra}
              onChange={(e) => setFormData({ ...formData, densidadSiembra: e.target.value })}
              placeholder="Ejemplo: 50000"
              required
              min="0"
              step="100"
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #d1d5db',
                borderRadius: '8px',
                fontSize: '14px'
              }}
            />
            <small style={{ color: '#6b7280', fontSize: '12px' }}>
              Cantidad de plantas por hectárea
            </small>
          </div>

          {/* Observaciones */}
          <div>
            <label style={{ display: 'block', marginBottom: '6px', fontWeight: 'bold', color: '#374151' }}>
              Observaciones
            </label>
            <textarea
              value={formData.observaciones}
              onChange={(e) => setFormData({ ...formData, observaciones: e.target.value })}
              placeholder="Condiciones del suelo, clima, etc."
              rows={3}
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #d1d5db',
                borderRadius: '8px',
                fontSize: '14px',
                resize: 'vertical'
              }}
            />
          </div>

          {/* Botones */}
          <div style={{ display: 'flex', gap: '12px', marginTop: '8px' }}>
            <button
              onClick={onClose}
              disabled={loading}
              style={{
                flex: 1,
                padding: '12px',
                background: '#6b7280',
                color: 'white',
                border: 'none',
                borderRadius: '8px',
                cursor: loading ? 'not-allowed' : 'pointer',
                fontSize: '14px',
                fontWeight: 'bold',
                opacity: loading ? 0.5 : 1
              }}
            >
              Cancelar
            </button>
            <button
              onClick={handleSembrar}
              disabled={loading}
              style={{
                flex: 1,
                padding: '12px',
                background: loading ? '#9ca3af' : '#4CAF50',
                color: 'white',
                border: 'none',
                borderRadius: '8px',
                cursor: loading ? 'not-allowed' : 'pointer',
                fontSize: '14px',
                fontWeight: 'bold'
              }}
            >
              {loading ? '🔄 Sembrando...' : '🌱 Confirmar Siembra'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SiembraModal;
