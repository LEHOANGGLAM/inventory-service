package com.yes4all.domain.enumeration;

import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Channel enumeration.
 */
@Getter
public enum Channel {
    AVC_WH_DI("AVC_WH_DI", "AVC WH-DI"),
    AVC_DROPSHIP("AVC_DROPSHIP", "AVC DROPSHIP"),
    ASC_FBA("ASC_FBA", "ASC FBA"),
    ASC_FBM("ASC_FBM", "ASC FBM"),
    WM_DSV("WM_DSV", "WM DSV"),
    WM_WFS("WM_WFS", "WM WFS"),
    LOCAL("LOCAL", "LOCAL"),
    WAYFAIR("WAYFAIR", "WAYFAIR"),
    ;

    private final String key;
    private final String value;

    Channel(String key, String value) {
        this.key = key;
        this.value = value;
        Channel.Holder.MAP.put(value, this);
    }

    private static class Holder {
        static Map<String, Channel> MAP = new HashMap<>();
    }

    public static Channel getEnumByValue(String value) {
        return Channel.Holder.MAP.get(value);
    }

    public static Channel getEnumByKey(String key){
        Optional<Channel> tier = Arrays.stream(Channel.values())
            .filter(channel -> channel.getKey().equals(key))
            .findFirst();
        return tier.isPresent() ? tier.get() : null;
    }

    public static String getValueByKey(String key) {
        Optional<Channel> tier = Arrays.stream(Channel.values())
            .filter(value -> value.getKey().equals(key))
            .findFirst();
        return tier.isPresent() ? tier.get().getValue() : "";
    }

    public static String getKeyByValue(String value) {
        Optional<Channel> tier = Arrays.stream(Channel.values())
            .filter(channel -> channel.getValue().equals(value))
            .findFirst();
        return tier.map(Channel::getKey).orElse(null);
    }

    public static LinkedHashMap<String, String> getKeyAndValue(){
        return Stream.of(values()).collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue(), (x, y) -> y, LinkedHashMap::new));
    }
}
