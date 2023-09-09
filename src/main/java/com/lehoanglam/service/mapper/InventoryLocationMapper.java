package com.yes4all.service.mapper;

import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.domain.InventoryLocation;
import com.yes4all.domain.Warehouse;
import com.yes4all.repository.WarehouseRepository;
import com.yes4all.service.dto.response.InventoryLocationResponseDTO;
import com.yes4all.service.dto.response.WMSInventoryLocationItemDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class InventoryLocationMapper {

    @Autowired
    WarehouseRepository warehouseRepository;

    public InventoryLocationResponseDTO mapEntityToDto(InventoryLocation inventoryLocation) {
        Optional<Warehouse> oWarehouse = warehouseRepository.findFirstByWarehouseCode(inventoryLocation.getWarehouseCode());
        return InventoryLocationResponseDTO
            .builder()
            .total(inventoryLocation.getTotal())
            .id(inventoryLocation.getId())
            .ovsQuantity(inventoryLocation.getOvsQuantity())
            .pkuLocation(inventoryLocation.getPkuLocation())
            .sku(inventoryLocation.getSku())
            .productTitle(inventoryLocation.getProductTitle())
            .wipQuantity(inventoryLocation.getWipQuantity())
            .warehouse(oWarehouse.isPresent() ? oWarehouse.get().getWarehouseName() : "-")
            .pkuQuantity(inventoryLocation.getPkuQuantity())
            .build();
    }

    public Page<InventoryLocationResponseDTO> mapListEntityToDto(Page<InventoryLocation> inventoryLocations) {
        return inventoryLocations.map(this::mapEntityToDto);
    }

    public List<InventoryLocationResponseDTO> mapWMSInventoryLocation(List<WMSInventoryLocationItemDTO> wmsLocations, String warehouse) {
        List<InventoryLocationResponseDTO> result = new ArrayList<>();
        if (CommonDataUtil.isNotEmpty(wmsLocations)) {
            result =
                wmsLocations
                    .stream()
                    .map(item ->
                        InventoryLocationResponseDTO
                            .builder()
                            .total(CommonDataUtil.parseIntFromString(item.getTotal()))
                            .ovsQuantity(CommonDataUtil.parseIntFromString(item.getOvs()))
                            .wipQuantity(CommonDataUtil.parseIntFromString(item.getWip()))
                            .pkuQuantity(CommonDataUtil.parseIntFromString(item.getPku()))
                            .pkuLocation(item.getLocation())
                            .sku(item.getSku())
                            .productTitle(item.getTitle())
                            .pickupRow(item.getRow())
                            .warehouse(warehouse)
                            .build()
                    )
                    .collect(Collectors.toList());
        }
        return result;
    }
}
