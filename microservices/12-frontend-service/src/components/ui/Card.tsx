import React from 'react';
import Image from 'next/image';
import { cva, type VariantProps } from 'class-variance-authority';
import { cn } from '@/utils/helpers';

const cardVariants = cva(
  'rounded-lg border border-gray-200 bg-white text-gray-900 shadow-sm transition-shadow',
  {
    variants: {
      variant: {
        default: 'border-gray-200',
        elevated: 'border-gray-200 shadow-md',
        outline: 'border-2 border-gray-300',
        ghost: 'border-transparent shadow-none',
      },
      padding: {
        none: 'p-0',
        sm: 'p-4',
        md: 'p-6',
        lg: 'p-8',
      },
      hover: {
        none: '',
        lift: 'hover:shadow-md',
        scale: 'hover:scale-[1.02] transition-transform',
        border: 'hover:border-gray-300',
      },
    },
    defaultVariants: {
      variant: 'default',
      padding: 'md',
      hover: 'none',
    },
  }
);

export interface CardProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof cardVariants> {
  children: React.ReactNode;
  asChild?: boolean;
}

export const Card: React.FC<CardProps> = ({
  className,
  variant,
  padding,
  hover,
  children,
  asChild,
  ...props
}) => {
  const Comp = asChild ? React.Fragment : 'div';
  
  if (asChild) {
    return <>{children}</>;
  }

  return (
    <Comp
      className={cn(cardVariants({ variant, padding, hover }), className)}
      {...props}
    >
      {children}
    </Comp>
  );
};

// カードヘッダーコンポーネント
export interface CardHeaderProps extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
  border?: boolean;
}

export const CardHeader: React.FC<CardHeaderProps> = ({
  className,
  children,
  border = false,
  ...props
}) => {
  return (
    <div
      className={cn(
        'flex flex-col space-y-1.5 p-6',
        border && 'border-b border-gray-200',
        className
      )}
      {...props}
    >
      {children}
    </div>
  );
};

// カードタイトルコンポーネント
export interface CardTitleProps extends React.HTMLAttributes<HTMLHeadingElement> {
  children: React.ReactNode;
  as?: 'h1' | 'h2' | 'h3' | 'h4' | 'h5' | 'h6';
}

export const CardTitle: React.FC<CardTitleProps> = ({
  className,
  children,
  as: Comp = 'h3',
  ...props
}) => {
  return (
    <Comp
      className={cn(
        'text-lg font-semibold leading-none tracking-tight',
        className
      )}
      {...props}
    >
      {children}
    </Comp>
  );
};

// カード説明コンポーネント
export interface CardDescriptionProps extends React.HTMLAttributes<HTMLParagraphElement> {
  children: React.ReactNode;
}

export const CardDescription: React.FC<CardDescriptionProps> = ({
  className,
  children,
  ...props
}) => {
  return (
    <p
      className={cn('text-sm text-gray-600', className)}
      {...props}
    >
      {children}
    </p>
  );
};

// カードコンテンツコンポーネント
export interface CardContentProps extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
}

export const CardContent: React.FC<CardContentProps> = ({
  className,
  children,
  ...props
}) => {
  return (
    <div className={cn('p-6 pt-0', className)} {...props}>
      {children}
    </div>
  );
};

// カードフッターコンポーネント
export interface CardFooterProps extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
  border?: boolean;
}

export const CardFooter: React.FC<CardFooterProps> = ({
  className,
  children,
  border = false,
  ...props
}) => {
  return (
    <div
      className={cn(
        'flex items-center p-6 pt-0',
        border && 'border-t border-gray-200 pt-6',
        className
      )}
      {...props}
    >
      {children}
    </div>
  );
};

// 画像付きカードコンポーネント
export interface ImageCardProps extends Omit<CardProps, 'children'> {
  image: {
    src: string;
    alt: string;
    width?: number;
    height?: number;
    className?: string;
  };
  children: React.ReactNode;
  imagePosition?: 'top' | 'left' | 'right';
}

export const ImageCard: React.FC<ImageCardProps> = ({
  image,
  children,
  imagePosition = 'top',
  className,
  ...props
}) => {
  const isHorizontal = imagePosition === 'left' || imagePosition === 'right';

  return (
    <Card
      className={cn(
        'overflow-hidden',
        isHorizontal && 'flex',
        imagePosition === 'right' && 'flex-row-reverse',
        className
      )}
      padding="none"
      {...props}
    >
      <div
        className={cn(
          isHorizontal ? 'w-1/3 flex-shrink-0' : 'w-full',
          'relative'
        )}
      >
        <Image
          src={image.src}
          alt={image.alt}
          width={image.width || 400}
          height={image.height || 300}
          className={cn(
            'object-cover',
            isHorizontal ? 'h-full w-full' : 'h-48 w-full',
            image.className
          )}
        />
      </div>
      <div className={cn(isHorizontal ? 'flex-1' : 'w-full')}>
        {children}
      </div>
    </Card>
  );
};
