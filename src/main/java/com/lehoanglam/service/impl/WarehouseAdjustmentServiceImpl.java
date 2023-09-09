package com.yes4all.service.impl;

import com.google.gson.Gson;
import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.config.Constants;
import com.yes4all.domain.Adjustment;
import com.yes4all.domain.WmsTransferLog;
import com.yes4all.repository.WmsTransferLogRepository;
import com.yes4all.service.WarehouseAdjustmentService;
import com.yes4all.service.dto.request.WarehouseAdjustmentItemDTO;
import com.yes4all.service.dto.request.WarehouseAdjustmentRequestDTO;
import com.yes4all.service.dto.request.WarehouseAdjustmentUpdateQtyDTO;
import com.yes4all.service.dto.response.WarehouseAdjustmentResponseDTO;
import com.yes4all.service.dto.response.WarehouseAdjustmentUpdatedResponseDTO;
import com.yes4all.service.mapper.AdjustmentItemMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class WarehouseAdjustmentServiceImpl implements WarehouseAdjustmentService {

    private static final Logger log = LoggerFactory.getLogger(WarehouseAdjustmentServiceImpl.class);

    @Autowired
    private Environment env;

    @Autowired
    WmsTransferLogRepository wmsTransferLogRepository;

    @Autowired
    AdjustmentItemMapper adjustmentItemMapper;

    @Override
    @Transactional(noRollbackFor = { BusinessException.class }, propagation = Propagation.REQUIRES_NEW)
    public boolean doAdjustReserveQuantity(WarehouseAdjustmentRequestDTO request) {
        boolean isSuccess = false;
        boolean isException = false;
        String defaultErrorMessage = "Fail to sync imported/exported quantity to WMS";
        String errorMessage = defaultErrorMessage;
        try {
            String requestHost = env.getProperty("ims.property.be3.host");
            String requestPath = env.getProperty("ims.property.be3.update-wh-qty.path");
            String requestUrl = requestHost + requestPath;
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<WarehouseAdjustmentRequestDTO> requestBody = new HttpEntity<>(request, headers);
            ResponseEntity<WarehouseAdjustmentResponseDTO> response = restTemplate.exchange(
                requestUrl,
                HttpMethod.POST,
                requestBody,
                WarehouseAdjustmentResponseDTO.class
            );

            Optional<WarehouseAdjustmentResponseDTO> oResponseBody = Optional.ofNullable(response.getBody());
            if (oResponseBody.isPresent()) {
                WarehouseAdjustmentResponseDTO responseBody = oResponseBody.get();
                if (responseBody.getSuccess()) {
                    log.info("Adjustment quantity success");
                    errorMessage = "";
                    isSuccess = true;
                } else {
                    List<String> messages = responseBody
                        .getMessage()
                        .parallelStream()
                        .filter(CommonDataUtil::isNotEmpty)
                        .collect(Collectors.toList());
                    if (CommonDataUtil.isNotEmpty(messages)) {
                        errorMessage = String.join("\n", messages);
                    }
                }
            }
            return isSuccess;
        } catch (Exception ex) {
            log.error("Fail to sync imported/exported quantity to WMS. Message: {}", ex.getMessage());
            errorMessage = ex.getMessage();
            isException = true;
            return false;
        } finally {
            doWriteWmsTransferLog(request, isSuccess, errorMessage);
            if (!isSuccess) {
                errorMessage = isException ? defaultErrorMessage : errorMessage;
                throw new BusinessException(errorMessage);
            }
        }
    }

    @Override
    @Transactional(noRollbackFor = { BusinessException.class }, propagation = Propagation.REQUIRES_NEW)
    public void doUpdateQtyWarehouseByAdjustment(Adjustment adjustment) {
        log.debug("------- START SYNC ADJUSTMENT QTY TO WMS ------");
        Gson gsonParser = new Gson();
        boolean isSuccess = false;
        boolean isException = false;
        String defaultErrorMessage = "Fail to sync Adjustment quantity to WMS";
        String errorMessage = defaultErrorMessage;
        try {
            String requestHost = env.getProperty("ims.property.be3.host");
            String requestPath = env.getProperty("ims.property.be3.update-qty-by-location.path");
            String requestUrl = requestHost + requestPath;
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Charset", "utf-8");
            headers.setContentType(MediaType.APPLICATION_JSON);

            List<WarehouseAdjustmentItemDTO> detail = adjustment
                .getAdjustmentItems()
                .parallelStream()
                .map(adjustmentItemMapper::mapEntityToWarehouseAdjustmentItemDTO)
                .collect(Collectors.toList());
            WarehouseAdjustmentUpdateQtyDTO request = WarehouseAdjustmentUpdateQtyDTO
                .builder()
                .note("New note")
                .adjType("moving_wh")
                .warehouseCode(adjustment.getWarehouseCode())
                .details(detail)
                .build();

            HttpEntity<WarehouseAdjustmentUpdateQtyDTO> requestBody = new HttpEntity<>(request, headers);
            ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.POST, requestBody, String.class);
            if (CommonDataUtil.isNotNull(response.getBody())) {
                WarehouseAdjustmentUpdatedResponseDTO responseBody = gsonParser.fromJson(
                    response.getBody(),
                    WarehouseAdjustmentUpdatedResponseDTO.class
                );
                if (responseBody.getDataResponse().isSuccess()) {
                    isSuccess = true;
                    errorMessage = "";
                    log.debug("------SYNC ADJUSTMENT QUANTITY TO WMS SUCCESSFULLY------");
                } else {
                    log.debug("------SYNC ADJUSTMENT QUANTITY TO WMS FAIL ------");
                    log.debug("Response: {}", response.getBody());
                }
            }
        } catch (Exception ex) {
            log.error("Fail to sync update quantity for Adjustment to WMS. Message: {}", ex.getMessage());
            errorMessage = ex.getMessage();
            isException = true;
        } finally {
            WarehouseAdjustmentRequestDTO request = WarehouseAdjustmentRequestDTO
                .builder()
                .warehouseCode(adjustment.getWarehouseCode())
                .reference(adjustment.getAdjustmentCode())
                .type(Constants.ADJUSTMENT_ADJUSTMENT_TYPE)
                .build();
            doWriteWmsTransferLog(request, isSuccess, errorMessage);
            if (!isSuccess) {
                errorMessage = isException ? errorMessage : defaultErrorMessage;
                throw new BusinessException(errorMessage);
            }
        }
    }

    private void doWriteWmsTransferLog(WarehouseAdjustmentRequestDTO request, boolean isSuccess, String errorMessage) {
        WmsTransferLog wmsTransferLog = new WmsTransferLog();
        wmsTransferLog.setType(request.getType());
        wmsTransferLog.setReferenceCode(request.getReference());
        wmsTransferLog.setIsSuccess(isSuccess);
        wmsTransferLog.setWarehouseCode(request.getWarehouseCode());
        wmsTransferLog.setErrorMessage(errorMessage);
        wmsTransferLogRepository.saveAndFlush(wmsTransferLog);
    }
}
