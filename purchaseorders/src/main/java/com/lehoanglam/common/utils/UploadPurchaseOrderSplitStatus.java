package com.yes4all.common.utils;

public class UploadPurchaseOrderSplitStatus {

    private String fileName;
    private String status;
    private String message;





    public UploadPurchaseOrderSplitStatus(String fileName, String status, String message ) {
        this.fileName = fileName;
        this.status = status;
        this.message = message;

    }


    public void setStatus(String status) {
        this.status = status;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getStatus() {
        return status;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
