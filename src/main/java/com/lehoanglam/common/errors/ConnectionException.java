package com.yes4all.common.errors;

public class ConnectionException extends RuntimeException {

    private static final long serialVersionUID = 3602587771075091036L;

    protected String errorCode;
    protected String errorDesc;

    public ConnectionException(String errorDesc) {
        super(errorDesc);
        this.errorDesc = errorDesc;
    }

    public ConnectionException(String errorCode, String errorDesc) {
        super(errorDesc);
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
    }
}
