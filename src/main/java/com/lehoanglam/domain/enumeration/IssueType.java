package com.yes4all.domain.enumeration;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

/**
 * The IssueType enumeration.
 */
@Getter
public enum IssueType {
    SALES_ORDERS("SALES_ORDERS", "SALES ORDERS"),
    INTERNAL_TRANSFER("INTERNAL_TRANSFER", "INTERNAL TRANSFER"),
    EXTERNAL_TRANSFER("EXTERNAL_TRANSFER", "EXTERNAL TRANSFER"),
    DECREASE_ADJUSTMENT("DECREASE_ADJUSTMENT", "DECREASE ADJUSTMENT"),
    OTHERS("OTHERS", "OTHERS");

    private final String key;
    private final String value;

    IssueType(String key, String value) {
        this.key = key;
        this.value = value;
        Holder.MAP.put(value, this);
    }

    private static class Holder {

        static Map<String, IssueType> MAP = new HashMap<>();
    }

    public static IssueType getEnumByValue(String value) {
        return Holder.MAP.get(value);
    }

    public static IssueType getEnumByKey(String key) {
        Optional<IssueType> tier = Arrays.stream(IssueType.values()).filter(issueType -> issueType.getKey().equals(key)).findFirst();
        return tier.isPresent() ? tier.get() : null;
    }

    public static String getValueByKey(String key) {
        Optional<IssueType> tier = Arrays.stream(IssueType.values()).filter(value -> value.getKey().equals(key)).findFirst();
        return tier.isPresent() ? tier.get().getValue() : "";
    }

    public static String getKeyByValue(String value) {
        Optional<IssueType> tier = Arrays.stream(IssueType.values()).filter(issueType -> issueType.getValue().equals(value)).findFirst();
        return tier.map(IssueType::getKey).orElse(null);
    }

    public static LinkedHashMap<String, String> getKeyAndValue() {
        return Stream.of(values()).collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue(), (x, y) -> y, LinkedHashMap::new));
    }
}
