import React, { useState, useEffect } from 'react';
import { authService, usuariosService } from '../services/apiServices';
import { Icon } from '../core/components/Icon';

interface User {
  id: number;
  name: string;
  email: string;
  roleName: string;
  permissions: string[];
  active: boolean;
  emailVerified: boolean;
  lastLogin: string;
  createdAt: string;
}

interface Role {
  id: number;
  name: string;
  description: string;
  permissions: string[];
  userCount: number;
}

interface CreateUserRequest {
  name: string;
  email: string;
  password: string;
  roleId: number;
  sendInvitationEmail: boolean;
}

const UsersManagement: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [formData, setFormData] = useState<CreateUserRequest>({
    name: '',
    email: '',
    password: '',
    roleId: 0,
    sendInvitationEmail: true
  });
  const [loading, setLoading] = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterRole, setFilterRole] = useState('');
  const [filterActive, setFilterActive] = useState<boolean | null>(null);
  const [stats, setStats] = useState<any>(null);

  // Cargar datos al montar el componente
  useEffect(() => {
    console.log('🔄 [UsersManagement] useEffect ejecutándose - montando componente');
    loadUsers();
    loadRoles();
    loadStats();
  }, []);

  // Debug: detectar cambios en showForm
  useEffect(() => {
    console.log('📝 [UsersManagement] showForm cambió a:', showForm);
  }, [showForm]);

  // Cargar usuarios
  const loadUsers = async () => {
    try {
      setLoading(true);
      const data = await authService.obtenerUsuarios();
      setUsers(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Error:', error);
    } finally {
      setLoading(false);
    }
  };

  // Cargar roles
  const loadRoles = async () => {
    try {
      const data = await authService.obtenerRoles();
      setRoles(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Error cargando roles:', error);
    }
  };

  // Cargar estadísticas
  const loadStats = async () => {
    try {
      const data = await authService.obtenerEstadisticas();
      setStats(data);
    } catch (error) {
      console.error('Error cargando estadísticas:', error);
    }
  };

  // Crear usuario
  const createUser = async () => {
    console.log('🚀 [UsersManagement] Iniciando creación de usuario...');
    console.log('📝 [UsersManagement] showForm antes:', showForm);
    console.log('📋 [UsersManagement] formData:', formData);
    try {
      setFormLoading(true);
      await usuariosService.crear({
        firstName: formData.name,
        email: formData.email,
        password: formData.password,
        roleIds: [formData.roleId],
      });
      
      console.log('✅ [UsersManagement] Usuario creado exitosamente');
      alert('✅ Usuario creado exitosamente');
      setFormData({ name: '', email: '', password: '', roleId: 0, sendInvitationEmail: true });
      setShowForm(false);
      loadUsers();
      loadStats();
    } catch (error: unknown) {
      console.error('❌ [UsersManagement] Error en catch:', error);
      const err = error as { message?: string; response?: { status?: number; data?: unknown; statusText?: string } };
      if (err.response) {
        console.error('❌ [UsersManagement] Error response:', err.response.status, err.response.data);
        alert('Error creando usuario: ' + (err.response.data ?? err.response.statusText ?? ''));
      } else {
        alert('Error creando usuario: ' + (err.message ?? String(error)));
      }
    } finally {
      setFormLoading(false);
      console.log('🏁 [UsersManagement] Finalizando creación de usuario');
    }
  };

  // Actualizar usuario
  const updateUser = async () => {
    if (!selectedUser) return;
    
    try {
      setFormLoading(true);
      await authService.actualizarUsuario(selectedUser.id, formData);
      setFormData({ name: '', email: '', password: '', roleId: 0, sendInvitationEmail: true });
      setSelectedUser(null);
      setShowForm(false);
      loadUsers();
    } catch (error) {
      console.error('Error:', error);
      alert('Error actualizando usuario');
    } finally {
      setFormLoading(false);
    }
  };

  // Cambiar estado de usuario
  const toggleUserStatus = async (userId: number) => {
    try {
      await authService.toggleEstadoUsuario(userId);
      loadUsers();
      loadStats();
    } catch (error) {
      console.error('Error:', error);
      alert('Error cambiando estado del usuario');
    }
  };

  // Eliminar usuario
  const deleteUser = async (userId: number) => {
    if (!window.confirm('¿Estás seguro de que quieres eliminar este usuario?')) return;
    
    try {
      await authService.eliminarUsuario(userId);
      loadUsers();
      loadStats();
    } catch (error) {
      console.error('Error:', error);
      alert('Error eliminando usuario');
    }
  };

  // Editar usuario
  const editUser = (user: User) => {
    setSelectedUser(user);
    setFormData({
      name: user.name,
      email: user.email,
      password: '',
      roleId: roles.find(r => r.name === user.roleName)?.id || 0,
      sendInvitationEmail: false
    });
    setShowForm(true);
  };

  // Filtrar usuarios
  const filteredUsers = users.filter(user => {
    const matchesSearch = user.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         user.email.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesRole = !filterRole || user.roleName === filterRole;
    const matchesActive = filterActive === null || user.active === filterActive;
    
    return matchesSearch && matchesRole && matchesActive;
  });

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
      <div style={{ 
        background: 'linear-gradient(135deg, #2196F3 0%, #1976D2 100%)', 
        color: 'white', 
        padding: '20px', 
        borderRadius: '10px', 
        marginBottom: '20px' 
      }}>
        <h1 style={{ margin: '0 0 10px 0', fontSize: '24px' }}>👥 Gestión de Usuarios y Roles</h1>
        <p style={{ margin: '0', opacity: '0.9' }}>
          Administra usuarios, roles y permisos del sistema
        </p>
      </div>

      {/* Estadísticas Compactas */}
      {stats && (
        <div style={{ 
          display: 'grid', 
          gridTemplateColumns: 'repeat(4, 1fr)', 
          gap: '10px', 
          marginBottom: '20px' 
        }}>
          <div style={{ background: '#e3f2fd', padding: '10px', borderRadius: '6px', textAlign: 'center' }}>
            <h4 style={{ margin: '0 0 3px 0', color: '#1976d2', fontSize: '14px' }}>Total Usuarios</h4>
            <p style={{ margin: '0', fontSize: '18px', fontWeight: 'bold', color: '#1976d2' }}>
              {stats.users?.totalUsers || 0}
            </p>
          </div>
          <div style={{ background: '#e8f5e8', padding: '10px', borderRadius: '6px', textAlign: 'center' }}>
            <h4 style={{ margin: '0 0 3px 0', color: '#388e3c', fontSize: '14px' }}>Activos</h4>
            <p style={{ margin: '0', fontSize: '18px', fontWeight: 'bold', color: '#388e3c' }}>
              {stats.users?.activeUsers || 0}
            </p>
          </div>
          <div style={{ background: '#fff3e0', padding: '10px', borderRadius: '6px', textAlign: 'center' }}>
            <h4 style={{ margin: '0 0 3px 0', color: '#f57c00', fontSize: '14px' }}>Pendientes</h4>
            <p style={{ margin: '0', fontSize: '18px', fontWeight: 'bold', color: '#f57c00' }}>
              {stats.users?.unverifiedUsers || 0}
            </p>
          </div>
          <div style={{ background: '#fce4ec', padding: '10px', borderRadius: '6px', textAlign: 'center' }}>
            <h4 style={{ margin: '0 0 3px 0', color: '#c2185b', fontSize: '14px' }}>Suspendidos</h4>
            <p style={{ margin: '0', fontSize: '18px', fontWeight: 'bold', color: '#c2185b' }}>
              {stats.users?.suspendedUsers || 0}
            </p>
          </div>
        </div>
      )}

      {/* Métricas de Uso del Sistema */}
      <div style={{ 
        display: 'grid', 
        gridTemplateColumns: '1fr 1fr', 
        gap: '20px', 
        marginBottom: '20px' 
      }}>
        {/* Tasa de Uso de Plugins */}
        <div style={{ 
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', 
          color: 'white', 
          padding: '20px', 
          borderRadius: '10px' 
        }}>
          <h3 style={{ margin: '0 0 15px 0', fontSize: '18px' }}>🔌 Tasa de Uso de Plugins (Hoy)</h3>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
            <div style={{ textAlign: 'center' }}>
              <p style={{ margin: '0', fontSize: '12px', opacity: '0.8' }}>Usados</p>
              <p style={{ margin: '0', fontSize: '24px', fontWeight: 'bold' }}>85%</p>
            </div>
            <div style={{ textAlign: 'center' }}>
              <p style={{ margin: '0', fontSize: '12px', opacity: '0.8' }}>Límite Diario</p>
              <p style={{ margin: '0', fontSize: '24px', fontWeight: 'bold' }}>100%</p>
            </div>
          </div>
          <div style={{ 
            background: 'rgba(255,255,255,0.2)', 
            borderRadius: '10px', 
            height: '8px', 
            marginTop: '10px' 
          }}>
            <div style={{ 
              background: 'white', 
              height: '100%', 
              width: '85%', 
              borderRadius: '10px' 
            }}></div>
          </div>
        </div>

        {/* Actividad del Sistema */}
        <div style={{ 
          background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', 
          color: 'white', 
          padding: '20px', 
          borderRadius: '10px' 
        }}>
          <h3 style={{ margin: '0 0 15px 0', fontSize: '18px', display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Icon name="BarChart" size={18} /> Actividad del Sistema
          </h3>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
            <div style={{ textAlign: 'center' }}>
              <p style={{ margin: '0', fontSize: '12px', opacity: '0.8' }}>Sesiones Hoy</p>
              <p style={{ margin: '0', fontSize: '24px', fontWeight: 'bold' }}>127</p>
            </div>
            <div style={{ textAlign: 'center' }}>
              <p style={{ margin: '0', fontSize: '12px', opacity: '0.8' }}>Operaciones</p>
              <p style={{ margin: '0', fontSize: '24px', fontWeight: 'bold' }}>1,234</p>
            </div>
          </div>
        </div>
      </div>

      {/* Usuarios y Empresas Más Activas */}
      <div style={{ 
        display: 'grid', 
        gridTemplateColumns: '1fr 1fr', 
        gap: '20px', 
        marginBottom: '20px' 
      }}>
        {/* Usuarios Más Activos */}
        <div style={{ 
          background: 'white', 
          border: '1px solid #e0e0e0', 
          borderRadius: '10px', 
          padding: '20px' 
        }}>
          <h3 style={{ margin: '0 0 15px 0', fontSize: '18px', color: '#333', display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Icon name="Users" size={18} /> Usuarios Más Activos (Últimos 30 días)
          </h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {[
              { nombre: 'Juan Pérez', actividades: 45 },
              { nombre: 'María García', actividades: 38 },
              { nombre: 'Carlos López', actividades: 32 },
              { nombre: 'Ana Martínez', actividades: 28 },
              { nombre: 'Pedro Rodríguez', actividades: 24 }
            ].map((usuario, index) => (
              <div key={index} style={{ 
                display: 'flex', 
                justifyContent: 'space-between', 
                alignItems: 'center',
                padding: '8px',
                background: index % 2 === 0 ? '#f8f9fa' : 'white',
                borderRadius: '4px'
              }}>
                <span style={{ fontSize: '14px', color: '#333' }}>{usuario.nombre}</span>
                <span style={{ 
                  fontSize: '12px', 
                  color: '#666',
                  background: '#e3f2fd',
                  padding: '2px 8px',
                  borderRadius: '12px'
                }}>
                  {usuario.actividades} actividades
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* Empresas Más Activas */}
        <div style={{ 
          background: 'white', 
          border: '1px solid #e0e0e0', 
          borderRadius: '10px', 
          padding: '20px' 
        }}>
          <h3 style={{ margin: '0 0 15px 0', fontSize: '18px', color: '#333', display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Icon name="Building" size={18} /> Empresas Más Activas
          </h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {[
              { nombre: 'AgroTech Solutions', recursos: 'campos, lotes, insumos' },
              { nombre: 'Campo Verde S.A.', recursos: 'campos, lotes, insumos' },
              { nombre: 'Finca del Norte', recursos: 'campos, lotes, insumos' },
              { nombre: 'AgroInnovación', recursos: 'campos, lotes, insumos' },
              { nombre: 'Sembrando Futuro', recursos: 'campos, lotes, insumos' }
            ].map((empresa, index) => (
              <div key={index} style={{ 
                display: 'flex', 
                justifyContent: 'space-between', 
                alignItems: 'center',
                padding: '8px',
                background: index % 2 === 0 ? '#f8f9fa' : 'white',
                borderRadius: '4px'
              }}>
                <span style={{ fontSize: '14px', color: '#333' }}>{empresa.nombre}</span>
                <span style={{ 
                  fontSize: '12px', 
                  color: '#666',
                  background: '#e8f5e8',
                  padding: '2px 8px',
                  borderRadius: '12px'
                }}>
                  {empresa.recursos}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Botón para agregar nuevo usuario */}
      <div style={{ marginBottom: '20px' }}>
        <button
          type="button"
          onClick={() => {
            console.log('📝 [UsersManagement] Abriendo formulario de nuevo usuario');
            setSelectedUser(null);
            setFormData({ name: '', email: '', password: '', roleId: 0, sendInvitationEmail: true });
            setShowForm(true);
          }}
          style={{
            background: '#2196F3',
            color: 'white',
            border: 'none',
            padding: '12px 24px',
            borderRadius: '8px',
            cursor: 'pointer',
            fontSize: '16px',
            fontWeight: 'bold'
          }}
        >
          <Icon name="Plus" size={16} style={{ marginRight: '4px', verticalAlign: 'middle' }} /> Agregar Nuevo Usuario
        </button>
      </div>

      {/* Formulario para nuevo/editar usuario */}
      {showForm && (
        <div style={{ 
          background: '#f9f9f9', 
          padding: '20px', 
          borderRadius: '10px', 
          marginBottom: '20px',
          border: '1px solid #ddd'
        }}>
          <h3 style={{ margin: '0 0 15px 0', color: '#333' }}>
            <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
              <Icon name="FileText" size={16} /> {selectedUser ? 'Editar Usuario' : 'Nuevo Usuario'}
            </span>
          </h3>
          
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Nombre:
              </label>
              <input
                type="text"
                value={formData.name}
                onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
                placeholder="Nombre completo"
              />
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Email:
              </label>
              <input
                type="email"
                value={formData.email}
                onChange={(e) => setFormData(prev => ({ ...prev, email: e.target.value }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
                placeholder="usuario@ejemplo.com"
              />
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Contraseña {selectedUser && '(dejar vacío para mantener)'}:
              </label>
              <input
                type="password"
                value={formData.password}
                onChange={(e) => setFormData(prev => ({ ...prev, password: e.target.value }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
                placeholder="Contraseña"
              />
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Rol:
              </label>
              <select
                value={formData.roleId}
                onChange={(e) => setFormData(prev => ({ ...prev, roleId: parseInt(e.target.value) }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              >
                <option value={0}>Seleccionar rol</option>
                {roles.map(role => (
                  <option key={role.id} value={role.id}>
                    {role.name} - {role.description}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {!selectedUser && (
            <div style={{ marginTop: '15px' }}>
              <label style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <input
                  type="checkbox"
                  checked={formData.sendInvitationEmail}
                  onChange={(e) => setFormData(prev => ({ ...prev, sendInvitationEmail: e.target.checked }))}
                />
                Enviar email de invitación
              </label>
            </div>
          )}

          <div style={{ display: 'flex', gap: '10px', marginTop: '15px' }}>
            <button
              type="button"
              onClick={selectedUser ? updateUser : createUser}
              disabled={formLoading || !formData.name || !formData.email || !formData.roleId || (!selectedUser && !formData.password)}
              style={{
                background: '#2196F3',
                color: 'white',
                border: 'none',
                padding: '10px 20px',
                borderRadius: '5px',
                cursor: formLoading ? 'not-allowed' : 'pointer',
                fontSize: '14px',
                opacity: formLoading ? 0.6 : 1
              }}
            >
              {formLoading ? (
                <>
                  <Icon name="Loader" size={14} style={{ marginRight: '4px', verticalAlign: 'middle' }} /> Guardando...
                </>
              ) : (
                <>
                  <Icon name="Save" size={14} style={{ marginRight: '4px', verticalAlign: 'middle' }} /> {selectedUser ? 'Actualizar Usuario' : 'Crear Usuario'}
                </>
              )}
            </button>
            
            <button
              type="button"
              onClick={() => {
                console.log('❌ [UsersManagement] Cerrando formulario manualmente');
                setShowForm(false);
                setSelectedUser(null);
                setFormData({ name: '', email: '', password: '', roleId: 0, sendInvitationEmail: true });
              }}
              style={{
                background: '#f44336',
                color: 'white',
                border: 'none',
                padding: '10px 20px',
                borderRadius: '5px',
                cursor: 'pointer',
                fontSize: '14px'
              }}
            >
              <Icon name="XCircle" size={14} style={{ marginRight: '4px', verticalAlign: 'middle' }} /> Cancelar
            </button>
          </div>
        </div>
      )}

      {/* Filtros */}
      <div style={{ 
        background: 'white', 
        padding: '15px', 
        borderRadius: '8px', 
        marginBottom: '20px',
        boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
      }}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '15px' }}>
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
              <Icon name="Search" size={14} style={{ marginRight: '4px', verticalAlign: 'middle' }} /> Buscar:
            </label>
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Buscar por nombre o email..."
              style={{
                width: '100%',
                padding: '8px',
                border: '1px solid #ddd',
                borderRadius: '5px',
                fontSize: '14px'
              }}
            />
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
              <Icon name="Tag" size={14} style={{ marginRight: '4px', verticalAlign: 'middle' }} /> Rol:
            </label>
            <select
              value={filterRole}
              onChange={(e) => setFilterRole(e.target.value)}
              style={{
                width: '100%',
                padding: '8px',
                border: '1px solid #ddd',
                borderRadius: '5px',
                fontSize: '14px'
              }}
            >
              <option value="">Todos los roles</option>
              {roles.map(role => (
                <option key={role.id} value={role.name}>{role.name}</option>
              ))}
            </select>
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
              <Icon name="BarChart" size={14} style={{ marginRight: '4px', verticalAlign: 'middle' }} /> Estado:
            </label>
            <select
              value={filterActive === null ? '' : filterActive.toString()}
              onChange={(e) => setFilterActive(e.target.value === '' ? null : e.target.value === 'true')}
              style={{
                width: '100%',
                padding: '8px',
                border: '1px solid #ddd',
                borderRadius: '5px',
                fontSize: '14px'
              }}
            >
              <option value="">Todos</option>
              <option value="true">Activos</option>
              <option value="false">Inactivos</option>
            </select>
          </div>
        </div>
      </div>

      {/* Lista de usuarios */}
      <div style={{ 
        background: 'white', 
        borderRadius: '10px', 
        overflow: 'hidden',
        boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
      }}>
        <div style={{ 
          background: '#f5f5f5', 
          padding: '15px', 
          borderBottom: '1px solid #ddd',
          fontWeight: 'bold'
        }}>
          <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Icon name="Clipboard" size={16} /> Usuarios ({filteredUsers.length})
          </span>
        </div>
        
        {loading ? (
          <div style={{ padding: '20px', textAlign: 'center', color: '#666' }}>
            <Icon name="RefreshCcw" size={16} style={{ marginRight: '4px', verticalAlign: 'middle' }} /> Cargando usuarios...
          </div>
        ) : filteredUsers.length === 0 ? (
          <div style={{ padding: '20px', textAlign: 'center', color: '#666' }}>
            No se encontraron usuarios
          </div>
        ) : (
          <div style={{ maxHeight: '600px', overflowY: 'auto' }}>
            {filteredUsers.map((user) => (
              <div key={user.id} style={{ 
                padding: '15px', 
                borderBottom: '1px solid #eee',
                display: 'grid',
                gridTemplateColumns: '1fr auto auto',
                gap: '15px',
                alignItems: 'center'
              }}>
                <div>
                  <h4 style={{ margin: '0 0 5px 0', color: '#333' }}>{user.name}</h4>
                  <p style={{ margin: '0 0 5px 0', color: '#666', fontSize: '14px' }}>
                    <Icon name="Mail" size={14} style={{ marginRight: '4px', verticalAlign: 'middle' }} /> {user.email}
                  </p>
                  <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                    <span style={{ 
                      background: user.active ? '#e8f5e8' : '#ffebee',
                      color: user.active ? '#388e3c' : '#d32f2f',
                      padding: '2px 8px',
                      borderRadius: '12px',
                      fontSize: '12px',
                      fontWeight: 'bold'
                    }}>
                      <span style={{ display: 'flex', alignItems: 'center', gap: '2px' }}>
                        {user.active ? (
                          <>
                            <Icon name="CheckCircle" size={12} /> Activo
                          </>
                        ) : (
                          <>
                            <Icon name="XCircle" size={12} /> Inactivo
                          </>
                        )}
                      </span>
                    </span>
                    <span style={{ 
                      background: '#e3f2fd',
                      color: '#1976d2',
                      padding: '2px 8px',
                      borderRadius: '12px',
                      fontSize: '12px'
                    }}>
                      <Icon name="Tag" size={12} style={{ marginRight: '2px', verticalAlign: 'middle' }} /> {user.roleName}
                    </span>
                    {user.emailVerified && (
                      <span style={{ 
                        background: '#e8f5e8',
                        color: '#388e3c',
                        padding: '2px 8px',
                        borderRadius: '12px',
                        fontSize: '12px'
                      }}>
                        ✓ Verificado
                      </span>
                    )}
                  </div>
                  <p style={{ margin: '5px 0 0 0', color: '#999', fontSize: '12px' }}>
                    Creado: {new Date(user.createdAt).toLocaleDateString()}
                    {user.lastLogin && ` | Último login: ${new Date(user.lastLogin).toLocaleDateString()}`}
                  </p>
                </div>

                <div style={{ display: 'flex', gap: '5px' }}>
                  <button
                    onClick={() => editUser(user)}
                    style={{
                      background: '#2196F3',
                      color: 'white',
                      border: 'none',
                      padding: '6px 12px',
                      borderRadius: '4px',
                      cursor: 'pointer',
                      fontSize: '12px'
                    }}
                  >
                    <Icon name="Pencil" size={12} style={{ marginRight: '2px', verticalAlign: 'middle' }} /> Editar
                  </button>
                  <button
                    onClick={() => toggleUserStatus(user.id)}
                    style={{
                      background: user.active ? '#ff9800' : '#4caf50',
                      color: 'white',
                      border: 'none',
                      padding: '6px 12px',
                      borderRadius: '4px',
                      cursor: 'pointer',
                      fontSize: '12px'
                    }}
                  >
                    {user.active ? (
                      <>
                        <Icon name="PauseCircle" size={12} style={{ marginRight: '2px', verticalAlign: 'middle' }} /> Desactivar
                      </>
                    ) : (
                      <>
                        <Icon name="PlayCircle" size={12} style={{ marginRight: '2px', verticalAlign: 'middle' }} /> Activar
                      </>
                    )}
                  </button>
                  <button
                    onClick={() => deleteUser(user.id)}
                    style={{
                      background: '#f44336',
                      color: 'white',
                      border: 'none',
                      padding: '6px 12px',
                      borderRadius: '4px',
                      cursor: 'pointer',
                      fontSize: '12px'
                    }}
                  >
                    <Icon name="Trash2" size={12} style={{ marginRight: '2px', verticalAlign: 'middle' }} /> Eliminar
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default UsersManagement;
