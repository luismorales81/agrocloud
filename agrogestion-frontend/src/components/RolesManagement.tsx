import React, { useState, useEffect } from 'react';

interface Role {
  id: number;
  name: string;
  description: string;
  permissions: string[];
  userCount: number;
}

interface CreateRoleRequest {
  name: string;
  description: string;
  permissions: string[];
}

const RolesManagement: React.FC = () => {
  const [roles, setRoles] = useState<Role[]>([]);
  const [availablePermissions, setAvailablePermissions] = useState<string[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [selectedRole, setSelectedRole] = useState<Role | null>(null);
  const [formData, setFormData] = useState<CreateRoleRequest>({
    name: '',
    description: '',
    permissions: []
  });
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [stats, setStats] = useState<any>(null);

  // Cargar datos al montar el componente
  useEffect(() => {
    loadRoles();
    loadAvailablePermissions();
    loadStats();
  }, []);

  // Cargar roles
  const loadRoles = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8080/api/roles', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        setRoles(data);
      } else {
        console.error('Error cargando roles');
      }
    } catch (error) {
      console.error('Error:', error);
    } finally {
      setLoading(false);
    }
  };

  // Cargar permisos disponibles
  const loadAvailablePermissions = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8080/api/roles/permissions/available', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        setAvailablePermissions(data);
      }
    } catch (error) {
      console.error('Error cargando permisos:', error);
    }
  };

  // Cargar estadísticas
  const loadStats = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8080/api/roles/stats', {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        setStats(data);
      }
    } catch (error) {
      console.error('Error cargando estadísticas:', error);
    }
  };

  // Crear rol
  const createRole = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8080/api/roles', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData)
      });
      
      if (response.ok) {
        setFormData({ name: '', description: '', permissions: [] });
        setShowForm(false);
        loadRoles();
        loadStats();
      } else {
        const error = await response.text();
        alert('Error creando rol: ' + error);
      }
    } catch (error) {
      console.error('Error:', error);
      alert('Error creando rol');
    } finally {
      setLoading(false);
    }
  };

  // Actualizar rol
  const updateRole = async () => {
    if (!selectedRole) return;
    
    try {
      setLoading(true);
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:8080/api/roles/${selectedRole.id}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData)
      });
      
      if (response.ok) {
        setFormData({ name: '', description: '', permissions: [] });
        setSelectedRole(null);
        setShowForm(false);
        loadRoles();
        loadStats();
      } else {
        const error = await response.text();
        alert('Error actualizando rol: ' + error);
      }
    } catch (error) {
      console.error('Error:', error);
      alert('Error actualizando rol');
    } finally {
      setLoading(false);
    }
  };

  // Eliminar rol
  const deleteRole = async (roleId: number) => {
    if (!window.confirm('¿Estás seguro de que quieres eliminar este rol?')) return;
    
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:8080/api/roles/${roleId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        loadRoles();
        loadStats();
      } else {
        alert('Error eliminando rol');
      }
    } catch (error) {
      console.error('Error:', error);
      alert('Error eliminando rol');
    }
  };

  // Editar rol
  const editRole = (role: Role) => {
    setSelectedRole(role);
    setFormData({
      name: role.name,
      description: role.description,
      permissions: [...role.permissions]
    });
    setShowForm(true);
  };

  // Agregar permiso
  const addPermission = async (roleId: number, permission: string) => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:8080/api/roles/${roleId}/permissions`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ permission })
      });
      
      if (response.ok) {
        loadRoles();
      } else {
        alert('Error agregando permiso');
      }
    } catch (error) {
      console.error('Error:', error);
      alert('Error agregando permiso');
    }
  };

  // Remover permiso
  const removePermission = async (roleId: number, permission: string) => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:8080/api/roles/${roleId}/permissions`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ permission })
      });
      
      if (response.ok) {
        loadRoles();
      } else {
        alert('Error removiendo permiso');
      }
    } catch (error) {
      console.error('Error:', error);
      alert('Error removiendo permiso');
    }
  };

  // Manejar cambio de permisos en el formulario
  const handlePermissionChange = (permission: string, checked: boolean) => {
    if (checked) {
      setFormData(prev => ({
        ...prev,
        permissions: [...prev.permissions, permission]
      }));
    } else {
      setFormData(prev => ({
        ...prev,
        permissions: prev.permissions.filter(p => p !== permission)
      }));
    }
  };

  // Filtrar roles
  const filteredRoles = roles.filter(role =>
    role.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    role.description.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Agrupar permisos por categoría
  const groupedPermissions = availablePermissions.reduce((groups, permission) => {
    const category = permission.split('_')[0];
    if (!groups[category]) {
      groups[category] = [];
    }
    groups[category].push(permission);
    return groups;
  }, {} as Record<string, string[]>);

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
      <div style={{ 
        background: 'linear-gradient(135deg, #9C27B0 0%, #7B1FA2 100%)', 
        color: 'white', 
        padding: '20px', 
        borderRadius: '10px', 
        marginBottom: '20px' 
      }}>
        <h1 style={{ margin: '0 0 10px 0', fontSize: '24px' }}>🏷️ Gestión de Roles y Permisos</h1>
        <p style={{ margin: '0', opacity: '0.9' }}>
          Administra roles y sus permisos en el sistema
        </p>
      </div>

      {/* Estadísticas */}
      {stats && (
        <div style={{ 
          display: 'grid', 
          gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', 
          gap: '15px', 
          marginBottom: '20px' 
        }}>
          <div style={{ background: '#f3e5f5', padding: '15px', borderRadius: '8px', textAlign: 'center' }}>
            <h3 style={{ margin: '0 0 5px 0', color: '#7b1fa2' }}>Total Roles</h3>
            <p style={{ margin: '0', fontSize: '24px', fontWeight: 'bold', color: '#7b1fa2' }}>
              {stats.totalRoles || 0}
            </p>
          </div>
          <div style={{ background: '#e8f5e8', padding: '15px', borderRadius: '8px', textAlign: 'center' }}>
            <h3 style={{ margin: '0 0 5px 0', color: '#388e3c' }}>Total Permisos</h3>
            <p style={{ margin: '0', fontSize: '24px', fontWeight: 'bold', color: '#388e3c' }}>
              {availablePermissions.length}
            </p>
          </div>
        </div>
      )}

      {/* Botón para agregar nuevo rol */}
      <div style={{ marginBottom: '20px' }}>
        <button
          onClick={() => {
            setSelectedRole(null);
            setFormData({ name: '', description: '', permissions: [] });
            setShowForm(true);
          }}
          style={{
            background: '#9C27B0',
            color: 'white',
            border: 'none',
            padding: '12px 24px',
            borderRadius: '8px',
            cursor: 'pointer',
            fontSize: '16px',
            fontWeight: 'bold'
          }}
        >
          ➕ Agregar Nuevo Rol
        </button>
      </div>

      {/* Formulario para nuevo/editar rol */}
      {showForm && (
        <div style={{ 
          background: '#f9f9f9', 
          padding: '20px', 
          borderRadius: '10px', 
          marginBottom: '20px',
          border: '1px solid #ddd'
        }}>
          <h3 style={{ margin: '0 0 15px 0', color: '#333' }}>
            {selectedRole ? '📝 Editar Rol' : '📝 Nuevo Rol'}
          </h3>
          
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px', marginBottom: '15px' }}>
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Nombre del Rol:
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
                placeholder="Ej: SUPERVISOR"
              />
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                Descripción:
              </label>
              <input
                type="text"
                value={formData.description}
                onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
                placeholder="Descripción del rol"
              />
            </div>
          </div>

          {/* Permisos */}
          <div>
            <label style={{ display: 'block', marginBottom: '10px', fontWeight: 'bold' }}>
              Permisos:
            </label>
            <div style={{ 
              maxHeight: '300px', 
              overflowY: 'auto', 
              border: '1px solid #ddd', 
              borderRadius: '5px', 
              padding: '15px',
              background: 'white'
            }}>
              {Object.entries(groupedPermissions).map(([category, permissions]) => (
                <div key={category} style={{ marginBottom: '20px' }}>
                  <h4 style={{ 
                    margin: '0 0 10px 0', 
                    color: '#333', 
                    borderBottom: '1px solid #eee', 
                    paddingBottom: '5px' 
                  }}>
                    {category.toUpperCase()}
                  </h4>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '10px' }}>
                    {permissions.map(permission => (
                      <label key={permission} style={{ 
                        display: 'flex', 
                        alignItems: 'center', 
                        gap: '8px',
                        fontSize: '14px',
                        cursor: 'pointer'
                      }}>
                        <input
                          type="checkbox"
                          checked={formData.permissions.includes(permission)}
                          onChange={(e) => handlePermissionChange(permission, e.target.checked)}
                        />
                        {permission.replace(/_/g, ' ')}
                      </label>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div style={{ display: 'flex', gap: '10px', marginTop: '15px' }}>
            <button
              onClick={selectedRole ? updateRole : createRole}
              disabled={loading || !formData.name || !formData.description}
              style={{
                background: '#9C27B0',
                color: 'white',
                border: 'none',
                padding: '10px 20px',
                borderRadius: '5px',
                cursor: loading ? 'not-allowed' : 'pointer',
                fontSize: '14px',
                opacity: loading ? 0.6 : 1
              }}
            >
              {loading ? '💾 Guardando...' : (selectedRole ? '💾 Actualizar Rol' : '💾 Crear Rol')}
            </button>
            
            <button
              onClick={() => {
                setShowForm(false);
                setSelectedRole(null);
                setFormData({ name: '', description: '', permissions: [] });
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
              ❌ Cancelar
            </button>
          </div>
        </div>
      )}

      {/* Búsqueda */}
      <div style={{ marginBottom: '20px' }}>
        <input
          type="text"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          placeholder="🔍 Buscar roles..."
          style={{
            width: '100%',
            padding: '12px',
            border: '1px solid #ddd',
            borderRadius: '8px',
            fontSize: '16px'
          }}
        />
      </div>

      {/* Lista de roles */}
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
          📋 Roles ({filteredRoles.length})
        </div>
        
        {loading ? (
          <div style={{ padding: '20px', textAlign: 'center', color: '#666' }}>
            🔄 Cargando roles...
          </div>
        ) : filteredRoles.length === 0 ? (
          <div style={{ padding: '20px', textAlign: 'center', color: '#666' }}>
            No se encontraron roles
          </div>
        ) : (
          <div style={{ maxHeight: '600px', overflowY: 'auto' }}>
            {filteredRoles.map((role) => (
              <div key={role.id} style={{ 
                padding: '20px', 
                borderBottom: '1px solid #eee'
              }}>
                <div style={{ 
                  display: 'flex', 
                  justifyContent: 'space-between', 
                  alignItems: 'flex-start',
                  marginBottom: '15px'
                }}>
                  <div>
                    <h3 style={{ margin: '0 0 5px 0', color: '#333' }}>{role.name}</h3>
                    <p style={{ margin: '0 0 10px 0', color: '#666', fontSize: '14px' }}>
                      {role.description}
                    </p>
                    <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                      <span style={{ 
                        background: '#e3f2fd',
                        color: '#1976d2',
                        padding: '4px 8px',
                        borderRadius: '12px',
                        fontSize: '12px',
                        fontWeight: 'bold'
                      }}>
                        👥 {role.userCount} usuarios
                      </span>
                      <span style={{ 
                        background: '#e8f5e8',
                        color: '#388e3c',
                        padding: '4px 8px',
                        borderRadius: '12px',
                        fontSize: '12px',
                        fontWeight: 'bold'
                      }}>
                        🔑 {role.permissions.length} permisos
                      </span>
                    </div>
                  </div>

                  <div style={{ display: 'flex', gap: '5px' }}>
                    <button
                      onClick={() => editRole(role)}
                      style={{
                        background: '#9C27B0',
                        color: 'white',
                        border: 'none',
                        padding: '8px 16px',
                        borderRadius: '4px',
                        cursor: 'pointer',
                        fontSize: '12px'
                      }}
                    >
                      ✏️ Editar
                    </button>
                    <button
                      onClick={() => deleteRole(role.id)}
                      disabled={role.userCount > 0}
                      style={{
                        background: role.userCount > 0 ? '#ccc' : '#f44336',
                        color: 'white',
                        border: 'none',
                        padding: '8px 16px',
                        borderRadius: '4px',
                        cursor: role.userCount > 0 ? 'not-allowed' : 'pointer',
                        fontSize: '12px'
                      }}
                      title={role.userCount > 0 ? 'No se puede eliminar un rol con usuarios asignados' : 'Eliminar rol'}
                    >
                      🗑️ Eliminar
                    </button>
                  </div>
                </div>

                {/* Lista de permisos */}
                <div>
                  <h4 style={{ margin: '0 0 10px 0', color: '#333', fontSize: '14px' }}>
                    Permisos asignados:
                  </h4>
                  <div style={{ 
                    display: 'flex', 
                    flexWrap: 'wrap', 
                    gap: '5px',
                    marginBottom: '10px'
                  }}>
                    {role.permissions.map(permission => (
                      <span key={permission} style={{ 
                        background: '#f3e5f5',
                        color: '#7b1fa2',
                        padding: '2px 8px',
                        borderRadius: '12px',
                        fontSize: '11px',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '5px'
                      }}>
                        {permission.replace(/_/g, ' ')}
                        <button
                          onClick={() => removePermission(role.id, permission)}
                          style={{
                            background: 'none',
                            border: 'none',
                            color: '#7b1fa2',
                            cursor: 'pointer',
                            fontSize: '10px',
                            padding: '0',
                            margin: '0'
                          }}
                        >
                          ✕
                        </button>
                      </span>
                    ))}
                  </div>

                  {/* Agregar nuevo permiso */}
                  <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                    <select
                      onChange={(e) => {
                        if (e.target.value) {
                          addPermission(role.id, e.target.value);
                          e.target.value = '';
                        }
                      }}
                      style={{
                        padding: '5px',
                        border: '1px solid #ddd',
                        borderRadius: '4px',
                        fontSize: '12px'
                      }}
                    >
                      <option value="">Agregar permiso...</option>
                      {availablePermissions
                        .filter(p => !role.permissions.includes(p))
                        .map(permission => (
                          <option key={permission} value={permission}>
                            {permission.replace(/_/g, ' ')}
                          </option>
                        ))}
                    </select>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default RolesManagement;
