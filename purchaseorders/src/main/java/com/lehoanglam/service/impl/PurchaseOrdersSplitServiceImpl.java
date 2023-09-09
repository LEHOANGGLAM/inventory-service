package com.yes4all.service.impl;

import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtils;
import com.yes4all.common.utils.PageRequestUtil;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.*;
import com.yes4all.domain.model.*;
import com.yes4all.repository.*;
import com.yes4all.service.PurchaseOrdersService;
import com.yes4all.service.PurchaseOrdersSplitService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Service Implementation for managing {@link PurchaseOrders}.
 */
@Service
@Transactional
public class PurchaseOrdersSplitServiceImpl implements PurchaseOrdersSplitService {

    private final Logger log = LoggerFactory.getLogger(PurchaseOrdersSplitServiceImpl.class);
    private static final String KEY_UPLOAD = ";";
    @Autowired
    private PurchaseOrdersSplitRepository purchaseOrdersSplitRepository;

    @Autowired
    private PurchaseOrdersSplitResultRepository purchaseOrdersSplitResultRepository;

    @Autowired
    private PurchaseOrdersSplitDataRepository purchaseOrdersSplitDataRepository;

    @Override
    public List<PurchaseOrdersSplit> createPurchaseOrdersSplit(List<ResultUploadDTO> data, String user) {
        try {
            List<PurchaseOrdersSplit> purchaseOrdersSplitList = new ArrayList<>();
            data.stream().forEach(i -> {
                i.getPurchaseOrdersSplit().entrySet().stream().forEach(k -> {
                    PurchaseOrdersSplit purchaseOrdersSplit = new PurchaseOrdersSplit();
                    Set<PurchaseOrdersSplitData> values = k.getValue().stream().collect(Collectors.toSet());
                    String fileName = k.getKey();
                    purchaseOrdersSplit.setRootFile(fileName);
                    purchaseOrdersSplit.setCreatedBy(user);
                    purchaseOrdersSplit.setStatus(1);
                    purchaseOrdersSplit.setCreatedDate(new Date().toInstant());
                    purchaseOrdersSplit.setUpdatedBy(user);
                    purchaseOrdersSplit.setUpdatedDate(new Date().toInstant());
                    purchaseOrdersSplit.setPurchaseOrdersSplitData(values);
                    purchaseOrdersSplitList.add(purchaseOrdersSplit);
                });
            });
            purchaseOrdersSplitRepository.saveAllAndFlush(purchaseOrdersSplitList);
            return purchaseOrdersSplitList;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }

    }

    private PurchaseOrderSplitDataDTO convertToObjectDto(Object o) {
        PurchaseOrderSplitDataDTO dto = new PurchaseOrderSplitDataDTO();
        PurchaseOrdersSplitData purchaseOrdersSplitData = (PurchaseOrdersSplitData) o;
        dto = CommonDataUtil.getModelMapper().map(o, PurchaseOrderSplitDataDTO.class);
        return dto;
    }

    private PurchaseOrderSplitResultDTO convertToObjectResultDto(Object o) {
        PurchaseOrderSplitResultDTO dto = new PurchaseOrderSplitResultDTO();
        PurchaseOrdersSplitResult purchaseOrdersSplitResult = (PurchaseOrdersSplitResult) o;
        dto = CommonDataUtil.getModelMapper().map(o, PurchaseOrderSplitResultDTO.class);
        return dto;
    }

    private PurchaseOrderSplitDataDTO convertToObjectResultDetailDto(Object o) {
        PurchaseOrderSplitDataDTO dto = new PurchaseOrderSplitDataDTO();
        PurchaseOrdersSplitData purchaseOrdersSplitData = (PurchaseOrdersSplitData) o;
        dto = CommonDataUtil.getModelMapper().map(o, PurchaseOrderSplitDataDTO.class);
        return dto;
    }

    @Override
    public PurchaseOrderDataPageDTO getPurchaseOrdersSplitData(Integer id, Integer page, Integer limit) {
        Optional<PurchaseOrdersSplit> purchaseOrdersSplit = purchaseOrdersSplitRepository.findById(id);
        if (purchaseOrdersSplit.isPresent()) {
            PurchaseOrdersSplit purchaseOrders = purchaseOrdersSplit.get();
            PurchaseOrderDataPageDTO data = CommonDataUtil.getModelMapper().map(purchaseOrders, PurchaseOrderDataPageDTO.class);
            Pageable pageable = PageRequestUtil.genPageRequest(page, limit, Sort.Direction.DESC, "sku");
            Page<PurchaseOrdersSplitData> pagePurchaseOrdersSplitData = purchaseOrdersSplitDataRepository.findByPurchaseOrdersSplit(purchaseOrders, pageable);
            Page<PurchaseOrderSplitDataDTO> pagePurchaseOrderSplitDataDTO = pagePurchaseOrdersSplitData.map(this::convertToObjectDto);
            data.setPurchaseOrderSplitDataDTO(pagePurchaseOrderSplitDataDTO);
            return data;
        }
        return null;
    }

    @Override
    public PurchaseOrderResultPageDTO getPurchaseOrdersSplitResult(Integer id, Integer page, Integer limit) {
        Optional<PurchaseOrdersSplit> purchaseOrdersSplit = purchaseOrdersSplitRepository.findById(id);
        if (purchaseOrdersSplit.isPresent()) {
            PurchaseOrdersSplit purchaseOrders = purchaseOrdersSplit.get();
            PurchaseOrderResultPageDTO data = CommonDataUtil.getModelMapper().map(purchaseOrders, PurchaseOrderResultPageDTO.class);
            Pageable pageable = PageRequestUtil.genPageRequest(page, limit, Sort.Direction.DESC, "vendor");
            Page<PurchaseOrdersSplitResult> pagePurchaseOrdersSplitResult = purchaseOrdersSplitResultRepository.findByPurchaseOrdersSplit(purchaseOrders, pageable);
            Page<PurchaseOrderSplitResultDTO> pagePurchaseOrderSplitResultDTO = pagePurchaseOrdersSplitResult.map(this::convertToObjectResultDto);
            data.setPurchaseOrderSplitResultDTO(pagePurchaseOrderSplitResultDTO);
            return data;
        }
        return null;
    }

    @Override
    public PurchaseOrderSplitResultDetailsDTO getPurchaseOrdersSplitResultDetail(Integer id, Integer page, Integer limit) {
        Pageable pageable = PageRequestUtil.genPageRequest(page, limit, Sort.Direction.DESC, "sku");
        PurchaseOrdersSplitResult purchaseOrdersSplitResult = purchaseOrdersSplitResultRepository.findById(id).get();
        PurchaseOrderSplitResultDetailsDTO data = CommonDataUtil.getModelMapper().map(purchaseOrdersSplitResult, PurchaseOrderSplitResultDetailsDTO.class);
        Page<PurchaseOrdersSplitData> pagePurchaseOrdersSplitData = purchaseOrdersSplitDataRepository.findByPurchaseOrdersSplitResult(purchaseOrdersSplitResult, pageable);
        Page<PurchaseOrderSplitDataDTO> pagePurchaseOrderSplitResultDetailsDTO = pagePurchaseOrdersSplitData.map(this::convertToObjectResultDetailDto);
        data.setPurchaseOrderSplitDataDTO(pagePurchaseOrderSplitResultDetailsDTO);
        return data;
    }

    @Override
    public Page<PurchaseOrdersMainSplitDTO> getAll(Integer page, Integer limit) {
        Pageable pageable = PageRequestUtil.genPageRequest(page, limit, Sort.Direction.DESC, "createdDate");
        Page<PurchaseOrdersSplit> data = purchaseOrdersSplitRepository.findAll(pageable);
        return data.map(item -> CommonDataUtil.getModelMapper().map(item, PurchaseOrdersMainSplitDTO.class));
    }

    @Override
    public String getNameFile(Integer id) {
        PurchaseOrdersSplitResult purchaseOrdersSplitResult = purchaseOrdersSplitResultRepository.findById(id).get();
        return purchaseOrdersSplitResult.getOrderNo();
    }

    @Override
    public boolean removePurchaseOrdersSplit(List<Integer> listPurchaseOrderId, String userName) {
        try {
            listPurchaseOrderId.stream().forEach(i -> {
                Optional<PurchaseOrdersSplit> oPurchaseOrder = purchaseOrdersSplitRepository.findById(i);
                if (oPurchaseOrder.isPresent()) {
                    PurchaseOrdersSplit purchaseOrders = oPurchaseOrder.get();
                    purchaseOrders.setStatus(3);
                    purchaseOrders.setDeletedDate(new Date().toInstant());
                    purchaseOrders.setDeletedBy(userName);
                    purchaseOrdersSplitRepository.saveAndFlush(purchaseOrders);
                }
            });
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public PurchaseOrdersSplit splitPurchaseOrder(Integer id) {
        try {
            PurchaseOrdersSplit purchaseOrdersSplit = purchaseOrdersSplitRepository.findById(id).get();
            if (purchaseOrdersSplit.getPurchaseOrdersSplitResult().size() > 0) {
                throw new BusinessException(String.format("This root file already split."));
            }
            purchaseOrdersSplit.setStatus(2);
            List<PurchaseOrdersSplitResult> purchaseOrdersSplitResultList = new ArrayList<>();
            Set<PurchaseOrdersSplitData> purchaseOrdersSplitDataSet = new HashSet<>();
            Map<String, List<PurchaseOrdersSplitData>> mapPOSplit = new HashMap<>();
            List<String> keymapPO = new ArrayList<>();
            keymapPO = purchaseOrdersSplit.getPurchaseOrdersSplitData().stream().map(k -> {
                String key =k.getCountry() + KEY_UPLOAD +k.getVendor() + KEY_UPLOAD + k.getFulfillmentCenter() + KEY_UPLOAD + k.getShipDate();
                return key;
            }).collect(Collectors.toList()).stream().distinct().collect(Collectors.toList());
            for (String element : keymapPO) {
                List<PurchaseOrdersSplitData> listPurchaseOrdersSplitData = new ArrayList<>();
                purchaseOrdersSplit.getPurchaseOrdersSplitData().stream().filter(k -> {
                    String key =k.getCountry() + KEY_UPLOAD + k.getVendor() + KEY_UPLOAD + k.getFulfillmentCenter() + KEY_UPLOAD + k.getShipDate();
                    if (key.equals(element)) {
                        return true;
                    } else {
                        return false;
                    }
                }).forEach(m -> {
                    PurchaseOrdersSplitData PO = new PurchaseOrdersSplitData();
                    CommonDataUtil.getModelMapper().map(m, PO);
                    listPurchaseOrdersSplitData.add(PO);
                });
                mapPOSplit.put(element, listPurchaseOrdersSplitData);
            }
            mapPOSplit.entrySet().stream().forEach(k -> {
                String key = k.getKey();
                String[] array = key.split(KEY_UPLOAD);
                String country = array[0];
                String vendor = array[1];
                String fulfillmentCenter = array[2];
                String shipDate = array[3];
                PurchaseOrdersSplitResult purchaseOrdersSplitResult = new PurchaseOrdersSplitResult();
                Long maxNumberOrderNo = purchaseOrdersSplitResultRepository.findMaxNumberOrderNo(vendor);
                Set<PurchaseOrdersSplitData> purchaseOrdersSplitData = k.getValue().stream().map(item -> {
                    PurchaseOrdersSplitData purchaseOrdersSplitData1 = new PurchaseOrdersSplitData();
                    CommonDataUtil.getModelMapper().map(item, purchaseOrdersSplitData1);
                    purchaseOrdersSplitData1.setPurchaseOrdersSplitResult(purchaseOrdersSplitResult);
                    purchaseOrdersSplitDataSet.add(purchaseOrdersSplitData1);
                    return purchaseOrdersSplitData1;
                }).collect(Collectors.toSet());
                String strSo = purchaseOrdersSplitData.stream().map(i -> i.getSaleOrder()).distinct().collect(Collectors.joining(", "));
                purchaseOrdersSplitResult.setVendor(vendor);
                purchaseOrdersSplitResult.setFulfillmentCenter(fulfillmentCenter);
                purchaseOrdersSplitResult.setShipDate(DateUtils.convertStringLocalDate(shipDate));
                purchaseOrdersSplitResult.setSaleOrder(strSo);
                String year = String.valueOf(new Date().getYear());
                year = year.substring(1, 3);
                //create new orderNo
                String orderNo = country+fulfillmentCenter + "DI" + vendor + year + String.format("%04d", maxNumberOrderNo + 1);
                purchaseOrdersSplitResult.setOrderNo(orderNo);
                purchaseOrdersSplitResult.setNumberOrderNo((int) (maxNumberOrderNo + 1));
                purchaseOrdersSplitResult.setTotalQuantity(purchaseOrdersSplitData.stream().map(x -> Objects.isNull(x.getQtyOrdered()) ? 0 : x.getQtyOrdered()).reduce(0L, Long::sum));
                purchaseOrdersSplitResult.setTotalAmount(purchaseOrdersSplitData.stream().map(x -> Objects.isNull(x.getAmount()) ? 0 : x.getAmount()).reduce(0.0, Double::sum));
                purchaseOrdersSplitResultList.add(purchaseOrdersSplitResult);
            });
            purchaseOrdersSplit.setPurchaseOrdersSplitResult(purchaseOrdersSplitResultList.stream().collect(Collectors.toSet()));
            purchaseOrdersSplit.setPurchaseOrdersSplitData(purchaseOrdersSplitDataSet);
            purchaseOrdersSplitRepository.saveAndFlush(purchaseOrdersSplit);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
        return null;
    }


    private void writeHeaderLine(XSSFWorkbook workbook, XSSFSheet sheet) {

        Row row = sheet.createRow(0);
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(11);
        style.setFont(font);
        byte[] rgb = new byte[3];
        rgb[0] = (byte) 226; // red
        rgb[1] = (byte) 239; // green
        rgb[2] = (byte) 218; // blue
        XSSFColor myColor = new XSSFColor(rgb);
        style.setFillForegroundColor(myColor);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        createCell(sheet, row, 0, "PO number", style);
        createCell(sheet, row, 1, "SKU", style);
        createCell(sheet, row, 2, "ASIN", style);
        createCell(sheet, row, 3, "Product Name", style);
        createCell(sheet, row, 4, "Quantity Ordered", style);
        createCell(sheet, row, 5, "Ship date", style);
        createCell(sheet, row, 6, "Fulfillment center", style);
        createCell(sheet, row, 7, "Country", style);
        createCell(sheet, row, 8, "Vendor", style);
        createCell(sheet, row, 9, "MTS", style);
        createCell(sheet, row, 10, "Unit Price", style);
        createCell(sheet, row, 11, "Amount", style);
        createCell(sheet, row, 12, "CBM", style);
        createCell(sheet, row, 13, "Net Weight", style);
        createCell(sheet, row, 14, "Gross Weight", style);
        createCell(sheet, row, 15, "Total Box", style);
        createCell(sheet, row, 16, "PCS/CTN", style);
    }

    private void createCell(XSSFSheet sheet, Row row, int columnCount, Object valueOfCell, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (valueOfCell instanceof Integer) {
            cell.setCellValue((Integer) valueOfCell);
        } else if (valueOfCell instanceof Long) {
            cell.setCellValue((Long) valueOfCell);
        } else if (valueOfCell instanceof Double) {
            cell.setCellValue((Double) valueOfCell);
        } else if (valueOfCell instanceof String) {
            cell.setCellValue((String) valueOfCell);
        } else if (valueOfCell instanceof LocalDate) {
            cell.setCellValue((LocalDate) valueOfCell);
        } else {
            cell.setCellValue((Boolean) valueOfCell);
        }
        cell.setCellStyle(style);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
    }

    public Workbook generateExcelFile(Integer id, XSSFWorkbook workbook) throws IOException {
        PurchaseOrdersSplitResult purchaseOrdersSplitResult = purchaseOrdersSplitResultRepository.findById(id).get();
        List<PurchaseOrdersSplitData> purchaseOrdersSplitDataList = purchaseOrdersSplitDataRepository.findAllByPurchaseOrdersSplitResult(purchaseOrdersSplitResult);
        XSSFSheet sheet = workbook.createSheet(purchaseOrdersSplitResult.getOrderNo());
        int rowCount = 1;
        writeHeaderLine(workbook, sheet);
        CellStyle style = workbook.createCellStyle();
        XSSFCellStyle styleDate = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        styleDate.setDataFormat(createHelper.createDataFormat().getFormat("d-mmm-yy"));
        for (PurchaseOrdersSplitData record : purchaseOrdersSplitDataList) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(sheet, row, columnCount++, record.getSaleOrder(), style);
            createCell(sheet, row, columnCount++, record.getSku(), style);
            createCell(sheet, row, columnCount++, record.getaSin(), style);
            createCell(sheet, row, columnCount++, record.getProductName(), style);
            createCell(sheet, row, columnCount++, record.getQtyOrdered(), style);
            createCell(sheet, row, columnCount++, record.getShipDate(), styleDate);
            createCell(sheet, row, columnCount++, record.getFulfillmentCenter(), style);
            createCell(sheet, row, columnCount++, record.getCountry(), style);
            createCell(sheet, row, columnCount++, record.getVendor(), style);
            createCell(sheet, row, columnCount++, record.getMakeToStock(), style);
            createCell(sheet, row, columnCount++, record.getUnitCost(), style);
            createCell(sheet, row, columnCount++, record.getAmount(), style);
            createCell(sheet, row, columnCount++, record.getCbm(), style);
            createCell(sheet, row, columnCount++, record.getNetWeight(), style);
            createCell(sheet, row, columnCount++, record.getGrossWeight(), style);
            createCell(sheet, row, columnCount++, record.getTotalBox(), style);
            createCell(sheet, row, columnCount++, record.getPcs(), style);
        }
        return workbook;
    }

    @Override
    public void export(String filename, Integer id) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        generateExcelFile(id, workbook);
        FileOutputStream fos = new FileOutputStream(filename);
        workbook.write(fos);
        fos.close();
    }
}
