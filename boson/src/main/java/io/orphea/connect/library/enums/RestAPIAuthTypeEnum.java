package io.orphea.connect.library.enums;

public enum RestAPIAuthTypeEnum {
    NONE("NONE"),
    BEARERTOKEN("BEARERTOKEN"),
    APIKEY("APIKEY");

    private final String displayName;

    RestAPIAuthTypeEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDatasetMappingEnums() {
        return displayName;
    }
}
