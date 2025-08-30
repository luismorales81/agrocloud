import React, { useState, useEffect } from 'react';

interface PWAInstallPromptProps {
  onInstall?: () => void;
  onDismiss?: () => void;
}

const PWAInstallPrompt: React.FC<PWAInstallPromptProps> = ({ onInstall, onDismiss }) => {
  console.log('🔧 [PWAInstallPrompt] Renderizando componente...');
  
  const [showPrompt, setShowPrompt] = useState(false);
  const [deferredPrompt, setDeferredPrompt] = useState<any>(null);

  console.log('🔧 [PWAInstallPrompt] Estado inicial - ShowPrompt:', showPrompt);

  useEffect(() => {
    console.log('🔧 [PWAInstallPrompt] Configurando event listeners...');
    
    // Detectar si ya se mostró el banner
    const bannerDismissed = localStorage.getItem('installBannerDismissed');
    if (bannerDismissed) {
      console.log('ℹ️ [PWAInstallPrompt] Banner ya fue descartado anteriormente');
      return;
    }

    // Detectar evento de instalación
    const handleBeforeInstallPrompt = (e: Event) => {
      console.log('🔧 [PWAInstallPrompt] Evento beforeinstallprompt detectado');
      e.preventDefault();
      setDeferredPrompt(e);
      
      // Mostrar después de un delay
      setTimeout(() => {
        console.log('🔧 [PWAInstallPrompt] Mostrando prompt de instalación');
        setShowPrompt(true);
      }, 3000);
    };

    // Detectar si ya está instalada
    const handleAppInstalled = () => {
      console.log('✅ [PWAInstallPrompt] Aplicación instalada');
      setShowPrompt(false);
      localStorage.setItem('installBannerDismissed', 'true');
    };

    window.addEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
    window.addEventListener('appinstalled', handleAppInstalled);

    console.log('🔧 [PWAInstallPrompt] Event listeners configurados');

    return () => {
      console.log('🔧 [PWAInstallPrompt] Limpiando event listeners');
      window.removeEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
      window.removeEventListener('appinstalled', handleAppInstalled);
    };
  }, []);

  const handleInstall = async () => {
    console.log('🔧 [PWAInstallPrompt] Usuario solicitó instalación');
    if (deferredPrompt) {
      try {
        deferredPrompt.prompt();
        const { outcome } = await deferredPrompt.userChoice;
        if (outcome === 'accepted') {
          console.log('✅ [PWAInstallPrompt] PWA instalada exitosamente');
          onInstall?.();
        } else {
          console.log('ℹ️ [PWAInstallPrompt] Usuario rechazó la instalación');
        }
        setDeferredPrompt(null);
      } catch (error) {
        console.error('❌ [PWAInstallPrompt] Error durante instalación:', error);
      }
    }
    handleDismiss();
  };

  const handleDismiss = () => {
    console.log('🔧 [PWAInstallPrompt] Usuario descartó el prompt');
    setShowPrompt(false);
    localStorage.setItem('installBannerDismissed', 'true');
    onDismiss?.();
  };

  console.log('🔧 [PWAInstallPrompt] Renderizando UI - ShowPrompt:', showPrompt);

  if (!showPrompt) {
    console.log('🔧 [PWAInstallPrompt] No mostrar prompt');
    return null;
  }

  return (
    <div
      style={{
        position: 'fixed',
        bottom: '20px',
        left: '20px',
        right: '20px',
        background: 'linear-gradient(135deg, #4CAF50 0%, #45a049 100%)',
        color: 'white',
        padding: '16px',
        borderRadius: '12px',
        boxShadow: '0 8px 25px rgba(0,0,0,0.15)',
        zIndex: 10000,
        animation: 'slideUp 0.3s ease',
        fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif'
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div style={{ flex: 1 }}>
          <div style={{ fontWeight: '600', marginBottom: '4px', fontSize: '16px' }}>
            📱 Instalar AgroCloud
          </div>
          <div style={{ fontSize: '14px', opacity: 0.9, lineHeight: '1.4' }}>
            Accede más rápido desde tu pantalla de inicio y usa offline
          </div>
        </div>
        
        <div style={{ display: 'flex', gap: '8px', marginLeft: '16px' }}>
          <button
            onClick={handleInstall}
            style={{
              background: 'white',
              color: '#4CAF50',
              border: 'none',
              padding: '8px 16px',
              borderRadius: '6px',
              fontWeight: '500',
              cursor: 'pointer',
              fontSize: '14px',
              transition: 'transform 0.2s ease'
            }}
            onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.05)'}
            onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
          >
            Instalar
          </button>
          
          <button
            onClick={handleDismiss}
            style={{
              background: 'transparent',
              color: 'white',
              border: '1px solid white',
              padding: '8px 16px',
              borderRadius: '6px',
              cursor: 'pointer',
              fontSize: '14px',
              transition: 'background-color 0.2s ease'
            }}
            onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.1)'}
            onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
          >
            Ahora no
          </button>
        </div>
      </div>

      <style>{`
        @keyframes slideUp {
          from { 
            transform: translateY(100%); 
            opacity: 0; 
          }
          to { 
            transform: translateY(0); 
            opacity: 1; 
          }
        }
      `}</style>
    </div>
  );
};

export default PWAInstallPrompt;
