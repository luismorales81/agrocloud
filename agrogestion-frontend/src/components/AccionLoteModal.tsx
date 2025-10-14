import React, { useState } from 'react';
import api from '../services/api';

interface Lote {
  id?: number;
  nombre: string;
  superficie: number | '';
  cultivo: string;
  estado: string;
}

type TipoAccion = 'abandonar' | 'limpiar' | 'forraje';

interface AccionLoteModalProps {
  lote: Lote;
  accion: TipoAccion;
  onClose: () => void;
  onSuccess: () => void;
}

const AccionLoteModal: React.FC<AccionLoteModalProps> = ({ lote, accion, onClose, onSuccess }) => {
  const [formData, setFormData] = useState({
    motivo: '',
    cantidadCosechada: '',
    unidadMedida: 'ton',
    observaciones: ''
  });
  const [loading, setLoading] = useState(false);

  const getConfiguracion = () => {
    switch (accion) {
      case 'abandonar':
        return {
          titulo: '⚠️ Abandonar Cultivo',
          color: '#F44336',
          colorFondo: '#ffebee',
          colorBorde: '#ef5350',
          descripcion: 'Esta acción marcará el cultivo como abandonado debido a problemas (plagas, sequía, etc.)',
          requiereCantidad: false,
          mensajeConfirmacion: '¿Está seguro de abandonar este cultivo?'
        };
      case 'limpiar':
        return {
          titulo: '🚜 Limpiar Cultivo',
          color: '#607D8B',
          colorFondo: '#f5f5f5',
          colorBorde: '#90a4ae',
          descripcion: 'Esta acción eliminará el cultivo y dejará el lote disponible para nueva siembra',
          requiereCantidad: false,
          mensajeConfirmacion: 'El cultivo será eliminado completamente. ¿Continuar?'
        };
      case 'forraje':
        return {
          titulo: '🐄 Convertir a Forraje',
          color: '#795548',
          colorFondo: '#efebe9',
          colorBorde: '#a1887f',
          descripcion: 'Cosecha anticipada del cultivo para uso como forraje en alimentación animal',
          requiereCantidad: false,
          mensajeConfirmacion: 'Se registrará como cosecha de forraje. ¿Continuar?'
        };
    }
  };

  const config = getConfiguracion();

  const handleConfirmar = async () => {
    if (!formData.motivo.trim()) {
      alert('Por favor ingrese el motivo de esta acción');
      return;
    }

    if (config.requiereCantidad && (!formData.cantidadCosechada || parseFloat(formData.cantidadCosechada) <= 0)) {
      alert('Por favor ingrese una cantidad válida');
      return;
    }

    setLoading(true);

    try {
      let endpoint = '';
      let body: any = {};

      switch (accion) {
        case 'abandonar':
          endpoint = `/api/v1/lotes/${lote.id}/abandonar`;
          body = { motivo: formData.motivo };
          break;
        
        case 'limpiar':
          endpoint = `/api/v1/lotes/${lote.id}/limpiar`;
          body = { motivo: formData.motivo };
          break;
        
        case 'forraje':
          endpoint = `/api/v1/lotes/${lote.id}/convertir-forraje`;
          body = {
            fechaCosecha: new Date().toISOString().split('T')[0],
            cantidadCosechada: parseFloat(formData.cantidadCosechada),
            unidadMedida: formData.unidadMedida,
            calidadCosecha: 'forraje',
            observaciones: formData.motivo + (formData.observaciones ? ' | ' + formData.observaciones : ''),
            maquinaria: [],
            manoObra: []
          };
          break;
      }

      const response = await api.post(endpoint, body);

      if (response.status >= 200 && response.status < 300) {
        const data = response.data;
        alert(`✅ ${data.message || 'Acción completada exitosamente'}`);
        onSuccess();
        onClose();
      } else {
        alert(`❌ Error: ${response.data?.message || 'No se pudo completar la acción'}`);
      }
    } catch (error) {
      console.error('Error al ejecutar acción:', error);
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
          borderBottom: `2px solid ${config.color}`,
          paddingBottom: '10px'
        }}>
          <h2 style={{ margin: 0, color: config.color, display: 'flex', alignItems: 'center', gap: '8px' }}>
            {config.titulo}
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

        {/* Info del lote */}
        <div style={{
          background: config.colorFondo,
          padding: '12px',
          borderRadius: '8px',
          marginBottom: '20px',
          border: `1px solid ${config.colorBorde}`
        }}>
          <div style={{ fontSize: '14px', color: config.color }}>
            <strong>Lote:</strong> {lote.nombre}
          </div>
          <div style={{ fontSize: '14px', color: config.color }}>
            <strong>Cultivo actual:</strong> {lote.cultivo || 'Sin cultivo'}
          </div>
          <div style={{ fontSize: '14px', color: config.color }}>
            <strong>Estado:</strong> {lote.estado.replace(/_/g, ' ')}
          </div>
        </div>

        {/* Advertencia */}
        <div style={{
          background: '#fff3e0',
          padding: '12px',
          borderRadius: '8px',
          marginBottom: '20px',
          border: '1px solid #FFB74D'
        }}>
          <div style={{ fontSize: '13px', color: '#e65100' }}>
            <strong>⚠️ Advertencia:</strong> {config.descripcion}
          </div>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {/* Motivo */}
          <div>
            <label style={{ display: 'block', marginBottom: '6px', fontWeight: 'bold', color: '#374151' }}>
              Motivo *
            </label>
            <textarea
              value={formData.motivo}
              onChange={(e) => setFormData({ ...formData, motivo: e.target.value })}
              placeholder={
                accion === 'abandonar' ? 'Ej: Plaga de langostas, pérdida total' :
                accion === 'limpiar' ? 'Ej: Cambio de plan de siembra, preparación para otro cultivo' :
                'Ej: Cultivo inmaduro, necesidad de forraje'
              }
              rows={3}
              required
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

          {/* Cantidad (solo para forraje) */}
          {config.requiereCantidad && (
            <>
              <div>
                <label style={{ display: 'block', marginBottom: '6px', fontWeight: 'bold', color: '#374151' }}>
                  Cantidad de Forraje *
                </label>
                <div style={{ display: 'flex', gap: '8px' }}>
                  <input
                    type="number"
                    value={formData.cantidadCosechada}
                    onChange={(e) => setFormData({ ...formData, cantidadCosechada: e.target.value })}
                    placeholder="Cantidad"
                    required
                    min="0"
                    step="0.1"
                    style={{
                      flex: 2,
                      padding: '10px',
                      border: '1px solid #d1d5db',
                      borderRadius: '8px',
                      fontSize: '14px'
                    }}
                  />
                  <select
                    value={formData.unidadMedida}
                    onChange={(e) => setFormData({ ...formData, unidadMedida: e.target.value })}
                    style={{
                      flex: 1,
                      padding: '10px',
                      border: '1px solid #d1d5db',
                      borderRadius: '8px',
                      fontSize: '14px'
                    }}
                  >
                    <option value="ton">Toneladas</option>
                    <option value="kg">Kilogramos</option>
                  </select>
                </div>
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '6px', fontWeight: 'bold', color: '#374151' }}>
                  Observaciones Adicionales
                </label>
                <textarea
                  value={formData.observaciones}
                  onChange={(e) => setFormData({ ...formData, observaciones: e.target.value })}
                  placeholder="Destino del forraje, condiciones, etc."
                  rows={2}
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
            </>
          )}

          {/* Confirmación */}
          <div style={{
            background: '#f0f9ff',
            padding: '10px',
            borderRadius: '6px',
            border: '1px solid #bfdbfe',
            fontSize: '13px',
            color: '#1e40af'
          }}>
            <strong>⚡ {config.mensajeConfirmacion}</strong>
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
              onClick={handleConfirmar}
              disabled={loading}
              style={{
                flex: 1,
                padding: '12px',
                background: loading ? '#9ca3af' : config.color,
                color: 'white',
                border: 'none',
                borderRadius: '8px',
                cursor: loading ? 'not-allowed' : 'pointer',
                fontSize: '14px',
                fontWeight: 'bold'
              }}
            >
              {loading ? '🔄 Procesando...' : '✓ Confirmar'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AccionLoteModal;
