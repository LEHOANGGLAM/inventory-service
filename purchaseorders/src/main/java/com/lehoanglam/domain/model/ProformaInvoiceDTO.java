package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.yes4all.domain.ProformaInvoiceDetail;
import com.yes4all.domain.Resource;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProformaInvoiceDTO extends ProformaInvoiceMainDTO {
    @JsonProperty("details")
    private Set<ProformaInvoiceDetailDTO> proformaInvoiceDetail = new HashSet<>();

    @JsonProperty("resource")
    private List<ResourceDTO> fileUploads;



}
