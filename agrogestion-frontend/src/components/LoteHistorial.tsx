import React, { useState, useEffect } from 'react';
import RegistrarAccionLote from './RegistrarAccionLote';

interface HistorialEntry {
  id: number;
  lote_id: number;
  tipo_accion: string;
  descripcion: string;
  datos_anteriores?: any;
  datos_nuevos?: any;
  lotes_relacionados?: any;
  responsable_nombre: string;
  fecha_accion: string;
}

interface LoteHistorialProps {
  loteId: number;
  loteNombre: string;
  onClose: () => void;
}

const LoteHistorial: React.FC<LoteHistorialProps> = ({ loteId, loteNombre, onClose }) => {
  const [historial, setHistorial] = useState<HistorialEntry[]>([]);
  const [loading, setLoading] = useState(false);
  const [filterType, setFilterType] = useState<string>('todos');
  const [showRegistrarAccion, setShowRegistrarAccion] = useState(false);

  // Tipos de acciones disponibles
  const tiposAccion = [
    { value: 'todos', label: '📋 Todas las acciones', color: '#6b7280' },
    { value: 'creacion', label: '🆕 Creación', color: '#10b981' },
    { value: 'modificacion', label: '✏️ Modificación', color: '#3b82f6' },
    { value: 'redimensionado', label: '📏 Redimensionado', color: '#f59e0b' },
    { value: 'fusion', label: '🔗 Fusión', color: '#8b5cf6' },
    { value: 'division', label: '✂️ División', color: '#ef4444' },
    { value: 'eliminacion', label: '🗑️ Eliminación', color: '#dc2626' },
    { value: 'siembra', label: '🌱 Siembra', color: '#059669' },
    { value: 'fertilizacion', label: '🌿 Fertilización', color: '#7c3aed' },
    { value: 'riego', label: '💧 Riego', color: '#0ea5e9' },
    { value: 'pulverizacion', label: '🧪 Pulverización', color: '#f97316' },
    { value: 'cosecha', label: '🌾 Cosecha', color: '#eab308' },
    { value: 'monitoreo', label: '👁️ Monitoreo', color: '#06b6d4' },
    { value: 'cambio_cultivo', label: '🔄 Cambio de Cultivo', color: '#84cc16' },
    { value: 'otro', label: '📝 Otro', color: '#64748b' }
  ];

  // Cargar historial del lote
  const loadHistorial = async () => {
    try {
      setLoading(true);
      // Simulación de carga desde API
      const mockHistorial: HistorialEntry[] = [
        {
          id: 1,
          lote_id: loteId,
          tipo_accion: 'creacion',
          descripcion: 'Lote creado exitosamente',
          datos_nuevos: { nombre: loteNombre, superficie: 25.5, cultivo: 'Soja' },
          responsable_nombre: 'Administrador Sistema',
          fecha_accion: '2024-01-15 10:30:00'
        },
        {
          id: 2,
          lote_id: loteId,
          tipo_accion: 'modificacion',
          descripcion: 'Cambio de cultivo de Soja a Maíz',
          datos_anteriores: { cultivo: 'Soja' },
          datos_nuevos: { cultivo: 'Maíz' },
          responsable_nombre: 'Juan Carlos González',
          fecha_accion: '2024-02-20 14:15:00'
        },
        {
          id: 3,
          lote_id: loteId,
          tipo_accion: 'redimensionado',
          descripcion: 'Redimensionado de 25.5 ha a 23.8 ha',
          datos_anteriores: { superficie: 25.5 },
          datos_nuevos: { superficie: 23.8 },
          responsable_nombre: 'Juan Carlos González',
          fecha_accion: '2024-02-25 09:45:00'
        },
        {
          id: 4,
          lote_id: loteId,
          tipo_accion: 'siembra',
          descripcion: 'Siembra de Maíz DK 72-10 con densidad de 70.000 plantas/ha',
          datos_nuevos: { densidad: '70000', variedad: 'DK 72-10' },
          responsable_nombre: 'Carlos Alberto López',
          fecha_accion: '2024-03-10 08:00:00'
        },
        {
          id: 5,
          lote_id: loteId,
          tipo_accion: 'fertilizacion',
          descripcion: 'Aplicación de fertilizante NPK 15-15-15 a razón de 200 kg/ha',
          datos_nuevos: { fertilizante: 'NPK 15-15-15', dosis: '200 kg/ha' },
          responsable_nombre: 'Carlos Alberto López',
          fecha_accion: '2024-03-15 11:30:00'
        },
        {
          id: 6,
          lote_id: loteId,
          tipo_accion: 'riego',
          descripcion: 'Riego por aspersión durante 4 horas. Humedad del suelo al 80%',
          datos_nuevos: { duracion: '4 horas', humedad: '80%' },
          responsable_nombre: 'Carlos Alberto López',
          fecha_accion: '2024-03-25 16:20:00'
        },
        {
          id: 7,
          lote_id: loteId,
          tipo_accion: 'pulverizacion',
          descripcion: 'Control de malezas con glifosato 48% a razón de 3 L/ha',
          datos_nuevos: { producto: 'Glifosato 48%', dosis: '3 L/ha' },
          responsable_nombre: 'Carlos Alberto López',
          fecha_accion: '2024-04-10 13:45:00'
        },
        {
          id: 8,
          lote_id: loteId,
          tipo_accion: 'monitoreo',
          descripcion: 'Monitoreo de plagas y enfermedades. No se detectaron problemas',
          datos_nuevos: { estado: 'Sin problemas detectados' },
          responsable_nombre: 'Juan Carlos González',
          fecha_accion: '2024-04-20 10:15:00'
        },
        {
          id: 9,
          lote_id: loteId,
          tipo_accion: 'cosecha',
          descripcion: 'Cosecha de Maíz con rendimiento de 12.5 tn/ha',
          datos_nuevos: { rendimiento: '12.5 tn/ha', fecha_cosecha: '2024-07-15' },
          responsable_nombre: 'Carlos Alberto López',
          fecha_accion: '2024-07-15 07:30:00'
        },
        {
          id: 10,
          lote_id: loteId,
          tipo_accion: 'cambio_cultivo',
          descripcion: 'Cambio de cultivo de Maíz a Soja para la próxima campaña',
          datos_anteriores: { cultivo: 'Maíz' },
          datos_nuevos: { cultivo: 'Soja' },
          responsable_nombre: 'María Elena Rodríguez',
          fecha_accion: '2024-08-01 15:00:00'
        }
      ];
      setHistorial(mockHistorial);
    } catch (error) {
      console.error('Error cargando historial:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHistorial();
  }, [loteId]);

  // Formatear fecha
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  // Obtener icono y color para tipo de acción
  const getActionInfo = (tipo: string) => {
    const action = tiposAccion.find(t => t.value === tipo);
    return action || tiposAccion[0];
  };

  // Filtrar historial
  const filteredHistorial = historial.filter(entry => 
    filterType === 'todos' || entry.tipo_accion === filterType
  );

  // Renderizar datos JSON
  const renderJsonData = (data: any, label: string) => {
    if (!data) return null;
    
    return (
      <div style={{ marginTop: '8px' }}>
        <strong style={{ color: '#374151' }}>{label}:</strong>
        <div style={{ 
          background: '#f9fafb', 
          padding: '8px', 
          borderRadius: '4px', 
          fontSize: '12px',
          marginTop: '4px',
          fontFamily: 'monospace'
        }}>
          {Object.entries(data).map(([key, value]) => (
            <div key={key} style={{ marginBottom: '2px' }}>
              <span style={{ color: '#6b7280' }}>{key}:</span> {String(value)}
            </div>
          ))}
        </div>
      </div>
    );
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
        maxWidth: '900px',
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
              📋 Historial del Lote: {loteNombre}
            </h2>
            <p style={{ margin: 0, opacity: 0.9, fontSize: '14px' }}>
              Registro cronológico de todas las acciones realizadas
            </p>
          </div>
          <button
            onClick={() => setShowRegistrarAccion(true)}
            style={{
              background: '#10b981',
              color: 'white',
              border: 'none',
              padding: '8px 16px',
              borderRadius: '6px',
              cursor: 'pointer',
              fontSize: '14px',
              fontWeight: 'bold'
            }}
          >
            ➕ Nueva Acción
          </button>
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

        {/* Filtros */}
        <div style={{ padding: '15px 20px', borderBottom: '1px solid #e5e7eb' }}>
          <div style={{ marginBottom: '10px' }}>
            <label style={{ fontWeight: 'bold', color: '#374151', marginRight: '10px' }}>
              🔍 Filtrar por tipo:
            </label>
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
            {tiposAccion.map((tipo) => (
              <button
                key={tipo.value}
                onClick={() => setFilterType(tipo.value)}
                style={{
                  background: filterType === tipo.value ? tipo.color : '#f3f4f6',
                  color: filterType === tipo.value ? 'white' : '#374151',
                  border: 'none',
                  padding: '6px 12px',
                  borderRadius: '20px',
                  fontSize: '12px',
                  cursor: 'pointer',
                  transition: 'all 0.2s'
                }}
              >
                {tipo.label}
              </button>
            ))}
          </div>
        </div>

        {/* Contenido */}
        <div style={{ flex: 1, overflow: 'auto', padding: '20px' }}>
          {loading ? (
            <div style={{ textAlign: 'center', padding: '40px', color: '#6b7280' }}>
              🔄 Cargando historial...
            </div>
          ) : filteredHistorial.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '40px', color: '#6b7280' }}>
              📭 No hay registros en el historial para este lote
            </div>
          ) : (
            <div style={{ position: 'relative' }}>
              {/* Línea de tiempo */}
              <div style={{
                position: 'absolute',
                left: '20px',
                top: 0,
                bottom: 0,
                width: '2px',
                background: '#e5e7eb'
              }} />
              
              {filteredHistorial.map((entry, index) => {
                const actionInfo = getActionInfo(entry.tipo_accion);
                return (
                  <div key={entry.id} style={{
                    position: 'relative',
                    marginBottom: '30px',
                    paddingLeft: '50px'
                  }}>
                    {/* Punto de la línea de tiempo */}
                    <div style={{
                      position: 'absolute',
                      left: '11px',
                      top: '5px',
                      width: '20px',
                      height: '20px',
                      borderRadius: '50%',
                      background: actionInfo.color,
                      border: '3px solid white',
                      boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                      zIndex: 2
                    }} />
                    
                    {/* Tarjeta de entrada */}
                    <div style={{
                      background: 'white',
                      border: '1px solid #e5e7eb',
                      borderRadius: '8px',
                      padding: '15px',
                      boxShadow: '0 2px 4px rgba(0,0,0,0.05)'
                    }}>
                      {/* Header de la entrada */}
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '10px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                          <span style={{ fontSize: '18px' }}>
                            {actionInfo.label.split(' ')[0]}
                          </span>
                          <span style={{ 
                            background: actionInfo.color, 
                            color: 'white', 
                            padding: '2px 8px', 
                            borderRadius: '12px', 
                            fontSize: '11px',
                            fontWeight: 'bold'
                          }}>
                            {entry.tipo_accion.toUpperCase()}
                          </span>
                        </div>
                        <div style={{ textAlign: 'right' }}>
                          <div style={{ fontSize: '12px', color: '#6b7280' }}>
                            {formatDate(entry.fecha_accion)}
                          </div>
                        </div>
                      </div>

                      {/* Descripción */}
                      <p style={{ 
                        margin: '0 0 10px 0', 
                        color: '#374151', 
                        fontSize: '14px',
                        lineHeight: '1.5'
                      }}>
                        {entry.descripcion}
                      </p>

                      {/* Responsable */}
                      <div style={{ 
                        fontSize: '12px', 
                        color: '#6b7280',
                        marginBottom: '10px'
                      }}>
                        <strong>👤 Responsable:</strong> {entry.responsable_nombre}
                      </div>

                      {/* Datos JSON */}
                      {entry.datos_anteriores && renderJsonData(entry.datos_anteriores, '📤 Datos Anteriores')}
                      {entry.datos_nuevos && renderJsonData(entry.datos_nuevos, '📥 Datos Nuevos')}
                      {entry.lotes_relacionados && renderJsonData(entry.lotes_relacionados, '🔗 Lotes Relacionados')}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Footer */}
        <div style={{
          padding: '15px 20px',
          borderTop: '1px solid #e5e7eb',
          background: '#f9fafb',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          <div style={{ fontSize: '14px', color: '#6b7280' }}>
            📊 Total de registros: {filteredHistorial.length}
          </div>
          <button
            onClick={onClose}
            style={{
              background: '#6b7280',
              color: 'white',
              border: 'none',
              padding: '8px 16px',
              borderRadius: '6px',
              cursor: 'pointer',
              fontSize: '14px'
            }}
          >
            Cerrar
          </button>
        </div>
      </div>

      {/* Modal para registrar nueva acción */}
      {showRegistrarAccion && (
        <RegistrarAccionLote
          loteId={loteId}
          loteNombre={loteNombre}
          onClose={() => setShowRegistrarAccion(false)}
          onActionRegistered={() => {
            loadHistorial(); // Recargar historial
            setShowRegistrarAccion(false);
          }}
        />
      )}
    </div>
  );
};

export default LoteHistorial;
