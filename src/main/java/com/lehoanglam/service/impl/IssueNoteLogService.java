package com.yes4all.service.impl;

import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtil;
import com.yes4all.domain.IssueItem;
import com.yes4all.domain.IssueNote;
import com.yes4all.domain.PeriodLog;
import com.yes4all.domain.Product;
import com.yes4all.domain.enumeration.NoteType;
import com.yes4all.service.InventoryLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

@Service
public class IssueNoteLogService extends BaseInventoryLogService<IssueItem> implements InventoryLogService<IssueNote> {

    private static final Logger logger = LoggerFactory.getLogger(IssueNoteLogService.class);

    @Override
    public void doInsertLog(IssueNote issueNote, Long warehouseId) {
        if (CommonDataUtil.isNotNull(issueNote) && CommonDataUtil.isNotEmpty(issueNote.getIssueItems())) {
            StopWatch stopWatch = DateUtil.initStopWatch();
            PeriodLog periodLog = getPeriodLogByDate(DateUtil.currentInstantUTC());
            logger.info("START insert log history");
            try {
                issueNote
                    .getIssueItems()
                    .parallelStream()
                    .forEach(issueItem -> {
                        Product product = issueItem.getProduct();
                        insertLog(
                            issueItem,
                            product,
                            warehouseId,
                            issueNote.getCreatedBy(),
                            issueNote.getIssueType().name(),
                            periodLog.getId()
                        );
                    });
            } catch (Exception ex) {
                logger.error(ex.getMessage());
                throw ex;
            }
            logger.info("END insert log history in {}", DateUtil.calculateTime(stopWatch));
        }
    }

    @Override
    protected Integer getUpdatedQuantity(IssueItem itemNote) {
        return itemNote.getActualExportedQty();
    }

    @Override
    protected String getNoteType() {
        return NoteType.ISSUE.name();
    }

    @Override
    protected Long getReferenceId(IssueItem itemNote) {
        return itemNote.getIssueNote().getId();
    }
}
