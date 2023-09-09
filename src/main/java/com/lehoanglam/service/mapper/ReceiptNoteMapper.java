package com.yes4all.service.mapper;

import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtil;
import com.yes4all.domain.ReceiptNote;
import com.yes4all.domain.User;
import com.yes4all.repository.UserRepository;
import com.yes4all.service.dto.response.ReceiptNoteDetailResponseDTO;
import com.yes4all.service.dto.response.ReceiptNoteListResponseDTO;
import com.yes4all.service.dto.response.ReceiptNoteResponseDTO;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class ReceiptNoteMapper {

    @Autowired
    ReceiptItemMapper receiptItemMapper;

    @Autowired
    UserRepository userRepository;

    public ReceiptNoteListResponseDTO mapEntityToDtoList(ReceiptNote receiptNote){
        Optional<User> oUser = userRepository.findByEmail(receiptNote.getCreatedBy());
        String createdUser = oUser.map(CommonDataUtil::getUserFullName).orElse(CommonDataUtil.DEFAULT_SYSTEM_USERNAME);
        return ReceiptNoteListResponseDTO
            .builder()
            .id(receiptNote.getId())
            .receiptStatus(CommonDataUtil.isNotNull(receiptNote.getStatus()) ? receiptNote.getStatus().name() : "")
            .receiptCode(receiptNote.getReceiptCode())
            .totalActualImportedQty(
                CommonDataUtil.isNotNull(receiptNote.getTotalActualImportedQty()) ? receiptNote.getTotalActualImportedQty() : 0
            )
            .department(
                CommonDataUtil.isNotNull(receiptNote.getDepartment()) ? receiptNote.getDepartment().name() : ""
            )
            .adjustmentCode(
                CommonDataUtil.isNotNull(receiptNote.getAdjustment()) ? receiptNote.getAdjustment().getAdjustmentCode() : ""
            )
            .totalDifferenceQty(CommonDataUtil.isNotNull(receiptNote.getTotalDifferenceQty()) ? receiptNote.getTotalDifferenceQty() : 0)
            .totalConfirmedQty(CommonDataUtil.isNotNull(receiptNote.getTotalConfirmedQty()) ? receiptNote.getTotalConfirmedQty() : 0)
            .createdDate(DateUtil.convertInstantToString(receiptNote.getCreatedDate(), DateUtil.SIMPLE_DATE_FORMAT_AM_PM_MARKER))
            .receiptDate(DateUtil.convertInstantToString(receiptNote.getReceiptDate(), DateUtil.SIMPLE_DATE_FORMAT_AM_PM_MARKER))
            .generalNote(CommonDataUtil.isNotNull(receiptNote.getGeneralNote()) ? receiptNote.getGeneralNote() : "")
            .receiptType(receiptNote.getReceiptType().getValue())
            .containerNo(receiptNote.getContainerNo())
            .shipmentNo(receiptNote.getShipmentNo())
            .isManualCreate(receiptNote.getIsManualCreate())
            .warehouse(receiptNote.getWarehouse().getWarehouseName())
            .warehouseId(receiptNote.getWarehouse().getId())
            .issueCode(receiptNote.getIssueCode())
            .createdBy(createdUser)
            .build();
    }

    public Page<ReceiptNoteListResponseDTO> mapListEntityToListDto(Page<ReceiptNote> receiptNotes){
        return receiptNotes.map(this::mapEntityToDtoList);
    }


    public ReceiptNoteResponseDTO mapEntityToDto(ReceiptNote receiptNote) {
        AtomicInteger ordinal = new AtomicInteger(1);
        Optional<User> oUser = userRepository.findByEmail(receiptNote.getCreatedBy());
        String createdUser = oUser.map(CommonDataUtil::getUserFullName).orElse(CommonDataUtil.DEFAULT_SYSTEM_USERNAME);
        return ReceiptNoteResponseDTO
            .builder()
            .id(receiptNote.getId())
            .receiptStatus(CommonDataUtil.isNotNull(receiptNote.getStatus()) ? receiptNote.getStatus().name() : "")
            .receiptCode(receiptNote.getReceiptCode())
            .totalActualImportedQty(
                CommonDataUtil.isNotNull(receiptNote.getTotalActualImportedQty()) ? receiptNote.getTotalActualImportedQty() : 0
            )
            .department(
                CommonDataUtil.isNotNull(receiptNote.getDepartment()) ? receiptNote.getDepartment().name() : ""
            )
            .adjustmentCode(
                CommonDataUtil.isNotNull(receiptNote.getAdjustment()) ? receiptNote.getAdjustment().getAdjustmentCode() : ""
            )
            .totalDifferenceQty(CommonDataUtil.isNotNull(receiptNote.getTotalDifferenceQty()) ? receiptNote.getTotalDifferenceQty() : 0)
            .totalConfirmedQty(CommonDataUtil.isNotNull(receiptNote.getTotalConfirmedQty()) ? receiptNote.getTotalConfirmedQty() : 0)
            .createdDate(DateUtil.convertInstantToString(receiptNote.getCreatedDate(), DateUtil.SIMPLE_DATE_FORMAT_AM_PM_MARKER))
            .receiptDate(DateUtil.convertInstantToString(receiptNote.getReceiptDate(), DateUtil.SIMPLE_DATE_FORMAT_AM_PM_MARKER))
            .generalNote(CommonDataUtil.isNotNull(receiptNote.getGeneralNote()) ? receiptNote.getGeneralNote() : "")
            .receiptType(receiptNote.getReceiptType().getValue())
            .containerNo(receiptNote.getContainerNo())
            .shipmentNo(receiptNote.getShipmentNo())
            .isManualCreate(receiptNote.getIsManualCreate())
            .warehouse(receiptNote.getWarehouse().getWarehouseName())
            .warehouseId(receiptNote.getWarehouse().getId())
            .issueCode(receiptNote.getIssueCode())
            .createdBy(createdUser)
            .receiptItemDto(
                receiptNote
                    .getReceiptItems()
                    .parallelStream()
                    .map(receiptItem -> {
                        ReceiptNoteDetailResponseDTO receiptItemDTO = receiptItemMapper.mapEntityToDto(receiptItem);
                        receiptItemDTO.setNo(ordinal.getAndIncrement());
                        return receiptItemDTO;
                    })
                    .sorted(Comparator.comparing(ReceiptNoteDetailResponseDTO::getNo))
                    .collect(Collectors.toCollection(LinkedHashSet::new))
            )
            .build();
    }

    public Page<ReceiptNoteResponseDTO> mapListEntityToDto(Page<ReceiptNote> receiptNotes) {
        return receiptNotes.map(this::mapEntityToDto);
    }
}
