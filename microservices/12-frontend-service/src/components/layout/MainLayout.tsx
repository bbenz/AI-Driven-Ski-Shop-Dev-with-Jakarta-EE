import React from 'react';
import { Header } from './Header';
import { Footer } from './Footer';

export interface MainLayoutProps {
  children: React.ReactNode;
  className?: string;
  showHeader?: boolean;
  showFooter?: boolean;
}

export const MainLayout: React.FC<MainLayoutProps> = ({
  children,
  className = '',
  showHeader = true,
  showFooter = true,
}) => {
  return (
    <div className={`min-h-screen flex flex-col ${className}`}>
      {showHeader && <Header />}
      
      <main className="flex-1">
        {children}
      </main>
      
      {showFooter && <Footer />}
    </div>
  );
};
