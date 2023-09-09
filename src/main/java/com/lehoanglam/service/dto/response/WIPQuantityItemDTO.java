package com.yes4all.service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WIPQuantityItemDTO {

    @JsonProperty("product_sku")
    private String sku;

    private String quantity;
}
