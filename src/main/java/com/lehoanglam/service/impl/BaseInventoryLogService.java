package com.yes4all.service.impl;

import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtil;
import com.yes4all.domain.InventoryLog;
import com.yes4all.domain.InventoryQuery;
import com.yes4all.domain.PeriodLog;
import com.yes4all.domain.Product;
import com.yes4all.domain.enumeration.NoteType;
import com.yes4all.repository.InventoryLogRepository;
import com.yes4all.repository.InventoryQueryRepository;
import com.yes4all.repository.PeriodLogRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public abstract class BaseInventoryLogService<T> {

    private final Logger log = LogManager.getLogger(getClass());

    @Autowired
    private InventoryLogRepository inventoryLogRepository;

    @Autowired
    private PeriodLogRepository periodLogRepository;

    @Autowired
    private InventoryQueryRepository inventoryQueryRepository;

    protected final void insertLog(T itemNote, Product product, Long warehouseId, String userId, String note, Long periodId) {
        //        PeriodLog periodLog = getPeriodLogByDate(DateUtil.currentInstantUTC());

        Integer openQuantity = 0;
        Integer lastQuantity = 0;
        Integer lastReceiptQuantity = 0;
        Integer lastIssueQuantity = 0;
        Integer updatedQuantity = getUpdatedQuantity(itemNote);
        InventoryQuery currentInventoryQuery = new InventoryQuery();
        InventoryQuery previousInventoryQuery = inventoryQueryRepository.findFirstByProductIdAndWarehouseIdOrderByFullDateDesc(
            product.getId(),
            product.getSku(),
            warehouseId
        );
        if (CommonDataUtil.isNotNull(previousInventoryQuery)) {
            if (previousInventoryQuery.getPeriodId().equals(periodId)) {
                currentInventoryQuery.setId(previousInventoryQuery.getId());
                openQuantity = previousInventoryQuery.getOpenQty();
                lastQuantity = previousInventoryQuery.getCloseQty();
                lastReceiptQuantity = previousInventoryQuery.getReceiptQty();
                lastIssueQuantity = previousInventoryQuery.getIssueQty();
            } else {
                openQuantity = previousInventoryQuery.getCloseQty();
                lastQuantity = openQuantity;
            }
        }

        if (NoteType.RECEIPT.name().equals(getNoteType())) {
            currentInventoryQuery.setCloseQty(lastQuantity + updatedQuantity);
            currentInventoryQuery.setReceiptQty(lastReceiptQuantity + updatedQuantity);
            currentInventoryQuery.setIssueQty(lastIssueQuantity);
        } else if (NoteType.ISSUE.name().equals(getNoteType())) {
            currentInventoryQuery.setCloseQty(lastQuantity - updatedQuantity);
            currentInventoryQuery.setIssueQty(lastIssueQuantity + updatedQuantity);
            currentInventoryQuery.setReceiptQty(lastReceiptQuantity);
        }
        currentInventoryQuery.setOpenQty(openQuantity);
        currentInventoryQuery.setProductId(product.getId());
        currentInventoryQuery.setPeriodId(periodId);
        currentInventoryQuery.setWarehouseId(warehouseId);
        currentInventoryQuery.setSku(product.getSku());
        currentInventoryQuery = inventoryQueryRepository.saveAndFlush(currentInventoryQuery);

        InventoryLog inventoryLog = new InventoryLog();
        inventoryLog.setProductId(product.getId());
        inventoryLog.setSku(product.getSku());
        inventoryLog.setQuantityBefore(lastQuantity);
        inventoryLog.setQuantityAfter(currentInventoryQuery.getCloseQty());
        inventoryLog.setReferenceId(getReferenceId(itemNote));
        inventoryLog.setUserId(userId);
        inventoryLog.setWarehouseId(warehouseId);
        inventoryLog.setNote(note);
        inventoryLog.setType(getNoteType());
        inventoryLogRepository.saveAndFlush(inventoryLog);
        log.info(
            String.format("Inserted inventory log for %d with open qty: %d, close qty: %d", product.getId(), openQuantity, lastQuantity)
        );
    }

    public PeriodLog getPeriodLogByDate(Instant now) {
        LocalDateTime ldt = DateUtil.convertInstantToLocalDateTime(now);
        PeriodLog periodLog = periodLogRepository.findByDayAndMonthAndYear(ldt.getDayOfMonth(), ldt.getMonthValue(), ldt.getYear());
        if (CommonDataUtil.isNull(periodLog)) {
            periodLog = new PeriodLog();
            periodLog.setDay(ldt.getDayOfMonth());
            periodLog.setMonth(ldt.getMonthValue());
            periodLog.setYear(ldt.getYear());
            periodLog.setFullDate(DateUtil.getStartOfDay(now));
            periodLog = periodLogRepository.saveAndFlush(periodLog);
        }
        return periodLog;
    }

    protected abstract Integer getUpdatedQuantity(T itemNote);

    protected abstract String getNoteType();

    protected abstract Long getReferenceId(T itemNote);
}
