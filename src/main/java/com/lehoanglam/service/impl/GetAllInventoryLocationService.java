package com.yes4all.service.impl;

import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.service.dto.request.InventoryLocationParamDTO;
import com.yes4all.service.dto.request.WMSInventoryLocationFilterDTO;
import com.yes4all.service.dto.response.WMSInventoryLocationResponseDTO;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class GetAllInventoryLocationService extends ExternalRestService<WMSInventoryLocationFilterDTO, WMSInventoryLocationResponseDTO> {

    private static final Logger logger = LoggerFactory.getLogger(GetAllInventoryLocationService.class);

    @Autowired
    private Environment env;

    @Autowired
    private ExternalService externalService;

    public WMSInventoryLocationResponseDTO getInventoryLocation(List<String> skuList, InventoryLocationParamDTO params) {
        WMSInventoryLocationResponseDTO result = new WMSInventoryLocationResponseDTO();
        if (CommonDataUtil.isNotNull(params.getWarehouseCode())) {
            WMSInventoryLocationFilterDTO.WMSInventoryLocationFilterParamDTO filterParam = WMSInventoryLocationFilterDTO.WMSInventoryLocationFilterParamDTO
                .builder()
                .productSku(skuList)
                .productTitle(params.getProductTitle())
                .pickupRow(params.getPickupRow())
                .pickupLocation(params.getPickupLocation())
                .build();
            WMSInventoryLocationFilterDTO requestDto = WMSInventoryLocationFilterDTO
                .builder()
                .pageNum(params.getPage())
                .offset(params.getSize())
                .warehouseCode(params.getWarehouseCode())
                .filter(filterParam)
                .build();

            try {
                WMSInventoryLocationResponseDTO response = callREST(requestDto, RestService.POST_METHOD);
                if (CommonDataUtil.isNotNull(response) && response.getTotalRows() > 0) {
                    result = response;
                }
            } catch (Exception ex) {
                logger.error("Fail to get WMS Inventory location info. Message: {}", ex.getMessage());
                throw new BusinessException("Fail to get WMS Inventory location info!");
            }
        }
        return result;
    }

    @Override
    protected Class<WMSInventoryLocationResponseDTO> getRSClass() {
        return WMSInventoryLocationResponseDTO.class;
    }

    @Override
    protected String getEndPoint() {
        String requestHost = env.getProperty("ims.property.be3.host");
        String requestPath = env.getProperty("ims.property.be3.get-qty-by-location.path");
        return requestHost + requestPath;
    }

    @Override
    protected Integer getTimeOut() {
        return 60 * 1000;
    }

    @Override
    protected String toError(WMSInventoryLocationResponseDTO response) {
        return null;
    }
}
