package com.yes4all.service.impl;

import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.domain.*;
import com.yes4all.repository.InventoryLockRepository;
import com.yes4all.repository.ProductRepository;
import com.yes4all.repository.WarehouseRepository;
import com.yes4all.service.InventoryLockService;
import com.yes4all.service.dto.ProductDTO;
import com.yes4all.service.dto.request.IssueNoteInfoDetailRequestDTO;
import com.yes4all.service.dto.request.IssueNoteInfoRequestDTO;
import com.yes4all.service.dto.request.WIPQuantityRequestDTO;
import com.yes4all.service.dto.response.WIPQuantityItemDTO;
import com.yes4all.service.dto.response.WIPQuantityResponseDTO;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
public class InventoryLockServiceImpl implements InventoryLockService {

    private static final Logger log = LoggerFactory.getLogger(InventoryLockServiceImpl.class);

    @Autowired
    InventoryLockRepository inventoryLockRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    WarehouseRepository warehouseRepository;

    @Autowired
    Environment env;

    @Override
    public void doValidateRemainingQuantity(IssueNoteInfoRequestDTO issueNoteDTO, Long warehouseId, boolean isNewNote) {
        if (CommonDataUtil.isNotNull(issueNoteDTO) && CommonDataUtil.isNotNull(issueNoteDTO.getDetails())) {
            Set<IssueNoteInfoDetailRequestDTO> details = issueNoteDTO.getDetails();
            Warehouse warehouse = warehouseRepository
                .findById(warehouseId)
                .orElseThrow(() -> new BusinessException("Current warehouse not found!"));
            List<Long> productIds = details.parallelStream().map(IssueNoteInfoDetailRequestDTO::getProductId).collect(Collectors.toList());
            Map<Long, String> products = getReferenceProducts(productIds)
                .stream()
                .collect(Collectors.toMap(ProductDTO::getId, ProductDTO::getSku));
            List<WIPQuantityItemDTO> wipQuantityList = new ArrayList<>();
            if (!isNewNote) {
                WIPQuantityRequestDTO wipRequest = WIPQuantityRequestDTO
                    .builder()
                    .warehouseCode(warehouse.getWarehouseCode())
                    .location("wip")
                    .items(new ArrayList<>(products.values()))
                    .build();
                wipQuantityList = doGetWIPQuantities(wipRequest);
            }

            List<String> messageRequireQuantity = new ArrayList<>();
            List<String> messageExceedRemaining = new ArrayList<>();
            List<String> messageExceedReserved = new ArrayList<>();
            for (IssueNoteInfoDetailRequestDTO detail : details) {
                String sku = products.get(detail.getProductId());
                Integer confirmedQuantity = detail.getConfirmedQty();

                if (confirmedQuantity < 1) {
                    String message = String.format("Please input quantity for SKU '%s'", sku);
                    messageRequireQuantity.add(message);
                    continue;
                }

                if (!isNewNote) {
                    WIPQuantityItemDTO wipQuantity = wipQuantityList
                        .stream()
                        .filter(item -> item.getSku().equals(sku))
                        .findFirst()
                        .orElse(null);
                    Integer remainingQuantity = wipQuantity != null ? CommonDataUtil.parseIntFromString(wipQuantity.getQuantity()) : 0;
                    Integer lockQuantity = inventoryLockRepository.getLockQtyByProductIdAndWarehouse(
                        detail.getProductId(),
                        warehouseId,
                        issueNoteDTO.getIssueCode()
                    );
                    lockQuantity = CommonDataUtil.getIntValueOrDefault(lockQuantity);

                    if (remainingQuantity < confirmedQuantity) {
                        String message = String.format("There are %d of SKU '%s' in WIP now.", remainingQuantity, sku);
                        messageExceedRemaining.add(message);
                        continue;
                    }
                    if (remainingQuantity - lockQuantity < confirmedQuantity) {
                        String message = String.format("There are %d of SKU '%s' was locking by other outbound.", lockQuantity, sku);
                        messageExceedReserved.add(message);
                    }
                }
            }

            if (!messageRequireQuantity.isEmpty()) {
                throw new BusinessException(String.join("\n", messageRequireQuantity));
            }
            if (!messageExceedRemaining.isEmpty()) {
                String errorMessage =
                    String.join("\n", messageExceedRemaining) +
                    "\n The reserved quantity is more " +
                    "than current WIP quantity. Please check again.";
                throw new BusinessException(errorMessage);
            }
            if (!messageExceedReserved.isEmpty()) {
                throw new BusinessException(String.join("\n", messageExceedReserved));
            }
        }
    }

    @Override
    public void doInsertLock(IssueNote issueNote, Long warehouseId) {
        if (CommonDataUtil.isNotNull(issueNote) && CommonDataUtil.isNotEmpty(issueNote.getIssueItems())) {
            for (IssueItem issueItem : issueNote.getIssueItems()) {
                InventoryLock inventoryLock = new InventoryLock();
                inventoryLock.setLock(issueItem.getConfirmedQty());
                inventoryLock.setWarehouseId(warehouseId);
                inventoryLock.setIssueCode(issueNote.getIssueCode());
                inventoryLock.setProductId(issueItem.getProduct().getId());
                inventoryLockRepository.saveAndFlush(inventoryLock);
            }
        }
    }

    @Override
    public void removeListLock(IssueNote issueNote) {
        if (CommonDataUtil.isNotNull(issueNote) && CommonDataUtil.isNotEmpty(issueNote.getIssueItems())) {
            inventoryLockRepository.deleteAllByIssueCode(issueNote.getIssueCode());
        }
    }

    @Override
    public void removeLockByProductIdAndWarehouseId(Long productId, Long warehouseId) {
        inventoryLockRepository.deleteByProductIdAndWarehouseId(productId, warehouseId);
    }

    private Product getReferenceProduct(Long productId) {
        return productRepository
            .findById(productId)
            .orElseThrow(() -> new BusinessException(String.format("Product with id '%d' was not found", productId)));
    }

    private List<ProductDTO> getReferenceProducts(List<Long> productIds) {
        return productRepository
            .findAllById(productIds)
            .parallelStream()
            .map(item -> {
                ProductDTO dto = new ProductDTO();
                dto.setId(item.getId());
                dto.setSku(item.getSku());
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<WIPQuantityItemDTO> doGetWIPQuantities(WIPQuantityRequestDTO request) {
        List<WIPQuantityItemDTO> items = new ArrayList<>();
        try {
            String requestHost = env.getProperty("ims.property.be3.host");
            String requestPath = env.getProperty("ims.property.be3.get-wip-qty.path");
            String requestUrl = requestHost + requestPath;
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<WIPQuantityRequestDTO> requestBody = new HttpEntity<>(request, headers);
            ResponseEntity<WIPQuantityResponseDTO> response = restTemplate.exchange(
                requestUrl,
                HttpMethod.POST,
                requestBody,
                WIPQuantityResponseDTO.class
            );

            Optional<WIPQuantityResponseDTO> oResponseBody = Optional.ofNullable(response.getBody());
            if (oResponseBody.isPresent()) {
                WIPQuantityResponseDTO responseBody = oResponseBody.get();
                if (responseBody.getSuccess()) {
                    items = responseBody.getMessage();
                }
            }
            return items;
        } catch (Exception ex) {
            log.error("Fail to sync imported/exported quantity to WMS. Message: {}", ex.getMessage());
            throw new BusinessException("Fail to check remaining WIP quantity!");
        }
    }
}
