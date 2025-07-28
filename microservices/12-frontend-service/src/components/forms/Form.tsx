import React from 'react';
import { useForm, FormProvider, FieldValues, UseFormProps } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { ZodSchema } from 'zod';
import { cn } from '@/utils/helpers';

export interface FormProps<T extends FieldValues> extends UseFormProps<T> {
  schema?: ZodSchema<T>;
  onSubmit: (data: T) => void | Promise<void>;
  children: React.ReactNode;
  className?: string;
  isLoading?: boolean;
}

export function Form<T extends FieldValues>({
  schema,
  onSubmit,
  children,
  className,
  isLoading = false,
  ...useFormProps
}: FormProps<T>) {
  const formOptions: UseFormProps<T> = {
    ...useFormProps,
  };

  if (schema) {
    // @ts-expect-error - zodResolver typing issue with generic constraints
    formOptions.resolver = zodResolver(schema);
  }

  const form = useForm<T>(formOptions);

  const handleSubmit = form.handleSubmit(async (data) => {
    try {
      await onSubmit(data);
    } catch (error) {
      console.error('Form submission error:', error);
    }
  });

  return (
    <FormProvider {...form}>
      <form
        onSubmit={handleSubmit}
        className={cn('space-y-6', className)}
        noValidate
      >
        <fieldset disabled={isLoading} className="space-y-6">
          {children}
        </fieldset>
      </form>
    </FormProvider>
  );
}
