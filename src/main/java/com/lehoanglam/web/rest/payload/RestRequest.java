package com.yes4all.web.rest.payload;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestRequest<T> {

    @NotNull
    @Valid
    private RestRequestHeader header;

    @NotNull
    @Valid
    private T body;
}
