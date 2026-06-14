import React, { useState, useEffect, useRef } from 'react';
import { Icon } from '../../core/components/Icon';

export interface AutocompleteOption<T = any> {
  value: string | number;
  label: string;
  data?: T;
}

interface AutocompleteProps<T = any> {
  options: AutocompleteOption<T>[];
  value?: string | number;
  onChange: (value: string | number | undefined, option?: AutocompleteOption<T>) => void;
  placeholder?: string;
  label?: string;
  required?: boolean;
  disabled?: boolean;
  error?: string;
  emptyMessage?: string;
  maxHeight?: number;
  style?: React.CSSProperties;
  /** Estilos adicionales para el input (ej. minHeight, padding, fontSize) */
  inputStyle?: React.CSSProperties;
}

export const Autocomplete = <T,>({
  options,
  value,
  onChange,
  placeholder = 'Buscar...',
  label,
  required = false,
  disabled = false,
  error,
  emptyMessage = 'No se encontraron resultados',
  maxHeight = 200,
  style,
  inputStyle,
}: AutocompleteProps<T>) => {
  const [isOpen, setIsOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [focusedIndex, setFocusedIndex] = useState(-1);
  const containerRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const listRef = useRef<HTMLUListElement>(null);

  // Obtener la opción seleccionada actual
  const selectedOption = options.find(opt => opt.value === value);

  // Filtrar opciones basado en el término de búsqueda
  const filteredOptions = options.filter(option =>
    option.label.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Manejar clicks fuera del componente
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false);
        setSearchTerm('');
        setFocusedIndex(-1);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Scroll a la opción enfocada cuando se navega con el teclado
  useEffect(() => {
    if (focusedIndex >= 0 && listRef.current) {
      const focusedElement = listRef.current.children[focusedIndex] as HTMLElement;
      if (focusedElement) {
        focusedElement.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
      }
    }
  }, [focusedIndex]);

  // Manejar selección de opción
  const handleSelect = (option: AutocompleteOption<T>) => {
    onChange(option.value, option);
    setIsOpen(false);
    setSearchTerm('');
    setFocusedIndex(-1);
    inputRef.current?.blur();
  };

  // Manejar input focus
  const handleFocus = () => {
    if (!disabled) {
      setIsOpen(true);
      setSearchTerm('');
    }
  };

  // Manejar cambios en el input
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const term = e.target.value;
    setSearchTerm(term);
    setIsOpen(true);
    setFocusedIndex(-1);
  };

  // Manejar navegación con teclado
  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (disabled) return;

    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        if (!isOpen) {
          setIsOpen(true);
        } else {
          setFocusedIndex(prev => 
            prev < filteredOptions.length - 1 ? prev + 1 : prev
          );
        }
        break;
      case 'ArrowUp':
        e.preventDefault();
        if (isOpen && focusedIndex > 0) {
          setFocusedIndex(prev => prev - 1);
        }
        break;
      case 'Enter':
        e.preventDefault();
        if (isOpen && focusedIndex >= 0 && filteredOptions[focusedIndex]) {
          handleSelect(filteredOptions[focusedIndex]);
        }
        break;
      case 'Escape':
        setIsOpen(false);
        setSearchTerm('');
        setFocusedIndex(-1);
        inputRef.current?.blur();
        break;
    }
  };

  // Limpiar selección
  const handleClear = (e: React.MouseEvent) => {
    e.stopPropagation();
    onChange(undefined, undefined);
    setSearchTerm('');
    setIsOpen(false);
    inputRef.current?.focus();
  };

  const displayValue = isOpen ? searchTerm : (selectedOption?.label || '');

  return (
    <div ref={containerRef} style={{ position: 'relative', width: '100%', ...style }}>
      {label && (
        <label style={{ 
          display: 'block', 
          marginBottom: '0.5rem', 
          fontSize: '0.875rem', 
          fontWeight: '500', 
          color: '#1f2937' 
        }}>
          {label}
          {required && <span style={{ color: '#ef4444', marginLeft: '0.25rem' }}>*</span>}
        </label>
      )}
      
      <div style={{ position: 'relative' }}>
        <input
          ref={inputRef}
          type="text"
          value={displayValue}
          onChange={handleInputChange}
          onFocus={handleFocus}
          onKeyDown={handleKeyDown}
          placeholder={selectedOption ? undefined : placeholder}
          disabled={disabled}
          required={required}
          style={{
            width: '100%',
            padding: '0.75rem',
            paddingRight: selectedOption ? '2.5rem' : '2.5rem',
            border: error ? '1px solid #ef4444' : '1px solid #d1d5db',
            borderRadius: '0.375rem',
            fontSize: '0.875rem',
            backgroundColor: disabled ? '#f9fafb' : 'white',
            cursor: disabled ? 'not-allowed' : 'text',
            outline: isOpen ? '2px solid #3b82f6' : 'none',
            outlineOffset: '0',
            ...inputStyle,
          }}
        />
        
        <div style={{
          position: 'absolute',
          right: '0.75rem',
          top: '50%',
          transform: 'translateY(-50%)',
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem',
        }}>
          {selectedOption && !disabled && (
            <button
              type="button"
              onClick={handleClear}
              style={{
                background: 'none',
                border: 'none',
                cursor: 'pointer',
                padding: '0.25rem',
                display: 'flex',
                alignItems: 'center',
                color: '#6b7280',
              }}
              tabIndex={-1}
            >
              <Icon name="X" size={16} />
            </button>
          )}
          <Icon 
            name={isOpen ? "ChevronUp" : "ChevronDown"} 
            size={18} 
            style={{ color: '#6b7280', pointerEvents: 'none' }}
          />
        </div>
      </div>

      {error && (
        <p style={{ 
          marginTop: '0.5rem', 
          fontSize: '0.875rem', 
          color: '#ef4444' 
        }}>
          {error}
        </p>
      )}

      {isOpen && !disabled && (
        <ul
          ref={listRef}
          style={{
            position: 'absolute',
            top: '100%',
            left: 0,
            right: 0,
            marginTop: '0.25rem',
            backgroundColor: 'white',
            border: '1px solid #d1d5db',
            borderRadius: '0.375rem',
            boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
            maxHeight: `${maxHeight}px`,
            overflowY: 'auto',
            zIndex: 1000,
            listStyle: 'none',
            padding: 0,
            margin: 0,
          }}
        >
          {filteredOptions.length === 0 ? (
            <li
              style={{
                padding: '0.75rem',
                color: '#6b7280',
                fontSize: '0.875rem',
                textAlign: 'center',
              }}
            >
              {emptyMessage}
            </li>
          ) : (
            filteredOptions.map((option, index) => (
              <li
                key={option.value}
                onClick={() => handleSelect(option)}
                onMouseEnter={() => setFocusedIndex(index)}
                style={{
                  padding: '0.75rem',
                  cursor: 'pointer',
                  backgroundColor: index === focusedIndex 
                    ? '#f3f4f6' 
                    : option.value === value 
                    ? '#eff6ff' 
                    : 'white',
                  borderBottom: index < filteredOptions.length - 1 
                    ? '1px solid #f3f4f6' 
                    : 'none',
                  transition: 'background-color 0.15s',
                  color: '#1f2937',
                  fontSize: '0.875rem',
                }}
              >
                {option.label}
              </li>
            ))
          )}
        </ul>
      )}
    </div>
  );
};

export default Autocomplete;

