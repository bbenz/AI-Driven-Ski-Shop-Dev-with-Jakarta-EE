package com.ski.shop.catalog.domain;

/**
 * 公開ステータス列挙型
 */
public enum PublishStatus {
    DRAFT("下書き"),
    REVIEW_PENDING("レビュー待ち"),
    PUBLISHED("公開"),
    ARCHIVED("アーカイブ");

    private final String displayName;

    PublishStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
