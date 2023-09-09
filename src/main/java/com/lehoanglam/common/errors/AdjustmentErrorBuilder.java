package com.yes4all.common.errors;

import java.util.List;
import java.util.stream.Collectors;

import static com.yes4all.common.constants.AdjustmentErrorConstants.*;

public class AdjustmentErrorBuilder {
    public static String buildDuplicate(List<String> listSku){
        return build(
            ADJUST_ERROR_DUPLICATE_SINGULAR,
            ADJUST_ERROR_DUPLICATE_MULTIPLE,
            listSku
        );
    }
    public static String buildNotFound(List<String> listSku){
        return build(
            ADJUST_ERROR_NOT_FOUND_SINGULAR,
            ADJUST_ERROR_NOT_FOUND_MULTIPLE,
            listSku
        );
    }
    public static String buildNotChange(List<String> listSku){
        return build(
            ADJUST_ERROR_NOT_CHANGE_SINGULAR,
            ADJUST_ERROR_NOT_CHANGE_MULTIPLE,
            listSku
        );
    }

//    public static String buildNotCorrectFormat(List<String> listSku){
//        return build(
//            ADJUST_ERROR_NOT_CORRECT_FORMAT_SINGULAR,
//            ADJUST_ERROR_NOT_CORRECT_FORMAT_MULTIPLE,
//            listSku
//        );
//    }


    public static String build(String strFormatSingle,String strFormatMulti, List<String> parameters){
        String skuErrorMessage ="";
        if (!parameters.isEmpty()){
            if (parameters.size() == 1){
                skuErrorMessage = String.format(
                    strFormatSingle,
                    parameters.get(0).toUpperCase()
                );
            }else {
                skuErrorMessage = String.format(
                    strFormatMulti,
                    parameters.stream().map(String::toUpperCase).collect(Collectors.joining("-"))
                );
            }
        }
        return skuErrorMessage;
    }
}
