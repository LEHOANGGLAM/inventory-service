package com.yes4all.service;

import com.yes4all.domain.Adjustment;
import com.yes4all.service.dto.request.AdjustmentDTO;
import com.yes4all.service.dto.request.AdjustmentExportRequestDTO;
import com.yes4all.service.dto.request.AdjustmentParamDTO;
import com.yes4all.service.dto.request.ReceiptExportRequestDTO;
import com.yes4all.service.dto.response.AdjustmentDetailResponseDTO;
import com.yes4all.service.dto.response.AdjustmentResponseDTO;
import java.io.IOException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface AdjustmentService {
    AdjustmentDetailResponseDTO createAdjustment(MultipartFile file, AdjustmentDTO adjustmentDTO) throws IOException;
    AdjustmentDetailResponseDTO getAdjustmentDetailByAdjustmentCode(String adjustmentCode, String warehouseCode);
    Page<AdjustmentResponseDTO> getListAdjustmentByCondition(AdjustmentParamDTO filter);
    byte[] exportAdjustmentExcelFile(String fileName, AdjustmentExportRequestDTO adjustmentExportRequestDTO);
    byte[] getExcelTemplate();
    boolean doValidateAdjustmentFileUpload(MultipartFile file, String warehouseCode) throws IOException;
    byte[] exportAdjustmentPdf(String adjustmentCode);
}
