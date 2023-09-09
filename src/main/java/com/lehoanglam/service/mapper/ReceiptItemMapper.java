package com.yes4all.service.mapper;

import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.domain.ReceiptItem;
import com.yes4all.service.dto.response.ReceiptNoteDetailResponseDTO;
import org.springframework.stereotype.Service;

@Service
public class ReceiptItemMapper {

    public ReceiptNoteDetailResponseDTO mapEntityToDto(ReceiptItem receiptItem) {
        return ReceiptNoteDetailResponseDTO
            .builder()
            .productId(receiptItem.getProduct().getId())
            .asin(receiptItem.getProduct().getAsin())
            .sku(receiptItem.getProduct().getSku())
            .id(receiptItem.getId())
            .actualImportedQty(receiptItem.getActualImportedQty())
            .note(CommonDataUtil.isNotNull(receiptItem.getNote()) ? receiptItem.getNote() : "")
            .confirmedQty(CommonDataUtil.isNotNull(receiptItem.getConfirmedQty()) ? receiptItem.getConfirmedQty() : 0)
            .differenceQty(CommonDataUtil.isNotNull(receiptItem.getDifferenceQty()) ? receiptItem.getDifferenceQty() : 0)
            .productTitle(receiptItem.getProduct().getProductTitle())
            .build();
    }
}
