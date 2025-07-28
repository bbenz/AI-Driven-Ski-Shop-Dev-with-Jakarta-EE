import React from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { cn } from '@/utils/helpers';

const badgeVariants = cva(
  'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2',
  {
    variants: {
      variant: {
        default: 'bg-gray-100 text-gray-800 hover:bg-gray-200',
        primary: 'bg-blue-100 text-blue-800 hover:bg-blue-200',
        secondary: 'bg-purple-100 text-purple-800 hover:bg-purple-200',
        success: 'bg-green-100 text-green-800 hover:bg-green-200',
        warning: 'bg-yellow-100 text-yellow-800 hover:bg-yellow-200',
        error: 'bg-red-100 text-red-800 hover:bg-red-200',
        outline: 'border border-gray-300 text-gray-700 hover:bg-gray-50',
      },
      size: {
        sm: 'px-2 py-0.5 text-xs',
        md: 'px-2.5 py-0.5 text-xs',
        lg: 'px-3 py-1 text-sm',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'md',
    },
  }
);

export interface BadgeProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof badgeVariants> {
  children: React.ReactNode;
  icon?: React.ReactNode;
  onRemove?: () => void;
}

export const Badge: React.FC<BadgeProps> = ({
  className,
  variant,
  size,
  children,
  icon,
  onRemove,
  ...props
}) => {
  return (
    <div
      className={cn(badgeVariants({ variant, size }), className)}
      {...props}
    >
      {icon && <span className="mr-1">{icon}</span>}
      <span>{children}</span>
      {onRemove && (
        <button
          type="button"
          className="ml-1 inline-flex h-4 w-4 items-center justify-center rounded-full hover:bg-black/10 focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2"
          onClick={onRemove}
        >
          <svg
            className="h-3 w-3"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
          <span className="sr-only">削除</span>
        </button>
      )}
    </div>
  );
};

// ステータス専用のBadgeコンポーネント
export interface StatusBadgeProps extends Omit<BadgeProps, 'variant'> {
  status: 'active' | 'inactive' | 'pending' | 'completed' | 'cancelled' | 'draft';
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({
  status,
  children,
  ...props
}) => {
  const statusVariants = {
    active: 'success' as const,
    inactive: 'default' as const,
    pending: 'warning' as const,
    completed: 'success' as const,
    cancelled: 'error' as const,
    draft: 'secondary' as const,
  };

  const statusLabels = {
    active: 'アクティブ',
    inactive: '非アクティブ',
    pending: '保留中',
    completed: '完了',
    cancelled: 'キャンセル',
    draft: '下書き',
  };

  return (
    <Badge variant={statusVariants[status]} {...props}>
      {children || statusLabels[status]}
    </Badge>
  );
};

// 数値表示用のBadgeコンポーネント
export interface CountBadgeProps extends Omit<BadgeProps, 'children'> {
  count: number;
  max?: number;
  showZero?: boolean;
}

export const CountBadge: React.FC<CountBadgeProps> = ({
  count,
  max,
  showZero = false,
  ...props
}) => {
  if (count === 0 && !showZero) {
    return null;
  }

  const displayCount = max && count > max ? `${max}+` : count.toString();

  return (
    <Badge variant="primary" size="sm" {...props}>
      {displayCount}
    </Badge>
  );
};
