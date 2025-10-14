import React, { useState, useEffect, useMemo, useCallback, memo } from 'react';
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
  roles?: Array<{ id: number; name: string; description: string }>;
  roleIds?: number[];
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
    password: '',
    roleIds: [] as number[]
  });

  const [nuevaContraseña, setNuevaContraseña] = useState('');

  // Persistencia de estado del modal y formulario para evitar cierre por remount
  useEffect(() => {
    try {
      const persistedCrear = localStorage.getItem('adminUsuarios.dialogCrear');
      const persistedForm = localStorage.getItem('adminUsuarios.nuevoUsuario');
      if (persistedCrear === '1') {
        setDialogCrear(true);
      }
      if (persistedForm) {
        const parsed = JSON.parse(persistedForm);
        if (parsed && typeof parsed === 'object') {
          setNuevoUsuario(prev => ({
            ...prev,
            ...parsed,
            roleIds: Array.isArray(parsed.roleIds) ? parsed.roleIds : []
          }));
        }
      }
    } catch (e) {
      console.warn('⚠️ [AdminUsuarios] No se pudo restaurar estado persistido', e);
    }
  }, []);

  useEffect(() => {
    try {
      localStorage.setItem('adminUsuarios.dialogCrear', dialogCrear ? '1' : '0');
    } catch {}
  }, [dialogCrear]);

  useEffect(() => {
    try {
      localStorage.setItem('adminUsuarios.nuevoUsuario', JSON.stringify(nuevoUsuario));
    } catch {}
  }, [nuevoUsuario]);

  // Función para filtrar usuarios (memoizada)
  const filtrarUsuarios = useCallback(() => {
    // Verificar que usuarios sea un array
    if (!Array.isArray(usuarios)) {
      console.error('❌ [AdminUsuarios] usuarios no es un array:', usuarios);
      return [];
    }
    
    return usuarios.filter(usuario => {
      const cumpleEstado = !filtroEstado || usuario.estado === filtroEstado;
      const cumpleRol = !filtroRol || (Array.isArray(usuario.roles) && usuario.roles.some(rol => rol.name === filtroRol));
      const cumpleActivo = filtroActivo === null || usuario.activo === filtroActivo;
      const cumpleBusqueda = !busqueda || 
        usuario.firstName.toLowerCase().includes(busqueda.toLowerCase()) ||
        usuario.lastName.toLowerCase().includes(busqueda.toLowerCase()) ||
        usuario.email.toLowerCase().includes(busqueda.toLowerCase()) ||
        usuario.username.toLowerCase().includes(busqueda.toLowerCase());

      return cumpleEstado && cumpleRol && cumpleActivo && cumpleBusqueda;
    });
  }, [usuarios, filtroEstado, filtroRol, filtroActivo, busqueda]);

  // Memoizar usuarios filtrados
  const usuariosFiltrados = useMemo(() => {
    return filtrarUsuarios();
  }, [filtrarUsuarios]);

  // Cargar datos iniciales
  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    try {
      setLoading(true);
      
      // Cargar usuarios usando el servicio de API
      try {
        const responseUsuarios = await api.get('/api/admin/usuarios');
        const usuariosData = Array.isArray(responseUsuarios.data) ? responseUsuarios.data : [];
        console.log('📊 [AdminUsuarios] Usuarios cargados del backend:', usuariosData);
        if (usuariosData.length > 0) {
          console.log('📊 [AdminUsuarios] Primer usuario con roles:', usuariosData[0]);
        }
        setUsuarios(usuariosData);
      } catch (error) {
        console.error('❌ [AdminUsuarios] Error cargando usuarios:', error);
        setUsuarios([]);
      }

      // Cargar roles usando el servicio de API
      try {
        const responseRoles = await api.get('/api/admin/usuarios/roles');
        const rolesData = Array.isArray(responseRoles.data) ? responseRoles.data : [];
        setRoles(rolesData);
      } catch (error) {
        console.error('❌ [AdminUsuarios] Error cargando roles:', error);
        setRoles([]);
      }

      // Cargar estadísticas usando el servicio de API
      const responseEstadisticas = await api.get('/api/admin/usuarios/estadisticas');
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
    if (!nuevoUsuario.email || !nuevoUsuario.firstName || !nuevoUsuario.lastName || nuevoUsuario.roleIds.length === 0) {
      showNotification('Por favor complete todos los campos obligatorios', 'error');
      return;
    }

    if (!nuevoUsuario.password || nuevoUsuario.password.length < 6) {
      showNotification('La contraseña debe tener al menos 6 caracteres', 'error');
      return;
    }

    try {
      const usuarioData = {
        username: nuevoUsuario.username || nuevoUsuario.email,
        firstName: nuevoUsuario.firstName,
        lastName: nuevoUsuario.lastName,
        email: nuevoUsuario.email,
        phone: nuevoUsuario.phone,
        password: nuevoUsuario.password,
        roleIds: nuevoUsuario.roleIds
      };

      // Usar el servicio de API
      await api.post('/api/admin/usuarios', usuarioData);

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
        roleIds: usuarioSeleccionado.roleIds // Enviar roleIds en lugar de roles
      };

      // Usar el servicio de API
      await api.put(`/api/admin/usuarios/${usuarioSeleccionado.id}`, usuarioData);

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
      await api.patch(`/api/admin/usuarios/${id}/estado?estado=${nuevoEstado}`);

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
      await api.patch(`/api/admin/usuarios/${id}/activo?activo=${activo}`);

      showNotification(`Usuario ${activo ? 'activado' : 'desactivado'}`, 'success');
      cargarDatos();
    } catch (error) {
      console.error('Error cambiando estado activo:', error);
      showNotification('Error al cambiar estado activo', 'error');
    }
  };

  const resetearContraseña = async () => {
    if (!usuarioSeleccionado || !nuevaContraseña) return;

    if (nuevaContraseña.length < 6) {
      showNotification('La contraseña debe tener al menos 6 caracteres', 'error');
      return;
    }

    try {
      // Usar el servicio de API
      await api.patch(`/api/admin/usuarios/${usuarioSeleccionado.id}/reset-password?nuevaContraseña=${nuevaContraseña}`);

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
      password: '',
      roleIds: []
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

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="bg-white rounded-lg shadow-lg p-6">
        <h1 className="text-3xl font-bold text-gray-800 mb-6">Administración de Usuarios</h1>
        
        {/* Estadísticas */}
        {estadisticas && (
          <div style={{ 
            display: 'grid', 
            gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', 
            gap: '16px', 
            marginBottom: '24px' 
          }}>
            <div style={{ 
              background: '#dbeafe', 
              padding: '16px', 
              borderRadius: '8px',
              border: '1px solid #93c5fd'
            }}>
              <h3 style={{ 
                fontSize: '16px', 
                fontWeight: '600', 
                color: '#1e40af', 
                margin: '0 0 8px 0' 
              }}>Total Usuarios</h3>
              <p style={{ 
                fontSize: '24px', 
                fontWeight: 'bold', 
                color: '#2563eb', 
                margin: '0' 
              }}>{estadisticas.totalUsuarios}</p>
            </div>
            <div style={{ 
              background: '#dcfce7', 
              padding: '16px', 
              borderRadius: '8px',
              border: '1px solid #86efac'
            }}>
              <h3 style={{ 
                fontSize: '16px', 
                fontWeight: '600', 
                color: '#166534', 
                margin: '0 0 8px 0' 
              }}>Activos</h3>
              <p style={{ 
                fontSize: '24px', 
                fontWeight: 'bold', 
                color: '#16a34a', 
                margin: '0' 
              }}>{estadisticas.usuariosActivos}</p>
            </div>
            <div style={{ 
              background: '#fef3c7', 
              padding: '16px', 
              borderRadius: '8px',
              border: '1px solid #fcd34d'
            }}>
              <h3 style={{ 
                fontSize: '16px', 
                fontWeight: '600', 
                color: '#92400e', 
                margin: '0 0 8px 0' 
              }}>Pendientes</h3>
              <p style={{ 
                fontSize: '24px', 
                fontWeight: 'bold', 
                color: '#d97706', 
                margin: '0' 
              }}>{estadisticas.usuariosPendientes}</p>
            </div>
            <div style={{ 
              background: '#fee2e2', 
              padding: '16px', 
              borderRadius: '8px',
              border: '1px solid #fca5a5'
            }}>
              <h3 style={{ 
                fontSize: '16px', 
                fontWeight: '600', 
                color: '#991b1b', 
                margin: '0 0 8px 0' 
              }}>Suspendidos</h3>
              <p style={{ 
                fontSize: '24px', 
                fontWeight: 'bold', 
                color: '#dc2626', 
                margin: '0' 
              }}>{estadisticas.usuariosSuspendidos}</p>
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
              {Array.isArray(roles) && roles.map(rol => (
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
                      {Array.isArray(usuario.roles) && usuario.roles.map((rol) => (
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
                            // Mapear roles a roleIds para el formulario
                            const usuarioConRoleIds = {
                              ...usuario,
                              roleIds: usuario.roles ? usuario.roles.map(r => r.id) : []
                            };
                            console.log('📝 [AdminUsuarios] Usuario seleccionado para editar:', usuarioConRoleIds);
                            console.log('📝 [AdminUsuarios] Roles del usuario:', usuario.roles);
                            setUsuarioSeleccionado(usuarioConRoleIds);
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

                <div className="admin-field-group">
                  <label className="admin-label">Contraseña *</label>
                  <input
                    type="password"
                    placeholder="Mínimo 6 caracteres"
                    value={nuevoUsuario.password}
                    onChange={(e) => setNuevoUsuario({...nuevoUsuario, password: e.target.value})}
                    className="admin-input"
                    required
                    minLength={6}
                  />
                  <div className="admin-help-text">Contraseña para el primer acceso (mínimo 6 caracteres)</div>
                </div>

                <div className="admin-field-group">
                  <label className="admin-label">Rol *</label>
                  <select
                    value={nuevoUsuario.roleIds[0] || ''}
                    onChange={(e) => setNuevoUsuario({...nuevoUsuario, roleIds: [parseInt(e.target.value)]})}
                    className="admin-input"
                    required
                  >
                    <option value="">Seleccionar rol</option>
                    {Array.isArray(roles) && roles.map(rol => (
                      <option key={rol.id} value={rol.id}>
                        {rol.name} - {rol.description}
                      </option>
                    ))}
                  </select>
                  <div className="admin-help-text">Rol que tendrá el usuario en el sistema</div>
                </div>

                <div className="admin-field-group">
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
          <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50 flex items-center justify-center">
            <div className="relative mx-auto p-5 border w-96 max-h-[90vh] overflow-y-auto shadow-lg rounded-md bg-white">
              <div className="mt-3">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Editar Usuario</h3>
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Nombre</label>
                    <input
                      type="text"
                      placeholder="Nombre"
                      value={usuarioSeleccionado.firstName}
                      onChange={(e) => setUsuarioSeleccionado({...usuarioSeleccionado, firstName: e.target.value})}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Apellido</label>
                    <input
                      type="text"
                      placeholder="Apellido"
                      value={usuarioSeleccionado.lastName}
                      onChange={(e) => setUsuarioSeleccionado({...usuarioSeleccionado, lastName: e.target.value})}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                    <input
                      type="email"
                      placeholder="Email"
                      value={usuarioSeleccionado.email}
                      onChange={(e) => setUsuarioSeleccionado({...usuarioSeleccionado, email: e.target.value})}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Rol</label>
                    <select
                      value={usuarioSeleccionado.roleIds?.[0] || ''}
                      onChange={(e) => setUsuarioSeleccionado({
                        ...usuarioSeleccionado, 
                        roleIds: e.target.value ? [parseInt(e.target.value)] : []
                      })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    >
                      <option value="">Seleccionar rol</option>
                      {Array.isArray(roles) && roles.map(rol => (
                        <option key={rol.id} value={rol.id}>
                          {rol.name} - {rol.description}
                        </option>
                      ))}
                    </select>
                    <p className="text-xs text-gray-500 mt-1">Cambiar el rol del usuario en el sistema</p>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Teléfono</label>
                    <input
                      type="tel"
                      placeholder="Teléfono"
                      value={usuarioSeleccionado.phone}
                      onChange={(e) => setUsuarioSeleccionado({...usuarioSeleccionado, phone: e.target.value})}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md"
                    />
                  </div>
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

export default memo(AdminUsuarios);