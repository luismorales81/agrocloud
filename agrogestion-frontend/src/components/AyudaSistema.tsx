import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/Card';
import Button from './ui/Button';

const AyudaSistema: React.FC = () => {
  const [seccionActiva, setSeccionActiva] = useState('general');

  const secciones = [
    { id: 'general', label: 'General', icon: '🏠' },
    { id: 'produccion', label: 'Producción', icon: '🌱' },
    { id: 'recursos', label: 'Recursos', icon: '📦' },
    { id: 'finanzas', label: 'Finanzas', icon: '💰' },
    { id: 'reportes', label: 'Reportes', icon: '📊' },
    { id: 'administracion', label: 'Administración', icon: '⚙️' },
    { id: 'roles', label: 'Roles y Permisos', icon: '👥' }
  ];

  const renderContenidoGeneral = () => (
    <div className="space-y-6">
      <div>
        <h3 className="text-xl font-bold text-gray-800 mb-4">🌐 Bienvenido a AgroCloud</h3>
        <p className="text-gray-600 mb-4">
          AgroCloud es un sistema integral de gestión agropecuaria diseñado para optimizar 
          la administración de tu empresa rural. Esta guía te ayudará a aprovechar al máximo 
          todas las funcionalidades disponibles.
        </p>
      </div>

      <div>
        <h4 className="text-lg font-semibold text-gray-700 mb-3">🎯 Objetivos del Sistema</h4>
        <ul className="list-disc list-inside space-y-2 text-gray-600">
          <li>Gestionar campos, lotes y cultivos de manera eficiente</li>
          <li>Controlar inventarios de insumos y maquinaria</li>
          <li>Registrar y monitorear labores agrícolas</li>
          <li>Realizar seguimiento financiero y análisis de rentabilidad</li>
          <li>Generar reportes operativos y financieros</li>
          <li>Administrar usuarios y permisos por empresa</li>
        </ul>
      </div>

      <div>
        <h4 className="text-lg font-semibold text-gray-700 mb-3">🚀 Guía de Configuración Inicial</h4>
        <div className="bg-blue-50 p-4 rounded-lg mb-4">
          <p className="text-blue-800 font-medium mb-2">📋 Checklist de Configuración Inicial</p>
          <p className="text-blue-700 text-sm">Completa estos pasos en orden para configurar tu sistema correctamente</p>
        </div>
        
        <div className="space-y-4">
          <div className="border-l-4 border-green-500 pl-4">
            <h5 className="font-semibold text-gray-800">Paso 1: Configurar Campos</h5>
            <ol className="list-decimal list-inside space-y-1 text-sm text-gray-600 mt-2">
              <li>Ve a <strong>Producción → Campos</strong></li>
              <li>Haz clic en <strong>"Agregar Campo"</strong></li>
              <li>Completa: nombre, ubicación, superficie total</li>
              <li>Selecciona tipo de suelo (arcilloso, arenoso, etc.)</li>
              <li>Agrega coordenadas GPS si las tienes</li>
              <li>Asigna un responsable del campo</li>
              <li>Guarda la información</li>
            </ol>
          </div>

          <div className="border-l-4 border-blue-500 pl-4">
            <h5 className="font-semibold text-gray-800">Paso 2: Crear Lotes</h5>
            <ol className="list-decimal list-inside space-y-1 text-sm text-gray-600 mt-2">
              <li>Ve a <strong>Producción → Lotes</strong></li>
              <li>Haz clic en <strong>"Crear Lote"</strong></li>
              <li>Selecciona el campo donde está ubicado</li>
              <li>Define superficie del lote (en hectáreas)</li>
              <li>Establece el estado inicial (Preparación, Siembra, etc.)</li>
              <li>Agrega observaciones si es necesario</li>
              <li>Guarda el lote</li>
            </ol>
          </div>

          <div className="border-l-4 border-purple-500 pl-4">
            <h5 className="font-semibold text-gray-800">Paso 3: Registrar Cultivos</h5>
            <ol className="list-decimal list-inside space-y-1 text-sm text-gray-600 mt-2">
              <li>Ve a <strong>Producción → Cultivos</strong></li>
              <li>Haz clic en <strong>"Agregar Cultivo"</strong></li>
              <li>Ingresa nombre del cultivo (ej: Soja, Maíz, Trigo)</li>
              <li>Define variedad específica</li>
              <li>Establece rendimiento esperado (kg/ha)</li>
              <li>Define ciclo de cultivo (días)</li>
              <li>Selecciona unidad de medida</li>
              <li>Guarda el cultivo</li>
            </ol>
          </div>

          <div className="border-l-4 border-orange-500 pl-4">
            <h5 className="font-semibold text-gray-800">Paso 4: Gestionar Insumos</h5>
            <ol className="list-decimal list-inside space-y-1 text-sm text-gray-600 mt-2">
              <li>Ve a <strong>Recursos & Stock → Insumos</strong></li>
              <li>Haz clic en <strong>"Agregar Insumo"</strong></li>
              <li>Selecciona tipo (Semilla, Fertilizante, Pesticida, etc.)</li>
              <li>Ingresa nombre y marca del producto</li>
              <li>Define unidad de medida (kg, litros, bolsas)</li>
              <li>Establece stock inicial</li>
              <li>Agrega precio por unidad</li>
              <li>Define fecha de vencimiento si aplica</li>
              <li>Guarda el insumo</li>
            </ol>
          </div>

          <div className="border-l-4 border-red-500 pl-4">
            <h5 className="font-semibold text-gray-800">Paso 5: Registrar Primera Labor</h5>
            <ol className="list-decimal list-inside space-y-1 text-sm text-gray-600 mt-2">
              <li>Ve a <strong>Producción → Labores</strong></li>
              <li>Haz clic en <strong>"Nueva Labor"</strong></li>
              <li>Selecciona el lote donde se realizará</li>
              <li>Elige tipo de labor (Siembra, Fertilización, Riego, etc.)</li>
              <li>Selecciona cultivo si aplica</li>
              <li>Define fecha de realización</li>
              <li>Agrega insumos utilizados y cantidades</li>
              <li>Registra maquinaria y mano de obra</li>
              <li>Calcula costos automáticamente</li>
              <li>Guarda la labor</li>
            </ol>
          </div>
        </div>
      </div>

      <div>
        <h4 className="text-lg font-semibold text-gray-700 mb-3">💡 Consejos para el Uso Diario</h4>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="bg-green-50 p-4 rounded-lg">
            <h5 className="font-semibold text-green-800 mb-2">✅ Buenas Prácticas</h5>
            <ul className="list-disc list-inside space-y-1 text-sm text-green-700">
              <li>Registra labores el mismo día que se realizan</li>
              <li>Mantén actualizado el inventario de insumos</li>
              <li>Revisa reportes semanalmente</li>
              <li>Asigna responsables claros a cada lote</li>
              <li>Documenta observaciones importantes</li>
            </ul>
          </div>
          <div className="bg-red-50 p-4 rounded-lg">
            <h5 className="font-semibold text-red-800 mb-2">❌ Evita Estos Errores</h5>
            <ul className="list-disc list-inside space-y-1 text-sm text-red-700">
              <li>No dejes lotes sin estado definido</li>
              <li>No olvides registrar costos de labores</li>
              <li>No mezcles cultivos en el mismo lote</li>
              <li>No uses insumos vencidos</li>
              <li>No olvides actualizar inventarios</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );

  const renderContenidoProduccion = () => (
    <div className="space-y-6">
      <div>
        <h3 className="text-xl font-bold text-gray-800 mb-4">🌱 Gestión de Producción</h3>
        <p className="text-gray-600 mb-4">
          La sección de producción es el corazón del sistema, donde gestionas todos los 
          aspectos relacionados con tus cultivos y actividades agrícolas.
        </p>
      </div>

      <div className="space-y-6">
        {/* Campos */}
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            🌾 Gestión de Campos
          </h4>
          <p className="text-gray-600 mb-4">
            Los campos son tus terrenos principales. Aquí registras la información básica 
            de cada propiedad rural.
          </p>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📝 Cómo Crear un Campo</h5>
              <ol className="list-decimal list-inside space-y-2 text-sm text-gray-600">
                <li>Ve a <strong>Producción → Campos</strong></li>
                <li>Haz clic en <strong>"Agregar Campo"</strong></li>
                <li><strong>Nombre:</strong> Identificador único (ej: "Campo Norte")</li>
                <li><strong>Ubicación:</strong> Dirección o referencia geográfica</li>
                <li><strong>Superficie Total:</strong> En hectáreas</li>
                <li><strong>Tipo de Suelo:</strong> Arcilloso, Arenoso, Franco, etc.</li>
                <li><strong>Coordenadas GPS:</strong> Latitud y longitud (opcional)</li>
                <li><strong>Responsable:</strong> Persona a cargo del campo</li>
                <li>Haz clic en <strong>"Guardar"</strong></li>
              </ol>
            </div>
            
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">🔧 Gestión Diaria</h5>
              <ul className="list-disc list-inside space-y-2 text-sm text-gray-600">
                <li><strong>Editar:</strong> Modifica información del campo</li>
                <li><strong>Ver Lotes:</strong> Lista todos los lotes del campo</li>
                <li><strong>Historial:</strong> Revisa actividades realizadas</li>
                <li><strong>Reportes:</strong> Genera análisis por campo</li>
              </ul>
            </div>
          </div>
        </div>

        {/* Lotes */}
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            🔲 Gestión de Lotes
          </h4>
          <p className="text-gray-600 mb-4">
            Los lotes son divisiones de los campos para facilitar la gestión. 
            Cada lote puede tener un cultivo diferente y estado independiente.
          </p>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📝 Cómo Crear un Lote</h5>
              <ol className="list-decimal list-inside space-y-2 text-sm text-gray-600">
                <li>Ve a <strong>Producción → Lotes</strong></li>
                <li>Haz clic en <strong>"Crear Lote"</strong></li>
                <li><strong>Selecciona Campo:</strong> Elige el campo padre</li>
                <li><strong>Nombre del Lote:</strong> Identificador (ej: "Lote A1")</li>
                <li><strong>Superficie:</strong> En hectáreas</li>
                <li><strong>Estado Inicial:</strong> Preparación, Siembra, Crecimiento, etc.</li>
                <li><strong>Observaciones:</strong> Notas importantes</li>
                <li>Haz clic en <strong>"Guardar"</strong></li>
              </ol>
            </div>
            
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">🔄 Estados de Lote</h5>
              <div className="space-y-2 text-sm">
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 bg-yellow-500 rounded-full"></span>
                  <span><strong>Preparación:</strong> Listo para sembrar</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 bg-green-500 rounded-full"></span>
                  <span><strong>Siembra:</strong> Recién sembrado</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 bg-blue-500 rounded-full"></span>
                  <span><strong>Crecimiento:</strong> En desarrollo</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 bg-orange-500 rounded-full"></span>
                  <span><strong>Cosecha:</strong> Listo para cosechar</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 bg-gray-500 rounded-full"></span>
                  <span><strong>Descanso:</strong> En barbecho</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Cultivos */}
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            🌱 Gestión de Cultivos
          </h4>
          <p className="text-gray-600 mb-4">
            Define los tipos de cultivos que manejas en tu empresa. 
            Cada cultivo tiene características específicas que afectan los cálculos.
          </p>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📝 Cómo Registrar un Cultivo</h5>
              <ol className="list-decimal list-inside space-y-2 text-sm text-gray-600">
                <li>Ve a <strong>Producción → Cultivos</strong></li>
                <li>Haz clic en <strong>"Agregar Cultivo"</strong></li>
                <li><strong>Nombre:</strong> Soja, Maíz, Trigo, etc.</li>
                <li><strong>Variedad:</strong> Específica del cultivo</li>
                <li><strong>Rendimiento Esperado:</strong> kg/ha</li>
                <li><strong>Ciclo de Cultivo:</strong> Días desde siembra a cosecha</li>
                <li><strong>Unidad de Medida:</strong> kg, toneladas, etc.</li>
                <li><strong>Precio de Referencia:</strong> $/kg (opcional)</li>
                <li>Haz clic en <strong>"Guardar"</strong></li>
              </ol>
            </div>
            
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📊 Información Importante</h5>
              <ul className="list-disc list-inside space-y-2 text-sm text-gray-600">
                <li><strong>Rendimiento:</strong> Usado para proyecciones</li>
                <li><strong>Ciclo:</strong> Para planificar labores</li>
                <li><strong>Precio:</strong> Para cálculos de rentabilidad</li>
                <li><strong>Variedad:</strong> Para seguimiento específico</li>
              </ul>
            </div>
          </div>
        </div>

        {/* Proceso de Siembra a Cosecha */}
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            🌱 Proceso Completo: Siembra a Cosecha
          </h4>
          <p className="text-gray-600 mb-4">
            Guía paso a paso del ciclo completo de producción, desde la preparación 
            del suelo hasta la cosecha y comercialización.
          </p>
          
          <div className="space-y-4">
            <div className="border-l-4 border-yellow-500 pl-4">
              <h5 className="font-semibold text-gray-800">1️⃣ Preparación del Suelo</h5>
              <p className="text-sm text-gray-600 mb-2">Estado del lote: <strong>Preparación</strong></p>
              <ul className="list-disc list-inside space-y-1 text-sm text-gray-600">
                <li><strong>Labranza:</strong> Arado y rastreado del suelo</li>
                <li><strong>Nivelación:</strong> Corrección de desniveles</li>
                <li><strong>Análisis de Suelo:</strong> pH, nutrientes, textura</li>
                <li><strong>Corrección:</strong> Enmiendas y fertilizantes base</li>
              </ul>
            </div>

            <div className="border-l-4 border-green-500 pl-4">
              <h5 className="font-semibold text-gray-800">2️⃣ Siembra</h5>
              <p className="text-sm text-gray-600 mb-2">Estado del lote: <strong>Siembra</strong></p>
              <ul className="list-disc list-inside space-y-1 text-sm text-gray-600">
                <li><strong>Selección de Semilla:</strong> Variedad y calidad</li>
                <li><strong>Densidad de Siembra:</strong> kg/ha según cultivo</li>
                <li><strong>Profundidad:</strong> cm según tipo de semilla</li>
                <li><strong>Distancia entre Hileras:</strong> cm según cultivo</li>
                <li><strong>Fecha Óptima:</strong> Según zona y clima</li>
              </ul>
            </div>

            <div className="border-l-4 border-blue-500 pl-4">
              <h5 className="font-semibold text-gray-800">3️⃣ Crecimiento y Desarrollo</h5>
              <p className="text-sm text-gray-600 mb-2">Estado del lote: <strong>Crecimiento</strong></p>
              <ul className="list-disc list-inside space-y-1 text-sm text-gray-600">
                <li><strong>Fertilización:</strong> Nutrientes según etapa</li>
                <li><strong>Riego:</strong> Aplicación de agua según necesidades</li>
                <li><strong>Control de Malezas:</strong> Herbicidas pre y post emergencia</li>
                <li><strong>Control de Plagas:</strong> Insecticidas y fungicidas</li>
                <li><strong>Monitoreo:</strong> Inspección regular del cultivo</li>
              </ul>
            </div>

            <div className="border-l-4 border-orange-500 pl-4">
              <h5 className="font-semibold text-gray-800">4️⃣ Cosecha</h5>
              <p className="text-sm text-gray-600 mb-2">Estado del lote: <strong>Cosecha</strong></p>
              <ul className="list-disc list-inside space-y-1 text-sm text-gray-600">
                <li><strong>Punto Óptimo:</strong> Madurez fisiológica</li>
                <li><strong>Humedad:</strong> % óptimo para cosecha</li>
                <li><strong>Rendimiento:</strong> kg/ha obtenidos</li>
                <li><strong>Calidad:</strong> Evaluación del grano</li>
                <li><strong>Almacenamiento:</strong> Condiciones de guarda</li>
              </ul>
            </div>

            <div className="border-l-4 border-gray-500 pl-4">
              <h5 className="font-semibold text-gray-800">5️⃣ Post-Cosecha</h5>
              <p className="text-sm text-gray-600 mb-2">Estado del lote: <strong>Descanso</strong></p>
              <ul className="list-disc list-inside space-y-1 text-sm text-gray-600">
                <li><strong>Comercialización:</strong> Venta de granos</li>
                <li><strong>Análisis de Rentabilidad:</strong> Costos vs Ingresos</li>
                <li><strong>Preparación para Próximo Ciclo:</strong> Planificación</li>
                <li><strong>Mantenimiento:</strong> Cuidado del suelo</li>
              </ul>
            </div>
          </div>
        </div>

        {/* Labores por Estado del Lote */}
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            ⚒️ Labores por Estado del Lote
          </h4>
          <p className="text-gray-600 mb-4">
            Cada estado del lote permite realizar ciertas labores específicas. 
            Aquí te explicamos qué labores puedes hacer según el estado.
          </p>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📝 Cómo Registrar una Labor</h5>
              <ol className="list-decimal list-inside space-y-2 text-sm text-gray-600">
                <li>Ve a <strong>Producción → Labores</strong></li>
                <li>Haz clic en <strong>"Nueva Labor"</strong></li>
                <li><strong>Selecciona Lote:</strong> Donde se realizará</li>
                <li><strong>Tipo de Labor:</strong> Según estado del lote</li>
                <li><strong>Cultivo:</strong> Si aplica</li>
                <li><strong>Fecha:</strong> Cuándo se realizará</li>
                <li><strong>Insumos:</strong> Productos a utilizar</li>
                <li><strong>Maquinaria:</strong> Equipos necesarios</li>
                <li><strong>Observaciones:</strong> Notas importantes</li>
                <li>Haz clic en <strong>"Guardar"</strong></li>
              </ol>
            </div>
            
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">🔧 Labores por Estado</h5>
              <div className="space-y-4 text-sm">
                <div className="border-l-4 border-yellow-500 pl-3">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="w-3 h-3 bg-yellow-500 rounded-full"></span>
                    <span><strong>Preparación:</strong></span>
                  </div>
                  <ul className="list-disc list-inside space-y-1 text-gray-600 ml-5">
                    <li>Labranza (Arado, Rastreado)</li>
                    <li>Nivelación del terreno</li>
                    <li>Análisis de suelo</li>
                    <li>Corrección de pH</li>
                    <li>Fertilización base</li>
                  </ul>
                </div>

                <div className="border-l-4 border-green-500 pl-3">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="w-3 h-3 bg-green-500 rounded-full"></span>
                    <span><strong>Siembra:</strong></span>
                  </div>
                  <ul className="list-disc list-inside space-y-1 text-gray-600 ml-5">
                    <li>Siembra directa</li>
                    <li>Fertilización de arranque</li>
                    <li>Tratamiento de semillas</li>
                    <li>Marcado de hileras</li>
                  </ul>
                </div>

                <div className="border-l-4 border-blue-500 pl-3">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="w-3 h-3 bg-blue-500 rounded-full"></span>
                    <span><strong>Crecimiento:</strong></span>
                  </div>
                  <ul className="list-disc list-inside space-y-1 text-gray-600 ml-5">
                    <li>Fertilización foliar</li>
                    <li>Riego complementario</li>
                    <li>Control de malezas</li>
                    <li>Control de plagas</li>
                    <li>Control de enfermedades</li>
                  </ul>
                </div>

                <div className="border-l-4 border-orange-500 pl-3">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="w-3 h-3 bg-orange-500 rounded-full"></span>
                    <span><strong>Cosecha:</strong></span>
                  </div>
                  <ul className="list-disc list-inside space-y-1 text-gray-600 ml-5">
                    <li>Cosecha mecánica</li>
                    <li>Limpieza de granos</li>
                    <li>Secado (si necesario)</li>
                    <li>Almacenamiento</li>
                  </ul>
                </div>

                <div className="border-l-4 border-gray-500 pl-3">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="w-3 h-3 bg-gray-500 rounded-full"></span>
                    <span><strong>Descanso:</strong></span>
                  </div>
                  <ul className="list-disc list-inside space-y-1 text-gray-600 ml-5">
                    <li>Mantenimiento del suelo</li>
                    <li>Planificación del próximo ciclo</li>
                    <li>Análisis de rentabilidad</li>
                    <li>Preparación para barbecho</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Flujo de Trabajo Integrado */}
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            🔄 Flujo de Trabajo Integrado
          </h4>
          <p className="text-gray-600 mb-4">
            Cómo integrar todos los módulos del sistema para un flujo de trabajo 
            completo y eficiente.
          </p>
          
          <div className="space-y-4">
            <div className="bg-blue-50 p-4 rounded-lg">
              <h5 className="font-semibold text-blue-800 mb-2">📋 Flujo Completo Recomendado</h5>
              <ol className="list-decimal list-inside space-y-2 text-sm text-blue-700">
                <li><strong>1. Configurar Campos y Lotes</strong> (Producción)</li>
                <li><strong>2. Registrar Cultivos</strong> (Producción)</li>
                <li><strong>3. Cargar Insumos en Inventario</strong> (Recursos)</li>
                <li><strong>4. Registrar Compra de Insumos como Gasto</strong> (Finanzas)</li>
                <li><strong>5. Crear Labores según Estado del Lote</strong> (Producción)</li>
                <li><strong>6. Registrar Cosechas</strong> (Recursos - Inventario Granos)</li>
                <li><strong>7. Registrar Venta de Granos como Ingreso</strong> (Finanzas)</li>
                <li><strong>8. Generar Reportes de Rentabilidad</strong> (Reportes)</li>
              </ol>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="bg-green-50 p-4 rounded-lg">
                <h5 className="font-semibold text-green-800 mb-2">✅ Buenas Prácticas</h5>
                <ul className="list-disc list-inside space-y-1 text-sm text-green-700">
                  <li>Registra labores el mismo día que se realizan</li>
                  <li>Carga gastos de insumos inmediatamente</li>
                  <li>Actualiza inventarios después de cada labor</li>
                  <li>Revisa reportes semanalmente</li>
                  <li>Mantén estados de lotes actualizados</li>
                </ul>
              </div>
              
              <div className="bg-red-50 p-4 rounded-lg">
                <h5 className="font-semibold text-red-800 mb-2">❌ Errores Comunes</h5>
                <ul className="list-disc list-inside space-y-1 text-sm text-red-700">
                  <li>No registrar labores a tiempo</li>
                  <li>Olvidar cargar gastos de insumos</li>
                  <li>No actualizar inventarios</li>
                  <li>Estados de lotes incorrectos</li>
                  <li>No revisar reportes regularmente</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );

  const renderContenidoRecursos = () => (
    <div className="space-y-6">
      <div>
        <h3 className="text-xl font-bold text-gray-800 mb-4">📦 Gestión de Recursos</h3>
        <p className="text-gray-600 mb-4">
          Controla todos los recursos necesarios para tu operación: insumos, 
          maquinaria, mano de obra e inventario de granos.
        </p>
      </div>

      <div className="space-y-6">
        {/* Insumos */}
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            🌱 Gestión de Insumos
          </h4>
          <p className="text-gray-600 mb-4">
            Los insumos son todos los productos que utilizas en tu producción: 
            semillas, fertilizantes, pesticidas, etc.
          </p>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📝 Cómo Agregar un Insumo</h5>
              <ol className="list-decimal list-inside space-y-2 text-sm text-gray-600">
                <li>Ve a <strong>Recursos & Stock → Insumos</strong></li>
                <li>Haz clic en <strong>"Agregar Insumo"</strong></li>
                <li><strong>Tipo:</strong> Semilla, Fertilizante, Pesticida, Otro</li>
                <li><strong>Nombre:</strong> Nombre del producto</li>
                <li><strong>Marca:</strong> Fabricante o proveedor</li>
                <li><strong>Unidad de Medida:</strong> kg, litros, bolsas, etc.</li>
                <li><strong>Stock Inicial:</strong> Cantidad disponible</li>
                <li><strong>Precio por Unidad:</strong> Costo del producto</li>
                <li><strong>Fecha de Vencimiento:</strong> Si aplica</li>
                <li><strong>Proveedor:</strong> Quien lo vende</li>
                <li>Haz clic en <strong>"Guardar"</strong></li>
              </ol>
            </div>
            
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📊 Control de Stock</h5>
              <ul className="list-disc list-inside space-y-2 text-sm text-gray-600">
                <li><strong>Stock Actual:</strong> Cantidad disponible</li>
                <li><strong>Stock Mínimo:</strong> Nivel de alerta</li>
                <li><strong>Última Actualización:</strong> Fecha del cambio</li>
                <li><strong>Próximo Vencimiento:</strong> Productos por vencer</li>
                <li><strong>Costo Total:</strong> Valor del inventario</li>
              </ul>
            </div>
          </div>
        </div>

        {/* Maquinaria */}
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            🚜 Gestión de Maquinaria
          </h4>
          <p className="text-gray-600 mb-4">
            Registra y controla todos tus equipos: tractores, sembradoras, 
            cosechadoras, pulverizadoras, etc.
          </p>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📝 Cómo Registrar Maquinaria</h5>
              <ol className="list-decimal list-inside space-y-2 text-sm text-gray-600">
                <li>Ve a <strong>Recursos & Stock → Maquinaria</strong></li>
                <li>Haz clic en <strong>"Agregar Maquinaria"</strong></li>
                <li><strong>Tipo:</strong> Tractor, Sembradora, Cosechadora, etc.</li>
                <li><strong>Marca y Modelo:</strong> Identificación del equipo</li>
                <li><strong>Año:</strong> Año de fabricación</li>
                <li><strong>Número de Serie:</strong> Identificación única</li>
                <li><strong>Estado:</strong> Operativo, Mantenimiento, Fuera de Servicio</li>
                <li><strong>Costo por Hora:</strong> Incluye combustible, mantenimiento</li>
                <li><strong>Horas de Uso:</strong> Total acumulado</li>
                <li><strong>Próximo Mantenimiento:</strong> Fecha programada</li>
                <li>Haz clic en <strong>"Guardar"</strong></li>
              </ol>
            </div>
            
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">🔧 Tipos de Maquinaria</h5>
              <div className="space-y-2 text-sm">
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 bg-green-500 rounded-full"></span>
                  <span><strong>Tractor:</strong> Trabajo general</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 bg-blue-500 rounded-full"></span>
                  <span><strong>Sembradora:</strong> Plantación de cultivos</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 bg-yellow-500 rounded-full"></span>
                  <span><strong>Cosechadora:</strong> Recolección</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 bg-red-500 rounded-full"></span>
                  <span><strong>Pulverizadora:</strong> Aplicación de productos</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 bg-purple-500 rounded-full"></span>
                  <span><strong>Otros:</strong> Equipos especializados</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Cargar Insumos como Gasto */}
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            💰 Cargar Insumos como Gasto desde Finanzas
          </h4>
          <p className="text-gray-600 mb-4">
            Cuando compras insumos, puedes registrarlos tanto en el inventario 
            como en los gastos financieros para un control completo.
          </p>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📝 Cómo Cargar Insumo como Gasto</h5>
              <ol className="list-decimal list-inside space-y-2 text-sm text-gray-600">
                <li>Ve a <strong>Gestión Financiera → Egresos</strong></li>
                <li>Haz clic en <strong>"Agregar Egreso"</strong></li>
                <li><strong>Categoría:</strong> Selecciona "Insumos"</li>
                <li><strong>Descripción:</strong> Ej: "Compra de semillas de soja"</li>
                <li><strong>Monto:</strong> Costo total de la compra</li>
                <li><strong>Fecha:</strong> Fecha de la compra</li>
                <li><strong>Proveedor:</strong> Quien vendió los insumos</li>
                <li><strong>Método de Pago:</strong> Efectivo, Transferencia, Cheque</li>
                <li><strong>Comprobante:</strong> Número de factura</li>
                <li><strong>Lote Asociado:</strong> Si ya sabes dónde se usará</li>
                <li>Haz clic en <strong>"Guardar"</strong></li>
              </ol>
            </div>
            
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📊 Tipos de Insumos como Gastos</h5>
              <div className="space-y-2 text-sm">
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 bg-green-500 rounded-full"></span>
                  <span><strong>Semillas:</strong> Costo por kg o bolsa</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 bg-blue-500 rounded-full"></span>
                  <span><strong>Fertilizantes:</strong> Urea, fosfato, potasio</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 bg-red-500 rounded-full"></span>
                  <span><strong>Pesticidas:</strong> Herbicidas, insecticidas</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 bg-yellow-500 rounded-full"></span>
                  <span><strong>Otros:</strong> Enmiendas, correctores</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Inventario de Granos */}
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            🌾 Inventario de Granos
          </h4>
          <p className="text-gray-600 mb-4">
            Controla la producción cosechada: granos almacenados, vendidos, 
            y pendientes de comercialización.
          </p>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📝 Cómo Registrar Cosecha</h5>
              <ol className="list-decimal list-inside space-y-2 text-sm text-gray-600">
                <li>Ve a <strong>Recursos & Stock → Inventario Granos</strong></li>
                <li>Haz clic en <strong>"Agregar Cosecha"</strong></li>
                <li><strong>Lote:</strong> De dónde proviene</li>
                <li><strong>Cultivo:</strong> Tipo de grano</li>
                <li><strong>Cantidad:</strong> kg o toneladas cosechadas</li>
                <li><strong>Fecha de Cosecha:</strong> Cuándo se realizó</li>
                <li><strong>Humedad:</strong> % de humedad del grano</li>
                <li><strong>Calidad:</strong> Excelente, Buena, Regular</li>
                <li><strong>Almacén:</strong> Dónde se guarda</li>
                <li><strong>Precio de Venta:</strong> $/kg (opcional)</li>
                <li>Haz clic en <strong>"Guardar"</strong></li>
              </ol>
            </div>
            
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📊 Control de Inventario</h5>
              <ul className="list-disc list-inside space-y-2 text-sm text-gray-600">
                <li><strong>Stock Disponible:</strong> Granos en almacén</li>
                <li><strong>Vendido:</strong> Granos comercializados</li>
                <li><strong>Pendiente:</strong> Por vender</li>
                <li><strong>Valor Total:</strong> Precio × Cantidad</li>
                <li><strong>Rendimiento:</strong> kg/ha del lote</li>
                <li><strong>Calidad Promedio:</strong> Evaluación general</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  );

  const renderContenidoFinanzas = () => (
    <div className="space-y-6">
      <div>
        <h3 className="text-xl font-bold text-gray-800 mb-4">💰 Gestión Financiera</h3>
        <p className="text-gray-600 mb-4">
          Controla los aspectos económicos de tu empresa rural con herramientas 
          especializadas para el sector agropecuario.
        </p>
      </div>

      <div className="space-y-6">
        {/* Ingresos */}
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            💳 Gestión de Ingresos
          </h4>
          <p className="text-gray-600 mb-4">
            Registra todos los ingresos de tu empresa: ventas de granos, 
            servicios prestados, subsidios, etc.
          </p>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📝 Cómo Registrar un Ingreso</h5>
              <ol className="list-decimal list-inside space-y-2 text-sm text-gray-600">
                <li>Ve a <strong>Gestión Financiera → Ingresos</strong></li>
                <li>Haz clic en <strong>"Agregar Ingreso"</strong></li>
                <li><strong>Tipo:</strong> Venta de Granos, Servicios, Subsidios, Otros</li>
                <li><strong>Descripción:</strong> Detalle del ingreso</li>
                <li><strong>Monto:</strong> Cantidad en pesos</li>
                <li><strong>Fecha:</strong> Cuándo se recibió</li>
                <li><strong>Cliente/Proveedor:</strong> Quien pagó</li>
                <li><strong>Método de Pago:</strong> Efectivo, Transferencia, Cheque</li>
                <li><strong>Comprobante:</strong> Número de factura/recibo</li>
                <li><strong>Observaciones:</strong> Notas adicionales</li>
                <li>Haz clic en <strong>"Guardar"</strong></li>
              </ol>
            </div>
            
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📊 Tipos de Ingresos</h5>
              <div className="space-y-2 text-sm">
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 bg-green-500 rounded-full"></span>
                  <span><strong>Venta de Granos:</strong> Comercialización de cosechas</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 bg-blue-500 rounded-full"></span>
                  <span><strong>Servicios:</strong> Trabajos para terceros</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 bg-yellow-500 rounded-full"></span>
                  <span><strong>Subsidios:</strong> Ayudas gubernamentales</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 bg-purple-500 rounded-full"></span>
                  <span><strong>Otros:</strong> Ingresos diversos</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Egresos */}
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            💸 Gestión de Egresos
          </h4>
          <p className="text-gray-600 mb-4">
            Controla todos los gastos de tu empresa: compra de insumos, 
            combustible, mano de obra, servicios, etc.
          </p>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📝 Cómo Registrar un Egreso</h5>
              <ol className="list-decimal list-inside space-y-2 text-sm text-gray-600">
                <li>Ve a <strong>Gestión Financiera → Egresos</strong></li>
                <li>Haz clic en <strong>"Agregar Egreso"</strong></li>
                <li><strong>Categoría:</strong> Insumos, Combustible, Mano de Obra, Servicios</li>
                <li><strong>Descripción:</strong> Detalle del gasto</li>
                <li><strong>Monto:</strong> Cantidad en pesos</li>
                <li><strong>Fecha:</strong> Cuándo se realizó</li>
                <li><strong>Proveedor:</strong> Quien vendió</li>
                <li><strong>Método de Pago:</strong> Efectivo, Transferencia, Cheque</li>
                <li><strong>Comprobante:</strong> Número de factura/recibo</li>
                <li><strong>Lote Asociado:</strong> Si aplica</li>
                <li>Haz clic en <strong>"Guardar"</strong></li>
              </ol>
            </div>
            
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📊 Categorías de Egresos</h5>
              <div className="space-y-2 text-sm">
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 bg-red-500 rounded-full"></span>
                  <span><strong>Insumos:</strong> Semillas, fertilizantes, pesticidas</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 bg-orange-500 rounded-full"></span>
                  <span><strong>Combustible:</strong> Gasoil, lubricantes</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 bg-blue-500 rounded-full"></span>
                  <span><strong>Mano de Obra:</strong> Salarios, jornales</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 bg-green-500 rounded-full"></span>
                  <span><strong>Servicios:</strong> Alquileres, mantenimientos</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Balance */}
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            📊 Balance Financiero
          </h4>
          <p className="text-gray-600 mb-4">
            Analiza la situación financiera de tu empresa con reportes 
            detallados por período, lote o cultivo.
          </p>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📝 Cómo Generar un Balance</h5>
              <ol className="list-decimal list-inside space-y-2 text-sm text-gray-600">
                <li>Ve a <strong>Gestión Financiera → Balance</strong></li>
                <li>Selecciona <strong>Período:</strong> Mes actual, Año actual, Personalizado</li>
                <li>Si es personalizado, define <strong>Fecha Inicio</strong> y <strong>Fecha Fin</strong></li>
                <li>Opcional: Selecciona <strong>Lote específico</strong> para análisis detallado</li>
                <li>Haz clic en <strong>"Generar Reporte"</strong></li>
                <li>Revisa el <strong>Resumen Ejecutivo</strong></li>
                <li>Analiza <strong>Ingresos vs Egresos</strong></li>
                <li>Revisa <strong>Margen de Beneficio</strong></li>
                <li>Exporta en <strong>Excel, PDF o CSV</strong> si necesitas</li>
              </ol>
            </div>
            
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📊 Métricas del Balance</h5>
              <ul className="list-disc list-inside space-y-2 text-sm text-gray-600">
                <li><strong>Ingresos Totales:</strong> Suma de todos los ingresos</li>
                <li><strong>Egresos Totales:</strong> Suma de todos los gastos</li>
                <li><strong>Beneficio Neto:</strong> Ingresos - Egresos</li>
                <li><strong>Margen %:</strong> (Beneficio / Ingresos) × 100</li>
                <li><strong>ROI:</strong> Retorno sobre la inversión</li>
                <li><strong>Costos por Hectárea:</strong> Egresos / Superficie</li>
              </ul>
            </div>
          </div>
        </div>

        {/* Rentabilidad */}
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            📈 Análisis de Rentabilidad
          </h4>
          <p className="text-gray-600 mb-4">
            Evalúa la rentabilidad de tus cultivos y lotes para tomar 
            decisiones informadas sobre tu producción.
          </p>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📝 Cómo Analizar Rentabilidad</h5>
              <ol className="list-decimal list-inside space-y-2 text-sm text-gray-600">
                <li>Ve a <strong>Gestión Financiera → Rentabilidad</strong></li>
                <li>Selecciona <strong>Cultivo:</strong> Soja, Maíz, Trigo, etc.</li>
                <li>Elige <strong>Período:</strong> Última campaña, Año actual</li>
                <li>Opcional: Filtra por <strong>Lote específico</strong></li>
                <li>Haz clic en <strong>"Calcular Rentabilidad"</strong></li>
                <li>Revisa <strong>Costos por Hectárea</strong></li>
                <li>Analiza <strong>Ingresos por Hectárea</strong></li>
                <li>Compara <strong>Rendimiento vs Costos</strong></li>
                <li>Exporta el <strong>Reporte de Rentabilidad</strong></li>
              </ol>
            </div>
            
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📊 Indicadores de Rentabilidad</h5>
              <ul className="list-disc list-inside space-y-2 text-sm text-gray-600">
                <li><strong>ROI por Cultivo:</strong> Retorno de inversión</li>
                <li><strong>Margen Bruto:</strong> Ingresos - Costos directos</li>
                <li><strong>Margen Neto:</strong> Ingresos - Todos los costos</li>
                <li><strong>Rendimiento Económico:</strong> $/hectárea</li>
                <li><strong>Punto de Equilibrio:</strong> Producción mínima rentable</li>
                <li><strong>Comparación Histórica:</strong> Evolución en el tiempo</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  );

  const renderContenidoReportes = () => (
    <div className="space-y-6">
      <div>
        <h3 className="text-xl font-bold text-gray-800 mb-4">📊 Reportes y Análisis</h3>
        <p className="text-gray-600 mb-4">
          Genera reportes detallados para tomar decisiones informadas sobre tu 
          producción y gestión.
        </p>
      </div>

      <div className="space-y-6">
        {/* Reportes de Rendimiento */}
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            📈 Reportes de Rendimiento
          </h4>
          <p className="text-gray-600 mb-4">
            Analiza la productividad de tus cultivos y lotes con reportes 
            detallados de rendimiento.
          </p>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📝 Cómo Generar Reporte de Rendimiento</h5>
              <ol className="list-decimal list-inside space-y-2 text-sm text-gray-600">
                <li>Ve a <strong>Reportes y Análisis → Reportes Operativos</strong></li>
                <li>Selecciona <strong>"Reporte de Rendimiento"</strong></li>
                <li>Elige <strong>Período:</strong> Última campaña, Año actual, Personalizado</li>
                <li>Selecciona <strong>Cultivo:</strong> Soja, Maíz, Trigo, Todos</li>
                <li>Opcional: Filtra por <strong>Lote específico</strong></li>
                <li>Haz clic en <strong>"Generar Reporte"</strong></li>
                <li>Revisa <strong>Rendimiento por Hectárea</strong></li>
                <li>Analiza <strong>Comparación entre Lotes</strong></li>
                <li>Exporta en <strong>Excel, PDF o CSV</strong></li>
              </ol>
            </div>
            
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📊 Métricas del Reporte</h5>
              <ul className="list-disc list-inside space-y-2 text-sm text-gray-600">
                <li><strong>Rendimiento Promedio:</strong> kg/ha por cultivo</li>
                <li><strong>Mejor Lote:</strong> Mayor productividad</li>
                <li><strong>Peor Lote:</strong> Menor productividad</li>
                <li><strong>Variabilidad:</strong> Desviación estándar</li>
                <li><strong>Tendencia:</strong> Evolución en el tiempo</li>
                <li><strong>Comparación Histórica:</strong> vs campañas anteriores</li>
              </ul>
            </div>
          </div>
        </div>

        {/* Reportes de Cosechas */}
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            🌾 Reportes de Cosechas
          </h4>
          <p className="text-gray-600 mb-4">
            Seguimiento detallado de la producción cosechada, calidad y 
            volúmenes por período.
          </p>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📝 Cómo Generar Reporte de Cosechas</h5>
              <ol className="list-decimal list-inside space-y-2 text-sm text-gray-600">
                <li>Ve a <strong>Reportes y Análisis → Reportes Operativos</strong></li>
                <li>Selecciona <strong>"Reporte de Cosechas"</strong></li>
                <li>Elige <strong>Período:</strong> Mes actual, Año actual, Personalizado</li>
                <li>Selecciona <strong>Cultivo:</strong> Específico o Todos</li>
                <li>Opcional: Filtra por <strong>Lote o Campo</strong></li>
                <li>Haz clic en <strong>"Generar Reporte"</strong></li>
                <li>Revisa <strong>Volúmenes Cosechados</strong></li>
                <li>Analiza <strong>Calidad y Humedad</strong></li>
                <li>Exporta el <strong>Reporte Completo</strong></li>
              </ol>
            </div>
            
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📊 Información del Reporte</h5>
              <ul className="list-disc list-inside space-y-2 text-sm text-gray-600">
                <li><strong>Total Cosechado:</strong> kg/toneladas por cultivo</li>
                <li><strong>Calidad Promedio:</strong> Excelente, Buena, Regular</li>
                <li><strong>Humedad Promedio:</strong> % de humedad</li>
                <li><strong>Fecha de Cosecha:</strong> Rango temporal</li>
                <li><strong>Lotes Cosechados:</strong> Cantidad y superficie</li>
                <li><strong>Proyecciones:</strong> Estimaciones futuras</li>
              </ul>
            </div>
          </div>
        </div>

        {/* Reportes Financieros */}
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            💰 Reportes Financieros
          </h4>
          <p className="text-gray-600 mb-4">
            Análisis económico detallado de tu empresa: balance, rentabilidad, 
            costos y proyecciones.
          </p>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📝 Cómo Generar Reporte Financiero</h5>
              <ol className="list-decimal list-inside space-y-2 text-sm text-gray-600">
                <li>Ve a <strong>Reportes y Análisis → Reportes Operativos</strong></li>
                <li>Selecciona <strong>"Reporte de Rentabilidad"</strong></li>
                <li>Elige <strong>Período:</strong> Mes, Año, Personalizado</li>
                <li>Selecciona <strong>Cultivo:</strong> Específico o Todos</li>
                <li>Opcional: Filtra por <strong>Lote específico</strong></li>
                <li>Haz clic en <strong>"Generar Reporte"</strong></li>
                <li>Revisa <strong>Balance Ingresos vs Egresos</strong></li>
                <li>Analiza <strong>Rentabilidad por Cultivo</strong></li>
                <li>Exporta en <strong>Excel, PDF o CSV</strong></li>
              </ol>
            </div>
            
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📊 Métricas Financieras</h5>
              <ul className="list-disc list-inside space-y-2 text-sm text-gray-600">
                <li><strong>Ingresos Totales:</strong> Suma de ventas</li>
                <li><strong>Costos Totales:</strong> Suma de gastos</li>
                <li><strong>Beneficio Neto:</strong> Ingresos - Costos</li>
                <li><strong>Margen %:</strong> (Beneficio / Ingresos) × 100</li>
                <li><strong>ROI:</strong> Retorno sobre inversión</li>
                <li><strong>Costos por Hectárea:</strong> $/ha</li>
              </ul>
            </div>
          </div>
        </div>

        {/* Reportes Operativos */}
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            📋 Reportes Operativos
          </h4>
          <p className="text-gray-600 mb-4">
            Seguimiento de la actividad operativa: labores realizadas, 
            uso de recursos y eficiencia.
          </p>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📝 Cómo Generar Reporte Operativo</h5>
              <ol className="list-decimal list-inside space-y-2 text-sm text-gray-600">
                <li>Ve a <strong>Reportes y Análisis → Reportes Operativos</strong></li>
                <li>Selecciona <strong>"Reporte de Labores"</strong></li>
                <li>Elige <strong>Período:</strong> Semana, Mes, Personalizado</li>
                <li>Selecciona <strong>Tipo de Labor:</strong> Siembra, Fertilización, etc.</li>
                <li>Opcional: Filtra por <strong>Lote o Campo</strong></li>
                <li>Haz clic en <strong>"Generar Reporte"</strong></li>
                <li>Revisa <strong>Actividad por Día</strong></li>
                <li>Analiza <strong>Uso de Recursos</strong></li>
                <li>Exporta el <strong>Reporte Detallado</strong></li>
              </ol>
            </div>
            
            <div>
              <h5 className="font-semibold text-gray-700 mb-3">📊 Indicadores Operativos</h5>
              <ul className="list-disc list-inside space-y-2 text-sm text-gray-600">
                <li><strong>Labores Realizadas:</strong> Cantidad por tipo</li>
                <li><strong>Horas Trabajadas:</strong> Tiempo total empleado</li>
                <li><strong>Insumos Utilizados:</strong> Cantidad y costos</li>
                <li><strong>Maquinaria:</strong> Horas de uso por equipo</li>
                <li><strong>Eficiencia:</strong> Labores por día</li>
                <li><strong>Costos Operativos:</strong> $ por labor</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  );

  const renderContenidoAdministracion = () => (
    <div className="space-y-6">
      <div>
        <h3 className="text-xl font-bold text-gray-800 mb-4">⚙️ Administración del Sistema</h3>
        <p className="text-gray-600 mb-4">
          Herramientas avanzadas para administrar usuarios, empresas y configuraciones 
          del sistema.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              👥 Gestión de Usuarios
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-gray-600 mb-3">
              <strong>Función:</strong> Administrar usuarios del sistema
            </p>
            <ul className="list-disc list-inside space-y-1 text-sm text-gray-600">
              <li>Crear y editar usuarios</li>
              <li>Asignar roles y permisos</li>
              <li>Gestionar accesos por empresa</li>
              <li>Controlar estados de usuarios</li>
            </ul>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              🏢 Gestión de Empresas
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-gray-600 mb-3">
              <strong>Función:</strong> Administrar empresas y relaciones
            </p>
            <ul className="list-disc list-inside space-y-1 text-sm text-gray-600">
              <li>Crear nuevas empresas</li>
              <li>Asignar usuarios a empresas</li>
              <li>Gestionar roles por empresa</li>
              <li>Configurar permisos específicos</li>
            </ul>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              📊 Dashboard Administrativo
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-gray-600 mb-3">
              <strong>Función:</strong> Vista global del sistema
            </p>
            <ul className="list-disc list-inside space-y-1 text-sm text-gray-600">
              <li>Estadísticas globales</li>
              <li>Actividad de usuarios</li>
              <li>Uso del sistema</li>
              <li>Métricas de rendimiento</li>
            </ul>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              🔧 Configuración
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-gray-600 mb-3">
              <strong>Función:</strong> Ajustes del sistema
            </p>
            <ul className="list-disc list-inside space-y-1 text-sm text-gray-600">
              <li>Configuración de moneda</li>
              <li>Parámetros del sistema</li>
              <li>Integraciones</li>
              <li>Backup y mantenimiento</li>
            </ul>
          </CardContent>
        </Card>
      </div>
    </div>
  );

  const renderContenidoRoles = () => (
    <div className="space-y-6">
      <div>
        <h3 className="text-xl font-bold text-gray-800 mb-4">👥 Roles y Permisos</h3>
        <p className="text-gray-600 mb-4">
          El sistema utiliza un sistema de roles jerárquico para controlar el acceso 
          a las diferentes funcionalidades según el tipo de usuario.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              👑 SUPERADMIN
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-gray-600 mb-3">
              <strong>Acceso:</strong> Control total del sistema
            </p>
            <ul className="list-disc list-inside space-y-1 text-sm text-gray-600">
              <li>Gestión global de empresas</li>
              <li>Administración de usuarios</li>
              <li>Dashboard administrativo</li>
              <li>Configuración del sistema</li>
            </ul>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              🏢 ADMINISTRADOR
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-gray-600 mb-3">
              <strong>Acceso:</strong> Administración de empresa
            </p>
            <ul className="list-disc list-inside space-y-1 text-sm text-gray-600">
              <li>Gestión de usuarios de la empresa</li>
              <li>Acceso a todas las funcionalidades</li>
              <li>Reportes financieros</li>
              <li>Configuración de empresa</li>
            </ul>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              🌾 JEFE DE CAMPO
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-gray-600 mb-3">
              <strong>Acceso:</strong> Gestión operativa
            </p>
            <ul className="list-disc list-inside space-y-1 text-sm text-gray-600">
              <li>Gestión de lotes y cultivos</li>
              <li>Registro de labores</li>
              <li>Control de recursos</li>
              <li>Reportes operativos</li>
            </ul>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              💰 JEFE FINANCIERO
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-gray-600 mb-3">
              <strong>Acceso:</strong> Gestión financiera
            </p>
            <ul className="list-disc list-inside space-y-1 text-sm text-gray-600">
              <li>Ingresos y egresos</li>
              <li>Reportes financieros</li>
              <li>Análisis de rentabilidad</li>
              <li>Balance y costos</li>
            </ul>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              👷 OPERARIO
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-gray-600 mb-3">
              <strong>Acceso:</strong> Operaciones básicas
            </p>
            <ul className="list-disc list-inside space-y-1 text-sm text-gray-600">
              <li>Registro de labores</li>
              <li>Consulta de información</li>
              <li>Reportes básicos</li>
              <li>Acceso limitado</li>
            </ul>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              👁️ CONSULTOR EXTERNO
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-gray-600 mb-3">
              <strong>Acceso:</strong> Solo lectura
            </p>
            <ul className="list-disc list-inside space-y-1 text-sm text-gray-600">
              <li>Consulta de datos</li>
              <li>Reportes operativos</li>
              <li>Análisis de rendimientos</li>
              <li>Sin permisos de edición</li>
            </ul>
          </CardContent>
        </Card>
      </div>
    </div>
  );

  const renderContenido = () => {
    switch (seccionActiva) {
      case 'general':
        return renderContenidoGeneral();
      case 'produccion':
        return renderContenidoProduccion();
      case 'recursos':
        return renderContenidoRecursos();
      case 'finanzas':
        return renderContenidoFinanzas();
      case 'reportes':
        return renderContenidoReportes();
      case 'administracion':
        return renderContenidoAdministracion();
      case 'roles':
        return renderContenidoRoles();
      default:
        return renderContenidoGeneral();
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-7xl mx-auto">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-800 mb-2">📚 Ayuda del Sistema</h1>
          <p className="text-gray-600">
            Guía completa de funcionalidades y uso de AgroCloud
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
          {/* Navegación lateral */}
          <div className="lg:col-span-1">
            <Card>
              <CardHeader>
                <CardTitle className="text-lg">Navegación</CardTitle>
              </CardHeader>
              <CardContent className="p-0">
                <nav className="space-y-1">
                  {secciones.map((seccion) => (
                    <button
                      key={seccion.id}
                      onClick={() => setSeccionActiva(seccion.id)}
                      className={`w-full text-left px-4 py-3 text-sm font-medium rounded-none border-l-4 transition-colors ${
                        seccionActiva === seccion.id
                          ? 'bg-blue-50 text-blue-700 border-blue-500'
                          : 'text-gray-600 hover:bg-gray-50 border-transparent'
                      }`}
                    >
                      <span className="mr-2">{seccion.icon}</span>
                      {seccion.label}
                    </button>
                  ))}
                </nav>
              </CardContent>
            </Card>
          </div>

          {/* Contenido principal */}
          <div className="lg:col-span-3">
            <Card>
              <CardContent className="p-8">
                {renderContenido()}
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AyudaSistema;
