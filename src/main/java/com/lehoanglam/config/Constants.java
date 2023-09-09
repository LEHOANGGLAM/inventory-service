package com.yes4all.config;

/**
 * Application constants.
 */
public final class Constants {

    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$";

    public static final String SYSTEM = "system";
    public static final String RECEIPT_PREFIX = "IN";
    public static final String ADJUSTMENT_PREFIX = "AN";
    public static final String ISSUE_NOTE_PREFIX = "ON";
    public static final int RECEIPT_LENGTH = 6;
    public static final String DEFAULT_LANGUAGE = "en";
    public static final String ADJUSTMENT_INCREASE_TYPE = "increase";
    public static final String ADJUSTMENT_ADJUSTMENT_TYPE = "adjustment";
    public static final String ADJUSTMENT_DECREASE_TYPE = "decrease";

    private Constants() {}
}
