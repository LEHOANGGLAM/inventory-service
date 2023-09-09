package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseOrderDetailPageDTO extends PurchaseOrdersMainDTO {
    @JsonProperty("details")
    private List<PurchaseOrderDetailDTO> purchaseOrdersDetail ;

}
