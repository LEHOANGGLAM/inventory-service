package com.yes4all.common.errors;

public class NotFoundException extends RuntimeException {

    protected String errorCode;
    protected String errorDesc;

    public NotFoundException(String errorDesc) {
        super(errorDesc);
        this.errorDesc = errorDesc;
    }

    public NotFoundException(String errorCode, String errorDesc) {
        super(errorDesc);
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDesc() {
        return errorDesc;
    }
}
