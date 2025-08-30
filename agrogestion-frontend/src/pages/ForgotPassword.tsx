import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import { authService, showNotification } from '../services/api';

const ForgotPassword: React.FC = () => {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!email) {
      showNotification('Por favor ingrese su email', 'error');
      return;
    }

    setLoading(true);
    try {
      await authService.requestPasswordReset(email);
      setSuccess(true);
      showNotification('Se ha enviado un email con las instrucciones', 'success');
    } catch (error: any) {
      showNotification(error.response?.data?.error || 'Error al enviar el email', 'error');
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-md w-full space-y-8">
          <div>
            <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
              Email Enviado
            </h2>
            <p className="mt-2 text-center text-sm text-gray-600">
              Revisa tu bandeja de entrada
            </p>
          </div>
          
          <div className="card p-8 text-center">
            <div className="mb-4">
              <svg className="mx-auto h-12 w-12 text-green-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
              </svg>
            </div>
            <p className="text-gray-600 mb-6">
              Hemos enviado un enlace de recuperación a tu email. 
              Haz clic en el enlace para restablecer tu contraseña.
            </p>
            <Link to="/login">
              <Button variant="primary" size="lg" className="w-full">
                Volver al Login
              </Button>
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            Recuperar Contraseña
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            Ingresa tu email para recibir instrucciones
          </p>
        </div>
        
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div className="card p-8">
            <Input
              label="Email"
              type="email"
              placeholder="Ingrese su email"
              value={email}
              onChange={setEmail}
              required
            />
            
            <Button
              type="submit"
              variant="primary"
              size="lg"
              disabled={loading}
              className="w-full"
            >
              {loading ? 'Enviando...' : 'Enviar Instrucciones'}
            </Button>
          </div>
        </form>
        
        <div className="text-center">
          <Link to="/login" className="text-sm text-blue-600 hover:text-blue-500">
            ¿Recordaste tu contraseña? Inicia sesión
          </Link>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;
