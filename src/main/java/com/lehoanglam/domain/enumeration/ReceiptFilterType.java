package com.yes4all.domain.enumeration;

public enum ReceiptFilterType {
    VOUCHER_CODE("VOUCHERCODE"),
    RECEIPT_TYPE("RECEIPTTYPE"),
    STATUS("STATUS"),
    CREATED_DATE("CREATEDDATE"),
    RECEIPT_DATE("RECEIPTDATE"),
    SKU("SKU"),
    DEFAULT("DEFAULT");

    private final String receiptFilterType;

    ReceiptFilterType(String receiptFilterType) {
        this.receiptFilterType = receiptFilterType;
    }

    @Override
    public String toString() {
        return receiptFilterType;
    }

    public static ReceiptFilterType valueOfOrDefault(String myValue) {
        String value = myValue.toUpperCase().replaceAll("\\s", "_");
        for (ReceiptFilterType type : ReceiptFilterType.class.getEnumConstants()) {
            if (type.toString().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return ReceiptFilterType.DEFAULT;
    }
}
