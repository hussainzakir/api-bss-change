package com.trinet.ambis.enums;

public enum ProductEnums {

    AMBROSE("Ambrose", "Ambrose Product"), 
    PASSPORT("Passport", "Passport Product"),
    HOSPITALITY("ACD", "Hosptality Product");
    
    private final String type;
    private final String description;

    private ProductEnums(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}