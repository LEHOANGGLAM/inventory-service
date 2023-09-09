package com.yes4all.domain.enumeration;

import lombok.Getter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Reason enumeration.
 */
@Getter
public enum Reason {
    SYSTEM_CORRECTION("SYSTEM_CORRECTION","SYSTEM CORRECTION"),
    MISPLACED_FOUND("MISPLACED_FOUND","MISPLACED FOUND"),
    DAMAGED_INVENTORY("DAMAGED_INVENTORY","DAMAGED INVENTORY"),
    WRITE_OFF_INVENTORY("WRITE_OFF_INVENTORY","WRITE OFF INVENTORY"),
    CYCLE_COUNT("CYCLE_COUNT","CYCLE COUNT"),
    MANUAL_PI_FBM("MANUAL_PI_FBM","MANUAL PI FBM"),
    MANUAL_PI_AVC_DS("MANUAL_PI_AVC_DS","MANUAL PI AVC DS"),
    MANUAL_PI_WM_DSV("MANUAL_PI_WM_DSV","MANUAL PI WM DSV"),
    MANUAL_PI_LOCAL("MANUAL_PI_LOCAL","MANUAL PI LOCAL");
    private final String key;
    private final String value;

    Reason(String key, String value) {
        this.key = key;
        this.value = value;
        Reason.Holder.MAP.put(value, this);
    }

    private static class Holder {
        static Map<String, Reason> MAP = new HashMap<>();
    }

    public static LinkedHashMap<String, String> getKeyAndValue() {
        return Stream.of(values()).collect(Collectors.toMap(Reason::getKey, Reason::getValue, (x, y) -> y, LinkedHashMap::new));
    }
}
