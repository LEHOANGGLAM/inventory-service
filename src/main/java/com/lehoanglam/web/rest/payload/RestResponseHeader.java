package com.yes4all.web.rest.payload;

import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestResponseHeader {

    private String messageUid;
    private String messageDt;
    private String reqMessageUid;

    @NotBlank
    private String respCode;

    @NotBlank
    private String respDesc;
}
