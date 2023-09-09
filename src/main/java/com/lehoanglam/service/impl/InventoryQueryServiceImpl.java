package com.yes4all.service.impl;

import com.yes4all.common.constants.ExcelInventoryHeaderConstant;
import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtil;
import com.yes4all.common.utils.ExcelServiceUtil;
import com.yes4all.common.utils.PageRequestUtil;
import com.yes4all.repository.InventoryQueryRepository;
import com.yes4all.service.InventoryQueryService;
import com.yes4all.service.dto.request.InventoryQueryExportRequestDTO;
import com.yes4all.service.dto.request.InventoryQueryRequestDTO;
import com.yes4all.service.dto.response.InventoryQueryResponseDTO;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class InventoryQueryServiceImpl implements InventoryQueryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryQueryServiceImpl.class);

    @Autowired
    InventoryQueryRepository inventoryQueryRepository;


    @Override
    public Page<InventoryQueryResponseDTO> getListInventoryByCondition(InventoryQueryRequestDTO filterParam) {
        Pageable pageable = PageRequestUtil.genPageRequest(filterParam.getPage(), filterParam.getSize(), Sort.Direction.ASC, "sku");
        return findInventoryLogByCondition(filterParam, pageable);
    }

    @Override
    public byte[] exportListingExcel(String fileName, InventoryQueryExportRequestDTO params) {
        byte[] result = null;

        try {
            List<String> listColumn = params.getListColumn();
            if (CommonDataUtil.isNotEmpty(params.getListColumn())) {
                Map<String, String> headerMap = new LinkedHashMap<>();

                for (String column : listColumn) {
                    switch (column) {
                        case ExcelInventoryHeaderConstant.WAREHOUSE:
                            headerMap.put("warehouseName", CommonDataUtil.toPascalCase(ExcelInventoryHeaderConstant.WAREHOUSE));
                            break;
                        case ExcelInventoryHeaderConstant.SKU:
                            headerMap.put("sku", ExcelInventoryHeaderConstant.SKU);
                            break;
                        case ExcelInventoryHeaderConstant.PRODUCT_NAME:
                            headerMap.put("productName", CommonDataUtil.toPascalCase(ExcelInventoryHeaderConstant.PRODUCT_NAME));
                            break;
                        case ExcelInventoryHeaderConstant.OPENING_STOCK:
                            headerMap.put("openingStock", CommonDataUtil.toPascalCase(ExcelInventoryHeaderConstant.OPENING_STOCK));
                            break;
                        case ExcelInventoryHeaderConstant.CLOSING_STOCK:
                            headerMap.put("closingStock", CommonDataUtil.toPascalCase(ExcelInventoryHeaderConstant.CLOSING_STOCK));
                            break;
                        case ExcelInventoryHeaderConstant.IMPORTED_QUANTITY:
                            headerMap.put("importedQty", CommonDataUtil.toPascalCase(ExcelInventoryHeaderConstant.IMPORTED_QUANTITY));
                            break;
                        case ExcelInventoryHeaderConstant.EXPORTED_QUANTITY:
                            headerMap.put("exportedQty", CommonDataUtil.toPascalCase(ExcelInventoryHeaderConstant.EXPORTED_QUANTITY));
                            break;
                        default:
                            break;
                    }
                }

                Pageable pageable = PageRequestUtil.genPageRequest(0, Integer.MAX_VALUE, Sort.Direction.ASC, "sku");
                List<InventoryQueryResponseDTO> listData = findInventoryLogByCondition(params, pageable).getContent();
                result = ExcelServiceUtil.generateExcelFile(fileName, listData, headerMap);
            }
        } catch (Exception e) {
            logger.error("Could not export excel listing. Message: {}", e.getMessage());
            throw new BusinessException("Could not export excel listing");
        }
        return result;
    }

    private Page<InventoryQueryResponseDTO> findInventoryLogByCondition(InventoryQueryRequestDTO filterParam, Pageable pageable) {
        try {
            String skuParam = filterParam.getCodes();

            String searchString = CommonDataUtil.isNotNull(filterParam.getCodes())
                ? "%".concat(filterParam.getCodes().trim().toUpperCase()).concat("%")
                : "%%";
            List<String> skuList = CommonDataUtil.convertStringToListTrim(skuParam, ",");
            return inventoryQueryRepository
                .findListInventoryByCondition(
                    searchString,
                    skuList,
                    DateUtil.convertDateToDateTimeStart(filterParam.getFromDate()),
                    DateUtil.convertDateToDateTimeEnd(filterParam.getToDate()),
                    filterParam.getWarehouseId(),
                    pageable
                )
                .map(item -> {
                    InventoryQueryResponseDTO dto = new InventoryQueryResponseDTO();
                    CommonDataUtil.getModelMapper().map(item, dto);
                    return dto;
                });
        } catch (Exception ex) {
            logger.error("Fail to find inventory log. Message = {}", ex.getMessage());
            throw new BusinessException("Fail to filter inventory log for current condition");
        }
    }
}
