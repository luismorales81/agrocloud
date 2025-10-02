import React from 'react';

interface InputProps {
  label?: string;
  type?: string;
  placeholder?: string;
  value: string;
  onChange: (value: string) => void;
  error?: string;
  required?: boolean;
  disabled?: boolean;
  className?: string;
  'data-testid'?: string;
}

const Input: React.FC<InputProps> = ({
  label,
  type = 'text',
  placeholder,
  value,
  onChange,
  error,
  required = false,
  disabled = false,
  className = '',
  'data-testid': dataTestId,
}) => {
  return (
    <div className={`mb-4 ${className}`}>
      {label && (
        <label className="block text-sm font-medium text-gray-700 mb-2">
          {label}
          {required && <span className="text-danger-500 ml-1">*</span>}
        </label>
      )}
      <input
        type={type}
        placeholder={placeholder}
        value={value}
        onChange={(e) => {
          if (e && e.target) {
            onChange(e.target.value);
          }
        }}
        disabled={disabled}
        className={`input ${error ? 'input-error' : ''}`}
        data-testid={dataTestId}
      />
      {error && (
        <p className="mt-1 text-sm text-danger-500">{error}</p>
      )}
    </div>
  );
};

export default Input;
