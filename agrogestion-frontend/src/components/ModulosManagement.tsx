import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { API_ENDPOINTS } from '../services/apiEndpoints';

interface Module {
  id: number;
  name: string;
  code: string;
  description: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

interface Company {
  id: number;
  nombre: string;
  cuit: string;
}

interface CompanyModule {
  id: number;
  companyId: number;
  companyName: string;
  moduleId: number;
  moduleCode: string;
  moduleName: string;
  enabled: boolean;
}

const ModulosManagement: React.FC = () => {
  const [modules, setModules] = useState<Module[]>([]);
  const [companies, setCompanies] = useState<Company[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'modules' | 'companies'>('modules');
  
  // Estados para módulos
  const [showCreateModule, setShowCreateModule] = useState(false);
  const [moduleForm, setModuleForm] = useState({ name: '', code: '', description: '', active: true });
  
  // Estados para empresas-módulos
  const [selectedCompany, setSelectedCompany] = useState<number | null>(null);
  const [companyModules, setCompanyModules] = useState<CompanyModule[]>([]);

  useEffect(() => {
    cargarDatos();
  }, []);

  useEffect(() => {
    if (activeTab === 'companies' && selectedCompany) {
      cargarModulosEmpresa(selectedCompany);
    }
  }, [activeTab, selectedCompany]);

  const cargarDatos = async () => {
    try {
      setLoading(true);
      await Promise.all([
        cargarModulos(),
        cargarEmpresas()
      ]);
    } catch (error) {
      console.error('Error cargando datos:', error);
      alert('Error al cargar los datos');
    } finally {
      setLoading(false);
    }
  };

  const cargarModulos = async () => {
    try {
      const response = await api.get(API_ENDPOINTS.MODULOS.LISTAR);
      const modulosData = Array.isArray(response.data) ? response.data : (response.data?.modules || []);
      setModules(modulosData);
      console.log('✅ Módulos cargados:', modulosData);
    } catch (error) {
      console.error('Error cargando módulos:', error);
      setModules([]);
    }
  };

  const cargarEmpresas = async () => {
    try {
      const response = await api.get(API_ENDPOINTS.ADMIN_GLOBAL.EMPRESAS_BASIC);
      const empresasData = Array.isArray(response.data) ? response.data : (response.data?.empresas || []);
      setCompanies(empresasData);
      console.log('✅ Empresas cargadas:', empresasData);
    } catch (error) {
      console.error('Error cargando empresas:', error);
      setCompanies([]);
    }
  };

  const cargarModulosEmpresa = async (companyId: number) => {
    try {
      const response = await api.get(API_ENDPOINTS.COMPANY_MODULES.LISTAR(companyId));
      const modulosData = Array.isArray(response.data) ? response.data : (response.data?.modules || []);
      setCompanyModules(modulosData);
      console.log('✅ Módulos de empresa cargados:', modulosData);
    } catch (error) {
      console.error('Error cargando módulos de empresa:', error);
      setCompanyModules([]);
    }
  };

  const crearModulo = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post(API_ENDPOINTS.MODULOS.CREAR, moduleForm);
      setShowCreateModule(false);
      setModuleForm({ name: '', code: '', description: '', active: true });
      cargarModulos();
      alert('Módulo creado exitosamente');
    } catch (error: any) {
      console.error('Error creando módulo:', error);
      alert(error.response?.data?.message || 'Error al crear el módulo');
    }
  };

  const toggleModuleActive = async (moduleId: number, active: boolean) => {
    try {
      if (active) {
        await api.patch(API_ENDPOINTS.MODULOS.ACTUALIZAR(moduleId), { active: true });
      } else {
        await api.delete(API_ENDPOINTS.MODULOS.ELIMINAR(moduleId));
      }
      cargarModulos();
    } catch (error) {
      console.error('Error actualizando módulo:', error);
      alert('Error al actualizar el módulo');
    }
  };

  const habilitarModuloEmpresa = async (companyId: number, moduleId: number) => {
    try {
      await api.post(API_ENDPOINTS.COMPANY_MODULES.HABILITAR(companyId, moduleId));
      cargarModulosEmpresa(companyId);
      alert('Módulo habilitado para la empresa');
    } catch (error) {
      console.error('Error habilitando módulo:', error);
      alert('Error al habilitar el módulo');
    }
  };

  const deshabilitarModuloEmpresa = async (companyId: number, moduleId: number) => {
    try {
      await api.delete(API_ENDPOINTS.COMPANY_MODULES.DESHABILITAR(companyId, moduleId));
      cargarModulosEmpresa(companyId);
      alert('Módulo deshabilitado para la empresa');
    } catch (error) {
      console.error('Error deshabilitando módulo:', error);
      alert('Error al deshabilitar el módulo');
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center p-8">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Cargando datos...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Tabs */}
      <div className="bg-white rounded-lg shadow">
        <div className="border-b border-gray-200">
          <nav className="flex -mb-px">
            {[
              { id: 'modules', name: '📦 Módulos', icon: '📦' },
              { id: 'companies', name: '🏢 Módulos por Empresa', icon: '🏢' }
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as any)}
                className={`px-6 py-3 text-sm font-medium border-b-2 ${
                  activeTab === tab.id
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                {tab.name}
              </button>
            ))}
          </nav>
        </div>
      </div>

      {/* Tab: Módulos */}
      {activeTab === 'modules' && (
        <div className="space-y-4">
          <div className="flex justify-between items-center">
            <h2 className="text-2xl font-bold text-gray-900">Gestión de Módulos</h2>
            <button
              onClick={() => setShowCreateModule(true)}
              className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
            >
              + Crear Módulo
            </button>
          </div>

          {showCreateModule && (
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-lg font-semibold mb-4">Crear Nuevo Módulo</h3>
              <form onSubmit={crearModulo} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700">Nombre</label>
                  <input
                    type="text"
                    value={moduleForm.name}
                    onChange={(e) => setModuleForm({ ...moduleForm, name: e.target.value })}
                    className="mt-1 block w-full rounded-md border-gray-300 shadow-sm"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Código</label>
                  <input
                    type="text"
                    value={moduleForm.code}
                    onChange={(e) => setModuleForm({ ...moduleForm, code: e.target.value.toLowerCase() })}
                    className="mt-1 block w-full rounded-md border-gray-300 shadow-sm"
                    required
                    placeholder="ej: pigs, cattle, ai"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700">Descripción</label>
                  <textarea
                    value={moduleForm.description}
                    onChange={(e) => setModuleForm({ ...moduleForm, description: e.target.value })}
                    className="mt-1 block w-full rounded-md border-gray-300 shadow-sm"
                    rows={3}
                  />
                </div>
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    checked={moduleForm.active}
                    onChange={(e) => setModuleForm({ ...moduleForm, active: e.target.checked })}
                    className="rounded border-gray-300"
                  />
                  <label className="ml-2 text-sm text-gray-700">Activo</label>
                </div>
                <div className="flex space-x-2">
                  <button
                    type="submit"
                    className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
                  >
                    Crear
                  </button>
                  <button
                    type="button"
                    onClick={() => setShowCreateModule(false)}
                    className="bg-gray-300 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-400"
                  >
                    Cancelar
                  </button>
                </div>
              </form>
            </div>
          )}

          <div className="bg-white rounded-lg shadow overflow-hidden">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Nombre</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Código</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Descripción</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Estado</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Acciones</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {Array.isArray(modules) && modules.length > 0 ? modules.map((module) => (
                  <tr key={module.id}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{module.id}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{module.name}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{module.code}</td>
                    <td className="px-6 py-4 text-sm text-gray-500">{module.description}</td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-2 py-1 text-xs rounded-full ${module.active ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                        {module.active ? 'Activo' : 'Inactivo'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      <button
                        onClick={() => toggleModuleActive(module.id, !module.active)}
                        className={`px-3 py-1 rounded ${module.active ? 'bg-red-100 text-red-700 hover:bg-red-200' : 'bg-green-100 text-green-700 hover:bg-green-200'}`}
                      >
                        {module.active ? 'Desactivar' : 'Activar'}
                      </button>
                    </td>
                  </tr>
                )) : (
                  <tr>
                    <td colSpan={6} className="px-6 py-4 text-center text-gray-500">
                      No hay módulos disponibles
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab: Módulos por Empresa */}
      {activeTab === 'companies' && (
        <div className="space-y-4">
          <h2 className="text-2xl font-bold text-gray-900">Módulos por Empresa</h2>
          
          <div className="bg-white rounded-lg shadow p-6">
            <label className="block text-sm font-medium text-gray-700 mb-2">Seleccionar Empresa</label>
            <select
              value={selectedCompany || ''}
              onChange={(e) => setSelectedCompany(Number(e.target.value))}
              className="block w-full rounded-md border-gray-300 shadow-sm"
            >
              <option value="">-- Seleccionar empresa --</option>
              {Array.isArray(companies) && companies.map((company) => (
                <option key={company.id} value={company.id}>
                  {company.nombre} ({company.cuit || 'Sin CUIT'})
                </option>
              ))}
            </select>
          </div>

          {selectedCompany && (
            <div className="bg-white rounded-lg shadow overflow-hidden">
              <div className="p-4 border-b">
                <h3 className="text-lg font-semibold">
                  Módulos de {companies.find(c => c.id === selectedCompany)?.nombre}
                </h3>
              </div>
              <div className="p-6">
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {Array.isArray(modules) && modules.length > 0 ? modules.map((module) => {
                    const companyModule = companyModules.find(cm => cm.moduleId === module.id);
                    const enabled = companyModule?.enabled || false;
                    
                    return (
                      <div key={module.id} className={`border rounded-lg p-4 ${enabled ? 'bg-green-50 border-green-200' : 'bg-gray-50 border-gray-200'}`}>
                        <div className="flex justify-between items-start mb-2">
                          <h4 className="font-semibold">{module.name}</h4>
                          <span className={`px-2 py-1 text-xs rounded ${enabled ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'}`}>
                            {enabled ? 'Habilitado' : 'Deshabilitado'}
                          </span>
                        </div>
                        <p className="text-sm text-gray-600 mb-3">{module.description}</p>
                        <button
                          onClick={() => enabled 
                            ? deshabilitarModuloEmpresa(selectedCompany, module.id)
                            : habilitarModuloEmpresa(selectedCompany, module.id)
                          }
                          className={`w-full px-3 py-2 rounded text-sm ${
                            enabled 
                              ? 'bg-red-100 text-red-700 hover:bg-red-200' 
                              : 'bg-green-100 text-green-700 hover:bg-green-200'
                          }`}
                        >
                          {enabled ? 'Deshabilitar' : 'Habilitar'}
                        </button>
                      </div>
                    );
                  }) : (
                    <div className="col-span-full text-center text-gray-500 py-4">
                      No hay módulos disponibles
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default ModulosManagement;
