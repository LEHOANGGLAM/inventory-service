package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yes4all.domain.PurchaseOrdersSplitResult;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseOrderResultPageDTO extends PurchaseOrdersMainSplitDTO {
    @JsonProperty("details")
    private Page<PurchaseOrderSplitResultDTO> purchaseOrderSplitResultDTO ;

}
