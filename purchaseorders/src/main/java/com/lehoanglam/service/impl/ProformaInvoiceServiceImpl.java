package com.yes4all.service.impl;

import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.PageRequestUtil;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.*;
import com.yes4all.domain.model.*;
import com.yes4all.repository.*;
import com.yes4all.service.ProformaInvoiceService;
import com.yes4all.service.SendMailService;
import liquibase.pro.packaged.A;
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


/**
 * Service Implementation for managing {@link ProformaInvoice}.
 */
@Service
@Transactional
public class ProformaInvoiceServiceImpl implements ProformaInvoiceService {

    private final Logger log = LoggerFactory.getLogger(ProformaInvoiceServiceImpl.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SendMailService sendMailService;
    @Value("${attribute.link.url}")
    private String linkPOMS;

    private static final String LINK_DETAIL_PI = "/proforma-invoice/detail/";
    @Autowired
    private ProformaInvoiceRepository proformaInvoiceRepository;
    private static final Boolean IS_DELETED = false;
    @Autowired
    private PurchaseOrdersRepository purchaseOrdersRepository;
    @Autowired
    private ProformaInvoiceDetailRepository proformaInvoiceDetailRepository;
    @Autowired
    private ResourceServiceImpl resourceService;
    @Autowired
    private ResourceRepository resourceRepository;

    @Override
    public Page<ProformaInvoiceMainDTO> listingProformaInvoiceWithCondition(Integer page, Integer limit, Map<String, String> filterParams) {
        Pageable pageable = PageRequestUtil.genPageRequest(page, limit, Sort.Direction.DESC, "created_date");
        Page<ProformaInvoice> data = proformaInvoiceRepository.findByCondition(filterParams.get("updatedBy"), filterParams.get("orderNo"), filterParams.get("fromSO"), filterParams.get("poNumber"), filterParams.get("term"), filterParams.get("shipDateFrom"), filterParams.get("shipDateTo"), filterParams.get("amountFrom"), filterParams.get("amountTo"), filterParams.get("status"), filterParams.get("updateDateFrom"), filterParams.get("updateDateTo"),  filterParams.get("supplier"),pageable);
        return data.map(item -> mappingEntityToDto(item, ProformaInvoiceMainDTO.class));
    }

    @Override
    public boolean removeProformaInvoice(List<Integer> listId, String userName) {
        try {
            listId.stream().forEach(i -> {
                List<PurchaseOrders> purchaseOrdersList = new ArrayList<>();
                Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findById(i);
                if (oProformaInvoice.isPresent()) {
                    ProformaInvoice proformaInvoice = oProformaInvoice.get();
                    if (proformaInvoice.getStatus() == 3) {
                        throw new BusinessException(String.format("Can't Deleted with status Confirmed"));
                    }
                    if (proformaInvoice.isDeleted()) {
                        throw new BusinessException(String.format("Id Proforma Invoice %s already deleted ", proformaInvoice.getId()));
                    }
                    proformaInvoice.setDeleted(true);
                    proformaInvoice.setDeletedDate(new Date().toInstant());
                    proformaInvoice.setDeletedBy(userName);
                    Long cdcVersionMaxDetail = proformaInvoiceDetailRepository.findMaxCdcVersion(proformaInvoice.getId());
                    Set<ProformaInvoiceDetail> proformaInvoiceDetail = proformaInvoice.getProformaInvoiceDetail().stream().map(item -> {
                        item.setDeleted(true);
                        item.setDeletedDate(new Date().toInstant());
                        item.setDeletedBy(userName);
                        item.setCdcVersion(cdcVersionMaxDetail + 1);
                        return item;
                    }).collect(Collectors.toSet());
                    proformaInvoice.setProformaInvoiceDetail(proformaInvoiceDetail);
                    purchaseOrdersRepository.saveAllAndFlush(purchaseOrdersList);
                }
            });
            return true;
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public ProformaInvoiceDTO getProformaInvoiceDetail(Integer id) {

        Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findById(id);
        if (oProformaInvoice.isPresent()) {
            ProformaInvoice proformaInvoice = oProformaInvoice.get();
            Set<ProformaInvoiceDetail> proformaInvoiceDetail = proformaInvoiceDetailRepository.findByIsDeletedAndProformaInvoice(IS_DELETED, proformaInvoice);
            proformaInvoice.setProformaInvoiceDetail(proformaInvoiceDetail);
            ProformaInvoiceDTO result = mappingEntityToDto(proformaInvoice, ProformaInvoiceDTO.class);
            result.setPurchaserOrderId(proformaInvoice.getPurchaseOrders().getId());
            result.setPurchaserOrderNo(proformaInvoice.getPurchaseOrders().getPoNumber());
            List<Resource> resourcesListing = resourceRepository.findByFileTypeAndProformaInvoiceId(GlobalConstant.FILE_UPLOAD, proformaInvoice.getId());
            if (CommonDataUtil.isNotEmpty(resourcesListing)) {
                List<ResourceDTO> resources = resourcesListing.parallelStream().map(item -> {
                    ResourceDTO data = new ResourceDTO();
                    data.setSrc(item.getPath());
                    data.setId(item.getId());
                    data.setModule(item.getModule());
                    data.setName(item.getName());
                    data.setType(item.getType());
                    data.setSize(item.getFileSize());
                    return data;
                }).collect(Collectors.toList());
                result.setFileUploads(resources);
            }
            return result;
        }
        return null;
    }

    @Override
    public ProformaInvoiceDTO createProformaInvoice(ProformaInvoiceDTO proformaInvoiceDTO) {
        try {
            ProformaInvoice proformaInvoice = new ProformaInvoice();
            BeanUtils.copyProperties(proformaInvoiceDTO, proformaInvoice);
            Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findByOrderNoAndIsDeleted(proformaInvoiceDTO.getOrderNo(), false);
            if (oProformaInvoice.isPresent()) {
                throw new BusinessException(String.format("Proforma Invoice No %s already exists.", proformaInvoiceDTO.getOrderNo()));
            }
            proformaInvoice.setStatus(GlobalConstant.STATUS_PI_NEW);
            //get list PO
            Optional<PurchaseOrders> oPurchaseOrders = purchaseOrdersRepository.findById(proformaInvoiceDTO.getPurchaserOrderId());
            if (oPurchaseOrders.isPresent()) {
                if (oPurchaseOrders.get().getDeleted()) {
                    throw new BusinessException(String.format("id PO %s deleted ", proformaInvoiceDTO.getPurchaserOrderId()));
                }
            } else {
                throw new BusinessException(String.format("id PO %s not exists ", proformaInvoiceDTO.getPurchaserOrderId()));
            }
            PurchaseOrders purchaseOrders=oPurchaseOrders.get();
            //get details PI from DTO
            Set<ProformaInvoiceDetail> detailSet = proformaInvoiceDTO.getProformaInvoiceDetail().parallelStream().map(item -> {
                ProformaInvoiceDetail proformaInvoiceDetail;
                proformaInvoiceDetail = CommonDataUtil.getModelMapper().map(item, ProformaInvoiceDetail.class);
                return proformaInvoiceDetail;
            }).collect(Collectors.toSet());
            //Remove new row and deleted
            detailSet.stream().filter(i-> i.isDeleted()==false).collect(Collectors.toSet());
            if (detailSet.isEmpty()) {
                throw new BusinessException(String.format("List sku must not empty"));
            }
            proformaInvoice.setProformaInvoiceDetail(detailSet);
            proformaInvoice.setAmount(detailSet.stream().filter(i -> !i.isDeleted()).map(x -> Objects.isNull(x.getAmount()) ? 0 : x.getAmount()).reduce(0.0, Double::sum));
            proformaInvoice.setCtn(detailSet.stream().filter(i -> !i.isDeleted()).map(x -> Objects.isNull(x.getCtn()) ? 0 : x.getCtn()).reduce(0.0, Double::sum));
            proformaInvoice.setGrossWeight(detailSet.stream().filter(i -> !i.isDeleted()).map(x -> Objects.isNull(x.getGrossWeight()) ? 0 : x.getGrossWeight()).reduce(0.0, Double::sum));
            proformaInvoice.setCbmTotal(detailSet.stream().filter(i -> !i.isDeleted()).map(x -> Objects.isNull(x.getCbmTotal()) ? 0 : x.getCbmTotal()).reduce(0.0, Double::sum));
            proformaInvoice.setPurchaseOrders(purchaseOrders);
            proformaInvoiceRepository.saveAndFlush(proformaInvoice);
            return mappingEntityToDto(proformaInvoice, ProformaInvoiceDTO.class);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }

    }

    @Override
    public ProformaInvoiceDTO updateProformaInvoice(Integer iD, ProformaInvoiceDTO proformaInvoiceDTO) throws IOException, URISyntaxException {
        try {
            ProformaInvoice proformaInvoice = proformaInvoiceRepository.findById(iD).get();
             proformaInvoice.setUpdatedDate(new Date().toInstant());
            //get list PO

            Optional<PurchaseOrders> oPurchaseOrders = purchaseOrdersRepository.findById(proformaInvoiceDTO.getPurchaserOrderId());
            if (oPurchaseOrders.isPresent()) {
                if (oPurchaseOrders.get().getDeleted()) {
                    throw new BusinessException(String.format("id PO %s deleted ", proformaInvoiceDTO.getPurchaserOrderId()));
                }
            } else {
                throw new BusinessException(String.format("id PO %s not exists ", proformaInvoiceDTO.getPurchaserOrderId()));
            }
            PurchaseOrders purchaseOrders=oPurchaseOrders.get();
            //get details previous update
            BeanUtils.copyProperties(proformaInvoiceDTO, proformaInvoice);
            proformaInvoice.setId(iD);
            //get all sku deleted when update and update isDelete=true
            //Get all qty old of sku for revert qty used in Purchase Order
            Set<ProformaInvoiceDetail> detailSet = proformaInvoiceDTO.getProformaInvoiceDetail().parallelStream().map(item -> {
                ProformaInvoiceDetail proformaInvoiceDetail;
                proformaInvoiceDetail = CommonDataUtil.getModelMapper().map(item, ProformaInvoiceDetail.class);
                return proformaInvoiceDetail;
            }).collect(Collectors.toSet());
            proformaInvoice.setProformaInvoiceDetail(detailSet);
            proformaInvoice.setAmount(detailSet.stream().filter(i -> !i.isDeleted()).map(x -> Objects.isNull(x.getAmount()) ? 0 : x.getAmount()).reduce(0.0, Double::sum));
            proformaInvoice.setCtn(detailSet.stream().filter(i -> !i.isDeleted()).map(x -> Objects.isNull(x.getCtn()) ? 0 : x.getCtn()).reduce(0.0, Double::sum));
            proformaInvoice.setGrossWeight(detailSet.stream().filter(i -> !i.isDeleted()).map(x -> Objects.isNull(x.getGrossWeight()) ? 0 : x.getGrossWeight()).reduce(0.0, Double::sum));
            proformaInvoice.setCbmTotal(detailSet.stream().filter(i -> !i.isDeleted()).map(x -> Objects.isNull(x.getCbmTotal()) ? 0 : x.getCbmTotal()).reduce(0.0, Double::sum));
            proformaInvoice.setPurchaseOrders(purchaseOrders);
            proformaInvoiceRepository.saveAndFlush(proformaInvoice);

            return mappingEntityToDto(proformaInvoice, ProformaInvoiceDTO.class);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());

        }
    }


    @Override
    public boolean confirmed(List<Integer> Id) {
        try {
            List<String> listPI = new ArrayList<>();
            Id.stream().forEach(i -> {
                Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findById(i);
                if (oProformaInvoice.isPresent()) {
                    ProformaInvoice proformaInvoice = oProformaInvoice.get();
                    if (proformaInvoice.getStatus() != 0 && proformaInvoice.getStatus() != 1) {
                        listPI.add(proformaInvoice.getOrderNo());
                    } else {
                        proformaInvoice.setStatus(GlobalConstant.STATUS_PI_CONFIRMED);
                        proformaInvoiceRepository.saveAndFlush(proformaInvoice);
                    }
                }
            });
            if (listPI.size() > 0) {
                throw new BusinessException(String.format("Proforma Invoice {%s} were confirmed by Yes4All. Please check again."), listPI.stream().collect(Collectors.joining(",")));
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean send(List<Integer> Id,String userName) {
        try {
            List<String> listPI = new ArrayList<>();
            Id.stream().forEach(i -> {
                Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findById(i);
                if (oProformaInvoice.isPresent()) {
                    ProformaInvoice proformaInvoice = oProformaInvoice.get();
                    if (proformaInvoice.getStatus() != 0) {
                        listPI.add(proformaInvoice.getOrderNo());
                    } else {
                        proformaInvoice.setStatus(GlobalConstant.STATUS_PI_SENT_BUYER);
                        proformaInvoiceRepository.saveAndFlush(proformaInvoice);
                        List<String> listEmail = new ArrayList<>();
                        Optional<User> userEmails = userRepository.findOneByEmail(proformaInvoice.getCreatedBy());
                        Optional<User> userEmailsSupplier = userRepository.findOneByVendor(proformaInvoice.getVendorCode());
                        if(userEmails.isPresent() && userEmailsSupplier.isPresent()){
                            User user=userEmails.get();
                            User userVendor=userEmailsSupplier.get();
                            listEmail.add(user.getEmail());
                            if(user.getEmail().length()>0) {
                                if (userName.equals(userVendor.getLogin())) {
                                    String supplier = (userVendor.getLastName() == null ? "" : userVendor.getLastName() + " ") + userVendor.getFirstName();
                                    String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PI + proformaInvoice.getId() + "?size=20&page=0", proformaInvoice.getPurchaseOrders().getPoNumber(),supplier, "The Proforma invoice for PO","created" , "CreatedPI");
                                    sendMailService.doSendMail("" + proformaInvoice.getOrderNo() + " - The Proforma Invoice has been created by supplier " + supplier + "", content, listEmail);
                                }
                           }
                        }
                    }
                }
            });
            if (listPI.size() > 0) {
                throw new BusinessException(String.format("Proforma Invoice { %s } were sent to Yes4All. Please check again."), listPI.stream().collect(Collectors.joining(",")));
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    private <T> T mappingEntityToDto(ProformaInvoice proformaInvoice, Class<T> clazz) {
        try {
            T dto = clazz.getDeclaredConstructor().newInstance();
            CommonDataUtil.getModelMapper().map(proformaInvoice, dto);
            ProformaInvoice objectModel;
            if (proformaInvoice.getProformaInvoiceDetail().isEmpty()) {
                objectModel = proformaInvoiceRepository.findById(proformaInvoice.getId()).get();
            } else {
                objectModel = proformaInvoice;
            }
            Set<ProformaInvoiceDetail> detailSet = objectModel.getProformaInvoiceDetail().stream().filter(i -> !i.isDeleted()).collect(Collectors.toSet());
            String fromSoStr = detailSet.stream().map(i -> i.getFromSo()).distinct().collect(Collectors.joining(", "));
            if (dto instanceof ProformaInvoiceDTO) {
                clazz.getMethod("setProformaInvoiceDetail", Set.class).invoke(dto, detailSet);
            } else if (dto instanceof ProformaInvoiceMainDTO) {
                clazz.getMethod("setFromSo", String.class).invoke(dto, fromSoStr);
            }

            return dto;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return null;
        }
    }

}
