package io.movetodata.dataset.library.enums;

public enum DatasetMappingEnums {
    DATASET("dataset"),
    PLATFORM("platform");

    private final String displayName;

    DatasetMappingEnums(String displayName) {
        this.displayName = displayName;
    }

    public String getDatasetMappingEnums() {
        return displayName;
    }
}