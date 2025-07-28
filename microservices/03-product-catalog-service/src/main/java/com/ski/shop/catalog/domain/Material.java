package com.ski.shop.catalog.domain;

/**
 * マテリアル列挙型
 */
public enum Material {
    WOOD_CORE("ウッドコア"),
    CARBON_FIBER("カーボンファイバー"),
    TITANIUM("チタニウム"),
    SYNTHETIC("合成素材"),
    ALUMINUM("アルミニウム"),
    PLASTIC("プラスチック"),
    FIBERGLASS("ファイバーグラス"),
    STEEL("スチール"),  
    WAX("ワックス"),
    LEATHER("レザー(革)"),
    COMPOSITE("コンポジット");

    private final String displayName;

    Material(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
