import React, { useState } from 'react';
import { useEmpresa } from '../contexts/EmpresaContext';

interface EmpresaSelectorProps {
  onEmpresaSeleccionada?: (empresaId: number) => void;
  mostrarTodasLasEmpresas?: boolean;
}

const EmpresaSelector: React.FC<EmpresaSelectorProps> = ({ 
  onEmpresaSeleccionada, 
  mostrarTodasLasEmpresas = false 
}) => {
  const { 
    empresaActiva, 
    empresasUsuario, 
    cambiarEmpresa, 
    loading, 
    error 
  } = useEmpresa();
  
  const [mostrarSelector, setMostrarSelector] = useState(false);

  const handleCambiarEmpresa = async (empresaId: number) => {
    try {
      await cambiarEmpresa(empresaId);
      setMostrarSelector(false);
      if (onEmpresaSeleccionada) {
        onEmpresaSeleccionada(empresaId);
      }
    } catch (error) {
      console.error('Error cambiando empresa:', error);
    }
  };

  const getEstadoColor = (estado: string) => {
    switch (estado) {
      case 'ACTIVO': return 'text-green-600 bg-green-100';
      case 'TRIAL': return 'text-blue-600 bg-blue-100';
      case 'SUSPENDIDO': return 'text-red-600 bg-red-100';
      case 'INACTIVO': return 'text-gray-600 bg-gray-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  const getRolColor = (rol: string) => {
    switch (rol) {
      case 'ADMINISTRADOR': return 'text-purple-600 bg-purple-100';
      case 'ASESOR': return 'text-blue-600 bg-blue-100';
      case 'CONTADOR': return 'text-green-600 bg-green-100';
      case 'TECNICO': return 'text-yellow-600 bg-yellow-100';
      case 'OPERARIO': return 'text-orange-600 bg-orange-100';
      case 'LECTURA': return 'text-gray-600 bg-gray-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  if (loading) {
    return (
      <div className="flex items-center space-x-2">
        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
        <span className="text-sm text-gray-600">Cargando empresas...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-red-600 text-sm">
        ❌ {error}
      </div>
    );
  }

  if (!empresaActiva || empresasUsuario.length === 0) {
    return (
      <div className="text-gray-600 text-sm">
        No hay empresas disponibles
      </div>
    );
  }

  return (
    <div className="relative">
      {/* Botón selector */}
      <button
        onClick={() => setMostrarSelector(!mostrarSelector)}
        className="flex items-center space-x-2 px-3 py-2 bg-white border border-gray-300 rounded-md shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 min-w-[200px]"
      >
        <div className="flex items-center space-x-2">
          <div className="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
            <span className="text-blue-600 text-sm font-medium">
              {empresaActiva.nombre?.charAt(0).toUpperCase() || 'E'}
            </span>
          </div>
          <div className="text-left">
            <div className="text-sm font-medium text-gray-900">
              {empresaActiva.nombre}
            </div>
            <div className="text-xs text-gray-500">
              {empresasUsuario.find(ue => ue.empresaId === empresaActiva.id)?.rol}
            </div>
          </div>
        </div>
        <svg 
          className={`w-4 h-4 text-gray-400 transition-transform ${mostrarSelector ? 'rotate-180' : ''}`}
          fill="none" 
          stroke="currentColor" 
          viewBox="0 0 24 24"
        >
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
        </svg>
      </button>

      {/* Dropdown selector */}
      {mostrarSelector && (
        <div className="absolute top-full left-0 mt-1 w-80 bg-white border border-gray-200 rounded-md shadow-lg z-50">
          <div className="p-3 border-b border-gray-200">
            <h3 className="text-sm font-medium text-gray-900">
              Seleccionar Empresa
            </h3>
            <p className="text-xs text-gray-500 mt-1">
              Cambia entre las empresas a las que tienes acceso
            </p>
          </div>
          
          <div className="max-h-64 overflow-y-auto">
            {empresasUsuario.map((usuarioEmpresa) => (
              <button
                key={usuarioEmpresa.id}
                onClick={() => handleCambiarEmpresa(usuarioEmpresa.id)}
                className={`w-full px-3 py-3 text-left hover:bg-gray-50 border-b border-gray-100 last:border-b-0 ${
                  empresaActiva.id === usuarioEmpresa.empresaId ? 'bg-blue-50' : ''
                }`}
              >
                <div className="flex items-center space-x-3">
                  <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                    <span className="text-blue-600 text-sm font-medium">
                      {usuarioEmpresa.empresaNombre.charAt(0).toUpperCase()}
                    </span>
                  </div>
                  
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center space-x-2">
                      <p className="text-sm font-medium text-gray-900 truncate">
                        {usuarioEmpresa.empresaNombre}
                      </p>
                      {empresaActiva.id === usuarioEmpresa.empresaId && (
                        <span className="text-blue-600 text-xs">✓</span>
                      )}
                    </div>
                    
                    <div className="flex items-center space-x-2 mt-1">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getRolColor(usuarioEmpresa.rol)}`}>
                        {usuarioEmpresa.rol}
                      </span>
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getEstadoColor(usuarioEmpresa.estado)}`}>
                        {usuarioEmpresa.estado}
                      </span>
                    </div>
                    
                    {usuarioEmpresa.cuit && (
                      <p className="text-xs text-gray-500 mt-1">
                        CUIT: {usuarioEmpresa.cuit}
                      </p>
                    )}
                  </div>
                </div>
              </button>
            ))}
          </div>
          
          <div className="p-3 border-t border-gray-200 bg-gray-50">
            <div className="flex items-center justify-between text-xs text-gray-500">
              <span>{empresasUsuario.length} empresa{empresasUsuario.length !== 1 ? 's' : ''} disponible{empresasUsuario.length !== 1 ? 's' : ''}</span>
              <span>Empresa activa: {empresaActiva.nombre}</span>
            </div>
          </div>
        </div>
      )}

      {/* Overlay para cerrar el dropdown */}
      {mostrarSelector && (
        <div 
          className="fixed inset-0 z-40" 
          onClick={() => setMostrarSelector(false)}
        />
      )}
    </div>
  );
};

export default EmpresaSelector;
