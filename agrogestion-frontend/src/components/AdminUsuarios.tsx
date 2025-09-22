import React, { useState, useEffect } from 'react';
import { showNotification } from '../utils/notification';
import api from '../services/api';
import '../styles/admin-forms.css';

interface Usuario {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  estado: string;
  activo: boolean;
  emailVerified: boolean;
  roles: Array<{ id: number; name: string; description: string }>;
  creadoPorId?: number;
  creadoPorNombre?: string;
  createdAt: string;
  updatedAt: string;
  parentUserId?: number;
  parentUserName?: string;
  childUsersCount?: number;
}

interface Role {
  id: number;
  name: string;
  description: string;
}

interface Estadisticas {
  totalUsuarios: number;
  usuariosActivos: number;
  usuariosPendientes: number;
  usuariosSuspendidos: number;
  usuariosEliminados: number;
  porcentajeActivos: number;
  porcentajePendientes: number;
  porcentajeSuspendidos: number;
  porcentajeEliminados: number;
}

const AdminUsuarios: React.FC = () => {
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [estadisticas, setEstadisticas] = useState<Estadisticas | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Estados para filtros
  const [filtroEstado, setFiltroEstado] = useState<string>('');
  const [filtroRol, setFiltroRol] = useState<string>('');
  const [filtroActivo, setFiltroActivo] = useState<boolean | null>(null);
  const [busqueda, setBusqueda] = useState<string>('');

  // Estados para diálogos
  const [dialogCrear, setDialogCrear] = useState(false);
  const [dialogEditar, setDialogEditar] = useState(false);
  const [dialogResetPassword, setDialogResetPassword] = useState(false);
  const [usuarioSeleccionado, setUsuarioSeleccionado] = useState<Usuario | null>(null);

  // Estados para formularios
  const [nuevoUsuario, setNuevoUsuario] = useState({
    username: '',
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    roleIds: [] as number[]
  });

  const [nuevaContraseña, setNuevaContraseña] = useState('');

  // Cargar datos iniciales
  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    try {
      setLoading(true);
      
      // Cargar usuarios usando el servicio de API
      const responseUsuarios = await api.get('/admin/usuarios');
      setUsuarios(responseUsuarios.data);

      // Cargar roles usando el servicio de API
      const responseRoles = await api.get('/admin/usuarios/roles');
      setRoles(responseRoles.data);

      // Cargar estadísticas usando el servicio de API
      const responseEstadisticas = await api.get('/admin/usuarios/estadisticas');
      setEstadisticas(responseEstadisticas.data);

    } catch (error) {
      console.error('Error cargando datos:', error);
      setError('Error al cargar los datos');
      showNotification('Error al cargar los datos', 'error');
    } finally {
      setLoading(false);
    }
  };

  const crearUsuario = async () => {
    try {
      const usuarioData = {
        username: nuevoUsuario.username,
        firstName: nuevoUsuario.firstName,
        lastName: nuevoUsuario.lastName,
        email: nuevoUsuario.email,
        phone: nuevoUsuario.phone,
        roles: nuevoUsuario.roleIds.map(id => ({ id }))
      };

      // Usar el servicio de API
      await api.post('/admin/usuarios', usuarioData);

      showNotification('Usuario creado exitosamente', 'success');
      setDialogCrear(false);
      limpiarFormulario();
      cargarDatos();
    } catch (error) {
      console.error('Error creando usuario:', error);
      showNotification('Error al crear usuario', 'error');
    }
  };

  const actualizarUsuario = async () => {
    if (!usuarioSeleccionado) return;

    try {
      const usuarioData = {
        firstName: usuarioSeleccionado.firstName,
        lastName: usuarioSeleccionado.lastName,
        email: usuarioSeleccionado.email,
        phone: usuarioSeleccionado.phone,
        estado: usuarioSeleccionado.estado,
        activo: usuarioSeleccionado.activo,
        emailVerified: usuarioSeleccionado.emailVerified,
        roles: usuarioSeleccionado.roles
      };

      // Usar el servicio de API
      await api.put(`/admin/usuarios/${usuarioSeleccionado.id}`, usuarioData);

      showNotification('Usuario actualizado exitosamente', 'success');
      setDialogEditar(false);
      cargarDatos();
    } catch (error) {
      console.error('Error actualizando usuario:', error);
      showNotification('Error al actualizar usuario', 'error');
    }
  };

  const cambiarEstadoUsuario = async (id: number, nuevoEstado: string) => {
    try {
      // Usar el servicio de API
      await api.patch(`/admin/usuarios/${id}/estado?estado=${nuevoEstado}`);

      showNotification(`Estado cambiado a ${nuevoEstado}`, 'success');
      cargarDatos();
    } catch (error) {
      console.error('Error cambiando estado:', error);
      showNotification('Error al cambiar estado', 'error');
    }
  };

  const cambiarEstadoActivo = async (id: number, activo: boolean) => {
    try {
      // Usar el servicio de API
      await api.patch(`/admin/usuarios/${id}/activo?activo=${activo}`);

      showNotification(`Usuario ${activo ? 'activado' : 'desactivado'}`, 'success');
      cargarDatos();
    } catch (error) {
      console.error('Error cambiando estado activo:', error);
      showNotification('Error al cambiar estado activo', 'error');
    }
  };

  const resetearContraseña = async () => {
    if (!usuarioSeleccionado || !nuevaContraseña) return;

    try {
      // Usar el servicio de API
      await api.patch(`/admin/usuarios/${usuarioSeleccionado.id}/reset-password?nuevaContraseña=${nuevaContraseña}`);

      showNotification('Contraseña reseteada exitosamente', 'success');
      setDialogResetPassword(false);
      setNuevaContraseña('');
      cargarDatos();
    } catch (error) {
      console.error('Error reseteando contraseña:', error);
      showNotification('Error al resetear contraseña', 'error');
    }
  };


  const limpiarFormulario = () => {
    setNuevoUsuario({
      username: '',
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      roleIds: []
    });
  };

  const filtrarUsuarios = () => {
    return usuarios.filter(usuario => {
      const cumpleEstado = !filtroEstado || usuario.estado === filtroEstado;
      const cumpleRol = !filtroRol || usuario.roles.some(rol => rol.name === filtroRol);
      const cumpleActivo = filtroActivo === null || usuario.activo === filtroActivo;
      const cumpleBusqueda = !busqueda || 
        usuario.firstName.toLowerCase().includes(busqueda.toLowerCase()) ||
        usuario.lastName.toLowerCase().includes(busqueda.toLowerCase()) ||
        usuario.email.toLowerCase().includes(busqueda.toLowerCase()) ||
        usuario.username.toLowerCase().includes(busqueda.toLowerCase());

      return cumpleEstado && cumpleRol && cumpleActivo && cumpleBusqueda;
    });
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
        <strong className="font-bold">Error:</strong>
        <span className="block sm:inline"> {error}</span>
      </div>
    );
  }

  const usuariosFiltrados = filtrarUsuarios();

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="bg-white rounded-lg shadow-lg p-6">
        <h1 className="text-3xl font-bold text-gray-800 mb-6">Administración de Usuarios</h1>
        
        {/* Estadísticas */}
        {estadisticas && (
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
            <div className="bg-blue-100 p-4 rounded-lg">
              <h3 className="text-lg font-semibold text-blue-800">Total Usuarios</h3>
              <p className="text-2xl font-bold text-blue-600">{estadisticas.totalUsuarios}</p>
            </div>
            <div className="bg-green-100 p-4 rounded-lg">
              <h3 className="text-lg font-semibold text-green-800">Activos</h3>
              <p className="text-2xl font-bold text-green-600">{estadisticas.usuariosActivos}</p>
            </div>
            <div className="bg-yellow-100 p-4 rounded-lg">
              <h3 className="text-lg font-semibold text-yellow-800">Pendientes</h3>
              <p className="text-2xl font-bold text-yellow-600">{estadisticas.usuariosPendientes}</p>
            </div>
            <div className="bg-red-100 p-4 rounded-lg">
              <h3 className="text-lg font-semibold text-red-800">Suspendidos</h3>
              <p className="text-2xl font-bold text-red-600">{estadisticas.usuariosSuspendidos}</p>
            </div>
          </div>
        )}

        {/* Filtros */}
        <div className="bg-gray-50 p-4 rounded-lg mb-6">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <input
              type="text"
              placeholder="Buscar usuarios..."
              value={busqueda}
              onChange={(e) => setBusqueda(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <select
              value={filtroEstado}
              onChange={(e) => setFiltroEstado(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">Todos los estados</option>
              <option value="ACTIVO">Activo</option>
              <option value="PENDIENTE">Pendiente</option>
              <option value="SUSPENDIDO">Suspendido</option>
            </select>
            <select
              value={filtroRol}
              onChange={(e) => setFiltroRol(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">Todos los roles</option>
              {roles.map(rol => (
                <option key={rol.id} value={rol.name}>{rol.name}</option>
              ))}
            </select>
            <button
              onClick={() => setDialogCrear(true)}
              className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              Crear Usuario
            </button>
          </div>
        </div>

        {/* Tabla de usuarios */}
        <div className="overflow-x-auto">
          <table className="min-w-full bg-white border border-gray-300">
            <thead>
              <tr className="bg-gray-100">
                <th className="px-6 py-3 border-b border-gray-300 text-left text-xs leading-4 font-medium text-gray-500 uppercase tracking-wider">
                  Usuario
                </th>
                <th className="px-6 py-3 border-b border-gray-300 text-left text-xs leading-4 font-medium text-gray-500 uppercase tracking-wider">
                  Email
                </th>
                <th className="px-6 py-3 border-b border-gray-300 text-left text-xs leading-4 font-medium text-gray-500 uppercase tracking-wider">
                  Roles
                </th>
                <th className="px-6 py-3 border-b border-gray-300 text-left text-xs leading-4 font-medium text-gray-500 uppercase tracking-wider">
                  Estado
                </th>
                <th className="px-6 py-3 border-b border-gray-300 text-left text-xs leading-4 font-medium text-gray-500 uppercase tracking-wider">
                  Acciones
                </th>
              </tr>
            </thead>
            <tbody className="bg-white">
              {usuariosFiltrados.map((usuario) => (
                <tr key={usuario.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div>
                      <div className="text-sm font-medium text-gray-900">
                        {usuario.firstName} {usuario.lastName}
                      </div>
                      <div className="text-sm text-gray-500">@{usuario.username}</div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {usuario.email}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex flex-wrap gap-1">
                      {usuario.roles.map((rol) => (
                        <span
                          key={rol.id}
                          className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800"
                        >
                          {rol.name}
                        </span>
                      ))}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                      usuario.estado === 'ACTIVO' ? 'bg-green-100 text-green-800' :
                      usuario.estado === 'PENDIENTE' ? 'bg-yellow-100 text-yellow-800' :
                      'bg-red-100 text-red-800'
                    }`}>
                      {usuario.estado}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <div className="flex flex-col space-y-1">
                      <div className="flex space-x-2">
                        <button
                          onClick={() => {
                            setUsuarioSeleccionado(usuario);
                            setDialogEditar(true);
                          }}
                          className="text-indigo-600 hover:text-indigo-900 text-xs"
                        >
                          Editar
                        </button>
                        <button
                          onClick={() => {
                            setUsuarioSeleccionado(usuario);
                            setDialogResetPassword(true);
                          }}
                          className="text-yellow-600 hover:text-yellow-900 text-xs"
                        >
                          Reset
                        </button>
                      </div>
                      <select
                        value={usuario.estado}
                        onChange={(e) => cambiarEstadoUsuario(usuario.id, e.target.value)}
                        className="text-xs border border-gray-300 rounded px-1 py-1 focus:outline-none focus:ring-1 focus:ring-blue-500 w-full"
                        title="Cambiar estado del usuario"
                      >
                        <option value="PENDIENTE">Pendiente</option>
                        <option value="ACTIVO">Activo</option>
                        <option value="SUSPENDIDO">Suspendido</option>
                        <option value="ELIMINADO">Eliminado</option>
                      </select>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Diálogo de crear usuario */}
        {dialogCrear && (
          <div className="admin-modal">
            <div className="admin-modal-content">
              <div className="admin-modal-header">
                <h3 className="admin-modal-title">👤 Crear Nuevo Usuario</h3>
                <button
                  onClick={() => setDialogCrear(false)}
                  className="admin-modal-close"
                >
                  ✕
                </button>
              </div>
              
              <div className="admin-form-grid">
                <div className="admin-field-group">
                  <label className="admin-label">Username *</label>
                  <input
                    type="text"
                    placeholder="Nombre de usuario"
                    value={nuevoUsuario.username}
                    onChange={(e) => setNuevoUsuario({...nuevoUsuario, username: e.target.value})}
                    className="admin-input"
                    required
                  />
                  <div className="admin-help-text">Identificador único del usuario</div>
                </div>

                <div className="admin-field-group">
                  <label className="admin-label">Nombre *</label>
                  <input
                    type="text"
                    placeholder="Nombre"
                    value={nuevoUsuario.firstName}
                    onChange={(e) => setNuevoUsuario({...nuevoUsuario, firstName: e.target.value})}
                    className="admin-input"
                    required
                  />
                  <div className="admin-help-text">Nombre de pila</div>
                </div>

                <div className="admin-field-group">
                  <label className="admin-label">Apellido *</label>
                  <input
                    type="text"
                    placeholder="Apellido"
                    value={nuevoUsuario.lastName}
                    onChange={(e) => setNuevoUsuario({...nuevoUsuario, lastName: e.target.value})}
                    className="admin-input"
                    required
                  />
                  <div className="admin-help-text">Apellido del usuario</div>
                </div>

                <div className="admin-field-group">
                  <label className="admin-label">Email *</label>
                  <input
                    type="email"
                    placeholder="usuario@ejemplo.com"
                    value={nuevoUsuario.email}
                    onChange={(e) => setNuevoUsuario({...nuevoUsuario, email: e.target.value})}
                    className="admin-input"
                    required
                  />
                  <div className="admin-help-text">Email válido para autenticación</div>
                </div>

                <div className="admin-field-group" style={{gridColumn: '1 / -1'}}>
                  <label className="admin-label">Teléfono</label>
                  <input
                    type="tel"
                    placeholder="+54 11 1234-5678"
                    value={nuevoUsuario.phone}
                    onChange={(e) => setNuevoUsuario({...nuevoUsuario, phone: e.target.value})}
                    className="admin-input"
                  />
                  <div className="admin-help-text">Número de teléfono opcional</div>
                </div>
              </div>

              <div className="admin-btn-group">
                <button
                  onClick={() => setDialogCrear(false)}
                  className="admin-btn admin-btn-cancel"
                >
                  Cancelar
                </button>
                <button
                  onClick={crearUsuario}
                  className="admin-btn admin-btn-primary"
                >
                  Crear Usuario
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Diálogo de editar usuario */}
        {dialogEditar && usuarioSeleccionado && (
          <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
            <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
              <div className="mt-3">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Editar Usuario</h3>
                <div className="space-y-4">
                  <input
                    type="text"
                    placeholder="Nombre"
                    value={usuarioSeleccionado.firstName}
                    onChange={(e) => setUsuarioSeleccionado({...usuarioSeleccionado, firstName: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md"
                  />
                  <input
                    type="text"
                    placeholder="Apellido"
                    value={usuarioSeleccionado.lastName}
                    onChange={(e) => setUsuarioSeleccionado({...usuarioSeleccionado, lastName: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md"
                  />
                  <input
                    type="email"
                    placeholder="Email"
                    value={usuarioSeleccionado.email}
                    onChange={(e) => setUsuarioSeleccionado({...usuarioSeleccionado, email: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md"
                  />
                  <input
                    type="tel"
                    placeholder="Teléfono"
                    value={usuarioSeleccionado.phone}
                    onChange={(e) => setUsuarioSeleccionado({...usuarioSeleccionado, phone: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md"
                  />
                </div>
                <div className="flex justify-end space-x-3 mt-6">
                  <button
                    onClick={() => setDialogEditar(false)}
                    className="px-4 py-2 text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50"
                  >
                    Cancelar
                  </button>
                  <button
                    onClick={actualizarUsuario}
                    className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                  >
                    Actualizar
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Diálogo de reset password */}
        {dialogResetPassword && usuarioSeleccionado && (
          <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
            <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
              <div className="mt-3">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Resetear Contraseña</h3>
                <p className="text-sm text-gray-600 mb-4">
                  Nueva contraseña para {usuarioSeleccionado.username}
                </p>
                <input
                  type="password"
                  placeholder="Nueva contraseña"
                  value={nuevaContraseña}
                  onChange={(e) => setNuevaContraseña(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md"
                />
                <div className="flex justify-end space-x-3 mt-6">
                  <button
                    onClick={() => setDialogResetPassword(false)}
                    className="px-4 py-2 text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50"
                  >
                    Cancelar
                  </button>
                  <button
                    onClick={resetearContraseña}
                    className="px-4 py-2 bg-yellow-600 text-white rounded-md hover:bg-yellow-700"
                  >
                    Resetear
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminUsuarios;
