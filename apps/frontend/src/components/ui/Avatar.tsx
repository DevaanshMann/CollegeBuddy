import type { AvatarProps } from '../../types';
import { clsx } from 'clsx';

export function Avatar({
  src,
  alt,
  size = 'md',
  className = '',
  fallback,
}: AvatarProps) {
  const sizeClasses = {
    xs: 'w-6 h-6 text-xs',
    sm: 'w-8 h-8 text-sm',
    md: 'w-10 h-10 text-base',
    lg: 'w-14 h-14 text-lg',
    xl: 'w-20 h-20 text-2xl',
  };

  const getInitials = (name: string) => {
    return name
      .split(' ')
      .map(n => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  };

  const initials = fallback ? getInitials(fallback) : alt.slice(0, 2).toUpperCase();

  return (
    <div
      className={clsx(
        'avatar flex items-center justify-center font-semibold text-gray-600 dark:text-gray-400',
        sizeClasses[size],
        className
      )}
    >
      {src ? (
        <img src={src} alt={alt} className="w-full h-full rounded-full object-cover" />
      ) : (
        <span>{initials}</span>
      )}
    </div>
  );
}
