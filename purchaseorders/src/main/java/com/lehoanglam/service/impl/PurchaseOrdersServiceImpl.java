package com.yes4all.service.impl;

import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.PageRequestUtil;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.*;
import com.yes4all.domain.model.*;
import com.yes4all.repository.*;
import com.yes4all.service.PurchaseOrdersService;
import com.yes4all.service.SendMailService;
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
public class PurchaseOrdersServiceImpl implements PurchaseOrdersService {

    private final Logger log = LoggerFactory.getLogger(PurchaseOrdersServiceImpl.class);
    @Autowired
    private ProformaInvoiceRepository proformaInvoiceRepository;

    @Autowired
    private ProformaInvoiceDetailRepository proformaInvoiceDetailRepository;

    @Value("${attribute.link.url}")
    private String linkPOMS;

    private static final String LINK_DETAIL_PO = "/purchase-order/detail/";
    @Autowired
    private PurchaseOrdersRepository purchaseOrdersRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PurchaseOrdersDetailRepository purchaseOrdersDetailRepository;

    @Autowired
    private CommercialInvoiceRepository commercialInvoiceRepository;
    @Autowired
    private SendMailService sendMailService;

    @Override
    public boolean removePurchaseOrders(List<Integer> listPurchaseOrderId, String userName) {
        try {
            listPurchaseOrderId.stream().forEach(i -> {
                Optional<PurchaseOrders> oPurchaseOrder = purchaseOrdersRepository.findById(i);
                if (oPurchaseOrder.isPresent()) {
                    PurchaseOrders purchaseOrders = oPurchaseOrder.get();
                    if (purchaseOrders.getStatus() == 3) {
                        throw new BusinessException("Can't Deleted with status Confirmed");
                    }
                    purchaseOrders.setDeleted(true);
                    purchaseOrders.setDeletedDate(new Date().toInstant());
                    purchaseOrders.setDeletedBy(userName);
                    List<PurchaseOrders> purchaseOrdersList = purchaseOrdersRepository.findAllByPoNumber(purchaseOrders.getPoNumber());
                    //find max cdc version update for po old
                    List<Long> cdcVersion = purchaseOrdersList.stream().map(k -> k.getCdcVersion() == null ? 0 : k.getCdcVersion()).collect(Collectors.toList());
                    Long cdcVersionMax = Collections.max(cdcVersion, null);
                    purchaseOrders.setCdcVersion(cdcVersionMax + 1);
                    Long cdcVersionMaxDetail = purchaseOrdersDetailRepository.findMaxCdcVersion(purchaseOrders.getId());
                    Set<PurchaseOrdersDetail> purchaseOrdersDetail = purchaseOrders.getPurchaseOrdersDetail().stream().map(item -> {
                        item.setDeleted(true);
                        item.setDeletedDate(new Date().toInstant());
                        item.setDeletedBy(userName);
                        item.setCdcVersion(cdcVersionMaxDetail + 1);
                        return item;
                    }).collect(Collectors.toSet());
                    purchaseOrders.setPurchaseOrdersDetail(purchaseOrdersDetail);
                    purchaseOrdersRepository.saveAndFlush(purchaseOrders);
                }
            });
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public boolean confirmedPurchaseOrders(List<Integer> listPurchaseOrderId, String userName) {
        try {
            List<String> listPO = new ArrayList<>();
            listPurchaseOrderId.stream().forEach(i -> {
                Optional<PurchaseOrders> oPurchaseOrder = purchaseOrdersRepository.findById(i);
                if (oPurchaseOrder.isPresent()) {
                    PurchaseOrders purchaseOrders = oPurchaseOrder.get();
                    if (purchaseOrders.getStatus() == 3) {
                        throw new BusinessException(String.format("Can't Deleted with status Confirmed"));
                    }
                    if (purchaseOrders.getStatus() != 0 && purchaseOrders.getStatus() != 1 && purchaseOrders.getStatus() != 2) {
                        listPO.add(purchaseOrders.getPoNumber());
                    } else {
                        purchaseOrders.setStatus(GlobalConstant.STATUS_PO_CONFIRMED);
                        purchaseOrdersRepository.saveAndFlush(purchaseOrders);
                        List<String> listEmail = new ArrayList<>();
                        Optional<User> userEmails = userRepository.findOneByEmail(purchaseOrders.getCreatedBy());
                        Optional<User> userEmailsSupplier = userRepository.findOneByVendor(purchaseOrders.getVendorId());
                        boolean isSupplier = false;

                        if (userEmails.isPresent()) {
                            User user = userEmails.get();
                            User userVendor = userEmailsSupplier.get();
                            if (userName.equals(userVendor.getLogin())) {
                                isSupplier = true;
                            }
                            if (user.getEmail().length() > 0) {
                                if (isSupplier) {
                                    listEmail.add(user.getEmail());
                                    String supplier = (userVendor.getLastName() == null ? "" : userVendor.getLastName() + " ") + userVendor.getFirstName();
                                    String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PO + purchaseOrders.getId() + "?size=20&page=0", purchaseOrders.getPoNumber(), supplier, "The purchase order", "confirmed", "Confirmed");
                                    sendMailService.doSendMail("" + purchaseOrders.getPoNumber() + " - The purchase order has been confirmed by supplier " + supplier + "", content, listEmail);
                                } else {
                                    listEmail.add(userVendor.getEmail());
                                    String supplier = "Yes4all";
                                    String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PO + purchaseOrders.getId() + "?size=20&page=0", purchaseOrders.getPoNumber(), supplier, "The purchase order adjustment", "confirmed", "ConfirmedAdjustment");
                                    sendMailService.doSendMail("" + purchaseOrders.getPoNumber() + " - The purchase order adjustment has been confirmed by supplier " + supplier + "", content, listEmail);
                                }
                            }
                        }
                    }
                }
            });
            if (listPO.size() > 0) {
                throw new BusinessException(String.format("Purchase Orders { %s } were confirmed by Supplier. Please check again."), listPO.stream().collect(Collectors.joining(",")));
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public boolean sendPurchaseOrders(List<Integer> listPurchaseOrderId,String userName) {
        try {
            List<String> listPO = new ArrayList<>();
            listPurchaseOrderId.stream().forEach(i -> {
                Optional<PurchaseOrders> oPurchaseOrder = purchaseOrdersRepository.findById(i);
                if (oPurchaseOrder.isPresent()) {
                    PurchaseOrders purchaseOrders = oPurchaseOrder.get();
                    if (purchaseOrders.getStatus() != 0) {
                        listPO.add(purchaseOrders.getPoNumber());
                    } else {
                        purchaseOrders.setStatus(GlobalConstant.STATUS_PO_SENT_VENDOR);
                        purchaseOrdersRepository.saveAndFlush(purchaseOrders);
                        List<String> listEmail = new ArrayList<>();
                        Optional<User> userEmails = userRepository.findOneByEmail(purchaseOrders.getCreatedBy());
                        Optional<User> userEmailsSupplier = userRepository.findOneByVendor(purchaseOrders.getVendorId());
                        Optional<User> oUserNames = userRepository.findOneByLogin(userName);
                        if (userEmails.isPresent() && userEmailsSupplier.isPresent()) {
                            User userVendor = userEmailsSupplier.get();
                            User UserNames = oUserNames.get();
                            listEmail.add(userVendor.getEmail());
                            if (userVendor.getEmail().length() > 0) {
                                if(!UserNames.getSupplier()) {
                                    String supplier = (userVendor.getLastName() == null ? "" : userVendor.getLastName() + " ") + userVendor.getFirstName();
                                    String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PO + purchaseOrders.getId() + "?size=20&page=0", purchaseOrders.getPoNumber(),supplier, "The purchase order","NEW" , "NEW");
                                    sendMailService.doSendMail("" + purchaseOrders.getPoNumber() + " - A new Purchase Order from " + supplier + "", content, listEmail);
                                }
                            }
                        }
                    }
                }
            });
            if (listPO.size() > 0) {
                throw new BusinessException(String.format("Purchase Orders { %s } were sent to Supplier. Please check again."), listPO.stream().collect(Collectors.joining(",")));
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public boolean cancelPurchaseOrders(List<Integer> listPurchaseOrderId, String userName) {
        try {
            List<String> listPO = new ArrayList<>();
            listPurchaseOrderId.stream().forEach(i -> {
                Optional<PurchaseOrders> oPurchaseOrder = purchaseOrdersRepository.findById(i);
                if (oPurchaseOrder.isPresent()) {
                    PurchaseOrders purchaseOrders = oPurchaseOrder.get();
                    purchaseOrders.setStatus(GlobalConstant.STATUS_PO_CANCEL);
                    purchaseOrdersRepository.saveAndFlush(purchaseOrders);
                    List<String> listEmail = new ArrayList<>();
                    Optional<User> userEmails = userRepository.findOneByEmail(purchaseOrders.getCreatedBy());
                    Optional<User> userEmailsSupplier = userRepository.findOneByVendor(purchaseOrders.getVendorId());
                    Optional<User> oUserNames = userRepository.findOneByLogin(userName);
                    if (userEmails.isPresent() && userEmailsSupplier.isPresent()) {
                        User userVendor = userEmailsSupplier.get();
                        User userNames = oUserNames.get();
                        listEmail.add(userVendor.getEmail());
                        if (userVendor.getEmail().length() > 0) {
                            if(!userNames.getSupplier()) {
                                String supplier = "Yes4all";
                                String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PO + purchaseOrders.getId(), purchaseOrders.getPoNumber(),supplier, "The purchase order adjustment","cancelled",  "Cancelled");
                                sendMailService.doSendMail("" + purchaseOrders.getPoNumber() + " - The purchase order adjustment has been cancelled by supplier " + supplier + "", content, listEmail);
                            }
                            }
                    }
                }
            });
            if (listPO.size() > 0) {
                throw new BusinessException(String.format("Purchase Orders {%s} were sent to Supplier. Please check again."), listPO.stream().collect(Collectors.joining(",")));
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public boolean removeSkuFromDetails(Integer purchaseOrderId, List<Integer> listIdDetails, String userName) {

        Optional<PurchaseOrders> oPurchaseOrder = purchaseOrdersRepository.findById(purchaseOrderId);
        if (oPurchaseOrder.isPresent()) {
            PurchaseOrders purchaseOrders = oPurchaseOrder.get();
            Long cdcVersionMax = purchaseOrdersDetailRepository.findMaxCdcVersion(purchaseOrders.getId());
            Set<PurchaseOrdersDetail> purchaseOrdersDetail = new HashSet<>();
            purchaseOrders.getPurchaseOrdersDetail().stream().forEach(item -> {
                if (listIdDetails.contains(item.getId())) {
                    item.setDeleted(true);
                    item.setDeletedDate(new Date().toInstant());
                    item.setDeletedBy(userName);
                    item.setCdcVersion(cdcVersionMax + 1);
                }
                purchaseOrdersDetail.add(item);
            });
            purchaseOrders.setPurchaseOrdersDetail(purchaseOrdersDetail);
            purchaseOrders.setTotalItem(purchaseOrdersDetail.stream().filter(i -> !i.getDeleted()).count());
            purchaseOrdersRepository.saveAndFlush(purchaseOrders);
            return true;
        }
        return false;

    }


    @Override
    public PurchaseOrderDetailPageDTO getPurchaseOrdersDetailWithFilter(Integer id, String sku, String aSin, String productName, Integer page, Integer limit) {

        Optional<PurchaseOrders> oPurchaseOrder = purchaseOrdersRepository.findById(id);
        if (oPurchaseOrder.isPresent()) {
            PurchaseOrders purchaseOrders = oPurchaseOrder.get();
            PurchaseOrderDetailPageDTO data = CommonDataUtil.getModelMapper().map(purchaseOrders, PurchaseOrderDetailPageDTO.class);
            // find details with value search is sku/aSin/productName
            List<PurchaseOrdersDetail> purchaseOrdersDetail = purchaseOrdersDetailRepository.findByCondition(sku, aSin, productName, purchaseOrders);
            // convert page entity to page entity dto
            // Page<PurchaseOrderDetailDTO> pagePurchaseOrdersDetailDto = pagePurchaseOrdersDetail.map(this::convertToObjectDto);
            List<PurchaseOrderDetailDTO> purchaseOrdersDetailDTO = purchaseOrdersDetail.stream().map(item -> {
                PurchaseOrderDetailDTO purchaseOrderDetailDTO = new PurchaseOrderDetailDTO();
                BeanUtils.copyProperties(item, purchaseOrderDetailDTO);
                return purchaseOrderDetailDTO;
            }).collect(Collectors.toList());
            data.setPurchaseOrdersDetail(purchaseOrdersDetailDTO);
            if (purchaseOrders.getProformaInvoice() != null) {
                data.setProformaInvoiceId(purchaseOrders.getProformaInvoice().getId());
            }
            return data;
        }
        return null;
    }

    @Override
    public PurchaseOrderDetailPageDTO getPurchaseOrdersDetailWithFilterOrderNo(String orderNo, Integer page, Integer limit) {
        Optional<PurchaseOrders> oPurchaseOrder = purchaseOrdersRepository.findByPoNumberAndIsDeleted(orderNo, false);
        if (oPurchaseOrder.isPresent()) {
            PurchaseOrders purchaseOrders = oPurchaseOrder.get();
            if (purchaseOrders.getStatus() == GlobalConstant.STATUS_PO_CONFIRMED || purchaseOrders.getStatus() == GlobalConstant.STATUS_PO_SENT_VENDOR) {
                PurchaseOrderDetailPageDTO data = CommonDataUtil.getModelMapper().map(purchaseOrders, PurchaseOrderDetailPageDTO.class);
                // find details with value search is sku/aSin/productName
                List<PurchaseOrdersDetail> purchaseOrdersDetail = purchaseOrdersDetailRepository.findByCondition("", "", "", purchaseOrders);
                // convert page entity to page entity dto
                List<PurchaseOrderDetailDTO> purchaseOrdersDetailDTO = purchaseOrdersDetail.stream().map(item -> {
                    PurchaseOrderDetailDTO purchaseOrderDetailDTO = new PurchaseOrderDetailDTO();
                    BeanUtils.copyProperties(item, purchaseOrderDetailDTO);
                    return purchaseOrderDetailDTO;
                }).collect(Collectors.toList());
                data.setPurchaseOrdersDetail(purchaseOrdersDetailDTO);
                return data;
            }
        }
        return null;
    }

    @Override
    public PurchaseOrderDetailPageDTO editSkuPurchaseOrder(Set<PurchaseOrderDetailDTO> purchaseOrderDetailDTO, Integer id) {
        Optional<PurchaseOrders> oPurchaseOrder = purchaseOrdersRepository.findById(id);
        if (oPurchaseOrder.isPresent()) {
            PurchaseOrders purchaseOrder = oPurchaseOrder.get();
            purchaseOrder.setStatus(GlobalConstant.STATUS_PO_NEGOTIATING);
            Set<PurchaseOrdersDetail> purchaseOrdersDetailPrevious = oPurchaseOrder.get().getPurchaseOrdersDetail();
            Set<PurchaseOrdersDetail> purchaseOrdersDetailNew = purchaseOrdersDetailPrevious.stream().map(i -> {
                PurchaseOrdersDetail purchaseOrderDetail = new PurchaseOrdersDetail();
                purchaseOrderDetailDTO.stream().filter(e -> e.getId().equals(i.getId())).forEach(
                    k -> {
                        BeanUtils.copyProperties(k, purchaseOrderDetail);
                        if (i.getAmount() == (k.getAmount())) {
                            purchaseOrderDetail.setAmountPrevious(i.getAmountPrevious());
                        } else {
                            purchaseOrderDetail.setAmountPrevious(i.getAmount());
                        }
                        if (i.getUnitCost() == (k.getUnitCost())) {
                            purchaseOrderDetail.setUnitCostPrevious(i.getUnitCostPrevious());
                        } else {
                            purchaseOrderDetail.setUnitCostPrevious(i.getUnitCost());
                        }
                        if (i.getQtyOrdered() == (k.getQtyOrdered())) {
                            purchaseOrderDetail.setQtyOrderedPrevious(i.getQtyOrderedPrevious());
                        } else {
                            purchaseOrderDetail.setQtyOrderedPrevious(i.getQtyOrdered());
                        }
                        if (i.getPcs() == (k.getPcs())) {
                            purchaseOrderDetail.setPcsPrevious(i.getPcsPrevious());
                        } else {
                            purchaseOrderDetail.setPcsPrevious(i.getPcs());
                        }
                        if (i.getTotalBoxNetWeight() == (k.getTotalBoxNetWeight())) {
                            purchaseOrderDetail.setTotalBoxNetWeight(i.getTotalBoxNetWeightPrevious());
                        } else {
                            purchaseOrderDetail.setTotalBoxNetWeightPrevious(i.getTotalBoxNetWeight());
                        }
                        if (i.getTotalBox() == (k.getTotalBox())) {
                            purchaseOrderDetail.setTotalBoxPrevious(i.getTotalBoxPrevious());
                        } else {
                            purchaseOrderDetail.setTotalBoxPrevious(i.getTotalBox());
                        }
                        if (i.getTotalVolume() == (k.getTotalVolume())) {
                            purchaseOrderDetail.setTotalVolumePrevious(i.getTotalVolumePrevious());
                        } else {
                            purchaseOrderDetail.setTotalVolumePrevious(i.getTotalVolume());
                        }
                        if (i.getTotalBoxCrossWeight() == (k.getTotalBoxCrossWeight())) {
                            purchaseOrderDetail.setTotalBoxCrossWeightPrevious(i.getTotalBoxCrossWeightPrevious());
                        } else {
                            purchaseOrderDetail.setTotalBoxCrossWeightPrevious(i.getTotalBoxCrossWeight());
                        }
                    }
                );
                return purchaseOrderDetail;
            }).collect(Collectors.toSet());
            purchaseOrder.setPurchaseOrdersDetail(purchaseOrdersDetailNew);
            purchaseOrdersRepository.saveAndFlush(purchaseOrder);
            PurchaseOrderDetailPageDTO data = CommonDataUtil.getModelMapper().map(purchaseOrder, PurchaseOrderDetailPageDTO.class);
            // find details with value search is sku/aSin/productName
            List<PurchaseOrdersDetail> purchaseOrdersDetail = purchaseOrdersDetailRepository.findByCondition("", "", "", purchaseOrder);
            // convert page entity to page entity dto
            List<PurchaseOrderDetailDTO> purchaseOrdersDetailDTO = purchaseOrdersDetail.stream().map(item -> {
                PurchaseOrderDetailDTO ePurchaseOrderDetailDTO = new PurchaseOrderDetailDTO();
                BeanUtils.copyProperties(item, ePurchaseOrderDetailDTO);
                return ePurchaseOrderDetailDTO;
            }).collect(Collectors.toList());
            data.setPurchaseOrdersDetail(purchaseOrdersDetailDTO);
            return data;
        }
        return null;

    }

    @Override
    public PurchaseOrdersMainDTO editPurchaseOrder(PurchaseOrdersMainDTO purchaseOrdersMainDTO, Integer id, String userName) {
        Optional<PurchaseOrders> oPurchaseOrder = purchaseOrdersRepository.findById(id);
        if (oPurchaseOrder.isPresent()) {
            //create new purchaser order
            PurchaseOrders purchaseOrdersNew = new PurchaseOrders();
            PurchaseOrders purchaseOrders = oPurchaseOrder.get();
            List<PurchaseOrders> purchaseOrdersList = purchaseOrdersRepository.findAllByPoNumber(purchaseOrders.getPoNumber());
            //find max cdc version update for po old
            List<Long> cdcVersion = purchaseOrdersList.stream().map(i -> i.getCdcVersion() == null ? 0 : i.getCdcVersion()).collect(Collectors.toList());
            Long cdcVersionMax = Collections.max(cdcVersion, null);
            Set<PurchaseOrdersDetail> purchaseOrdersDetails = purchaseOrders.getPurchaseOrdersDetail();
            BeanUtils.copyProperties(purchaseOrdersMainDTO, purchaseOrders);
            purchaseOrders.setPurchaseOrdersDetail(purchaseOrdersDetails);
            purchaseOrders.setId(id);
            purchaseOrders.setCdcVersion(cdcVersionMax + 1);
            purchaseOrders.setDeleted(true);
            purchaseOrders.setProformaInvoice(null);
            purchaseOrders.setStatus(GlobalConstant.STATUS_PO_NEGOTIATING);
            //get proforma invoice
            ProformaInvoice proformaInvoice = purchaseOrders.getProformaInvoice();
            purchaseOrders.setProformaInvoice(null);
            // save PO old
            purchaseOrdersRepository.saveAndFlush(purchaseOrders);
            // copy info from po old to po new
            BeanUtils.copyProperties(purchaseOrders, purchaseOrdersNew);
            purchaseOrdersNew.setCdcVersion(null);
            purchaseOrdersNew.setId(null);
            purchaseOrdersNew.setDeleted(false);
            purchaseOrdersDetails = purchaseOrdersDetails.stream().map(i -> {
                i.setPurchaseOrders(null);
                return i;
            }).collect(Collectors.toSet());
            purchaseOrdersNew.setPurchaseOrdersDetail(purchaseOrdersDetails);
            purchaseOrdersNew.setProformaInvoice(proformaInvoice);
            purchaseOrdersNew.setFromId(purchaseOrders.getId());
            // save PO new
            purchaseOrdersRepository.saveAndFlush(purchaseOrdersNew);
            List<String> listEmail = new ArrayList<>();
            Optional<User> userEmails = userRepository.findOneByEmail(purchaseOrders.getCreatedBy());
            Optional<User> userEmailsSupplier = userRepository.findOneByVendor(purchaseOrders.getVendorId());
            if (userEmails.isPresent()) {
                User user = userEmails.get();
                User userVendor = userEmailsSupplier.get();
                listEmail.add(userVendor.getEmail());
                if (user.getEmail().length() > 0) {
                    //username is supplier can send mail
                    if (userName.equals(userVendor.getLogin())) {
                        String supplier = (userVendor.getLastName() == null ? "" : userVendor.getLastName() + " ") + userVendor.getFirstName();
                        String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PO + purchaseOrders.getId() + "?size=20&page=0", purchaseOrders.getPoNumber(), supplier, "The purchase order", "adjusted", "Adjusted");
                        sendMailService.doSendMail("" + purchaseOrders.getPoNumber() + " - The purchase order has been adjusted by supplier " + supplier + "", content, listEmail);
                    }
                }
            }
            return mappingEntityToDto(purchaseOrdersNew, PurchaseOrdersMainDTO.class);
        }
        return null;
    }

    @Override
    public Page<PurchaseOrdersMainDTO> listingPurchaseOrdersWithCondition(Integer page, Integer limit, Map<String, String> filterParams) {
        Pageable pageable = PageRequestUtil.genPageRequest(page, limit, Sort.Direction.DESC, "created_date");
        Page<PurchaseOrders> data = purchaseOrdersRepository.findByCondition(filterParams.get("poNumber"), filterParams.get("vendor"), filterParams.get("country"), filterParams.get("fulfillmentCenter"), filterParams.get("updateDateFrom"), filterParams.get("updateDateTo"), filterParams.get("status"), filterParams.get("supplier"), pageable);
        return data.map(item -> mappingEntityToDto(item, PurchaseOrdersMainDTO.class));
    }

    public ListDetailPODTO getListSkuFromPO(List<Integer> Id) {
        try {
            ListDetailPODTO listDetailPODTO = new ListDetailPODTO();
            List<ProformaInvoiceDetailDTO> proformaInvoiceDetailDTOList = new ArrayList<>();
            final String[] purchaseOrderNo = {""};
            final Integer[] purchaseOrderId = {null};
            Id.stream().forEach(i -> {
                Optional<PurchaseOrders> oPurchaseOrders = purchaseOrdersRepository.findById(i);
                if (oPurchaseOrders.isPresent()) {
                    PurchaseOrders purchaseOrders = oPurchaseOrders.get();
                    purchaseOrderNo[0] = purchaseOrders.getPoNumber();
                    purchaseOrderId[0] = purchaseOrders.getId();
                    Set<PurchaseOrdersDetail> purchaseOrdersDetail = purchaseOrders.getPurchaseOrdersDetail();
                    purchaseOrdersDetail.stream().filter(k -> !k.isDeleted()).forEach(item -> {
                        ProformaInvoiceDetailDTO proformaInvoiceDetailDTO = new ProformaInvoiceDetailDTO();
                        proformaInvoiceDetailDTO.setSku(item.getSku());
                        proformaInvoiceDetailDTO.setBarcode(item.getAsin());
                        proformaInvoiceDetailDTO.setFromSo(item.getFromSo());
                        proformaInvoiceDetailDTO.setProductTitle(item.getProductName());
                        proformaInvoiceDetailDTO.setQty(Math.toIntExact(item.getQtyOrdered()));
                        proformaInvoiceDetailDTO.setQtyOrdered(item.getQtyOrdered());
                        proformaInvoiceDetailDTO.setShipDate(item.getShipDate());
                        proformaInvoiceDetailDTO.setCtn(item.getTotalBox());
                        proformaInvoiceDetailDTO.setCbmTotal(item.getTotalVolume());
                        proformaInvoiceDetailDTO.setAmount(item.getAmount());
                        proformaInvoiceDetailDTO.setGrossWeight(item.getTotalBoxCrossWeight());
                        proformaInvoiceDetailDTO.setNetWeight(item.getTotalBoxNetWeight());
                        proformaInvoiceDetailDTOList.add(proformaInvoiceDetailDTO);
                    });
                }
            });
            listDetailPODTO.setDetail(proformaInvoiceDetailDTOList);
            listDetailPODTO.setPurchaserOrderNo(purchaseOrderNo[0]);
            listDetailPODTO.setPurchaserOrderId(purchaseOrderId[0]);
            return listDetailPODTO;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    private <T> T mappingEntityToDto(PurchaseOrders purchaseOrders, Class<T> clazz) {
        try {

            T dto = clazz.getDeclaredConstructor().newInstance();
            CommonDataUtil.getModelMapper().map(purchaseOrders, dto);
            ProformaInvoice proformaInvoice = purchaseOrders.getProformaInvoice();
            Integer proformaInvoiceId = null;
            Integer commercialInvoiceId = null;
            if (proformaInvoice != null) {
                proformaInvoiceId = proformaInvoice.getId();
                CommercialInvoice commercialInvoice = proformaInvoice.getCommercialInvoice();
                if (commercialInvoice != null) {
                    commercialInvoiceId = commercialInvoice.getId();
                }
            }
            if (dto instanceof PurchaseOrdersMainDTO) {
                String fromSoStr = purchaseOrdersDetailRepository.findAllFromSOByPOId(purchaseOrders.getId());
                clazz.getMethod("setFromSo", String.class).invoke(dto, fromSoStr);
                clazz.getMethod("setProformaInvoiceId", Integer.class).invoke(dto, proformaInvoiceId);
                clazz.getMethod("setCommercialInvoiceId", Integer.class).invoke(dto, commercialInvoiceId);
            }
            return dto;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return null;
        }
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
        createCell(sheet, row, 5, "Unit Price", style);
        createCell(sheet, row, 6, "Amount", style);
        createCell(sheet, row, 7, "PCS/CARTON", style);
        createCell(sheet, row, 8, "TOTAL BOX", style);
        createCell(sheet, row, 9, "TOTAL VOLUME (CBM)", style);
        createCell(sheet, row, 10, "TOTAL NET WEIGHT", style);
        createCell(sheet, row, 11, "TOTAL GROSS WEIGHT", style);
        createCell(sheet, row, 12, "MAKE-TO-STOCK", style);

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
        } else if (valueOfCell instanceof Double) {
            cell.setCellValue((Double) valueOfCell);
        } else {
            cell.setCellValue((String) valueOfCell);
        }
        cell.setCellStyle(style);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
    }

    public Workbook generateExcelFile(Integer id, XSSFWorkbook workbook) throws IOException {
        PurchaseOrders purchaseOrders = purchaseOrdersRepository.findById(id).get();
        XSSFSheet sheet = workbook.createSheet(purchaseOrders.getPoNumber());
        int rowCount = 1;
        writeHeaderLine(workbook, sheet);
        CellStyle style = workbook.createCellStyle();
        XSSFCellStyle styleDate = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        styleDate.setDataFormat(createHelper.createDataFormat().getFormat("d-mmm-yy"));
        Set<PurchaseOrdersDetail> purchaseOrdersDetailSet = purchaseOrders.getPurchaseOrdersDetail().stream().filter(k -> !k.isDeleted()).collect(Collectors.toSet());
        for (PurchaseOrdersDetail record : purchaseOrdersDetailSet) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(sheet, row, columnCount++, record.getFromSo(), style);
            createCell(sheet, row, columnCount++, record.getSku(), style);
            createCell(sheet, row, columnCount++, record.getAsin(), style);
            createCell(sheet, row, columnCount++, record.getProductName(), style);
            createCell(sheet, row, columnCount++, record.getQtyOrdered(), style);
            createCell(sheet, row, columnCount++, record.getUnitCost(), style);
            createCell(sheet, row, columnCount++, record.getAmount(), style);
            createCell(sheet, row, columnCount++, record.getPcs(), style);
            createCell(sheet, row, columnCount++, record.getTotalBox(), style);
            createCell(sheet, row, columnCount++, record.getTotalVolume(), style);
            createCell(sheet, row, columnCount++, record.getTotalBoxNetWeight(), style);
            createCell(sheet, row, columnCount++, record.getTotalBoxCrossWeight(), style);
            createCell(sheet, row, columnCount++, record.getMakeToStock(), style);
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
