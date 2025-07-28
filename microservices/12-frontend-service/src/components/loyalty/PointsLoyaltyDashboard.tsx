'use client';

import { useState, useEffect, useCallback } from 'react';
import { Star, Gift, TrendingUp, Clock, Award, ChevronRight, Plus } from 'lucide-react';
import { pointsLoyaltyService, type PointsBalance, type LoyaltyTier, type PointTransaction, type PointsRedemption } from '@/services/api/points-loyalty';

interface PointsDisplayProps {
  points: number;
  size?: 'small' | 'medium' | 'large';
  showIcon?: boolean;
}

function PointsDisplay({ points, size = 'medium', showIcon = true }: PointsDisplayProps) {
  const sizeClasses = {
    small: 'text-sm',
    medium: 'text-lg font-semibold',
    large: 'text-2xl font-bold'
  };

  return (
    <div className={`flex items-center gap-1 text-blue-600 ${sizeClasses[size]}`}>
      {showIcon && <Star size={size === 'small' ? 14 : size === 'medium' ? 18 : 24} className="fill-current" />}
      <span>{points.toLocaleString()}ポイント</span>
    </div>
  );
}

interface TierBadgeProps {
  tier: LoyaltyTier;
  size?: 'small' | 'medium';
}

function TierBadge({ tier, size = 'medium' }: TierBadgeProps) {
  const sizeClasses = {
    small: 'px-2 py-1 text-xs',
    medium: 'px-3 py-1 text-sm'
  };

  // 色をTailwindクラスに変換
  const colorClass = tier.color === '#FFD700' ? 'bg-yellow-500' : 
                     tier.color === '#C0C0C0' ? 'bg-gray-400' : 
                     tier.color === '#CD7F32' ? 'bg-orange-600' : 
                     'bg-blue-600';

  return (
    <div 
      className={`inline-flex items-center gap-1 rounded-full text-white font-medium ${sizeClasses[size]} ${colorClass}`}
    >
      <Award size={size === 'small' ? 12 : 16} />
      {tier.name}
    </div>
  );
}

interface PointsLoyaltyDashboardProps {
  userId: string;
  isOpen: boolean;
  onClose: () => void;
}

export default function PointsLoyaltyDashboard({ userId, isOpen, onClose }: PointsLoyaltyDashboardProps) {
  const [pointsBalance, setPointsBalance] = useState<PointsBalance | null>(null);
  const [currentTier, setCurrentTier] = useState<LoyaltyTier | null>(null);
  const [nextTier, setNextTier] = useState<{ tier: LoyaltyTier; pointsNeeded: number } | null>(null);
  const [recentTransactions, setRecentTransactions] = useState<PointTransaction[]>([]);
  const [availableRedemptions, setAvailableRedemptions] = useState<PointsRedemption[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [activeTab, setActiveTab] = useState<'overview' | 'history' | 'redeem' | 'tiers'>('overview');

  const loadUserLoyaltyData = useCallback(async () => {
    setIsLoading(true);
    try {
      // 並行してデータを取得
      const [profile, history, redemptions] = await Promise.all([
        pointsLoyaltyService.getUserLoyaltyProfile(userId),
        pointsLoyaltyService.getPointsHistory(userId, { limit: 5 }),
        pointsLoyaltyService.getRedemptions({ available: true })
      ]);

      setPointsBalance(profile.pointsBalance);
      setCurrentTier(profile.currentTier);
      setNextTier(profile.nextTier || null);
      setRecentTransactions(history.transactions);
      setAvailableRedemptions(redemptions.slice(0, 6)); // 最大6件表示
    } catch (error) {
      console.error('Failed to load loyalty data:', error);
      // サービスが利用できない場合のフォールバックデータ
      const fallbackBalance = {
        userId,
        totalPoints: 0,
        availablePoints: 0,
        pendingPoints: 0,
        expiringPoints: [],
        lastUpdated: new Date().toISOString()
      };
      const fallbackTier = {
        id: 'bronze',
        name: 'ブロンズ',
        minPoints: 0,
        maxPoints: 999,
        benefits: [],
        color: '#CD7F32',
        icon: '🥉'
      };
      
      setPointsBalance(fallbackBalance);
      setCurrentTier(fallbackTier);
      setNextTier(null);
      setRecentTransactions([]);
      setAvailableRedemptions([]);
    } finally {
      setIsLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    if (isOpen && userId) {
      loadUserLoyaltyData();
    }
  }, [isOpen, userId, loadUserLoyaltyData]);

  const handleRedemption = async (redemptionId: string, pointsToRedeem: number) => {
    try {
      await pointsLoyaltyService.redeemPoints({
        userId,
        redemptionId,
        pointsToRedeem
      });
      
      // データを再読み込み
      await loadUserLoyaltyData();
      
      alert('ポイント交換が完了しました！');
    } catch (error) {
      console.error('Failed to redeem points:', error);
      alert('ポイント交換に失敗しました。再度お試しください。');
    }
  };

  const getTransactionIcon = (type: PointTransaction['type']) => {
    switch (type) {
      case 'earned':
        return <Plus className="w-4 h-4 text-green-600" />;
      case 'redeemed':
        return <Gift className="w-4 h-4 text-blue-600" />;
      case 'expired':
        return <Clock className="w-4 h-4 text-red-600" />;
      default:
        return <TrendingUp className="w-4 h-4 text-gray-600" />;
    }
  };

  const getTransactionColor = (type: PointTransaction['type']) => {
    switch (type) {
      case 'earned':
        return 'text-green-600';
      case 'redeemed':
        return 'text-red-600';
      case 'expired':
        return 'text-gray-600';
      default:
        return 'text-gray-600';
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-hidden">
        {/* ヘッダー */}
        <div className="bg-gradient-to-r from-blue-600 to-purple-600 text-white p-6">
          <div className="flex justify-between items-start">
            <div>
              <h2 className="text-2xl font-bold mb-2">ポイント・ロイヤルティ</h2>
              {pointsBalance && (
                <div className="flex items-center gap-4">
                  <PointsDisplay points={pointsBalance.availablePoints} size="large" />
                  {currentTier && <TierBadge tier={currentTier} />}
                </div>
              )}
            </div>
            <button
              onClick={onClose}
              className="text-white hover:bg-white hover:bg-opacity-20 p-2 rounded-full transition-colors"
              aria-label="閉じる"
            >
              ×
            </button>
          </div>

          {/* 次のティアまでの進捗 */}
          {nextTier && pointsBalance && (() => {
            const progress = Math.max(0, Math.min(100, 
              ((pointsBalance.totalPoints - currentTier!.minPoints) / 
               (nextTier.tier.minPoints - currentTier!.minPoints)) * 100
            ));
            
            const progressClass = progress >= 75 ? 'w-3/4' :
                                  progress >= 50 ? 'w-1/2' :
                                  progress >= 25 ? 'w-1/4' : 'w-1/12';
            
            return (
              <div className="mt-4 bg-white bg-opacity-20 rounded-lg p-4">
                <div className="flex justify-between items-center mb-2">
                  <span className="text-sm">次のティア: {nextTier.tier.name}</span>
                  <span className="text-sm">{nextTier.pointsNeeded.toLocaleString()}ポイント不足</span>
                </div>
                <div className="w-full bg-white bg-opacity-30 rounded-full h-2 relative">
                  <div 
                    className={`bg-white h-2 rounded-full transition-all duration-500 ${progressClass}`}
                  ></div>
                  <span className="text-xs text-white absolute -top-5 right-0">
                    {progress.toFixed(0)}%
                  </span>
                </div>
              </div>
            );
          })()}
        </div>

        {/* タブナビゲーション */}
        <div className="border-b">
          <nav className="flex">
            {[
              { key: 'overview' as const, label: '概要' },
              { key: 'history' as const, label: '履歴' },
              { key: 'redeem' as const, label: '交換' },
              { key: 'tiers' as const, label: 'ティア' }
            ].map((tab) => (
              <button
                key={tab.key}
                onClick={() => setActiveTab(tab.key)}
                className={`px-6 py-3 font-medium transition-colors ${
                  activeTab === tab.key
                    ? 'text-blue-600 border-b-2 border-blue-600 bg-blue-50'
                    : 'text-gray-600 hover:text-blue-600 hover:bg-gray-50'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </nav>
        </div>

        {/* タブコンテンツ */}
        <div className="p-6 max-h-[50vh] overflow-y-auto">
          {isLoading ? (
            <div className="text-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
              <p className="mt-2 text-gray-600">読み込み中...</p>
            </div>
          ) : (
            <>
              {/* 概要タブ */}
              {activeTab === 'overview' && (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {/* ポイント残高詳細 */}
                  {pointsBalance && (
                    <div className="bg-gray-50 rounded-lg p-4">
                      <h3 className="font-semibold mb-3">ポイント残高</h3>
                      <div className="space-y-2">
                        <div className="flex justify-between">
                          <span className="text-gray-600">利用可能ポイント</span>
                          <PointsDisplay points={pointsBalance.availablePoints} size="small" />
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-600">保留中ポイント</span>
                          <PointsDisplay points={pointsBalance.pendingPoints} size="small" />
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-600">総ポイント</span>
                          <PointsDisplay points={pointsBalance.totalPoints} size="small" />
                        </div>
                      </div>
                    </div>
                  )}

                  {/* 最近の取引 */}
                  <div className="bg-gray-50 rounded-lg p-4">
                    <h3 className="font-semibold mb-3">最近の取引</h3>
                    <div className="space-y-2">
                      {recentTransactions.slice(0, 3).map((transaction) => (
                        <div key={transaction.id} className="flex items-center justify-between py-2">
                          <div className="flex items-center gap-2">
                            {getTransactionIcon(transaction.type)}
                            <span className="text-sm text-gray-700">{transaction.description}</span>
                          </div>
                          <span className={`text-sm font-medium ${getTransactionColor(transaction.type)}`}>
                            {transaction.type === 'earned' ? '+' : '-'}{transaction.points.toLocaleString()}
                          </span>
                        </div>
                      ))}
                      <button
                        onClick={() => setActiveTab('history')}
                        className="w-full text-blue-600 text-sm py-2 hover:bg-blue-50 rounded transition-colors"
                      >
                        すべて見る
                      </button>
                    </div>
                  </div>
                </div>
              )}

              {/* 履歴タブ */}
              {activeTab === 'history' && (
                <div>
                  <h3 className="font-semibold mb-4">ポイント取引履歴</h3>
                  <div className="space-y-3">
                    {recentTransactions.map((transaction) => (
                      <div key={transaction.id} className="border rounded-lg p-4">
                        <div className="flex items-center justify-between mb-2">
                          <div className="flex items-center gap-2">
                            {getTransactionIcon(transaction.type)}
                            <span className="font-medium">{transaction.description}</span>
                          </div>
                          <span className={`font-semibold ${getTransactionColor(transaction.type)}`}>
                            {transaction.type === 'earned' ? '+' : '-'}{transaction.points.toLocaleString()}ポイント
                          </span>
                        </div>
                        <div className="text-sm text-gray-600">
                          {new Date(transaction.timestamp).toLocaleDateString('ja-JP', {
                            year: 'numeric',
                            month: 'long',
                            day: 'numeric',
                            hour: '2-digit',
                            minute: '2-digit'
                          })}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* 交換タブ */}
              {activeTab === 'redeem' && (
                <div>
                  <h3 className="font-semibold mb-4">ポイント交換</h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {availableRedemptions.map((redemption) => (
                      <div key={redemption.id} className="border rounded-lg p-4">
                        <h4 className="font-medium mb-2">{redemption.name}</h4>
                        <p className="text-sm text-gray-600 mb-3">{redemption.description}</p>
                        <div className="flex items-center justify-between mb-3">
                          <PointsDisplay points={redemption.pointsRequired} size="small" />
                          <span className="text-lg font-semibold text-green-600">
                            ¥{redemption.value.toLocaleString()}相当
                          </span>
                        </div>
                        <button
                          onClick={() => handleRedemption(redemption.id, redemption.pointsRequired)}
                          disabled={!pointsBalance || pointsBalance.availablePoints < redemption.pointsRequired}
                          className="w-full py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                        >
                          {pointsBalance && pointsBalance.availablePoints >= redemption.pointsRequired 
                            ? '交換する' 
                            : 'ポイント不足'
                          }
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* ティアタブ */}
              {activeTab === 'tiers' && currentTier && (
                <div>
                  <h3 className="font-semibold mb-4">ロイヤルティティア</h3>
                  <div className="bg-gradient-to-r from-blue-50 to-purple-50 rounded-lg p-6 mb-6">
                    <div className="text-center mb-4">
                      <TierBadge tier={currentTier} size="medium" />
                      <p className="mt-2 text-gray-600">現在のティア</p>
                    </div>
                    <div>
                      <h4 className="font-medium mb-2">特典</h4>
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                        {currentTier.benefits.map((benefit, index) => (
                          <div key={index} className="flex items-center gap-2 text-sm">
                            <ChevronRight size={14} className="text-blue-600" />
                            <span>{benefit.description}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>

                  {nextTier && (
                    <div className="border rounded-lg p-4">
                      <h4 className="font-medium mb-2">次のティア</h4>
                      <div className="flex items-center justify-between">
                        <TierBadge tier={nextTier.tier} size="medium" />
                        <span className="text-sm text-gray-600">
                          あと{nextTier.pointsNeeded.toLocaleString()}ポイント
                        </span>
                      </div>
                    </div>
                  )}
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}

// ポイント表示用の小さなウィジェット
interface PointsWidgetProps {
  userId: string;
  onOpenDashboard: () => void;
}

export function PointsWidget({ userId, onOpenDashboard }: Readonly<PointsWidgetProps>) {
  const [pointsBalance, setPointsBalance] = useState<PointsBalance | null>(null);
  const [currentTier, setCurrentTier] = useState<LoyaltyTier | null>(null);
  const [hasError, setHasError] = useState(false);

  const loadPointsData = useCallback(async () => {
    try {
      setHasError(false);
      const [balance, profile] = await Promise.all([
        pointsLoyaltyService.getPointsBalance(userId),
        pointsLoyaltyService.getUserLoyaltyProfile(userId)
      ]);
      setPointsBalance(balance);
      setCurrentTier(profile.currentTier);
    } catch (error) {
      console.error('Failed to load points data:', error);
      setHasError(true);
      // ポイントサービスが利用できない場合のフォールバック
      setPointsBalance({
        userId,
        totalPoints: 0,
        availablePoints: 0,
        pendingPoints: 0,
        expiringPoints: [],
        lastUpdated: new Date().toISOString()
      });
      setCurrentTier({
        id: 'bronze',
        name: 'ブロンズ',
        minPoints: 0,
        maxPoints: 999,
        benefits: [],
        color: '#CD7F32',
        icon: '🥉'
      });
    }
  }, [userId]);

  useEffect(() => {
    if (userId) {
      loadPointsData();
    }
  }, [userId, loadPointsData]);

  if (!pointsBalance) return null;

  return (
    <button
      onClick={onOpenDashboard}
      className={`bg-white border rounded-lg p-3 hover:shadow-md transition-shadow ${
        hasError ? 'border-yellow-300 bg-yellow-50' : ''
      }`}
      title={hasError ? 'ポイントサービスが一時的に利用できません' : undefined}
    >
      <div className="flex items-center justify-between">
        <div>
          <PointsDisplay points={pointsBalance.availablePoints} size="small" />
          {currentTier && (
            <div className="mt-1">
              <TierBadge tier={currentTier} size="small" />
            </div>
          )}
          {hasError && (
            <div className="text-xs text-yellow-600 mt-1">
              オフライン
            </div>
          )}
        </div>
        <ChevronRight size={16} className="text-gray-400" />
      </div>
    </button>
  );
}
