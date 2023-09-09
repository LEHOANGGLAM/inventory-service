package com.yes4all.service;

import com.yes4all.domain.Adjustment;
import com.yes4all.service.dto.request.WarehouseAdjustmentRequestDTO;

public interface WarehouseAdjustmentService {
    boolean doAdjustReserveQuantity(WarehouseAdjustmentRequestDTO request);
    void doUpdateQtyWarehouseByAdjustment(Adjustment adjustment);
}
