package io.bosler.connect.library.enums;

public enum SourceAuthTypeEnum {
    DEFAULT("DEFAULT"),
    KEYPAIR("KEYPAIR");


    private final String displayName;

    SourceAuthTypeEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDatasetMappingEnums() {
        return displayName;
    }
}
