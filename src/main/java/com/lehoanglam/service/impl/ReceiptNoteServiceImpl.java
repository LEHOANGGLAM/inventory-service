package com.yes4all.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.yes4all.common.constants.ErrorConstant;
import com.yes4all.common.constants.ExcelReceiptHeaderConstant;
import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.errors.BusinessLogicException;
import com.yes4all.common.errors.NotFoundException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtil;
import com.yes4all.common.utils.ExcelServiceUtil;
import com.yes4all.common.utils.JasperReportUtil;
import com.yes4all.config.Constants;
import com.yes4all.domain.*;
import com.yes4all.domain.enumeration.ReceiptFilterType;
import com.yes4all.domain.enumeration.ReceiptNoteStatus;
import com.yes4all.domain.enumeration.ReceiptType;
import com.yes4all.repository.*;
import com.yes4all.service.IssueNoteService;
import com.yes4all.service.ReceiptNoteService;
import com.yes4all.service.WarehouseAdjustmentService;
import com.yes4all.service.dto.request.*;
import com.yes4all.service.dto.response.ReceiptNoteDetailResponseDTO;
import com.yes4all.service.dto.response.ReceiptNoteListResponseDTO;
import com.yes4all.service.dto.response.ReceiptNoteResponseDTO;
import com.yes4all.service.mapper.ReceiptNoteMapper;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ReceiptNote}.
 */
@Service
@Transactional
public class ReceiptNoteServiceImpl implements ReceiptNoteService {

    private final Logger log = LoggerFactory.getLogger(ReceiptNoteServiceImpl.class);

    @Autowired
    ReceiptNoteRepository receiptNoteRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    IssueNoteService issueNoteService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    IssueNoteRepository issueNoteRepository;

    @Autowired
    WarehouseRepository warehouseRepository;

    @Autowired
    ReceiptItemRepository receiptItemRepository;

    @Autowired
    ReceiptNoteLogService receiptNoteLogService;

    @Autowired
    ReceiptNoteMapper receiptNoteMapper;

    @Autowired
    WarehouseAdjustmentService warehouseAdjustmentService;

    @Autowired
    MessageSource messageSource;

    @Override
    public ReceiptNoteResponseDTO saveReceiptNote(ReceiptNoteDTO receiptNoteDto) {
        log.debug("Request to save ReceiptNote : {}", receiptNoteDto);
        ReceiptNote receiptNote = modelMapper.map(receiptNoteDto, ReceiptNote.class);
        boolean isManualCreate = receiptNoteDto.getIsManualCreate();

        Warehouse warehouse;
        if (isManualCreate) {
            warehouse =
                warehouseRepository
                    .findById(receiptNoteDto.getWarehouse())
                    .orElseThrow(() -> new BusinessException(String.format("Warehouse '%s' does not exist!", receiptNoteDto.getWarehouse()))
                    );
            receiptNote.setStatus(receiptNoteDto.getIsConfirmed() ? ReceiptNoteStatus.CONFIRMED : ReceiptNoteStatus.NEW);
        } else {
            receiptNote.setStatus(ReceiptNoteStatus.COMPLETED);
            warehouse =
                warehouseRepository
                    .findFirstByWarehouseCode(receiptNoteDto.getWarehouseCode())
                    .orElseThrow(() ->
                        new BusinessException(String.format("Warehouse with code '%s' does not exist!", receiptNoteDto.getReceiptCode()))
                    );
            int totalConfirmedQty = receiptNoteDto.getDetails().stream().mapToInt(ReceiptItemDTO::getConfirmedQty).sum();
            int totalActualImportQty = receiptNoteDto.getDetails().stream().mapToInt(ReceiptItemDTO::getActualImportedQty).sum();
            int totalDiffQty = receiptNoteDto.getDetails().stream().mapToInt(ReceiptItemDTO::getDifferenceQty).sum();
            receiptNote.setTotalConfirmedQty(totalConfirmedQty);
            receiptNote.setTotalActualImportedQty(totalActualImportQty);
            receiptNote.setShortageQty(totalDiffQty);
        }

        if (ReceiptType.INTERNAL_TRANSFER.equals(receiptNoteDto.getReceiptType())) {
            String issueCode = receiptNoteDto.getIssueCode();
            IssueNote issueNote = issueNoteRepository
                .findByIssueCode(receiptNoteDto.getIssueCode())
                .orElseThrow(() -> new BusinessException(String.format("Outbound '%s' does not exists", issueCode)));
            if (issueNote.getTotalConfirmedQty() < receiptNoteDto.getTotalActualImportedQty()) {
                throw new BusinessLogicException("Total confirmed quantity must greater than total imported quantity!");
            }
        }
        receiptNote.setWarehouse(warehouse);
        Instant createdDate = DateUtil.currentInstantUTC();
        if (CommonDataUtil.isNotEmpty(receiptNoteDto.getCreatedDate())) {
            createdDate = DateUtil.convertStringToInstant(receiptNoteDto.getCreatedDate(), DateUtil.SIMPLE_DATE_FORMAT_AM_PM_MARKER);
        }

        Instant receiptDate = CommonDataUtil.isEmpty(receiptNoteDto.getReceiptDate())
            ? createdDate
            : DateUtil.convertStringToInstant(receiptNoteDto.getReceiptDate(), DateUtil.SIMPLE_DATE_FORMAT_AM_PM_MARKER);
        receiptNote.setReceiptDate(receiptDate);
        receiptNote.setCreatedDate(createdDate);
        receiptNote.setReceiptCode(
            CommonDataUtil.getCodeByValue(Constants.RECEIPT_PREFIX, receiptNoteRepository.getNextReceiptNoteValue().toString())
        );
        receiptNote.setIsManualCreate(receiptNoteDto.getIsManualCreate());
        receiptNote.setIsActive(true);

        receiptNote = receiptNoteRepository.saveAndFlush(receiptNote);
        if (ReceiptType.INCREASE_ADJUSTMENT.equals(receiptNote.getReceiptType())) {
            receiptNote.setAdjustment(receiptNoteDto.getAdjustmentDto());
            receiptNote = receiptNoteRepository.saveAndFlush(receiptNote);
        }

        for (ReceiptItemDTO receiptItemDto : receiptNoteDto.getDetails()) {
            Optional<Product> oProduct = isManualCreate
                ? productRepository.findById(receiptItemDto.getProductId())
                : productRepository.findBySku(receiptItemDto.getSku());

            ReceiptItem receiptItem = modelMapper.map(receiptItemDto, ReceiptItem.class);
            receiptItem.setDifferenceQty(receiptItem.getConfirmedQty() - receiptItem.getActualImportedQty());
            receiptItem.setReceiptNote(receiptNote);
            oProduct.ifPresent(receiptItem::setProduct);
            receiptNote.addReceiptItem(receiptItem);
            receiptItemRepository.save(receiptItem);
        }
        if (!isManualCreate) {
            receiptNoteLogService.doInsertLog(receiptNote, warehouse.getId());
        }
        return receiptNoteMapper.mapEntityToDto(receiptNote);
    }

    @Override
    public ReceiptNoteResponseDTO saveOrUpdateReceiptNote(ReceiptNoteDTO receiptNoteDTO, Boolean isConfirmed) {
        receiptNoteDTO.setIsConfirmed(isConfirmed);
        if (receiptNoteDTO.getReceiptCode() == null) {
            return saveReceiptNote(receiptNoteDTO);
        } else {
            return updateReceiptNote(receiptNoteDTO.getReceiptCode(), receiptNoteDTO);
        }
    }

    @Override
    public ReceiptNoteResponseDTO getReceiptNoteDetail(String receiptCode, Long currentWarehouseId) {
        ReceiptNote receiptNote = receiptNoteRepository
            .findByReceiptCode(receiptCode)
            .orElseThrow(() ->
                new BusinessException(ErrorConstant.NOT_FOUND_RESPONSE_CODE, String.format("Inbound '%s' does not exist!", receiptCode))
            );
        if (!receiptNote.getIsActive()) {
            throw new BusinessException(ErrorConstant.NOT_FOUND_RESPONSE_CODE, String.format("Inbound '%s' was deleted.", receiptCode));
        }
        if (CommonDataUtil.isNotNull(currentWarehouseId)) {
            Warehouse currentWarehouse = warehouseRepository
                .findById(currentWarehouseId)
                .orElseThrow(() -> new BusinessException("Current warehouse not found!"));
            Warehouse inboundWarehouse = receiptNote.getWarehouse();
            if (!currentWarehouse.equals(inboundWarehouse)) {
                throw new BusinessException(
                    ErrorConstant.NOT_FOUND_RESPONSE_CODE,
                    String.format(
                        "The Inbound %s does not belong to %s. This Inbound belongs to %s.",
                        receiptCode,
                        currentWarehouse.getWarehouseName(),
                        inboundWarehouse.getWarehouseName()
                    )
                );
            }
        }
        ReceiptNoteResponseDTO response = receiptNoteMapper.mapEntityToDto(receiptNote);
        if (ReceiptType.INTERNAL_TRANSFER.equals(receiptNote.getReceiptType())) {
            IssueNote referenceIssueNote = issueNoteRepository
                .findByIssueCode(receiptNote.getIssueCode())
                .orElseThrow(() ->
                    new BusinessException(
                        ErrorConstant.NOT_FOUND_RESPONSE_CODE,
                        String.format("Reference Outbound '%s' not found", receiptNote.getIssueCode())
                    )
                );
            response.setWarehouseFrom(referenceIssueNote.getWarehouseFrom().getWarehouseName());
        }
        return response;
    }

    @Override
    public Page<ReceiptNoteListResponseDTO> getListReceiptNote(ReceiptRequestParam filterParam) {
        Pageable pageable = PageRequest.of(filterParam.getPage(), filterParam.getSize(), Sort.Direction.DESC, "createdDate", "receiptCode");
        String searchString = "%".concat(filterParam.getSearchValue().trim().toUpperCase()).concat("%");
        List<String> listSearchValue = CommonDataUtil.convertStringToListTrimAndUpper(filterParam.getSearchValue().trim(), ",");
        ReceiptFilterType filterType = ReceiptFilterType.valueOfOrDefault(filterParam.getSearchBy().toUpperCase());
        Warehouse warehouse = warehouseRepository
            .findById(filterParam.getWarehouseId())
            .orElseThrow(() -> new BusinessException("Cannot find related warehouse for this Inbound."));

        Page<ReceiptNote> receiptNotes;
        switch (filterType) {
            case VOUCHER_CODE:
                receiptNotes = receiptNoteRepository.findAllByReceiptCodeAndWarehouse(searchString, listSearchValue, warehouse, pageable);
                break;
            case RECEIPT_TYPE:
                List<ReceiptType> receiptTypes = listSearchValue.isEmpty() || listSearchValue.contains("ALL")
                    ? Arrays.asList(ReceiptType.values())
                    : listSearchValue.stream().map(ReceiptType::valueOf).collect(Collectors.toList());
                receiptNotes = receiptNoteRepository.getAllByReceiptTypeLikeAndWarehouse(receiptTypes, warehouse, pageable);
                break;
            case STATUS:
                List<ReceiptNoteStatus> statuses = listSearchValue.isEmpty() || listSearchValue.contains("ALL")
                    ? Arrays.asList(ReceiptNoteStatus.values())
                    : listSearchValue.stream().map(ReceiptNoteStatus::valueOf).collect(Collectors.toList());
                receiptNotes = receiptNoteRepository.getAllByStatusAndWarehouse(statuses, warehouse, pageable);
                break;
            case RECEIPT_DATE:
                receiptNotes =
                    receiptNoteRepository.getAllByReceiptDateBetweenAndWarehouse(
                        DateUtil.convertDateToDateTimeStart(filterParam.getFromDate()),
                        DateUtil.convertDateToDateTimeEnd(filterParam.getToDate()),
                        warehouse,
                        pageable
                    );
                break;
            case CREATED_DATE:
                receiptNotes =
                    receiptNoteRepository.getAllByCreatedDateBetweenAndWarehouse(
                        DateUtil.convertDateToDateTimeStart(filterParam.getFromDate()),
                        DateUtil.convertDateToDateTimeEnd(filterParam.getToDate()),
                        warehouse,
                        pageable
                    );
                break;
            case SKU:
                receiptNotes =
                    receiptNoteRepository.getAllByReceiptItems_ProductSkuLikeAndWarehouse(
                        searchString,
                        listSearchValue,
                        warehouse,
                        pageable
                    );
                break;
            default:
                receiptNotes = receiptNoteRepository.findAllByWarehouseAndIsActiveTrue(warehouse, pageable);
                break;
        }
        return receiptNoteMapper.mapListEntityToListDto(receiptNotes);
    }

    @Override
    public void deleteReceiptNote(String receiptCode, ReceiptChangeInfoDto receiptChangeInfoDto) {
        ReceiptNote receiptNote = receiptNoteRepository
            .findByReceiptCode(receiptCode)
            .orElseThrow(() -> new NotFoundException(String.format("Inbound '%s' does not exist!", receiptCode)));
        if (!ReceiptNoteStatus.NEW.equals(receiptNote.getStatus())) {
            throw new BusinessLogicException("Just allow delete Inbound with status: NEW!");
        }
        receiptNote.setModifiedDate(DateUtil.currentInstantUTC());
        receiptNote.setModifiedBy(receiptChangeInfoDto.getUsername());
        receiptNote.setIsActive(false);
        receiptNoteRepository.save(receiptNote);
    }

    @Override
    public byte[] exportExcelFile(String fileName, ReceiptExportRequestDTO receiptExportParam) {
        log.debug("REST request: /receipt-notes/list/export --- Export list Receipt Note");

        // Create list detail data
        byte[] result = null;
        try {
            if (CommonDataUtil.isNotEmpty(receiptExportParam.getListColumn())) {
                Map<String, String> headerMap = new LinkedHashMap<>();

                for (String column : receiptExportParam.getListColumn()) {
                    switch (column) {
                        case ExcelReceiptHeaderConstant.RECEIPT_HEADER_WAREHOUSE:
                            headerMap.put("warehouse", CommonDataUtil.toPascalCase(ExcelReceiptHeaderConstant.RECEIPT_HEADER_WAREHOUSE));
                            break;
                        case ExcelReceiptHeaderConstant.RECEIPT_HEADER_STATUS:
                            headerMap.put("receiptStatus", CommonDataUtil.toPascalCase(ExcelReceiptHeaderConstant.RECEIPT_HEADER_STATUS));
                            break;
                        case ExcelReceiptHeaderConstant.RECEIPT_HEADER_VOUCHER_CODE:
                            headerMap.put(
                                "receiptCode",
                                CommonDataUtil.toPascalCase(ExcelReceiptHeaderConstant.RECEIPT_HEADER_VOUCHER_CODE)
                            );
                            break;
                        case ExcelReceiptHeaderConstant.RECEIPT_HEADER_IMPORTED_QUANTITY:
                            headerMap.put(
                                "totalActualImportedQty",
                                CommonDataUtil.toPascalCase(ExcelReceiptHeaderConstant.RECEIPT_HEADER_IMPORTED_QUANTITY)
                            );
                            break;
                        case ExcelReceiptHeaderConstant.RECEIPT_HEADER_INCOMING_QTY:
                            headerMap.put(
                                "totalConfirmedQty",
                                CommonDataUtil.toPascalCase(ExcelReceiptHeaderConstant.RECEIPT_HEADER_INCOMING_QTY)
                            );
                            break;
                        case ExcelReceiptHeaderConstant.RECEIPT_HEADER_REMAINING_QTY:
                            headerMap.put(
                                "totalDifferenceQty",
                                CommonDataUtil.toPascalCase(ExcelReceiptHeaderConstant.RECEIPT_HEADER_REMAINING_QTY)
                            );
                            break;
                        case ExcelReceiptHeaderConstant.RECEIPT_HEADER_RECEIPT_TYPE:
                            headerMap.put(
                                "receiptType",
                                CommonDataUtil.toPascalCase(ExcelReceiptHeaderConstant.RECEIPT_HEADER_RECEIPT_TYPE)
                            );
                            break;
                        case ExcelReceiptHeaderConstant.RECEIPT_HEADER_ADJUSTMENT_CODE:
                            headerMap.put(
                                "adjustmentCode",
                                CommonDataUtil.toPascalCase(ExcelReceiptHeaderConstant.RECEIPT_HEADER_ADJUSTMENT_CODE)
                            );
                            break;
                        case ExcelReceiptHeaderConstant.RECEIPT_HEADER_OUTBOUND_CODE:
                            headerMap.put(
                                "issueCode",
                                CommonDataUtil.toPascalCase(ExcelReceiptHeaderConstant.RECEIPT_HEADER_OUTBOUND_CODE)
                            );
                            break;
                        case ExcelReceiptHeaderConstant.RECEIPT_HEADER_DATE_CREATED:
                            headerMap.put("createdDate", ExcelReceiptHeaderConstant.RECEIPT_HEADER_DATE_CREATED_PASCAL_CASE);
                            break;
                        case ExcelReceiptHeaderConstant.RECEIPT_HEADER_CONTAINER_NO:
                            headerMap.put(
                                "containerNo",
                                CommonDataUtil.toPascalCase(ExcelReceiptHeaderConstant.RECEIPT_HEADER_CONTAINER_NO)
                            );
                            break;
                        case ExcelReceiptHeaderConstant.RECEIPT_HEADER_SHIPMENT_NO:
                            headerMap.put("shipmentNo", CommonDataUtil.toPascalCase(ExcelReceiptHeaderConstant.RECEIPT_HEADER_SHIPMENT_NO));
                            break;
                        case ExcelReceiptHeaderConstant.RECEIPT_HEADER_DATE_RECEIPT:
                            headerMap.put("receiptDate", ExcelReceiptHeaderConstant.RECEIPT_HEADER_DATE_RECEIPT_PASCAL_CASE);
                            break;
                        case ExcelReceiptHeaderConstant.RECEIPT_HEADER_CREATED_BY:
                            headerMap.put("createdBy", CommonDataUtil.toPascalCase(ExcelReceiptHeaderConstant.RECEIPT_HEADER_CREATED_BY));
                            break;
                        case ExcelReceiptHeaderConstant.RECEIPT_HEADER_MANUAL_CREATION:
                            headerMap.put(
                                "isManualCreate",
                                CommonDataUtil.toPascalCase(ExcelReceiptHeaderConstant.RECEIPT_HEADER_MANUAL_CREATION)
                            );
                            break;
                        default:
                            break;
                    }
                }
                List<ReceiptNoteListResponseDTO> listData;
                if (receiptExportParam.isFlgAll()) {
                    listData =
                        getListReceiptNote(
                            new ReceiptRequestParam(
                                receiptExportParam.getSearchValue(),
                                receiptExportParam.getSearchBy(),
                                Integer.MAX_VALUE,
                                0,
                                receiptExportParam.getFromDate(),
                                receiptExportParam.getToDate(),
                                receiptExportParam.getWarehouseId()
                            )
                        )
                            .toList();
                } else {
                    List<String> listReceiptCode = CommonDataUtil.convertStringToListTrim(receiptExportParam.getCodes(), ",");
                    listData =
                        receiptNoteMapper
                            .mapListEntityToListDto(
                                receiptNoteRepository.findAllByReceiptCodeIn(listReceiptCode, PageRequest.of(0, Integer.MAX_VALUE))
                            )
                            .toList();
                }
                result = ExcelServiceUtil.generateExcelFile(fileName, listData, headerMap);
            }
        } catch (Exception e) {
            log.error("Could not created excel file. Message: {}", e.getMessage());
            throw new BusinessException("Could not created excel file");
        }
        return result;
    }

    @Override
    public byte[] exportReceiptNotePdf(String receiptCode) {
        byte[] result = null;
        ReceiptNoteResponseDTO data = getReceiptNoteDetail(receiptCode, null);
        if (CommonDataUtil.isNotNull(data)) {
            // Path template file
            String fileName = "template/receipt_note_info.jrxml";

            // Set parameter
            Map<String, Object> parameters = new HashMap<>();

            parameters.put("issueNote", CommonDataUtil.isNotNull(data.getIssueCode()) ? data.getIssueCode() : "");
            parameters.put("containerNo", CommonDataUtil.isNotNull(data.getContainerNo()) ? data.getContainerNo() : "");
            parameters.put("shipmentNo", CommonDataUtil.isNotNull(data.getShipmentNo()) ? data.getShipmentNo() : "");
            parameters.put("receiptCode", data.getReceiptCode());
            parameters.put("warehouse", data.getWarehouse());
            parameters.put("warehouseFrom", CommonDataUtil.isNotNull(data.getWarehouseFrom()) ? data.getWarehouseFrom() : "");
            parameters.put(
                "totalShipmentQty",
                CommonDataUtil.isNotNull(data.getTotalConfirmedQty()) ? data.getTotalConfirmedQty().toString() : "0"
            );
            parameters.put("adjustmentCode", CommonDataUtil.isNotEmpty(data.getAdjustmentCode()) ? data.getAdjustmentCode() : "-");
            parameters.put("receiptType", data.getReceiptType());
            parameters.put("totalActualImportedQty", data.getTotalActualImportedQty().toString());
            parameters.put("totalTransferred", data.getTotalConfirmedQty().toString());
            parameters.put("totalDifferenceQty", data.getTotalDifferenceQty().toString());
            parameters.put("createdBy", data.getCreatedBy());
            parameters.put("note", data.getGeneralNote());
            parameters.put("status", data.getReceiptStatus());
            parameters.put("createdDate", data.getCreatedDate());
            parameters.put("department", CommonDataUtil.isNotEmpty(data.getDepartment()) ? data.getDepartment() : "-");
            parameters.put("receiptDate", CommonDataUtil.isNotNull(data.getReceiptDate()) ? data.getReceiptDate() : "-");

            // Fill data and export
            if (data.getReceiptItemDto().size() == 0) {
                data.getReceiptItemDto().add(new ReceiptNoteDetailResponseDTO());
            }
            result = JasperReportUtil.export(fileName, parameters, data.getReceiptItemDto());
        }
        return result;
    }

    @Override
    public ReceiptNoteResponseDTO updateReceiptNote(String receiptCode, ReceiptNoteDTO receiptNoteDTO) {
        ObjectWriter om = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            String json = om.writeValueAsString(receiptNoteDTO);
            log.debug("[LOGGING] updateReceiptNote - data: {}", json);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        ReceiptNote receiptNote = receiptNoteRepository
            .findByReceiptCode(receiptCode)
            .orElseThrow(() -> new NotFoundException(String.format("Inbound '%s' not found!", receiptCode)));
        if (!ReceiptNoteStatus.NEW.equals(receiptNote.getStatus())) {
            throw new BusinessLogicException("Inbound just allowed update with status 'NEW'!");
        }

        if (receiptNoteDTO.getIsConfirmed()) {
            receiptNoteDTO.setReceiptStatus(ReceiptNoteStatus.CONFIRMED);
        }
        CommonDataUtil.getModelMapper().map(receiptNoteDTO, receiptNote);
        //        receiptNote.setReceiptType(ReceiptType.getEnumByKey(receiptNoteDTO.getReceiptType().getKey()));

        // delete receipt item update
        List<Long> updatedReceiptCodes = receiptNoteDTO
            .getDetails()
            .parallelStream()
            .filter(receiptItemDTO -> receiptItemDTO.getId() != null)
            .map(ReceiptItemDTO::getId)
            .collect(Collectors.toList());

        receiptNote
            .getReceiptItems()
            .parallelStream()
            .forEach(receiptItem -> {
                if (!updatedReceiptCodes.contains(receiptItem.getId())) {
                    receiptItemRepository.deleteById(receiptItem.getId());
                }
            });

        // create new and update receipt item update
        receiptNote.getReceiptItems().clear();
        receiptNoteDTO
            .getDetails()
            .forEach(receiptItemDTO -> {
                ReceiptItem receiptItem;
                if (receiptItemDTO.getId() == null) {
                    receiptItem = modelMapper.map(receiptItemDTO, ReceiptItem.class);
                } else {
                    receiptItem =
                        receiptItemRepository
                            .findById(receiptItemDTO.getId())
                            .orElseThrow(() ->
                                new NotFoundException(String.format("Inbound item '%s' does not exist!", receiptItemDTO.getId()))
                            );
                    modelMapper.map(receiptItemDTO, receiptItem);
                }
                receiptItem.setProduct(
                    productRepository
                        .findById(receiptItemDTO.getProductId())
                        .orElseThrow(() -> new NotFoundException("product '%s' does not exist!"))
                );
                receiptItem = receiptItemRepository.save(receiptItem);
                receiptNote.addReceiptItem(receiptItem);
            });

        //        receiptNote.setWarehouse(
        //            warehouseRepository
        //                .findById(receiptNoteDTO.getWarehouse())
        //                .orElseThrow(() -> new NotFoundException(String.format("warehouse '%s' does not exist!", receiptCode)))
        //        );
        Instant receiptDate = CommonDataUtil.isEmpty(receiptNoteDTO.getReceiptDate())
            ? receiptNote.getCreatedDate()
            : DateUtil.convertStringToInstant(receiptNoteDTO.getReceiptDate(), DateUtil.SIMPLE_DATE_FORMAT_AM_PM_MARKER);
        receiptNote.setReceiptDate(receiptDate);
        receiptNote.setModifiedDate(DateUtil.currentInstantUTC());
        receiptNote.setStatus(receiptNoteDTO.getReceiptStatus());
        receiptNoteRepository.saveAndFlush(receiptNote);
        return receiptNoteMapper.mapEntityToDto(receiptNote);
    }

    @Override
    public ReceiptNoteResponseDTO approveReceiptNote(String receiptCode, ReceiptChangeInfoDto receiptChangeInfoDto) {
        ReceiptNote receiptNote = receiptNoteRepository
            .findByReceiptCode(receiptCode)
            .orElseThrow(() -> new NotFoundException(String.format("Inbound '%s' does note exist!", receiptCode)));
        if (!ReceiptNoteStatus.CONFIRMED.equals(receiptNote.getStatus())) {
            throw new BusinessLogicException("Inbound status must be: CONFIRMED");
        }
        receiptNote.setStatus(ReceiptNoteStatus.APPROVED);
        receiptNote.setModifiedDate(DateUtil.currentInstantUTC());
        receiptNote.setModifiedBy(receiptChangeInfoDto.getUsername());
        return receiptNoteMapper.mapEntityToDto(receiptNoteRepository.save(receiptNote));
    }

    @Override
    public ReceiptNoteResponseDTO completeReceiptNote(String receiptCode, ReceiptChangeInfoDto receiptChangeInfoDto) {
        ReceiptNote receiptNote = receiptNoteRepository
            .findByReceiptCode(receiptCode)
            .orElseThrow(() -> new NotFoundException(String.format("Inbound '%s' does note exist!", receiptCode)));

        ReceiptNoteStatus validStatus = ReceiptType.INTERNAL_TRANSFER.equals(receiptNote.getReceiptType())
            ? ReceiptNoteStatus.APPROVED
            : ReceiptNoteStatus.CONFIRMED;

        if (!validStatus.equals(receiptNote.getStatus())) {
            throw new BusinessLogicException(String.format("Inbound status must be: %s", validStatus));
        }

        receiptNote.setStatus(ReceiptNoteStatus.COMPLETED);
        IssueNoteDTO issueNoteDTO = new IssueNoteDTO();
        issueNoteDTO.setIssueCode(receiptNote.getIssueCode());
        issueNoteDTO.setUserName(receiptChangeInfoDto.getUsername());

        receiptNote.setModifiedBy(receiptChangeInfoDto.getUsername());
        receiptNote.setModifiedDate(DateUtil.currentInstantUTC());
        receiptNote = receiptNoteRepository.save(receiptNote);

        doActualImportedReceiptNote(receiptNote);
        if (ReceiptType.INTERNAL_TRANSFER.equals(receiptNote.getReceiptType())) {
            issueNoteService.completeIssueNote(issueNoteDTO, receiptNote);
        }

        return receiptNoteMapper.mapEntityToDto(receiptNote);
    }

    @Override
    public Page<ReceiptNoteResponseDTO> filterCompletedReceiptNote(CompletedNoteRequestDTO requestParamDTO) {
        Pageable pageable = PageRequest.of(requestParamDTO.getPage(), requestParamDTO.getSize());
        Page<ReceiptNote> data = receiptNoteRepository.filterCompleteReceiptNote(
            requestParamDTO.getSku(),
            DateUtil.convertDateToDateTimeStart(requestParamDTO.getFromDate()),
            DateUtil.convertDateToDateTimeEnd(requestParamDTO.getToDate()),
            requestParamDTO.getWarehouseId(),
            pageable
        );
        return receiptNoteMapper.mapListEntityToDto(data);
    }

    private WarehouseAdjustmentRequestDTO buildWarehouseAdjustmentRequest(ReceiptNote receiptNote) {
        List<WarehouseAdjustmentRequestDTO.AdjustmentItemDTO> adjustmentItems = receiptNote
            .getReceiptItems()
            .parallelStream()
            .map(item ->
                WarehouseAdjustmentRequestDTO.AdjustmentItemDTO
                    .builder()
                    .sku(item.getProduct().getSku())
                    .quantity(item.getActualImportedQty().toString())
                    .build()
            )
            .collect(Collectors.toList());
        return WarehouseAdjustmentRequestDTO
            .builder()
            .type(Constants.ADJUSTMENT_INCREASE_TYPE)
            .reference(receiptNote.getReceiptCode())
            .warehouseCode(receiptNote.getWarehouse().getWarehouseCode())
            .items(adjustmentItems)
            .build();
    }

    private void doActualImportedReceiptNote(ReceiptNote receiptNote) {
        receiptNoteLogService.doInsertLog(receiptNote, receiptNote.getWarehouse().getId());
        warehouseAdjustmentService.doAdjustReserveQuantity(buildWarehouseAdjustmentRequest(receiptNote));
    }
}
