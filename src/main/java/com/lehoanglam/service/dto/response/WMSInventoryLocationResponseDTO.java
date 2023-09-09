package com.yes4all.service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WMSInventoryLocationResponseDTO {

    Integer totalRows;
    List<WMSInventoryLocationItemDTO> dataList;
}
