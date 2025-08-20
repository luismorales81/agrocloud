import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';

const Login: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!username || !password) {
      return;
    }

    setLoading(true);
    const success = await login(username, password);
    
    if (success) {
      navigate('/dashboard');
    }
    
    setLoading(false);
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            AgroGestion
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            Sistema de Gestión Agrícola
          </p>
        </div>
        
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div className="card p-8">
            <Input
              label="Usuario"
              type="text"
              placeholder="Ingrese su usuario"
              value={username}
              onChange={setUsername}
              required
            />
            
            <Input
              label="Contraseña"
              type="password"
              placeholder="Ingrese su contraseña"
              value={password}
              onChange={setPassword}
              required
            />
            
            <Button
              type="submit"
              variant="primary"
              size="lg"
              disabled={loading}
              className="w-full"
            >
              {loading ? 'Iniciando sesión...' : 'Iniciar Sesión'}
            </Button>
          </div>
        </form>
        
        <div className="text-center text-sm text-gray-600">
          <p>Usuarios de prueba:</p>
          <p>admin / admin123</p>
          <p>tecnico / tecnico123</p>
          <p>productor / productor123</p>
        </div>
      </div>
    </div>
  );
};

export default Login;
