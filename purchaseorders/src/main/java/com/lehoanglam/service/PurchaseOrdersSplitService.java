package com.yes4all.service;

import com.yes4all.domain.PurchaseOrders;
import com.yes4all.domain.PurchaseOrdersSplit;
import com.yes4all.domain.PurchaseOrdersSplitData;
import com.yes4all.domain.model.*;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Service Interface for managing {@link PurchaseOrders}.
 */
public interface PurchaseOrdersSplitService {
    List<PurchaseOrdersSplit>  createPurchaseOrdersSplit(List<ResultUploadDTO> data, String user);
    void export(String filename,Integer id) throws IOException;
    Page<PurchaseOrdersMainSplitDTO> getAll(Integer page, Integer limit);
    PurchaseOrdersSplit splitPurchaseOrder(Integer id);
    boolean removePurchaseOrdersSplit(List<Integer> listPurchaseOrderId, String userName);
    PurchaseOrderDataPageDTO  getPurchaseOrdersSplitData(Integer id, Integer page, Integer limit);
    PurchaseOrderResultPageDTO getPurchaseOrdersSplitResult(Integer id, Integer page, Integer limit);
    String getNameFile(Integer id);
    PurchaseOrderSplitResultDetailsDTO getPurchaseOrdersSplitResultDetail(Integer id, Integer page, Integer limit);
}
