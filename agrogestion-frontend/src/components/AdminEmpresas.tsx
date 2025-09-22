import React, { useState, useEffect } from 'react';
import api from '../services/api';
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
      const responseUsuarios = await api.get('/admin/usuarios/basic');
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
      const response = await api.get('/roles-empresa');
      setRolesEmpresa(response.data);
    } catch (error) {
      console.error('Error cargando roles de empresa:', error);
      // Mantener roles por defecto si falla la carga
    }
  };

  const cargarUsuariosEmpresas = async () => {
    try {
      // Cargar todas las relaciones usuario-empresa
      const response = await api.get('/empresa-usuario/todas-relaciones');
      if (response.data.success) {
        setUsuariosEmpresas(response.data.data);
      }
    } catch (error) {
      console.error('Error cargando usuarios de empresas:', error);
      // Si no existe el endpoint, intentar con la empresa por defecto
      try {
        const response = await api.get('/empresa-usuario/empresa/1/usuarios');
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
        telefono: nuevaEmpresaForm.telefono,
        direccion: nuevaEmpresaForm.direccion,
        descripcion: nuevaEmpresaForm.descripcion,
        activo: true
      };

      const response = await api.post('/empresas', empresaData);
      
      if (response.data.success) {
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
        alert('Error: ' + response.data.message);
      }
    } catch (error) {
      console.error('Error creando empresa:', error);
      alert('Error al crear la empresa');
    }
  };

  const asignarUsuarioAEmpresa = async () => {
    try {
      const response = await api.post('/empresa-usuario/asignar', asignacionForm);
      
      if (response.data.success) {
        alert('Usuario asignado a empresa correctamente');
        setShowAsignarForm(false);
        setAsignacionForm({ usuarioId: 0, empresaId: 0, rol: 'OPERARIO' });
        await cargarUsuariosEmpresas();
      } else {
        alert('Error: ' + response.data.message);
      }
    } catch (error) {
      console.error('Error asignando usuario:', error);
      alert('Error al asignar usuario a empresa');
    }
  };

  const cambiarRolUsuario = async () => {
    try {
      const response = await api.put('/empresa-usuario/cambiar-rol', {
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
        <button
          onClick={() => setShowCrearEmpresaForm(true)}
          className="bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500"
        >
          🏢 Crear Nueva Empresa
        </button>
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

      {/* Modal para asignar usuario */}
      {showAsignarForm && (
        <div className="admin-modal">
          <div className="admin-modal-content">
            <div className="admin-modal-header">
              <h3 className="admin-modal-title">➕ Asignar Usuario a Empresa</h3>
              <button
                onClick={() => setShowAsignarForm(false)}
                className="admin-modal-close"
              >
                ✕
              </button>
            </div>
            
            <div className="admin-field-group">
              <label className="admin-label">Usuario</label>
              <select
                value={asignacionForm.usuarioId}
                onChange={(e) => setAsignacionForm({...asignacionForm, usuarioId: parseInt(e.target.value)})}
                className="admin-select"
              >
                <option value={0}>Seleccionar usuario</option>
                {usuarios.map(usuario => (
                  <option key={usuario.id} value={usuario.id}>
                    {usuario.firstName} {usuario.lastName} ({usuario.email})
                  </option>
                ))}
              </select>
              <div className="admin-help-text">Selecciona el usuario a asignar</div>
            </div>

            <div className="admin-field-group">
              <label className="admin-label">Empresa</label>
              <select
                value={asignacionForm.empresaId}
                onChange={(e) => setAsignacionForm({...asignacionForm, empresaId: parseInt(e.target.value)})}
                className="admin-select"
              >
                <option value={0}>Seleccionar empresa</option>
                {empresas.map(empresa => (
                  <option key={empresa.id} value={empresa.id}>
                    {empresa.nombre}
                  </option>
                ))}
              </select>
              <div className="admin-help-text">Selecciona la empresa destino</div>
            </div>

            <div className="admin-field-group">
              <label className="admin-label">Rol en la Empresa</label>
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
              <div className="admin-help-text">Define el rol y permisos del usuario</div>
            </div>

            <div className="admin-btn-group">
              <button
                onClick={() => setShowAsignarForm(false)}
                className="admin-btn admin-btn-cancel"
              >
                Cancelar
              </button>
              <button
                onClick={asignarUsuarioAEmpresa}
                className="admin-btn admin-btn-primary"
              >
                Asignar Usuario
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
