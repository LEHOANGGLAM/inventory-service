package com.yes4all.web.rest.payload;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestRequestHeader {

    @NotBlank
    private String appId;

    @NotBlank
    private String messageUid;

    @NotBlank
    private String messageDt;
}
