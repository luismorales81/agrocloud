import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useEmpresa } from '../contexts/EmpresaContext';
import { agroquimicosIntegradosService, insumosService } from '../services/apiServices';
import PermissionGate from './PermissionGate';
import InsumoWizard from './InsumoWizard';
import SugerenciasDosisModal from './SugerenciasDosisModal';
import RegistroCantidadRealModal from './RegistroCantidadRealModal';
import CondicionesAmbientalesModal from './CondicionesAmbientalesModal';
import { Icon } from '../core/components/Icon';

interface InsumoAgroquimico {
  id: number;
  nombre: string;
  descripcion: string;
  tipo: string;
  unidadMedida: string;
  stockActual: number;
  stockMinimo: number;
  precioUnitario: number;
  proveedor?: string;
  principioActivo?: string;
  concentracion?: string;
  claseQuimica?: string;
  categoriaToxicologica?: string;
  periodoCarenciaDias?: number;
  dosisMinimaPorHa?: number;
  dosisMaximaPorHa?: number;
  unidadDosis?: string;
  activo: boolean;
}

interface DosisSugerida {
  agroquimicoId: number;
  nombreAgroquimico: string;
  tipoAplicacion: string;
  dosisSugeridaPorHa: number;
  cantidadTotalSugerida: number;
  superficieHa: number;
  stockDisponible: number;
  stockSuficiente: boolean;
  unidadMedida: string;
  condicionesAplicacion: string;
  factorAjuste: number;
  nivelRiesgo: string;
  observaciones?: string;
}

interface CondicionesAmbientales {
  temperatura: number;
  humedad: number;
  velocidadViento: number;
  direccionViento?: string;
  presionAtmosferica?: number;
  observaciones?: string;
}

const AgroquimicosIntegrados: React.FC = () => {
  const { user } = useAuth();
  const { rolUsuario } = useEmpresa();
  
  const [insumos, setInsumos] = useState<InsumoAgroquimico[]>([]);
  const [agroquimicos, setAgroquimicos] = useState<InsumoAgroquimico[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Estados para modales
  const [showSugerenciasModal, setShowSugerenciasModal] = useState(false);
  const [showCantidadRealModal, setShowCantidadRealModal] = useState(false);
  const [showCondicionesModal, setShowCondicionesModal] = useState(false);
  
  // Estados para el wizard
  const [showWizard, setShowWizard] = useState(false);
  const [wizardMode, setWizardMode] = useState<'create' | 'edit'>('create');
  const [editingInsumo, setEditingInsumo] = useState<any>(null);
  
  // Estados para formularios
  const [insumoSeleccionado, setInsumoSeleccionado] = useState<InsumoAgroquimico | null>(null);
  const [sugerencias, setSugerencias] = useState<DosisSugerida[]>([]);
  const [condiciones, setCondiciones] = useState<CondicionesAmbientales | null>(null);

  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    try {
      setLoading(true);
      
      // Intentar cargar agroquímicos integrados
      try {
        const agroquimicosData = await agroquimicosIntegradosService.listar();
        setAgroquimicos(Array.isArray(agroquimicosData) ? agroquimicosData : []);
      } catch (agroquimicosError) {
        console.warn('Endpoint de agroquímicos integrados no disponible, usando datos simulados');
        // Datos simulados para desarrollo
        setAgroquimicos([]);
      }
      
      // Cargar todos los insumos para conversión
      try {
        const insumosData = await insumosService.listar();
        const insumosAgroquimicos = (Array.isArray(insumosData) ? insumosData : []).filter((insumo: any) => 
          ['HERBICIDA', 'FUNGICIDA', 'INSECTICIDA', 'FERTILIZANTE'].includes(insumo.tipo)
        );
        setInsumos(insumosAgroquimicos);
      } catch (insumosError) {
        console.warn('Endpoint de insumos no disponible, usando datos simulados');
        // Datos simulados para desarrollo
        setInsumos([
          {
            id: 1,
            nombre: 'Glifosato 48%',
            descripcion: 'Herbicida sistémico',
            tipo: 'HERBICIDA',
            unidadMedida: 'L',
            stockActual: 100,
            stockMinimo: 10,
            precioUnitario: 1500,
            activo: true
          },
          {
            id: 2,
            nombre: 'Atrazina 50%',
            descripcion: 'Herbicida selectivo',
            tipo: 'HERBICIDA',
            unidadMedida: 'L',
            stockActual: 50,
            stockMinimo: 5,
            precioUnitario: 2000,
            activo: true
          }
        ]);
      }
      
    } catch (err) {
      setError('Error al cargar los datos. Verifique que el backend esté ejecutándose.');
      console.error('Error:', err);
    } finally {
      setLoading(false);
    }
  };

  // Funciones para el wizard
  const handleCreate = () => {
    setEditingInsumo(null);
    setWizardMode('create');
    setShowWizard(true);
  };

  const handleEdit = (insumo: any) => {
    setEditingInsumo(insumo);
    setWizardMode('edit');
    setShowWizard(true);
  };

  const handleWizardClose = () => {
    setShowWizard(false);
    setEditingInsumo(null);
  };

  const handleWizardSave = () => {
    cargarDatos();
    setShowWizard(false);
    setEditingInsumo(null);
  };

  const convertirInsumoAAgroquimico = async (insumo: InsumoAgroquimico) => {
    try {
      await agroquimicosIntegradosService.convertirInsumo(insumo.id, {
        principioActivo: '',
        concentracion: '',
        claseQuimica: '',
        categoriaToxicologica: '',
        periodoCarenciaDias: 0,
        dosisMinimaPorHa: 0,
        dosisMaximaPorHa: 0,
        unidadDosis: 'L',
        dosisPorTipo: []
      });
      
      // Recargar datos
      await cargarDatos();
      alert('Insumo convertido a agroquímico exitosamente');
      
    } catch (err) {
      setError('Error al convertir insumo a agroquímico');
      console.error('Error:', err);
    }
  };

  const obtenerSugerencias = async (agroquimico: InsumoAgroquimico) => {
    try {
      setInsumoSeleccionado(agroquimico);
      setShowSugerenciasModal(true);
    } catch (err) {
      setError('Error al obtener sugerencias');
      console.error('Error:', err);
    }
  };

  const registrarCantidadReal = async (aplicacionId: number) => {
    try {
      setShowCantidadRealModal(true);
    } catch (err) {
      setError('Error al registrar cantidad real');
      console.error('Error:', err);
    }
  };

  const configurarCondiciones = () => {
    setShowCondicionesModal(true);
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  return (
    <PermissionGate permission="canViewInsumos">
      <div className="container mx-auto px-4 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Agroquímicos Integrados</h1>
          <p className="text-gray-600">Sistema integrado para gestión de agroquímicos con sugerencias inteligentes</p>
        </div>

        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            {error}
          </div>
        )}

        {/* Mensaje informativo sobre el estado del sistema */}
        <div className="mb-6 p-4 bg-blue-50 border border-blue-200 rounded-lg">
          <div className="flex items-center">
            <span className="text-blue-500 text-xl mr-3">ℹ️</span>
            <div>
              <h3 className="font-semibold text-blue-900">Sistema de Agroquímicos Integrados</h3>
              <p className="text-blue-700 text-sm mt-1">
                Este sistema está en desarrollo. Algunas funcionalidades pueden requerir que el backend esté ejecutándose.
              </p>
            </div>
          </div>
        </div>

        {/* Botones de acción */}
        <div className="mb-6 flex flex-wrap gap-4">
          <button
            onClick={handleCreate}
            className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
          >
            ➕ Nuevo Insumo/Agroquímico
          </button>
          <button
            onClick={configurarCondiciones}
            className="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded"
          >
            🌡️ Configurar Condiciones Ambientales
          </button>
        </div>

        {/* Sección de Insumos para Convertir */}
        <div className="mb-8">
          <h2 className="text-xl font-semibold mb-4">Insumos para Convertir en Agroquímicos</h2>
          <div className="bg-white shadow overflow-hidden sm:rounded-md">
            {insumos.filter(insumo => !insumo.principioActivo).length === 0 ? (
              <div className="px-6 py-8 text-center text-gray-500">
                <p>No hay insumos agroquímicos sin configurar.</p>
              </div>
            ) : (
              <ul className="divide-y divide-gray-200">
                {insumos.filter(insumo => !insumo.principioActivo).map((insumo) => (
                  <li key={insumo.id} className="px-6 py-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <h3 className="text-lg font-medium text-gray-900">
                          {insumo.nombre}
                        </h3>
                        <p className="text-sm text-gray-500">
                          Tipo: {insumo.tipo} | Stock: {insumo.stockActual} {insumo.unidadMedida}
                        </p>
                        <p className="text-sm text-gray-500">
                          Precio: ${insumo.precioUnitario} | Proveedor: {insumo.proveedor || 'No especificado'}
                        </p>
                      </div>
                      <div className="flex space-x-2">
                        <button
                          onClick={() => convertirInsumoAAgroquimico(insumo)}
                          className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
                        >
                          Configurar como Agroquímico
                        </button>
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>

        {/* Sección de Agroquímicos Configurados */}
        <div className="mb-8">
          <h2 className="text-xl font-semibold mb-4">Agroquímicos Configurados</h2>
          <div className="bg-white shadow overflow-hidden sm:rounded-md">
            {agroquimicos.length === 0 ? (
              <div className="px-6 py-8 text-center text-gray-500">
                <p>No hay agroquímicos configurados.</p>
                <p className="text-sm">Convierte algunos insumos en agroquímicos para comenzar.</p>
              </div>
            ) : (
              <ul className="divide-y divide-gray-200">
                {agroquimicos.map((agroquimico) => (
                  <li key={agroquimico.id} className="px-6 py-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <h3 className="text-lg font-medium text-gray-900">
                          {agroquimico.nombre}
                        </h3>
                        <p className="text-sm text-gray-500">
                          {agroquimico.principioActivo && (
                            <>Principio Activo: {agroquimico.principioActivo} | </>
                          )}
                          {agroquimico.concentracion && (
                            <>Concentración: {agroquimico.concentracion} | </>
                          )}
                          {agroquimico.claseQuimica && (
                            <>Clase: {agroquimico.claseQuimica}</>
                          )}
                        </p>
                        <p className="text-sm text-gray-500">
                          Stock: {agroquimico.stockActual} {agroquimico.unidadMedida} | 
                          Precio: ${agroquimico.precioUnitario}
                        </p>
                        {agroquimico.categoriaToxicologica && (
                          <p className="text-sm text-orange-600">
                            Categoría Toxicológica: {agroquimico.categoriaToxicologica} | 
                            Período de Carencia: {agroquimico.periodoCarenciaDias} días
                          </p>
                        )}
                      </div>
                      <div className="flex space-x-2">
                        <button
                          onClick={() => obtenerSugerencias(agroquimico)}
                          className="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded flex items-center gap-2"
                        >
                          <Icon name="Lightbulb" size={16} /> Obtener Sugerencias
                        </button>
                        <button
                          onClick={() => registrarCantidadReal(agroquimico.id)}
                          className="bg-purple-500 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded flex items-center gap-2"
                        >
                          <Icon name="BarChart" size={16} /> Registrar Aplicación
                        </button>
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>

        {/* Modales */}
        {showSugerenciasModal && insumoSeleccionado && (
          <SugerenciasDosisModal
            agroquimico={insumoSeleccionado}
            condiciones={condiciones}
            onClose={() => {
              setShowSugerenciasModal(false);
              setInsumoSeleccionado(null);
            }}
            onSugerenciasObtenidas={setSugerencias}
          />
        )}

        {showCantidadRealModal && (
          <RegistroCantidadRealModal
            onClose={() => setShowCantidadRealModal(false)}
            onCantidadRegistrada={() => {
              setShowCantidadRealModal(false);
              cargarDatos();
            }}
          />
        )}

        {showCondicionesModal && (
          <CondicionesAmbientalesModal
            condiciones={condiciones}
            onClose={() => setShowCondicionesModal(false)}
            onCondicionesGuardadas={(nuevasCondiciones) => {
              setCondiciones(nuevasCondiciones);
              setShowCondicionesModal(false);
            }}
          />
        )}

        {/* Wizard de Insumos */}
        <InsumoWizard
          isOpen={showWizard}
          onClose={handleWizardClose}
          insumoEditando={editingInsumo}
          onGuardar={handleWizardSave}
        />
      </div>
    </PermissionGate>
  );
};

export default AgroquimicosIntegrados;
