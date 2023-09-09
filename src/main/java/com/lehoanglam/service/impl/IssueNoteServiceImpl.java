package com.yes4all.service.impl;

import com.yes4all.common.constants.ErrorConstant;
import com.yes4all.common.constants.ExcelIssueHeaderConstant;
import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtil;
import com.yes4all.common.utils.ExcelServiceUtil;
import com.yes4all.common.utils.JasperReportUtil;
import com.yes4all.config.Constants;
import com.yes4all.domain.*;
import com.yes4all.domain.enumeration.*;
import com.yes4all.repository.*;
import com.yes4all.service.InventoryLockService;
import com.yes4all.service.InventoryLogService;
import com.yes4all.service.IssueNoteService;
import com.yes4all.service.ReceiptNoteService;
import com.yes4all.service.WarehouseAdjustmentService;
import com.yes4all.service.dto.request.*;
import com.yes4all.service.dto.response.*;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link IssueNote}.
 */
@Service
@Transactional
public class IssueNoteServiceImpl implements IssueNoteService {

    private final Logger log = LoggerFactory.getLogger(IssueNoteServiceImpl.class);

    @Autowired
    IssueNoteRepository issueNoteRepository;

    @Autowired
    IssueItemRepository issueItemRepository;

    @Autowired
    InventoryLockService inventoryLockService;

    @Autowired
    WarehouseRepository warehouseRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    InventoryLogService<IssueNote> inventoryLogService;

    @Autowired
    ReceiptNoteService receiptNoteService;

    @Autowired
    WarehouseAdjustmentService warehouseAdjustmentService;

    public Page<IssueNoteListResponseDTO> listIssueNote(
        String searchBy,
        String searchValue,
        Long warehouseId,
        String fromDateStr,
        String toDateStr,
        boolean flgAll,
        String codes,
        Pageable pageable
    ) {
        try {
            String issueCodeLike = "";
            List<String> listIssueCodeIn = new ArrayList<>();
            List<String> searchList = new ArrayList<>();
            List<Integer> listId = new ArrayList<>();
            Instant fromDate = null;
            Instant toDate = null;

            if (
                CommonDataUtil.isNotEmpty(searchBy) &&
                (
                    (CommonDataUtil.isNotEmpty(searchValue)) ||
                    (CommonDataUtil.isNotEmpty(fromDateStr) && CommonDataUtil.isNotEmpty(toDateStr))
                )
            ) {
                searchValue = searchValue.replace(" ", "").toUpperCase();
                switch (searchBy) {
                    case "voucherCode":
                        listIssueCodeIn = Arrays.asList(searchValue.split(","));
                        if (listIssueCodeIn.size() == 1) {
                            searchBy = "issueCodeLike";
                            issueCodeLike = listIssueCodeIn.get(0);
                        } else {
                            searchBy = "issueCodeIn";
                        }
                        break;
                    case "issueType":
                    case "channel":
                    case "status":
                        searchList = Arrays.asList(searchValue.split(","));
                        break;
                    case "sku":
                        List<String> listSKU = Arrays.asList(searchValue.split(","));
                        if (listSKU.size() == 1) {
                            listId = issueNoteRepository.getListIdIssueNoteBySkuLike(listSKU.get(0));
                        } else {
                            listId = issueNoteRepository.getListIdIssueNoteByListSku(listSKU);
                        }
                        break;
                    case "issueDate":
                    case "createdDate":
                        fromDate = DateUtil.convertDateToDateTimeStart(fromDateStr);
                        toDate = DateUtil.convertDateToDateTimeEnd(toDateStr);
                        break;
                    default:
                        break;
                }
            } else {
                searchBy = "other";
            }

            // List issueCode selected when export excel
            // Convert codes to list issueCode
            List<String> listIssueCode = new ArrayList<String>();
            if (CommonDataUtil.isNotEmpty(codes)) {
                listIssueCode = Arrays.asList(codes.split(","));
            }

            // Get data
            Page<IssueNote> data = issueNoteRepository.findByCondition(
                issueCodeLike,
                listIssueCodeIn,
                searchList,
                listId,
                searchBy,
                warehouseId,
                fromDate,
                toDate,
                flgAll,
                listIssueCode,
                pageable
            );

            // Convert data to DTO
            return data.map(item -> mappingEntityToDTO(item, IssueNoteListResponseDTO.class));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public Page<IssueNoteListResponseDTO> filterCompletedIssueNote(CompletedNoteRequestDTO requestParamDTO) {
        Pageable pageable = PageRequest.of(requestParamDTO.getPage(), requestParamDTO.getSize());
        Page<IssueNote> data = issueNoteRepository.filterCompletedIssueNote(
            requestParamDTO.getSku(),
            DateUtil.convertDateToDateTimeStart(requestParamDTO.getFromDate()),
            DateUtil.convertDateToDateTimeEnd(requestParamDTO.getToDate()),
            requestParamDTO.getWarehouseId(),
            pageable
        );
        return data.map(item -> mappingEntityToDTO(item, IssueNoteListResponseDTO.class));
    }

    public byte[] listIssueNoteExport(
        String fileName,
        String searchBy,
        String searchValue,
        Long warehouseId,
        String fromDateStr,
        String toDateStr,
        boolean flgAll,
        String codes,
        List<String> listColumn,
        Pageable pageable
    ) {
        try {
            byte[] result = null;
            if (CommonDataUtil.isNotEmpty(listColumn)) {
                Map<String, String> headerMap = new LinkedHashMap<>();

                for (String column : listColumn) {
                    switch (column) {
                        case ExcelIssueHeaderConstant.ISSUE_HEADER_WAREHOUSE:
                            headerMap.put(
                                "warehouseNameFrom",
                                CommonDataUtil.toPascalCase(ExcelIssueHeaderConstant.ISSUE_HEADER_WAREHOUSE)
                            );
                            break;
                        case ExcelIssueHeaderConstant.ISSUE_HEADER_VOUCHER_CODE:
                            headerMap.put("issueCode", CommonDataUtil.toPascalCase(ExcelIssueHeaderConstant.ISSUE_HEADER_VOUCHER_CODE));
                            break;
                        case ExcelIssueHeaderConstant.ISSUE_HEADER_STATUS:
                            headerMap.put("status", CommonDataUtil.toPascalCase(ExcelIssueHeaderConstant.ISSUE_HEADER_STATUS));
                            break;
                        case ExcelIssueHeaderConstant.ISSUE_HEADER_CHANNELS:
                            headerMap.put("channel", CommonDataUtil.toPascalCase(ExcelIssueHeaderConstant.ISSUE_HEADER_CHANNELS));
                            break;
                        case ExcelIssueHeaderConstant.ISSUE_HEADER_EXPORTED_QTY:
                            headerMap.put(
                                "totalActualExportedQty",
                                CommonDataUtil.toPascalCase(ExcelIssueHeaderConstant.ISSUE_HEADER_EXPORTED_QTY)
                            );
                            break;
                        case ExcelIssueHeaderConstant.ISSUE_HEADER_ADJUSTMENT_CODE:
                            headerMap.put(
                                "adjustmentCode",
                                CommonDataUtil.toPascalCase(ExcelIssueHeaderConstant.ISSUE_HEADER_ADJUSTMENT_CODE)
                            );
                            break;
                        case ExcelIssueHeaderConstant.ISSUE_HEADER_INBOUND_CODE:
                            headerMap.put("receiptCode", CommonDataUtil.toPascalCase(ExcelIssueHeaderConstant.ISSUE_HEADER_INBOUND_CODE));
                            break;
                        case ExcelIssueHeaderConstant.ISSUE_HEADER_TOTAL_CONFIRMED_QTY:
                            headerMap.put(
                                "totalConfirmedQty",
                                CommonDataUtil.toPascalCase(ExcelIssueHeaderConstant.ISSUE_HEADER_TOTAL_CONFIRMED_QTY)
                            );
                            break;
                        case ExcelIssueHeaderConstant.ISSUE_HEADER_TOTAL_REMAINING:
                            headerMap.put(
                                "totalRemainingQty",
                                CommonDataUtil.toPascalCase(ExcelIssueHeaderConstant.ISSUE_HEADER_TOTAL_REMAINING)
                            );
                            break;
                        case ExcelIssueHeaderConstant.ISSUE_HEADER_ISSUE_TYPE:
                            headerMap.put("issueType", CommonDataUtil.toPascalCase(ExcelIssueHeaderConstant.ISSUE_HEADER_ISSUE_TYPE));
                            break;
                        case ExcelIssueHeaderConstant.ISSUE_HEADER_DATE_CREATED:
                            headerMap.put("createdDate", ExcelIssueHeaderConstant.ISSUE_HEADER_DATE_CREATED_PASCAL_CASE);
                            break;
                        case ExcelIssueHeaderConstant.ISSUE_HEADER_DATE_ISSUE:
                            headerMap.put("issueDate", ExcelIssueHeaderConstant.ISSUE_HEADER_DATE_ISSUE_PASCAL_CASE);
                            break;
                        case ExcelIssueHeaderConstant.ISSUE_HEADER_CREATED_BY:
                            headerMap.put("createdBy", CommonDataUtil.toPascalCase(ExcelIssueHeaderConstant.ISSUE_HEADER_CREATED_BY));
                            break;
                        case ExcelIssueHeaderConstant.ISSUE_HEADER_MANUAL_CREATION:
                            headerMap.put(
                                "isManualCreate",
                                CommonDataUtil.toPascalCase(ExcelIssueHeaderConstant.ISSUE_HEADER_MANUAL_CREATION)
                            );
                            break;
                        default:
                            break;
                    }
                }
                List<IssueNoteListResponseDTO> listData = listIssueNote(
                    searchBy,
                    searchValue,
                    warehouseId,
                    fromDateStr,
                    toDateStr,
                    flgAll,
                    codes,
                    pageable
                )
                    .getContent();
                result = ExcelServiceUtil.generateExcelFile(fileName, listData, headerMap);
            }
            if (CommonDataUtil.isNull(result)) {
                throw new BusinessException("Could not created file");
            }
            return result;
        } catch (Exception e) {
            log.error("Could not created excel file. Message: {}", e.getMessage());
            throw new BusinessException("Could not created excel file");
        }
    }

    public IssueNoteInfoResponseDTO getIssueNoteByCode(String issueCode, Long currentWarehouseId) {
        // Get data by issueCode
        IssueNote issueNote = issueNoteRepository
            .findByIssueCode(issueCode)
            .orElseThrow(() ->
                new BusinessException(ErrorConstant.NOT_FOUND_RESPONSE_CODE, String.format("Outbound '%s' does not exists!", issueCode))
            );

        if (!issueNote.getIsActive()) {
            throw new BusinessException(ErrorConstant.NOT_FOUND_RESPONSE_CODE, String.format("Outbound '%s' was deleted.", issueCode));
        }
        if (CommonDataUtil.isNotNull(currentWarehouseId)) {
            Warehouse currentWarehouse = warehouseRepository
                .findById(currentWarehouseId)
                .orElseThrow(() -> new BusinessException("Current warehouse not found!"));
            Warehouse outboundWarehouse = issueNote.getWarehouseFrom();
            if (!currentWarehouse.equals(outboundWarehouse)) {
                throw new BusinessException(
                    ErrorConstant.NOT_FOUND_RESPONSE_CODE,
                    String.format(
                        "The Outbound %s does not belong to %s. This Outbound belongs to %s.",
                        issueCode,
                        currentWarehouse.getWarehouseName(),
                        outboundWarehouse.getWarehouseName()
                    )
                );
            }
        }

        // Convert entity to DTO
        IssueNoteInfoResponseDTO result = mappingEntityToDTO(issueNote, IssueNoteInfoResponseDTO.class);

        // Get list detail
        if (CommonDataUtil.isNotNull(result)) {
            result.setDetails(getListDetail(issueNote.getId()));
        }
        return result;
    }

    private Set<IssueNoteInfoDetailResponseDTO> getListDetail(Long id) {
        Set<IssueNoteInfoDetailResponseDTO> listDetail = new LinkedHashSet<>();
        if (CommonDataUtil.isNotNull(id)) {
            int i = 0;
            Set<IssueItem> listItem = issueItemRepository.findByIssueNoteId(id);
            for (IssueItem item : listItem) {
                IssueNoteInfoDetailResponseDTO itemDTO = new IssueNoteInfoDetailResponseDTO();
                BeanUtils.copyProperties(item, itemDTO);
                itemDTO.setNo(++i);
                itemDTO.setSku(CommonDataUtil.toEmpty(item.getProduct().getSku()));
                itemDTO.setAsin(CommonDataUtil.toEmpty(item.getProduct().getAsin()));
                itemDTO.setProductTitle(CommonDataUtil.toEmpty(item.getProduct().getProductTitle()));
                itemDTO.setNote(CommonDataUtil.toEmpty(item.getNote()));
                itemDTO.setProductId(CommonDataUtil.isNull(item.getProduct()) ? null : item.getProduct().getId());
                listDetail.add(itemDTO);
            }
        }
        return listDetail;
    }

    public boolean approveIssueNote(IssueNoteDTO issueNoteDTO) {
        try {
            Optional<IssueNote> optIssueNote = issueNoteRepository.findByIssueCode(issueNoteDTO.getIssueCode());
            if (optIssueNote.isPresent()) {
                IssueNote issueNote = optIssueNote.get();
                if (!IssueNoteStatus.CONFIRMED.name().equals(issueNote.getStatus().name())) {
                    throw new BusinessException("The Outbound status must be: CONFIRMED");
                }
                Long warehouseId = issueNote.getWarehouseFrom().getId();

                // User and time update
                issueNote.setModifiedBy(issueNoteDTO.getUserName());

                if (IssueType.INTERNAL_TRANSFER.name().equals(issueNote.getIssueType().name())) {
                    // Update status IssueNote: CONFIRMED -> APPROVED
                    issueNote.setStatus(IssueNoteStatus.APPROVED);
                    issueNoteRepository.saveAndFlush(issueNote);

                    // Set list item
                    issueNote.setIssueItems(issueItemRepository.findByIssueNoteId(issueNote.getId()));

                    // remove lock inventory
                    inventoryLockService.removeListLock(issueNote);

                    // Create Receipt Note
                    ReceiptNoteResponseDTO receiptNoteDTO = generateReceiptNote(issueNoteDTO.getUserName(), issueNote);
                    issueNote.receiptCode(receiptNoteDTO.getReceiptCode());
                    issueNoteRepository.saveAndFlush(issueNote);
                    // Update Actual Exported Qty
                    updateActualExportedQty(issueNote, receiptNoteDTO);
                    // Do export quantity
                    doActualExportIssueNote(issueNote);
                } else {
                    // Update status IssueNote: CONFIRMED -> COMPLETED
                    issueNote.setStatus(IssueNoteStatus.COMPLETED);
                    issueNoteRepository.saveAndFlush(issueNote);

                    // Set list item
                    issueNote.setIssueItems(issueItemRepository.findByIssueNoteId(issueNote.getId()));

                    // Insert log
                    inventoryLogService.doInsertLog(issueNote, warehouseId);
                }
                return true;
            } else {
                throw new BusinessException("Can't find the Outbound");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    public boolean completeIssueNote(IssueNoteDTO issueNoteDTO, ReceiptNote receiptNote) {
        try {
            String issueCode = issueNoteDTO.getIssueCode();
            Optional<IssueNote> optIssueNote = issueNoteRepository.findByIssueCode(issueNoteDTO.getIssueCode());
            if (optIssueNote.isPresent()) {
                IssueNote issueNote = optIssueNote.get();

                // User update
                issueNote.setModifiedBy(issueNoteDTO.getUserName());
                // Update status IssueNote: APPROVED -> COMPLETED
                issueNote.setStatus(IssueNoteStatus.COMPLETED);

                // TODO: Change due to new Transfer flow
                // Quantity
                /*int totalActualImportedQty = CommonDataUtil.isNull(receiptNote.getTotalActualImportedQty())
                    ? 0
                    : receiptNote.getTotalActualImportedQty();
                int totalConfirmedQty = CommonDataUtil.isNull(issueNote.getTotalConfirmedQty()) ? 0 : issueNote.getTotalConfirmedQty();
                issueNote.setTotalActualExportedQty(totalActualImportedQty);
                issueNote.setTotalRemainingQty(totalConfirmedQty - totalActualImportedQty);

                // Map item receipt
                Map<Long, ReceiptItem> mapReceiptItem = receiptNote
                    .getReceiptItems()
                    .stream()
                    .collect(Collectors.toMap(item -> item.getProduct().getId(), Function.identity()));

                // List item issue
                Set<IssueItem> listIssueItem = issueItemRepository
                    .findByIssueNoteId(issueNote.getId())
                    .stream()
                    .map(issueItem -> {
                        ReceiptItem receiptItem = mapReceiptItem.get(issueItem.getProduct().getId());
                        int actualImportedQty = CommonDataUtil.isNull(receiptItem.getActualImportedQty())
                            ? 0
                            : receiptItem.getActualImportedQty();
                        int confirmedQty = CommonDataUtil.isNull(issueItem.getConfirmedQty()) ? 0 : issueItem.getConfirmedQty();
                        issueItem.actualExportedQty(actualImportedQty);
                        issueItem.setRemainingQty(confirmedQty - actualImportedQty);
                        return issueItem;
                    })
                    .collect(Collectors.toSet());
                // Set list item
                issueNote.setIssueItems(listIssueItem);*/

                // Save IssueNote
                issueNoteRepository.saveAndFlush(issueNote);

                if (!IssueType.INTERNAL_TRANSFER.equals(issueNote.getIssueType())) {
                    doActualExportIssueNote(issueNote);
                }
                return true;
            } else {
                throw new BusinessException(String.format("Can't find the Outbound with code: '%s'", issueCode));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    public boolean deleteIssueNote(IssueNoteDTO issueNoteDTO) {
        try {
            String issueCode = issueNoteDTO.getIssueCode();
            Optional<IssueNote> optIssueNote = issueNoteRepository.findByIssueCode(issueCode);
            if (optIssueNote.isPresent()) {
                IssueNote issueNote = optIssueNote.get();
                String status = issueNote.getStatus().name();
                if (!IssueNoteStatus.NEW.name().equals(status)) {
                    throw new BusinessException(
                        String.format("The Outbound '%s' are in %s status and are not allowed to be deleted.", issueCode, status)
                    );
                }

                // User update
                issueNote.setModifiedBy(issueNoteDTO.getUserName());
                // IssueNote inActive
                issueNote.setIsActive(false);
                issueNoteRepository.saveAndFlush(issueNote);
                inventoryLockService.removeListLock(issueNote);

                return true;
            } else {
                throw new BusinessException(String.format("Can't find the Outbound '%s'", issueCode));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    public byte[] exportIssueNotePdf(String issueCode) {
        try {
            byte[] result = null;
            IssueNoteInfoResponseDTO data = getIssueNoteByCode(issueCode, null);
            if (CommonDataUtil.isNotNull(data)) {
                // Path template file
                String fileName = "template/issue_note_info.jrxml";

                // Set parameter
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("issueCode", data.getIssueCode());
                parameters.put("warehouseNameFrom", data.getWarehouseNameFrom());
                parameters.put("warehouseNameTo", data.getWarehouseNameTo());
                parameters.put("issueToName", data.getIssueToName());
                parameters.put("issueToAddress", data.getIssueToAddress());
                parameters.put("issueToPhone", data.getIssueToPhone());
                parameters.put("receiptCode", data.getReceiptCode());
                parameters.put("issueType", data.getIssueType());
                parameters.put("adjustmentCode", CommonDataUtil.isNotNull(data.getAdjustmentCode()) ? data.getAdjustmentCode() : "-");
                parameters.put("channel", data.getChannel());
                parameters.put("totalConfirmedQtyStr", data.getTotalConfirmedQtyStr());
                parameters.put("totalActualExportedQtyStr", data.getTotalActualExportedQtyStr());
                parameters.put("totalRemainingQtyStr", data.getTotalRemainingQtyStr());
                parameters.put("createdBy", data.getCreatedBy());
                parameters.put("createdDate", data.getCreatedDate());
                parameters.put("issueDate", CommonDataUtil.isNotNull(data.getIssueDate()) ? data.getIssueDate() : "-");
                parameters.put("status", data.getStatus());
                parameters.put("department", CommonDataUtil.isNotEmpty(data.getDepartment()) ? data.getDepartment() : "-");
                parameters.put("note", data.getGeneralNote());

                // Fill data and export
                result = JasperReportUtil.export(fileName, parameters, data.getDetails());
            }
            if (CommonDataUtil.isNull(result)) {
                throw new BusinessException("Could not created file");
            }

            return result;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    public IssueNoteInfoResponseDTO saveOrUpdateIssueNote(IssueNoteInfoRequestDTO issueNoteInfoReqDTO) {
        try {
            // Validate transfer same warehouse
            boolean isInternalTransfer = IssueType.INTERNAL_TRANSFER.getValue().equals(issueNoteInfoReqDTO.getIssueType());
            boolean isNewNote = CommonDataUtil.isEmpty(issueNoteInfoReqDTO.getIssueCode());
            if (isInternalTransfer) {
                if (issueNoteInfoReqDTO.getWarehouseIdFrom().equals(issueNoteInfoReqDTO.getWarehouseIdTo())) {
                    throw new BusinessException("This Outbound is transfer from and to  the same warehouse. Please check again!");
                }
            }
            // validate lock quantity
            if (issueNoteInfoReqDTO.getIsManualCreate()) {
                inventoryLockService.doValidateRemainingQuantity(issueNoteInfoReqDTO, issueNoteInfoReqDTO.getWarehouseIdFrom(), isNewNote);
            }

            IssueNote issueNote;
            if (isNewNote) {
                // Convert DTO to entity
                issueNote = convertDtoToEntity("insert", null, issueNoteInfoReqDTO);

                // Save entity
                issueNote = issueNoteRepository.saveAndFlush(issueNote);
                if (IssueType.DECREASE_ADJUSTMENT.equals(issueNote.getIssueType())) {
                    issueNote.setAdjustment(issueNoteInfoReqDTO.getAdjustmentDto());
                    issueNoteRepository.saveAndFlush(issueNote);
                }

                if (IssueNoteStatus.COMPLETED.name().equals(issueNote.getStatus().name())) {
                    // Save log
                    inventoryLogService.doInsertLog(issueNote, issueNote.getWarehouseFrom().getId());
                }
            } else {
                // Get IssueNote by issueCode
                Optional<IssueNote> optIssueNote = issueNoteRepository.findByIssueCode(issueNoteInfoReqDTO.getIssueCode());
                if (optIssueNote.isPresent()) {
                    if (!optIssueNote.get().getIsActive()) {
                        throw new BusinessException("The Outbound was deleted");
                    }
                    if (!IssueNoteStatus.NEW.name().equals(optIssueNote.get().getStatus().name())) {
                        throw new BusinessException("The Outbound has been created.\nPlease refresh the page to see the updates.");
                    }
                } else {
                    throw new BusinessException("Can't find the Outbound");
                }

                // Convert DTO to entity
                issueNote = convertDtoToEntity("update", optIssueNote.get(), issueNoteInfoReqDTO);

                // Get list detail before
                List<Long> listIdCurrent = issueNoteInfoReqDTO
                    .getDetails()
                    .stream()
                    .map(IssueNoteInfoDetailRequestDTO::getId)
                    .collect(Collectors.toList());
                List<Long> listIdRemove = issueItemRepository
                    .findByIssueNoteId(issueNote.getId())
                    .stream()
                    .map(IssueItem::getId)
                    .collect(Collectors.toList());
                listIdRemove.removeAll(listIdCurrent);
                // Delete item
                issueItemRepository.deleteAllById(listIdRemove);

                // Update entity
                issueNote = issueNoteRepository.saveAndFlush(issueNote);
            }
            // insert lock
            if (!isNewNote & isInternalTransfer) {
                inventoryLockService.removeListLock(issueNote);
                inventoryLockService.doInsertLock(issueNote, issueNote.getWarehouseFrom().getId());
            }

            // Convert entity to DTO
            IssueNoteInfoResponseDTO result = mappingEntityToDTO(issueNote, IssueNoteInfoResponseDTO.class);
            if (CommonDataUtil.isNotNull(result)) {
                result.setDetails(getListDetail(issueNote.getId()));
            }
            return result;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    public void updateActualExportedQty(IssueNote issueNote, ReceiptNoteResponseDTO receiptNoteDTO) {
        // Quantity
        int totalActualImportedQty = CommonDataUtil.isNull(receiptNoteDTO.getTotalActualImportedQty())
            ? 0
            : receiptNoteDTO.getTotalActualImportedQty();
        int totalConfirmedQty = CommonDataUtil.isNull(issueNote.getTotalConfirmedQty()) ? 0 : issueNote.getTotalConfirmedQty();
        issueNote.setTotalActualExportedQty(totalActualImportedQty);
        issueNote.setTotalRemainingQty(totalConfirmedQty - totalActualImportedQty);

        // Map item receipt
        Map<Long, ReceiptNoteDetailResponseDTO> mapReceiptItem = receiptNoteDTO
            .getReceiptItemDto()
            .stream()
            .collect(Collectors.toMap(ReceiptNoteDetailResponseDTO::getProductId, Function.identity()));

        Set<IssueItem> issueItems = issueNote
            .getIssueItems()
            .parallelStream()
            .peek(issueItem -> {
                ReceiptNoteDetailResponseDTO receiptItem = mapReceiptItem.get(issueItem.getProduct().getId());
                int actualImportedQty = CommonDataUtil.isNull(receiptItem.getActualImportedQty()) ? 0 : receiptItem.getActualImportedQty();
                int confirmedQty = CommonDataUtil.isNull(issueItem.getConfirmedQty()) ? 0 : issueItem.getConfirmedQty();
                issueItem.actualExportedQty(actualImportedQty);
                issueItem.setRemainingQty(confirmedQty - actualImportedQty);
            })
            .collect(Collectors.toSet());
        issueNote.setIssueItems(issueItems);
        // Save IssueNote
        issueNoteRepository.saveAndFlush(issueNote);
    }

    private ReceiptNoteResponseDTO generateReceiptNote(String userName, IssueNote issueNote) {
        // Receipt DTO
        ReceiptNoteDTO requestDTO = ReceiptNoteDTO
            .builder()
            .isConfirmed(false)
            .department(CommonDataUtil.isNotNull(issueNote.getDepartment()) ? issueNote.getDepartment().name() : "")
            .isManualCreate(issueNote.getIsManualCreate())
            .receiptType(ReceiptType.INTERNAL_TRANSFER)
            .warehouse(issueNote.getWarehouseTo().getId())
            .issueCode(issueNote.getIssueCode())
            .totalConfirmedQty(issueNote.getTotalConfirmedQty())
            .totalActualImportedQty(issueNote.getTotalConfirmedQty())
            .totalDifferenceQty(0)
            .createdBy(userName)
            .generalNote(issueNote.getGeneralNote())
            .build();

        // List item DTO
        Set<ReceiptItemDTO> listItem = new HashSet<>();
        for (IssueItem itemIssueNote : issueNote.getIssueItems()) {
            ReceiptItemDTO itemDTO = ReceiptItemDTO
                .builder()
                .productId(itemIssueNote.getProduct().getId())
                .confirmedQty(itemIssueNote.getConfirmedQty())
                .actualImportedQty(itemIssueNote.getConfirmedQty())
                .differenceQty(0)
                .note(itemIssueNote.getNote())
                .build();
            listItem.add(itemDTO);
        }
        requestDTO.setDetails(listItem);

        return receiptNoteService.saveReceiptNote(requestDTO);
    }

    private IssueNote convertDtoToEntity(String mode, IssueNote issueNoteBefore, IssueNoteInfoRequestDTO dto) {
        // Copy data DTO to entity
        IssueNote issueNote = new IssueNote();
        BeanUtils.copyProperties(dto, issueNote);

        // TODO: Update created date in every actions - Change later
        Instant createdDate = DateUtil.currentInstantUTC();
        if (CommonDataUtil.isNotEmpty(dto.getCreatedDate())) {
            createdDate = DateUtil.convertStringToInstant(dto.getCreatedDate(), DateUtil.SIMPLE_DATE_FORMAT_AM_PM_MARKER);
        }
        issueNote.setCreatedDate(createdDate);
        issueNote.setCreatedBy(dto.getCreatedBy());

        if ("insert".equals(mode)) {
            // Issue code
            Long nextValue = issueNoteRepository.getNextIssueNoteValue();
            if (CommonDataUtil.isNull(nextValue)) {
                throw new BusinessException("Can't get Outbound");
            }
            issueNote.setIssueCode(CommonDataUtil.getCodeByValue(Constants.ISSUE_NOTE_PREFIX, String.valueOf(nextValue)));

            // Status: IssueNote create manual -> NEW else COMPLETED
            if (dto.getIsManualCreate() != null && !dto.getIsManualCreate()) {
                issueNote.setStatus(IssueNoteStatus.COMPLETED);
            } else {
                // When UI click confirm status is CONFIRMED else NEW
                if (dto.getIsConfirmed()) {
                    issueNote.setStatus(IssueNoteStatus.CONFIRMED);
                } else {
                    issueNote.setStatus(IssueNoteStatus.NEW);
                }
            }
        } else if ("update".equals(mode)) {
            // Id
            issueNote.setId(issueNoteBefore.getId());

            // Issue code
            issueNote.setIssueCode(issueNoteBefore.getIssueCode());

            // Manual
            issueNote.setIsManualCreate(issueNoteBefore.getIsManualCreate());

            // User update
            issueNote.setModifiedBy(dto.getCreatedBy());
            // When UI click confirm status is CONFIRMED else NEW
            if (dto.getIsConfirmed()) {
                issueNote.setStatus(IssueNoteStatus.CONFIRMED);
            } else {
                issueNote.setStatus(IssueNoteStatus.NEW);
            }
        }

        // set or update department
        if (CommonDataUtil.isNotEmpty(dto.getDepartment())) {
            issueNote.setDepartment(Department.valueOf(dto.getDepartment()));
        }
        // set issueDate
        Instant issueDate = CommonDataUtil.isEmpty(dto.getIssueDate())
            ? issueNote.getCreatedDate()
            : DateUtil.convertStringToInstant(dto.getIssueDate(), DateUtil.SIMPLE_DATE_FORMAT_AM_PM_MARKER);
        issueNote.setIssueDate(issueDate);

        issueNote.setIsActive(true);
        // Get Warehouse from
        if (CommonDataUtil.isNotNull(dto.getWarehouseIdFrom())) {
            Optional<Warehouse> optWarehouseFrom = warehouseRepository.findById(dto.getWarehouseIdFrom());
            optWarehouseFrom.ifPresent(issueNote::setWarehouseFrom);
        }

        // Get Warehouse transfer if exist
        if (CommonDataUtil.isNotNull(dto.getWarehouseIdTo())) {
            Optional<Warehouse> optWarehouseTo = warehouseRepository.findById(dto.getWarehouseIdTo());
            optWarehouseTo.ifPresent(issueNote::setWarehouseTo);
        }

        // Get Warehouse from
        if (CommonDataUtil.isNotNull(dto.getWarehouseCodeFrom())) {
            Optional<Warehouse> optWarehouseFrom = warehouseRepository.findFirstByWarehouseCode(dto.getWarehouseCodeFrom());
            optWarehouseFrom.ifPresent(issueNote::setWarehouseFrom);
        }

        // Get Warehouse transfer if exist
        if (CommonDataUtil.isNotNull(dto.getWarehouseCodeTo())) {
            Optional<Warehouse> optWarehouseTo = warehouseRepository.findFirstByWarehouseCode(dto.getWarehouseCodeTo());
            optWarehouseTo.ifPresent(issueNote::setWarehouseTo);
        }

        // IssueType
        issueNote.setIssueType(IssueType.getEnumByKey(dto.getIssueType()));

        // Channel
        issueNote.setChannel(Channel.getEnumByKey(dto.getChannel()));

        // Total remaining
        if (CommonDataUtil.isNull(issueNote.getTotalRemainingQty())) {
            int totalConfirmedQty = CommonDataUtil.toZero(dto.getTotalConfirmedQty());
            int totalActualExportedQty = CommonDataUtil.toZero(dto.getTotalActualExportedQty());
            issueNote.totalRemainingQty(totalConfirmedQty - totalActualExportedQty);
        }

        // List detail
        issueNote.setIssueItems(convertDetailDtoToEntity(dto.getDetails()));

        // Validation
        validate(issueNote);

        return issueNote;
    }

    private void validate(IssueNote issueNote) {
        if (CommonDataUtil.isNotNull(issueNote)) {
            if (CommonDataUtil.isEmpty(issueNote.getIssueCode())) {
                throw new BusinessException("Outbound cannot be null");
            }

            if (CommonDataUtil.isNull(issueNote.getWarehouseFrom())) {
                throw new BusinessException("Warehouse cannot be null");
            }
        } else {
            throw new BusinessException("Outbound doesn't exists");
        }
    }

    private Set<IssueItem> convertDetailDtoToEntity(Set<IssueNoteInfoDetailRequestDTO> listDetail) {
        Set<IssueItem> listResult = new HashSet<>();
        if (CommonDataUtil.isNotEmpty(listDetail)) {
            listResult =
                listDetail
                    .stream()
                    .map(dto -> {
                        IssueItem item = new IssueItem();
                        BeanUtils.copyProperties(dto, item);
                        if (CommonDataUtil.isNotNull(dto.getProductId())) {
                            Optional<Product> optProduct = productRepository.findById(dto.getProductId());
                            optProduct.ifPresent(item::setProduct);
                        }
                        Optional<Product> optProductTmp = productRepository.findBySku(dto.getSku());
                        optProductTmp.ifPresent(item::setProduct);
                        if (CommonDataUtil.isNull(item.getRemainingQty())) {
                            int confirmedQty = CommonDataUtil.toZero(item.getConfirmedQty());
                            int actualExportedQty = CommonDataUtil.toZero(item.getActualExportedQty());
                            item.setRemainingQty(confirmedQty - actualExportedQty);
                        }
                        return item;
                    })
                    .collect(Collectors.toSet());
        }
        return listResult;
    }

    private <T> T mappingEntityToDTO(IssueNote entity, Class<T> clazz) {
        try {
            T dto = clazz.getDeclaredConstructor().newInstance();
            CommonDataUtil.getModelMapper().map(entity, dto);

            if (dto instanceof IssueNoteListResponseDTO || dto instanceof IssueNoteInfoResponseDTO) {
                String createdDate = CommonDataUtil.isNull(entity.getCreatedDate())
                    ? ""
                    : DateUtil.convertInstantToString(entity.getCreatedDate(), DateUtil.SIMPLE_DATE_FORMAT_AM_PM_MARKER);

                // Set warehouse name from
                String warehouseNameFrom = "";
                if (CommonDataUtil.isNotNull(entity.getWarehouseFrom())) {
                    warehouseNameFrom = CommonDataUtil.toEmpty(entity.getWarehouseFrom().getWarehouseName());
                    clazz.getMethod("setWarehouseIdFrom", Long.class).invoke(dto, entity.getWarehouseFrom().getId());
                }
                clazz.getMethod("setWarehouseNameFrom", String.class).invoke(dto, warehouseNameFrom);

                // Set warehouse name transfer
                String warehouseNameTo = "";
                if (CommonDataUtil.isNotNull(entity.getWarehouseTo())) {
                    warehouseNameTo = CommonDataUtil.toEmpty(entity.getWarehouseTo().getWarehouseName());
                    clazz.getMethod("setWarehouseIdTo", Long.class).invoke(dto, entity.getWarehouseTo().getId());
                }
                clazz.getMethod("setWarehouseNameTo", String.class).invoke(dto, warehouseNameTo);

                // Set create by
                clazz.getMethod("setCreatedBy", String.class).invoke(dto, CommonDataUtil.DEFAULT_SYSTEM_USERNAME);
                if (CommonDataUtil.isNotEmpty(entity.getCreatedBy())) {
                    Optional<User> optUser = userRepository.findByEmail(entity.getCreatedBy());
                    String username = optUser.map(CommonDataUtil::getUserFullName).orElse(CommonDataUtil.DEFAULT_SYSTEM_USERNAME);
                    clazz.getMethod("setCreatedBy", String.class).invoke(dto, username);
                }

                clazz
                    .getMethod("setIssueType", String.class)
                    .invoke(
                        dto,
                        CommonDataUtil.isNull(entity.getIssueType()) ? "" : IssueType.getValueByKey(entity.getIssueType().getKey())
                    );
                clazz
                    .getMethod("setChannel", String.class)
                    .invoke(dto, CommonDataUtil.isNull(entity.getChannel()) ? "" : Channel.getValueByKey(entity.getChannel().getKey()));
                clazz
                    .getMethod("setAdjustmentCode", String.class)
                    .invoke(dto, CommonDataUtil.isNull(entity.getAdjustment()) ? "" : entity.getAdjustment().getAdjustmentCode());
                clazz
                    .getMethod("setReceiptCode", String.class)
                    .invoke(dto, CommonDataUtil.isNull(entity.getReceiptCode()) ? "" : entity.getReceiptCode());
                clazz
                    .getMethod("setStatus", String.class)
                    .invoke(
                        dto,
                        CommonDataUtil.isNull(entity.getStatus()) ? "" : IssueNoteStatus.getValueByKey(entity.getStatus().getKey())
                    );
                clazz
                    .getMethod("setIssueDate", String.class)
                    .invoke(dto, DateUtil.convertInstantToString(entity.getIssueDate(), DateUtil.SIMPLE_DATE_FORMAT_AM_PM_MARKER));
                clazz.getMethod("setCreatedDate", String.class).invoke(dto, createdDate);
                clazz.getMethod("setGeneralNote", String.class).invoke(dto, CommonDataUtil.toEmpty(entity.getGeneralNote()));
                clazz.getMethod("setIssueToName", String.class).invoke(dto, CommonDataUtil.toEmpty(entity.getIssueToName()));
                clazz.getMethod("setIssueToAddress", String.class).invoke(dto, CommonDataUtil.toEmpty(entity.getIssueToAddress()));
                clazz.getMethod("setIssueToPhone", String.class).invoke(dto, CommonDataUtil.toEmpty(entity.getIssueToPhone()));
                clazz
                    .getMethod("setDepartment", String.class)
                    .invoke(dto, CommonDataUtil.isNull(entity.getDepartment()) ? "" : entity.getDepartment().toString());
                clazz
                    .getMethod("setAdjustmentCode", String.class)
                    .invoke(dto, CommonDataUtil.isNull(entity.getAdjustment()) ? "" : entity.getAdjustment().getAdjustmentCode());
            }
            return dto;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    public void resetSequence() {
        issueNoteRepository.resetSequence();
    }

    public List<String> getListCodeTransfer() {
        return issueNoteRepository.getListCodeTransfer(IssueType.INTERNAL_TRANSFER.getKey());
    }

    private WarehouseAdjustmentRequestDTO buildWarehouseAdjustmentRequest(IssueNote issueNote) {
        List<WarehouseAdjustmentRequestDTO.AdjustmentItemDTO> adjustmentItems = issueNote
            .getIssueItems()
            .parallelStream()
            .map(item ->
                WarehouseAdjustmentRequestDTO.AdjustmentItemDTO
                    .builder()
                    .sku(item.getProduct().getSku())
                    .quantity(item.getActualExportedQty().toString())
                    .build()
            )
            .collect(Collectors.toList());
        WarehouseAdjustmentRequestDTO adjustmentRequestDTO = WarehouseAdjustmentRequestDTO
            .builder()
            .type(Constants.ADJUSTMENT_DECREASE_TYPE)
            .reference(issueNote.getIssueCode())
            .warehouseCode(issueNote.getWarehouseFrom().getWarehouseCode())
            .items(adjustmentItems)
            .build();
        return adjustmentRequestDTO;
    }

    private void doActualExportIssueNote(IssueNote issueNote) {
        // Insert log
        inventoryLogService.doInsertLog(issueNote, issueNote.getWarehouseFrom().getId());
        // Adjust quantity in WMS
        warehouseAdjustmentService.doAdjustReserveQuantity(buildWarehouseAdjustmentRequest(issueNote));
    }
}
