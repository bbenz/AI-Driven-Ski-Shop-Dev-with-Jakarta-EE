package com.skiresort.order;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS アプリケーション設定
 */
@ApplicationPath("/")
public class OrderManagementApplication extends Application {
    // 自動的にCDIによってエンドポイントが検出される
}
