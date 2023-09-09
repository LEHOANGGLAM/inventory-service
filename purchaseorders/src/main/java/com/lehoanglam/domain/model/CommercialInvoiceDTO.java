package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yes4all.domain.Resource;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommercialInvoiceDTO extends CommercialInvoiceMainDTO {
    @JsonProperty("details")
    private Set<CommercialInvoiceDetailDTO> commercialInvoiceDetail = new HashSet<>();


    @JsonProperty("resource")
    private List<ResourceDTO> fileUploads;


}
