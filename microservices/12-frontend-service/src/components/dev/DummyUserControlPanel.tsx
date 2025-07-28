'use client';

import React, { useState, useEffect } from 'react';
import { Card, Button } from '@/components/ui';
import { toggleDummyUser, isDummyUserActive, DUMMY_USER, DUMMY_CREDENTIALS } from '@/services/api/dummy-auth';

interface DummyUserControlPanelProps {
  className?: string;
}

export default function DummyUserControlPanel({ className = '' }: DummyUserControlPanelProps) {
  const [isEnabled, setIsEnabled] = useState(false);
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    setIsEnabled(isDummyUserActive());
    // 開発環境でのみ表示
    setIsVisible(process.env.NODE_ENV === 'development');
  }, []);

  const handleToggle = () => {
    const newState = !isEnabled;
    toggleDummyUser(newState);
    setIsEnabled(newState);
  };

  // 本番環境では何も表示しない
  if (!isVisible) {
    return null;
  }

  return (
    <div className={`fixed top-4 right-4 z-50 ${className}`}>
      <Card className="p-4 bg-yellow-50 border-yellow-200 shadow-lg max-w-sm">
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-sm font-semibold text-yellow-800">🔧 開発者パネル</h3>
          <span className="text-xs bg-yellow-200 text-yellow-800 px-2 py-1 rounded">DEV</span>
        </div>
        
        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-sm text-gray-700">ダミーユーザ機能</span>
            <Button
              size="sm"
              variant={isEnabled ? "default" : "outline"}
              onClick={handleToggle}
              className={`text-xs ${isEnabled ? 'bg-green-600 hover:bg-green-700' : 'text-gray-600'}`}
            >
              {isEnabled ? '有効' : '無効'}
            </Button>
          </div>

          {isEnabled && (
            <div className="text-xs text-gray-600 bg-white p-3 rounded border">
              <div className="font-medium mb-2">ダミーユーザ情報:</div>
              <div className="space-y-1 font-mono">
                <div>📧 {DUMMY_CREDENTIALS.email}</div>
                <div>🔑 {DUMMY_CREDENTIALS.password}</div>
                <div>👤 {DUMMY_USER.lastName} {DUMMY_USER.firstName}</div>
              </div>
            </div>
          )}
        </div>

        <div className="mt-3 pt-3 border-t border-yellow-200">
          <p className="text-xs text-yellow-700">
            本番環境では自動的に無効化されます
          </p>
        </div>
      </Card>
    </div>
  );
}
