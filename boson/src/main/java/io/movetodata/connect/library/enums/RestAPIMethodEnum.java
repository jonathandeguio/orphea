package io.movetodata.connect.library.enums;

public enum RestAPIMethodEnum {
    GET("NONE"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE");

    private final String displayName;

    RestAPIMethodEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDatasetMappingEnums() {
        return displayName;
    }
}
