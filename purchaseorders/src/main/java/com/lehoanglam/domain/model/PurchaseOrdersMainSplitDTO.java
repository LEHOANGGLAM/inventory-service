package com.yes4all.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseOrdersMainSplitDTO {
    private Integer id;
    private String rootFile;
    private String updatedBy;
    private Integer status;
    private Instant updatedDate;
}
