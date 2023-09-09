package com.yes4all.service;

import com.yes4all.domain.PurchaseOrders;

import com.yes4all.domain.model.*;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service Interface for managing {@link PurchaseOrders}.
 */
public interface PurchaseOrdersService {

    Page<PurchaseOrdersMainDTO> listingPurchaseOrdersWithCondition(Integer page, Integer limit, Map<String, String> filterParams);

    boolean removePurchaseOrders(List<Integer> purchaseOrderDetailId, String userName);
    boolean confirmedPurchaseOrders(List<Integer> purchaseOrderDetailId,String userName);
    boolean sendPurchaseOrders(List<Integer> purchaseOrderDetailId,String userName);

    boolean cancelPurchaseOrders(List<Integer> purchaseOrderDetailId, String userName);

    void export(String filename,Integer id) throws IOException;

    boolean removeSkuFromDetails(Integer purchaseOrderId, List<Integer> purchaseOrderDetailId, String userName);
    PurchaseOrderDetailPageDTO getPurchaseOrdersDetailWithFilter(Integer id, String sku,String aSin,String productName,Integer page,Integer limit);
    ListDetailPODTO getListSkuFromPO(List<Integer> purchaseOrderDetailId);
    PurchaseOrdersMainDTO editPurchaseOrder(PurchaseOrdersMainDTO purchaseOrdersMainDTO,Integer id,String userName) ;
    PurchaseOrderDetailPageDTO getPurchaseOrdersDetailWithFilterOrderNo( String oderNo,Integer page,Integer limit);

    PurchaseOrderDetailPageDTO editSkuPurchaseOrder(Set<PurchaseOrderDetailDTO> PurchaseOrderDetailDTO, Integer id) ;

}
