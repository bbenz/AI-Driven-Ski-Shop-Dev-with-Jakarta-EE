import React from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { cn } from '@/utils/helpers';

const spinnerVariants = cva(
  'animate-spin rounded-full border-2 border-current border-t-transparent',
  {
    variants: {
      size: {
        sm: 'h-4 w-4',
        md: 'h-6 w-6',
        lg: 'h-8 w-8',
        xl: 'h-12 w-12',
      },
      variant: {
        default: 'text-gray-500',
        primary: 'text-blue-600',
        white: 'text-white',
      },
    },
    defaultVariants: {
      size: 'md',
      variant: 'default',
    },
  }
);

export interface SpinnerProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof spinnerVariants> {}

export const Spinner: React.FC<SpinnerProps> = ({
  className,
  size,
  variant,
  ...props
}) => {
  return (
    <div
      className={cn(spinnerVariants({ size, variant }), className)}
      {...props}
    >
      <span className="sr-only">読み込み中...</span>
    </div>
  );
};

// ドットローディングコンポーネント
export interface DotsLoadingProps extends React.HTMLAttributes<HTMLDivElement> {
  size?: 'sm' | 'md' | 'lg';
  variant?: 'default' | 'primary' | 'white';
}

export const DotsLoading: React.FC<DotsLoadingProps> = ({
  className,
  size = 'md',
  variant = 'default',
  ...props
}) => {
  const sizeClasses = {
    sm: 'h-1 w-1',
    md: 'h-2 w-2',
    lg: 'h-3 w-3',
  };

  const variantClasses = {
    default: 'bg-gray-500',
    primary: 'bg-blue-600',
    white: 'bg-white',
  };

  return (
    <div
      className={cn('flex space-x-1', className)}
      {...props}
    >
      {[0, 1, 2].map((i) => (
        <div
          key={i}
          className={cn(
            'rounded-full animate-pulse',
            sizeClasses[size],
            variantClasses[variant]
          )}
        />
      ))}
      <span className="sr-only">読み込み中...</span>
    </div>
  );
};

// パルスローディングコンポーネント
export interface PulseLoadingProps extends React.HTMLAttributes<HTMLDivElement> {
  className?: string;
  lines?: number;
  height?: 'sm' | 'md' | 'lg';
}

export const PulseLoading: React.FC<PulseLoadingProps> = ({
  className,
  lines = 3,
  height = 'md',
  ...props
}) => {
  const heightClasses = {
    sm: 'h-3',
    md: 'h-4',
    lg: 'h-6',
  };

  return (
    <div className={cn('space-y-3', className)} {...props}>
      {Array.from({ length: lines }, (_, i) => (
        <div
          key={i}
          className={cn(
            'animate-pulse bg-gray-200 rounded',
            heightClasses[height],
            i === lines - 1 && lines > 1 ? 'w-3/4' : 'w-full'
          )}
        />
      ))}
    </div>
  );
};

// フルページローディングコンポーネント
export interface FullPageLoadingProps {
  text?: string;
  overlay?: boolean;
  variant?: 'spinner' | 'dots';
}

export const FullPageLoading: React.FC<FullPageLoadingProps> = ({
  text = '読み込み中...',
  overlay = true,
  variant = 'spinner',
}) => {
  const LoadingComponent = variant === 'spinner' ? Spinner : DotsLoading;

  return (
    <div
      className={cn(
        'fixed inset-0 z-50 flex items-center justify-center',
        overlay && 'bg-white bg-opacity-80 backdrop-blur-sm'
      )}
    >
      <div className="flex flex-col items-center space-y-4">
        <LoadingComponent size="lg" variant="primary" />
        {text && (
          <p className="text-gray-600 font-medium">{text}</p>
        )}
      </div>
    </div>
  );
};

// セクションローディングコンポーネント
export interface SectionLoadingProps extends React.HTMLAttributes<HTMLDivElement> {
  text?: string;
  variant?: 'spinner' | 'dots' | 'pulse';
  minHeight?: string;
}

export const SectionLoading: React.FC<SectionLoadingProps> = ({
  className,
  text = '読み込み中...',
  variant = 'spinner',
  ...props
}) => {
  const LoadingComponent = 
    variant === 'spinner' ? Spinner : 
    variant === 'dots' ? DotsLoading : 
    PulseLoading;

  return (
    <div
      className={cn(
        'flex items-center justify-center p-8',
        className
      )}
      {...props}
    >
      {variant === 'pulse' ? (
        <div className="w-full max-w-md">
          <PulseLoading lines={4} />
        </div>
      ) : (
        <div className="flex flex-col items-center space-y-4">
          <LoadingComponent size="lg" variant="primary" />
          {text && (
            <p className="text-gray-600">{text}</p>
          )}
        </div>
      )}
    </div>
  );
};

// ボタン内ローディングコンポーネント
export interface ButtonLoadingProps {
  isLoading: boolean;
  children: React.ReactNode;
  loadingText?: string;
}

export const ButtonLoading: React.FC<ButtonLoadingProps> = ({
  isLoading,
  children,
  loadingText,
}) => {
  if (!isLoading) {
    return <>{children}</>;
  }

  return (
    <div className="flex items-center space-x-2">
      <Spinner size="sm" variant="white" />
      <span>{loadingText || '処理中...'}</span>
    </div>
  );
};
