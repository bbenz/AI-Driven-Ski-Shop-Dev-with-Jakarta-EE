package com.skiresort.inventory.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RSアプリケーション設定
 */
@ApplicationPath("/")
public class InventoryApplication extends Application {
    // Jakarta REST アプリケーションの設定
    // 特別な設定が不要な場合は空でも可
}
