package com.yes4all.common.generator;

import com.yes4all.config.Constants;

public class Generator {

    public static String generateReceiptCode(Long sequence) {
        return Constants.RECEIPT_PREFIX + String.format("%0"+Constants.RECEIPT_LENGTH+"d", sequence+1);
    }
}
