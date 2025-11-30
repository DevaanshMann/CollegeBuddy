import type { TextAreaProps } from '../../types';
import { clsx } from 'clsx';

export function TextArea({
  label,
  placeholder,
  value,
  onChange,
  error,
  disabled = false,
  required = false,
  rows = 4,
  maxLength,
  showCount = false,
  className = '',
}: TextAreaProps) {
  return (
    <div className={clsx('w-full', className)}>
      {label && (
        <label className="block text-sm font-medium mb-1.5 text-light-text-primary dark:text-dark-text-primary">
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
      )}
      <textarea
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        disabled={disabled}
        required={required}
        rows={rows}
        maxLength={maxLength}
        className={clsx(
          'input resize-none',
          error && 'border-red-500 focus:ring-red-500 focus:border-red-500'
        )}
      />
      <div className="flex justify-between items-center mt-1">
        {error ? (
          <p className="text-sm text-red-500">{error}</p>
        ) : (
          <span></span>
        )}
        {showCount && maxLength && (
          <p className="text-sm text-gray-500">
            {value.length}/{maxLength}
          </p>
        )}
      </div>
    </div>
  );
}
