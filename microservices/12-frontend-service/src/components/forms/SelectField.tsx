import React from 'react';
import { useFormContext, FieldPath, FieldValues } from 'react-hook-form';
import { cn } from '@/utils/helpers';

export interface SelectOption {
  value: string;
  label: string;
  disabled?: boolean;
}

export interface SelectFieldProps<T extends FieldValues>
  extends Omit<React.SelectHTMLAttributes<HTMLSelectElement>, 'name'> {
  name: FieldPath<T>;
  label?: string;
  description?: string;
  options: SelectOption[];
  placeholder?: string;
  required?: boolean;
}

export function SelectField<T extends FieldValues>({
  name,
  label,
  description,
  options,
  placeholder,
  required = false,
  className,
  ...selectProps
}: SelectFieldProps<T>) {
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
      
      <select
        id={name}
        className={cn(
          'block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm',
          errorMessage && 'border-red-300 focus:ring-red-500 focus:border-red-500',
          className
        )}
        {...register(name, { required: required ? `${label || name}は必須です` : false })}
        {...selectProps}
      >
        {placeholder && (
          <option value="" disabled>
            {placeholder}
          </option>
        )}
        {options.map((option) => (
          <option
            key={option.value}
            value={option.value}
            disabled={option.disabled}
          >
            {option.label}
          </option>
        ))}
      </select>
      
      {errorMessage && (
        <p className="text-sm text-red-600">{errorMessage}</p>
      )}
    </div>
  );
}
