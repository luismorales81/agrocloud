import React, { useState } from 'react';
import { Icon } from '../../../components/icons';

/**
 * Pantalla de Ayuda del Módulo de Porcinos
 * Guía completa para orientar al usuario en todas las funcionalidades del módulo
 */
const AyudaPorcinosScreen: React.FC = () => {
  const [seccionActiva, setSeccionActiva] = useState('general');

  const secciones = [
    { id: 'general', label: 'General', icon: 'Home' },
    { id: 'reproduccion', label: 'Reproducción', icon: 'Baby' },
    { id: 'alimentacion', label: 'Alimentación', icon: 'Wheat' },
    { id: 'sanidad', label: 'Sanidad', icon: 'Syringe' },
    { id: 'produccion', label: 'Producción', icon: 'PiggyBank' },
    { id: 'configuracion', label: 'Configuración', icon: 'Settings' },
    { id: 'reportes', label: 'Reportes', icon: 'BarChart' },
  ];

  const renderContenidoGeneral = () => (
    <div style={{ padding: '2rem' }}>
      <div>
        <h3 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937', marginBottom: '1rem' }}>
          <Icon name="PiggyBank" size={24} style={{ marginRight: '0.5rem', display: 'inline-block', verticalAlign: 'middle' }} /> Bienvenido al Módulo de Porcinos
        </h3>
        <p style={{ color: '#6b7280', marginBottom: '2rem', lineHeight: '1.6' }}>
          El módulo de Porcinos de AgroCloud te permite gestionar de manera integral toda la producción porcina,
          desde la reproducción hasta la venta. Esta guía te ayudará a aprovechar al máximo todas las funcionalidades disponibles.
        </p>
      </div>

      <div style={{ marginBottom: '2rem' }}>
        <h4 style={{ fontSize: '1.25rem', fontWeight: '600', color: '#374151', marginBottom: '1rem' }}>
          <Icon name="Target" size={24} style={{ marginRight: '0.5rem', display: 'inline-block', verticalAlign: 'middle' }} /> Objetivos del Módulo
        </h4>
        <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', color: '#6b7280', lineHeight: '1.8' }}>
          <li>Gestionar el ciclo reproductivo completo (servicios, gestación, partos, destetes)</li>
          <li><strong>Consumo diario automático</strong> de alimento basado en recetas y cantidad de animales</li>
          <li>Calendario de alimentación con confirmación diaria de consumo</li>
          <li>Control de stock con manejo inteligente de déficits</li>
          <li>Registro de derrames, pérdidas y accidentes</li>
          <li>Registrar y monitorear la sanidad de los animales</li>
          <li>Gestionar lotes y movimientos de animales</li>
          <li>Realizar seguimiento de producción y rentabilidad</li>
          <li>Generar reportes productivos y económicos con cálculos de costos avanzados</li>
          <li>Configurar parámetros productivos, económicos y recetas por etapa</li>
        </ul>
      </div>

      <div style={{ marginBottom: '2rem' }}>
        <h4 style={{ fontSize: '1.25rem', fontWeight: '600', color: '#374151', marginBottom: '1rem' }}>
          <Icon name="Rocket" size={24} style={{ marginRight: '0.5rem', display: 'inline-block', verticalAlign: 'middle' }} /> Guía de Configuración Inicial
        </h4>
        <div style={{ backgroundColor: '#dbeafe', padding: '1rem', borderRadius: '0.5rem', marginBottom: '1rem' }}>
          <p style={{ color: '#1e40af', fontWeight: '500', marginBottom: '0.5rem' }}>
            <Icon name="ClipboardCheck" size={20} style={{ marginRight: '0.5rem', display: 'inline-block', verticalAlign: 'middle' }} /> Checklist de Configuración Inicial
          </p>
          <p style={{ color: '#1e3a8a', fontSize: '0.875rem' }}>
            Completa estos pasos en orden para configurar el módulo correctamente
          </p>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div style={{ borderLeft: '4px solid #6366f1', paddingLeft: '1rem', backgroundColor: 'rgba(99, 102, 241, 0.06)' }}>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.5rem' }}>Dónde está la Configuración</h5>
            <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.5rem' }}>
              Menú <strong>Porcinos → Configuración</strong>. En la pestaña <strong>Configuración del Módulo</strong> se abre la pantalla &quot;Configuraciones del Módulo Porcinos&quot; con 11 pestañas: Establecimiento, Productivos, Económicos, Razas, Servicios, Mortalidad, Motivos Baja, Sanitarios, Tipos Parto, Ubicaciones, Generales. Ahí se configuran todos los parámetros y catálogos del módulo.
            </p>
          </div>

          <div style={{ borderLeft: '4px solid #10b981', paddingLeft: '1rem' }}>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.5rem' }}>Paso 1: Configurar Parámetros</h5>
            <ol style={{ listStyle: 'decimal', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li>Ve a <strong>Porcinos → Configuración</strong> y abre la pestaña <strong>Configuración del Módulo</strong></li>
              <li>En <strong>Establecimiento</strong>: unidad de manejo y nombre del establecimiento</li>
              <li>En <strong>Productivos</strong>: días de gestación, lactancia, control celo, alertas (partos, destetes, ecografías, etc.)</li>
              <li>En <strong>Económicos</strong>: datos económicos (precios, costos, método de imputación de granos)</li>
              <li>Revisa los catálogos (Razas, Servicios, Mortalidad, Motivos Baja, Sanitarios, Tipos Parto, Ubicaciones) y crea los que uses</li>
              <li>Guarda los cambios en cada pestaña</li>
            </ol>
          </div>

          <div style={{ borderLeft: '4px solid #3b82f6', paddingLeft: '1rem' }}>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.5rem' }}>Paso 2: Registrar Madres</h5>
            <ol style={{ listStyle: 'decimal', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li>Ve a <strong>Madres</strong></li>
              <li>Haz clic en <strong>"Nueva Madre"</strong></li>
              <li>Ingresa identificación única (caravana, arete, etc.)</li>
              <li>Selecciona origen (EXTERNA o INTERNA)</li>
              <li>Define fecha de nacimiento</li>
              <li>Establece estado inicial (CACHORRA, ADULTA, etc.)</li>
              <li>Asigna a un lote</li>
              <li>Guarda la madre</li>
            </ol>
          </div>

          <div style={{ borderLeft: '4px solid #8b5cf6', paddingLeft: '1rem' }}>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.5rem' }}>Paso 3: Registrar Padrillos</h5>
            <ol style={{ listStyle: 'decimal', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li>Ve a <strong>Padrillos</strong></li>
              <li>Haz clic en <strong>"Nuevo Padrillo"</strong></li>
              <li>Ingresa identificación única</li>
              <li>Define fecha de nacimiento</li>
              <li>Registra información genética si aplica</li>
              <li>Guarda el padrillo</li>
            </ol>
          </div>

          <div style={{ borderLeft: '4px solid #f59e0b', paddingLeft: '1rem' }}>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.5rem' }}>Paso 4: Crear Recetas y Asociarlas a Etapas</h5>
            <ol style={{ listStyle: 'decimal', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li>Ve a <strong>Alimentación → Insumos Compuestos</strong></li>
              <li>Haz clic en <strong>"Nueva Receta"</strong></li>
              <li>Define nombre (ej: "Ración Gestación") y selecciona tipo <strong>RACIÓN</strong></li>
              <li>Agrega componentes (maíz propio, soja propia, núcleo, etc.)</li>
              <li>Establece porcentajes o cantidades fijas</li>
              <li>El sistema calculará el costo automáticamente usando FIFO para granos propios</li>
              <li>Guarda la receta</li>
              <li>En la lista de recetas, haz clic en <strong>"Asociar a Etapa"</strong> (botón morado con icono Share2)</li>
              <li>Para cada etapa (GESTACION, LACTANCIA, F1, F2, F3, F4, DESARROLLO, TERMINACION):
                <ul style={{ listStyle: 'circle', paddingLeft: '1.5rem', marginTop: '0.5rem' }}>
                  <li>Selecciona la <strong>etapa</strong> de alimentación</li>
                  <li>Define el <strong>consumo diario por animal</strong> (kg/día)</li>
                  <li>Confirma la asociación</li>
                </ul>
              </li>
              <li>Esta configuración permite el cálculo automático del consumo diario</li>
              <li><strong>Alternativa:</strong> También puedes configurar recetas por etapa desde <strong>Porcinos → Configuración → Configuración del Módulo</strong> (pestaña Generales o desde Insumos Compuestos → Asociar a Etapa)</li>
            </ol>
          </div>

          <div style={{ borderLeft: '4px solid #ef4444', paddingLeft: '1rem' }}>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.5rem' }}>Paso 5: Registrar Primer Servicio</h5>
            <ol style={{ listStyle: 'decimal', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li>Ve a <strong>Servicios</strong></li>
              <li>Haz clic en <strong>"Nuevo Servicio"</strong></li>
              <li>Selecciona la madre</li>
              <li>Elige tipo (MONTA_NATURAL o IA)</li>
              <li>Selecciona el padrillo</li>
              <li>Registra fecha del servicio</li>
              <li>El sistema creará automáticamente la gestación</li>
              <li>Guarda el servicio</li>
            </ol>
          </div>
        </div>
      </div>

      <div>
        <h4 style={{ fontSize: '1.25rem', fontWeight: '600', color: '#374151', marginBottom: '1rem' }}>
          💡 Consejos para el Uso Diario
        </h4>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1rem' }}>
          <div style={{ backgroundColor: '#f0fdf4', padding: '1rem', borderRadius: '0.5rem' }}>
            <h5 style={{ fontWeight: '600', color: '#166534', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="CheckCircle" size={20} /> Buenas Prácticas</h5>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#15803d', lineHeight: '1.8' }}>
              <li>Registra servicios el mismo día que se realizan</li>
              <li>Actualiza el estado de las madres regularmente</li>
              <li>Registra partos inmediatamente después de ocurrir</li>
              <li><strong>Confirma la alimentación diaria</strong> en el calendario de alimentación</li>
              <li>Registra derrames y pérdidas cuando ocurran (no esperes al final del día)</li>
              <li>Revisa las alertas de stock insuficiente en el dashboard</li>
              <li>Revisa el calendario de alertas diariamente</li>
              <li>Documenta observaciones importantes en eventos y registros</li>
            </ul>
          </div>
          <div style={{ backgroundColor: '#fef2f2', padding: '1rem', borderRadius: '0.5rem' }}>
            <h5 style={{ fontWeight: '600', color: '#991b1b', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="XCircle" size={20} /> Evita Estos Errores</h5>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#dc2626', lineHeight: '1.8' }}>
              <li>No olvides registrar servicios después de realizarlos</li>
              <li>No dejes gestaciones sin fecha de parto</li>
              <li>No mezcles animales de diferentes etapas en el mismo lote</li>
              <li>No olvides actualizar estados después de eventos</li>
              <li>No ignores las alertas del calendario ni las alertas de stock insuficiente</li>
              <li>No registres partos sin validar la gestación</li>
              <li>No registres derrames sin motivo y observaciones (obligatorio)</li>
              <li>No modifiques consumos ya confirmados - usa correcciones en su lugar</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );

  const renderContenidoReproduccion = () => (
    <div style={{ padding: '2rem' }}>
      <div>
        <h3 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937', marginBottom: '1rem' }}>
          <Icon name="Baby" size={24} style={{ marginRight: '0.5rem', display: 'inline-block', verticalAlign: 'middle' }} /> Gestión del Ciclo Reproductivo
        </h3>
        <p style={{ color: '#6b7280', marginBottom: '2rem', lineHeight: '1.6' }}>
          El ciclo reproductivo es el corazón de la producción porcina. Aquí gestionas servicios, gestaciones y partos.
        </p>
      </div>

      {/* Servicios */}
      <div style={{ backgroundColor: 'white', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1.5rem', marginBottom: '1.5rem' }}>
        <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#374151', marginBottom: '1rem' }}>
          🔗 Servicios
        </h4>
        <p style={{ color: '#6b7280', marginBottom: '1rem' }}>
          Los servicios registran el apareamiento o inseminación artificial de las madres.
        </p>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem' }}>📝 Cómo Registrar un Servicio</h5>
            <ol style={{ listStyle: 'decimal', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li>Ve a <strong>Servicios</strong></li>
              <li>Haz clic en <strong>"Nuevo Servicio"</strong></li>
              <li>Selecciona la <strong>madre</strong> (debe estar en estado ADULTA o CACHORRA)</li>
              <li>Elige <strong>tipo</strong>: Monta Natural o Inseminación Artificial</li>
              <li>Selecciona el <strong>padrillo</strong></li>
              <li>Registra la <strong>fecha del servicio</strong></li>
              <li>Agrega <strong>observaciones</strong> si es necesario</li>
              <li>El sistema creará automáticamente una <strong>gestación</strong></li>
              <li>Guarda el servicio</li>
            </ol>
          </div>

          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="BarChart" size={20} /> Estados del Servicio</h5>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li><strong>PENDIENTE_CONTROL:</strong> Esperando confirmación de preñez (21 días)</li>
              <li><strong>PREÑEZ_CONFIRMADA:</strong> Ecografía positiva, gestación confirmada</li>
              <li><strong>FALLIDO:</strong> El servicio no resultó en preñez</li>
            </ul>

            <h5 style={{ fontWeight: '600', color: '#1f2937', marginTop: '1rem', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="Bell" size={20} /> Alertas Automáticas</h5>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li>Ecografía programada (21 días después del servicio)</li>
              <li>Control de celo (21 días después, si retorna el celo el servicio falló)</li>
            </ul>
          </div>
        </div>
      </div>

      {/* Gestación */}
      <div style={{ backgroundColor: 'white', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1.5rem', marginBottom: '1.5rem' }}>
        <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#374151', marginBottom: '1rem' }}>
          <Icon name="Heart" size={24} style={{ marginRight: '0.5rem', display: 'inline-block', verticalAlign: 'middle' }} /> Gestación
        </h4>
        <p style={{ color: '#6b7280', marginBottom: '1rem' }}>
          La gestación se crea automáticamente al registrar un servicio. Dura aproximadamente 115 días.
        </p>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem' }}>📝 Gestión de Gestaciones</h5>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li><strong>Ver Gestaciones:</strong> Lista todas las gestaciones activas</li>
              <li><strong>Fecha Probable de Parto:</strong> Calculada automáticamente (fecha servicio + 115 días)</li>
              <li><strong>Marcar para Maternidad:</strong> Indica que la madre pasó a sala de maternidad</li>
              <li><strong>Registrar Aborto:</strong> Si la gestación se interrumpe</li>
            </ul>
          </div>

          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="Bell" size={20} /> Alertas del Calendario</h5>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li><strong>Parto Próximo:</strong> X días antes de la fecha probable</li>
              <li><strong>Pasaje a Maternidad:</strong> X días antes del parto</li>
              <li><strong>Gestación Vencida:</strong> Si pasó la fecha y no hay parto registrado</li>
            </ul>
          </div>
        </div>
      </div>

      {/* Partos */}
      <div style={{ backgroundColor: 'white', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1.5rem' }}>
        <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#374151', marginBottom: '1rem' }}>
          <Icon name="Baby" size={24} style={{ marginRight: '0.5rem', display: 'inline-block', verticalAlign: 'middle' }} /> Partos
        </h4>
        <p style={{ color: '#6b7280', marginBottom: '1rem' }}>
          El registro de partos es crítico. Al registrar un parto, el sistema realiza múltiples acciones automáticas.
        </p>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem' }}>📝 Cómo Registrar un Parto</h5>
            <ol style={{ listStyle: 'decimal', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li>Ve a <strong>Partos</strong></li>
              <li>Haz clic en <strong>"Nuevo Parto"</strong></li>
              <li>Selecciona la <strong>madre</strong> (debe tener gestación activa)</li>
              <li>Registra <strong>fecha y hora</strong> del parto</li>
              <li>Ingresa <strong>nacidos vivos</strong></li>
              <li>Ingresa <strong>nacidos muertos</strong> (si los hay)</li>
              <li>Ingresa <strong>momias</strong> (si las hay)</li>
              <li>Registra <strong>peso promedio</strong> de los lechones</li>
              <li>Agrega <strong>observaciones</strong></li>
              <li>Guarda el parto</li>
            </ol>
          </div>

          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="Settings" size={20} /> Acciones Automáticas</h5>
            <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.75rem' }}>
              Al registrar un parto, el sistema automáticamente:
            </p>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li>Cierra la gestación</li>
              <li>Cambia el estado de la madre a <strong>LACTANCIA</strong></li>
              <li>Crea registros de <strong>Recría</strong> para cada lechón vivo</li>
              <li>Crea registros de <strong>Muerte</strong> para lechones muertos</li>
              <li>Programa el <strong>destete</strong> (fecha parto + días lactancia)</li>
              <li>Actualiza estadísticas productivas</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );

  const renderContenidoAlimentacion = () => (
    <div style={{ padding: '2rem' }}>
      <div>
        <h3 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937', marginBottom: '1rem' }}>
          <Icon name="Wheat" size={24} style={{ marginRight: '0.5rem', display: 'inline-block', verticalAlign: 'middle' }} /> Gestión de Alimentación
        </h3>
        <p style={{ color: '#6b7280', marginBottom: '2rem', lineHeight: '1.6' }}>
          El sistema de alimentación gestiona automáticamente el consumo diario de alimento basado en recetas, 
          cantidad de animales y etapas de crianza. Además, permite registrar derrames y pérdidas para mantener 
          un control preciso del inventario.
        </p>
      </div>

      {/* Consumo Diario Automático */}
      <div style={{ backgroundColor: '#ecfdf5', border: '2px solid #10b981', borderRadius: '0.5rem', padding: '1.5rem', marginBottom: '1.5rem' }}>
        <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#065f46', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Icon name="Zap" size={24} /> Sistema de Consumo Diario Automático
        </h4>
        <p style={{ color: '#047857', marginBottom: '1rem', fontWeight: '500' }}>
          El sistema genera automáticamente el consumo de alimento una vez por día para todos los animales activos.
        </p>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
          <div>
            <h5 style={{ fontWeight: '600', color: '#065f46', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="Clock" size={20} /> Funcionamiento Automático</h5>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#047857', lineHeight: '1.8' }}>
              <li><strong>Generación Diaria:</strong> Se ejecuta automáticamente cada día</li>
              <li><strong>Cálculo por Animal:</strong> Basado en cantidad de animales por corral/lote</li>
              <li><strong>Por Etapa:</strong> Usa la receta asignada según la etapa de crianza</li>
              <li><strong>Consumo Diario:</strong> Multiplica cantidad de animales × consumo por animal</li>
              <li><strong>Descuento de Stock:</strong> Descuenta automáticamente del inventario</li>
            </ul>
          </div>

          <div>
            <h5 style={{ fontWeight: '600', color: '#065f46', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="AlertTriangle" size={20} /> Manejo de Stock Insuficiente</h5>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#047857', lineHeight: '1.8' }}>
              <li><strong>Consumo Siempre Se Registra:</strong> Incluso si no hay stock suficiente</li>
              <li><strong>Stock Negativo Permitido:</strong> Solo para consumo automático</li>
              <li><strong>Alertas Visibles:</strong> Se generan alertas en el dashboard y calendario</li>
              <li><strong>Déficit Registrado:</strong> Se registra la cantidad faltante por insumo</li>
              <li><strong>No Bloquea Operación:</strong> El sistema sigue funcionando normalmente</li>
            </ul>
          </div>
        </div>
      </div>

      {/* Calendario de Alimentación */}
      <div style={{ backgroundColor: 'white', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1.5rem', marginBottom: '1.5rem' }}>
        <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#374151', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Icon name="Calendar" size={24} /> Calendario de Alimentación
        </h4>
        <p style={{ color: '#6b7280', marginBottom: '1rem' }}>
          Cada día se crea automáticamente un registro en el calendario cuando se genera el consumo. 
          El calendario muestra el estado de cada día y permite confirmar la alimentación.
        </p>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="Circle" size={20} color="#ef4444" /> Estados del Día</h5>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li><strong style={{ color: '#ef4444' }}>🔴 PENDIENTE:</strong> Día creado, consumo generado, pendiente de confirmación</li>
              <li><strong style={{ color: '#10b981' }}>🟢 CONFIRMADO:</strong> Alimentación confirmada por un usuario</li>
              <li><strong style={{ color: '#f59e0b' }}>🟡 CONFIRMADO_CON_CORRECCIONES:</strong> Confirmado pero con ajustes realizados</li>
            </ul>
          </div>

          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="CheckCircle" size={20} /> Confirmar Alimentación</h5>
            <ol style={{ listStyle: 'decimal', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li>Accede al <strong>Calendario de Alimentación</strong></li>
              <li>Visualiza los días del mes con sus estados (colores)</li>
              <li>Haz clic en un día para ver detalles</li>
              <li>Revisa los consumos generados por etapa</li>
              <li>Verifica alertas de stock insuficiente si las hay</li>
              <li>Haz clic en <strong>"Confirmar Alimentación"</strong></li>
              <li>El día pasará a estado CONFIRMADO (verde)</li>
            </ol>
            <div style={{ backgroundColor: '#fef3c7', padding: '0.75rem', borderRadius: '0.375rem', marginTop: '0.75rem', fontSize: '0.875rem', color: '#92400e' }}>
              <strong>⚠️ Importante:</strong> Confirmar un día NO modifica stock ni recalcula consumos. 
              Solo registra que la alimentación fue verificada y realizada.
            </div>
          </div>
        </div>
      </div>

      {/* Derrames y Pérdidas */}
      <div style={{ backgroundColor: 'white', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1.5rem', marginBottom: '1.5rem' }}>
        <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#374151', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Icon name="AlertTriangle" size={24} /> Derrames, Pérdidas y Accidentes
        </h4>
        <p style={{ color: '#6b7280', marginBottom: '1rem' }}>
          Los derrames, pérdidas y accidentes son movimientos de stock independientes del consumo diario automático.
        </p>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem' }}>📝 Registrar Derrame/Pérdida</h5>
            <ol style={{ listStyle: 'decimal', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li>Ve a <strong>Calendario de Alimentación</strong></li>
              <li>Selecciona el día del derrame/pérdida</li>
              <li>Haz clic en <strong>"Registrar Derrame/Pérdida"</strong></li>
              <li>Selecciona el <strong>insumo</strong> (insumo compuesto, grano propio, o insumo simple)</li>
              <li>Ingresa la <strong>cantidad</strong> perdida (kg)</li>
              <li>Selecciona el <strong>tipo</strong> (DERRAME, PERDIDA, ACCIDENTE)</li>
              <li>Ingresa el <strong>motivo</strong> (obligatorio)</li>
              <li>Agrega <strong>observaciones</strong> (obligatorio)</li>
              <li>Confirma el registro</li>
            </ol>
          </div>

          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="Info" size={20} /> Características</h5>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li><strong>Impacto Inmediato:</strong> Descuenta stock al momento del registro</li>
              <li><strong>No Modifica Consumo:</strong> No afecta los consumos ya generados</li>
              <li><strong>Validación Estricta:</strong> No permite stock negativo para derrames manuales</li>
              <li><strong>Trazabilidad Completa:</strong> Se registra usuario, fecha y hora</li>
              <li><strong>Asociado a Fecha:</strong> Se vincula al día correspondiente</li>
            </ul>

            <div style={{ backgroundColor: '#fee2e2', padding: '0.75rem', borderRadius: '0.375rem', marginTop: '0.75rem', fontSize: '0.875rem', color: '#991b1b' }}>
              <strong>⚠️ Diferencias con Consumo Automático:</strong>
              <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', marginTop: '0.5rem' }}>
                <li>Los derrames NO permiten stock negativo</li>
                <li>Los derrames impactan inmediatamente el stock</li>
                <li>Los derrames no forman parte del consumo diario</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      {/* Insumos Compuestos */}
      <div style={{ backgroundColor: 'white', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1.5rem', marginBottom: '1.5rem' }}>
        <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#374151', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Icon name="UtensilsCrossed" size={24} /> Insumos Compuestos (Recetas)
        </h4>
        <p style={{ color: '#6b7280', marginBottom: '1rem' }}>
          Los insumos compuestos son recetas/formulas de alimento que combinan diferentes ingredientes. 
          Se asignan a etapas de alimentación y el sistema las usa automáticamente para calcular el consumo diario.
        </p>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem' }}>📝 Crear una Receta</h5>
            <ol style={{ listStyle: 'decimal', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li>Ve a <strong>Alimentación → Insumos Compuestos</strong></li>
              <li>Haz clic en <strong>"Nueva Receta"</strong></li>
              <li>Define <strong>nombre</strong> (ej: "Ración Gestación")</li>
              <li>Selecciona <strong>tipo</strong> (Ración, Núcleo, Mezcla, etc.)</li>
              <li>Agrega <strong>componentes</strong>:
                <ul style={{ listStyle: 'circle', paddingLeft: '1.5rem', marginTop: '0.5rem' }}>
                  <li><strong>Insumos:</strong> Núcleos, vitaminas, antibióticos comprados</li>
                  <li><strong>Granos Propios:</strong> Maíz, soja de tus cultivos (InventarioGrano)</li>
                  <li><strong>Insumos Compuestos:</strong> Recetas dentro de recetas (sub-recetas)</li>
                </ul>
              </li>
              <li>Define <strong>porcentajes</strong> o <strong>cantidades fijas</strong></li>
              <li>El sistema <strong>calcula el costo</strong> automáticamente usando FIFO para granos propios</li>
              <li>Guarda la receta</li>
              <li><strong>Asocia a Etapa:</strong> Haz clic en el botón <strong>"Asociar a Etapa"</strong> en la lista de recetas (solo disponible para recetas tipo RACIÓN)</li>
              <li>Selecciona la <strong>etapa</strong> (GESTACION, LACTANCIA, F1, F2, F3, F4, DESARROLLO, TERMINACION)</li>
              <li>Define el <strong>consumo diario por animal</strong> (kg/día)</li>
              <li>Confirma la asociación</li>
            </ol>
          </div>

          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem' }}>💡 Ejemplo de Receta</h5>
            <div style={{ backgroundColor: '#f9fafb', padding: '1rem', borderRadius: '0.375rem', fontSize: '0.875rem' }}>
              <p style={{ fontWeight: '600', marginBottom: '0.5rem' }}>Ración Gestación:</p>
              <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', color: '#6b7280', lineHeight: '1.8' }}>
                <li>60% Maíz Propio (de InventarioGrano)</li>
                <li>30% Soja Propia (de InventarioGrano)</li>
                <li>8% Núcleo Gestación (insumo comprado)</li>
                <li>2% Vitaminas (insumo comprado)</li>
              </ul>
              <p style={{ marginTop: '0.75rem', color: '#059669', fontWeight: '500' }}>
                Costo calculado: $X.XX / kg (actualizado automáticamente)
              </p>
            </div>

            <h5 style={{ fontWeight: '600', color: '#1f2937', marginTop: '1rem', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="Share2" size={20} /> Asociación a Etapas</h5>
            <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.75rem' }}>
              Puedes asociar una receta a una etapa de dos formas:
            </p>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li><strong>Desde Insumos Compuestos:</strong> Haz clic en <strong>"Asociar a Etapa"</strong> junto a cada receta tipo RACIÓN</li>
              <li><strong>Desde Configuración:</strong> Ve a <strong>Porcinos → Configuración → Configuración del Módulo</strong>. Para asociar recetas a etapas, usa <strong>Alimentación → Insumos Compuestos → Asociar a Etapa</strong></li>
            </ul>
            <div style={{ backgroundColor: '#ecfdf5', padding: '0.75rem', borderRadius: '0.375rem', marginTop: '0.75rem', fontSize: '0.875rem', color: '#065f46' }}>
              <strong>✅ Ventajas:</strong> Puedes asociar múltiples recetas a la misma etapa. El sistema usará la receta marcada como "por defecto" o la primera activa.
            </div>
          </div>
        </div>
      </div>

      {/* Etapas de Alimentación */}
      <div style={{ backgroundColor: 'white', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1.5rem' }}>
        <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#374151', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Icon name="Clipboard" size={24} /> Etapas de Alimentación
        </h4>
        <p style={{ color: '#6b7280', marginBottom: '1rem' }}>
          Cada etapa del ciclo productivo requiere una alimentación específica. El sistema determina automáticamente 
          la etapa basándose en los eventos productivos registrados.
        </p>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1rem' }}>
          <div style={{ backgroundColor: '#f9fafb', padding: '1rem', borderRadius: '0.375rem' }}>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="Heart" size={18} /> Gestación</h5>
            <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.5rem' }}>
              Madres gestantes. Determinada automáticamente si hay gestación activa.
            </p>
            <p style={{ fontSize: '0.75rem', color: '#9ca3af', fontStyle: 'italic' }}>
              Cambia a LACTANCIA al registrar el parto
            </p>
          </div>

          <div style={{ backgroundColor: '#f9fafb', padding: '1rem', borderRadius: '0.375rem' }}>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.5rem' }}>🍼 Lactancia</h5>
            <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.5rem' }}>
              Madres en lactancia. Automática desde el parto hasta el destete.
            </p>
            <p style={{ fontSize: '0.75rem', color: '#9ca3af', fontStyle: 'italic' }}>
              Duración configurada en parámetros productivos
            </p>
          </div>

          <div style={{ backgroundColor: '#f9fafb', padding: '1rem', borderRadius: '0.375rem' }}>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.5rem' }}>F1, F2, F3, F4</h5>
            <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.5rem' }}>
              Etapas de recría. Asignadas según edad y peso del lote de recría.
            </p>
            <p style={{ fontSize: '0.75rem', color: '#9ca3af', fontStyle: 'italic' }}>
              Cada etapa tiene su receta y consumo configurado
            </p>
          </div>

          <div style={{ backgroundColor: '#f9fafb', padding: '1rem', borderRadius: '0.375rem' }}>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="Dumbbell" size={18} /> Desarrollo</h5>
            <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.5rem' }}>
              Engorde temprano. Ración para crecimiento acelerado.
            </p>
            <p style={{ fontSize: '0.75rem', color: '#9ca3af', fontStyle: 'italic' }}>
              Asignada según destino del lote
            </p>
          </div>

          <div style={{ backgroundColor: '#f9fafb', padding: '1rem', borderRadius: '0.375rem' }}>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="Target" size={18} /> Terminación</h5>
            <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.5rem' }}>
              Engorde final. Ración para acabado y peso de venta.
            </p>
            <p style={{ fontSize: '0.75rem', color: '#9ca3af', fontStyle: 'italic' }}>
              Última etapa antes de venta/faena
            </p>
          </div>
        </div>
      </div>
    </div>
  );

  const renderContenidoSanidad = () => (
    <div style={{ padding: '2rem' }}>
      <div>
        <h3 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937', marginBottom: '1rem' }}>
          <Icon name="Syringe" size={24} style={{ marginRight: '0.5rem', display: 'inline-block', verticalAlign: 'middle' }} /> Gestión de Sanidad
        </h3>
        <p style={{ color: '#6b7280', marginBottom: '2rem', lineHeight: '1.6' }}>
          El control sanitario es esencial para mantener la salud del plantel y prevenir enfermedades. 
          El sistema permite registrar eventos sanitarios con trazabilidad completa y control de retiros.
        </p>
      </div>

      <div style={{ backgroundColor: 'white', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1.5rem', marginBottom: '1.5rem' }}>
        <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#374151', marginBottom: '1rem' }}>
          <Icon name="Clipboard" size={24} style={{ marginRight: '0.5rem', display: 'inline-block', verticalAlign: 'middle' }} /> Eventos Sanitarios
        </h4>
        <p style={{ color: '#6b7280', marginBottom: '1rem' }}>
          Registra todos los eventos sanitarios: vacunaciones, tratamientos, desparasitaciones y controles de salud.
        </p>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem' }}>📝 Cómo Registrar un Evento Sanitario</h5>
            <ol style={{ listStyle: 'decimal', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li>Ve a <strong>Eventos Sanitarios</strong></li>
              <li>Haz clic en <strong>"Nuevo Evento"</strong></li>
              <li>Selecciona el <strong>tipo de evento</strong> (vacunación, tratamiento, etc.)</li>
              <li>Elige la <strong>entidad</strong> (Madre, Recría, Lote, etc.)</li>
              <li>Registra <strong>fecha</strong> del evento</li>
              <li>Selecciona el <strong>medicamento/insumo</strong> utilizado (opcional pero recomendado)</li>
              <li>Define <strong>dosis</strong> y <strong>unidad</strong> (ml, unidades, etc.)</li>
              <li>Si aplica, configura <strong>fecha de retiro</strong> (días sin consumo)</li>
              <li>Registra <strong>profesional responsable</strong></li>
              <li>Agrega <strong>observaciones</strong></li>
              <li>Guarda el evento</li>
            </ol>

            <div style={{ backgroundColor: '#ecfdf5', padding: '0.75rem', borderRadius: '0.375rem', marginTop: '0.75rem', fontSize: '0.875rem', color: '#065f46' }}>
              <strong>✅ Actualización Automática de Inventario:</strong>
              <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', marginTop: '0.5rem' }}>
                <li>Si seleccionas un <strong>insumo/medicamento</strong> y registras una <strong>dosis</strong>, 
                    el sistema <strong>descuenta automáticamente</strong> el stock del inventario</li>
                <li>Se <strong>valida stock disponible</strong> antes de registrar el evento</li>
                <li>Si no hay stock suficiente, el evento <strong>no se puede registrar</strong></li>
                <li>Se crea un <strong>movimiento de inventario</strong> para trazabilidad completa</li>
                <li>Al <strong>eliminar un evento sanitario</strong>, el stock se <strong>restaura automáticamente</strong></li>
              </ul>
            </div>
          </div>

          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="Bell" size={20} /> Control de Retiros</h5>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li><strong>Fecha de Retiro:</strong> Días sin consumo de carne/leche después del tratamiento</li>
              <li><strong>Retiros Pendientes:</strong> Aparecen en alertas y calendario</li>
              <li><strong>Retiros Vencidos:</strong> Alertas cuando ya se cumplió el retiro</li>
              <li><strong>Marcar Retiro Cumplido:</strong> Indica que se respetó el período de retiro</li>
              <li>El sistema calcula automáticamente las fechas de retiro según configuración</li>
            </ul>

            <h5 style={{ fontWeight: '600', color: '#1f2937', marginTop: '1rem', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="BarChart" size={20} /> Tipos de Eventos</h5>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li>Vacunaciones preventivas</li>
              <li>Tratamientos terapéuticos</li>
              <li>Desparasitaciones</li>
              <li>Controles de salud</li>
              <li>Quimioprofilaxis</li>
              <li>Otras intervenciones sanitarias</li>
            </ul>
          </div>
        </div>
      </div>

      <div style={{ backgroundColor: '#eff6ff', border: '1px solid #3b82f6', borderRadius: '0.5rem', padding: '1.5rem' }}>
        <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#1e40af', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Icon name="Info" size={24} /> Importancia de la Trazabilidad Sanitaria
        </h4>
        <p style={{ color: '#1e3a8a', marginBottom: '1rem', fontSize: '0.875rem' }}>
          El registro completo de eventos sanitarios es fundamental para:
        </p>
        <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#1e3a8a', lineHeight: '1.8' }}>
          <li>Cumplimiento de normativas sanitarias</li>
          <li>Trazabilidad de productos (carnes, leche)</li>
          <li>Control de retiros de medicamentos</li>
          <li>Historial médico completo de cada animal</li>
          <li>Análisis de eficacia de tratamientos</li>
          <li>Prevención de problemas de salud recurrente</li>
          <li><strong>Control preciso del inventario de medicamentos y vacunas</strong></li>
        </ul>
      </div>

      <div style={{ backgroundColor: '#ecfdf5', border: '1px solid #10b981', borderRadius: '0.5rem', padding: '1.5rem', marginTop: '1.5rem' }}>
        <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#065f46', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Icon name="Package" size={24} /> Gestión Automática de Inventario
        </h4>
        <p style={{ color: '#047857', marginBottom: '1rem', fontSize: '0.875rem' }}>
          El sistema actualiza automáticamente el inventario cuando registras eventos sanitarios con medicamentos:
        </p>
        <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#047857', lineHeight: '1.8' }}>
          <li><strong>Validación de Stock:</strong> Verifica que haya stock suficiente antes de permitir registrar el evento</li>
          <li><strong>Descuento Automático:</strong> Si seleccionas un insumo/medicamento y registras dosis, se descuenta automáticamente del inventario</li>
          <li><strong>Movimientos Registrados:</strong> Cada descuento crea un movimiento de inventario para auditoría completa</li>
          <li><strong>Restauración al Eliminar:</strong> Si eliminas un evento sanitario, el stock se restaura automáticamente</li>
          <li><strong>Unidades:</strong> La dosis debe estar en la misma unidad que el insumo (ml, unidades, etc.)</li>
          <li><strong>Requisitos:</strong> Para que se descuente stock, debes seleccionar un insumo Y registrar una dosis</li>
        </ul>
        <div style={{ backgroundColor: 'white', padding: '0.75rem', borderRadius: '0.375rem', marginTop: '0.75rem', fontSize: '0.875rem', color: '#065f46' }}>
          <strong>💡 Nota:</strong> Si no seleccionas un insumo o no registras dosis, el evento se guarda igual pero no afecta el inventario. 
          Esto permite registrar eventos sanitarios sin consumo de medicamentos (ej: controles de salud).
        </div>
      </div>
    </div>
  );

  const renderContenidoProduccion = () => (
    <div style={{ padding: '2rem' }}>
      <div>
        <h3 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937', marginBottom: '1rem' }}>
          <Icon name="PiggyBank" size={24} style={{ marginRight: '0.5rem', display: 'inline-block', verticalAlign: 'middle' }} /> Gestión de Producción
        </h3>
        <p style={{ color: '#6b7280', marginBottom: '2rem', lineHeight: '1.6' }}>
          Gestiona lotes, recría, engorde y ventas de tu producción porcina.
        </p>
      </div>

      {/* Recría */}
      <div style={{ backgroundColor: 'white', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1.5rem', marginBottom: '1.5rem' }}>
        <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#374151', marginBottom: '1rem' }}>
          <Icon name="PiggyBank" size={24} style={{ marginRight: '0.5rem', display: 'inline-block', verticalAlign: 'middle' }} /> Recría
        </h4>
        <p style={{ color: '#6b7280', marginBottom: '1rem' }}>
          La recría se crea automáticamente al registrar un parto. Gestiona los lechones desde el destete hasta la venta o engorde.
        </p>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem' }}>📝 Gestión de Recría</h5>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li><strong>Ingreso Automático:</strong> Al registrar parto con lechones vivos</li>
              <li><strong>Etapas:</strong> F1, F2, F3, F4 según edad y peso</li>
              <li><strong>Registro de Muertes:</strong> Actualiza cantidad de animales</li>
              <li><strong>Destino:</strong> Venta, Futura Madre, o Engorde</li>
            </ul>
          </div>

          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="Bell" size={20} /> Destete Programado</h5>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li>El sistema calcula automáticamente la fecha de destete</li>
              <li>Aparece en el calendario como alerta</li>
              <li>Fecha = Fecha Parto + Días de Lactancia</li>
            </ul>
          </div>
        </div>
      </div>

      {/* Engorde */}
      <div style={{ backgroundColor: 'white', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1.5rem', marginBottom: '1.5rem' }}>
        <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#374151', marginBottom: '1rem' }}>
          <Icon name="Dumbbell" size={24} style={{ marginRight: '0.5rem', display: 'inline-block', verticalAlign: 'middle' }} /> Engorde
        </h4>
        <p style={{ color: '#6b7280', marginBottom: '1rem' }}>
          Los animales destinados a engorde pasan por las etapas de Desarrollo y Terminación.
        </p>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="BarChart" size={20} /> Etapas de Engorde</h5>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li><strong>Desarrollo:</strong> Crecimiento acelerado</li>
              <li><strong>Terminación:</strong> Acabado y peso de venta</li>
              <li>Cada etapa tiene su receta de alimentación</li>
            </ul>
          </div>

          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="TrendingUp" size={20} /> Seguimiento</h5>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li>Peso promedio del lote</li>
              <li>Consumo de alimento</li>
              <li>Conversión alimenticia</li>
              <li>Días en engorde</li>
            </ul>
          </div>
        </div>
      </div>

      {/* Faena */}
      <div style={{ backgroundColor: 'white', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1.5rem' }}>
        <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#374151', marginBottom: '1rem' }}>
          <Icon name="Scissors" size={24} style={{ marginRight: '0.5rem', display: 'inline-block', verticalAlign: 'middle' }} /> Faena
        </h4>
        <p style={{ color: '#6b7280', marginBottom: '1rem' }}>
          Registra las faenas realizadas para calcular rendimientos y ingresos.
        </p>

        <div>
          <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem' }}>📝 Cómo Registrar una Faena</h5>
          <ol style={{ listStyle: 'decimal', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
            <li>Ve a <strong>Faena</strong></li>
            <li>Haz clic en <strong>"Nueva Faena"</strong></li>
            <li>Selecciona el <strong>lote de recría</strong></li>
            <li>Registra <strong>fecha de envío</strong></li>
            <li>Ingresa <strong>cantidad de animales</strong></li>
            <li>Registra <strong>peso total de envío</strong></li>
            <li>Después de la faena, ingresa <strong>peso de faena</strong></li>
            <li>El sistema calcula <strong>rendimiento</strong> automáticamente</li>
            <li>Ingresa <strong>precio por kg</strong> si aplica</li>
            <li>Guarda la faena</li>
          </ol>
        </div>
      </div>
    </div>
  );

  const renderContenidoConfiguracion = () => (
    <div style={{ padding: '2rem' }}>
      <div>
        <h3 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937', marginBottom: '1rem' }}>
          <Icon name="Settings" size={24} style={{ marginRight: '0.5rem', display: 'inline-block', verticalAlign: 'middle' }} /> Configuración del Módulo Porcinos
        </h3>
        <p style={{ color: '#6b7280', marginBottom: '1rem', lineHeight: '1.6' }}>
          Configura los parámetros productivos y económicos que el sistema utiliza en todos los cálculos.
        </p>
        <div style={{ backgroundColor: '#eff6ff', padding: '1rem', borderRadius: '0.5rem', marginBottom: '2rem', border: '1px solid #bfdbfe' }}>
          <h4 style={{ fontSize: '1rem', fontWeight: '600', color: '#1e40af', marginBottom: '0.5rem' }}>Dónde está</h4>
          <p style={{ fontSize: '0.875rem', color: '#1e3a8a', marginBottom: '0.75rem' }}>
            Menú <strong>Porcinos → Configuración</strong>. En la pestaña <strong>Configuración del Módulo</strong> se abre la pantalla &quot;Configuraciones del Módulo Porcinos&quot; con las siguientes pestañas:
          </p>
          <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#1e3a8a', lineHeight: '1.8' }}>
            <li><strong>Establecimiento:</strong> Parámetros del establecimiento (unidad de manejo, nombre).</li>
            <li><strong>Productivos:</strong> Días de gestación, lactancia, control celo, alertas (partos, destetes, ecografías, pasaje maternidad, revisiones sanitarias).</li>
            <li><strong>Económicos:</strong> Datos económicos (costos por etapa, precios, método de imputación de granos propios).</li>
            <li><strong>Razas:</strong> Catálogo de razas porcinas.</li>
            <li><strong>Servicios:</strong> Tipos de servicio (monta natural, IA, etc.).</li>
            <li><strong>Mortalidad:</strong> Causas de mortalidad.</li>
            <li><strong>Motivos Baja:</strong> Motivos de baja de animales.</li>
            <li><strong>Sanitarios:</strong> Esquemas sanitarios.</li>
            <li><strong>Tipos Parto:</strong> Tipos de parto.</li>
            <li><strong>Ubicaciones:</strong> Ubicaciones internas (galpón, sala, corral).</li>
            <li><strong>Generales:</strong> Configuraciones clave/valor generales del módulo.</li>
          </ul>
          <p style={{ fontSize: '0.875rem', color: '#1e3a8a', marginTop: '0.5rem' }}>
            Además, en <strong>Porcinos → Configuración</strong> tienes las pestañas <strong>Usuarios</strong> (gestión de usuarios de la empresa) y <strong>Configuración General</strong> (parámetros clave/valor del sistema).
          </p>
        </div>
      </div>

      {/* Parámetros Productivos */}
      <div style={{ backgroundColor: 'white', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1.5rem', marginBottom: '1.5rem' }}>
        <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#374151', marginBottom: '1rem' }}>
          <Icon name="BarChart" size={24} style={{ marginRight: '0.5rem', display: 'inline-block', verticalAlign: 'middle' }} /> Parámetros Productivos
        </h4>
        <p style={{ color: '#6b7280', marginBottom: '1rem' }}>
          Define los tiempos y valores estándar del ciclo productivo.
        </p>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="RefreshCw" size={20} /> Ciclo Reproductivo</h5>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li><strong>Días de Gestación:</strong> 115 días (promedio)</li>
              <li><strong>Días de Lactancia:</strong> 21 días (promedio)</li>
              <li><strong>Días Control Celo:</strong> 21 días después del servicio</li>
              <li><strong>Días Entre Celos:</strong> 21 días</li>
              <li><strong>Días Pasaje Maternidad:</strong> 7 días antes del parto</li>
            </ul>
          </div>

          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem' }}>🔔 Alertas y Recordatorios</h5>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li><strong>Días Antelación - Partos:</strong> Cuántos días antes alertar</li>
              <li><strong>Días Antelación - Ecografías:</strong> Cuántos días antes alertar</li>
              <li><strong>Días Antelación - Destetes:</strong> Cuántos días antes alertar</li>
              <li><strong>Días Antelación - Pasaje Maternidad:</strong> Cuántos días antes alertar</li>
              <li><strong>Días Antelación - Revisiones Sanitarias:</strong> Cuántos días antes alertar</li>
            </ul>
          </div>
        </div>
      </div>

      {/* Datos Económicos */}
      <div style={{ backgroundColor: 'white', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1.5rem', marginBottom: '1.5rem' }}>
        <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#374151', marginBottom: '1rem' }}>
          <Icon name="DollarSign" size={24} style={{ marginRight: '0.5rem', display: 'inline-block', verticalAlign: 'middle' }} /> Datos Económicos
        </h4>
        <p style={{ color: '#6b7280', marginBottom: '1rem' }}>
          Configura precios y costos para cálculos económicos y análisis de rentabilidad. 
          Los costos de alimentación se calculan automáticamente desde las recetas.
        </p>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="BarChart" size={20} /> Parámetros Configurables</h5>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li>Costos por etapa (gestación, lactancia, engorde)</li>
              <li>Costos de insumos (para insumos comprados)</li>
              <li>Costos de mano de obra</li>
              <li>Precios de venta por categoría</li>
              <li>Precio manual de cultivos (si aplica)</li>
            </ul>
          </div>

          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="Settings" size={20} /> Método de Imputación de Costos</h5>
            <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.75rem' }}>
              Para granos propios (cultivos), puedes elegir cómo calcular el costo:
            </p>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li><strong>PRECIO_MANUAL:</strong> Usa el precio configurado manualmente</li>
              <li><strong>PRECIO_MERCADO:</strong> Usa precio de mercado (si está disponible)</li>
              <li><strong>PROMEDIO_PONDERADO:</strong> Calcula desde InventarioGrano usando método FIFO</li>
            </ul>
          </div>
        </div>
      </div>

      {/* Ubicaciones Internas */}
      <div style={{ backgroundColor: 'white', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1.5rem', marginBottom: '1.5rem' }}>
        <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#374151', marginBottom: '1rem' }}>
          <Icon name="MapPin" size={24} style={{ marginRight: '0.5rem', display: 'inline-block', verticalAlign: 'middle' }} /> Ubicaciones Internas
        </h4>
        <p style={{ color: '#6b7280', marginBottom: '1rem' }}>
          El sistema usa <strong>Ubicaciones Internas</strong> para organizar la infraestructura del establecimiento. 
          Esta es la forma unificada de gestionar galpones, salas y corrales.
        </p>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem' }}>📐 Jerarquía de Ubicaciones</h5>
            <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.75rem' }}>
              Las ubicaciones se organizan en tres niveles jerárquicos:
            </p>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li><strong>GALPON:</strong> Nivel más alto (edificio completo)</li>
              <li><strong>SALA:</strong> Nivel intermedio (sala dentro del galpón)</li>
              <li><strong>CORRAL:</strong> Nivel más bajo (corral dentro de la sala)</li>
            </ul>
            <div style={{ backgroundColor: '#eff6ff', padding: '0.75rem', borderRadius: '0.375rem', marginTop: '0.75rem', fontSize: '0.875rem', color: '#1e40af' }}>
              <strong>💡 Ejemplo:</strong> Galpón 1 → Sala Maternidad A → Corral 3
            </div>
          </div>

          <div>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem' }}>📝 Tipos de Ubicación</h5>
            <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.75rem' }}>
              Cada ubicación puede tener un tipo específico:
            </p>
            <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li><strong>MATERNIDAD:</strong> Sala/corral de maternidad</li>
              <li><strong>GESTACION:</strong> Sala/corral de gestación</li>
              <li><strong>RECRIA:</strong> Sala/corral de recría</li>
              <li><strong>ENGORDE:</strong> Sala/corral de engorde</li>
              <li><strong>AISLAMIENTO:</strong> Aislamiento/cuarentena</li>
              <li><strong>GENERAL:</strong> Uso general</li>
            </ul>
          </div>
        </div>

        <div style={{ marginTop: '1rem' }}>
          <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem' }}>🔧 Cómo Gestionar Ubicaciones</h5>
          <ol style={{ listStyle: 'decimal', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
            <li>Ve a <strong>Porcinos → Configuración → Configuración del Módulo</strong></li>
            <li>Selecciona la pestaña <strong>Ubicaciones</strong></li>
            <li>Haz clic en <strong>"Nueva Ubicación"</strong></li>
            <li>Define <strong>nombre</strong> (ej: "Galpón 1")</li>
            <li>Selecciona <strong>nivel</strong> (GALPON, SALA, CORRAL)</li>
            <li>Si es SALA o CORRAL, selecciona la <strong>ubicación padre</strong></li>
            <li>Define <strong>tipo de ubicación</strong> (opcional pero recomendado)</li>
            <li>Establece <strong>capacidad máxima</strong> si aplica</li>
            <li>Guarda la ubicación</li>
          </ol>
        </div>
      </div>

      {/* Configuración de Recetas por Etapa */}
      <div style={{ backgroundColor: 'white', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1.5rem' }}>
        <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#374151', marginBottom: '1rem' }}>
          <Icon name="UtensilsCrossed" size={24} style={{ marginRight: '0.5rem', display: 'inline-block', verticalAlign: 'middle' }} /> Configuración de Recetas por Etapa
        </h4>
        <p style={{ color: '#6b7280', marginBottom: '1rem' }}>
          Configura qué receta usar y cuánto alimento consumir por animal en cada etapa. 
          Esta configuración es esencial para el sistema de consumo diario automático.
        </p>

        <div>
          <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.75rem' }}>📝 Cómo Asociar Recetas a Etapas</h5>
          <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.75rem' }}>
            Existen dos formas de asociar recetas a etapas de alimentación:
          </p>
          
          <div style={{ marginBottom: '1rem' }}>
            <h6 style={{ fontWeight: '600', color: '#374151', marginBottom: '0.5rem' }}>Método 1: Desde Insumos Compuestos (Recomendado)</h6>
            <ol style={{ listStyle: 'decimal', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li>Ve a <strong>Alimentación → Insumos Compuestos</strong></li>
              <li>Encuentra la receta que quieres asociar (debe ser tipo <strong>RACIÓN</strong>)</li>
              <li>Haz clic en el botón <strong>"Asociar a Etapa"</strong> (icono Share2, de color morado)</li>
              <li>Selecciona la <strong>etapa de alimentación</strong>:
                <ul style={{ listStyle: 'circle', paddingLeft: '1.5rem', marginTop: '0.5rem' }}>
                  <li><strong>GESTACION:</strong> Madres gestantes</li>
                  <li><strong>LACTANCIA:</strong> Madres en lactancia</li>
                  <li><strong>F1, F2, F3, F4:</strong> Etapas de recría (0-140 días)</li>
                  <li><strong>DESARROLLO:</strong> Engorde temprano (141-180 días)</li>
                  <li><strong>TERMINACION:</strong> Engorde final ({'>'}180 días)</li>
                </ul>
              </li>
              <li>Define el <strong>consumo diario por animal</strong> (kg/día)</li>
              <li>Confirma la asociación</li>
            </ol>
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <h6 style={{ fontWeight: '600', color: '#374151', marginBottom: '0.5rem' }}>Método 2: Desde Configuración</h6>
            <ol style={{ listStyle: 'decimal', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
              <li>Ve a <strong>Porcinos → Configuración → Configuración del Módulo</strong></li>
              <li>En la pestaña <strong>Generales</strong> o en <strong>Productivos</strong> puedes tener configuraciones relacionadas con alimentación por etapa</li>
              <li>O bien asocia recetas a etapas desde <strong>Alimentación → Insumos Compuestos → Asociar a Etapa</strong> (recomendado)</li>
              <li>Para cada etapa, selecciona la <strong>receta</strong> y define el <strong>consumo diario</strong> por animal</li>
              <li>Guarda la configuración</li>
            </ol>
          </div>

          <div style={{ backgroundColor: '#f0fdf4', padding: '1rem', borderRadius: '0.375rem', marginTop: '1rem', fontSize: '0.875rem', color: '#166534' }}>
            <strong>💡 Ejemplo:</strong> Si tienes 10 madres en GESTACION y asociaste la receta "Ración Gestación" con 2.5 kg/día, 
            el sistema generará automáticamente 25 kg de esa receta por día (10 madres × 2.5 kg/día).
          </div>

          <div style={{ backgroundColor: '#eff6ff', padding: '0.75rem', borderRadius: '0.375rem', marginTop: '0.75rem', fontSize: '0.875rem', color: '#1e40af' }}>
            <strong>ℹ️ Nota:</strong> Puedes tener múltiples recetas asociadas a la misma etapa. El sistema usará la receta marcada como "por defecto" o la primera activa encontrada.
          </div>
        </div>
      </div>
    </div>
  );

  const renderContenidoReportes = () => (
    <div style={{ padding: '2rem' }}>
      <div>
        <h3 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937', marginBottom: '1rem' }}>
          <Icon name="BarChart" size={24} style={{ marginRight: '0.5rem', display: 'inline-block', verticalAlign: 'middle' }} /> Reportes y Análisis
        </h3>
        <p style={{ color: '#6b7280', marginBottom: '2rem', lineHeight: '1.6' }}>
          El sistema genera reportes completos basados en el consumo diario automático y los datos productivos 
          registrados. Todos los reportes pueden exportarse a Excel, PDF o CSV.
        </p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '1.5rem', marginBottom: '1.5rem' }}>
        {/* Reporte Reproductivo */}
        <div style={{ backgroundColor: 'white', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1.5rem' }}>
          <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#374151', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Icon name="Baby" size={24} /> Reporte Reproductivo
          </h4>
          <p style={{ color: '#6b7280', marginBottom: '1rem', fontSize: '0.875rem' }}>
            Analiza el ciclo reproductivo completo con servicios, gestaciones, partos y destetes.
          </p>
          <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
            <li>Listado detallado de servicios</li>
            <li>Gestaciones activas y finalizadas</li>
            <li>Detalle de partos con estadísticas</li>
            <li>Destetes realizados</li>
            <li>Índices: Tasa de parición, lechones vivos/parto, lechones destetados/parto</li>
          </ul>
          <p style={{ fontSize: '0.75rem', color: '#9ca3af', marginTop: '0.75rem', fontStyle: 'italic' }}>
            Requiere rango de fechas
          </p>
        </div>

        {/* Reporte de Mortalidad */}
        <div style={{ backgroundColor: 'white', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1.5rem' }}>
          <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#374151', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Icon name="Skull" size={24} /> Reporte de Mortalidad
          </h4>
          <p style={{ color: '#6b7280', marginBottom: '1rem', fontSize: '0.875rem' }}>
            Análisis detallado de muertes por etapa y causa de mortalidad.
          </p>
          <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
            <li>Muertes de madres</li>
            <li>Muertes de lechones por etapa</li>
            <li>Muertes en recría</li>
            <li>Agrupación por causa</li>
            <li>Estadísticas y porcentajes</li>
          </ul>
          <p style={{ fontSize: '0.75rem', color: '#9ca3af', marginTop: '0.75rem', fontStyle: 'italic' }}>
            Requiere rango de fechas
          </p>
        </div>

        {/* Reporte Productivo */}
        <div style={{ backgroundColor: 'white', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1.5rem' }}>
          <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#374151', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Icon name="TrendingUp" size={24} /> Reporte Productivo
          </h4>
          <p style={{ color: '#6b7280', marginBottom: '1rem', fontSize: '0.875rem' }}>
            KPIs e índices productivos comparados con objetivos configurados.
          </p>
          <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
            <li>Total de madres y padrillos activos</li>
            <li>Gestaciones activas</li>
            <li>Partos próximos (7 días)</li>
            <li>Tasa de parición</li>
            <li>Lechones vivos por parto</li>
            <li>Lechones destetados por parto</li>
            <li>Comparación con objetivos</li>
          </ul>
          <p style={{ fontSize: '0.75rem', color: '#9ca3af', marginTop: '0.75rem', fontStyle: 'italic' }}>
            Fechas opcionales - Sin fechas muestra datos actuales; con fechas filtra servicios, partos y destetes por el rango seleccionado
          </p>
        </div>

        {/* Reporte de Alimentación */}
        <div style={{ backgroundColor: 'white', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1.5rem' }}>
          <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#374151', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Icon name="Wheat" size={24} /> Reporte de Alimentación
          </h4>
          <p style={{ color: '#6b7280', marginBottom: '1rem', fontSize: '0.875rem' }}>
            Análisis completo del consumo de alimento basado en el sistema automático diario.
          </p>
          <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
            <li>Consumo diario por fecha</li>
            <li>Consumo total de recetas (kg)</li>
            <li>Consumo total de insumos (kg)</li>
            <li>Agrupación por etapa de alimentación</li>
            <li>Agrupación por receta utilizada</li>
            <li>Detalle de consumos por día</li>
            <li>Estados de confirmación de días</li>
          </ul>
          <p style={{ fontSize: '0.75rem', color: '#9ca3af', marginTop: '0.75rem', fontStyle: 'italic' }}>
            Requiere rango de fechas - Basado en consumo diario automático
          </p>
        </div>

        {/* Reporte Económico */}
        <div style={{ backgroundColor: 'white', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1.5rem' }}>
          <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#374151', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Icon name="DollarSign" size={24} /> Reporte Económico
          </h4>
          <p style={{ color: '#6b7280', marginBottom: '1rem', fontSize: '0.875rem' }}>
            Análisis financiero completo con costos de alimentación calculados desde recetas y componentes.
          </p>
          <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
            <li>Ventas realizadas en el período</li>
            <li>Ingresos totales</li>
            <li><strong>Costos de alimentación:</strong> Calculados desde consumo diario automático</li>
            <li><strong>Costo por receta:</strong> Basado en componentes y precios actuales</li>
            <li><strong>Costo de granos propios:</strong> Calculado con método FIFO desde InventarioGrano</li>
            <li>Costos de mano de obra</li>
            <li>Balance neto y margen de beneficio</li>
          </ul>
          <p style={{ fontSize: '0.75rem', color: '#9ca3af', marginTop: '0.75rem', fontStyle: 'italic' }}>
            Requiere rango de fechas - Costos calculados automáticamente
          </p>
        </div>

        {/* Reporte de Inventario */}
        <div style={{ backgroundColor: 'white', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1.5rem' }}>
          <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#374151', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Icon name="Clipboard" size={24} /> Reporte de Inventario
          </h4>
          <p style={{ color: '#6b7280', marginBottom: '1rem', fontSize: '0.875rem' }}>
            Stock actual de animales por etapa y ubicación.
          </p>
          <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
            <li>Total de madres por estado</li>
            <li>Total de padrillos</li>
            <li>Total de animales en recría</li>
            <li>Detalle de madres con información completa</li>
            <li>Detalle de padrillos</li>
            <li>Detalle de recrías activas</li>
          </ul>
          <p style={{ fontSize: '0.75rem', color: '#9ca3af', marginTop: '0.75rem', fontStyle: 'italic' }}>
            Fechas opcionales - Sin fechas muestra estado actual completo; con fechas filtra recrías por fecha de ingreso en el rango seleccionado
          </p>
        </div>

        {/* Reporte Sanitario */}
        <div style={{ backgroundColor: 'white', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1.5rem' }}>
          <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#374151', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Icon name="Syringe" size={24} /> Reporte Sanitario
          </h4>
          <p style={{ color: '#6b7280', marginBottom: '1rem', fontSize: '0.875rem' }}>
            Eventos sanitarios y tratamientos aplicados.
          </p>
          <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
            <li>Listado de eventos sanitarios</li>
            <li>Agrupación por tipo de evento</li>
            <li>Retiros pendientes</li>
            <li>Retiros vencidos</li>
            <li>Detalle de tratamientos aplicados</li>
          </ul>
          <p style={{ fontSize: '0.75rem', color: '#9ca3af', marginTop: '0.75rem', fontStyle: 'italic' }}>
            Requiere rango de fechas
          </p>
        </div>

        {/* Reporte de Ventas */}
        <div style={{ backgroundColor: 'white', border: '1px solid #e5e7eb', borderRadius: '0.5rem', padding: '1.5rem' }}>
          <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#374151', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Icon name="ShoppingCart" size={24} /> Reporte de Ventas
          </h4>
          <p style={{ color: '#6b7280', marginBottom: '1rem', fontSize: '0.875rem' }}>
            Análisis detallado de ventas realizadas y análisis de ingresos.
          </p>
          <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#6b7280', lineHeight: '1.8' }}>
            <li>Listado completo de ventas</li>
            <li>Ingresos totales</li>
            <li>Precio promedio por kg</li>
            <li>Peso promedio vendido</li>
            <li>Agrupación por cliente</li>
            <li>Agrupación por tipo de venta</li>
            <li>Cantidad total de animales vendidos</li>
          </ul>
          <p style={{ fontSize: '0.75rem', color: '#9ca3af', marginTop: '0.75rem', fontStyle: 'italic' }}>
            Requiere rango de fechas
          </p>
        </div>
      </div>

      {/* Uso de Reportes */}
      <div style={{ backgroundColor: '#ecfdf5', border: '2px solid #10b981', borderRadius: '0.5rem', padding: '1.5rem', marginTop: '1.5rem' }}>
        <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#065f46', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Icon name="Zap" size={24} /> Cómo Generar Reportes
        </h4>
        <p style={{ color: '#047857', marginBottom: '1rem', fontSize: '0.875rem', fontWeight: '500' }}>
          Todos los reportes tienen un botón <strong>"Generar Reporte"</strong> siempre visible. 
          Algunos reportes requieren fechas obligatorias, otros las aceptan como opcionales.
        </p>
        <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#047857', lineHeight: '1.8' }}>
          <li><strong>Reportes con fechas obligatorias (*):</strong> Reproductivo, Mortalidad, Alimentación, Económico, Sanitario, Ventas</li>
          <li><strong>Reportes con fechas opcionales:</strong> Productivo (filtra servicios/partos/destetes), Inventario (filtra recrías por fecha de ingreso)</li>
          <li>Los campos de fecha siempre están visibles. Si un reporte requiere fechas, aparecen marcadas con <strong>*</strong>; si son opcionales, se muestra <strong>(opcional)</strong></li>
          <li>Si un reporte no requiere fechas pero las ingresas, el sistema las usará para filtrar datos históricos</li>
        </ul>
      </div>

      {/* Funcionalidades de Exportación */}
      <div style={{ backgroundColor: '#eff6ff', border: '1px solid #3b82f6', borderRadius: '0.5rem', padding: '1.5rem', marginTop: '1.5rem' }}>
        <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#1e40af', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Icon name="Download" size={24} /> Exportación de Reportes
        </h4>
        <p style={{ color: '#1e3a8a', marginBottom: '1rem', fontSize: '0.875rem' }}>
          Todos los reportes pueden exportarse en diferentes formatos para su análisis externo o archivo. 
          Los botones de exportación aparecen después de generar un reporte.
        </p>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
          <div style={{ backgroundColor: 'white', padding: '1rem', borderRadius: '0.375rem' }}>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="FileSpreadsheet" size={18} /> Excel</h5>
            <p style={{ fontSize: '0.875rem', color: '#6b7280' }}>
              Formato .xlsx con todas las tablas y datos del reporte
            </p>
          </div>
          <div style={{ backgroundColor: 'white', padding: '1rem', borderRadius: '0.375rem' }}>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="FileText" size={18} /> PDF</h5>
            <p style={{ fontSize: '0.875rem', color: '#6b7280' }}>
              Documento PDF listo para imprimir o compartir
            </p>
          </div>
          <div style={{ backgroundColor: 'white', padding: '1rem', borderRadius: '0.375rem' }}>
            <h5 style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="FileSpreadsheet" size={18} /> CSV</h5>
            <p style={{ fontSize: '0.875rem', color: '#6b7280' }}>
              Formato CSV para importar en hojas de cálculo
            </p>
          </div>
        </div>
      </div>

      {/* Información sobre Costos */}
      <div style={{ backgroundColor: '#fef3c7', border: '1px solid #f59e0b', borderRadius: '0.5rem', padding: '1.5rem', marginTop: '1.5rem' }}>
        <h4 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#92400e', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Icon name="Info" size={24} /> Cálculo de Costos en Reportes
        </h4>
        <p style={{ color: '#78350f', marginBottom: '1rem', fontSize: '0.875rem' }}>
          Los reportes económicos calculan los costos de alimentación de manera inteligente:
        </p>
        <ul style={{ listStyle: 'disc', paddingLeft: '1.5rem', fontSize: '0.875rem', color: '#78350f', lineHeight: '1.8' }}>
          <li><strong>Recetas:</strong> Se usa el costo unitario de la receta (manual o calculado desde componentes)</li>
          <li><strong>Componentes de Recetas:</strong> Si no hay costo, se calcula desde componentes usando precios actuales</li>
          <li><strong>Granos Propios:</strong> Se usa método FIFO desde InventarioGrano para obtener costos reales</li>
          <li><strong>Insumos Comprados:</strong> Se usa el precio unitario configurado en el insumo</li>
          <li><strong>Sub-recetas:</strong> Se calcula recursivamente si una receta contiene otras recetas</li>
          <li><strong>Método de Imputación:</strong> Configurable en Datos Económicos (PRECIO_MANUAL, PRECIO_MERCADO, PROMEDIO_PONDERADO)</li>
        </ul>
      </div>
    </div>
  );

  const renderContenido = () => {
    switch (seccionActiva) {
      case 'general':
        return renderContenidoGeneral();
      case 'reproduccion':
        return renderContenidoReproduccion();
      case 'alimentacion':
        return renderContenidoAlimentacion();
      case 'sanidad':
        return renderContenidoSanidad();
      case 'produccion':
        return renderContenidoProduccion();
      case 'configuracion':
        return renderContenidoConfiguracion();
      case 'reportes':
        return renderContenidoReportes();
      default:
        return renderContenidoGeneral();
    }
  };

  return (
    <div style={{ minHeight: '100vh', backgroundColor: '#f9fafb', padding: '2rem' }}>
      <div style={{ maxWidth: '1400px', margin: '0 auto' }}>
        <div style={{ marginBottom: '2rem' }}>
          <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937', marginBottom: '0.5rem' }}>
            <Icon name="Book" size={32} style={{ marginRight: '0.5rem', display: 'inline-block', verticalAlign: 'middle' }} /> Ayuda del Módulo de Porcinos
          </h1>
          <p style={{ color: '#6b7280' }}>
            Guía completa de funcionalidades y uso del módulo de Porcinos
          </p>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 3fr', gap: '1.5rem' }}>
          {/* Navegación lateral */}
          <div>
            <div style={{ backgroundColor: 'white', borderRadius: '0.5rem', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
              <div style={{ padding: '1rem', borderBottom: '1px solid #e5e7eb' }}>
                <h2 style={{ fontSize: '1.125rem', fontWeight: '600', color: '#1f2937' }}>Navegación</h2>
              </div>
              <nav style={{ padding: '0.5rem 0' }}>
                {secciones.map((seccion) => (
                  <button
                    key={seccion.id}
                    onClick={() => setSeccionActiva(seccion.id)}
                    style={{
                      width: '100%',
                      textAlign: 'left',
                      padding: '0.75rem 1rem',
                      fontSize: '0.875rem',
                      fontWeight: '500',
                      border: 'none',
                      borderLeft: seccionActiva === seccion.id ? '4px solid #3b82f6' : '4px solid transparent',
                      backgroundColor: seccionActiva === seccion.id ? '#eff6ff' : 'transparent',
                      color: seccionActiva === seccion.id ? '#1e40af' : '#6b7280',
                      cursor: 'pointer',
                      transition: 'all 0.2s',
                    }}
                    onMouseEnter={(e) => {
                      if (seccionActiva !== seccion.id) {
                        e.currentTarget.style.backgroundColor = '#f9fafb';
                      }
                    }}
                    onMouseLeave={(e) => {
                      if (seccionActiva !== seccion.id) {
                        e.currentTarget.style.backgroundColor = 'transparent';
                      }
                    }}
                  >
                    {typeof seccion.icon === 'string' && /^[A-Z]/.test(seccion.icon) ? (
                      <Icon name={seccion.icon as any} size={20} style={{ marginRight: '0.5rem', display: 'inline-block' }} />
                    ) : (
                      <span style={{ marginRight: '0.5rem' }}>{seccion.icon}</span>
                    )}
                    {seccion.label}
                  </button>
                ))}
              </nav>
            </div>
          </div>

          {/* Contenido principal */}
          <div>
            <div style={{ backgroundColor: 'white', borderRadius: '0.5rem', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
              {renderContenido()}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AyudaPorcinosScreen;









