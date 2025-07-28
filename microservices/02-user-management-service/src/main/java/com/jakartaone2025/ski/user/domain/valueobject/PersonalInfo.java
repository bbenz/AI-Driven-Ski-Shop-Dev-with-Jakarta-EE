package com.jakartaone2025.ski.user.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.time.Period;

/**
 * 個人情報を表すValue Object
 * Record型を使用してイミュータブルな値オブジェクトとして実装
 */
@Embeddable
public record PersonalInfo(
    @NotBlank(message = "名前（名）は必須です")
    @Column(name = "first_name", nullable = false, length = 50)
    String firstName,
    
    @NotBlank(message = "名前（姓）は必須です")
    @Column(name = "last_name", nullable = false, length = 50)
    String lastName,
    
    @Column(name = "first_name_kana", length = 50)
    String firstNameKana,
    
    @Column(name = "last_name_kana", length = 50)
    String lastNameKana,
    
    @Past(message = "生年月日は過去の日付である必要があります")
    @Column(name = "birth_date")
    LocalDate birthDate,
    
    @NotNull(message = "性別は必須です")
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    Gender gender,
    
    @Pattern(regexp = "^0\\d{1,4}-\\d{1,4}-\\d{4}$", message = "正しい電話番号形式で入力してください")
    @Column(name = "phone_number", length = 20)
    String phoneNumber
) {
    
    /**
     * フルネームを取得
     */
    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }
    
    /**
     * フルネーム（カナ）を取得
     */
    public String getFullNameKana() {
        if (firstNameKana != null && lastNameKana != null) {
            return String.format("%s %s", firstNameKana, lastNameKana);
        }
        return null;
    }
    
    /**
     * 年齢を計算
     */
    public Integer getAge() {
        if (birthDate == null) {
            return null;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
    
    /**
     * 成人かどうかを判定
     */
    public boolean isAdult() {
        Integer age = getAge();
        return age != null && age >= 18;
    }
    
    /**
     * 表示用の電話番号を取得（マスク処理）
     */
    public String getMaskedPhoneNumber() {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return phoneNumber;
        }
        // 末尾4桁以外をマスク
        String masked = phoneNumber.substring(0, phoneNumber.length() - 4)
                                  .replaceAll("\\d", "*");
        return masked + phoneNumber.substring(phoneNumber.length() - 4);
    }
    
    /**
     * バリデーション付きビルダー
     */
    public static PersonalInfo of(String firstName, String lastName, LocalDate birthDate, Gender gender) {
        return new PersonalInfo(firstName, lastName, null, null, birthDate, gender, null);
    }
    
    /**
     * 完全な情報でのビルダー
     */
    public static PersonalInfo create(String firstName, String lastName, String firstNameKana, 
                                     String lastNameKana, LocalDate birthDate, Gender gender, 
                                     String phoneNumber) {
        return new PersonalInfo(firstName, lastName, firstNameKana, lastNameKana, 
                               birthDate, gender, phoneNumber);
    }
}
