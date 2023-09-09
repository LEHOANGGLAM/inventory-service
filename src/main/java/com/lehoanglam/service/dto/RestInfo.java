package com.yes4all.service.dto;

import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestInfo<T> {

    @NotBlank
    private String endpoint;

    @NotBlank
    private int timeOut;

    @NotBlank
    private Class<T> resType;
}
