import React from 'react';
import { useFormContext, FieldPath, FieldValues } from 'react-hook-form';
import { cn } from '@/utils/helpers';

export interface CheckboxFieldProps<T extends FieldValues>
  extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'name' | 'type'> {
  name: FieldPath<T>;
  label?: string;
  description?: string;
  required?: boolean;
}

export function CheckboxField<T extends FieldValues>({
  name,
  label,
  description,
  required = false,
  className,
  ...inputProps
}: CheckboxFieldProps<T>) {
  const {
    register,
    formState: { errors },
  } = useFormContext<T>();

  const error = errors[name];
  const errorMessage = error?.message as string | undefined;

  return (
    <div className={cn('space-y-2', className)}>
      <div className="flex items-start">
        <div className="flex items-center h-5">
          <input
            id={name}
            type="checkbox"
            className={cn(
              'h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded',
              errorMessage && 'border-red-300 focus:ring-red-500',
              className
            )}
            {...register(name, { required: required ? `${label || name}は必須です` : false })}
            {...inputProps}
          />
        </div>
        <div className="ml-3 text-sm">
          {label && (
            <label
              htmlFor={name}
              className="font-medium text-gray-700"
            >
              {label}
              {required && <span className="text-red-500 ml-1">*</span>}
            </label>
          )}
          {description && (
            <p className="text-gray-500">{description}</p>
          )}
        </div>
      </div>
      
      {errorMessage && (
        <p className="text-sm text-red-600 ml-7">{errorMessage}</p>
      )}
    </div>
  );
}
