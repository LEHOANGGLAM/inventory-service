package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListDetailPODTO {

    private String purchaserOrderNo;
    private Integer purchaserOrderId;

    private List<ProformaInvoiceDetailDTO> detail;
}
