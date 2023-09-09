package com.yes4all.service.dto;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ErrorResponseDTO {

    private String message;
    private int status;
    private String description;
    private ZonedDateTime timestamp;
}
