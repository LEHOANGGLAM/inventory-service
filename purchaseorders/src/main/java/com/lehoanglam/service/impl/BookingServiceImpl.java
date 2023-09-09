package com.yes4all.service.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtils;
import com.yes4all.common.utils.PageRequestUtil;
import com.yes4all.common.utils.ResultUploadPackingListDetail;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.*;
import com.yes4all.domain.model.*;
import com.yes4all.repository.*;
import com.yes4all.service.BookingService;
import net.bytebuddy.implementation.bytecode.Throw;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.yes4all.common.constants.Constant.MODULE_BOOKING;


/**
 * Service Implementation for managing {@link PurchaseOrders}.
 */
@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);
    private static final String KEY_UPLOAD = ";";
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ResourceRepository resourceRepository;

    private String API_VENDOR = "vendors/detail/";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingProformaInvoiceRepository bookingProformaInvoiceRepository;
    @Autowired
    private BookingPurchaseOrderRepository bookingPurchaseOrderRepository;
    @Autowired
    private PurchaseOrdersDetailRepository purchaseOrdersDetailRepository;
    @Autowired
    private PurchaseOrdersRepository purchaseOrdersRepository;
    @Autowired
    private BookingPackingListRepository bookingPackingListRepository;

    @Autowired
    private CommercialInvoiceRepository commercialInvoiceRepository;
    @Autowired
    private BookingPackingListDetailRepository bookingPackingListDetailRepository;

    @Value("${attribute.host.url_pims}")
    private String linkPIMS;
    @Autowired
    private ProformaInvoiceRepository proformaInvoiceRepository;
    @Autowired
    private ProformaInvoiceDetailRepository proformaInvoiceDetailRepository;

    @Override
    public BookingDTO createBooking(BookingDTO bookingDetailsDTO) {
        try {
            Booking booking = new Booking();
            BeanUtils.copyProperties(bookingDetailsDTO, booking);
            Set<BookingPurchaseOrder> bookingPurchaseOrderSet = bookingDetailsDTO.getBookingPurchaseOrderDTO().stream().map(item -> {
                BookingPurchaseOrder detail = new BookingPurchaseOrder();
                BeanUtils.copyProperties(item, detail);
                return detail;
            }).collect(Collectors.toSet());
            booking.setBookingPurchaseOrder(bookingPurchaseOrderSet);
            Set<BookingProformaInvoice> bookingProformaInvoice = bookingDetailsDTO.getBookingProformaInvoiceMainDTO().stream().map(item -> {
                BookingProformaInvoice detail = new BookingProformaInvoice();
                BeanUtils.copyProperties(item, detail);
                if (item.getBookingPackingListDTO() != null) {
                    BookingPackingList bookingPackingList = new BookingPackingList();
                    Set<BookingPackingListDetail> bookingPackingListDetailSet = new HashSet<>();
                    BeanUtils.copyProperties(item.getBookingPackingListDTO(), bookingPackingList);
                    if (item.getBookingPackingListDTO() != null) {
                        bookingPackingListDetailSet = item.getBookingPackingListDTO().getBookingPackingListDetailsDTO().stream().map(k -> {
                            BookingPackingListDetail bookingPackingListDetail = new BookingPackingListDetail();
                            BeanUtils.copyProperties(k, bookingPackingListDetail);
                            return bookingPackingListDetail;
                        }).collect(Collectors.toSet());
                        bookingPackingList.setBookingPackingListDetail(bookingPackingListDetailSet);
                    }
                    detail.setBookingPackingList(bookingPackingList);
                }
                Optional<ProformaInvoice> proformaInvoice = proformaInvoiceRepository.findByOrderNoAndIsDeleted(item.getInvoiceNo(), false);
                if (!proformaInvoice.isPresent()) {
                    throw new BusinessException(String.format("Proforma Invoice not exits."));
                }
                return detail;
            }).collect(Collectors.toSet());
            booking.setBookingProformaInvoice(bookingProformaInvoice);
            Set<BookingPurchaseOrderLocation> bookingPurchaseOrderLocation = bookingDetailsDTO.getBookingPurchaseOrderLocationDTO().stream().map(item -> {
                BookingPurchaseOrderLocation detail = new BookingPurchaseOrderLocation();
                BeanUtils.copyProperties(item, detail);
                return detail;
            }).collect(Collectors.toSet());
            booking.setBookingPurchaseOrderLocation(bookingPurchaseOrderLocation);
            bookingRepository.saveAndFlush(booking);
            return mappingEntityToDto(booking, BookingDTO.class);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }

    }

    @Override
    public BookingDTO completedBooking(Integer id) {

        Optional<Booking> oBooking = bookingRepository.findById(id);
        if (oBooking.isPresent()) {
            Booking booking=oBooking.get();
            Set<BookingProformaInvoice> bookingProformaInvoices = booking.getBookingProformaInvoice();
            Map<Integer, Integer> bookingPackingListStatus = new HashMap<>();
            Map<Integer, Integer> bookingCIStatus = new HashMap<>();
            bookingProformaInvoices.parallelStream().forEach(item -> {
                bookingPackingListStatus.put(item.getBookingPackingList().getId(), item.getBookingPackingList().getStatus());
                if(item.getBookingPackingList().getCommercialInvoice()!=null) {
                    bookingCIStatus.put(item.getBookingPackingList().getCommercialInvoice().getId(), item.getBookingPackingList().getCommercialInvoice().getStatus());
                }else{
                    bookingCIStatus.put(-1,-1);
                }
            });

            boolean checkStatusPackingList = true;
            for (Map.Entry<Integer, Integer> entry : bookingPackingListStatus.entrySet()) {
                if (entry.getValue()!=2 ) {
                    checkStatusPackingList = false;
                }
            }
            boolean checkStatusCI = true;
            for (Map.Entry<Integer, Integer> entry : bookingCIStatus.entrySet()) {
                if (entry.getValue()!=2 ) {
                    checkStatusCI = true;
                }
            }
            // if all status packing list and CI equal confirm  then status booking change completed
            if (checkStatusPackingList && checkStatusCI) {
                booking.setStatus(GlobalConstant.STATUS_BOOKING_CONFIRM_PACKING_LIST_AND_CI);
                bookingRepository.saveAndFlush(booking);
            }
            return CommonDataUtil.getModelMapper().map(booking, BookingDTO.class);

        }
        return null;
    }

    @Override
    public BookingPackingListDTO submitPackingList(BookingPackingListDTO bookingPackingListDTO) {
        try {
            Optional<BookingPackingList> oBookingPackingList = bookingPackingListRepository.findById(bookingPackingListDTO.getId());
            if (oBookingPackingList.isPresent()) {
                BookingPackingList bookingPackingList = oBookingPackingList.get();
                BeanUtils.copyProperties(bookingPackingListDTO, bookingPackingList);
                Set<BookingPackingListDetail> detailSetPrevious = bookingPackingList.getBookingPackingListDetail();
                Set<BookingPackingListDetail> detailSet = bookingPackingListDTO.getBookingPackingListDetailsDTO().parallelStream().map(item -> {
                    BookingPackingListDetail bookingPackingListDetail;
                    bookingPackingListDetail = CommonDataUtil.getModelMapper().map(item, BookingPackingListDetail.class);
                    BookingPackingListDetail detailPrevious = detailSetPrevious.stream().filter(k -> k.getId() == bookingPackingListDetail.getId()).findFirst().orElse(null);
                    if (detailPrevious != null) {
                        bookingPackingListDetail.setGrossWeightPrevious(detailPrevious.getGrossWeight());
                        bookingPackingListDetail.setNetWeightPrevious(detailPrevious.getNetWeight());
                        bookingPackingListDetail.setQtyEachCartonPrevious(detailPrevious.getQtyEachCarton());
                        bookingPackingListDetail.setQuantityPrevious(detailPrevious.getQuantity());
                        bookingPackingListDetail.setTotalCartonPrevious(detailPrevious.getTotalCarton());
                        bookingPackingListDetail.setCbmPrevious(detailPrevious.getCbm());
                    }
                    return bookingPackingListDetail;
                }).collect(Collectors.toSet());
                bookingPackingList.setBookingPackingListDetail(detailSet);
                bookingPackingList.setStatus(1);
                bookingPackingListRepository.saveAndFlush(bookingPackingList);
                Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findByOrderNoAndIsDeleted(bookingPackingList.getBookingProformaInvoice().getProformaInvoiceNo(), false);
                if (!oProformaInvoice.isPresent()) {
                    throw new BusinessException("Proforma Invoice not exists");
                } else {
                    ProformaInvoice proformaInvoice = oProformaInvoice.get();
                    proformaInvoice.setStatus(GlobalConstant.STATUS_PI_NEGOTIATING);
                    proformaInvoiceRepository.saveAndFlush(proformaInvoice);
                }
                return CommonDataUtil.getModelMapper().map(bookingPackingList, BookingPackingListDTO.class);
            }
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());

        }
        return null;

    }

    @Override
    public Integer confirmPackingList(BookingPackingListDTO bookingPackingListDTO) {
        try {
            Optional<BookingPackingList> oBookingPackingList = bookingPackingListRepository.findById(bookingPackingListDTO.getId());
            if (oBookingPackingList.isPresent()) {
                BookingPackingList bookingPackingList = oBookingPackingList.get();
                Booking booking = bookingPackingList.getBookingProformaInvoice().getBooking();
                Set<BookingProformaInvoice> bookingProformaInvoices = booking.getBookingProformaInvoice();
                Map<Integer, Integer> bookingPackingListStatus = new HashMap<>();
                bookingProformaInvoices.parallelStream().forEach(item -> {
                    bookingPackingListStatus.put(item.getBookingPackingList().getId(), item.getBookingPackingList().getStatus());
                });
                // if all packing list status=confirm=2 then status booking change loaded
                boolean checkStatus = false;
                for (Map.Entry<Integer, Integer> entry : bookingPackingListStatus.entrySet()) {
                    if (entry.getKey() != bookingPackingList.getId() && entry.getValue() != 2) {
                        checkStatus = true;
                    }
                }
                if (checkStatus) {
                    booking.setStatus(GlobalConstant.STATUS_BOOKING_CONFIRM_PACKING_LIST);
                    bookingRepository.saveAndFlush(booking);
                }
                BeanUtils.copyProperties(bookingPackingListDTO, bookingPackingList);
                Set<BookingPackingListDetail> detailSetPrevious = bookingPackingList.getBookingPackingListDetail();
                Set<BookingPackingListDetail> detailSet = bookingPackingListDTO.getBookingPackingListDetailsDTO().parallelStream().map(item -> {
                    BookingPackingListDetail bookingPackingListDetail;
                    bookingPackingListDetail = CommonDataUtil.getModelMapper().map(item, BookingPackingListDetail.class);
                    BookingPackingListDetail detailPrevious = detailSetPrevious.stream().filter(k -> k.getId() == bookingPackingListDetail.getId()).findFirst().orElse(null);
                    if (detailPrevious != null) {
                        //set value previous of detail booking packing list
                        bookingPackingListDetail.setGrossWeightPrevious(detailPrevious.getGrossWeight());
                        bookingPackingListDetail.setNetWeightPrevious(detailPrevious.getNetWeight());
                        bookingPackingListDetail.setQtyEachCartonPrevious(detailPrevious.getQtyEachCarton());
                        bookingPackingListDetail.setQuantityPrevious(detailPrevious.getQuantity());
                        bookingPackingListDetail.setTotalCartonPrevious(detailPrevious.getTotalCarton());
                        bookingPackingListDetail.setCbmPrevious(detailPrevious.getCbm());
                    }
                    return bookingPackingListDetail;
                }).collect(Collectors.toSet());
                bookingPackingList.setBookingPackingListDetail(detailSet);
                if (bookingPackingList.getStatus() == 2) {
                    throw new BusinessException("Packing list already confirm.");
                }
                bookingPackingList.setStatus(2);
                bookingPackingListRepository.saveAndFlush(bookingPackingList);
                //create CI with detail Packing list
                CommercialInvoice commercialInvoice = new CommercialInvoice();
                commercialInvoice.setSupplier(bookingPackingList.getSupplier());
                BookingProformaInvoice bookingProformaInvoice = bookingPackingList.getBookingProformaInvoice();
                Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findByOrderNoAndIsDeleted(bookingProformaInvoice.getProformaInvoiceNo(), false);
                if (oProformaInvoice.isPresent()) {
                    ProformaInvoice proformaInvoice = oProformaInvoice.get();
                    PurchaseOrders purchaseOrders = proformaInvoice.getPurchaseOrders();
                    purchaseOrders.setStatus(GlobalConstant.STATUS_PO_PACKING_LIST_CREATED);
                    purchaseOrdersRepository.saveAndFlush(purchaseOrders);
                    commercialInvoice.setProformaInvoice(proformaInvoice);
                    commercialInvoice.setInvoiceNo(bookingProformaInvoice.getProformaInvoiceNo());
                    commercialInvoice.setBookingPackingList(bookingPackingList);
                    commercialInvoice.setStatus(GlobalConstant.STATUS_CI_NEW);
                    Set<ProformaInvoiceDetail> proformaInvoiceDetailSet = proformaInvoice.getProformaInvoiceDetail();
                    BeanUtils.copyProperties(proformaInvoice, commercialInvoice);
                    commercialInvoice.setId(null);
                    Set<CommercialInvoiceDetail> commercialInvoiceDetailSet = new HashSet<>();
                    // copy data detail packing list to detail Commercial Invoice
                    detailSet.stream().forEach(packingList -> {
                        if(packingList.getQuantity()>0) {
                            CommercialInvoiceDetail commercialInvoiceDetail = new CommercialInvoiceDetail();
                            ProformaInvoiceDetail proformaInvoiceDetail = proformaInvoiceDetailSet.stream().filter(pi -> pi.getSku().equals(packingList.getSku()) && pi.getaSin().equals(packingList.getaSin()) && pi.getSku().equals(packingList.getSku()) && pi.getFromSo().equals(packingList.getPoNumber())).findFirst().orElse(null);
                            if (proformaInvoiceDetail == null) {
                                throw new BusinessException("Missing data!");
                            }
                            commercialInvoiceDetail.setSku(packingList.getSku());
                            commercialInvoiceDetail.setaSin(packingList.getaSin());
                            commercialInvoiceDetail.setFromSo(packingList.getPoNumber());
                            commercialInvoiceDetail.setProductTitle(packingList.getTitle());
                            commercialInvoiceDetail.setQty(packingList.getQuantity());
                            commercialInvoiceDetail.setUnitPrice(proformaInvoiceDetail.getUnitPrice());
                            commercialInvoiceDetail.setAmount(commercialInvoiceDetail.getQty() * commercialInvoiceDetail.getUnitPrice());
                            commercialInvoiceDetailSet.add(commercialInvoiceDetail);
                        }
                    });
                    commercialInvoice.setAmount(commercialInvoiceDetailSet.stream().filter(i -> !i.isDeleted()).map(x -> Objects.isNull(x.getAmount()) ? 0 : x.getAmount()).reduce(0.0, Double::sum));
                    commercialInvoice.setCommercialInvoiceDetail(commercialInvoiceDetailSet);
                    //  proformaInvoiceRepository.saveAndFlush(proformaInvoice);
                } else {
                    throw new BusinessException("Proforma Invoice not exists!");
                }

                commercialInvoiceRepository.saveAndFlush(commercialInvoice);
                return commercialInvoice.getId();
            }
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
        return null;
    }

    @Override
    public BookingDetailsDTO getBookingDetailsDTO(BookingPageGetDetailDTO request) {
        Optional<Booking> oBooking = bookingRepository.findById(request.getId());
        if (oBooking.isPresent()) {
            Booking booking = oBooking.get();
            BookingDetailsDTO data = CommonDataUtil.getModelMapper().map(booking, BookingDetailsDTO.class);
            Pageable pageablePO = PageRequestUtil.genPageRequest(request.getPageProduct(), request.getSizeProduct(), Sort.Direction.DESC, "sku");
            Pageable pageablePI = PageRequestUtil.genPageRequest(request.getPagePI(), request.getSizePI(), Sort.Direction.DESC, "id");
            Pageable pageableResource = PageRequestUtil.genPageRequest(request.getPageResource(), request.getSizeResource(), Sort.Direction.DESC, "uploadDate");
            //
            Page<BookingPurchaseOrder> pagePODetail = bookingPurchaseOrderRepository.findAllByBooking(booking, pageablePO);
            Page<BookingProformaInvoice> pagePIDetail = bookingProformaInvoiceRepository.findAllByBooking(booking, pageablePI);
            Page<Resource> pageResourceDetail = resourceRepository.findByFileTypeAndBookingId(MODULE_BOOKING, booking.getId(), pageableResource);
            Page<BookingPurchaseOrderDTO> pageBookingPurchaseOrderDTO = pagePODetail.map(this::convertToObjectBookingPurchaseOrderDTO);
            Page<BookingProformaInvoiceMainDTO> pageBookingProformaInvoiceMainDTO = pagePIDetail.map(this::convertToObjectBookingProformaInvoiceMainDTO);
            Page<ResourceDTO> pageResourceDTO = pageResourceDetail.map(this::convertToObjectResourceDTO);
            data.setBookingProformaInvoice(pageBookingProformaInvoiceMainDTO);
            data.setBookingPurchaseOrder(pageBookingPurchaseOrderDTO);
            data.setResource(pageResourceDTO);
            return data;
        }
        return null;
    }

    @Override
    public Integer updateBooking(BookingPackingListDTO request) {
        Optional<Booking> oBooking=bookingRepository.findById(request.getId());
            if(oBooking.isPresent()){
                Booking booking=oBooking.get();
                booking.setCds(request.getCds());
                bookingRepository.saveAndFlush(booking);
                return booking.getId();
            }
            return null;
    }

    @Override
    public Page<BookingMainDTO> listingBookingWithCondition(Integer page, Integer limit, Map<String, String> filterParams) {
        Pageable pageable = PageRequestUtil.genPageRequest(page, limit, Sort.Direction.DESC, "updated_at");
        Page<Booking> data = bookingRepository.findByCondition(filterParams.get("booking"), filterParams.get("poAmazon"), filterParams.get("masterPO"),filterParams.get("supplier"), pageable);
        return data.map(item -> mappingEntityToDto(item, BookingMainDTO.class));
    }

    @Override
    public BookingPackingListDTO getPackingListDetailsDTO(Integer id) {
        Optional<BookingPackingList> data = bookingPackingListRepository.findById(id);
        BookingPackingListDTO bookingPackingListDTO = new BookingPackingListDTO();
        if (data.isPresent()) {
            BookingPackingList bookingPackingList = data.get();
            BeanUtils.copyProperties(bookingPackingList, bookingPackingListDTO);
            Set<BookingPackingListDetailsDTO> detailSet = bookingPackingList.getBookingPackingListDetail().parallelStream().map(item -> {
                BookingPackingListDetailsDTO bookingPackingListDetailsDTO;
                bookingPackingListDetailsDTO = CommonDataUtil.getModelMapper().map(item, BookingPackingListDetailsDTO.class);
                return bookingPackingListDetailsDTO;
            }).collect(Collectors.toSet());
            bookingPackingListDTO.setBookingPackingListDetailsDTO(detailSet);
            return bookingPackingListDTO;
        }
        return null;
    }

//    @Override
//    public BookingPackingListPageDTO getPackingListDetailsPageDTO(Integer id, Integer page, Integer limit) {
//        Pageable pageable = PageRequestUtil.genPageRequest(page, limit, Sort.Direction.DESC, "id");
//        Optional<BookingPackingList> data = bookingPackingListRepository.findById(id);
//        BookingPackingListPageDTO bookingPackingListPageDTO = new BookingPackingListPageDTO();
//        if (data.isPresent()) {
//            BookingPackingList bookingPackingList = data.get();
//            Page<BookingPackingListDetail> pageBookingPackingListDetail = bookingPackingListDetailRepository.findAllByBookingPackingList(bookingPackingList, pageable);
//            Page<BookingPackingListDetailsDTO> pageBookingPackingListDetailDTO = pageBookingPackingListDetail.map(this::convertToObjectBookingPackingListDetailDTO);
//            BeanUtils.copyProperties(bookingPackingList, bookingPackingListPageDTO);
//            bookingPackingListPageDTO.setBookingPackingListDetailsDTO(pageBookingPackingListDetailDTO);
//            return bookingPackingListPageDTO;
//        }
//        return null;
//    }

    private ResourceDTO convertToObjectResourceDTO(Object o) {
        ResourceDTO dto = new ResourceDTO();
        dto = CommonDataUtil.getModelMapper().map(o, ResourceDTO.class);
        return dto;
    }

    private BookingProformaInvoiceMainDTO convertToObjectBookingProformaInvoiceMainDTO(Object o) {
        BookingProformaInvoiceMainDTO dto = new BookingProformaInvoiceMainDTO();
        BookingProformaInvoice bookingProformaInvoice = (BookingProformaInvoice) o;
        dto = CommonDataUtil.getModelMapper().map(o, BookingProformaInvoiceMainDTO.class);
        if (bookingProformaInvoice.getBookingPackingList() != null) {
            Set<BookingPackingListDetailsDTO> bookingPackingListDetailsDTOSet = new HashSet<>();
            BookingPackingList bookingPackingList = bookingProformaInvoice.getBookingPackingList();
            BookingPackingListDTO bookingPackingListDTO = new BookingPackingListDTO();
            dto.setBookingPackingListId(bookingPackingList.getId());
            dto.setBookingPackingListStatus(bookingPackingList.getStatus());
            Set<BookingPackingListDetail> bookingPackingListDetailSet = bookingPackingList.getBookingPackingListDetail();
            BeanUtils.copyProperties(bookingPackingList, bookingPackingListDTO);
            bookingPackingListDetailsDTOSet = bookingPackingListDetailSet.stream().map(item -> {
                BookingPackingListDetailsDTO bookingPackingListDetailsDTO = new BookingPackingListDetailsDTO();
                BeanUtils.copyProperties(item, bookingPackingListDetailsDTO);
                return bookingPackingListDetailsDTO;
            }).collect(Collectors.toSet());
            bookingPackingListDTO.setBookingPackingListDetailsDTO(bookingPackingListDetailsDTOSet);
            dto.setBookingPackingListDTO(bookingPackingListDTO);
        }
        return dto;
    }

    private BookingPurchaseOrderDTO convertToObjectBookingPurchaseOrderDTO(Object o) {
        BookingPurchaseOrderDTO dto = new BookingPurchaseOrderDTO();
        dto = CommonDataUtil.getModelMapper().map(o, BookingPurchaseOrderDTO.class);
        return dto;
    }

    private BookingPackingListDetailsDTO convertToObjectBookingPackingListDetailDTO(Object o) {
        BookingPackingListDetailsDTO dto = new BookingPackingListDetailsDTO();
        dto = CommonDataUtil.getModelMapper().map(o, BookingPackingListDetailsDTO.class);
        return dto;
    }

    private <T> T mappingEntityToDto(Booking booking, Class<T> clazz) {
        try {
            T dto = clazz.getDeclaredConstructor().newInstance();
            CommonDataUtil.getModelMapper().map(booking, dto);
            if (dto instanceof BookingMainDTO) {
                String fromSoStr = bookingPurchaseOrderRepository.findAllFromSOByBookingId(booking.getId());
                clazz.getMethod("setPOAmazon", String.class).invoke(dto, fromSoStr);
            }
            return dto;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return null;
        }
    }

    @Override
    public void export(String filename, Integer id) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        generateExcelFile(id, workbook);
        FileOutputStream fos = new FileOutputStream(filename);
        workbook.write(fos);
        fos.close();
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
        createCell(sheet, row, 0, "PROFORMA INVOICE NO.", style);
        createCell(sheet, row, 1, "PO", style);
        createCell(sheet, row, 2, "SKU", style);
        createCell(sheet, row, 3, "PRODUCT TITLE", style);
        createCell(sheet, row, 4, "ASIN", style);
        createCell(sheet, row, 5, "QUANTITY", style);
        createCell(sheet, row, 6, "QTY OF EACH CARTON", style);
        createCell(sheet, row, 7, "TOTAL CARTON", style);
        createCell(sheet, row, 8, "NET WEIGHT", style);
        createCell(sheet, row, 9, "GROSS WEIGHT", style);
        createCell(sheet, row, 10, "VOLUME", style);
        createCell(sheet, row, 11, "CONTAINER", style);
    }

    private void createCell(XSSFSheet sheet, Row row, int columnCount, Object valueOfCell, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (valueOfCell instanceof Integer) {
            cell.setCellValue((Integer) valueOfCell);
        } else if (valueOfCell instanceof Long) {
            cell.setCellValue((Long) valueOfCell);
        } else if (valueOfCell instanceof String) {
            cell.setCellValue((String) valueOfCell);
        } else if (valueOfCell instanceof LocalDate) {
            cell.setCellValue((LocalDate) valueOfCell);
        } else if (valueOfCell instanceof Boolean) {
            cell.setCellValue((Boolean) valueOfCell);
        } else {
            cell.setCellValue((String) valueOfCell);
        }
        cell.setCellStyle(style);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
    }

    public Workbook generateExcelFile(Integer id, XSSFWorkbook workbook) {
        BookingPackingList bookingPackingList = bookingPackingListRepository.findById(id).get();
        XSSFSheet sheet = workbook.createSheet(bookingPackingList.getPoNumber());
        int rowCount = 1;
        writeHeaderLine(workbook, sheet);
        CellStyle style = workbook.createCellStyle();
        XSSFCellStyle styleDate = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        styleDate.setDataFormat(createHelper.createDataFormat().getFormat("d-mmm-yy"));
        for (BookingPackingListDetail record : bookingPackingList.getBookingPackingListDetail()) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(sheet, row, columnCount++, record.getProformaInvoiceNo(), style);
            createCell(sheet, row, columnCount++, record.getPoNumber(), style);
            createCell(sheet, row, columnCount++, record.getSku(), style);
            createCell(sheet, row, columnCount++, record.getTitle(), style);
            createCell(sheet, row, columnCount++, record.getaSin(), style);
            createCell(sheet, row, columnCount++, record.getQuantity(), style);
            createCell(sheet, row, columnCount++, record.getQtyEachCarton(), style);
            createCell(sheet, row, columnCount++, record.getTotalCarton(), style);
            createCell(sheet, row, columnCount++, record.getNetWeight(), style);
            createCell(sheet, row, columnCount++, record.getGrossWeight(), style);
            createCell(sheet, row, columnCount++, record.getCbm(), style);
            createCell(sheet, row, columnCount++, record.getContainer(), style);
        }
        return workbook;
    }

    @Override
    public Booking save(MultipartFile file, String userId) {
        try (
            CSVReader br = new CSVReader(new InputStreamReader(file.getInputStream()));
        ) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                Booking booking = new Booking();
                Map<String, BookingProformaInvoice> mapPI = new HashMap<>();
                Map<String, BookingPackingList> mapPackingList = new HashMap<>();
                booking.setCreatedBy(userId);
                booking.setCreatedAt(new Date().toInstant());
                Map<String, String> listPO = new HashMap<>();
                Set<BookingPurchaseOrder> bookingPurchaseOrderSet = new HashSet<>();
                Set<BookingPurchaseOrderLocation> bookingPurchaseOrderLocationSet = new HashSet<>();
                Set<BookingProformaInvoice> bookingProformaInvoices = new HashSet<>();
                int row = 0;
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
                mapper.registerModule(new JavaTimeModule());
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                Optional<User> oUser = userRepository.findOneByLogin(userId);
                String company = "", fax = "", address = "", telephone = "";
                if (oUser.isPresent()) {
                    User user = oUser.get();
                    String vendorCode = user.getVendor();
                    if (vendorCode == null) {
                        throw new BusinessException("Vendor code not define!");
                    }
                    ResponseEntity<String> response = restTemplate.getForEntity(linkPIMS + API_VENDOR + vendorCode, String.class);
                    Gson g = new Gson();
                    VendorDetailDTO vendorDetailDTO = g.fromJson(response.getBody(), VendorDetailDTO.class);
                    if (vendorDetailDTO != null) {
                        company = vendorDetailDTO.getCompanyOwner();
                        address = vendorDetailDTO.getFactoryAddress();
                        telephone = vendorDetailDTO.getMainContactPhone();
                    } else {
                        throw new BusinessException("Vendor code not exists!");
                    }
                }
                String[] nextLine;
                while ((nextLine = br.readNext()) != null) {
                    BookingPurchaseOrder bookingPurchaseOrder = new BookingPurchaseOrder();
                    for (int i = 0; i < nextLine.length; i++) {
                        if (row == 1) {
                            switch (i) {
                                case 0:
                                    booking.setBookingConfirmation(nextLine[i]);
                                    break;
                                case 12:
                                    if (nextLine[i].startsWith("Master Booking:")) {
                                        booking.setContainer(nextLine[i].substring(15));
                                    }
                                    break;
                                case 38:
                                    booking.setPortOfLoading(nextLine[i]);
                                    break;
                                case 39:
                                    booking.setOriginEtd(DateUtils.convertStringLocalDateBooking((nextLine[i])));
                                    break;
                                case 40:
                                    booking.setFreightMode(nextLine[i]);
                                    break;
                                case 41:
                                    booking.setEstimatedDeliveryDate(DateUtils.convertStringLocalDateBooking((nextLine[i])));
                                    break;
                                case 42:
                                    booking.setPortOfDischarge(nextLine[i]);
                                    break;
                                case 43:
                                    booking.setDischargeEta(DateUtils.convertStringLocalDateBooking((nextLine[i])));
                                    break;
                                case 46:
                                    booking.setFcrNo(nextLine[i]);
                                    break;
                                case 47:
                                    if (nextLine[i].startsWith("Container Type & QTY :")) {
                                        booking.setContainer(nextLine[i].substring(22));
                                    }
                                    break;
                                case 50:
                                    booking.setShipToLocation(nextLine[i]);
                                    break;
                            }
                        }
                        if (row == 4 || row == 5) {
                            switch (i) {
                                case 3:
                                    bookingPurchaseOrder.setPoNumber(nextLine[i]);
                                    break;
                                case 4:
                                    bookingPurchaseOrder.setaSin(nextLine[i]);
                                    break;
                                case 5:
                                    bookingPurchaseOrder.setSku(nextLine[i]);
                                    break;
                                case 6:
                                    bookingPurchaseOrder.setQuantity(Long.valueOf(nextLine[i]));
                                    break;
                                case 7:
                                    bookingPurchaseOrder.setQuantityCtns(Long.valueOf(nextLine[i]));
                                    break;
                                case 11:
                                    if (nextLine[i].startsWith("Ship To Location:")) {
                                        bookingPurchaseOrder.setShipLocation(nextLine[i].substring(17));
                                    }
                                    break;
                            }
                        }
                        if (row > 7) {
                            switch (i) {
                                case 19:
                                    bookingPurchaseOrder.setPoNumber(nextLine[i]);
                                    break;
                                case 20:
                                    bookingPurchaseOrder.setaSin(nextLine[i]);
                                    break;
                                case 21:
                                    bookingPurchaseOrder.setSku(nextLine[i]);
                                    break;
                                case 22:
                                    bookingPurchaseOrder.setQuantity(Long.valueOf(nextLine[i]));
                                    break;
                                case 23:
                                    bookingPurchaseOrder.setQuantityCtns(Long.valueOf(nextLine[i]));
                                    break;
                            }
                        }

                    }
                    if (row == 4 || row == 5 || row > 7) {
                        bookingPurchaseOrderSet.add(bookingPurchaseOrder);
                    }
                    row++;
                }
                if (bookingPurchaseOrderSet.size() > 0) {
                    String finalCompany = company;
                    String finalAddress = address;
                    String finalTelephone = telephone;
                    bookingPurchaseOrderSet = bookingPurchaseOrderSet.stream().map(bookingPurchaseOrder -> {
                        Optional<PurchaseOrdersDetail> oPurchaseOrdersDetail = purchaseOrdersDetailRepository.findBySkuAndFromSoAndQtyOrderedAndPcsAndIsDeletedAndAsin(bookingPurchaseOrder.getSku(), bookingPurchaseOrder.getPoNumber(), bookingPurchaseOrder.getQuantity(), Math.toIntExact(bookingPurchaseOrder.getQuantityCtns()), false, bookingPurchaseOrder.getaSin());
                        if (oPurchaseOrdersDetail.isPresent()) {
                            //set value to purchase order detail
                            PurchaseOrdersDetail purchaseOrdersDetail = oPurchaseOrdersDetail.get();
                            bookingPurchaseOrder.setFobPrice(purchaseOrdersDetail.getUnitCost());
                            bookingPurchaseOrder.setCbm(purchaseOrdersDetail.getTotalVolume());
                            bookingPurchaseOrder.setGrossWeight(purchaseOrdersDetail.getTotalBoxCrossWeight());
                           //add list PO key <OrderNO,Location>
                            listPO.put(purchaseOrdersDetail.getPurchaseOrders().getPoNumber(), bookingPurchaseOrder.getShipLocation());
                            ProformaInvoice proformaInvoice = purchaseOrdersDetail.getPurchaseOrders().getProformaInvoice();
                            if (proformaInvoice != null) {
                                bookingPurchaseOrder.setSupplier(proformaInvoice.getSupplier());
                                String proformaInvoiceNo = proformaInvoice.getOrderNo();
                                Set<ProformaInvoiceDetail> proformaInvoiceDetailSet = proformaInvoice.getProformaInvoiceDetail();
                                // create packing list
                                BookingPackingListDetail bookingPackingListDetail = new BookingPackingListDetail();
                                bookingPackingListDetail.setPoNumber(purchaseOrdersDetail.getFromSo());
                                bookingPackingListDetail.setProformaInvoiceNo(proformaInvoiceNo);
                                bookingPackingListDetail.setSku(purchaseOrdersDetail.getSku());
                                bookingPackingListDetail.setaSin(purchaseOrdersDetail.getAsin());
                                bookingPackingListDetail.setTitle(purchaseOrdersDetail.getProductName());
                                bookingPackingListDetail.setQuantity(Math.toIntExact(purchaseOrdersDetail.getQtyOrdered()));
                                bookingPackingListDetail.setQuantityPrevious(Math.toIntExact(purchaseOrdersDetail.getQtyOrdered()));
                                bookingPackingListDetail.setQtyEachCarton(purchaseOrdersDetail.getPcs());
                                double totalCarton = 0;
                                if (purchaseOrdersDetail.getPcs() != 0) {
                                    totalCarton = Double.valueOf(purchaseOrdersDetail.getQtyOrdered() / purchaseOrdersDetail.getPcs());
                                }
                                bookingPackingListDetail.setCbm(purchaseOrdersDetail.getTotalVolume());
                                bookingPackingListDetail.setTotalCarton(totalCarton);
                                bookingPackingListDetail.setTotalCartonPrevious(totalCarton);
                                bookingPackingListDetail.setNetWeight(purchaseOrdersDetail.getTotalBoxNetWeight());
                                bookingPackingListDetail.setGrossWeight(purchaseOrdersDetail.getTotalBoxCrossWeight());
                                bookingPackingListDetail.setNetWeightPrevious(purchaseOrdersDetail.getTotalBoxNetWeight());
                                bookingPackingListDetail.setGrossWeightPrevious(purchaseOrdersDetail.getTotalBoxCrossWeight());

                                String poNumber = purchaseOrdersDetail.getPurchaseOrders().getPoNumber();
                                // check line data of must in PI
                                long count = proformaInvoiceDetailSet.parallelStream().filter(pi -> {
                                    if (pi.getFromSo().equals(bookingPackingListDetail.getPoNumber()) && pi.getSku().equals(bookingPackingListDetail.getSku()) && pi.getaSin().equals(bookingPackingListDetail.getaSin()) && pi.getQty().equals(bookingPackingListDetail.getQuantity())) {
                                        return true;
                                    }
                                    return false;
                                }).count();
                                if (count == 0) {
                                    throw new BusinessException(String.format("{Sku= %s ,Po Number= %s ,ASin= %s ,Quantity= %s  } not exists Proforma Invoice.", bookingPackingListDetail.getSku(), bookingPackingListDetail.getPoNumber(), bookingPackingListDetail.getaSin(), bookingPackingListDetail.getQuantity()));
                                }
                                //define booking packing list
                                if (mapPackingList.get(proformaInvoiceNo) == null) {
                                    BookingPackingList bookingPackingList = new BookingPackingList();
                                    bookingPackingList.setInvoice(proformaInvoiceNo);
                                    bookingPackingList.setDate(LocalDate.now());
                                    Set<BookingPackingListDetail> bookingPackingListDetails = new HashSet<>();
                                    bookingPackingListDetails.add(bookingPackingListDetail);
                                    bookingPackingList.setBookingPackingListDetail(bookingPackingListDetails);
                                    bookingPackingList.setPoNumber(poNumber);
                                    bookingPackingList.setStatus(0);
                                    bookingPackingList.setSoldToCompany(GlobalConstant.SOLD_TO_COMPANY);
                                    bookingPackingList.setSoldToAddress(GlobalConstant.SOLD_TO_ADDRESS);
                                    bookingPackingList.setSoldToTelephone(GlobalConstant.SOLD_TO_TELEPHONE);
                                    bookingPackingList.setSoldToFax(GlobalConstant.SOLD_TO_FAX);
                                    bookingPackingList.setFromCompany(finalCompany);
                                    bookingPackingList.setFromAddress(finalAddress);
                                    bookingPackingList.setFromTelephone(finalTelephone);
                                    bookingPackingList.setFromFax(fax);
                                    mapPackingList.put(proformaInvoiceNo, bookingPackingList);
                                } else {
                                    //add row to booking packing list detail
                                    BookingPackingList bookingPackingList = mapPackingList.get(proformaInvoiceNo);
                                    Set<BookingPackingListDetail> bookingPackingListDetails = bookingPackingList.getBookingPackingListDetail();
                                    bookingPackingListDetails.add(bookingPackingListDetail);
                                    bookingPackingList.setBookingPackingListDetail(bookingPackingListDetails);
                                    bookingPackingList.setStatus(0);
                                    String poNumberCurrent = bookingPackingList.getPoNumber() == null ? "" : bookingPackingList.getPoNumber();
                                    if (!poNumberCurrent.contains(poNumber)) {
                                        if (poNumber.length() == 0) {
                                            poNumberCurrent = poNumber;
                                        } else {
                                            poNumberCurrent = poNumberCurrent + "/" + poNumber;
                                        }
                                        bookingPackingList.setPoNumber(poNumberCurrent);
                                    }
                                    bookingPackingList.setFromCompany(finalCompany);
                                    bookingPackingList.setFromAddress(finalAddress);
                                    bookingPackingList.setFromTelephone(finalTelephone);
                                    bookingPackingList.setFromFax(fax);
                                    bookingPackingList.setSupplier(proformaInvoice.getSupplier());
                                    bookingPackingList.setSoldToCompany(GlobalConstant.SOLD_TO_COMPANY);
                                    bookingPackingList.setSoldToAddress(GlobalConstant.SOLD_TO_ADDRESS);
                                    bookingPackingList.setSoldToTelephone(GlobalConstant.SOLD_TO_TELEPHONE);
                                    bookingPackingList.setSoldToFax(GlobalConstant.SOLD_TO_FAX);
                                    proformaInvoiceDetailSet.parallelStream().forEach(item -> {
                                            long checkCount = bookingPackingListDetails.parallelStream().filter(packingList -> packingList.getPoNumber().equals(item.getFromSo()) && packingList.getSku().equals(item.getSku()) && packingList.getaSin().equals(item.getaSin()) && packingList.getQuantity().equals(item.getQty())).count();
                                            if (checkCount == 0) {
                                                throw new BusinessException(String.format("{Sku= %s ,Po Number= %s ,ASin= %s ,Quantity= %s  } missing data.", item.getSku(), item.getFromSo(), item.getaSin(), item.getQty()));
                                            }
                                        }
                                    );
                                    mapPackingList.put(proformaInvoiceNo, bookingPackingList);
                                }
                                //add data booking proforma invoice
                                BookingProformaInvoice bookingProformaInvoice = new BookingProformaInvoice();
                                if (mapPI.get(proformaInvoiceNo) != null) {
                                    bookingProformaInvoice = mapPI.get(proformaInvoiceNo);
                                }
                                bookingProformaInvoice.setCbm(purchaseOrdersDetail.getTotalVolume());
                                bookingProformaInvoice.setCtn(purchaseOrdersDetail.getPcs());
                                bookingProformaInvoice.setQuantity(purchaseOrdersDetail.getQtyOrdered());
                                bookingProformaInvoice.setShipDate(purchaseOrdersDetail.getShipDate());
                                bookingProformaInvoice.setProformaInvoiceNo(proformaInvoiceNo);
                                bookingProformaInvoice.setBookingPackingList(mapPackingList.get(proformaInvoiceNo));
                                String fromSo = bookingProformaInvoice.getPoAmazon() == null ? "" : bookingProformaInvoice.getPoAmazon();
                                if (!fromSo.contains(purchaseOrdersDetail.getFromSo())) {
                                    if (fromSo.length() == 0) {
                                        fromSo = purchaseOrdersDetail.getFromSo();
                                    } else {
                                        fromSo = fromSo + "," + purchaseOrdersDetail.getFromSo();
                                    }
                                    bookingProformaInvoice.setPoAmazon(fromSo);
                                }
                                bookingProformaInvoice.setSupplier(proformaInvoice.getSupplier());
                                mapPI.put(proformaInvoiceNo, bookingProformaInvoice);
                            }
                        }
                        return bookingPurchaseOrder;
                    }).collect(Collectors.toSet());
                    if (!listPO.isEmpty()) {
                        for (Map.Entry<String, String> entry : listPO.entrySet()) {
                            BookingPurchaseOrderLocation bookingPurchaseOrderLocation = new BookingPurchaseOrderLocation();
                            bookingPurchaseOrderLocation.setPoNumber(entry.getKey());
                            bookingPurchaseOrderLocation.setShipLocation(entry.getValue());
                            bookingPurchaseOrderLocationSet.add(bookingPurchaseOrderLocation);
                            Optional<PurchaseOrders> oPurchaseOrders = purchaseOrdersRepository.findByPoNumberAndIsDeleted(entry.getKey(), false);
                            if (!oPurchaseOrders.isPresent()) {
                                throw new BusinessException(String.format("Purchase order %s not exists", entry.getKey()));
                            } else {
                                PurchaseOrders purchaseOrders = oPurchaseOrders.get();
                                purchaseOrders.setStatus(GlobalConstant.STATUS_PO_BOOKING_CREATED);
                                purchaseOrdersRepository.saveAndFlush(purchaseOrders);
                            }
                        }
                    }
                    if (!mapPI.isEmpty()) {
                        for (Map.Entry<String, BookingProformaInvoice> entry : mapPI.entrySet()) {
                            BookingProformaInvoice bookingProformaInvoice = entry.getValue();
                            bookingProformaInvoices.add(bookingProformaInvoice);
                        }
                    }
                }
                booking.setBookingProformaInvoice(bookingProformaInvoices);
                booking.setBookingPurchaseOrderLocation(bookingPurchaseOrderLocationSet);
                booking.setBookingPurchaseOrder(bookingPurchaseOrderSet);
                booking.setStatus(GlobalConstant.STATUS_BOOKING_CREATED);
                bookingRepository.saveAndFlush(booking);
                return booking;
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new BusinessException(e.getMessage());
            }
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }

    }


}
