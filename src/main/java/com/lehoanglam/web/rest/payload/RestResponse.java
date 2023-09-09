package com.yes4all.web.rest.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestResponse<T> {

    @NotNull
    @Valid
    private RestResponseHeader header;

    @Valid
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T body;
}
