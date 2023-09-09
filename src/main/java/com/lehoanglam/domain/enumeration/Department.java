package com.yes4all.domain.enumeration;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Department enumeration.
 */
public enum Department {
    RECEIVING("RECEIVING"),
    RETURN("RETURN"),
    WHOLESALE("WHOLESALE"),
    RETAIL("RETAIL");

    private final String value;

    Department(String value) {
        this.value = value;
    }

    public static LinkedHashMap<String, String> getKeyAndValue() {
        return Stream.of(values()).collect(Collectors.toMap(Enum::name, Department::getValue, (x, y) -> y, LinkedHashMap::new));
    }

    public String getValue() {
        return this.value;
    }
}
