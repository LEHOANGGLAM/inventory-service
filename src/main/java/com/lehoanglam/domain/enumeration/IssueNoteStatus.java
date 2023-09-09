package com.yes4all.domain.enumeration;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

/**
 * The IssueNoteStatus enumeration.
 */
@Getter
public enum IssueNoteStatus {
    NEW("NEW", "NEW"),
    CONFIRMED("CONFIRMED", "CONFIRMED"),
    APPROVED("APPROVED", "APPROVED"),
    COMPLETED("COMPLETED", "COMPLETED");

    private final String key;
    private final String value;

    IssueNoteStatus(String key, String value) {
        this.key = key;
        this.value = value;
        IssueNoteStatus.Holder.MAP.put(value, this);
    }

    private static class Holder {

        static Map<String, IssueNoteStatus> MAP = new HashMap<>();
    }

    public static IssueNoteStatus getEnumByValue(String value) {
        return IssueNoteStatus.Holder.MAP.get(value);
    }

    public static IssueNoteStatus getEnumByKey(String key) {
        Optional<IssueNoteStatus> tier = Arrays
            .stream(IssueNoteStatus.values())
            .filter(issueNoteStatus -> issueNoteStatus.getKey().equals(key))
            .findFirst();
        return tier.isPresent() ? tier.get() : null;
    }

    public static String getValueByKey(String key) {
        Optional<IssueNoteStatus> tier = Arrays.stream(IssueNoteStatus.values()).filter(value -> value.getKey().equals(key)).findFirst();
        return tier.isPresent() ? tier.get().getValue() : "";
    }

    public static String getKeyByValue(String value) {
        Optional<IssueNoteStatus> tier = Arrays
            .stream(IssueNoteStatus.values())
            .filter(issueNoteStatus -> issueNoteStatus.getValue().equals(value))
            .findFirst();
        return tier.map(IssueNoteStatus::getKey).orElse(null);
    }

    public static LinkedHashMap<String, String> getKeyAndValue() {
        return Stream.of(values()).collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue(), (x, y) -> y, LinkedHashMap::new));
    }
}
