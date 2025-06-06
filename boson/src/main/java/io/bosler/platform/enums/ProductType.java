package io.bosler.platform.enums;

public enum ProductType {
    DATA_PLATFORM("DATA_PLATFORM"),
    DATA_HUB( "DATA_HUB"),
    DATA_VIZ("DATA_VIZ");

    private final String name;

    ProductType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
