import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import '../styles/admin-forms.css';

interface Empresa {
  id: number;
  nombre: string;
  cuit: string;
  emailContacto: string;
  estado: string;
  activo: boolean;
  fechaCreacion: string;
}

interface Usuario {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  activo: boolean;
}

interface UsuarioEmpresa {
  id: number;
  usuarioId: number;
  usuarioEmail: string;
  usuarioNombre: string;
  empresaId: number;
  empresaNombre: string;
  rol: string;
  estado: string;
  fechaInicio: string;
}

const AdminEmpresas: React.FC = () => {
  const { user } = useAuth();
  const [empresas, setEmpresas] = useState<Empresa[]>([]);
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [usuariosEmpresas, setUsuariosEmpresas] = useState<UsuarioEmpresa[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Estados para formularios
  const [showCrearEmpresaForm, setShowCrearEmpresaForm] = useState(false);
  const [nuevaEmpresaForm, setNuevaEmpresaForm] = useState({
    nombre: '',
    cuit: '',
    emailContacto: '',
    telefono: '',
    direccion: '',
    descripcion: ''
  });

  const [showAsignarForm, setShowAsignarForm] = useState(false);
  const [asignacionForm, setAsignacionForm] = useState({
    usuarioId: 0,
    empresaId: 0,
    rol: 'OPERARIO'
  });

  // Estados para la nueva interfaz de asignación
  const [busquedaUsuario, setBusquedaUsuario] = useState('');
  const [busquedaEmpresa, setBusquedaEmpresa] = useState('');
  const [usuariosFiltrados, setUsuariosFiltrados] = useState<Usuario[]>([]);
  const [empresasFiltradas, setEmpresasFiltradas] = useState<Empresa[]>([]);
  const [usuarioSeleccionado, setUsuarioSeleccionado] = useState<Usuario | null>(null);
  const [empresaSeleccionada, setEmpresaSeleccionada] = useState<Empresa | null>(null);

  const [showCambiarRolForm, setShowCambiarRolForm] = useState(false);
  const [cambioRolForm, setCambioRolForm] = useState({
    usuarioId: 0,
    empresaId: 0,
    nuevoRol: 'OPERARIO'
  });

  const [selectedEmpresa, setSelectedEmpresa] = useState<number | null>(null);

  const [rolesEmpresa, setRolesEmpresa] = useState([
    { value: 'ADMINISTRADOR', label: 'Administrador de Empresa' },
    { value: 'ASESOR', label: 'Asesor/Ingeniero Agrónomo' },
    { value: 'OPERARIO', label: 'Operario' },
    { value: 'TECNICO', label: 'Técnico' },
    { value: 'CONTADOR', label: 'Contador' },
    { value: 'LECTURA', label: 'Solo Lectura' },
    { value: 'PRODUCTOR', label: 'Productor' }
  ]);

  useEffect(() => {
    cargarDatos();
  }, []);

  // Efectos para filtrado
  useEffect(() => {
    if (busquedaUsuario.trim() === '') {
      setUsuariosFiltrados(usuarios);
    } else {
      const filtrados = usuarios.filter(usuario => 
        usuario.firstName.toLowerCase().includes(busquedaUsuario.toLowerCase()) ||
        usuario.lastName.toLowerCase().includes(busquedaUsuario.toLowerCase()) ||
        usuario.email.toLowerCase().includes(busquedaUsuario.toLowerCase()) ||
        usuario.username.toLowerCase().includes(busquedaUsuario.toLowerCase())
      );
      setUsuariosFiltrados(filtrados);
    }
  }, [busquedaUsuario, usuarios]);

  useEffect(() => {
    if (busquedaEmpresa.trim() === '') {
      setEmpresasFiltradas(empresas);
    } else {
      const filtrados = empresas.filter(empresa => 
        empresa.nombre.toLowerCase().includes(busquedaEmpresa.toLowerCase()) ||
        empresa.cuit.toLowerCase().includes(busquedaEmpresa.toLowerCase()) ||
        empresa.emailContacto.toLowerCase().includes(busquedaEmpresa.toLowerCase())
      );
      setEmpresasFiltradas(filtrados);
    }
  }, [busquedaEmpresa, empresas]);

  const cargarDatos = async () => {
    try {
      setLoading(true);
      
      // Cargar empresas del usuario
      const responseEmpresas = await api.get('/api/v1/empresas/mis-empresas');
      // Transformar UsuarioEmpresaDTO a formato de empresa
      const empresasTransformadas = responseEmpresas.data.map((relacion: any) => ({
        id: relacion.empresaId,
        nombre: relacion.empresaNombre,
        cuit: relacion.empresaCuit,
        emailContacto: relacion.empresaEmail,
        activo: relacion.estado === 'ACTIVO'
      }));
      setEmpresas(empresasTransformadas);

      // Cargar usuarios - usar endpoint básico
      const responseUsuarios = await api.get('/api/admin/usuarios/basic');
      setUsuarios(responseUsuarios.data);

      // Cargar roles de empresa dinámicamente
      await cargarRolesEmpresa();

      // Cargar relaciones usuario-empresa para todas las empresas
      await cargarUsuariosEmpresas();

    } catch (error) {
      console.error('Error cargando datos:', error);
      setError('Error al cargar los datos');
    } finally {
      setLoading(false);
    }
  };

  const cargarRolesEmpresa = async () => {
    try {
      const response = await api.get('/api/roles-empresa');
      setRolesEmpresa(response.data);
    } catch (error) {
      console.error('Error cargando roles de empresa:', error);
      // Mantener roles por defecto si falla la carga
    }
  };

  const cargarUsuariosEmpresas = async () => {
    try {
      // Cargar todas las relaciones usuario-empresa
      const response = await api.get('/api/empresa-usuario/todas-relaciones');
      if (response.data.success) {
        setUsuariosEmpresas(response.data.data);
      }
    } catch (error) {
      console.error('Error cargando usuarios de empresas:', error);
      // Si no existe el endpoint, intentar con la empresa por defecto
      try {
        const response = await api.get('/api/empresa-usuario/empresa/1/usuarios');
        if (response.data.success) {
          setUsuariosEmpresas(response.data.data);
        }
      } catch (fallbackError) {
        console.error('Error en fallback:', fallbackError);
      }
    }
  };

  const crearNuevaEmpresa = async () => {
    try {
      const empresaData = {
        nombre: nuevaEmpresaForm.nombre,
        cuit: nuevaEmpresaForm.cuit,
        emailContacto: nuevaEmpresaForm.emailContacto,
        telefonoContacto: nuevaEmpresaForm.telefono,
        direccion: nuevaEmpresaForm.direccion
      };

      const response = await api.post('/api/admin-global/empresas', empresaData);
      
      if (response.data) {
        alert('Empresa creada exitosamente');
        setShowCrearEmpresaForm(false);
        setNuevaEmpresaForm({
          nombre: '',
          cuit: '',
          emailContacto: '',
          telefono: '',
          direccion: '',
          descripcion: ''
        });
        await cargarDatos(); // Recargar la lista de empresas
      } else {
        alert('Error al crear la empresa');
      }
    } catch (error) {
      console.error('Error creando empresa:', error);
      alert('Error al crear la empresa');
    }
  };

  const asignarUsuarioAEmpresa = async () => {
    try {
      const response = await api.post('/api/empresa-usuario/asignar', asignacionForm);
      
      if (response.data.success) {
        alert('Usuario asignado a empresa correctamente');
        setShowAsignarForm(false);
        setAsignacionForm({ usuarioId: 0, empresaId: 0, rol: 'OPERARIO' });
        await cargarUsuariosEmpresas();
        limpiarSeleccion();
      } else {
        alert('Error: ' + response.data.message);
      }
    } catch (error) {
      console.error('Error asignando usuario:', error);
      alert('Error al asignar usuario a empresa');
    }
  };

  const limpiarSeleccion = () => {
    setUsuarioSeleccionado(null);
    setEmpresaSeleccionada(null);
    setBusquedaUsuario('');
    setBusquedaEmpresa('');
    setAsignacionForm({ usuarioId: 0, empresaId: 0, rol: 'OPERARIO' });
  };

  const cambiarRolUsuario = async () => {
    try {
      const response = await api.put('/api/empresa-usuario/cambiar-rol', {
        usuarioId: cambioRolForm.usuarioId,
        empresaId: cambioRolForm.empresaId,
        rol: cambioRolForm.nuevoRol
      });
      
      if (response.data.success) {
        alert('Rol cambiado correctamente');
        setShowCambiarRolForm(false);
        setCambioRolForm({ usuarioId: 0, empresaId: 0, nuevoRol: 'OPERARIO' });
        await cargarUsuariosEmpresas();
      } else {
        alert('Error: ' + response.data.message);
      }
    } catch (error) {
      console.error('Error cambiando rol:', error);
      alert('Error al cambiar rol del usuario');
    }
  };

  const removerUsuarioDeEmpresa = async (usuarioId: number, empresaId: number) => {
    if (!window.confirm('¿Está seguro de que desea remover este usuario de la empresa?')) {
      return;
    }

    try {
      const response = await api.delete(`/empresa-usuario/remover/${usuarioId}/${empresaId}`);
      
      if (response.data.success) {
        alert('Usuario removido de la empresa correctamente');
        await cargarUsuariosEmpresas();
      } else {
        alert('Error: ' + response.data.message);
      }
    } catch (error) {
      console.error('Error removiendo usuario:', error);
      alert('Error al remover usuario de la empresa');
    }
  };

  const getRolColor = (rol: string) => {
    const colors: { [key: string]: string } = {
      'ADMINISTRADOR': 'bg-red-100 text-red-800',
      'PRODUCTOR': 'bg-purple-100 text-purple-800',
      'ASESOR': 'bg-blue-100 text-blue-800',
      'CONTADOR': 'bg-green-100 text-green-800',
      'TECNICO': 'bg-yellow-100 text-yellow-800',
      'OPERARIO': 'bg-gray-100 text-gray-800',
      'LECTURA': 'bg-gray-50 text-gray-600'
    };
    return colors[rol] || 'bg-gray-100 text-gray-800';
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-lg">Cargando...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
        {error}
      </div>
    );
  }

  return (
    <div className="p-6">
      <h1 className="text-3xl font-bold text-gray-900 mb-6">
        Administración de Empresas y Usuarios
      </h1>

      {/* Estadísticas */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-lg font-semibold text-gray-700">Total Empresas</h3>
          <p className="text-3xl font-bold text-blue-600">{empresas.length}</p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-lg font-semibold text-gray-700">Total Usuarios</h3>
          <p className="text-3xl font-bold text-green-600">{usuarios.length}</p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-lg font-semibold text-gray-700">Asignaciones</h3>
          <p className="text-3xl font-bold text-purple-600">{usuariosEmpresas.length}</p>
        </div>
      </div>

      {/* Botones de acción */}
      <div className="mb-6 flex space-x-4">
        {user?.roleName === 'SUPERADMIN' && (
          <button
            onClick={() => setShowCrearEmpresaForm(true)}
            className="bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500"
          >
            🏢 Crear Nueva Empresa
          </button>
        )}
        <button
          onClick={() => setShowAsignarForm(true)}
          className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          ➕ Asignar Usuario a Empresa
        </button>
      </div>

      {/* Lista de empresas */}
      <div className="bg-white rounded-lg shadow mb-6">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-900">Empresas</h2>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Nombre
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  CUIT
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Email
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Estado
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Acciones
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {empresas.map((empresa) => (
                <tr key={empresa.id}>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    {empresa.nombre}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {empresa.cuit}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {empresa.emailContacto}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                      empresa.activo ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                    }`}>
                      {empresa.activo ? 'Activa' : 'Inactiva'}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <button
                      onClick={() => setSelectedEmpresa(empresa.id)}
                      className="text-blue-600 hover:text-blue-900 mr-3"
                    >
                      Ver Usuarios
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Usuarios asignados a empresas */}
      <div className="bg-white rounded-lg shadow">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-900">Usuarios Asignados a Empresas</h2>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Usuario
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Empresa
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Rol
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Estado
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Fecha Inicio
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Acciones
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {usuariosEmpresas.map((relacion) => (
                <tr key={relacion.id}>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div>
                      <div className="text-sm font-medium text-gray-900">
                        {relacion.usuarioNombre}
                      </div>
                      <div className="text-sm text-gray-500">
                        {relacion.usuarioEmail}
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {relacion.empresaNombre}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getRolColor(relacion.rol)}`}>
                      {relacion.rol}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                      relacion.estado === 'ACTIVO' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                    }`}>
                      {relacion.estado}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {new Date(relacion.fechaInicio).toLocaleDateString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <button
                      onClick={() => {
                        setCambioRolForm({
                          usuarioId: relacion.usuarioId,
                          empresaId: relacion.empresaId,
                          nuevoRol: relacion.rol
                        });
                        setShowCambiarRolForm(true);
                      }}
                      className="text-yellow-600 hover:text-yellow-900 mr-3"
                    >
                      Cambiar Rol
                    </button>
                    <button
                      onClick={() => removerUsuarioDeEmpresa(relacion.usuarioId, relacion.empresaId)}
                      className="text-red-600 hover:text-red-900"
                    >
                      Remover
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal para crear empresa */}
      {showCrearEmpresaForm && (
        <div className="admin-modal">
          <div className="admin-modal-content admin-form-large">
            <div className="admin-modal-header">
              <h3 className="admin-modal-title">🏢 Crear Nueva Empresa</h3>
              <button
                onClick={() => setShowCrearEmpresaForm(false)}
                className="admin-modal-close"
              >
                ✕
              </button>
            </div>
            
            <div className="admin-form-grid">
              <div className="admin-field-group">
                <label className="admin-label">Nombre de la Empresa *</label>
                <input
                  type="text"
                  value={nuevaEmpresaForm.nombre}
                  onChange={(e) => setNuevaEmpresaForm({...nuevaEmpresaForm, nombre: e.target.value})}
                  className="admin-input"
                  placeholder="Ej: AgroEmpresa S.A."
                  required
                />
                <div className="admin-help-text">Nombre oficial de la empresa</div>
              </div>

              <div className="admin-field-group">
                <label className="admin-label">CUIT *</label>
                <input
                  type="text"
                  value={nuevaEmpresaForm.cuit}
                  onChange={(e) => setNuevaEmpresaForm({...nuevaEmpresaForm, cuit: e.target.value})}
                  className="admin-input"
                  placeholder="20-12345678-9"
                  required
                />
                <div className="admin-help-text">CUIT con formato XX-XXXXXXXX-X</div>
              </div>

              <div className="admin-field-group">
                <label className="admin-label">Email de Contacto *</label>
                <input
                  type="email"
                  value={nuevaEmpresaForm.emailContacto}
                  onChange={(e) => setNuevaEmpresaForm({...nuevaEmpresaForm, emailContacto: e.target.value})}
                  className="admin-input"
                  placeholder="contacto@empresa.com"
                  required
                />
                <div className="admin-help-text">Email principal de la empresa</div>
              </div>

              <div className="admin-field-group">
                <label className="admin-label">Teléfono</label>
                <input
                  type="text"
                  value={nuevaEmpresaForm.telefono}
                  onChange={(e) => setNuevaEmpresaForm({...nuevaEmpresaForm, telefono: e.target.value})}
                  className="admin-input"
                  placeholder="+54 11 1234-5678"
                />
                <div className="admin-help-text">Teléfono de contacto opcional</div>
              </div>

              <div className="admin-field-group" style={{gridColumn: '1 / -1'}}>
                <label className="admin-label">Dirección</label>
                <input
                  type="text"
                  value={nuevaEmpresaForm.direccion}
                  onChange={(e) => setNuevaEmpresaForm({...nuevaEmpresaForm, direccion: e.target.value})}
                  className="admin-input"
                  placeholder="Av. Principal 123, Ciudad, Provincia"
                />
                <div className="admin-help-text">Dirección completa de la empresa</div>
              </div>

              <div className="admin-field-group" style={{gridColumn: '1 / -1'}}>
                <label className="admin-label">Descripción</label>
                <textarea
                  value={nuevaEmpresaForm.descripcion}
                  onChange={(e) => setNuevaEmpresaForm({...nuevaEmpresaForm, descripcion: e.target.value})}
                  className="admin-textarea-sm"
                  rows={3}
                  placeholder="Descripción de la empresa y sus actividades principales..."
                />
                <div className="admin-help-text">Información adicional sobre la empresa</div>
              </div>
            </div>


            <div className="admin-btn-group">
              <button
                onClick={() => setShowCrearEmpresaForm(false)}
                className="admin-btn admin-btn-cancel"
              >
                Cancelar
              </button>
              <button
                onClick={crearNuevaEmpresa}
                className="admin-btn admin-btn-primary"
              >
                Crear Empresa
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal para asignar usuario - Nueva interfaz mejorada */}
      {showAsignarForm && (
        <div className="admin-modal">
          <div className="admin-modal-content" style={{ width: '90vw', maxWidth: '1200px' }}>
            <div className="admin-modal-header">
              <h3 className="admin-modal-title">➕ Asignar Usuario a Empresa</h3>
              <button
                onClick={() => {
                  setShowAsignarForm(false);
                  limpiarSeleccion();
                }}
                className="admin-modal-close"
              >
                ✕
              </button>
            </div>
            
            <div className="grid grid-cols-1 xl:grid-cols-2 gap-8">
              {/* Panel de Selección de Usuario */}
              <div className="space-y-4">
                <h4 className="text-lg font-semibold text-gray-900 border-b pb-2">
                  👤 Seleccionar Usuario
                </h4>
                
                {/* Búsqueda de Usuario */}
                <div className="admin-field-group">
                  <label className="admin-label">🔍 Buscar Usuario</label>
                  <input
                    type="text"
                    value={busquedaUsuario}
                    onChange={(e) => setBusquedaUsuario(e.target.value)}
                    className="admin-input"
                    placeholder="Buscar por nombre, apellido, email o username..."
                  />
                  <div className="admin-help-text">
                    Escribe para filtrar usuarios ({usuariosFiltrados.length} encontrados)
                  </div>
                </div>

                {/* Lista de Usuarios */}
                <div className="max-h-80 overflow-y-auto border rounded-lg">
                  {usuariosFiltrados.length === 0 ? (
                    <div className="p-4 text-center text-gray-500">
                      {busquedaUsuario ? 'No se encontraron usuarios' : 'No hay usuarios disponibles'}
                    </div>
                  ) : (
                    usuariosFiltrados.map(usuario => (
                      <div
                        key={usuario.id}
                        onClick={() => {
                          setUsuarioSeleccionado(usuario);
                          setAsignacionForm({...asignacionForm, usuarioId: usuario.id});
                        }}
                        className={`p-4 border-b cursor-pointer hover:bg-gray-50 transition-colors ${
                          usuarioSeleccionado?.id === usuario.id ? 'bg-blue-50 border-blue-200' : ''
                        }`}
                      >
                        <div className="flex items-center justify-between">
                          <div>
                            <div className="font-medium text-gray-900">
                              {usuario.firstName} {usuario.lastName}
                            </div>
                            <div className="text-sm text-gray-500">{usuario.email}</div>
                            <div className="text-xs text-gray-400">@{usuario.username}</div>
                          </div>
                          {usuarioSeleccionado?.id === usuario.id && (
                            <div className="text-blue-600">✓</div>
                          )}
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>

              {/* Panel de Selección de Empresa */}
              <div className="space-y-4">
                <h4 className="text-lg font-semibold text-gray-900 border-b pb-2">
                  🏢 Seleccionar Empresa
                </h4>
                
                {/* Búsqueda de Empresa */}
                <div className="admin-field-group">
                  <label className="admin-label">🔍 Buscar Empresa</label>
                  <input
                    type="text"
                    value={busquedaEmpresa}
                    onChange={(e) => setBusquedaEmpresa(e.target.value)}
                    className="admin-input"
                    placeholder="Buscar por nombre, CUIT o email..."
                  />
                  <div className="admin-help-text">
                    Escribe para filtrar empresas ({empresasFiltradas.length} encontradas)
                  </div>
                </div>

                {/* Lista de Empresas */}
                <div className="max-h-80 overflow-y-auto border rounded-lg">
                  {empresasFiltradas.length === 0 ? (
                    <div className="p-4 text-center text-gray-500">
                      {busquedaEmpresa ? 'No se encontraron empresas' : 'No hay empresas disponibles'}
                    </div>
                  ) : (
                    empresasFiltradas.map(empresa => (
                      <div
                        key={empresa.id}
                        onClick={() => {
                          setEmpresaSeleccionada(empresa);
                          setAsignacionForm({...asignacionForm, empresaId: empresa.id});
                        }}
                        className={`p-4 border-b cursor-pointer hover:bg-gray-50 transition-colors ${
                          empresaSeleccionada?.id === empresa.id ? 'bg-green-50 border-green-200' : ''
                        }`}
                      >
                        <div className="flex items-center justify-between">
                          <div>
                            <div className="font-medium text-gray-900">{empresa.nombre}</div>
                            <div className="text-sm text-gray-500">CUIT: {empresa.cuit}</div>
                            <div className="text-xs text-gray-400">{empresa.emailContacto}</div>
                          </div>
                          {empresaSeleccionada?.id === empresa.id && (
                            <div className="text-green-600">✓</div>
                          )}
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            </div>

            {/* Panel de Configuración */}
            {(usuarioSeleccionado || empresaSeleccionada) && (
              <div className="mt-6 p-4 bg-gray-50 rounded-lg">
                <h4 className="text-lg font-semibold text-gray-900 mb-4">
                  ⚙️ Configuración de Asignación
                </h4>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {/* Usuario Seleccionado */}
                  {usuarioSeleccionado && (
                    <div className="p-3 bg-blue-50 rounded-lg">
                      <div className="text-sm font-medium text-blue-900">Usuario Seleccionado:</div>
                      <div className="text-blue-800">
                        {usuarioSeleccionado.firstName} {usuarioSeleccionado.lastName}
                      </div>
                      <div className="text-xs text-blue-600">{usuarioSeleccionado.email}</div>
                    </div>
                  )}

                  {/* Empresa Seleccionada */}
                  {empresaSeleccionada && (
                    <div className="p-3 bg-green-50 rounded-lg">
                      <div className="text-sm font-medium text-green-900">Empresa Seleccionada:</div>
                      <div className="text-green-800">{empresaSeleccionada.nombre}</div>
                      <div className="text-xs text-green-600">CUIT: {empresaSeleccionada.cuit}</div>
                    </div>
                  )}
                </div>

                {/* Selección de Rol */}
                <div className="mt-4">
                  <div className="admin-field-group">
                    <label className="admin-label">🎭 Rol en la Empresa</label>
                    <select
                      value={asignacionForm.rol}
                      onChange={(e) => setAsignacionForm({...asignacionForm, rol: e.target.value})}
                      className="admin-select"
                    >
                      {rolesEmpresa.map(rol => (
                        <option key={rol.value} value={rol.value}>
                          {rol.label}
                        </option>
                      ))}
                    </select>
                    <div className="admin-help-text">Define el rol y permisos del usuario en la empresa</div>
                  </div>
                </div>
              </div>
            )}

            {/* Botones de Acción */}
            <div className="admin-btn-group mt-6">
              <button
                onClick={() => {
                  setShowAsignarForm(false);
                  limpiarSeleccion();
                }}
                className="admin-btn admin-btn-cancel"
              >
                Cancelar
              </button>
              <button
                onClick={asignarUsuarioAEmpresa}
                disabled={!usuarioSeleccionado || !empresaSeleccionada}
                className={`admin-btn ${
                  usuarioSeleccionado && empresaSeleccionada 
                    ? 'admin-btn-primary' 
                    : 'admin-btn-disabled'
                }`}
              >
                {usuarioSeleccionado && empresaSeleccionada 
                  ? '✅ Asignar Usuario' 
                  : '⚠️ Selecciona Usuario y Empresa'
                }
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal para cambiar rol */}
      {showCambiarRolForm && (
        <div className="admin-modal">
          <div className="admin-modal-content">
            <div className="admin-modal-header">
              <h3 className="admin-modal-title">🔄 Cambiar Rol de Usuario</h3>
              <button
                onClick={() => setShowCambiarRolForm(false)}
                className="admin-modal-close"
              >
                ✕
              </button>
            </div>
            
            <div className="admin-field-group">
              <label className="admin-label">Nuevo Rol</label>
              <select
                value={cambioRolForm.nuevoRol}
                onChange={(e) => setCambioRolForm({...cambioRolForm, nuevoRol: e.target.value})}
                className="admin-select"
              >
                {rolesEmpresa.map(rol => (
                  <option key={rol.value} value={rol.value}>
                    {rol.label}
                  </option>
                ))}
              </select>
              <div className="admin-help-text">Selecciona el nuevo rol para el usuario</div>
            </div>

            <div className="admin-btn-group">
              <button
                onClick={() => setShowCambiarRolForm(false)}
                className="admin-btn admin-btn-cancel"
              >
                Cancelar
              </button>
              <button
                onClick={cambiarRolUsuario}
                className="admin-btn admin-btn-secondary"
              >
                Cambiar Rol
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminEmpresas;
