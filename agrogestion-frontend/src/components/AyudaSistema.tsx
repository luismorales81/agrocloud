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

        {/* Opciones de Cosecha */}
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            🌾 Opciones de Cosecha y sus Comportamientos
          </h4>
          <p className="text-gray-600 mb-4">
            El sistema ofrece diferentes opciones al momento de cosechar un lote, cada una con un comportamiento específico. 
            Es importante entender cuándo usar cada opción para registrar correctamente la información.
          </p>
          
          <div className="space-y-6">
            {/* Cosechar Normal */}
            <div className="border-l-4 border-orange-500 pl-4">
              <h5 className="font-semibold text-gray-800 mb-2 flex items-center gap-2">
                🌾 1. Cosechar Normal
              </h5>
              <div className="space-y-3 text-sm">
                <div>
                  <strong className="text-gray-700">Cuándo usar:</strong>
                  <ul className="list-disc list-inside ml-4 mt-1 text-gray-600">
                    <li>Lote en estado <strong>LISTO_PARA_COSECHA</strong> (momento óptimo)</li>
                    <li>Cosecha anticipada desde estados <strong>SEMBRADO, EN_CRECIMIENTO, EN_FLORACION, EN_FRUTIFICACION</strong></li>
                    <li>Cosecha normal del cultivo cuando está maduro</li>
                  </ul>
                </div>
                <div className="bg-orange-50 p-3 rounded-lg border border-orange-200">
                  <strong className="text-orange-800">Comportamiento del sistema:</strong>
                  <ul className="list-disc list-inside ml-4 mt-2 space-y-1 text-orange-700">
                    <li>✅ Registra la <strong>labor de cosecha</strong> con todos los detalles (cantidad, fecha, estado del suelo, etc.)</li>
                    <li>✅ Crea un registro en <strong>historial de cosechas</strong> con rendimiento real calculado</li>
                    <li>✅ Crea automáticamente un registro en <strong>inventario de granos</strong></li>
                    <li>✅ Cambia el estado del lote a <strong>COSECHADO</strong></li>
                    <li>✅ Calcula el <strong>costo total de producción</strong> (suma todas las labores del lote)</li>
                    <li>✅ Permite ingresar <strong>precio de venta</strong> para calcular rentabilidad</li>
                    <li>✅ Permite marcar si el suelo <strong>requiere descanso</strong> y días recomendados</li>
                    <li>✅ Calcula y guarda el <strong>rendimiento real</strong> (cantidad / superficie en ha)</li>
                  </ul>
                </div>
                <div className="bg-blue-50 p-3 rounded-lg border border-blue-200">
                  <strong className="text-blue-800">💡 Información que puedes registrar:</strong>
                  <ul className="list-disc list-inside ml-4 mt-2 space-y-1 text-blue-700">
                    <li>Cantidad cosechada (kg, toneladas, quintales)</li>
                    <li>Fecha de cosecha</li>
                    <li>Variedad de semilla</li>
                    <li>Estado del suelo (Bueno, Descansando, Agotado)</li>
                    <li>Si requiere descanso y cuántos días</li>
                    <li>Precio de venta (opcional)</li>
                    <li>Observaciones sobre la cosecha</li>
                  </ul>
                </div>
              </div>
            </div>

            {/* Convertir a Forraje */}
            <div className="border-l-4 border-brown-500 pl-4">
              <h5 className="font-semibold text-gray-800 mb-2 flex items-center gap-2">
                🐄 2. Convertir a Forraje
              </h5>
              <div className="space-y-3 text-sm">
                <div>
                  <strong className="text-gray-700">Cuándo usar:</strong>
                  <ul className="list-disc list-inside ml-4 mt-1 text-gray-600">
                    <li>Cosecha anticipada del cultivo para alimentación animal</li>
                    <li>Cultivo inmaduro que se convertirá en forraje</li>
                    <li>Necesidad de forraje para ganado</li>
                  </ul>
                </div>
                <div className="bg-brown-50 p-3 rounded-lg border border-brown-200">
                  <strong className="text-brown-800">Comportamiento del sistema:</strong>
                  <ul className="list-disc list-inside ml-4 mt-2 space-y-1 text-brown-700">
                    <li>✅ Similar a <strong>Cosechar Normal</strong>, pero marca como forraje</li>
                    <li>✅ Agrega automáticamente la observación <strong>"CONVERSIÓN A FORRAJE | Cosecha anticipada para alimentación animal"</strong></li>
                    <li>✅ Marca el estado del suelo como <strong>BUENO</strong> (el forraje no agota el suelo)</li>
                    <li>✅ Si no ingresas cantidad, usa <strong>1 tonelada por defecto</strong></li>
                    <li>✅ Cambia el estado del lote a <strong>COSECHADO</strong></li>
                    <li>✅ Crea historial de cosecha e inventario de granos</li>
                    <li>✅ Calcula rendimiento y costos de producción</li>
                  </ul>
                </div>
                <div className="bg-yellow-50 p-3 rounded-lg border border-yellow-200">
                  <strong className="text-yellow-800">⚠️ Diferencia clave:</strong>
                  <p className="text-yellow-700 mt-1">
                    Esta opción está diseñada para cultivos que se cosechan anticipadamente para forraje, 
                    no para grano. El sistema lo marca especialmente para que quede registrado el destino del cultivo.
                  </p>
                </div>
              </div>
            </div>

            {/* Limpiar Cultivo */}
            <div className="border-l-4 border-gray-500 pl-4">
              <h5 className="font-semibold text-gray-800 mb-2 flex items-center gap-2">
                🚜 3. Limpiar Cultivo
              </h5>
              <div className="space-y-3 text-sm">
                <div>
                  <strong className="text-gray-700">Cuándo usar:</strong>
                  <ul className="list-disc list-inside ml-4 mt-1 text-gray-600">
                    <li>Eliminar un cultivo sin cosechar (cambio de planes)</li>
                    <li>Preparar el lote para otro cultivo</li>
                    <li>Errores en la siembra o cultivo incorrecto</li>
                  </ul>
                </div>
                <div className="bg-gray-50 p-3 rounded-lg border border-gray-200">
                  <strong className="text-gray-800">Comportamiento del sistema:</strong>
                  <ul className="list-disc list-inside ml-4 mt-2 space-y-1 text-gray-700">
                    <li>❌ <strong>NO crea historial de cosecha</strong> (no hay producción)</li>
                    <li>❌ <strong>NO crea inventario de granos</strong> (no hay producción para almacenar)</li>
                    <li>✅ Crea una <strong>labor de tipo "OTROS"</strong> con observación "LIMPIEZA DE CULTIVO"</li>
                    <li>✅ <strong>Resetea el lote completamente</strong> a estado <strong>DISPONIBLE</strong></li>
                    <li>✅ <strong>Elimina todos los datos del cultivo</strong>:
                      <ul className="list-disc list-inside ml-4 mt-1">
                        <li>Cultivo actual → null</li>
                        <li>Fecha de siembra → null</li>
                        <li>Fecha de cosecha esperada → null</li>
                        <li>Fecha de cosecha real → null</li>
                        <li>Rendimiento real → null</li>
                      </ul>
                    </li>
                    <li>✅ Guarda el motivo de limpieza para referencia</li>
                    <li>✅ El lote queda listo para comenzar un nuevo ciclo</li>
                  </ul>
                </div>
                <div className="bg-red-50 p-3 rounded-lg border border-red-200">
                  <strong className="text-red-800">⚠️ Advertencia importante:</strong>
                  <p className="text-red-700 mt-1">
                    Esta acción es <strong>irreversible</strong>. Una vez que limpias un cultivo, 
                    toda la información del cultivo se elimina del lote. El lote vuelve a estado DISPONIBLE 
                    como si nunca hubiera tenido un cultivo.
                  </p>
                </div>
              </div>
            </div>

            {/* Abandonar Cultivo */}
            <div className="border-l-4 border-red-500 pl-4">
              <h5 className="font-semibold text-gray-800 mb-2 flex items-center gap-2">
                ⚠️ 4. Abandonar Cultivo
              </h5>
              <div className="space-y-3 text-sm">
                <div>
                  <strong className="text-gray-700">Cuándo usar:</strong>
                  <ul className="list-disc list-inside ml-4 mt-1 text-gray-600">
                    <li>Problemas graves que impiden continuar el cultivo (plagas, sequía, heladas)</li>
                    <li>Pérdida total del cultivo</li>
                    <li>Condiciones que hacen inviable continuar</li>
                  </ul>
                </div>
                <div className="bg-red-50 p-3 rounded-lg border border-red-200">
                  <strong className="text-red-800">Comportamiento del sistema:</strong>
                  <ul className="list-disc list-inside ml-4 mt-2 space-y-1 text-red-700">
                    <li>❌ <strong>NO crea historial de cosecha</strong> (no hubo producción)</li>
                    <li>❌ <strong>NO crea inventario de granos</strong> (no hay producción)</li>
                    <li>✅ Crea una <strong>labor de tipo "OTROS"</strong> con observación "CULTIVO ABANDONADO"</li>
                    <li>✅ Cambia el estado del lote a <strong>ABANDONADO</strong></li>
                    <li>✅ <strong>NO elimina el cultivo</strong> (queda registrado como abandonado para referencia histórica)</li>
                    <li>✅ Guarda el <strong>motivo del abandono</strong> para análisis futuro</li>
                    <li>✅ El lote queda marcado como abandonado hasta que se reactive manualmente</li>
                    <li>✅ Para reactivar, debes hacer una labor de preparación que cambiará el estado</li>
                  </ul>
                </div>
                <div className="bg-orange-50 p-3 rounded-lg border border-orange-200">
                  <strong className="text-orange-800">💡 Diferencia con Limpiar Cultivo:</strong>
                  <p className="text-orange-700 mt-1">
                    <strong>Abandonar</strong> mantiene el registro del cultivo (para análisis histórico) pero marca el lote como abandonado. 
                    <strong>Limpiar</strong> elimina completamente el cultivo y deja el lote disponible inmediatamente.
                  </p>
                </div>
              </div>
            </div>

            {/* Comparación */}
            <div className="bg-blue-50 p-4 rounded-lg border border-blue-200">
              <h5 className="font-semibold text-blue-800 mb-3">📊 Resumen Comparativo</h5>
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="bg-blue-100">
                      <th className="border border-blue-300 px-3 py-2 text-left">Opción</th>
                      <th className="border border-blue-300 px-3 py-2 text-center">Historial Cosecha</th>
                      <th className="border border-blue-300 px-3 py-2 text-center">Inventario</th>
                      <th className="border border-blue-300 px-3 py-2 text-center">Estado Final</th>
                      <th className="border border-blue-300 px-3 py-2 text-center">Cultivo</th>
                    </tr>
                  </thead>
                  <tbody className="text-gray-700">
                    <tr>
                      <td className="border border-blue-300 px-3 py-2 font-medium">Cosechar Normal</td>
                      <td className="border border-blue-300 px-3 py-2 text-center">✅ Sí</td>
                      <td className="border border-blue-300 px-3 py-2 text-center">✅ Sí</td>
                      <td className="border border-blue-300 px-3 py-2 text-center">COSECHADO</td>
                      <td className="border border-blue-300 px-3 py-2 text-center">Se mantiene</td>
                    </tr>
                    <tr className="bg-gray-50">
                      <td className="border border-blue-300 px-3 py-2 font-medium">Convertir a Forraje</td>
                      <td className="border border-blue-300 px-3 py-2 text-center">✅ Sí</td>
                      <td className="border border-blue-300 px-3 py-2 text-center">✅ Sí</td>
                      <td className="border border-blue-300 px-3 py-2 text-center">COSECHADO</td>
                      <td className="border border-blue-300 px-3 py-2 text-center">Se mantiene</td>
                    </tr>
                    <tr>
                      <td className="border border-blue-300 px-3 py-2 font-medium">Limpiar Cultivo</td>
                      <td className="border border-blue-300 px-3 py-2 text-center">❌ No</td>
                      <td className="border border-blue-300 px-3 py-2 text-center">❌ No</td>
                      <td className="border border-blue-300 px-3 py-2 text-center">DISPONIBLE</td>
                      <td className="border border-blue-300 px-3 py-2 text-center">Se elimina</td>
                    </tr>
                    <tr className="bg-gray-50">
                      <td className="border border-blue-300 px-3 py-2 font-medium">Abandonar Cultivo</td>
                      <td className="border border-blue-300 px-3 py-2 text-center">❌ No</td>
                      <td className="border border-blue-300 px-3 py-2 text-center">❌ No</td>
                      <td className="border border-blue-300 px-3 py-2 text-center">ABANDONADO</td>
                      <td className="border border-blue-300 px-3 py-2 text-center">Se mantiene</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>

        {/* Flujos del Sistema */}
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            🔄 Flujos del Sistema y su Comportamiento
          </h4>
          <p className="text-gray-600 mb-4">
            El sistema gestiona automáticamente varios flujos importantes. Entender cómo funcionan te ayudará a usar el sistema correctamente.
          </p>
          
          <div className="space-y-6">
            {/* Flujo de Siembra */}
            <div className="border-l-4 border-green-500 pl-4">
              <h5 className="font-semibold text-gray-800 mb-3">🌱 Flujo de Siembra</h5>
              <div className="space-y-3 text-sm">
                <div className="bg-green-50 p-3 rounded-lg border border-green-200">
                  <strong className="text-green-800">Proceso paso a paso:</strong>
                  <ol className="list-decimal list-inside ml-4 mt-2 space-y-1 text-green-700">
                    <li>El lote debe estar en estado <strong>PREPARADO</strong> o <strong>DISPONIBLE</strong></li>
                    <li>Se crea una labor de tipo <strong>SIEMBRA</strong> con todos los detalles (cultivo, variedad, densidad, etc.)</li>
                    <li>El sistema <strong>propone cambiar el estado</strong> a <strong>SEMBRADO</strong></li>
                    <li>Se muestra un <strong>mensaje de confirmación</strong> antes de cambiar el estado</li>
                    <li>Al confirmar, el lote cambia a estado <strong>SEMBRADO</strong></li>
                    <li>Se registra la <strong>fecha de siembra</strong> y el <strong>cultivo actual</strong></li>
                    <li>Se calcula automáticamente la <strong>fecha de cosecha esperada</strong> basada en el ciclo del cultivo</li>
                    <li>Se actualiza el <strong>inventario de insumos</strong> (se descuentan las semillas utilizadas)</li>
                  </ol>
                </div>
                <div className="bg-yellow-50 p-3 rounded-lg border border-yellow-200">
                  <strong className="text-yellow-800">⚠️ Validaciones:</strong>
                  <ul className="list-disc list-inside ml-4 mt-1 text-yellow-700">
                    <li>El lote debe estar en estado válido para siembra (PREPARADO o DISPONIBLE)</li>
                    <li>Debe haber stock suficiente de semillas en inventario</li>
                    <li>Debe estar seleccionado un cultivo válido</li>
                    <li>La fecha de siembra debe ser válida</li>
                  </ul>
                </div>
              </div>
            </div>

            {/* Flujo de Labores y Cambios de Estado */}
            <div className="border-l-4 border-blue-500 pl-4">
              <h5 className="font-semibold text-gray-800 mb-3">⚒️ Flujo de Labores y Cambios de Estado Automáticos</h5>
              <div className="space-y-3 text-sm">
                <div className="bg-blue-50 p-3 rounded-lg border border-blue-200">
                  <strong className="text-blue-800">Cómo funcionan los cambios automáticos:</strong>
                  <ol className="list-decimal list-inside ml-4 mt-2 space-y-1 text-blue-700">
                    <li>Al crear una labor, el sistema <strong>evalúa automáticamente</strong> si debe cambiar el estado del lote</li>
                    <li>Los cambios se basan en:
                      <ul className="list-disc list-inside ml-4 mt-1">
                        <li><strong>Tipo de labor realizada</strong> (arado, rastra, fertilización, etc.)</li>
                        <li><strong>Estado actual del lote</strong></li>
                        <li><strong>Historial de labores previas</strong> del lote</li>
                        <li><strong>Días transcurridos desde la siembra</strong> (para estados de desarrollo)</li>
                      </ul>
                    </li>
                    <li>El sistema <strong>propone el cambio</strong> y muestra un mensaje de confirmación</li>
                    <li>Al confirmar, el estado se actualiza automáticamente</li>
                  </ol>
                </div>
                <div className="bg-purple-50 p-3 rounded-lg border border-purple-200">
                  <strong className="text-purple-800">Ejemplos de transiciones automáticas:</strong>
                  <ul className="list-disc list-inside ml-4 mt-2 space-y-2 text-purple-700">
                    <li><strong>DISPONIBLE + Arado</strong> → Cambia a <strong>EN_PREPARACION</strong></li>
                    <li><strong>EN_PREPARACION + Rastra</strong> (si ya hay 2 labores de preparación) → Cambia a <strong>PREPARADO</strong></li>
                    <li><strong>SEMBRADO</strong> (después de 15 días) → Cambia automáticamente a <strong>EN_CRECIMIENTO</strong></li>
                    <li><strong>EN_CRECIMIENTO</strong> (después de 45 días desde siembra) → Cambia a <strong>EN_FLORACION</strong></li>
                    <li><strong>EN_FLORACION</strong> (después de 65 días) → Cambia a <strong>EN_FRUTIFICACION</strong></li>
                    <li><strong>EN_FRUTIFICACION</strong> (después de 100 días) → Cambia a <strong>LISTO_PARA_COSECHA</strong></li>
                    <li><strong>COSECHADO + Arado</strong> → Cambia a <strong>EN_PREPARACION</strong> (nuevo ciclo)</li>
                  </ul>
                </div>
                <div className="bg-orange-50 p-3 rounded-lg border border-orange-200">
                  <strong className="text-orange-800">💡 Nota importante:</strong>
                  <p className="text-orange-700 mt-1">
                    Los cambios automáticos basados en días son <strong>aproximados</strong> y dependen del ciclo del cultivo registrado. 
                    El sistema también puede cambiar estados basándose en las labores realizadas, no solo en el tiempo transcurrido.
                  </p>
                </div>
              </div>
            </div>

            {/* Flujo de Inventario de Granos */}
            <div className="border-l-4 border-yellow-500 pl-4">
              <h5 className="font-semibold text-gray-800 mb-3">🌾 Flujo de Inventario de Granos</h5>
              <div className="space-y-3 text-sm">
                <div className="bg-yellow-50 p-3 rounded-lg border border-yellow-200">
                  <strong className="text-yellow-800">Proceso automático al cosechar:</strong>
                  <ol className="list-decimal list-inside ml-4 mt-2 space-y-1 text-yellow-700">
                    <li>Al realizar <strong>Cosechar Normal</strong> o <strong>Convertir a Forraje</strong></li>
                    <li>El sistema <strong>crea automáticamente</strong> un registro en el inventario de granos</li>
                    <li>El registro incluye:
                      <ul className="list-disc list-inside ml-4 mt-1">
                        <li>Cantidad cosechada</li>
                        <li>Cultivo y variedad</li>
                        <li>Fecha de cosecha</li>
                        <li>Lote de origen</li>
                        <li>Estado del suelo</li>
                      </ul>
                    </li>
                    <li>El grano queda marcado como <strong>disponible</strong> para venta</li>
                    <li>Puedes agregar información adicional (humedad, calidad, almacén) manualmente</li>
                  </ol>
                </div>
                <div className="bg-green-50 p-3 rounded-lg border border-green-200">
                  <strong className="text-green-800">Venta de granos:</strong>
                  <ul className="list-disc list-inside ml-4 mt-1 text-green-700">
                    <li>Al vender granos, puedes asociar la venta al inventario</li>
                    <li>El sistema actualiza automáticamente el stock disponible</li>
                    <li>Se registra el ingreso en el módulo de finanzas</li>
                    <li>Se puede calcular la rentabilidad de la venta</li>
                  </ul>
                </div>
              </div>
            </div>

            {/* Flujo de Costos y Rentabilidad */}
            <div className="border-l-4 border-purple-500 pl-4">
              <h5 className="font-semibold text-gray-800 mb-3">💰 Flujo de Costos y Rentabilidad</h5>
              <div className="space-y-3 text-sm">
                <div className="bg-purple-50 p-3 rounded-lg border border-purple-200">
                  <strong className="text-purple-800">Cálculo automático de costos:</strong>
                  <ol className="list-decimal list-inside ml-4 mt-2 space-y-1 text-purple-700">
                    <li>Cada labor registrada <strong>calcula su costo total</strong>:
                      <ul className="list-disc list-inside ml-4 mt-1">
                        <li>Costo de insumos utilizados</li>
                        <li>Costo de maquinaria (horas × costo/hora)</li>
                        <li>Costo de mano de obra (horas × costo/hora)</li>
                      </ul>
                    </li>
                    <li>Al cosechar, el sistema <strong>suma todos los costos</strong> de todas las labores del lote</li>
                    <li>Este costo total se guarda en el <strong>historial de cosecha</strong></li>
                    <li>Si ingresas precio de venta, se calcula automáticamente:
                      <ul className="list-disc list-inside ml-4 mt-1">
                        <li>Ingreso total = Cantidad × Precio de venta</li>
                        <li>Beneficio neto = Ingreso total - Costo total de producción</li>
                        <li>Margen de rentabilidad = (Beneficio / Ingreso) × 100</li>
                      </ul>
                    </li>
                  </ol>
                </div>
                <div className="bg-indigo-50 p-3 rounded-lg border border-indigo-200">
                  <strong className="text-indigo-800">Reportes de rentabilidad:</strong>
                  <ul className="list-disc list-inside ml-4 mt-1 text-indigo-700">
                    <li>Puedes generar reportes de rentabilidad por cultivo, lote o período</li>
                    <li>El sistema compara costos vs ingresos automáticamente</li>
                    <li>Se puede analizar el rendimiento económico ($/hectárea)</li>
                    <li>Se pueden comparar diferentes lotes o campañas</li>
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

        {/* Agroquímicos y Dosis */}
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center gap-2">
            🧪 Configuración y Uso de Agroquímicos
          </h4>
          <p className="text-gray-600 mb-4">
            El sistema permite configurar dosis recomendadas para agroquímicos y calcular automáticamente 
            las cantidades necesarias al crear labores, simplificando la gestión y asegurando el uso correcto.
          </p>
          
          <div className="space-y-6">
            <div className="border-l-4 border-purple-500 pl-4">
              <h5 className="font-semibold text-gray-800 mb-3">📝 Paso 1: Configurar un Agroquímico</h5>
              <p className="text-sm text-gray-600 mb-2">
                Cuando creas o editas un insumo, puedes marcarlo como agroquímico y configurar sus dosis recomendadas.
              </p>
              <ol className="list-decimal list-inside space-y-2 text-sm text-gray-600 ml-2">
                <li>Ve a <strong>Recursos & Stock → Insumos</strong></li>
                <li>Haz clic en <strong>"Agregar Insumo"</strong> o edita uno existente</li>
                <li>Selecciona el <strong>Tipo</strong> como uno de los agroquímicos:
                  <ul className="list-disc list-inside ml-4 mt-1">
                    <li>Herbicida</li>
                    <li>Insecticida</li>
                    <li>Fungicida</li>
                    <li>Fertilizante</li>
                    <li>Acaricida</li>
                    <li>Otros tipos de agroquímicos</li>
                  </ul>
                </li>
                <li>Completa los <strong>Propiedades del Agroquímico</strong>:
                  <ul className="list-disc list-inside ml-4 mt-1">
                    <li>Principio activo</li>
                    <li>Concentración</li>
                    <li>Clase química</li>
                    <li>Categoría toxicológica</li>
                    <li>Período de carencia (días)</li>
                    <li>Dosis mínima y máxima por hectárea</li>
                  </ul>
                </li>
                <li>En la sección <strong>"Dosis por Tipo de Aplicación"</strong>, haz clic en <strong>"Agregar Dosis"</strong></li>
                <li>Para cada dosis, configura:
                  <ul className="list-disc list-inside ml-4 mt-1">
                    <li><strong>Tipo de Aplicación:</strong> Pre-emergencia, Pos-emergencia, Cobertura</li>
                    <li><strong>Forma de Aplicación:</strong> Terrestre, Aérea, Manual</li>
                    <li><strong>Dosis Recomendada por Hectárea:</strong> Cantidad en L/ha, kg/ha o ml/ha</li>
                  </ul>
                </li>
                <li>Puedes agregar múltiples configuraciones de dosis según diferentes tipos y formas de aplicación</li>
                <li>Guarda el insumo</li>
              </ol>
            </div>

            <div className="border-l-4 border-blue-500 pl-4">
              <h5 className="font-semibold text-gray-800 mb-3">⚒️ Paso 2: Usar Agroquímicos en Labores</h5>
              <p className="text-sm text-gray-600 mb-2">
                Al crear una labor que involucra agroquímicos (fertilización, pulverización, control de plagas, etc.), 
                el sistema detecta automáticamente los agroquímicos configurados.
              </p>
              <ol className="list-decimal list-inside space-y-2 text-sm text-gray-600 ml-2">
                <li>Ve a <strong>Producción → Labores</strong></li>
                <li>Haz clic en <strong>"Nueva Labor"</strong></li>
                <li>Selecciona un <strong>Tipo de Labor</strong> que involucra agroquímicos:
                  <ul className="list-disc list-inside ml-4 mt-1">
                    <li>Fertilización</li>
                    <li>Pulverización</li>
                    <li>Aplicación de herbicida</li>
                    <li>Aplicación de insecticida</li>
                    <li>Control de plagas</li>
                    <li>Control de malezas</li>
                  </ul>
                </li>
                <li>Selecciona el <strong>Lote</strong> donde se realizará la labor</li>
                <li>En la sección <strong>"Insumos Utilizados"</strong>, haz clic en <strong>"Agregar Insumo"</strong></li>
                <li>Selecciona un <strong>agroquímico configurado</strong> del listado</li>
                <li>Si el agroquímico tiene dosis configuradas:
                  <ul className="list-disc list-inside ml-4 mt-1">
                    <li>El sistema mostrará un <strong>selector de tipo de aplicación</strong></li>
                    <li>Selecciona el <strong>Tipo de Aplicación</strong> y <strong>Forma de Aplicación</strong> que realizarás</li>
                    <li>El sistema <strong>calculará automáticamente</strong> la cantidad necesaria basándose en:
                      <ul className="list-disc list-inside ml-4 mt-1">
                        <li>Las hectáreas del lote seleccionado</li>
                        <li>La dosis recomendada por hectárea configurada</li>
                        <li>Fórmula: <strong>Cantidad = Hectáreas × Dosis Recomendada (por ha)</strong></li>
                      </ul>
                    </li>
                    <li>La cantidad se <strong>completa automáticamente</strong> en el campo "Cantidad"</li>
                    <li>Aparecerá un mensaje informativo con el cálculo realizado</li>
                  </ul>
                </li>
                <li>Si el agroquímico <strong>no tiene dosis configuradas</strong>, puedes ingresar la cantidad manualmente</li>
              </ol>
            </div>

            <div className="border-l-4 border-green-500 pl-4">
              <h5 className="font-semibold text-gray-800 mb-3">✅ Validaciones Automáticas</h5>
              <p className="text-sm text-gray-600 mb-2">
                El sistema realiza validaciones automáticas para asegurar un uso correcto y seguro de los agroquímicos.
              </p>
              <div className="space-y-3">
                <div className="bg-green-50 p-3 rounded-lg">
                  <h6 className="font-semibold text-green-800 mb-1">✓ Validación de Stock</h6>
                  <p className="text-sm text-green-700">
                    El sistema verifica automáticamente si hay stock suficiente del agroquímico antes de permitir 
                    agregarlo a la labor. Si no hay stock suficiente, muestra una advertencia pero permite continuar 
                    (el stock se descontará al confirmar la labor si es suficiente).
                  </p>
                </div>
                <div className="bg-yellow-50 p-3 rounded-lg">
                  <h6 className="font-semibold text-yellow-800 mb-1">⚠ Validación de Dosis</h6>
                  <p className="text-sm text-yellow-700">
                    Si modificas manualmente la cantidad calculada, el sistema valida que la diferencia no exceda 
                    ±20% de la dosis recomendada. Si excede este margen, el sistema rechazará la solicitud con un 
                    mensaje claro indicando que la cantidad está fuera del rango permitido.
                  </p>
                  <p className="text-xs text-yellow-600 mt-1 italic">
                    Ejemplo: Si la dosis recomendada es 2 L/ha para 100 ha (200 L), puedes usar entre 160 L y 240 L (±20%).
                  </p>
                </div>
                <div className="bg-blue-50 p-3 rounded-lg">
                  <h6 className="font-semibold text-blue-800 mb-1">📊 Descuento Automático de Stock</h6>
                  <p className="text-sm text-blue-700">
                    Al confirmar la labor, el sistema descuenta automáticamente el stock del agroquímico utilizado 
                    y registra el movimiento en el inventario para mantener un control preciso.
                  </p>
                </div>
              </div>
            </div>

            <div className="border-l-4 border-orange-500 pl-4">
              <h5 className="font-semibold text-gray-800 mb-3">📋 Cómo se Refleja en la Labor</h5>
              <p className="text-sm text-gray-600 mb-2">
                Los agroquímicos utilizados aparecen en el detalle de costos de la labor junto con los demás insumos.
              </p>
              <div className="space-y-2 text-sm text-gray-600">
                <p><strong>Al ver el detalle de costos de una labor:</strong></p>
                <ul className="list-disc list-inside space-y-1 ml-4">
                  <li>En el <strong>resumen de costos</strong>, verás una caja separada para <strong>"Insumos"</strong> que incluye todos los agroquímicos utilizados</li>
                  <li>En la sección <strong>"Insumos Utilizados"</strong>, cada agroquímico aparece con:
                    <ul className="list-disc list-inside ml-4 mt-1">
                      <li>Nombre del agroquímico</li>
                      <li>Cantidad utilizada con su unidad de medida</li>
                      <li>Precio unitario</li>
                      <li>Costo total (cantidad × precio unitario)</li>
                    </ul>
                  </li>
                  <li>El <strong>costo total</strong> de la labor incluye automáticamente:
                    <ul className="list-disc list-inside ml-4 mt-1">
                      <li>Costo de insumos (incluyendo agroquímicos)</li>
                      <li>Costo de maquinaria</li>
                      <li>Costo de mano de obra</li>
                    </ul>
                  </li>
                </ul>
              </div>
            </div>

            <div className="bg-blue-50 p-4 rounded-lg">
              <h5 className="font-semibold text-blue-800 mb-2">💡 Consejos y Mejores Prácticas</h5>
              <ul className="list-disc list-inside space-y-1 text-sm text-blue-700">
                <li><strong>Configura dosis temprano:</strong> Configura las dosis recomendadas al registrar los agroquímicos para aprovechar el cálculo automático</li>
                <li><strong>Revisa las dosis calculadas:</strong> Siempre revisa que la cantidad calculada sea la correcta antes de confirmar la labor</li>
                <li><strong>Actualiza stock regularmente:</strong> Mantén el stock actualizado para evitar advertencias y asegurar disponibilidad</li>
                <li><strong>Múltiples configuraciones:</strong> Puedes configurar diferentes dosis para el mismo agroquímico según el tipo y forma de aplicación</li>
                <li><strong>Sin dosis configuradas:</strong> Si un agroquímico no tiene dosis configuradas, funciona como un insumo normal permitiendo entrada manual</li>
                <li><strong>Validación de dosis:</strong> El margen de ±20% te permite ajustar la dosis según condiciones específicas del lote</li>
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
