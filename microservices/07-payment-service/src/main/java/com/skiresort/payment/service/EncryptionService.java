package com.skiresort.payment.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PostConstruct;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * 暗号化サービス（Java標準ライブラリ使用）
 */
@ApplicationScoped
public class EncryptionService {
    
    private static final Logger logger = Logger.getLogger(EncryptionService.class.getName());
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    
    @ConfigProperty(name = "payment.encryption.key", defaultValue = "default-key-32-chars-length-here")
    private String encryptionKey;
    
    private SecretKey secretKey;
    private SecureRandom secureRandom;
    
    @PostConstruct
    public void init() {
        try {
            // キーを32バイトに調整
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(encryptionKey.getBytes(StandardCharsets.UTF_8));
            secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            secureRandom = new SecureRandom();
            logger.info("EncryptionService initialized");
        } catch (Exception e) {
            logger.severe("EncryptionService初期化エラー: " + e.getMessage());
            throw new RuntimeException("暗号化サービスの初期化に失敗しました", e);
        }
    }
    
    /**
     * データを暗号化する
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }
        
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            
            // IVを生成
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            // IVと暗号化データを結合
            byte[] encryptedWithIv = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(cipherText, 0, encryptedWithIv, iv.length, cipherText.length);
            
            return Base64.getEncoder().encodeToString(encryptedWithIv);
        } catch (Exception e) {
            logger.severe("暗号化エラー: " + e.getMessage());
            throw new RuntimeException("暗号化に失敗しました", e);
        }
    }
    
    /**
     * データを復号化する
     */
    public String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return encryptedData;
        }
        
        try {
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            
            // IVと暗号化データを分離
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] cipherText = new byte[decodedData.length - GCM_IV_LENGTH];
            
            System.arraycopy(decodedData, 0, iv, 0, iv.length);
            System.arraycopy(decodedData, iv.length, cipherText, 0, cipherText.length);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.severe("復号化エラー: " + e.getMessage());
            throw new RuntimeException("復号化に失敗しました", e);
        }
    }
    
    /**
     * カード番号をマスクする
     */
    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 6) {
            return "**** **** **** ****";
        }
        
        String cleaned = cardNumber.replaceAll("\\D", "");
        if (cleaned.length() < 6) {
            return "**** **** **** ****";
        }
        
        String first4 = cleaned.substring(0, 4);
        String last4 = cleaned.substring(Math.max(4, cleaned.length() - 4));
        
        return first4 + " **** **** " + last4;
    }
    
    /**
     * セキュアハッシュを生成する
     */
    public String generateHash(String data) {
        if (data == null) {
            return null;
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            logger.severe("ハッシュ生成エラー: " + e.getMessage());
            throw new RuntimeException("ハッシュ生成に失敗しました", e);
        }
    }
}
