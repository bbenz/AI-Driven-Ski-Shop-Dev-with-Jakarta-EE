package com.skiresort.auth.config;

import jakarta.annotation.security.DeclareRoles;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.auth.LoginConfig;

/**
 * MicroProfile JWT 2.1セキュリティ設定
 */
@ApplicationPath("/api")
@ApplicationScoped
@LoginConfig(authMethod = "MP-JWT", realmName = "ski-equipment-shop")
@DeclareRoles({"user", "admin", "employee", "guest"})
public class JwtSecurityConfig extends Application {
    
    // MicroProfile JWT 2.1の設定は@LoginConfigアノテーションで行う
    // 追加の設定はmicroprofile-config.propertiesで行う
}
