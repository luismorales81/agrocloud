import React from 'react';

interface SelectProps {
  children: React.ReactNode;
  value?: string;
  onValueChange?: (value: string) => void;
  placeholder?: string;
  disabled?: boolean;
}

interface SelectTriggerProps {
  children: React.ReactNode;
  onClick?: () => void;
  disabled?: boolean;
}

interface SelectContentProps {
  children: React.ReactNode;
  isOpen?: boolean;
}

interface SelectItemProps {
  children: React.ReactNode;
  value: string;
  onClick?: () => void;
}

interface SelectValueProps {
  children?: React.ReactNode;
  placeholder?: string;
}

// Componente Select simplificado que usa select nativo
export const Select: React.FC<SelectProps> = ({ 
  children, 
  value, 
  onValueChange, 
  placeholder,
  disabled = false 
}) => {
  // Extraer opciones de los SelectItem
  const options: { value: string; label: string }[] = [];
  
  React.Children.forEach(children, (child) => {
    if (React.isValidElement(child) && child.type === SelectContent) {
      React.Children.forEach(child.props.children, (item: any) => {
        if (React.isValidElement(item) && item.type === SelectItem) {
          options.push({
            value: item.props.value,
            label: item.props.children
          });
        }
      });
    }
  });

  return (
    <select
      value={value || ''}
      onChange={(e) => onValueChange && onValueChange(e.target.value)}
      disabled={disabled}
      className="flex h-10 w-full items-center justify-between rounded-md border border-gray-300 bg-white px-3 py-2 text-sm ring-offset-white placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-gray-950 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
    >
      <option value="">{placeholder || 'Seleccionar...'}</option>
      {options.map((option) => (
        <option key={option.value} value={option.value}>
          {option.label}
        </option>
      ))}
    </select>
  );
};

// Componentes de compatibilidad (no se usan en la versión simplificada)
export const SelectTrigger: React.FC<SelectTriggerProps> = ({ children }) => {
  return <>{children}</>;
};

export const SelectContent: React.FC<SelectContentProps> = ({ children }) => {
  return <>{children}</>;
};

export const SelectItem: React.FC<SelectItemProps> = ({ children }) => {
  return <>{children}</>;
};

export const SelectValue: React.FC<SelectValueProps> = ({ children, placeholder }) => {
  return <span>{children || placeholder}</span>;
};
