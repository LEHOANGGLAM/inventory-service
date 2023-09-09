package com.yes4all.service.mapper;

import com.yes4all.common.constants.AdjustmentErrorConstants;
import com.yes4all.common.errors.AdjustmentErrorBuilder;
import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.errors.NotFoundException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.domain.Adjustment;
import com.yes4all.domain.AdjustmentItem;
import com.yes4all.domain.Product;
import com.yes4all.repository.ProductRepository;
import com.yes4all.service.dto.request.AdjustmentItemDTO;
import com.yes4all.service.dto.request.WarehouseAdjustmentItemDTO;
import com.yes4all.service.dto.response.AdjustmentItemResponseDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdjustmentItemMapper {

    @Autowired
    ProductRepository productRepository;

    public WarehouseAdjustmentItemDTO mapEntityToWarehouseAdjustmentItemDTO(AdjustmentItem adjustmentItem) {
        return WarehouseAdjustmentItemDTO
            .builder()
            .sku(adjustmentItem.getSku())
            .wip(adjustmentItem.getWipQuantityAfter())
            .pku(adjustmentItem.getPkuQuantityAfter())
            .build();
    }

    public AdjustmentItemResponseDTO mapEntityToDto(AdjustmentItem adjustmentItem) {
        return AdjustmentItemResponseDTO
            .builder()
            .sku(adjustmentItem.getSku())
            .productTitle(adjustmentItem.getProductTitle())
            .asin(adjustmentItem.getAsin())
            .inboundCode(
                CommonDataUtil.isNotNull(adjustmentItem.getInboundCode()) ? adjustmentItem.getInboundCode() : ""
            )
            .outboundCode(
                CommonDataUtil.isNotNull(adjustmentItem.getOutboundCode()) ? adjustmentItem.getOutboundCode() : ""
            )
            .pkuQuantityAfter(adjustmentItem.getPkuQuantityAfter())
            .pkuQuantityBefore(adjustmentItem.getPkuQuantityBefore())
            .wipQuantityBefore(adjustmentItem.getWipQuantityBefore())
            .wipQuantityAfter(adjustmentItem.getWipQuantityAfter())
            .totalQuantityAfter(adjustmentItem.getTotalQuantityAfter())
            .totalQuantityBefore(adjustmentItem.getTotalQuantityBefore())
            .build();
    }

    public List<AdjustmentItemDTO> trimData(List<AdjustmentItemDTO> adjustmentItemDTOList) {
            return adjustmentItemDTOList
            .stream()
            .peek(adjustmentItemDTO -> {
                String checkExternalValue = CommonDataUtil.isNotNull(adjustmentItemDTO.getUnknownCells())
                    ? adjustmentItemDTO.getUnknownCells().values().stream().findFirst().orElse("").trim()
                    : "";
                adjustmentItemDTO.setSku(
                    CommonDataUtil.isNotNull(adjustmentItemDTO.getSku()) ? adjustmentItemDTO.getSku().trim() : ""
                );
                adjustmentItemDTO.setPkuQuantity(
                    CommonDataUtil.isNotNull(adjustmentItemDTO.getPkuQuantity()) ? adjustmentItemDTO.getPkuQuantity().trim() : ""
                );
                adjustmentItemDTO.setWipQuantity(
                    CommonDataUtil.isNotNull(adjustmentItemDTO.getWipQuantity()) ? adjustmentItemDTO.getWipQuantity().trim() : ""
                );
                if (!checkExternalValue.trim().isBlank()) {
                    throw new BusinessException(AdjustmentErrorConstants.ADJUST_ERROR_FILE_INVALID_FORMAT);
                } else {
                    if (adjustmentItemDTO.getUnknownCells() != null){
                        adjustmentItemDTO.getUnknownCells().clear();
                    }
                }
            })
            .filter(
                adjustmentItemDTO -> !adjustmentItemDTO.getSku().isBlank() || !adjustmentItemDTO.getPkuQuantity().isBlank() || !adjustmentItemDTO.getWipQuantity().isBlank() || !adjustmentItemDTO.getUnknownCells().isEmpty()
            ).collect(Collectors.toList());
    }

    public List<AdjustmentItem> mapListDtoToEntity(List<AdjustmentItemDTO> adjustmentItemDTOList, Adjustment adjustment) {
        List<String> listSkuNotFound = new ArrayList<>();
        List<String> listSkuNotCorrectFormat = new ArrayList<>();
        List<AdjustmentItem> adjustmentItems = new ArrayList<>();
        adjustmentItemDTOList.forEach(adjustmentItemDTO -> {
            Optional<Product> oProduct = productRepository.findBySku(adjustmentItemDTO.getSku());
            int pkuQuantity, wipQuantity;
            if (
                adjustmentItemDTO.getSku() == null ||
                    adjustmentItemDTO.getPkuQuantity() == null ||
                    adjustmentItemDTO.getWipQuantity() == null
            ) {
                throw new BusinessException(AdjustmentErrorConstants.ADJUST_ERROR_FILE_INVALID_FORMAT);
            }
            if (
                CommonDataUtil.isPositiveInteger(adjustmentItemDTO.getPkuQuantity()) &&
                    CommonDataUtil.isPositiveInteger(adjustmentItemDTO.getWipQuantity())
            ) {
                pkuQuantity = Integer.parseInt(adjustmentItemDTO.getPkuQuantity());
                wipQuantity = Integer.parseInt(adjustmentItemDTO.getWipQuantity());
            } else {
                pkuQuantity = 0;
                wipQuantity = 0;
                listSkuNotCorrectFormat.add(adjustmentItemDTO.getSku());
            }
            if (oProduct.isPresent()) {
                Product product = oProduct.get();
                AdjustmentItem adjustmentItem = new AdjustmentItem();
                adjustmentItem.setAsin(product.getAsin());
                adjustmentItem.setSku(product.getSku());
                adjustmentItem.setProductTitle(product.getProductTitle());
                adjustmentItem.setPkuQuantityAfter(pkuQuantity);
                adjustmentItem.setWipQuantityAfter(wipQuantity);

                Integer totalQtyAfter = pkuQuantity + wipQuantity;
                adjustmentItem.setTotalQuantityAfter(totalQtyAfter);
                adjustmentItem.setAdjustment(adjustment);
                adjustmentItems.add(adjustmentItem);
            } else {
                listSkuNotFound.add(adjustmentItemDTO.getSku());
            }
        });
        if (!listSkuNotCorrectFormat.isEmpty()) {
//            String notCorrectFormatError = AdjustmentErrorBuilder.buildNotCorrectFormat(listSkuNotCorrectFormat);
            throw new BusinessException(AdjustmentErrorConstants.ADJUST_ERROR_FILE_INVALID_FORMAT);
        }
        if (!listSkuNotFound.isEmpty()) {
            String notFoundErrorStr = AdjustmentErrorBuilder.buildNotFound(listSkuNotFound);
            throw new NotFoundException(notFoundErrorStr);
        }
        return adjustmentItems;
    }
}
