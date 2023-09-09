package com.yes4all.domain.enumeration;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

/**
 * The ReceiptType enumeration.
 */
@Getter
public enum ReceiptType {
    PURCHASE_ORDER("PURCHASE_ORDER", "PURCHASE ORDERS"),
    EXTERNAL_TRANSFER("EXTERNAL_TRANSFER", "EXTERNAL TRANSFER"),
    RETURN("RETURN", "RETURN"),
    INTERNAL_TRANSFER("INTERNAL_TRANSFER", "INTERNAL TRANSFER"),
    INCREASE_ADJUSTMENT("INCREASE_ADJUSTMENT", "INCREASE ADJUSTMENT"),
    OTHERS("OTHERS", "OTHERS");

    private final String key;
    private final String value;

    ReceiptType(String key, String value) {
        this.key = key;
        this.value = value;
        Holder.MAP.put(value, this);
    }

    private static class Holder {

        static Map<String, ReceiptType> MAP = new HashMap<>();
    }

    public static ReceiptType getEnumByValue(String value) {
        return Holder.MAP.get(value);
    }

    public static String getValueByKey(String key) {
        Optional<ReceiptType> tier = Arrays.stream(ReceiptType.values()).filter(value -> value.getKey().equals(key)).findFirst();
        return tier.isPresent() ? tier.get().getValue() : "";
    }

    public static LinkedHashMap<String, String> getKeyAndValue() {
        return Stream.of(values()).collect(Collectors.toMap(ReceiptType::getKey, ReceiptType::getValue, (x, y) -> y, LinkedHashMap::new));
    }

    public static ReceiptType getEnumByKey(String key) {
        Optional<ReceiptType> tier = Arrays
            .stream(ReceiptType.values())
            .filter(receiptType -> receiptType.getKey().equals(key))
            .findFirst();
        return tier.orElse(null);
    }
}
