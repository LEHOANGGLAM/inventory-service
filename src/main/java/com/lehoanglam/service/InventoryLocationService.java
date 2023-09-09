package com.yes4all.service;

import com.yes4all.service.dto.request.InventoryLocationExportRequestDTO;
import com.yes4all.service.dto.request.InventoryLocationParamDTO;
import com.yes4all.service.dto.response.InventoryLocationResponseDTO;
import org.springframework.data.domain.Page;

public interface InventoryLocationService {
    Page<InventoryLocationResponseDTO> getListInventoryLocationByCondition(InventoryLocationParamDTO filterParam);
    byte[] exportListInventoryLocationToExcelFile(String fileName, InventoryLocationExportRequestDTO requestDTO);
}
