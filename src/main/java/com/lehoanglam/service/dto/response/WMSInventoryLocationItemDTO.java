package com.yes4all.service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WMSInventoryLocationItemDTO {

    @JsonProperty("product_sku")
    private String sku;

    private String title;
    private String location;
    private String row;
    private String total;
    private String ovs;
    private String wip;
    private String pku;
}
