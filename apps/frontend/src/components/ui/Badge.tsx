import type { BadgeProps } from '../../types';
import { clsx } from 'clsx';

export function Badge({
  count,
  max = 99,
  showZero = false,
  className = '',
}: BadgeProps) {
  if (count === 0 && !showZero) return null;

  const displayCount = count > max ? `${max}+` : count;

  return (
    <span className={clsx('badge', className)}>
      {displayCount}
    </span>
  );
}
