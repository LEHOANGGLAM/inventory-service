package com.yes4all.domain.enumeration;

public enum AdjustmentFilterType {
    ADJUSTMENT_CODE("ADJUSTMENTCODE"),
    REASON("REASON"),
    CREATED_DATE("CREATEDDATE"),
    DEFAULT("DEFAULT");

    private final String adjustmentFilterType;

    AdjustmentFilterType(String adjustmentFilterType) {
        this.adjustmentFilterType = adjustmentFilterType;
    }

    @Override
    public String toString() {
        return adjustmentFilterType;
    }

    public static AdjustmentFilterType valueOfOrDefault(String myValue) {
        String value = myValue.toUpperCase().replaceAll("\\s", "_");
        for (AdjustmentFilterType type : AdjustmentFilterType.class.getEnumConstants()) {
            if (type.toString().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return AdjustmentFilterType.DEFAULT;
    }
}
