'use client';

import { useState } from 'react';
import { useAuth } from '@/hooks/useAuth';
import PointsLoyaltyDashboard, { PointsWidget } from '@/components/loyalty/PointsLoyaltyDashboard';

interface IntegratedServicesProps {
  children: React.ReactNode;
}

export default function IntegratedServices({ children }: IntegratedServicesProps) {
  const { user, isAuthenticated } = useAuth();
  const [isAISupportOpen, setIsAISupportOpen] = useState(false);
  const [isLoyaltyDashboardOpen, setIsLoyaltyDashboardOpen] = useState(false);

  return (
    <>
      {children}
      
      {/* AIサポートとポイント機能のUI（認証済みユーザーのみ） */}
      {isAuthenticated && user && (
        <>
          {/* ヘッダーにポイントウィジェット */}
          <div className="fixed top-4 right-4 z-30">
            <PointsWidget 
              userId={user.id} 
              onOpenDashboard={() => setIsLoyaltyDashboardOpen(true)}
            />
          </div>


          {/* ポイント・ロイヤルティダッシュボード */}
          <PointsLoyaltyDashboard
            userId={user.id}
            isOpen={isLoyaltyDashboardOpen}
            onClose={() => setIsLoyaltyDashboardOpen(false)}
          />
        </>
      )}
    </>
  );
}
