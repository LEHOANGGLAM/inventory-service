package com.yes4all.service.impl;

import com.poiji.bind.Poiji;
import com.poiji.exception.PoijiExcelType;
import com.yes4all.common.constants.AdjustmentErrorConstants;
import com.yes4all.common.constants.ErrorConstant;
import com.yes4all.common.constants.ExcelAdjustmentHeaderConstant;
import com.yes4all.common.errors.AdjustmentErrorBuilder;
import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.errors.NotFoundException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtil;
import com.yes4all.common.utils.ExcelServiceUtil;
import com.yes4all.common.utils.JasperReportUtil;
import com.yes4all.config.Constants;
import com.yes4all.domain.Adjustment;
import com.yes4all.domain.AdjustmentItem;
import com.yes4all.domain.PeriodLog;
import com.yes4all.domain.Warehouse;
import com.yes4all.domain.enumeration.AdjustmentFilterType;
import com.yes4all.domain.enumeration.IssueType;
import com.yes4all.domain.enumeration.Reason;
import com.yes4all.domain.enumeration.ReceiptType;
import com.yes4all.repository.AdjustmentRepository;
import com.yes4all.repository.PeriodLogRepository;
import com.yes4all.repository.WarehouseRepository;
import com.yes4all.service.AdjustmentService;
import com.yes4all.service.IssueNoteService;
import com.yes4all.service.ReceiptNoteService;
import com.yes4all.service.WarehouseAdjustmentService;
import com.yes4all.service.dto.request.*;
import com.yes4all.service.dto.response.*;
import com.yes4all.service.mapper.AdjustmentItemMapper;
import com.yes4all.service.mapper.AdjustmentMapper;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class AdjustmentServiceImpl implements AdjustmentService {

    private final Logger log = LoggerFactory.getLogger(AdjustmentServiceImpl.class);

    @Autowired
    AdjustmentRepository adjustmentRepository;

    @Autowired
    WarehouseAdjustmentService warehouseAdjustmentService;

    @Autowired
    GetAllInventoryLocationService getAllInventoryLocationService;

    @Autowired
    ReceiptNoteService receiptNoteService;

    @Autowired
    WarehouseRepository warehouseRepository;

    @Autowired
    IssueNoteService issueNoteService;

    @Autowired
    AdjustmentMapper adjustmentMapper;

    @Autowired
    AdjustmentItemMapper adjustmentItemMapper;

    @Autowired
    GetAllInventoryLocationService wmsInventoryLocationService;

    @Autowired
    PeriodLogRepository periodLogRepository;

    @Override
    public boolean doValidateAdjustmentFileUpload(MultipartFile file, String warehouseCode) {
        List<AdjustmentItemDTO> listAdjustmentItemDto;
        try {
            // CASE : check header column invalid
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            Row row = sheet.getRow(0);
            int columnCount = 0;
            for (Cell cell : row) {
                String headerName = cell.getStringCellValue();
                if (!headerName.isBlank()) {
                    columnCount++;
                }
            }
            // Must have limit 3 column header
            if (columnCount > 3) {
                throw new BusinessException(AdjustmentErrorConstants.ADJUST_ERROR_FILE_INVALID_FORMAT);
            }

            // Trim list Data from update file.
            listAdjustmentItemDto = Poiji.fromExcel(file.getInputStream(), PoijiExcelType.XLSX, AdjustmentItemDTO.class);
            listAdjustmentItemDto = adjustmentItemMapper.trimData(listAdjustmentItemDto);
        } catch (Exception exception) {
            throw new BusinessException(AdjustmentErrorConstants.ADJUST_ERROR_FILE_INVALID_FORMAT);
        }

        // CASE : File upload is empty
        if (listAdjustmentItemDto.isEmpty()) {
            throw new BusinessException(AdjustmentErrorConstants.ADJUST_ERROR_FILE_INVALID_FORMAT);
        }

        // CASE : Check format and sku exist in resource in mapListDtoToEntity func
        List<AdjustmentItem> listAdjustItem = adjustmentItemMapper.mapListDtoToEntity(listAdjustmentItemDto, new Adjustment());

        // CASE : Duplicate sku in file upload
        Set<String> listSkuUnique = new HashSet<>();
        Set<AdjustmentItemDTO> listSkuDuplicate = listAdjustmentItemDto
            .stream()
            .filter(check -> !listSkuUnique.add(check.getSku()))
            .collect(Collectors.toSet());
        if (!listSkuDuplicate.isEmpty()) {
            String skuErrorMessage = AdjustmentErrorBuilder.buildDuplicate(
                listSkuDuplicate.stream().map(AdjustmentItemDTO::getSku).collect(Collectors.toList())
            );
            throw new BusinessException(skuErrorMessage);
        }

        List<String> listSku = listAdjustItem.stream().map(AdjustmentItem::getSku).collect(Collectors.toList());
        List<WMSInventoryLocationItemDTO> listWIPBefore = getWMSInventoryLocations(listSku, warehouseCode);
        if (listWIPBefore.size() != listAdjustItem.size()) {
            List<String> listSkuBefore = listWIPBefore.stream().map(WMSInventoryLocationItemDTO::getSku).collect(Collectors.toList());
            listSku.removeAll(listSkuBefore);
            if (!listSku.isEmpty()) {
                String notFoundErrorStr = AdjustmentErrorBuilder.buildNotFound(listSku);
                throw new NotFoundException(notFoundErrorStr);
            }
        }

        for (WMSInventoryLocationItemDTO itemBefore : listWIPBefore) {
            for (AdjustmentItem adjustItem : listAdjustItem) {
                if (itemBefore.getSku().equals(adjustItem.getSku())) {
                    Integer wipBefore = Integer.parseInt(itemBefore.getWip());
                    Integer pkuBefore = Integer.parseInt(itemBefore.getPku());
                    adjustItem.setWipQuantityBefore(wipBefore);
                    adjustItem.setPkuQuantityBefore(pkuBefore);
                    adjustItem.setTotalQuantityBefore(wipBefore + pkuBefore);
                }
            }
        }

        // CASE : check sku have not changing
        List<String> listSkuNotChange = Collections.synchronizedList(new ArrayList<>());
        listAdjustItem
            .parallelStream()
            .forEach(adjItem -> {
                if (
                    adjItem.getPkuQuantityBefore().equals(adjItem.getPkuQuantityAfter()) &&
                    adjItem.getWipQuantityBefore().equals(adjItem.getWipQuantityAfter())
                ) {
                    listSkuNotChange.add(adjItem.getSku());
                }
            });
        if (!listSkuNotChange.isEmpty()) {
            String skuNotChangeError = AdjustmentErrorBuilder.buildNotChange(listSkuNotChange);
            throw new BusinessException(skuNotChangeError);
        }
        return true;
    }

    @Override
    @Transactional
    public AdjustmentDetailResponseDTO createAdjustment(MultipartFile file, AdjustmentDTO adjustmentDTO) {
        StopWatch generalSW = DateUtil.initStopWatch();
        log.debug("[START] createAdjustment");
        // DO : Validate file Valid before create adjustment flow.
        // doValidateAdjustmentFileUpload(file, adjustmentDTO.getWarehouseCode());

        log.debug("[START] Validate upload file");
        StopWatch sw = DateUtil.initStopWatch();
        // D0 : Start do Adjustment flow.
        List<AdjustmentItemDTO> listAdjustmentItemDto;
        try {
            listAdjustmentItemDto = Poiji.fromExcel(file.getInputStream(), PoijiExcelType.XLSX, AdjustmentItemDTO.class);
            // Trim list Data from upload file .
            listAdjustmentItemDto = adjustmentItemMapper.trimData(listAdjustmentItemDto);
        } catch (Exception exception) {
            throw new BusinessException(AdjustmentErrorConstants.ADJUST_ERROR_FILE_INVALID_FORMAT);
        }
        Adjustment adjustment = adjustmentMapper.mapDtoToEntity(adjustmentDTO);
        String receiptCode = "";
        String issueCode = "";

        if (listAdjustmentItemDto.isEmpty()) {
            throw new BusinessException(AdjustmentErrorConstants.ADJUST_ERROR_FILE_INVALID_FORMAT);
        }

        // Validate : check duplicate sku in file upload
        Set<String> listSkuUnique = new HashSet<>();
        Set<AdjustmentItemDTO> listSkuDuplicate = listAdjustmentItemDto
            .stream()
            .filter(check -> !listSkuUnique.add(check.getSku()))
            .collect(Collectors.toSet());
        if (!listSkuDuplicate.isEmpty()) {
            String skuErrorMessage = AdjustmentErrorBuilder.buildDuplicate(
                listSkuDuplicate.stream().map(AdjustmentItemDTO::getSku).collect(Collectors.toList())
            );

            throw new BusinessException(skuErrorMessage);
        }

        // Do: create new
        adjustment.setAdjustmentCode(
            CommonDataUtil.getCodeByValue(Constants.ADJUSTMENT_PREFIX, adjustmentRepository.getNextAdjustmentValue().toString())
        );

        adjustment.setDateCreated(DateUtil.currentInstantUTC());
        adjustment = adjustmentRepository.saveAndFlush(adjustment);
        Set<AdjustmentItem> listAdjustItem = new HashSet<>(adjustmentItemMapper.mapListDtoToEntity(listAdjustmentItemDto, adjustment));
        List<String> skuList = listAdjustmentItemDto.stream().map(AdjustmentItemDTO::getSku).collect(Collectors.toList());
        List<WMSInventoryLocationItemDTO> listWIPBefore = getWMSInventoryLocations(skuList, adjustment.getWarehouseCode());

        for (WMSInventoryLocationItemDTO itemBefore : listWIPBefore) {
            AdjustmentItem adjustmentItem = listAdjustItem
                .stream()
                .filter(item -> item.getSku().equalsIgnoreCase(itemBefore.getSku()))
                .findFirst()
                .orElse(null);
            if (CommonDataUtil.isNotNull(adjustmentItem)) {
                Integer wipBefore = Integer.parseInt(itemBefore.getWip());
                Integer pkuBefore = Integer.parseInt(itemBefore.getPku());
                adjustmentItem.setWipQuantityBefore(wipBefore);
                adjustmentItem.setPkuQuantityBefore(pkuBefore);
                adjustmentItem.setTotalQuantityBefore(wipBefore + pkuBefore);
            }
        }

        // Validate : check sku have not changing
        List<String> listSkuNotChange = Collections.synchronizedList(new ArrayList<>());
        listAdjustItem
            .parallelStream()
            .forEach(adjItem -> {
                if (
                    adjItem.getPkuQuantityBefore().equals(adjItem.getPkuQuantityAfter()) &&
                    adjItem.getWipQuantityBefore().equals(adjItem.getWipQuantityAfter())
                ) {
                    listSkuNotChange.add(adjItem.getSku());
                }
            });
        if (!listSkuNotChange.isEmpty()) {
            String skuNotChangeError = AdjustmentErrorBuilder.buildNotChange(listSkuNotChange);
            throw new BusinessException(skuNotChangeError);
        }
        log.debug("[END] 2nd validate in: {}", DateUtil.calculateTime(sw));
        // Create Inbound and Outbound
        // set list receipt and issue
        log.debug("[START] list receipt and issue");
        sw = DateUtil.initStopWatch();
        Set<ReceiptItemDTO> receiptItemDTOS = new HashSet<>();
        Set<IssueNoteInfoDetailRequestDTO> issueItemDTOs = new HashSet<>();
        String warehouseCode = adjustment.getWarehouseCode();
        Warehouse warehouse = warehouseRepository
            .findFirstByWarehouseCode(warehouseCode)
            .orElseThrow(() -> new BusinessException(String.format("Warehouse '%s' does not exist!", warehouseCode)));
        listAdjustItem.forEach(adjustmentItem -> {
            if (adjustmentItem.getTotalQuantityAfter() > adjustmentItem.getTotalQuantityBefore()) {
                Integer confirmQty = adjustmentItem.getTotalQuantityAfter() - adjustmentItem.getTotalQuantityBefore();
                receiptItemDTOS.add(
                    ReceiptItemDTO
                        .builder()
                        .sku(adjustmentItem.getSku())
                        .note(adjustmentDTO.getGeneralNote())
                        .confirmedQty(confirmQty)
                        .actualImportedQty(confirmQty)
                        .differenceQty(0)
                        .build()
                );
            } else if (adjustmentItem.getTotalQuantityAfter() < adjustmentItem.getTotalQuantityBefore()) {
                Integer exportQty = adjustmentItem.getTotalQuantityBefore() - adjustmentItem.getTotalQuantityAfter();
                issueItemDTOs.add(
                    IssueNoteInfoDetailRequestDTO
                        .builder()
                        .note(adjustmentDTO.getGeneralNote())
                        .sku(adjustmentItem.getSku())
                        .actualExportedQty(exportQty)
                        .confirmedQty(exportQty)
                        .remainingQty(0)
                        .build()
                );
            }
        });
        log.debug("[END] list receipt and issue in: {}", DateUtil.calculateTime(sw));

        // Create Inbound/Outbound
        log.debug("[START] Create Inbound/Outbound");
        sw = DateUtil.initStopWatch();
        syncPeriodLog();
        final Adjustment finalAdjustment = adjustment;
        CompletableFuture<String> futureInbound = CompletableFuture.supplyAsync(() ->
            createAdjustedInbound(receiptItemDTOS, finalAdjustment)
        );
        CompletableFuture<String> futureOutbound = CompletableFuture.supplyAsync(() ->
            createAdjustedOutbound(issueItemDTOs, finalAdjustment, warehouse)
        );
        try {
            receiptCode = futureInbound.get();
            issueCode = futureOutbound.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());
            throw new BusinessException("Fail to create adjusted Inbound/Outbound");
        }
        log.debug("[END] Create Inbound/Outbound in: {}", DateUtil.calculateTime(sw));

        int totalSkuChange = 0;
        for (AdjustmentItem adjustmentItem : listAdjustItem) {
            if (adjustmentItem.getTotalQuantityBefore() < adjustmentItem.getTotalQuantityAfter()) {
                adjustmentItem.setInboundCode(receiptCode);
            } else {
                adjustmentItem.setOutboundCode(issueCode);
            }
            totalSkuChange += Math.abs(adjustmentItem.getTotalQuantityAfter() - adjustmentItem.getTotalQuantityBefore());
        }

        log.debug("[START] Mapping total SKU");
        sw = DateUtil.initStopWatch();
        adjustment.setAdjustmentItems(listAdjustItem);
        adjustment.setTotalSku(totalSkuChange);
        adjustment.setId(finalAdjustment.getId());
        adjustmentRepository.saveAndFlush(adjustment);
        log.debug("[END] Mapping total SKU in: {}", DateUtil.calculateTime(sw));

        // Do : Sync to WMS
        log.debug("[START] Sync to WMS");
        sw = DateUtil.initStopWatch();
        warehouseAdjustmentService.doUpdateQtyWarehouseByAdjustment(adjustment);
        log.debug("[END] Sync to WMS in: {}", DateUtil.calculateTime(sw));

        log.debug("[START] Mapping to DTO");
        sw = DateUtil.initStopWatch();
        AdjustmentDetailResponseDTO dto = adjustmentMapper.mapEntityToDetailDto(adjustment);
        log.debug("[END] Mapping to DTO in: {}", DateUtil.calculateTime(sw));
        log.debug("[END] createAdjustment in: {}", DateUtil.calculateTime(generalSW));
        return dto;
    }

    @Override
    public AdjustmentDetailResponseDTO getAdjustmentDetailByAdjustmentCode(String adjustmentCode, String warehouseCode) {
        Adjustment adjustment = adjustmentRepository
            .findByAdjustmentCode(adjustmentCode)
            .orElseThrow(() -> new NotFoundException(String.format("resource not found for adjustmentCode: %s", adjustmentCode)));
        if (CommonDataUtil.isNotNull(warehouseCode)) {
            if (!adjustment.getWarehouseCode().equals(warehouseCode)) {
                throw new BusinessException(
                    ErrorConstant.NOT_FOUND_RESPONSE_CODE,
                    String.format(
                        "The Adjustment %s does not belong to %s. This Adjustment belongs to %s.",
                        adjustmentCode,
                        warehouseCode,
                        adjustment.getWarehouseCode()
                    )
                );
            }
        }
        return adjustmentMapper.mapEntityToDetailDto(adjustment);
    }

    @Override
    public Page<AdjustmentResponseDTO> getListAdjustmentByCondition(AdjustmentParamDTO filter) {
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), Sort.Direction.DESC, "dateCreated");
        String searchValue = '%' + filter.getSearchValue().trim().toUpperCase() + '%';
        AdjustmentFilterType filterType = AdjustmentFilterType.valueOfOrDefault(filter.getSearchBy().toUpperCase());
        List<String> listSearchValue = CommonDataUtil.convertStringToListTrimAndUpper(filter.getSearchValue(), ",");

        Page<Adjustment> adjustments;
        switch (filterType) {
            case REASON:
                List<Reason> listReason = listSearchValue.isEmpty() || listSearchValue.contains("ALL")
                    ? Arrays.asList(Reason.values())
                    : listSearchValue.stream().map(Reason::valueOf).collect(Collectors.toList());
                adjustments = adjustmentRepository.findAllByReasonAndWarehouse(filter.getWarehouseCode(), listReason, pageable);
                break;
            case CREATED_DATE:
                adjustments =
                    adjustmentRepository.findAllByDateCreatedAndWarehouse(
                        DateUtil.convertDateToDateTimeStart(filter.getFromDate()),
                        DateUtil.convertDateToDateTimeEnd(filter.getToDate()),
                        filter.getWarehouseCode(),
                        pageable
                    );
                break;
            case ADJUSTMENT_CODE:
                adjustments =
                    adjustmentRepository.findAllByAdjustmentCodeAndWarehouseCode(
                        filter.getWarehouseCode(),
                        listSearchValue,
                        searchValue,
                        pageable
                    );
                break;
            default:
                adjustments = adjustmentRepository.findAllByWarehouseCodeLike(filter.getWarehouseCode(), pageable);
                break;
        }
        return adjustmentMapper.mapListEntityToDto(adjustments);
    }

    @Override
    public byte[] exportAdjustmentExcelFile(String fileName, AdjustmentExportRequestDTO adjustmentExportRequestDTO) {
        log.debug("REST request: /adjustments/list/export --- Export list Adjustment");

        // Create list detail data
        byte[] result = null;
        try {
            if (CommonDataUtil.isNotEmpty(adjustmentExportRequestDTO.getListColumn())) {
                Map<String, String> headerMap = new LinkedHashMap<>();
                for (String column : adjustmentExportRequestDTO.getListColumn()) {
                    switch (column) {
                        case ExcelAdjustmentHeaderConstant.ADJUSTMENT_HEADER_WAREHOUSE:
                            headerMap.put(
                                "warehouse",
                                CommonDataUtil.toPascalCase(ExcelAdjustmentHeaderConstant.ADJUSTMENT_HEADER_WAREHOUSE)
                            );
                            break;
                        case ExcelAdjustmentHeaderConstant.ADJUSTMENT_HEADER_STATUS:
                            headerMap.put("status", CommonDataUtil.toPascalCase(ExcelAdjustmentHeaderConstant.ADJUSTMENT_HEADER_STATUS));
                            break;
                        case ExcelAdjustmentHeaderConstant.ADJUSTMENT_HEADER_ADJUSTMENT_CODE:
                            headerMap.put(
                                "adjustmentCode",
                                CommonDataUtil.toPascalCase(ExcelAdjustmentHeaderConstant.ADJUSTMENT_HEADER_ADJUSTMENT_CODE)
                            );
                            break;
                        case ExcelAdjustmentHeaderConstant.ADJUSTMENT_HEADER_TOTAL_SKU:
                            headerMap.put(
                                "totalSku",
                                CommonDataUtil.toPascalCase(ExcelAdjustmentHeaderConstant.ADJUSTMENT_HEADER_TOTAL_SKU)
                            );
                            break;
                        case ExcelAdjustmentHeaderConstant.ADJUSTMENT_HEADER_REASON:
                            headerMap.put("reason", CommonDataUtil.toPascalCase(ExcelAdjustmentHeaderConstant.ADJUSTMENT_HEADER_REASON));
                            break;
                        case ExcelAdjustmentHeaderConstant.ADJUSTMENT_HEADER_OTHER_NOTE:
                            headerMap.put(
                                "otherNote",
                                CommonDataUtil.toPascalCase(ExcelAdjustmentHeaderConstant.ADJUSTMENT_HEADER_OTHER_NOTE)
                            );
                            break;
                        case ExcelAdjustmentHeaderConstant.ADJUSTMENT_HEADER_DATE_CREATED:
                            headerMap.put("dateCreated", ExcelAdjustmentHeaderConstant.ADJUSTMENT_HEADER_DATE_CREATED_PASCAL_CASE);
                            break;
                        case ExcelAdjustmentHeaderConstant.ADJUSTMENT_HEADER_CREATED_BY:
                            headerMap.put(
                                "createdBy",
                                CommonDataUtil.toPascalCase(ExcelAdjustmentHeaderConstant.ADJUSTMENT_HEADER_CREATED_BY)
                            );
                            break;
                        default:
                            break;
                    }
                }
                List<AdjustmentResponseDTO> listData;
                if (adjustmentExportRequestDTO.isFlgAll()) {
                    listData =
                        getListAdjustmentByCondition(
                            new AdjustmentParamDTO(
                                adjustmentExportRequestDTO.getSearchValue(),
                                adjustmentExportRequestDTO.getSearchBy(),
                                Integer.MAX_VALUE,
                                0,
                                adjustmentExportRequestDTO.getFromDate(),
                                adjustmentExportRequestDTO.getToDate(),
                                adjustmentExportRequestDTO.getWarehouseCode()
                            )
                        )
                            .toList();
                } else {
                    List<String> listAdjustmentCode = CommonDataUtil.convertStringToListTrim(adjustmentExportRequestDTO.getCodes(), ",");
                    listData =
                        adjustmentMapper
                            .mapListEntityToDto(
                                adjustmentRepository.findAllByAdjustmentCodeIn(listAdjustmentCode, PageRequest.of(0, Integer.MAX_VALUE))
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
    public byte[] getExcelTemplate() {
        String fileName = "template/Import_internal_template.xlsx";
        byte[] result = new byte[0];
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (CommonDataUtil.isNotNull(inputStream)) {
                result = inputStream.readAllBytes();
            }
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new BusinessException("can't export adjustment template file!");
        }
        return result;
    }

    @Override
    public byte[] exportAdjustmentPdf(String adjustmentCode) {
        byte[] result = null;
        AdjustmentDetailResponseDTO data = getAdjustmentDetailByAdjustmentCode(adjustmentCode, null);
        if (CommonDataUtil.isNotNull(data)) {
            // Path template file
            String fileName = "template/adjustment_info.jrxml";

            // Set parameter
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("adjustmentCode", data.getAdjustmentCode());
            parameters.put("warehouse", data.getWarehouse());
            parameters.put("totalSku", data.getTotalSku().toString());
            parameters.put("reason", data.getReason());
            parameters.put("createdBy", data.getCreatedBy());
            parameters.put("createdDate", data.getCreatedDate());
            parameters.put("status", data.getStatus());
            parameters.put("note", data.getOtherNote());

            // Fill data and export
            List<AdjustmentItemPdfResponseDto> listData = Collections.synchronizedList(new ArrayList<>());
            if (data.getDetails().size() == 0) {
                listData.add(new AdjustmentItemPdfResponseDto());
            } else {
                data
                    .getDetails()
                    .parallelStream()
                    .forEach(item -> {
                        AdjustmentItemPdfResponseDto itemPdf = AdjustmentItemPdfResponseDto
                            .builder()
                            .no(item.getNo())
                            .sku(item.getSku())
                            .inboundCode(CommonDataUtil.isNotNull(item.getInboundCode()) ? item.getInboundCode() : "")
                            .outboundCode(CommonDataUtil.isNotNull(item.getOutboundCode()) ? item.getOutboundCode() : "")
                            .wipQuantity(
                                !Objects.equals(item.getWipQuantityAfter(), item.getWipQuantityBefore())
                                    ? String.format("%d → %s", item.getWipQuantityBefore(), item.getWipQuantityAfter())
                                    : item.getWipQuantityAfter().toString()
                            )
                            .pkuQty(
                                !Objects.equals(item.getPkuQuantityBefore(), item.getPkuQuantityAfter())
                                    ? String.format("%d → %d", item.getPkuQuantityBefore(), item.getPkuQuantityAfter())
                                    : item.getPkuQuantityAfter().toString()
                            )
                            .totalQty(
                                !Objects.equals(item.getTotalQuantityAfter(), item.getTotalQuantityBefore())
                                    ? String.format("%d → %d", item.getTotalQuantityBefore(), item.getTotalQuantityAfter())
                                    : item.getTotalQuantityAfter().toString()
                            )
                            .asin(CommonDataUtil.isNotNull(item.getAsin()) ? item.getAsin() : "")
                            .productTitle(item.getProductTitle())
                            .build();
                        listData.add(itemPdf);
                    });
            }
            result = JasperReportUtil.export(fileName, parameters, listData);
        }
        return result;
    }

    private List<WMSInventoryLocationItemDTO> getWMSInventoryLocations(List<String> querySkuList, String warehouseCode) {
        log.debug("[START] Get WMS inventory location");
        StopWatch sw = DateUtil.initStopWatch();
        int pageNum = 0;
        int index = 0;
        final int batchSize = 10;
        int querySize = querySkuList.size();
        List<WMSInventoryLocationItemDTO> inventoryLocations = new ArrayList<>();
        while (index < querySize) {
            InventoryLocationParamDTO filterParams = InventoryLocationParamDTO
                .builder()
                .page(pageNum)
                .size(batchSize)
                .warehouseCode(warehouseCode)
                .build();
            WMSInventoryLocationResponseDTO result = getAllInventoryLocationService.getInventoryLocation(querySkuList, filterParams);
            inventoryLocations.addAll(result.getDataList());
            index += batchSize;
            pageNum++;
        }
        log.debug("[END] Get WMS inventory location in: {}", DateUtil.calculateTime(sw));
        return inventoryLocations;
    }

    private String createAdjustedInbound(Set<ReceiptItemDTO> receiptItems, Adjustment adjustment) {
        log.debug("[START] Create Inbound");
        StopWatch sw = DateUtil.initStopWatch();
        String receiptCode = null;
        if (!receiptItems.isEmpty()) {
            Integer totalImportQty = receiptItems.stream().mapToInt(ReceiptItemDTO::getActualImportedQty).sum();
            ReceiptNoteDTO receiptNoteDTO = ReceiptNoteDTO
                .builder()
                .generalNote(adjustment.getNote())
                .createdBy(adjustment.getCreatedBy())
                .isConfirmed(true)
                .adjustmentDto(adjustment)
                .receiptType(ReceiptType.INCREASE_ADJUSTMENT)
                .isManualCreate(false)
                .warehouseCode(adjustment.getWarehouseCode())
                .details(receiptItems)
                .totalActualImportedQty(totalImportQty)
                .totalConfirmedQty(totalImportQty)
                .totalDifferenceQty(0)
                .build();
            receiptCode = receiptNoteService.saveReceiptNote(receiptNoteDTO).getReceiptCode();
        }
        log.debug("[END] Create Inbound in: {}", DateUtil.calculateTime(sw));
        return receiptCode;
    }

    private String createAdjustedOutbound(Set<IssueNoteInfoDetailRequestDTO> issueItemDTOs, Adjustment adjustment, Warehouse warehouse) {
        log.debug("[START] Create Outbound");
        StopWatch sw = DateUtil.initStopWatch();
        String inboundCode = null;
        if (!issueItemDTOs.isEmpty()) {
            Integer totalExportQty = issueItemDTOs.stream().mapToInt(IssueNoteInfoDetailRequestDTO::getActualExportedQty).sum();
            IssueNoteInfoRequestDTO issueNoteInfoRequestDTO = IssueNoteInfoRequestDTO
                .builder()
                .generalNote(adjustment.getNote())
                .details(issueItemDTOs)
                .createdBy(adjustment.getCreatedBy())
                .isConfirmed(true)
                .adjustmentDto(adjustment)
                .issueToName(warehouse.getWarehouseName())
                .issueToAddress(warehouse.getAddress())
                .issueType(IssueType.DECREASE_ADJUSTMENT.getKey())
                .isManualCreate(false)
                .warehouseCodeFrom(adjustment.getWarehouseCode())
                .warehouseCodeTo(adjustment.getWarehouseCode())
                .totalActualExportedQty(totalExportQty)
                .totalConfirmedQty(totalExportQty)
                .totalRemainingQty(0)
                .build();
            inboundCode = issueNoteService.saveOrUpdateIssueNote(issueNoteInfoRequestDTO).getIssueCode();
        }
        log.debug("[END] Create Outbound in: {}", DateUtil.calculateTime(sw));
        return inboundCode;
    }

    private void syncPeriodLog() {
        Instant now = DateUtil.currentInstantUTC();
        LocalDateTime ldt = DateUtil.convertInstantToLocalDateTime(DateUtil.currentInstantUTC());
        PeriodLog periodLog = periodLogRepository.findByDayAndMonthAndYear(ldt.getDayOfMonth(), ldt.getMonthValue(), ldt.getYear());
        if (CommonDataUtil.isNull(periodLog)) {
            periodLog = new PeriodLog();
            periodLog.setDay(ldt.getDayOfMonth());
            periodLog.setMonth(ldt.getMonthValue());
            periodLog.setYear(ldt.getYear());
            periodLog.setFullDate(DateUtil.getStartOfDay(now));
            periodLogRepository.saveAndFlush(periodLog);
        }
    }
}
