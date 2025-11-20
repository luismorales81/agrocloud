import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import { authService, showNotification } from '../services/api';
import './Login.css';

const ResetPassword: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [token, setToken] = useState('');

  useEffect(() => {
    // Obtener todos los parámetros de la URL para debug
    const allParams = Object.fromEntries(searchParams.entries());
    console.log('🔍 [ResetPassword] Parámetros de URL:', allParams);
    console.log('🔍 [ResetPassword] URL completa:', window.location.href);
    
    const tokenParam = searchParams.get('token');
    console.log('🔍 [ResetPassword] Token obtenido:', tokenParam);
    
    if (!tokenParam) {
      console.error('❌ [ResetPassword] No se encontró token en la URL');
      showNotification('Token de recuperación no válido. Por favor, solicita un nuevo enlace de recuperación.', 'error');
      // Redirigir después de un pequeño delay para que el usuario vea el mensaje
      setTimeout(() => {
        navigate('/forgot-password');
      }, 2000);
      return;
    }
    
    console.log('✅ [ResetPassword] Token válido encontrado');
    setToken(tokenParam);
  }, [searchParams, navigate]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!newPassword || !confirmPassword) {
      showNotification('Por favor complete todos los campos', 'error');
      return;
    }

    if (newPassword !== confirmPassword) {
      showNotification('Las contraseñas no coinciden', 'error');
      return;
    }

    if (newPassword.length < 6) {
      showNotification('La contraseña debe tener al menos 6 caracteres', 'error');
      return;
    }

    setLoading(true);
    try {
      await authService.resetPassword(token, newPassword);
      setSuccess(true);
      showNotification('Contraseña actualizada correctamente', 'success');
    } catch (error: any) {
      showNotification(error.response?.data?.error || 'Error al actualizar la contraseña', 'error');
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className="min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 login-background">
        {/* Overlay oscuro para mejorar legibilidad */}
        <div className="absolute inset-0 login-overlay"></div>
        
        {/* Contenido */}
        <div className="relative z-10 max-w-md w-full space-y-8">
          <div>
            <h2 className="mt-6 text-center text-4xl font-extrabold text-white login-title">
              Contraseña Actualizada
            </h2>
            <p className="mt-2 text-center text-lg text-white login-subtitle">
              Tu contraseña ha sido actualizada exitosamente
            </p>
          </div>
          
          <div className="login-card p-8 text-center">
            <div className="mb-4">
              <svg className="mx-auto h-12 w-12 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <p className="text-gray-700 mb-6">
              Ya puedes iniciar sesión con tu nueva contraseña.
            </p>
            <Button
              variant="primary"
              size="lg"
              className="w-full"
              onClick={() => navigate('/login')}
            >
              Ir al Login
            </Button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8 login-background">
      {/* Overlay oscuro para mejorar legibilidad */}
      <div className="absolute inset-0 login-overlay"></div>
      
      {/* Contenido */}
      <div className="relative z-10 max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-4xl font-extrabold text-white login-title">
            Nueva Contraseña
          </h2>
          <p className="mt-2 text-center text-lg text-white login-subtitle">
            Ingresa tu nueva contraseña
          </p>
        </div>
        
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div className="login-card p-8">
            <Input
              label="Nueva Contraseña"
              type="password"
              placeholder="Ingrese su nueva contraseña"
              value={newPassword}
              onChange={setNewPassword}
              required
            />
            
            <Input
              label="Confirmar Contraseña"
              type="password"
              placeholder="Confirme su nueva contraseña"
              value={confirmPassword}
              onChange={setConfirmPassword}
              required
            />
            
            <Button
              type="submit"
              variant="primary"
              size="lg"
              disabled={loading}
              className="w-full"
            >
              {loading ? 'Actualizando...' : 'Actualizar Contraseña'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ResetPassword;
