package com.yes4all.service.mapper;

import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtil;
import com.yes4all.domain.*;
import com.yes4all.domain.enumeration.AdjustmentStatus;
import com.yes4all.domain.enumeration.Reason;
import com.yes4all.repository.UserRepository;
import com.yes4all.repository.WarehouseRepository;
import com.yes4all.service.dto.request.AdjustmentDTO;
import com.yes4all.service.dto.response.AdjustmentDetailResponseDTO;
import com.yes4all.service.dto.response.AdjustmentItemResponseDTO;
import com.yes4all.service.dto.response.AdjustmentResponseDTO;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class AdjustmentMapper {

    @Autowired
    AdjustmentItemMapper adjustmentItemMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    WarehouseRepository warehouseRepository;

    public AdjustmentDetailResponseDTO mapEntityToDetailDto(Adjustment adjustment) {
        AtomicInteger ordinal = new AtomicInteger(1);
        Optional<User> oUser = userRepository.findByEmail(adjustment.getCreatedBy());
        String createdUser = oUser.map(CommonDataUtil::getUserFullName).orElse(CommonDataUtil.DEFAULT_SYSTEM_USERNAME);
        Optional<Warehouse> oWarehouse = warehouseRepository.findFirstByWarehouseCode(adjustment.getWarehouseCode());
        return AdjustmentDetailResponseDTO
            .builder()
            .otherNote(adjustment.getNote())
            .createdBy(createdUser)
            .status(adjustment.getStatus().toString())
            .totalSku(adjustment.getTotalSku())
            .adjustmentCode(adjustment.getAdjustmentCode())
            .reason(adjustment.getReason().getValue())
            .createdDate(DateUtil.convertInstantToString(adjustment.getDateCreated(), DateUtil.SIMPLE_DATE_FORMAT_AM_PM_MARKER))
            .warehouse(oWarehouse.isPresent() ? oWarehouse.get().getWarehouseName() : "-")
            .details(
                adjustment
                    .getAdjustmentItems()
                    .stream()
                    .map(adjustmentItem -> {
                        AdjustmentItemResponseDTO adjustItem = adjustmentItemMapper.mapEntityToDto(adjustmentItem);
                        adjustItem.setNo(ordinal.getAndIncrement());
                        return adjustItem;
                    })
                    .collect(Collectors.toList())
            )
            .build();
    }

    public Page<AdjustmentResponseDTO> mapListEntityToDto(Page<Adjustment> adjustments) {
        return adjustments.map(this::mapEntityToDto);
    }

    public Adjustment mapDtoToEntity(AdjustmentDTO adjustmentDTO) {
        return Adjustment
            .builder()
            .note(adjustmentDTO.getGeneralNote())
            .reason(Reason.valueOf(adjustmentDTO.getReason()))
            .warehouseCode(adjustmentDTO.getWarehouseCode())
            .status(AdjustmentStatus.COMPLETED)
            .createdBy(adjustmentDTO.getCreatedBy())
            .build();
    }

    public AdjustmentResponseDTO mapEntityToDto(Adjustment adjustment) {
        Optional<User> oUser = userRepository.findByEmail(adjustment.getCreatedBy());
        String createdUser = oUser.map(CommonDataUtil::getUserFullName).orElse(CommonDataUtil.DEFAULT_SYSTEM_USERNAME);
        Optional<Warehouse> oWarehouse = warehouseRepository.findFirstByWarehouseCode(adjustment.getWarehouseCode());
        return AdjustmentResponseDTO
            .builder()
            .createdBy(createdUser)
            .adjustmentCode(adjustment.getAdjustmentCode())
            .dateCreated(DateUtil.convertInstantToString(adjustment.getDateCreated(), DateUtil.SIMPLE_DATE_FORMAT_AM_PM_MARKER))
            .status(adjustment.getStatus().toString())
            .totalSku(adjustment.getTotalSku())
            .otherNote(adjustment.getNote())
            .reason(adjustment.getReason().getValue())
            .warehouse(oWarehouse.isPresent() ? oWarehouse.get().getWarehouseName() : "-")
            .build();
    }
}
