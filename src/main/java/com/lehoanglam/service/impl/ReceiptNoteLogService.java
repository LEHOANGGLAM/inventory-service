package com.yes4all.service.impl;

import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtil;
import com.yes4all.domain.PeriodLog;
import com.yes4all.domain.ReceiptItem;
import com.yes4all.domain.ReceiptNote;
import com.yes4all.domain.enumeration.NoteType;
import com.yes4all.service.InventoryLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

@Service
public class ReceiptNoteLogService extends BaseInventoryLogService<ReceiptItem> implements InventoryLogService<ReceiptNote> {

    private static final Logger logger = LoggerFactory.getLogger(IssueNoteLogService.class);

    @Override
    public void doInsertLog(ReceiptNote receiptNote, Long warehouseId) {
        if (CommonDataUtil.isNotNull(receiptNote) && CommonDataUtil.isNotEmpty(receiptNote.getReceiptItems())) {
            StopWatch stopWatch = DateUtil.initStopWatch();
            logger.info("START insert log history");
            try {
                PeriodLog periodLog = getPeriodLogByDate(DateUtil.currentInstantUTC());
                receiptNote
                    .getReceiptItems()
                    .parallelStream()
                    .forEach(receiptItem ->
                        insertLog(
                            receiptItem,
                            receiptItem.getProduct(),
                            warehouseId,
                            receiptNote.getCreatedBy(),
                            receiptNote.getReceiptType().name(),
                            periodLog.getId()
                        )
                    );
            } catch (Exception ex) {
                logger.error(ex.getMessage());
                throw ex;
            }

            logger.info("END insert log history in {}", DateUtil.calculateTime(stopWatch));
        }
    }

    @Override
    protected Integer getUpdatedQuantity(ReceiptItem itemNote) {
        return itemNote.getActualImportedQty();
    }

    @Override
    protected String getNoteType() {
        return NoteType.RECEIPT.name();
    }

    @Override
    protected Long getReferenceId(ReceiptItem itemNote) {
        return itemNote.getReceiptNote().getId();
    }
}
