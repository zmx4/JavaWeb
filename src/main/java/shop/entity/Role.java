package shop.entity;

public enum Role {
    CUSTOMER("顾客"),
    MERCHANT("商户"),
    ADMIN("管理员");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
