package com.yes4all.service;

import com.yes4all.domain.IssueNote;
import com.yes4all.service.dto.request.IssueNoteInfoRequestDTO;
import com.yes4all.service.dto.request.WIPQuantityRequestDTO;
import com.yes4all.service.dto.response.WIPQuantityItemDTO;
import java.util.List;

public interface InventoryLockService {
    void doValidateRemainingQuantity(IssueNoteInfoRequestDTO issueNoteDTO, Long warehouseId, boolean isNewNote);
    void doInsertLock(IssueNote issueNote, Long warehouseId);
    void removeListLock(IssueNote issueNote);
    void removeLockByProductIdAndWarehouseId(Long productId, Long warehouseId);
    List<WIPQuantityItemDTO> doGetWIPQuantities(WIPQuantityRequestDTO request);
}
