import React from 'react';

interface CardProps {
  children: React.ReactNode;
  className?: string;
  style?: React.CSSProperties;
}

interface CardHeaderProps {
  children: React.ReactNode;
  className?: string;
  style?: React.CSSProperties;
}

interface CardTitleProps {
  children: React.ReactNode;
  className?: string;
  style?: React.CSSProperties;
}

interface CardContentProps {
  children: React.ReactNode;
  className?: string;
  style?: React.CSSProperties;
}

export const Card: React.FC<CardProps> = ({ children, className = '', style = {} }) => {
  return (
    <div 
      className={className}
      style={{
        backgroundColor: 'white',
        borderRadius: '0.5rem',
        border: '1px solid #e5e7eb',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        ...style
      }}
    >
      {children}
    </div>
  );
};

export const CardHeader: React.FC<CardHeaderProps> = ({ children, className = '', style = {} }) => {
  return (
    <div 
      className={className}
      style={{
        padding: '1.5rem',
        borderBottom: '1px solid #e5e7eb',
        ...style
      }}
    >
      {children}
    </div>
  );
};

export const CardTitle: React.FC<CardTitleProps> = ({ children, className = '', style = {} }) => {
  return (
    <h3 
      className={className}
      style={{
        fontSize: '1.125rem',
        fontWeight: '600',
        color: '#1f2937',
        margin: 0,
        ...style
      }}
    >
      {children}
    </h3>
  );
};

export const CardContent: React.FC<CardContentProps> = ({ children, className = '', style = {} }) => {
  return (
    <div 
      className={className}
      style={{
        padding: '1.5rem',
        ...style
      }}
    >
      {children}
    </div>
  );
};
