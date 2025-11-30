import type { InputProps } from '../../types';
import { clsx } from 'clsx';

export function Input({
  type = 'text',
  label,
  placeholder,
  value,
  onChange,
  error,
  disabled = false,
  required = false,
  className = '',
}: InputProps) {
  return (
    <div className={clsx('w-full', className)}>
      {label && (
        <label className="block text-sm font-medium mb-1.5 text-light-text-primary dark:text-dark-text-primary">
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
      )}
      <input
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        disabled={disabled}
        required={required}
        className={clsx(
          'input',
          error && 'border-red-500 focus:ring-red-500 focus:border-red-500'
        )}
      />
      {error && (
        <p className="mt-1 text-sm text-red-500">{error}</p>
      )}
    </div>
  );
}
