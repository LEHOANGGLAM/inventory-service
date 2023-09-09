package com.yes4all.common.constants;

import java.util.Arrays;
import java.util.List;
import org.checkerframework.checker.units.qual.A;
import org.w3c.dom.stylesheets.LinkStyle;

public class GlobalConstant {

    public static final String PDF_FILE_EXTENSION = ".pdf";
    public static final String EXCEL_FILE_EXTENSION = ".xlsx";
    public static final String JASPER_FILE_EXTENSION = ".jasper";
    public static final String PHONE_PATTERN_REGEX ="^(\\+\\d{1,5}( )?)?((\\(\\d{1,5}\\))|\\d{1,5})[- .]?\\d{1,5}[- .]?\\d{4}$";
    public static final String JASPER_XML_FILE_EXTENSION = ".jrxml";
    public static final String REASON_PATTERN_REGEX = "SYSTEM_CORRECTION" +
        "|MISPLACED_FOUND" +
        "|DAMAGED_INVENTORY" +
        "|WRITE_OFF_INVENTORY" +
        "|CYCLE_COUNT" +
        "|MANUAL_PI_FBM" +
        "|MANUAL_PI_AVC_DS" +
        "|MANUAL_PI_WM_DSV" +
        "|MANUAL_PI_LOCAL";

}
