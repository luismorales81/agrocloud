import React, { useState, useEffect } from 'react';

interface Lote {
  id?: number;
  nombre: string;
  superficie: number | '';
  cultivo: string;
  estado: string;
}

interface InfoCosecha {
  loteId: number;
  nombreLote: string;
  superficie: number;
  cultivoActual: string;
  fechaSiembra: string;
  rendimientoEsperadoLote: number | null;
  cultivoId: number | null;
  variedadSemilla: string;
  rendimientoEsperadoCultivo: number | null;
  unidadRendimiento: string;
  cicloDias: number | null;
  precioPorTonelada: number | null;
}

interface CosechaModalProps {
  lote: Lote;
  onClose: () => void;
  onSuccess: () => void;
}

const CosechaModal: React.FC<CosechaModalProps> = ({ lote, onClose, onSuccess }) => {
  const [formData, setFormData] = useState({
    fechaCosecha: new Date().toISOString().split('T')[0],
    cantidadCosechada: '',
    unidadMedida: 'ton',
    variedadSemilla: '',
    estadoSuelo: 'BUENO',
    requiereDescanso: false,
    diasDescansoRecomendados: '0',
    precioVenta: '',
    observaciones: ''
  });
  const [loading, setLoading] = useState(false);
  const [infoCosecha, setInfoCosecha] = useState<InfoCosecha | null>(null);
  const [loadingInfo, setLoadingInfo] = useState(true);

  // Cargar información del cultivo automáticamente
  useEffect(() => {
    const cargarInfoCosecha = async () => {
      if (!lote.id) return;
      
      try {
        setLoadingInfo(true);
        const token = localStorage.getItem('token');
        const response = await fetch(`http://localhost:8080/api/v1/lotes/${lote.id}/info-cosecha`, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });

        if (response.ok) {
          const info = await response.json();
          setInfoCosecha(info);
          
          // Auto-completar campos con datos del cultivo
          setFormData(prev => ({
            ...prev,
            variedadSemilla: info.variedadSemilla || '',
            unidadMedida: info.unidadRendimiento || 'ton',
            precioVenta: info.precioPorTonelada ? info.precioPorTonelada.toString() : ''
          }));
        } else {
          console.error('Error al cargar información de cosecha');
        }
      } catch (error) {
        console.error('Error al cargar información de cosecha:', error);
      } finally {
        setLoadingInfo(false);
      }
    };

    cargarInfoCosecha();
  }, [lote.id]);

  const handleCosechar = async () => {
    if (!formData.cantidadCosechada || parseFloat(formData.cantidadCosechada) <= 0) {
      alert('Por favor ingrese una cantidad cosechada válida');
      return;
    }

    setLoading(true);

    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:8080/api/v1/lotes/${lote.id}/cosechar`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          fechaCosecha: formData.fechaCosecha,
          cantidadCosechada: parseFloat(formData.cantidadCosechada),
          unidadMedida: formData.unidadMedida,
          variedadSemilla: formData.variedadSemilla || null,
          estadoSuelo: formData.estadoSuelo || 'BUENO',
          requiereDescanso: formData.requiereDescanso || false,
          diasDescansoRecomendados: formData.requiereDescanso ? parseInt(formData.diasDescansoRecomendados) : 0,
          precioVenta: formData.precioVenta ? parseFloat(formData.precioVenta) : null,
          observaciones: formData.observaciones,
          maquinaria: [],
          manoObra: []
        })
      });

      if (response.ok) {
        const data = await response.json();
        
        // Calcular rendimiento si es posible
        let rendimiento = '';
        if (lote.superficie && typeof lote.superficie === 'number') {
          const rendimientoPorHa = parseFloat(formData.cantidadCosechada) / lote.superficie;
          rendimiento = `\n📊 Rendimiento: ${rendimientoPorHa.toFixed(2)} ${formData.unidadMedida}/ha`;
        }
        
        alert(`✅ ${data.message || 'Lote cosechado exitosamente'}${rendimiento}`);
        onSuccess();
        onClose();
      } else {
        const errorData = await response.json();
        alert(`❌ Error: ${errorData.message || 'No se pudo cosechar el lote'}`);
      }
    } catch (error) {
      console.error('Error al cosechar:', error);
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
          borderBottom: '2px solid #FF9800',
          paddingBottom: '10px'
        }}>
          <h2 style={{ margin: 0, color: '#e65100', display: 'flex', alignItems: 'center', gap: '8px' }}>
            🌾 Cosechar Lote
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
          background: '#fff3e0',
          padding: '12px',
          borderRadius: '8px',
          marginBottom: '20px',
          border: '1px solid #FFB74D'
        }}>
          <div style={{ fontSize: '14px', color: '#e65100' }}>
            <strong>Lote:</strong> {lote.nombre}
          </div>
          <div style={{ fontSize: '14px', color: '#e65100' }}>
            <strong>Superficie:</strong> {lote.superficie} ha
          </div>
          <div style={{ fontSize: '14px', color: '#e65100' }}>
            <strong>Cultivo:</strong> {lote.cultivo}
          </div>
          <div style={{ fontSize: '14px', color: '#e65100' }}>
            <strong>Estado actual:</strong> {lote.estado.replace(/_/g, ' ')}
          </div>
        </div>

        {/* Información del Cultivo */}
        {loadingInfo ? (
          <div style={{
            background: '#f3f4f6',
            padding: '12px',
            borderRadius: '8px',
            marginBottom: '20px',
            textAlign: 'center'
          }}>
            <div style={{ fontSize: '14px', color: '#6b7280' }}>
              🔄 Cargando información del cultivo...
            </div>
          </div>
        ) : infoCosecha ? (
          <div style={{
            background: '#e8f5e8',
            padding: '12px',
            borderRadius: '8px',
            marginBottom: '20px',
            border: '1px solid #4caf50'
          }}>
            <div style={{ fontSize: '14px', color: '#2e7d32', marginBottom: '8px' }}>
              <strong>📋 Información del Cultivo</strong>
            </div>
            <div style={{ fontSize: '13px', color: '#2e7d32' }}>
              <strong>Variedad:</strong> {infoCosecha.variedadSemilla || 'No especificada'}
            </div>
            {infoCosecha.fechaSiembra && (
              <div style={{ fontSize: '13px', color: '#2e7d32' }}>
                <strong>Fecha de Siembra:</strong> {new Date(infoCosecha.fechaSiembra).toLocaleDateString()}
              </div>
            )}
            {infoCosecha.rendimientoEsperadoCultivo && (
              <div style={{ fontSize: '13px', color: '#2e7d32' }}>
                <strong>Rendimiento Esperado:</strong> {infoCosecha.rendimientoEsperadoCultivo} {infoCosecha.unidadRendimiento}/ha
              </div>
            )}
            {infoCosecha.cicloDias && (
              <div style={{ fontSize: '13px', color: '#2e7d32' }}>
                <strong>Ciclo:</strong> {infoCosecha.cicloDias} días
              </div>
            )}
          </div>
        ) : null}

        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {/* Fecha de Cosecha */}
          <div>
            <label style={{ display: 'block', marginBottom: '6px', fontWeight: 'bold', color: '#374151' }}>
              Fecha de Cosecha *
            </label>
            <input
              type="date"
              value={formData.fechaCosecha}
              onChange={(e) => setFormData({ ...formData, fechaCosecha: e.target.value })}
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

          {/* Cantidad Cosechada */}
          <div>
            <label style={{ display: 'block', marginBottom: '6px', fontWeight: 'bold', color: '#374151' }}>
              Cantidad Cosechada *
            </label>
            <div style={{ display: 'flex', gap: '8px' }}>
              <input
                type="number"
                value={formData.cantidadCosechada}
                onChange={(e) => setFormData({ ...formData, cantidadCosechada: e.target.value })}
                placeholder="Ejemplo: 100"
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
                <option value="qq">Quintales</option>
              </select>
            </div>
            {lote.superficie && typeof lote.superficie === 'number' && formData.cantidadCosechada && (
              <small style={{ color: '#6b7280', fontSize: '12px', display: 'block', marginTop: '4px' }}>
                Rendimiento estimado: {(parseFloat(formData.cantidadCosechada) / lote.superficie).toFixed(2)} {formData.unidadMedida}/ha
              </small>
            )}
            
            {/* Comparación con cantidad esperada */}
            {infoCosecha && infoCosecha.rendimientoEsperadoCultivo && lote.superficie && formData.cantidadCosechada && (
              <div style={{
                background: '#fff3cd',
                padding: '8px',
                borderRadius: '6px',
                marginTop: '8px',
                border: '1px solid #ffeaa7'
              }}>
                <div style={{ fontSize: '12px', color: '#856404', marginBottom: '4px' }}>
                  <strong>📊 Comparación con Rendimiento Esperado</strong>
                </div>
                {(() => {
                  const cantidadEsperada = infoCosecha.rendimientoEsperadoCultivo * lote.superficie;
                  const cantidadCosechada = parseFloat(formData.cantidadCosechada);
                  const diferencia = cantidadCosechada - cantidadEsperada;
                  const porcentaje = cantidadEsperada > 0 ? ((cantidadCosechada / cantidadEsperada) * 100) : 0;
                  
                  return (
                    <div style={{ fontSize: '11px', color: '#856404' }}>
                      <div>Esperado: {cantidadEsperada.toFixed(1)} {infoCosecha.unidadRendimiento}</div>
                      <div>Cosechado: {cantidadCosechada.toFixed(1)} {formData.unidadMedida}</div>
                      <div style={{ 
                        fontWeight: 'bold',
                        color: diferencia >= 0 ? '#28a745' : '#dc3545'
                      }}>
                        Diferencia: {diferencia >= 0 ? '+' : ''}{diferencia.toFixed(1)} {formData.unidadMedida} 
                        ({porcentaje.toFixed(1)}%)
                      </div>
                    </div>
                  );
                })()}
              </div>
            )}
          </div>

          {/* Variedad de Semilla */}
          <div>
            <label style={{ display: 'block', marginBottom: '6px', fontWeight: 'bold', color: '#374151' }}>
              Variedad de Semilla {infoCosecha && infoCosecha.variedadSemilla ? '(del cultivo)' : ''}
            </label>
            <input
              type="text"
              value={formData.variedadSemilla}
              onChange={(e) => setFormData({ ...formData, variedadSemilla: e.target.value })}
              placeholder={infoCosecha ? "Variedad cargada automáticamente" : "Ej: DK692, Pioneer 3000, etc."}
              readOnly={infoCosecha && infoCosecha.variedadSemilla ? true : false}
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #d1d5db',
                borderRadius: '8px',
                fontSize: '14px',
                backgroundColor: infoCosecha && infoCosecha.variedadSemilla ? '#f9f9f9' : 'white',
                cursor: infoCosecha && infoCosecha.variedadSemilla ? 'not-allowed' : 'text'
              }}
            />
            {infoCosecha && infoCosecha.variedadSemilla && (
              <small style={{ color: '#6b7280', fontSize: '12px', display: 'block', marginTop: '4px' }}>
                ✅ Variedad obtenida automáticamente del cultivo sembrado
              </small>
            )}
          </div>

          {/* Estado del Suelo */}
          <div>
            <label style={{ display: 'block', marginBottom: '6px', fontWeight: 'bold', color: '#374151' }}>
              Estado del Suelo *
            </label>
            <select
              value={formData.estadoSuelo}
              onChange={(e) => setFormData({ ...formData, estadoSuelo: e.target.value })}
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #d1d5db',
                borderRadius: '8px',
                fontSize: '14px'
              }}
            >
              <option value="BUENO">✅ Bueno - Listo para próxima siembra</option>
              <option value="DESCANSANDO">⏸️ Descansando - Requiere período de recuperación</option>
              <option value="AGOTADO">⚠️ Agotado - Necesita intervención</option>
            </select>
          </div>

          {/* Requiere Descanso */}
          <div style={{ 
            background: '#f0fdf4', 
            padding: '12px', 
            borderRadius: '8px', 
            border: '1px solid #bbf7d0' 
          }}>
            <label style={{ 
              display: 'flex', 
              alignItems: 'center', 
              cursor: 'pointer',
              fontSize: '14px',
              fontWeight: 'bold',
              color: '#166534'
            }}>
              <input
                type="checkbox"
                checked={formData.requiereDescanso}
                onChange={(e) => setFormData({ 
                  ...formData, 
                  requiereDescanso: e.target.checked,
                  diasDescansoRecomendados: e.target.checked ? formData.diasDescansoRecomendados : '0'
                })}
                style={{ marginRight: '8px', width: '18px', height: '18px', cursor: 'pointer' }}
              />
              🌱 ¿Requiere período de descanso?
            </label>
            
            {formData.requiereDescanso && (
              <div style={{ marginTop: '12px' }}>
                <label style={{ display: 'block', marginBottom: '6px', fontWeight: 'bold', color: '#374151', fontSize: '13px' }}>
                  Días de descanso recomendados *
                </label>
                <input
                  type="number"
                  value={formData.diasDescansoRecomendados}
                  onChange={(e) => setFormData({ ...formData, diasDescansoRecomendados: e.target.value })}
                  placeholder="Ej: 30, 60, 90"
                  min="0"
                  required={formData.requiereDescanso}
                  style={{
                    width: '100%',
                    padding: '10px',
                    border: '1px solid #bbf7d0',
                    borderRadius: '8px',
                    fontSize: '14px'
                  }}
                />
                <small style={{ color: '#6b7280', fontSize: '12px', display: 'block', marginTop: '4px' }}>
                  💡 Recomendado: 30-60 días para recuperación del suelo
                </small>
              </div>
            )}
          </div>

          {/* Precio de Venta (Opcional) */}
          <div>
            <label style={{ display: 'block', marginBottom: '6px', fontWeight: 'bold', color: '#374151' }}>
              Precio de Venta (opcional)
            </label>
            <input
              type="number"
              value={formData.precioVenta}
              onChange={(e) => setFormData({ ...formData, precioVenta: e.target.value })}
              placeholder="Precio por unidad"
              min="0"
              step="0.01"
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #d1d5db',
                borderRadius: '8px',
                fontSize: '14px'
              }}
            />
            <small style={{ color: '#6b7280', fontSize: '12px' }}>
              Precio por {formData.unidadMedida}
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
              placeholder="Condiciones de la cosecha, problemas encontrados, etc."
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
              onClick={handleCosechar}
              disabled={loading}
              style={{
                flex: 1,
                padding: '12px',
                background: loading ? '#9ca3af' : '#FF9800',
                color: 'white',
                border: 'none',
                borderRadius: '8px',
                cursor: loading ? 'not-allowed' : 'pointer',
                fontSize: '14px',
                fontWeight: 'bold'
              }}
            >
              {loading ? '🔄 Cosechando...' : '🌾 Confirmar Cosecha'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CosechaModal;
