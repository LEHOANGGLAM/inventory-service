package com.yes4all.service.dto;

import lombok.Data;

@Data
public class RestLoggingDTO {

    private String timestamp;
    private String level;
    private String detailed_message;
    private String functionName;
    private String refID;
    private String requestURL;
    private long responseTime;
}
