import React from 'react';

interface EstadosLoteAyudaProps {
  onClose: () => void;
}

const EstadosLoteAyuda: React.FC<EstadosLoteAyudaProps> = ({ onClose }) => {
  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0,0,0,0.7)',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      zIndex: 3000,
      padding: '20px'
    }}>
      <div style={{
        background: 'white',
        borderRadius: '16px',
        maxWidth: '1200px',
        width: '100%',
        maxHeight: '90vh',
        display: 'flex',
        flexDirection: 'column',
        boxShadow: '0 25px 50px -12px rgba(0,0,0,0.25)'
      }}>
        {/* Header */}
        <div style={{
          padding: '24px',
          borderBottom: '3px solid #10b981',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
          borderRadius: '16px 16px 0 0'
        }}>
          <h2 style={{ 
            margin: 0, 
            color: 'white',
            fontSize: '24px',
            display: 'flex',
            alignItems: 'center',
            gap: '12px'
          }}>
            📊 Flujo de Estados del Lote
          </h2>
          <button
            onClick={onClose}
            style={{
              background: 'rgba(255,255,255,0.2)',
              border: 'none',
              borderRadius: '50%',
              width: '36px',
              height: '36px',
              fontSize: '20px',
              cursor: 'pointer',
              color: 'white',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              transition: 'all 0.2s'
            }}
            onMouseEnter={(e) => e.currentTarget.style.background = 'rgba(255,255,255,0.3)'}
            onMouseLeave={(e) => e.currentTarget.style.background = 'rgba(255,255,255,0.2)'}
          >
            ✕
          </button>
        </div>

        {/* Contenido Scrolleable */}
        <div style={{
          flex: 1,
          overflowY: 'auto',
          padding: '24px'
        }}>
          {/* Descripción General */}
          <div style={{
            background: '#eff6ff',
            padding: '16px',
            borderRadius: '12px',
            marginBottom: '24px',
            border: '2px solid #3b82f6'
          }}>
            <h3 style={{ margin: '0 0 8px 0', color: '#1e40af', fontSize: '16px' }}>
              💡 ¿Cómo Funciona?
            </h3>
            <p style={{ margin: 0, color: '#1e3a8a', fontSize: '14px', lineHeight: '1.6' }}>
              El sistema cambia <strong>automáticamente</strong> el estado de tus lotes según las labores que realizas.
              Esto te ahorra tiempo y asegura que el flujo de trabajo sea correcto. Solo las acciones principales 
              como <strong>Siembra</strong> y <strong>Cosecha</strong> requieren confirmación del usuario.
            </p>
          </div>

          {/* Diagrama de Flujo Principal */}
          <div style={{
            background: '#f9fafb',
            padding: '20px',
            borderRadius: '12px',
            marginBottom: '24px',
            border: '2px solid #e5e7eb',
            fontFamily: 'monospace',
            fontSize: '12px',
            overflowX: 'auto'
          }}>
            <h3 style={{ margin: '0 0 16px 0', color: '#374151', fontSize: '16px', fontFamily: 'sans-serif' }}>
              🔄 Ciclo Normal de Cultivo
            </h3>
            <pre style={{ margin: 0, color: '#1f2937', lineHeight: '1.8' }}>
{`┌──────────────┐
│  DISPONIBLE  │ ← Lote libre, sin cultivo
└──────┬───────┘
       │ 1️⃣  Primera labor de preparación (arado/rastra)
       ↓
┌──────────────┐
│EN_PREPARACION│ ← Preparando el suelo
└──────┬───────┘
       │ 2️⃣  Completar 2 labores de preparación
       ↓
┌──────────────┐
│  PREPARADO   │ ← Listo para sembrar
└──────┬───────┘
       │ 🌱 ACCIÓN: Sembrar (requiere confirmación)
       ↓
┌──────────────┐
│   SEMBRADO   │ ← Cultivo recién plantado
└──────┬───────┘
       │ ⏱️  Después de 15 días o labores de desarrollo
       ↓
┌──────────────┐
│EN_CRECIMIENTO│ ← Desarrollo vegetativo
└──────┬───────┘
       │ ⏱️  Después de 45 días desde siembra
       ↓
┌──────────────┐
│ EN_FLORACION │ ← Cultivo floreciendo
└──────┬───────┘
       │ ⏱️  Después de 65 días desde siembra
       ↓
┌────────────────┐
│EN_FRUTIFICACION│ ← Formación de granos/frutos
└────────┬───────┘
         │ ⏱️  Después de 100 días desde siembra
         ↓
┌────────────────┐
│LISTO_PARA_     │ ← ¡Momento óptimo de cosecha!
│    COSECHA     │
└────────┬───────┘
         │ 🌾 ACCIÓN: Cosechar
         ↓
┌────────────────┐
│   COSECHADO    │ ← Cultivo recolectado
└────────┬───────┘
         │ 3️⃣  Primera labor de preparación
         ↓
┌────────────────┐
│ EN_PREPARACION │ ← Nuevo ciclo comienza...
└────────────────┘`}
            </pre>
          </div>

          {/* Estados y Labores */}
          <div style={{ marginBottom: '24px' }}>
            <h3 style={{ margin: '0 0 16px 0', color: '#374151', fontSize: '18px' }}>
              📋 Estados y Labores Disponibles
            </h3>
            
            <div style={{ display: 'grid', gap: '12px' }}>
              {/* DISPONIBLE */}
              <EstadoCard
                estado="DISPONIBLE"
                color="#10b981"
                emoji="🟢"
                descripcion="Lote libre, listo para nuevo ciclo"
                labores={['🚜 Arado', '🔧 Rastra', '🌿 Fertilización', '👁️ Monitoreo']}
                transicion="→ EN_PREPARACION al hacer primera labor de preparación"
              />

              {/* EN_PREPARACION */}
              <EstadoCard
                estado="EN_PREPARACION"
                color="#3b82f6"
                emoji="🔵"
                descripcion="Preparando el suelo para siembra"
                labores={['🚜 Arado', '🔧 Rastra', '🌿 Fertilización', '👁️ Monitoreo']}
                transicion="→ PREPARADO cuando se completan 2 labores de preparación"
              />

              {/* PREPARADO */}
              <EstadoCard
                estado="PREPARADO"
                color="#8b5cf6"
                emoji="🟣"
                descripcion="Listo para sembrar"
                labores={['🌱 Siembra (principal)', '🌿 Fertilización', '👁️ Monitoreo']}
                transicion="→ SEMBRADO al realizar la siembra"
              />

              {/* SEMBRADO */}
              <EstadoCard
                estado="SEMBRADO"
                color="#06b6d4"
                emoji="🔵"
                descripcion="Cultivo recién plantado (0-15 días)"
                labores={['💧 Riego', '🌿 Fertilización', '💨 Pulverización', '👁️ Monitoreo']}
                transicion="→ EN_CRECIMIENTO después de 15 días o labores de desarrollo"
              />

              {/* EN_CRECIMIENTO */}
              <EstadoCard
                estado="EN_CRECIMIENTO"
                color="#0ea5e9"
                emoji="🔵"
                descripcion="Desarrollo vegetativo activo (15-45 días)"
                labores={['💧 Riego', '🌿 Fertilización', '💨 Pulverización', '🌾 Desmalezado', '🧪 Herbicida', '🐛 Insecticida', '👁️ Monitoreo']}
                transicion="→ EN_FLORACION después de 45 días desde siembra"
              />

              {/* EN_FLORACION */}
              <EstadoCard
                estado="EN_FLORACION"
                color="#f59e0b"
                emoji="🟡"
                descripcion="Cultivo en floración (45-65 días)"
                labores={['💧 Riego', '💨 Pulverización', '🐛 Insecticida', '👁️ Monitoreo']}
                transicion="→ EN_FRUTIFICACION después de 65 días desde siembra"
              />

              {/* EN_FRUTIFICACION */}
              <EstadoCard
                estado="EN_FRUTIFICACION"
                color="#f97316"
                emoji="🟠"
                descripcion="Formación de granos/frutos (65-100 días)"
                labores={['💧 Riego (crítico)', '💨 Pulverización', '🐛 Insecticida', '👁️ Monitoreo']}
                transicion="→ LISTO_PARA_COSECHA después de 100 días desde siembra"
              />

              {/* LISTO_PARA_COSECHA */}
              <EstadoCard
                estado="LISTO_PARA_COSECHA"
                color="#eab308"
                emoji="⭐"
                descripcion="¡Momento óptimo para cosechar!"
                labores={['🌾 Cosecha (principal)', '👁️ Monitoreo']}
                transicion="→ COSECHADO al realizar la cosecha"
                destacado={true}
              />

              {/* COSECHADO */}
              <EstadoCard
                estado="COSECHADO"
                color="#8b5cf6"
                emoji="🟣"
                descripcion="Cosecha completada"
                labores={['🚜 Arado', '🔧 Rastra', '👁️ Monitoreo']}
                transicion="→ EN_PREPARACION al hacer labor de preparación (nuevo ciclo)"
              />

              {/* EN_DESCANSO */}
              <EstadoCard
                estado="EN_DESCANSO"
                color="#6b7280"
                emoji="⚪"
                descripcion="Recuperación del suelo (30+ días)"
                labores={['👁️ Monitoreo']}
                transicion="→ EN_PREPARACION después de 30 días + labor de preparación"
              />

              {/* ENFERMO */}
              <EstadoCard
                estado="ENFERMO"
                color="#ef4444"
                emoji="🔴"
                descripcion="Cultivo con problemas - Requiere atención"
                labores={['💨 Pulverización', '🧪 Herbicida', '🐛 Insecticida', '👁️ Monitoreo']}
                transicion="→ Estado de cultivo correspondiente después de 2 tratamientos"
                alerta={true}
              />

              {/* ABANDONADO */}
              <EstadoCard
                estado="ABANDONADO"
                color="#78716c"
                emoji="⚫"
                descripcion="Lote fuera de uso temporal"
                labores={['👁️ Monitoreo']}
                transicion="→ EN_PREPARACION al hacer labor de preparación (reactivación)"
              />
            </div>
          </div>

          {/* Leyenda */}
          <div style={{
            background: '#fef3c7',
            padding: '16px',
            borderRadius: '12px',
            border: '2px solid #fbbf24'
          }}>
            <h3 style={{ margin: '0 0 12px 0', color: '#92400e', fontSize: '16px' }}>
              🔑 Leyenda de Transiciones
            </h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '8px' }}>
              <div style={{ fontSize: '13px', color: '#78350f' }}>
                <strong>1️⃣</strong> Cambio por labor completada
              </div>
              <div style={{ fontSize: '13px', color: '#78350f' }}>
                <strong>2️⃣</strong> Cambio por múltiples labores
              </div>
              <div style={{ fontSize: '13px', color: '#78350f' }}>
                <strong>3️⃣</strong> Cambio por labor de preparación
              </div>
              <div style={{ fontSize: '13px', color: '#78350f' }}>
                <strong>⏱️</strong> Cambio automático por días transcurridos
              </div>
              <div style={{ fontSize: '13px', color: '#78350f' }}>
                <strong>🌱</strong> Acción principal del usuario
              </div>
              <div style={{ fontSize: '13px', color: '#78350f' }}>
                <strong>🌾</strong> Acción de cosecha
              </div>
            </div>
          </div>

          {/* Notas Importantes */}
          <div style={{
            marginTop: '24px',
            background: '#dbeafe',
            padding: '16px',
            borderRadius: '12px',
            border: '2px solid #60a5fa'
          }}>
            <h3 style={{ margin: '0 0 12px 0', color: '#1e40af', fontSize: '16px' }}>
              ⚠️ Notas Importantes
            </h3>
            <ul style={{ margin: 0, paddingLeft: '20px', color: '#1e3a8a', fontSize: '13px', lineHeight: '1.8' }}>
              <li><strong>Solo se puede sembrar desde estado PREPARADO</strong></li>
              <li><strong>Los cambios son automáticos</strong> - El sistema gestiona las transiciones por ti</li>
              <li><strong>Puedes cosechar desde SEMBRADO en adelante</strong> (cosecha anticipada)</li>
              <li><strong>El estado ideal para cosechar es LISTO_PARA_COSECHA</strong> (máximo rendimiento)</li>
              <li><strong>Después de cosechar, el lote puede ir a descanso o preparación directa</strong></li>
              <li><strong>Las transiciones basadas en días son aproximadas</strong> (varían según cultivo)</li>
            </ul>
          </div>

          {/* Ejemplos Prácticos */}
          <div style={{
            marginTop: '24px',
            background: '#f0fdf4',
            padding: '16px',
            borderRadius: '12px',
            border: '2px solid #4ade80'
          }}>
            <h3 style={{ margin: '0 0 12px 0', color: '#166534', fontSize: '16px' }}>
              ✅ Ejemplos Prácticos
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              <EjemploPractico
                titulo="Preparación Básica"
                pasos={[
                  'Lote DISPONIBLE → Hacer "Arado" → Cambia a EN_PREPARACION',
                  'EN_PREPARACION → Hacer "Rastra" → Cambia a PREPARADO',
                  'PREPARADO → Hacer "Siembra" → Cambia a SEMBRADO'
                ]}
              />
              
              <EjemploPractico
                titulo="Desarrollo Natural"
                pasos={[
                  'SEMBRADO (día 0) → Esperar 15 días → Cambia a EN_CRECIMIENTO',
                  'EN_CRECIMIENTO → Esperar hasta día 45 → Cambia a EN_FLORACION',
                  'EN_FLORACION → Esperar hasta día 65 → Cambia a EN_FRUTIFICACION',
                  'EN_FRUTIFICACION → Esperar hasta día 100 → Cambia a LISTO_PARA_COSECHA'
                ]}
              />
              
              <EjemploPractico
                titulo="Después de Cosecha"
                pasos={[
                  'COSECHADO → Hacer "Arado" → Cambia a EN_PREPARACION',
                  'EN_PREPARACION → Hacer "Rastra" → Cambia a PREPARADO',
                  '¡Listo para nuevo ciclo!'
                ]}
              />
            </div>
          </div>
        </div>

        {/* Footer */}
        <div style={{
          padding: '16px 24px',
          borderTop: '2px solid #e5e7eb',
          display: 'flex',
          justifyContent: 'center',
          background: '#f9fafb',
          borderRadius: '0 0 16px 16px'
        }}>
          <button
            onClick={onClose}
            style={{
              background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
              color: 'white',
              border: 'none',
              padding: '12px 32px',
              borderRadius: '8px',
              fontSize: '14px',
              fontWeight: 'bold',
              cursor: 'pointer',
              transition: 'transform 0.2s',
              boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)'
            }}
            onMouseEnter={(e) => e.currentTarget.style.transform = 'translateY(-2px)'}
            onMouseLeave={(e) => e.currentTarget.style.transform = 'translateY(0)'}
          >
            ✓ Entendido
          </button>
        </div>
      </div>
    </div>
  );
};

// Componente auxiliar para tarjetas de estado
const EstadoCard: React.FC<{
  estado: string;
  color: string;
  emoji: string;
  descripcion: string;
  labores: string[];
  transicion: string;
  destacado?: boolean;
  alerta?: boolean;
}> = ({ estado, color, emoji, descripcion, labores, transicion, destacado, alerta }) => {
  return (
    <div style={{
      background: destacado ? '#fef3c7' : alerta ? '#fee2e2' : 'white',
      border: `2px solid ${color}`,
      borderRadius: '10px',
      padding: '14px',
      transition: 'all 0.2s'
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
        <span style={{ fontSize: '20px' }}>{emoji}</span>
        <h4 style={{ margin: 0, color: color, fontSize: '15px', fontWeight: 'bold' }}>
          {estado.replace(/_/g, ' ')}
        </h4>
      </div>
      
      <p style={{ margin: '0 0 10px 0', fontSize: '13px', color: '#6b7280', lineHeight: '1.4' }}>
        {descripcion}
      </p>
      
      <div style={{ marginBottom: '10px' }}>
        <div style={{ fontSize: '12px', fontWeight: 'bold', color: '#374151', marginBottom: '4px' }}>
          Labores disponibles:
        </div>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px' }}>
          {labores.map((labor, idx) => (
            <span
              key={idx}
              style={{
                background: '#f3f4f6',
                padding: '4px 8px',
                borderRadius: '6px',
                fontSize: '11px',
                color: '#374151'
              }}
            >
              {labor}
            </span>
          ))}
        </div>
      </div>
      
      <div style={{
        fontSize: '12px',
        color: color,
        fontWeight: 'bold',
        background: destacado ? 'rgba(234, 179, 8, 0.1)' : alerta ? 'rgba(239, 68, 68, 0.1)' : 'rgba(0,0,0,0.05)',
        padding: '6px 10px',
        borderRadius: '6px'
      }}>
        {transicion}
      </div>
    </div>
  );
};

// Componente auxiliar para ejemplos prácticos
const EjemploPractico: React.FC<{
  titulo: string;
  pasos: string[];
}> = ({ titulo, pasos }) => {
  return (
    <div style={{
      background: 'white',
      padding: '12px',
      borderRadius: '8px',
      border: '1px solid #86efac'
    }}>
      <div style={{ fontSize: '13px', fontWeight: 'bold', color: '#166534', marginBottom: '8px' }}>
        {titulo}
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
        {pasos.map((paso, idx) => (
          <div key={idx} style={{ fontSize: '12px', color: '#166534', paddingLeft: '12px' }}>
            {idx + 1}. {paso}
          </div>
        ))}
      </div>
    </div>
  );
};

export default EstadosLoteAyuda;


