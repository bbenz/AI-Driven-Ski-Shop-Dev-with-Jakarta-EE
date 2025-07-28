import React from 'react';
import { useFormContext, FieldPath, FieldValues } from 'react-hook-form';
import { Input, InputProps } from '@/components/ui/Input';
import { cn } from '@/utils/helpers';

export interface FormFieldProps<T extends FieldValues>
  extends Omit<InputProps, 'name' | 'value' | 'onChange' | 'onBlur'> {
  name: FieldPath<T>;
  label?: string;
  description?: string;
  required?: boolean;
}

export function FormField<T extends FieldValues>({
  name,
  label,
  description,
  required = false,
  className,
  ...inputProps
}: FormFieldProps<T>) {
  const {
    register,
    formState: { errors },
  } = useFormContext<T>();

  const error = errors[name];
  const errorMessage = error?.message as string | undefined;

  return (
    <div className={cn('space-y-2', className)}>
      {label && (
        <label
          htmlFor={name}
          className="block text-sm font-medium text-gray-700"
        >
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
      )}
      
      {description && (
        <p className="text-sm text-gray-500">{description}</p>
      )}
      
      <Input
        id={name}
        error={errorMessage}
        {...register(name, { required: required ? `${label || name}は必須です` : false })}
        {...inputProps}
      />
    </div>
  );
}
