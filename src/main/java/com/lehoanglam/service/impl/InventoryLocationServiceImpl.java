package com.yes4all.service.impl;

import com.yes4all.common.constants.ExcelInventoryLocationHeaderConstant;
import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.ExcelServiceUtil;
import com.yes4all.domain.Warehouse;
import com.yes4all.repository.InventoryLocationRepository;
import com.yes4all.repository.ProductRepository;
import com.yes4all.repository.WarehouseRepository;
import com.yes4all.service.InventoryLocationService;
import com.yes4all.service.dto.request.InventoryLocationExportRequestDTO;
import com.yes4all.service.dto.request.InventoryLocationParamDTO;
import com.yes4all.service.dto.response.InventoryLocationResponseDTO;
import com.yes4all.service.dto.response.WMSInventoryLocationResponseDTO;
import com.yes4all.service.mapper.InventoryLocationMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class InventoryLocationServiceImpl implements InventoryLocationService {

    private final Logger log = LoggerFactory.getLogger(InventoryLocationService.class);

    @Autowired
    InventoryLocationRepository inventoryLocationRepository;

    @Autowired
    InventoryLocationMapper inventoryLocationMapper;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    WarehouseRepository warehouseRepository;

    @Autowired
    GetAllInventoryLocationService wmsService;

    @Override
    public Page<InventoryLocationResponseDTO> getListInventoryLocationByCondition(InventoryLocationParamDTO filterParam) {
        List<String> listSku = CommonDataUtil.convertStringToListTrimAndUpper(filterParam.getSku(), ",");
        String warehouseCode = filterParam.getWarehouseCode();
        Warehouse warehouse = warehouseRepository
            .findFirstByWarehouseCode(warehouseCode)
            .orElseThrow(() -> new BusinessException(String.format("Warehouse with code '%s' not found!", warehouseCode)));

        WMSInventoryLocationResponseDTO result = wmsService.getInventoryLocation(listSku, filterParam);
        Pageable pageable = PageRequest.of(filterParam.getPage(), filterParam.getSize());
        List<InventoryLocationResponseDTO> dtoList = inventoryLocationMapper.mapWMSInventoryLocation(
            result.getDataList(),
            warehouse.getWarehouseName()
        );
        int totalRows = CommonDataUtil.getIntValueOrDefault(result.getTotalRows());
        return new PageImpl<>(dtoList, pageable, totalRows);
    }

    @Override
    public byte[] exportListInventoryLocationToExcelFile(String fileName, InventoryLocationExportRequestDTO requestDTO) {
        log.debug("REST request: /receipt-notes/list/export --- Export list Inventory Location");

        // Create list detail data
        byte[] result = null;
        try {
            if (CommonDataUtil.isNotEmpty(requestDTO.getListColumn())) {
                Map<String, String> headerMap = new LinkedHashMap<>();

                for (String column : requestDTO.getListColumn()) {
                    switch (column.toUpperCase()) {
                        case ExcelInventoryLocationHeaderConstant.INVENTORY_LOCATION_HEADER_WAREHOUSE:
                            headerMap.put(
                                "warehouse",
                                CommonDataUtil.toPascalCase(ExcelInventoryLocationHeaderConstant.INVENTORY_LOCATION_HEADER_WAREHOUSE)
                            );
                            break;
                        case ExcelInventoryLocationHeaderConstant.INVENTORY_LOCATION_HEADER_OVS_QUANTITY:
                            headerMap.put(
                                "ovsQuantity",
                                CommonDataUtil.toPascalCase(ExcelInventoryLocationHeaderConstant.INVENTORY_LOCATION_HEADER_OVS_QUANTITY)
                            );
                            break;
                        case ExcelInventoryLocationHeaderConstant.INVENTORY_LOCATION_HEADER_PKU_LOCATION:
                            headerMap.put(
                                "pkuLocation",
                                CommonDataUtil.toPascalCase(ExcelInventoryLocationHeaderConstant.INVENTORY_LOCATION_HEADER_PKU_LOCATION)
                            );
                            break;
                        case ExcelInventoryLocationHeaderConstant.INVENTORY_LOCATION_HEADER_SKU:
                            headerMap.put(
                                "sku",
                                CommonDataUtil.toPascalCase(ExcelInventoryLocationHeaderConstant.INVENTORY_LOCATION_HEADER_SKU)
                            );
                            break;
                        case ExcelInventoryLocationHeaderConstant.INVENTORY_LOCATION_HEADER_PICKUP_ROW:
                            headerMap.put(
                                "pickupRow",
                                CommonDataUtil.toPascalCase(ExcelInventoryLocationHeaderConstant.INVENTORY_LOCATION_HEADER_PICKUP_ROW)
                            );
                            break;
                        case ExcelInventoryLocationHeaderConstant.INVENTORY_LOCATION_HEADER_PKU_QUANTITY:
                            headerMap.put(
                                "pkuQuantity",
                                CommonDataUtil.toPascalCase(ExcelInventoryLocationHeaderConstant.INVENTORY_LOCATION_HEADER_PKU_QUANTITY)
                            );
                            break;
                        case ExcelInventoryLocationHeaderConstant.INVENTORY_LOCATION_HEADER_PRODUCT_TITLE:
                            headerMap.put(
                                "productTitle",
                                CommonDataUtil.toPascalCase(ExcelInventoryLocationHeaderConstant.INVENTORY_LOCATION_HEADER_PRODUCT_TITLE)
                            );
                            break;
                        case ExcelInventoryLocationHeaderConstant.INVENTORY_LOCATION_HEADER_TOTAL:
                            headerMap.put(
                                "total",
                                CommonDataUtil.toPascalCase(ExcelInventoryLocationHeaderConstant.INVENTORY_LOCATION_HEADER_TOTAL)
                            );
                            break;
                        case ExcelInventoryLocationHeaderConstant.INVENTORY_LOCATION_HEADER_WIP_QUANTITY:
                            headerMap.put("wipQuantity", ExcelInventoryLocationHeaderConstant.INVENTORY_LOCATION_HEADER_WIP_QUANTITY);
                            break;
                        default:
                            break;
                    }
                }
                List<InventoryLocationResponseDTO> listData;
                if (requestDTO.isFlgAll()) {
                    listData =
                        getListInventoryLocationByCondition(
                            new InventoryLocationParamDTO(
                                requestDTO.getSku(),
                                0,
                                Integer.MAX_VALUE,
                                requestDTO.getWarehouseCode(),
                                requestDTO.getProductTitle(),
                                requestDTO.getPickupRow(),
                                requestDTO.getPickupLocation()
                            )
                        )
                            .toList();
                } else {
                    List<String> inventoryCode = CommonDataUtil.convertStringToListTrim(requestDTO.getCodes(), ",");
                    List<Long> listId = inventoryCode.stream().map(Long::valueOf).collect(Collectors.toList());
                    listData =
                        inventoryLocationMapper
                            .mapListEntityToDto(inventoryLocationRepository.findAllByIdIn(listId, PageRequest.of(0, Integer.MAX_VALUE)))
                            .toList();
                }
                result = ExcelServiceUtil.generateExcelFile(fileName, listData, headerMap);
            }
        } catch (Exception e) {
            log.error("Could not created excel file. Message: {}", e.getMessage());
            throw new BusinessException("Could not created excel file");
        }
        return result;
    }
}
