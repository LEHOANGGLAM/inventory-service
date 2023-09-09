package com.yes4all.common.constants;

public class AdjustmentErrorConstants {

    public static final String ADJUST_ERROR_DUPLICATE_SINGULAR =
        "The '%s' in this file is duplicated. \n" + "Please check file again!\n" + "Notice: Every SKU(s) is unique in the import file.";
    public static final String ADJUST_ERROR_DUPLICATE_MULTIPLE =
        "The following SKU(s) in this file is duplicated: %s .\n" +
        "Please check this file again! \n" +
        "Notice: Every SKU(s) is unique in the import file.";
    public static final String ADJUST_ERROR_NOT_FOUND_SINGULAR =
        "The '%s' in this file is not imported in the inventory before.\n" + "Please check this file again!";
    public static final String ADJUST_ERROR_NOT_FOUND_MULTIPLE =
        "The following SKU(s) in this file is not imported in the inventory before: '%s' .\n" + "Please check this file again!";
    public static final String ADJUST_ERROR_NOT_CHANGE_SINGULAR =
        "The quantity at WIP and PKU of the %s in this file is equal to the quantity at WIP and PKU in system now.\n" +
        "Please check this file again!";
    public static final String ADJUST_ERROR_NOT_CHANGE_MULTIPLE =
        "The quantity at WIP and PKU of the following SKU(s) in this file is equal to the quantity at WIP and PKU in system now: %s .\n" +
        "Please check this file again!";
    public static final String ADJUST_ERROR_NOT_CORRECT_FORMAT_SINGULAR =
        "The format of the '%s' is not corrected.\n" + "Please check this file again!";
    public static final String ADJUST_ERROR_NOT_CORRECT_FORMAT_MULTIPLE =
        "The format of the following SKU(s) is not corrected: %s .\n" + "Please check this file again!";

    public static final String ADJUST_ERROR_FILE_INVALID_FORMAT = "This file is invalid to import.\nPlease check this file again!";
}
