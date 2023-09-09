package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yes4all.common.utils.UploadPurchaseOrder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListingDTO {
    private Integer page;
    private Integer size;
    private String searchBy;
    private String searchByValue;
    private Object fromValue;
    private Object toValue;
    private String supplier;
}
