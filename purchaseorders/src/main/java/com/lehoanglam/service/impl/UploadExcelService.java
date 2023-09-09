package com.yes4all.service.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.*;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.*;
import com.yes4all.domain.model.*;
import com.yes4all.repository.BookingPackingListRepository;
import com.yes4all.repository.PurchaseOrdersRepository;
import com.yes4all.repository.PurchaseOrdersSplitRepository;
import com.yes4all.repository.UserRepository;
import com.yes4all.service.SendMailService;
import liquibase.pro.packaged.A;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class UploadExcelService {

    private static final Logger logger = LoggerFactory.getLogger(UploadExcelService.class);

    private static final String KEY_UPLOAD = "@#$!@";

    @Autowired
    private PurchaseOrdersRepository purchaseOrdersRepository;
    @Autowired
    private PurchaseOrdersSplitRepository purchaseOrdersSplitRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingPackingListRepository bookingPackingListRepository;

    @Autowired
    private SendMailService sendMailService;

    private static final String COLUMN_SO = "PO_NUMBER";
    private static final String COLUMN_SKU = "SKU";
    private static final String COLUMN_ASIN = "ASIN";

    private static final String COLUMN_PRODUCT_NAME = "PRODUCT_NAME";
    private static final String COLUMN_QUANTITY_ORDERED = "QUANTITY_ORDERED";
    private static final String COLUMN_SHIPDATE = "SHIPDATE";
    private static final String COLUMN_FULFILLMENT_CENTER = "FULFILLMENT_CENTER";
    private static final String COLUMN_VENDOR = "VENDOR";
    private static final String COLUMN_MTS = "MTS";
    private static final String COLUMN_UNIT_PRICE = "COLUMN_UNIT_PRICE";
    private static final String COLUMN_AMOUNT = "COLUMN_AMOUNT";

    private static final String COLUMN_COUNTRY = "COLUMN_COUNTRY";

    private static final String COLUMN_PCS = "COLUMN_PCS";
    private static final String COLUMN_TOTAL_BOX = "COLUMN_TOTAL_BOX";
    private static final String COLUMN_PROFORMA_INVOICE = "COLUMN_TOTAL_PROFORMA_INVOICE";
    private static final String COLUMN_PO_NUMBER = "COLUMN_TOTAL_PO_NUMBER";
    private static final String COLUMN_QTY_OF_EACH_CARTON = "COLUMN_TOTAL_QTY_OF_EACH_CARTON";
    private static final String COLUMN_TOTAL_CARTON = "COLUMN_TOTAL_CARTON";
    private static final String COLUMN_NET_WEIGHT = "COLUMN_TOTAL_NET_WEIGHT";
    private static final String COLUMN_GROSS_WEIGHT = "COLUMN_TOTAL_NET_WEIGHT";
    private static final String COLUMN_CBM = "COLUMN_TOTAL_CBM";
    private static final String COLUMN_CONTAINER = "COLUMN_CONTAINER";

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${attribute.host.url}")
    private String linkPOMS;

    private static final String LINK_DETAIL_PO = "/api/purchase-order/";



    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResultUploadDTO mappingToPO(MultipartFile file,String userName) {
        ResultUploadDTO resultUploadDTO = new ResultUploadDTO();
        if (ExcelHelper.hasExcelFormat(file)) {
            try {

                logger.info("START import excel  file");
                mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
                mapper.registerModule(new JavaTimeModule());
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                StopWatch stopWatch = DateUtils.initStopWatch();
                Workbook wb = ExcelHelper.readUploadExcel(file.getInputStream());
                Map<String, List<Map<String, Object>>> purchaseOrdersInfo = mappingTableSheet(wb);
                // count row i errors
                final int[] i = {-1};
                List<UploadPurchaseOrder> listUploadPurchaseOrder = new ArrayList<>();
                purchaseOrdersInfo.entrySet().stream().forEach(purchaseOrderEntry -> {
                    try {
                        i[0]++;
                        String namePO = purchaseOrderEntry.getKey();
                        Map<String, Integer> resultUploadDetail = new HashMap<>();
                        Optional<PurchaseOrders> oPurchaseOrder = purchaseOrdersRepository.findByPoNumberAndIsDeleted(namePO, false);
                        if (!oPurchaseOrder.isPresent()) {
                            // get list Po Number
                            List<Map<String, Object>> listPO = purchaseOrderEntry.getValue();
                            PurchaseOrders purchaseOrders = new PurchaseOrders();
                            purchaseOrders.setPoNumber(namePO);
                            purchaseOrders.setCreatedBy(userName);
                            purchaseOrders.setCreatedDate(new Date().toInstant());
                            purchaseOrders.setStatus(GlobalConstant.STATUS_PO_NEW);
                            //0 is Direct Import
                            purchaseOrders.setChannel(0);
                            Set<PurchaseOrdersDetail> sPurchaseOrdersDetail = new HashSet<>();
                            List<String> listSkuFromSo = new ArrayList<>();
                            List<String> listDuplicateSkuSo = new ArrayList<>();
                            listPO.stream().forEach(item -> {
                                Object oShipDate = item.get(COLUMN_SHIPDATE);
                                Object oFulfillmentCenter = item.get(COLUMN_FULFILLMENT_CENTER);
                                Object oVendor = item.get(COLUMN_VENDOR);
                                purchaseOrders.setExpectedShipDate(DateUtils.convertStringLocalDate(oShipDate.toString()));
                                purchaseOrders.setFulfillmentCenter(oFulfillmentCenter.toString());
                                purchaseOrders.setVendorId(oVendor.toString());
                                PurchaseOrdersDetail purchaseOrdersDetail = new PurchaseOrdersDetail();
                                Object oSO = item.get(COLUMN_SO);
                                Object oSku = item.get(COLUMN_SKU);
                                Object oASin = item.get(COLUMN_ASIN);
                                Object oProductName = item.get(COLUMN_PRODUCT_NAME);
                                Object oQuantityOrdered = item.get(COLUMN_QUANTITY_ORDERED);
                                Object oMTS = item.get(COLUMN_MTS);
                                Object oUnitPrice = item.get(COLUMN_UNIT_PRICE);
                                Object oAmount = item.get(COLUMN_AMOUNT);
                                Object oCBM = item.get(COLUMN_CBM);
                                Object oNetWeight = item.get(COLUMN_NET_WEIGHT);
                                Object oGrossWeight = item.get(COLUMN_GROSS_WEIGHT);
                                Object oTotalBox = item.get(COLUMN_TOTAL_BOX);
                                Object oPCS = item.get(COLUMN_PCS);
                                boolean isDateValid = CommonDataUtil.isDateValid((String) oShipDate);
                                if (!isDateValid) {
                                    listUploadPurchaseOrder.add(new UploadPurchaseOrder(namePO, "errors", "PO Number already in Purchase Orders", null));
                                }
                                purchaseOrdersDetail.setUnitCost(oUnitPrice.equals("") ? 0 : Double.parseDouble(oUnitPrice.toString()));
                                purchaseOrdersDetail.setAmount(oAmount.equals("") ? 0 : Double.parseDouble(oAmount.toString()));
                                purchaseOrdersDetail.setTotalVolume(oCBM.equals("") ? 0 : Double.parseDouble(oCBM.toString()));
                                purchaseOrdersDetail.setTotalBoxNetWeight(oNetWeight.equals("") ? 0 : Double.parseDouble(oNetWeight.toString()));
                                purchaseOrdersDetail.setTotalBoxCrossWeight(oGrossWeight.equals("") ? 0 : Double.parseDouble(oGrossWeight.toString()));
                                purchaseOrdersDetail.setTotalBox(oTotalBox.equals("") ? 0 : Double.parseDouble(oTotalBox.toString()));
                                purchaseOrdersDetail.setPcs(oPCS.equals("") ? 0 : Integer.parseInt(oPCS.toString()));
                                purchaseOrdersDetail.setFromSo(oSO.toString());
                                purchaseOrdersDetail.setShipDate(DateUtils.convertStringLocalDate(oShipDate.toString()));
                                purchaseOrdersDetail.setSku(oSku.toString());
                                purchaseOrdersDetail.setAsin(oASin.toString());
                                purchaseOrdersDetail.setQtyUsed(0L);
                                purchaseOrdersDetail.setProductName(oProductName.toString());
                                double qtyOrdered=oQuantityOrdered.equals("") ? 0 : Double.parseDouble(oQuantityOrdered.toString());
                                long qtyOrderedValue = (long) qtyOrdered;
                                purchaseOrdersDetail.setQtyOrdered(qtyOrderedValue);
                                purchaseOrdersDetail.setQtyOrderedPrevious(qtyOrderedValue);
                                double mts=oMTS.equals("") ? 0 : Double.parseDouble(oMTS.toString());
                                long mtsValue = (long) mts;
                                purchaseOrdersDetail.setMakeToStock(mtsValue);
                                String keySkuSo = oSku + "/" + oSO;
                                if (listSkuFromSo.contains(keySkuSo)) {
                                    listDuplicateSkuSo.add(keySkuSo);
                                }
                                listSkuFromSo.add(keySkuSo);
                                sPurchaseOrdersDetail.add(purchaseOrdersDetail);
                                if (CommonDataUtil.isNotNull(resultUploadDetail.get(oSO.toString()))) {
                                    resultUploadDetail.put(oSO.toString(), resultUploadDetail.get(oSO.toString()) + 1);
                                } else {
                                    resultUploadDetail.put(oSO.toString(), 1);
                                }
                            });
                            if (listDuplicateSkuSo.size() == 0) {
                                purchaseOrders.setPurchaseOrdersDetail(sPurchaseOrdersDetail);
                                purchaseOrders.setTotalItem(sPurchaseOrdersDetail.stream().filter(k -> !k.isDeleted()).map(x -> Objects.isNull(x.getQtyOrdered()) ? 0 : x.getQtyOrdered()).reduce(0L, Long::sum));
                                purchaseOrdersRepository.saveAndFlush(purchaseOrders);
                                List<String> listEmail = new ArrayList<>();
                                Optional<User> userEmails = userRepository.findOneByVendor(purchaseOrders.getVendorId());
                                if(userEmails.isPresent()){
                                    User user=userEmails.get();
                                    listEmail.add(user.getEmail());
                                    if(user.getEmail().length()>0) {
                                        String supplier = (user.getLastName() == null ? "" : user.getLastName() + " ") + user.getFirstName();
                                        String content = CommonDataUtil.contentMail(linkPOMS+LINK_DETAIL_PO+purchaseOrders.getId()+"?size=20&page=0", purchaseOrders.getPoNumber(),supplier,"","","NEW");
                                        sendMailService.doSendMail("" + purchaseOrders.getPoNumber() + " - A new Purchase Order from Yes4All", content, listEmail);
                                    }
                                }
                                listUploadPurchaseOrder.add(new UploadPurchaseOrder(namePO, "successes", "PO Number created ", resultUploadDetail));
                            } else {
                                listUploadPurchaseOrder.add(new UploadPurchaseOrder(namePO, "errors", String.format("SO and Sku duplicate in PO list={%s}", listDuplicateSkuSo.stream().collect(Collectors.joining(", "))), null));
                            }
                        } else {
                            listUploadPurchaseOrder.add(new UploadPurchaseOrder(namePO, "errors", "PO Number already in Purchase Orders", null));
                        }


                    } catch (Exception e) {
                        throw new BusinessException(e.getMessage());
                    }
                });
                resultUploadDTO.setUploadPurchaseOrder(listUploadPurchaseOrder);
                logger.info("END ==========");
                logger.info("Total time executed: {}", DateUtils.calculateTime(stopWatch));
                return resultUploadDTO;


            } catch (Exception e) {
                throw new BusinessException(e.getMessage());
            }
        }
        return null;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResultUploadDTO mappingToDetailPO(MultipartFile file,Integer id) {
        ResultUploadDTO resultUploadDTO = new ResultUploadDTO();
        PurchaseOrderDTO purchaseOrderDTO=new PurchaseOrderDTO();
        if (ExcelHelper.hasExcelFormat(file)) {
            try {

                logger.info("START import excel  file");
                mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
                mapper.registerModule(new JavaTimeModule());
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                StopWatch stopWatch = DateUtils.initStopWatch();
                Workbook wb = ExcelHelper.readUploadExcel(file.getInputStream());
                Map<String, List<Map<String, Object>>> purchaseOrdersInfo = mappingTableSheetDetailPO(wb);
                // count row i errors
                final int[] i = {-1};
                List<UploadPurchaseOrderDetail> listUploadPurchaseOrderDetail = new ArrayList<>();
                purchaseOrdersInfo.entrySet().stream().forEach(purchaseOrderEntry -> {
                    try {
                        i[0]++;
                        Map<Integer, Boolean> resultUploadDetail = new HashMap<>();
                        Optional<PurchaseOrders> oPurchaseOrder = purchaseOrdersRepository.findById(id);
                        if (oPurchaseOrder.isPresent()) {
                            PurchaseOrders purchaseOrders=oPurchaseOrder.get();
                            CommonDataUtil.getModelMapper().map(purchaseOrders,purchaseOrderDTO);
                            //get details previous
                            Set<PurchaseOrderDetailDTO> purchaseOrdersDetailsPrevious=purchaseOrderDTO.getPurchaseOrdersDetail();
                            //details new
                            Set<PurchaseOrderDetailDTO> purchaseOrdersDetailsNew=new HashSet<>();
                            // get list Po Number
                            List<Map<String, Object>> listPO = purchaseOrderEntry.getValue();
                            final int[] index = {1};
                            listPO.stream().forEach(item -> {

                                Object oSO = item.get(COLUMN_SO);
                                Object oSku = item.get(COLUMN_SKU);
                                Object oASin = item.get(COLUMN_ASIN);
                                Object oQuantityOrdered = item.get(COLUMN_QUANTITY_ORDERED);
                                Object oUnitPrice = item.get(COLUMN_UNIT_PRICE);
                                Object oAmount = item.get(COLUMN_AMOUNT);
                                Object oCBM = item.get(COLUMN_CBM);
                                Object oNetWeight = item.get(COLUMN_NET_WEIGHT);
                                Object oGrossWeight = item.get(COLUMN_GROSS_WEIGHT);
                                Object oTotalBox = item.get(COLUMN_TOTAL_BOX);
                                Object oPCS = item.get(COLUMN_PCS);
                                double qtyOrdered=oQuantityOrdered.equals("") ? 0 : Double.parseDouble(oQuantityOrdered.toString());
                                double unitPrice=oUnitPrice.equals("") ? 0 : Double.parseDouble(oUnitPrice.toString());
                               // double amount=oAmount.equals("") ? 0 : Double.parseDouble(oAmount.toString());
                                double amount=unitPrice*qtyOrdered;
                                int pcs=oPCS.equals("") ? 0 : Integer.parseInt(oPCS.toString());
                               //  double totalBox=oTotalBox.equals("") ? 0 : Double.parseDouble(oTotalBox.toString());
                                double totalBox= pcs==0?0:qtyOrdered/pcs;

                                Optional<PurchaseOrderDetailDTO> purchaseOrdersDetail= purchaseOrdersDetailsPrevious.stream().filter(k->k.getFromSo().equals(oSO.toString()) && k.getASin().equals(oASin.toString()) && k.getSku().equals(oSku.toString()) && !k.isDeleted()).findFirst();
                                if(purchaseOrdersDetail.isPresent()){
                                    PurchaseOrderDetailDTO purchaseOrderDetailDTOOld=purchaseOrdersDetail.get();
                                    purchaseOrderDetailDTOOld.setQtyOrderedPrevious(purchaseOrderDetailDTOOld.getQtyOrdered());
                                    purchaseOrderDetailDTOOld.setQtyOrdered(oQuantityOrdered.equals("") ? 0L : Long.parseLong(oNetWeight.toString()));
                                    purchaseOrderDetailDTOOld.setUnitCostPrevious(purchaseOrderDetailDTOOld.getUnitCostPrevious());
                                    purchaseOrderDetailDTOOld.setUnitCost(unitPrice);
                                    purchaseOrderDetailDTOOld.setAmountPrevious(purchaseOrderDetailDTOOld.getAmount());
                                    purchaseOrderDetailDTOOld.setAmount(amount);
                                    purchaseOrderDetailDTOOld.setPcsPrevious(purchaseOrderDetailDTOOld.getPcs());
                                    purchaseOrderDetailDTOOld.setPcs(pcs);
                                    purchaseOrderDetailDTOOld.setTotalBoxNetWeightPrevious(purchaseOrderDetailDTOOld.getTotalBoxNetWeight());
                                    purchaseOrderDetailDTOOld.setTotalBoxNetWeight(oNetWeight.equals("") ? 0 : Double.parseDouble(oNetWeight.toString()));
                                    purchaseOrderDetailDTOOld.setTotalBoxPrevious(purchaseOrderDetailDTOOld.getTotalBox());
                                    purchaseOrderDetailDTOOld.setTotalBox(totalBox);
                                    purchaseOrderDetailDTOOld.setTotalVolumePrevious(purchaseOrderDetailDTOOld.getTotalVolume());
                                    purchaseOrderDetailDTOOld.setTotalVolume(oCBM.equals("") ? 0 : Double.parseDouble(oCBM.toString()));
                                    purchaseOrderDetailDTOOld.setTotalBoxCrossWeightPrevious(purchaseOrderDetailDTOOld.getTotalBoxCrossWeight());
                                    purchaseOrderDetailDTOOld.setTotalBoxCrossWeight(oGrossWeight.equals("") ? 0 : Double.parseDouble(oGrossWeight.toString()));
                                    purchaseOrdersDetailsNew.add(purchaseOrderDetailDTOOld);
                                    resultUploadDetail.put(purchaseOrderDetailDTOOld.getId(),true);
                                }else{
                                    listUploadPurchaseOrderDetail.add(new UploadPurchaseOrderDetail(index[0],oSO.toString(),oASin.toString(),oSku.toString(),"Not exists in detail this Purchaser Order!"));
                                }
                                index[0]++;
                            });
                            if(resultUploadDetail.size()>0) {
                                purchaseOrdersDetailsPrevious.stream().filter(item -> resultUploadDetail.get(item.getId())==null || !resultUploadDetail.get(item.getId())).forEach(element -> {
                                    listUploadPurchaseOrderDetail.add(new UploadPurchaseOrderDetail(index[0], element.getFromSo(), element.getASin(), element.getSku(), "Not found in file Excel!"));
                                });
                            }
                            if(listUploadPurchaseOrderDetail.isEmpty()) {
                                purchaseOrderDTO.setPurchaseOrdersDetail(purchaseOrdersDetailsNew);
                            }
                        }
                    } catch (Exception e) {
                        throw new BusinessException(e.getMessage());
                    }
                });
                resultUploadDTO.setUploadPurchaseOrderDetail(listUploadPurchaseOrderDetail);
                logger.info("END ==========");
                logger.info("Total time executed: {}", DateUtils.calculateTime(stopWatch));
                if(listUploadPurchaseOrderDetail.isEmpty()) {
                    resultUploadDTO.setPurchaseOrderDTO(purchaseOrderDTO);
                }
                return resultUploadDTO;


            } catch (Exception e) {
                throw new BusinessException(e.getMessage());
            }
        }
        return null;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResultUploadDTO mappingToDetailPackingList(MultipartFile file,Integer id) {
        ResultUploadDTO resultUploadDTO = new ResultUploadDTO();
        if (ExcelHelper.hasExcelFormat(file)) {
            try {

                logger.info("START import excel  file");
                mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
                mapper.registerModule(new JavaTimeModule());
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                StopWatch stopWatch = DateUtils.initStopWatch();
                Workbook wb = ExcelHelper.readUploadExcel(file.getInputStream());
                Map<String, List<Map<String, Object>>> packingListInfo = mappingTableSheetPackingList(wb);
                // count row i errors
                final int[] i = {-1};
                List<ResultUploadPackingListDetail> resultUploadPackingListDetail = new ArrayList<>();
                BookingPackingListDTO bookingPackingListDTO=new BookingPackingListDTO();
                packingListInfo.entrySet().stream().forEach(packingListEntry -> {
                    try {
                        i[0]++;
                        Map<Integer, Boolean> resultUploadDetail = new HashMap<>();

                        Optional<BookingPackingList> oBookingPackingList = bookingPackingListRepository.findById(id);
                        if (oBookingPackingList.isPresent()) {
                            BookingPackingList bookingPackingList=oBookingPackingList.get();
                            BeanUtils.copyProperties(bookingPackingList, bookingPackingListDTO);
                            //get details previous
                            Set<BookingPackingListDetailsDTO> bookingPackingListDetailPrevious=bookingPackingList.getBookingPackingListDetail().stream().map(element->{
                                BookingPackingListDetailsDTO bookingPackingListDetailsDTO =new BookingPackingListDetailsDTO();
                                BeanUtils.copyProperties(element, bookingPackingListDetailsDTO);
                                return bookingPackingListDetailsDTO;
                            }).collect(Collectors.toSet());
                            //details new
                            Set<BookingPackingListDetailsDTO> bookingPackingListDetailNew=new HashSet<>();
                            // get list Po Number
                            List<Map<String, Object>> listPackingList  = packingListEntry.getValue();
                            final int[] index = {1};
                            listPackingList.stream().forEach(item -> {

                                Object oInvoiceNo = item.get(COLUMN_PROFORMA_INVOICE);
                                Object oPO = item.get(COLUMN_PO_NUMBER);
                                Object oASin = item.get(COLUMN_ASIN);
                                Object oSku = item.get(COLUMN_SKU);
                                Object oName = item.get(COLUMN_PRODUCT_NAME);
                                Object oQuantityOrdered = item.get(COLUMN_QUANTITY_ORDERED);
                                Object oQtyOfEachCarton = item.get(COLUMN_QTY_OF_EACH_CARTON);
                                Object oCarTon = item.get(COLUMN_TOTAL_CARTON);
                                Object oNetWeight = item.get(COLUMN_NET_WEIGHT);
                                Object oGrossWeight = item.get(COLUMN_GROSS_WEIGHT);
                                Object oCbm = item.get(COLUMN_CBM);
                                Object oContainer = item.get(COLUMN_CONTAINER);
                                int qtyOrdered=oQuantityOrdered.equals("") ? 0 : Integer.parseInt(oQuantityOrdered.toString());
                                double grossWeight=oGrossWeight.equals("") ? 0 : Double.parseDouble(oGrossWeight.toString());
                                double netWeight=oNetWeight.equals("") ? 0 : Double.parseDouble(oNetWeight.toString());
                                int qtyOfEachCarton=oQtyOfEachCarton.equals("") ? 0 : Integer.parseInt(oQtyOfEachCarton.toString());
                                double cbm=oCbm.equals("") ? 0 : Double.parseDouble(oCbm.toString());
                                double carTon=oCarTon.equals("") ? 0 : Double.parseDouble(oCarTon.toString());

                                //set value for field previous
                                Optional<BookingPackingListDetailsDTO> bookingPackingListDetail= bookingPackingListDetailPrevious.stream().filter(k->k.getProformaInvoiceNo().equals(oInvoiceNo.toString()) && k.getASin().equals(oASin.toString()) && k.getSku().equals(oSku.toString()) &&  k.getPoNumber().equals(oPO.toString())).findFirst();
                                if(bookingPackingListDetail.isPresent()){
                                    BookingPackingListDetailsDTO bookingPackingListDetailOld=bookingPackingListDetail.get();
                                    bookingPackingListDetailOld.setQuantityPrevious(bookingPackingListDetailOld.getQuantity());
                                    bookingPackingListDetailOld.setQuantity(qtyOrdered);
                                    bookingPackingListDetailOld.setQtyEachCartonPrevious(bookingPackingListDetailOld.getQtyEachCarton());
                                    bookingPackingListDetailOld.setQtyEachCarton(qtyOfEachCarton);
                                    bookingPackingListDetailOld.setTotalCartonPrevious(bookingPackingListDetailOld.getTotalCarton());
                                    bookingPackingListDetailOld.setTotalCarton( carTon);
                                    bookingPackingListDetailOld.setNetWeightPrevious(bookingPackingListDetailOld.getNetWeight());
                                    bookingPackingListDetailOld.setNetWeight(netWeight);
                                    bookingPackingListDetailOld.setGrossWeightPrevious(bookingPackingListDetailOld.getGrossWeight());
                                    bookingPackingListDetailOld.setGrossWeight(grossWeight);
                                    bookingPackingListDetailOld.setCbmPrevious(bookingPackingListDetailOld.getCbm());
                                    bookingPackingListDetailOld.setCbm(cbm);
                                    bookingPackingListDetailOld.setContainer(oContainer.toString());
                                    bookingPackingListDetailNew.add(bookingPackingListDetailOld);
                                    resultUploadDetail.put(bookingPackingListDetailOld.getId(),true);
                                }else{
                                    resultUploadPackingListDetail.add(new ResultUploadPackingListDetail(index[0],oInvoiceNo.toString(),oPO.toString(),oSku.toString(),oASin.toString(),"Data dose not exist in Proforma Invoice"));
                                }
                                index[0]++;
                            });
                            bookingPackingListDetailPrevious.stream().filter(item->resultUploadDetail.get(item.getId())==null).forEach(element->{
                                resultUploadPackingListDetail.add(new ResultUploadPackingListDetail(index[0],element.getProformaInvoiceNo(),element.getPoNumber(),element.getSku(),element.getASin(),"Missing data!"));
                            });
                            if(resultUploadPackingListDetail.isEmpty()) {
                                bookingPackingListDTO.setBookingPackingListDetailsDTO(bookingPackingListDetailNew);
                             }
                        }
                    } catch (Exception e) {
                        throw new BusinessException(e.getMessage());
                    }
                });
                resultUploadDTO.setResultUploadPackingListDetail(resultUploadPackingListDetail);
                resultUploadDTO.setBookingPackingListDTO(bookingPackingListDTO);
                logger.info("END ==========");
                logger.info("Total time executed: {}", DateUtils.calculateTime(stopWatch));
                return resultUploadDTO;
            } catch (Exception e) {
                throw new BusinessException(e.getMessage());
            }
        }
        return null;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResultUploadDTO mappingToPOSplit(MultipartFile file) {
        ResultUploadDTO resultUploadDTO = new ResultUploadDTO();
        String filename=file.getOriginalFilename();
        filename=filename.replace(".xlsx","").replace(".xls","");
        Optional<PurchaseOrdersSplit>  purchaseOrdersSplit=purchaseOrdersSplitRepository.findByRootFile(filename);
        List<UploadPurchaseOrderSplitStatus> listUploadPurchaseOrderStatus = new ArrayList<>();
        if(purchaseOrdersSplit.isPresent()){
            listUploadPurchaseOrderStatus.add(new UploadPurchaseOrderSplitStatus(filename, "errors", "File name already exists in system."));
        }
        if (ExcelHelper.hasExcelFormat(file) && !purchaseOrdersSplit.isPresent()) {
            try {

                logger.info("START import excel  file");
                mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
                mapper.registerModule(new JavaTimeModule());
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                StopWatch stopWatch = DateUtils.initStopWatch();
                Workbook wb = ExcelHelper.readUploadExcel(file.getInputStream());
                List<Map<String, Object>> purchaseOrdersSplitInfo = mappingTableSheetSplitPO(wb.getSheetAt(0));
                // count row i errors
                final int[] i = {-1};
                List<PurchaseOrdersSplitData> purchaseOrdersSplitDataSet = new ArrayList<>();
                Map<String,List<PurchaseOrdersSplitData>> mapPurchaseOrdersSplitData =new HashMap<>();
                List<String> duplicateSoSku=new ArrayList<>();
                String finalFilename = filename;
                purchaseOrdersSplitInfo.stream().filter(m->m.size()>0).forEach(item -> {
                    try {
                        i[0]++;

                        PurchaseOrdersSplitData purchaseOrdersSplitData = new PurchaseOrdersSplitData();
                        Object oShipDate = item.get(COLUMN_SHIPDATE);
                        Object oFulfillmentCenter = item.get(COLUMN_FULFILLMENT_CENTER);
                        Object oVendor = item.get(COLUMN_VENDOR);
                        Object oCountry = item.get(COLUMN_COUNTRY);
                        Object oSO = item.get(COLUMN_SO);
                        Object oSku = item.get(COLUMN_SKU);
                        Object oASin = item.get(COLUMN_ASIN);
                        Object oProductName = item.get(COLUMN_PRODUCT_NAME);
                        Object oQuantityOrdered = item.get(COLUMN_QUANTITY_ORDERED);
                        Object oMTS = item.get(COLUMN_MTS);
                        Object oUnitPrice = item.get(COLUMN_UNIT_PRICE);
                        Object oAmount = item.get(COLUMN_AMOUNT);
                        Object oCBM = item.get(COLUMN_CBM);
                        Object oNetWeight = item.get(COLUMN_NET_WEIGHT);
                        Object oGrossWeight = item.get(COLUMN_GROSS_WEIGHT);
                        Object oTotalBox = item.get(COLUMN_TOTAL_BOX);
                        Object oPCS = item.get(COLUMN_PCS);
                        if(oVendor.toString().trim().length()==0 || oShipDate.toString().trim().length()==0|| oFulfillmentCenter.toString().trim().length()==0){
                            listUploadPurchaseOrderStatus.add(new UploadPurchaseOrderSplitStatus(finalFilename, "errors", "Vendor or Fulfillment center or Ship date  cannot empty."));
                        }
                        if(duplicateSoSku.contains(oSO.toString()+KEY_UPLOAD+oSku.toString())){
                            listUploadPurchaseOrderStatus.add(new UploadPurchaseOrderSplitStatus(finalFilename, "errors", String.format("{Sku=%s ,Po Number=%s} cannot duplicate.", oSku, oSO)));
                        }else {
                            duplicateSoSku.add(oSO + KEY_UPLOAD + oSku);
                        }
                        boolean isDateValid = CommonDataUtil.isDateValidSplitPO(oShipDate.toString());
                        if (!isDateValid) {
                            listUploadPurchaseOrderStatus.add(new UploadPurchaseOrderSplitStatus(finalFilename, "errors", String.format("Format Ship Date must dd-MMM-yy", oSku, oSO)));
                        }
                        purchaseOrdersSplitData.setShipDate(DateUtils.convertStringLocalDate(oShipDate.toString()));
                        purchaseOrdersSplitData.setFulfillmentCenter(oFulfillmentCenter.toString());
                        purchaseOrdersSplitData.setVendor(oVendor.toString());
                        purchaseOrdersSplitData.setSaleOrder(oSO.toString());
                        purchaseOrdersSplitData.setCountry(oCountry.toString());
                        purchaseOrdersSplitData.setSku(oSku.toString());
                        purchaseOrdersSplitData.setaSin(oASin.toString());
                        purchaseOrdersSplitData.setProductName(oProductName.toString());
                        purchaseOrdersSplitData.setQtyOrdered(oQuantityOrdered.equals("") ? 0 : Long.parseLong(oQuantityOrdered.toString()));
                        purchaseOrdersSplitData.setMakeToStock(oMTS.equals("") ? 0 : Long.parseLong(oMTS.toString()));
                        purchaseOrdersSplitData.setUnitCost(oUnitPrice.equals("") ? 0 : Double.parseDouble(oUnitPrice.toString()));
                        purchaseOrdersSplitData.setAmount(oAmount.equals("") ? 0 : Double.parseDouble(oAmount.toString()));
                        purchaseOrdersSplitData.setCbm(oCBM.equals("") ? 0 : Double.parseDouble(oCBM.toString()));
                        purchaseOrdersSplitData.setNetWeight(oNetWeight.equals("") ? 0 : Double.parseDouble(oNetWeight.toString()));
                        purchaseOrdersSplitData.setGrossWeight(oGrossWeight.equals("") ? 0 : Double.parseDouble(oGrossWeight.toString()));
                        purchaseOrdersSplitData.setTotalBox(oTotalBox.equals("") ? 0 : Double.parseDouble(oTotalBox.toString()));
                        purchaseOrdersSplitData.setPcs(oPCS.equals("") ? 0 : Integer.parseInt(oPCS.toString()));
                        purchaseOrdersSplitDataSet.add(purchaseOrdersSplitData);

                    } catch (Exception e) {
                        throw new BusinessException(e.getMessage());
                    }
                });
                if(listUploadPurchaseOrderStatus.size()==0){
                    listUploadPurchaseOrderStatus.add(new UploadPurchaseOrderSplitStatus(filename, "successes","Upload successes!"));
                }
                resultUploadDTO.setUploadPurchaseOrderSplitStatus(listUploadPurchaseOrderStatus);
                mapPurchaseOrdersSplitData.put(filename,purchaseOrdersSplitDataSet);
                resultUploadDTO.setPurchaseOrdersSplit(mapPurchaseOrdersSplitData);
                logger.info("END ==========");
                logger.info("Total time executed: {}", DateUtils.calculateTime(stopWatch));
                return resultUploadDTO;
            } catch (Exception e) {
                throw new BusinessException(e.getMessage());
            }
        }
        resultUploadDTO.setUploadPurchaseOrderSplitStatus(listUploadPurchaseOrderStatus);
        return resultUploadDTO;
    }

    private Map<String, List<Map<String, Object>>> mappingTableSheet(Workbook wb) {
        ArrayList<String> sheetNames = new ArrayList<String>();
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            Sheet firstSheet = wb.getSheetAt(i);
            sheetNames.add(firstSheet.getSheetName());
        }
        if (sheetNames == null) {
            return null;
        }
        Map<String, List<Map<String, Object>>> data = new HashMap<>();
        sheetNames.forEach(item -> {
            Sheet sheet = wb.getSheet(item);
            Iterator<Row> rows = sheet.iterator();
            DataFormatter dataFormatter = new DataFormatter();
            Row keyRow = null;
            String nameSheet = item;
            int colNum = 0;
            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                Map<String, Object> rowData = new HashMap<>();
                if (rowNumber < 1) {
                    if (rowNumber == 0) {
                        keyRow = currentRow;
                        colNum = keyRow.getLastCellNum();
                    }
                    rowNumber++;
                    continue;
                }

                for (int cellIdx = 0; cellIdx < colNum; cellIdx++) {
                    Cell cellData = currentRow.getCell(cellIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    Cell keyCell = keyRow.getCell(cellIdx);
                    if (Objects.isNull(keyCell)) {
                        break;
                    }
                    if (cellIdx == 0 && Objects.isNull(cellData)) {
                        break;
                    }
                    //get key with index column
                    String key = getKey(cellIdx);
                    data.computeIfAbsent(nameSheet, k -> new ArrayList<>());
                    String value ="";
                    if (cellData.getCellType() == CellType.FORMULA) {
                        switch (cellData.getCachedFormulaResultType()) {
                            case BOOLEAN:
                                value= String.valueOf(cellData.getBooleanCellValue());
                                break;
                            case NUMERIC:
                                value= String.valueOf(cellData.getNumericCellValue());
                                break;
                            case STRING:
                                value= String.valueOf(cellData.getRichStringCellValue());
                                break;
                        }
                    }else{
                        value = dataFormatter.formatCellValue(cellData);
                    }
                    rowData.put(key, value);
                }
                if (rowData.size() > 0) {
                    data.get(nameSheet).add(rowData);
                } else {
                    break;
                }
                rowNumber++;
            }
        });

        return data;
    }

    private List<Map<String, Object>> mappingTableSheetSplitPO(Sheet sheet) {
        if (sheet == null) {
            return null;
        }
        Iterator<Row> rows = sheet.iterator();
        DataFormatter dataFormatter = new DataFormatter();
        Row keyRow = null;
        int colNum = 0;
        List<Map<String, Object>> data = new ArrayList<>();

        int rowNumber = 0;

        while (rows.hasNext()) {
            Row currentRow = rows.next();
            Map<String, Object> rowData = new HashMap<>();
            if (rowNumber < 1) {
                if (rowNumber == 0) {
                    keyRow = currentRow;
                    colNum = keyRow.getLastCellNum();
                }
                rowNumber++;
                continue;
            }

            for (int cellIdx = 0; cellIdx < colNum; cellIdx++) {
                Cell cellData = currentRow.getCell(cellIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                Cell keyCell = keyRow.getCell(cellIdx);
                if (Objects.isNull(keyCell)) {
                    break;
                }
                if(cellIdx==0){
                    String value = dataFormatter.formatCellValue(cellData);
                    if(value==null || value.trim().length()==0){
                        break;
                    }
                }
                String value = dataFormatter.formatCellValue(cellData);
                String key = getKey(cellIdx);
                rowData.put(key, value);
            }

            data.add(rowData);
            rowNumber++;
        }
        return data;
    }
    private Map<String, List<Map<String, Object>>> mappingTableSheetDetailPO(Workbook wb) {
        ArrayList<String> sheetNames = new ArrayList<String>();
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            Sheet firstSheet = wb.getSheetAt(i);
            sheetNames.add(firstSheet.getSheetName());
        }
        Map<String, List<Map<String, Object>>> data = new HashMap<>();
        sheetNames.forEach(item -> {
            Sheet sheet = wb.getSheet(item);
            Iterator<Row> rows = sheet.iterator();
            DataFormatter dataFormatter = new DataFormatter();
            Row keyRow = null;
            String nameSheet = item;
            int colNum = 0;
            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                Map<String, Object> rowData = new HashMap<>();
                if (rowNumber < 1) {
                    if (rowNumber == 0) {
                        keyRow = currentRow;
                        colNum = keyRow.getLastCellNum();
                    }
                    rowNumber++;
                    continue;
                }

                for (int cellIdx = 0; cellIdx < colNum; cellIdx++) {
                    Cell cellData = currentRow.getCell(cellIdx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    Cell keyCell = keyRow.getCell(cellIdx);
                    if (Objects.isNull(keyCell)) {
                        break;
                    }
                    if (cellIdx == 0 && Objects.isNull(cellData)) {
                        break;
                    }
                    //get key with index column
                    String key = getKeyDetailPO(cellIdx);
                    data.computeIfAbsent(nameSheet, k -> new ArrayList<>());
                    String value ="";
                    if (cellData.getCellType() == CellType.FORMULA) {
                        switch (cellData.getCachedFormulaResultType()) {
                            case BOOLEAN:
                                value= String.valueOf(cellData.getBooleanCellValue());
                                break;
                            case NUMERIC:
                                value= String.valueOf(cellData.getNumericCellValue());
                                break;
                            case STRING:
                                value= String.valueOf(cellData.getRichStringCellValue());
                                break;
                        }
                    }else{
                        value = dataFormatter.formatCellValue(cellData);
                    }
                    rowData.put(key, value);
                }
                if (rowData.size() > 0) {
                    data.get(nameSheet).add(rowData);
                } else {
                    break;
                }
                rowNumber++;
            }
        });

        return data;
    }
    private Map<String, List<Map<String, Object>>> mappingTableSheetPackingList(Workbook wb) {
        ArrayList<String> sheetNames = new ArrayList<String>();
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            Sheet firstSheet = wb.getSheetAt(i);
            sheetNames.add(firstSheet.getSheetName());
        }
        Map<String, List<Map<String, Object>>> data = new HashMap<>();
        sheetNames.forEach(item -> {
            Sheet sheet = wb.getSheet(item);
            Iterator<Row> rows = sheet.iterator();
            DataFormatter dataFormatter = new DataFormatter();
            Row keyRow = null;
            String nameSheet = item;
            int colNum = 0;
            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                Map<String, Object> rowData = new HashMap<>();
                if (rowNumber < 1) {
                    if (rowNumber == 0) {
                        keyRow = currentRow;
                        colNum = keyRow.getLastCellNum();
                    }
                    rowNumber++;
                    continue;
                }

                for (int cellIdx = 0; cellIdx < colNum; cellIdx++) {
                    Cell cellData = currentRow.getCell(cellIdx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    Cell keyCell = keyRow.getCell(cellIdx);
                    if (Objects.isNull(keyCell)) {
                        break;
                    }
                    if (cellIdx == 0 && Objects.isNull(cellData)) {
                        break;
                    }
                    //get key with index column
                    String key = getKeyDetailPackingList(cellIdx);
                    data.computeIfAbsent(nameSheet, k -> new ArrayList<>());
                    String value ="";
                    if (cellData.getCellType() == CellType.FORMULA) {
                        switch (cellData.getCachedFormulaResultType()) {
                            case BOOLEAN:
                                value= String.valueOf(cellData.getBooleanCellValue());
                                break;
                            case NUMERIC:
                                value= String.valueOf(cellData.getNumericCellValue());
                                break;
                            case STRING:
                                value= String.valueOf(cellData.getRichStringCellValue());
                                break;
                        }
                    }else{
                        value = dataFormatter.formatCellValue(cellData);
                    }
                    rowData.put(key, value);
                }
                if (rowData.size() > 0) {
                    data.get(nameSheet).add(rowData);
                } else {
                    break;
                }
                rowNumber++;
            }
        });

        return data;
    }
    private <T> Set<T> getSheetData(List<Map<String, Object>> listOfData, Class<T> tClass) {
        Set<T> dataSet = new HashSet<>();
        if (CommonDataUtil.isNotEmpty(listOfData)) {
            dataSet = listOfData.parallelStream().map(data -> mapper.convertValue(data, tClass)).collect(Collectors.toSet());
        }
        return dataSet;
    }

    public String getKey(int indexColNum) {
        String key = "";
        switch (indexColNum) {
            case 0:
                key = COLUMN_SO;
                break;
            case 1:
                key = COLUMN_SKU;
                break;
            case 2:
                key = COLUMN_ASIN;
                break;
            case 3:
                key = COLUMN_PRODUCT_NAME;
                break;
            case 4:
                key = COLUMN_QUANTITY_ORDERED;
                break;
            case 5:
                key = COLUMN_SHIPDATE;
                break;
            case 6:
                key = COLUMN_FULFILLMENT_CENTER;
                break;
            case 7:
                key = COLUMN_COUNTRY;
                break;
            case 8:
                key = COLUMN_VENDOR;
                break;
            case 9:
                key = COLUMN_MTS;
                break;
            case 10:
                key = COLUMN_UNIT_PRICE;
                break;
            case 11:
                key = COLUMN_AMOUNT;
                break;
            case 12:
                key = COLUMN_CBM;
                break;
            case 13:
                key = COLUMN_NET_WEIGHT;
                break;
            case 14:
                key = COLUMN_GROSS_WEIGHT;
                break;
            case 15:
                key = COLUMN_TOTAL_BOX;
                break;
            case 16:
                key = COLUMN_PCS;
                break;
        }
        return key;
    }
    public String getKeyDetailPO(int indexColNum) {
        String key = "";
        switch (indexColNum) {
            case 0:
                key = COLUMN_SO;
                break;
            case 1:
                key = COLUMN_SKU;
                break;
            case 2:
                key = COLUMN_ASIN;
                break;
            case 3:
                key = COLUMN_PRODUCT_NAME;
                break;
            case 4:
                key = COLUMN_QUANTITY_ORDERED;
                break;
            case 5:
                key = COLUMN_UNIT_PRICE;
                break;
            case 6:
                key = COLUMN_AMOUNT;
                break;
            case 7:
                key = COLUMN_PCS;
                break;
            case 8:
                key = COLUMN_TOTAL_BOX;
                break;
            case 9:
                key = COLUMN_CBM;
                break;
            case 10:
                key = COLUMN_NET_WEIGHT;
                break;
            case 11:
                key = COLUMN_GROSS_WEIGHT;
                break;
            case 12:
                key = COLUMN_MTS;
                break;
        }
        return key;
    }
    public String getKeyDetailPackingList(int indexColNum) {
        String key = "";
        switch (indexColNum) {
            case 0:
                key = COLUMN_PROFORMA_INVOICE;
                break;
            case 1:
                key = COLUMN_PO_NUMBER;
                break;
            case 2:
                key = COLUMN_SKU;
                break;
            case 3:
                key = COLUMN_PRODUCT_NAME;
                break;
            case 4:
                key = COLUMN_ASIN;
                break;
            case 5:
                key = COLUMN_QUANTITY_ORDERED;
                break;
            case 6:
                key = COLUMN_QTY_OF_EACH_CARTON;
                break;
            case 7:
                key = COLUMN_TOTAL_CARTON;
                break;
            case 8:
                key = COLUMN_NET_WEIGHT;
                break;
            case 9:
                key = COLUMN_GROSS_WEIGHT;
                break;
            case 10:
                key = COLUMN_CBM;
                break;
            case 11:
                key = COLUMN_CONTAINER;
                break;
        }
        return key;
    }
}
