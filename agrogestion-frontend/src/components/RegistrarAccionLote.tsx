import React, { useState } from 'react';

interface RegistrarAccionLoteProps {
  loteId: number;
  loteNombre: string;
  onClose: () => void;
  onActionRegistered: () => void;
}

const RegistrarAccionLote: React.FC<RegistrarAccionLoteProps> = ({ 
  loteId, 
  loteNombre, 
  onClose, 
  onActionRegistered 
}) => {
  const [formData, setFormData] = useState({
    tipo_accion: '',
    descripcion: '',
    datos_adicionales: '',
    responsable_nombre: ''
  });
  const [loading, setLoading] = useState(false);

  // Tipos de acciones disponibles
  const tiposAccion = [
    { value: 'siembra', label: '🌱 Siembra', color: '#059669' },
    { value: 'fertilizacion', label: '🌿 Fertilización', color: '#7c3aed' },
    { value: 'riego', label: '💧 Riego', color: '#0ea5e9' },
    { value: 'pulverizacion', label: '🧪 Pulverización', color: '#f97316' },
    { value: 'cosecha', label: '🌾 Cosecha', color: '#eab308' },
    { value: 'monitoreo', label: '👁️ Monitoreo', color: '#06b6d4' },
    { value: 'modificacion', label: '✏️ Modificación', color: '#3b82f6' },
    { value: 'redimensionado', label: '📏 Redimensionado', color: '#f59e0b' },
    { value: 'fusion', label: '🔗 Fusión', color: '#8b5cf6' },
    { value: 'division', label: '✂️ División', color: '#ef4444' },
    { value: 'cambio_cultivo', label: '🔄 Cambio de Cultivo', color: '#84cc16' },
    { value: 'otro', label: '📝 Otro', color: '#64748b' }
  ];

  // Ejemplos de descripciones por tipo de acción
  const ejemplosDescripcion = {
    siembra: 'Siembra de [cultivo] [variedad] con densidad de [X] plantas/ha',
    fertilizacion: 'Aplicación de [fertilizante] a razón de [X] kg/ha',
    riego: 'Riego por [tipo] durante [X] horas. Humedad del suelo al [X]%',
    pulverizacion: 'Control de [plaga/maleza] con [producto] a razón de [X] L/ha',
    cosecha: 'Cosecha de [cultivo] con rendimiento de [X] tn/ha',
    monitoreo: 'Monitoreo de [plagas/enfermedades]. [Resultado del monitoreo]',
    modificacion: 'Modificación de [campo] de [valor anterior] a [nuevo valor]',
    redimensionado: 'Redimensionado de [X] ha a [Y] ha',
    fusion: 'Fusión con lote [ID] para formar nuevo lote',
    division: 'División del lote en [X] nuevos lotes',
    cambio_cultivo: 'Cambio de cultivo de [cultivo anterior] a [nuevo cultivo]',
    otro: 'Descripción detallada de la acción realizada'
  };

  // Manejar cambio de tipo de acción
  const handleTipoAccionChange = (tipo: string) => {
    setFormData(prev => ({
      ...prev,
      tipo_accion: tipo,
      descripcion: ejemplosDescripcion[tipo as keyof typeof ejemplosDescripcion] || ''
    }));
  };

  // Registrar acción
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.tipo_accion || !formData.descripcion) {
      alert('Por favor complete todos los campos obligatorios');
      return;
    }

    try {
      setLoading(true);
      
      // Simulación de registro en API
      const nuevaAccion = {
        lote_id: loteId,
        tipo_accion: formData.tipo_accion,
        descripcion: formData.descripcion,
        datos_adicionales: formData.datos_adicionales,
        responsable_nombre: formData.responsable_nombre || 'Usuario Actual',
        fecha_accion: new Date().toISOString()
      };

      console.log('Registrando nueva acción:', nuevaAccion);
      
      // Aquí se haría la llamada a la API
      // await api.registrarAccionLote(nuevaAccion);
      
      // Simular delay
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      onActionRegistered();
      onClose();
    } catch (error) {
      console.error('Error registrando acción:', error);
      alert('Error al registrar la acción. Por favor intente nuevamente.');
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
      backgroundColor: 'rgba(0, 0, 0, 0.5)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 1000,
      padding: '20px'
    }}>
      <div style={{
        background: 'white',
        borderRadius: '12px',
        width: '100%',
        maxWidth: '600px',
        maxHeight: '90vh',
        overflow: 'hidden',
        display: 'flex',
        flexDirection: 'column'
      }}>
        {/* Header */}
        <div style={{
          background: 'linear-gradient(135deg, #1f2937 0%, #374151 100%)',
          color: 'white',
          padding: '20px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          <div>
            <h2 style={{ margin: '0 0 5px 0', fontSize: '20px' }}>
              📝 Registrar Nueva Acción
            </h2>
            <p style={{ margin: 0, opacity: 0.9, fontSize: '14px' }}>
              Lote: {loteNombre}
            </p>
          </div>
          <button
            onClick={onClose}
            style={{
              background: 'none',
              border: 'none',
              color: 'white',
              fontSize: '24px',
              cursor: 'pointer',
              padding: '5px'
            }}
          >
            ✕
          </button>
        </div>

        {/* Formulario */}
        <form onSubmit={handleSubmit} style={{ flex: 1, overflow: 'auto', padding: '20px' }}>
          {/* Tipo de Acción */}
          <div style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold', color: '#374151' }}>
              🎯 Tipo de Acción *
            </label>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '8px' }}>
              {tiposAccion.map((tipo) => (
                <button
                  key={tipo.value}
                  type="button"
                  onClick={() => handleTipoAccionChange(tipo.value)}
                  style={{
                    background: formData.tipo_accion === tipo.value ? tipo.color : '#f3f4f6',
                    color: formData.tipo_accion === tipo.value ? 'white' : '#374151',
                    border: 'none',
                    padding: '10px',
                    borderRadius: '8px',
                    cursor: 'pointer',
                    fontSize: '12px',
                    transition: 'all 0.2s',
                    textAlign: 'left'
                  }}
                >
                  {tipo.label}
                </button>
              ))}
            </div>
          </div>

          {/* Descripción */}
          <div style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold', color: '#374151' }}>
              📝 Descripción *
            </label>
            <textarea
              value={formData.descripcion}
              onChange={(e) => setFormData(prev => ({ ...prev, descripcion: e.target.value }))}
              style={{
                width: '100%',
                padding: '12px',
                border: '1px solid #d1d5db',
                borderRadius: '8px',
                fontSize: '14px',
                minHeight: '100px',
                resize: 'vertical',
                fontFamily: 'Arial, sans-serif'
              }}
              placeholder="Describe detalladamente la acción realizada..."
              required
            />
            {formData.tipo_accion && (
              <div style={{ 
                marginTop: '8px', 
                padding: '8px', 
                background: '#f0f9ff', 
                borderRadius: '4px',
                fontSize: '12px',
                color: '#0369a1'
              }}>
                💡 <strong>Ejemplo:</strong> {ejemplosDescripcion[formData.tipo_accion as keyof typeof ejemplosDescripcion]}
              </div>
            )}
          </div>

          {/* Datos Adicionales */}
          <div style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold', color: '#374151' }}>
              📊 Datos Adicionales (Opcional)
            </label>
            <textarea
              value={formData.datos_adicionales}
              onChange={(e) => setFormData(prev => ({ ...prev, datos_adicionales: e.target.value }))}
              style={{
                width: '100%',
                padding: '12px',
                border: '1px solid #d1d5db',
                borderRadius: '8px',
                fontSize: '14px',
                minHeight: '80px',
                resize: 'vertical',
                fontFamily: 'monospace'
              }}
              placeholder='Ejemplo: {"densidad": "70000", "variedad": "DK 72-10", "humedad": "80%"}'
            />
            <div style={{ 
              marginTop: '4px', 
              fontSize: '12px', 
              color: '#6b7280' 
            }}>
              💡 Puede incluir datos técnicos en formato JSON para mejor organización
            </div>
          </div>

          {/* Responsable */}
          <div style={{ marginBottom: '20px' }}>
            <label style={{ display: 'block', marginBottom: '8px', fontWeight: 'bold', color: '#374151' }}>
              👤 Responsable (Opcional)
            </label>
            <input
              type="text"
              value={formData.responsable_nombre}
              onChange={(e) => setFormData(prev => ({ ...prev, responsable_nombre: e.target.value }))}
              style={{
                width: '100%',
                padding: '12px',
                border: '1px solid #d1d5db',
                borderRadius: '8px',
                fontSize: '14px'
              }}
              placeholder="Nombre del responsable de la acción"
            />
          </div>

          {/* Botones */}
          <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
            <button
              type="button"
              onClick={onClose}
              style={{
                background: '#6b7280',
                color: 'white',
                border: 'none',
                padding: '12px 24px',
                borderRadius: '8px',
                cursor: 'pointer',
                fontSize: '14px'
              }}
            >
              ❌ Cancelar
            </button>
            <button
              type="submit"
              disabled={loading || !formData.tipo_accion || !formData.descripcion}
              style={{
                background: loading || !formData.tipo_accion || !formData.descripcion ? '#9ca3af' : '#10b981',
                color: 'white',
                border: 'none',
                padding: '12px 24px',
                borderRadius: '8px',
                cursor: loading || !formData.tipo_accion || !formData.descripcion ? 'not-allowed' : 'pointer',
                fontSize: '14px',
                fontWeight: 'bold'
              }}
            >
              {loading ? '💾 Registrando...' : '💾 Registrar Acción'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default RegistrarAccionLote;
