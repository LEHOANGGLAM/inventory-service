package com.yes4all.service.impl;

import com.yes4all.common.constants.Constant;
import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.PageRequestUtil;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.*;
import com.yes4all.domain.model.*;
import com.yes4all.repository.*;
import com.yes4all.service.CommercialInvoiceService;
import com.yes4all.service.SendMailService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Service Implementation for managing {@link CommercialInvoice}.
 */
@Service
@Transactional
public class CommercialInvoiceServiceImpl implements CommercialInvoiceService {

    private final Logger log = LoggerFactory.getLogger(CommercialInvoiceServiceImpl.class);

    @Autowired
    private CommercialInvoiceRepository commercialInvoiceRepository;
    private static final Boolean IS_DELETED = false;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SendMailService sendMailService;
    @Value("${attribute.link.url}")
    private String linkPOMS;

    private static final String LINK_DETAIL_PI = "/commercial-invoice/detail";
    @Autowired
    private CommercialInvoiceDetailRepository commercialInvoiceDetailRepository;

    @Autowired
    private ProformaInvoiceRepository proformaInvoiceRepository;
    @Autowired
    private PurchaseOrdersRepository purchaseOrdersRepository;
    @Autowired
    private ResourceServiceImpl resourceService;
    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private BookingPackingListRepository bookingPackingListRepository;


    @Override
    public Page<CommercialInvoiceMainDTO> listingCommercialInvoiceWithCondition(Integer page, Integer limit, Map<String, String> filterParams) {
        Pageable pageable = PageRequestUtil.genPageRequest(page, limit, Sort.Direction.DESC, "created_date");
        Page<CommercialInvoice> data = commercialInvoiceRepository.findByCondition(filterParams.get("fromSO"), filterParams.get("invoiceNo"), filterParams.get("term"), filterParams.get("shipDateFrom"), filterParams.get("shipDateTo"), filterParams.get("amountFrom"), filterParams.get("amountTo"), filterParams.get("status"), filterParams.get("updateDateFrom"), filterParams.get("updateDateTo"),filterParams.get("supplier"), pageable);
        return data.map(item -> mappingEntityToDto(item, CommercialInvoiceMainDTO.class));
    }

    @Override
    public boolean removeCommercialInvoice(List<Integer> listId, String userName) {
        try {
            listId.stream().forEach(i -> {
                List<ProformaInvoice> proformaInvoiceList = new ArrayList<>();
                Optional<CommercialInvoice> oCommercialInvoice = commercialInvoiceRepository.findById(i);
                if (oCommercialInvoice.isPresent()) {
                    CommercialInvoice commercialInvoice = oCommercialInvoice.get();
                    if (commercialInvoice.getStatus() == 2) {
                        throw new BusinessException(String.format("Can't Deleted with status Confirmed"));
                    }
                    if (commercialInvoice.isDeleted()) {
                        throw new BusinessException(String.format("Id Commercial Invoice %s already deleted ", commercialInvoice.getInvoiceNo()));
                    }
                    commercialInvoice.setDeleted(true);
                    commercialInvoice.setDeletedDate(new Date().toInstant());
                    commercialInvoice.setDeletedBy(userName);
                    Long cdcVersionMaxDetail = commercialInvoiceDetailRepository.findMaxCdcVersion(commercialInvoice.getId());
                    Set<CommercialInvoiceDetail> commercialInvoiceDetail = commercialInvoice.getCommercialInvoiceDetail().stream().map(item -> {
                        item.setDeleted(true);
                        item.setDeletedDate(new Date().toInstant());
                        item.setDeletedBy(userName);
                        item.setCdcVersion(cdcVersionMaxDetail + 1);
                        return item;
                    }).collect(Collectors.toSet());
                    commercialInvoice.setCommercialInvoiceDetail(commercialInvoiceDetail);
                    proformaInvoiceRepository.saveAllAndFlush(proformaInvoiceList);
                }
            });
            return true;
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());

        }
    }


    @Override
    @Transactional(readOnly = true)
    public CommercialInvoiceDTO getCommercialInvoiceDetail(Integer id) {

        Optional<CommercialInvoice> oCommercialInvoice = commercialInvoiceRepository.findById(id);
        if (oCommercialInvoice.isPresent()) {
            CommercialInvoice commercialInvoice = oCommercialInvoice.get();
            Set<CommercialInvoiceDetail> commercialInvoiceDetail = commercialInvoiceDetailRepository.findByIsDeletedAndCommercialInvoice(IS_DELETED, commercialInvoice);
            commercialInvoice.setCommercialInvoiceDetail(commercialInvoiceDetail);
            CommercialInvoiceDTO result = CommonDataUtil.getModelMapper().map(commercialInvoice, CommercialInvoiceDTO.class);
            result.setProformaInvoiceId(commercialInvoice.getProformaInvoice().getId());
            List<Resource> resourcesListing = resourceRepository.findByFileTypeAndCommercialInvoiceId(GlobalConstant.FILE_UPLOAD, commercialInvoice.getId());
            if (CommonDataUtil.isNotEmpty(resourcesListing)) {
                List<ResourceDTO> resources = resourcesListing.parallelStream().map(item -> {
                    ResourceDTO data = new ResourceDTO();
                    data.setSrc(item.getPath());
                    data.setId(item.getId());
                    data.setModule(item.getModule());
                    return data;
                }).collect(Collectors.toList());
                result.setFileUploads(resources);
            }
            return result;

        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public CommercialInvoiceDTO getCommercialInvoiceDetailWithInvoiceNo(String invoiceNo) {

        Optional<CommercialInvoice> oCommercialInvoice = commercialInvoiceRepository.findByInvoiceNoAndIsDeleted(invoiceNo, false);
        if (oCommercialInvoice.isPresent()) {
            CommercialInvoice commercialInvoice = oCommercialInvoice.get();
            Set<CommercialInvoiceDetail> CommercialInvoiceDetail = commercialInvoiceDetailRepository.findByIsDeletedAndCommercialInvoice(IS_DELETED, commercialInvoice);
            commercialInvoice.setCommercialInvoiceDetail(CommercialInvoiceDetail);
            CommercialInvoiceDTO result = CommonDataUtil.getModelMapper().map(commercialInvoice, CommercialInvoiceDTO.class);
            List<Resource> resourcesListing = resourceRepository.findByFileTypeAndCommercialInvoiceId(GlobalConstant.FILE_UPLOAD, commercialInvoice.getId());
            if (CommonDataUtil.isNotEmpty(resourcesListing)) {
                List<ResourceDTO> resources = resourcesListing.parallelStream().map(item -> {
                    ResourceDTO data = new ResourceDTO();
                    data.setSrc(item.getPath());
                    data.setId(item.getId());
                    data.setModule(item.getModule());
                    return data;
                }).collect(Collectors.toList());
                result.setFileUploads(resources);
            }
            return result;

        }
        return null;
    }

    @Override
    public CommercialInvoiceDTO createCommercialInvoice(CommercialInvoiceDTO commercialInvoiceDTO) throws IOException, URISyntaxException {
        try {
            CommercialInvoice commercialInvoice = new CommercialInvoice();
            BeanUtils.copyProperties(commercialInvoiceDTO, commercialInvoice);
            Optional<CommercialInvoice> oCommercialInvoiceDuplicate = commercialInvoiceRepository.findByInvoiceNoAndIsDeleted(commercialInvoiceDTO.getInvoiceNo(), false);
            if (oCommercialInvoiceDuplicate.isPresent()) {
                throw new BusinessException(String.format("Invoice No %s already exists.", commercialInvoiceDTO.getInvoiceNo()));
            }
            commercialInvoice.setStatus(GlobalConstant.STATUS_CI_NEW);
            //pi
            Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findById(commercialInvoiceDTO.getProformaInvoiceId());
            //packing list
            Optional<BookingPackingList> oBookingPackingList = bookingPackingListRepository.findById(commercialInvoiceDTO.getBookingPackingListId());

            //get details PI from DTO
            Set<CommercialInvoiceDetail> detailSet = commercialInvoiceDTO.getCommercialInvoiceDetail().parallelStream().map(item -> {
                CommercialInvoiceDetail commercialInvoiceDetail;
                commercialInvoiceDetail = CommonDataUtil.getModelMapper().map(item, CommercialInvoiceDetail.class);
                Optional<ProformaInvoice> oProformaInvoiceDetail = proformaInvoiceRepository.findByOrderNoAndIsDeleted(item.getProformaInvoiceNo(), false);
                if (oProformaInvoiceDetail.isPresent()) {
                    ProformaInvoice proformaInvoiceDetail = oProformaInvoiceDetail.get();
                    Set<ProformaInvoiceDetail> proformaInvoiceDetailSet = proformaInvoiceDetail.getProformaInvoiceDetail();
                    ProformaInvoiceDetail proformaInvoiceDetailElement = proformaInvoiceDetailSet.stream().filter(k -> k.getFromSo().equals(commercialInvoiceDetail.getFromSo())
                        && k.getSku().equals(commercialInvoiceDetail.getSku())).findFirst().orElse(null);
                    if (proformaInvoiceDetailElement == null) {
                        throw new BusinessException(String.format("Sku= %s ; SO= %s not exists in PI",commercialInvoiceDetail.getSku(),commercialInvoiceDetail.getFromSo()));
                    } else {
                        commercialInvoiceDetail.setUnitPrice(proformaInvoiceDetailElement.getUnitPrice());
                        commercialInvoiceDetail.setAmount(proformaInvoiceDetailElement.getUnitPrice()*commercialInvoiceDetail.getQty());
                    }
                } else {
                    throw new BusinessException(String.format("ProformaInvoice Not exists!"));
                }
                return commercialInvoiceDetail;
            }).collect(Collectors.toSet());
            commercialInvoice.setCommercialInvoiceDetail(detailSet);
            commercialInvoice.setAmount(detailSet.stream().filter(i -> !i.isDeleted()).map(x -> Objects.isNull(x.getAmount()) ? 0 : x.getAmount()).reduce(0.0, Double::sum));
            if (oProformaInvoice.isPresent()) {
                commercialInvoice.setProformaInvoice(oProformaInvoice.get());
            }
            if (oBookingPackingList.isPresent()) {
                commercialInvoice.setBookingPackingList(oBookingPackingList.get());
            }
            commercialInvoiceRepository.saveAndFlush(commercialInvoice);

            return mappingEntityToDto(commercialInvoice, CommercialInvoiceDTO.class);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ex.getMessage());
        }

    }

    @Override
    public CommercialInvoiceDTO updateCommercialInvoice(Integer iD, CommercialInvoiceDTO commercialInvoiceDTO) throws IOException, URISyntaxException {

        try {
            CommercialInvoice commercialInvoice = commercialInvoiceRepository.findById(iD).get();
            commercialInvoice.setUpdatedDate(new Date().toInstant());

            //pi
            Optional<ProformaInvoice> oProformaInvoice = proformaInvoiceRepository.findById(commercialInvoiceDTO.getProformaInvoiceId());
            //packing list
            Optional<BookingPackingList> oBookingPackingList = bookingPackingListRepository.findById(commercialInvoiceDTO.getBookingPackingListId());
            Set<CommercialInvoiceDetail> detailSet = commercialInvoiceDTO.getCommercialInvoiceDetail().parallelStream().map(item -> {
                CommercialInvoiceDetail commercialInvoiceDetail;
                commercialInvoiceDetail = CommonDataUtil.getModelMapper().map(item, CommercialInvoiceDetail.class);
                return commercialInvoiceDetail;
            }).collect(Collectors.toSet());
            BeanUtils.copyProperties(commercialInvoiceDTO, commercialInvoice);
            commercialInvoice.setId(iD);
            commercialInvoice.setCommercialInvoiceDetail(detailSet);
            if (oProformaInvoice.isPresent()) {
                commercialInvoice.setProformaInvoice(oProformaInvoice.get());
            }
            if (oBookingPackingList.isPresent()) {
                commercialInvoice.setBookingPackingList(oBookingPackingList.get());
            }
            commercialInvoiceRepository.saveAndFlush(commercialInvoice);
            return mappingEntityToDto(commercialInvoice, CommercialInvoiceDTO.class);
        } catch (Exception ex) {
            log.error(ex.getMessage());

            throw new BusinessException(ex.getMessage());
        }
    }


    @Override
    public boolean confirmed(List<Integer> Id) {
        try {
            List<String> listCI = new ArrayList<>();
            Id.stream().forEach(i -> {
                Optional<CommercialInvoice> oCommercialInvoice = commercialInvoiceRepository.findById(i);
                if (oCommercialInvoice.isPresent()) {
                    CommercialInvoice commercialInvoice = oCommercialInvoice.get();
                    if (commercialInvoice.getStatus() != 0 && commercialInvoice.getStatus() != 1) {
                        listCI.add(commercialInvoice.getInvoiceNo());
                    } else {
                        commercialInvoice.setStatus(GlobalConstant.STATUS_CI_CONFIRMED);
                        commercialInvoiceRepository.saveAndFlush(commercialInvoice);
                    }
                }
            });
            if (listCI.size() > 0) {
                throw new BusinessException(String.format("Invoice {%s} were confirmed by Yes4all. Please check again."), listCI.stream().collect(Collectors.joining(",")));
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
            List<String> listCI = new ArrayList<>();
            Id.stream().forEach(i -> {
                Optional<CommercialInvoice> oCommercialInvoice = commercialInvoiceRepository.findById(i);
                if (oCommercialInvoice.isPresent()) {
                    CommercialInvoice commercialInvoice = oCommercialInvoice.get();
                    if (commercialInvoice.getStatus() != 0) {
                        listCI.add(commercialInvoice.getInvoiceNo());
                    } else {
                        commercialInvoice.setStatus(GlobalConstant.STATUS_CI_SENT_BUYER);
                        commercialInvoiceRepository.saveAndFlush(commercialInvoice);
                        List<String> listEmail = new ArrayList<>();
                        Optional<User> userEmails = userRepository.findOneByEmail(commercialInvoice.getCreatedBy());
                        Optional<User> userEmailsSupplier = userRepository.findOneByVendor(commercialInvoice.getVendorCode());
                        if(userEmails.isPresent()){
                            User user=userEmails.get();
                            User userVendor=userEmailsSupplier.get();
                            listEmail.add(user.getEmail());
                            if(user.getEmail().length()>0) {
                                if (userName.equals(userVendor.getLogin())) {
                                    String supplier = (userVendor.getLastName() == null ? "" : userVendor.getLastName() + " ") + userVendor.getFirstName();
                                    String content = CommonDataUtil.contentMail(linkPOMS + LINK_DETAIL_PI + commercialInvoice.getId() + "?size=20&page=0", commercialInvoice.getProformaInvoice().getPurchaseOrders().getPoNumber(),supplier, "The Commercial invoice for PO", "created", "CreatedCI");
                                    sendMailService.doSendMail("" + commercialInvoice.getInvoiceNo() + " - The Commercial Invoice has been created by supplier " + supplier + "", content, listEmail);
                                }
                                }
                        }
                    }
                }
            });
            if (listCI.size() > 0) {
                throw new BusinessException(String.format("Invoice {%s} were sent to Yes4All. Please check again."), listCI.stream().collect(Collectors.joining(",")));
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    private <T> T mappingEntityToDto(CommercialInvoice commercialInvoice, Class<T> clazz) {
        try {
            T dto = clazz.getDeclaredConstructor().newInstance();
            CommonDataUtil.getModelMapper().map(commercialInvoice, dto);
            CommercialInvoice cIModel = new CommercialInvoice();
            if (commercialInvoice.getCommercialInvoiceDetail().isEmpty()) {
                cIModel = commercialInvoiceRepository.findById(commercialInvoice.getId()).get();
            } else {
                cIModel = commercialInvoice;
            }
            Set<CommercialInvoiceDetail> listDetailPI = cIModel.getCommercialInvoiceDetail().stream().filter(i -> !i.isDeleted()).collect(Collectors.toSet());
            String fromSoStr = listDetailPI.stream().map(i -> i.getFromSo()).distinct().collect(Collectors.joining(", "));
            Integer bookingPackingListId = null;
            if (commercialInvoice.getBookingPackingList() != null) {

                bookingPackingListId = commercialInvoice.getBookingPackingList().getId();

            }
            if (dto instanceof CommercialInvoiceDTO) {
                clazz.getMethod("setCommercialInvoiceDetail", Set.class).invoke(dto, listDetailPI);
            } else if (dto instanceof CommercialInvoiceMainDTO) {
                clazz.getMethod("setFromSo", String.class).invoke(dto, fromSoStr);
                clazz.getMethod("setBookingPackingListId", Integer.class).invoke(dto, bookingPackingListId);
            }
            return dto;
        } catch (Exception ex) {
            throw new BusinessException(ex.getMessage());

        }
    }

}
