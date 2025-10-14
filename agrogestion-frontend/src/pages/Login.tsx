import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useEmpresa } from '../contexts/EmpresaContext';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import EmpresaSelector from '../components/EmpresaSelector';
import TestUsersSection from '../components/TestUsersSection';
import { type TestUser } from '../data/testUsers';
import './Login.css';

const Login: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [mostrarSelectorEmpresa, setMostrarSelectorEmpresa] = useState(false);
  const [rememberMe, setRememberMe] = useState(true); // Por defecto activado
  const { login } = useAuth();
  const { empresasUsuario, cargarEmpresasUsuario } = useEmpresa();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(''); // Limpiar errores previos
    
    if (!username || !password) {
      setError('Por favor completa todos los campos');
      return;
    }

    setLoading(true);
    
    try {
      const success = await login(username, password, rememberMe);
      
      if (success) {
        // Cargar empresas del usuario
        const empresasCargadas = await cargarEmpresasUsuario();
        
        // Usar las empresas cargadas directamente
        if (empresasCargadas.length > 1) {
          setMostrarSelectorEmpresa(true);
        } else if (empresasCargadas.length === 1) {
          // Si solo tiene una empresa, navegar directamente
          navigate('/dashboard');
        } else {
          // Si no tiene empresas, mostrar mensaje de error
          setError('No tienes acceso a ninguna empresa. Contacta al administrador.');
        }
      } else {
        setError('❌ Email o contraseña incorrectos. Por favor, verifica tus credenciales.');
      }
    } catch (err) {
      setError('❌ Error al conectar con el servidor. Por favor, intenta nuevamente.');
    } finally {
      setLoading(false);
    }
  };

  const handleEmpresaSeleccionada = (empresaId: number) => {
    setMostrarSelectorEmpresa(false);
    navigate('/dashboard');
  };

  const handleTestUserSelect = (user: TestUser) => {
    setUsername(user.email);
    setPassword(user.password);
    setError(''); // Limpiar error al seleccionar usuario de prueba
  };

  return (
    <div className="min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 login-background">
      {/* Overlay oscuro para mejorar legibilidad */}
      <div className="absolute inset-0 login-overlay"></div>
      
      {/* Contenido del login */}
      <div className="relative z-10 max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-4xl font-extrabold text-white login-title">
            AgroCloud
          </h2>
          <p className="mt-2 text-center text-lg text-white login-subtitle">
            Sistema de Gestión Agropecuaria
          </p>
          <p className="mt-1 text-center text-sm text-gray-200 login-subtitle">
            Tecnología moderna para el campo argentino
          </p>
        </div>
        
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div className="login-card rounded-xl p-8">
            {/* Mensaje de error visible */}
            {error && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg">
                <p className="text-sm text-red-700 font-medium">{error}</p>
              </div>
            )}
            
            <Input
              label="Email"
              type="email"
              placeholder="Ingrese su email"
              value={username}
              onChange={(value) => {
                setUsername(value);
                setError(''); // Limpiar error al escribir
              }}
              data-testid="email-input"
              required
            />
            
            <Input
              label="Contraseña"
              type="password"
              placeholder="Ingrese su contraseña"
              value={password}
              onChange={(value) => {
                setPassword(value);
                setError(''); // Limpiar error al escribir
              }}
              data-testid="password-input"
              required
            />
            
            {/* Checkbox para mantener sesión iniciada */}
            <div className="flex items-center space-x-2 mb-4">
              <input
                type="checkbox"
                id="rememberMe"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
                className="w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 rounded focus:ring-blue-500 focus:ring-2"
              />
              <label htmlFor="rememberMe" className="text-sm text-gray-700 cursor-pointer">
                Mantener sesión iniciada
              </label>
            </div>
            
            <div className="text-right">
              <Link to="/forgot-password" className="text-sm text-blue-600 hover:text-blue-500 transition-colors login-link">
                ¿Olvidaste tu contraseña?
              </Link>
            </div>
            
            <Button
              type="submit"
              variant="primary"
              size="lg"
              disabled={loading}
              className="w-full mt-6"
              data-testid="login-button"
            >
              {loading ? 'Iniciando sesión...' : 'Iniciar Sesión'}
            </Button>
            
            {/* Ayuda rápida con credenciales */}
            <div className="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
              <p className="text-xs text-blue-800 font-medium mb-2">💡 Credenciales de prueba:</p>
              <div className="grid grid-cols-2 gap-2 text-xs">
                <div className="bg-white p-2 rounded border border-blue-100">
                  <p className="font-semibold text-gray-700">Email:</p>
                  <p className="text-blue-600">tecnico.juan@agrocloud.com</p>
                </div>
                <div className="bg-white p-2 rounded border border-blue-100">
                  <p className="font-semibold text-gray-700">Contraseña:</p>
                  <p className="text-blue-600 font-mono">admin123</p>
                </div>
              </div>
              <p className="text-xs text-gray-600 mt-2 text-center">
                Haz clic en "Mostrar usuarios" abajo para ver todos los usuarios de prueba
              </p>
            </div>
          </div>
        </form>
        
        <div className="text-center text-sm text-white login-users-info rounded-lg p-4">
          <TestUsersSection onUserSelect={handleTestUserSelect} />
        </div>
      </div>

      {/* Modal de Selección de Empresa */}
      {mostrarSelectorEmpresa && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">
                🏢 Seleccionar Empresa
              </h3>
              <p className="text-sm text-gray-600 mb-4">
                Tienes acceso a múltiples empresas. Selecciona con cuál deseas trabajar:
              </p>
              
              <div className="space-y-3">
                {empresasUsuario.map((usuarioEmpresa) => (
                  <button
                    key={usuarioEmpresa.id}
                    onClick={() => handleEmpresaSeleccionada(usuarioEmpresa.id)}
                    className="w-full p-4 text-left border border-gray-200 rounded-lg hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <div className="flex items-center space-x-3">
                      <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                        <span className="text-blue-600 text-sm font-medium">
                          {usuarioEmpresa.empresaNombre?.charAt(0)?.toUpperCase() || 'E'}
                        </span>
                      </div>
                      <div className="flex-1">
                        <h4 className="text-sm font-medium text-gray-900">
                          {usuarioEmpresa.empresaNombre || 'Empresa'}
                        </h4>
                        <p className="text-xs text-gray-500">
                          Rol: {usuarioEmpresa.rol} • Estado: {usuarioEmpresa.estado}
                        </p>
                      </div>
                    </div>
                  </button>
                ))}
              </div>
              
              <div className="mt-4 pt-4 border-t border-gray-200">
                <button
                  onClick={() => {
                    setMostrarSelectorEmpresa(false);
                    // Logout si cancela
                    localStorage.removeItem('token');
                    localStorage.removeItem('user');
                    window.location.reload();
                  }}
                  className="w-full bg-gray-300 text-gray-700 px-4 py-2 rounded-md hover:bg-gray-400"
                >
                  Cancelar
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Login;
