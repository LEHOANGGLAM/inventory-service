package com.yes4all.service;

import com.yes4all.domain.CommercialInvoice;
import com.yes4all.domain.model.CommercialInvoiceDTO;
import com.yes4all.domain.model.CommercialInvoiceMainDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * Service Interface for managing {@link CommercialInvoice}.
 */
public interface CommercialInvoiceService {

    Page<CommercialInvoiceMainDTO> listingCommercialInvoiceWithCondition(Integer page, Integer limit, Map<String, String> filterParams);

    boolean removeCommercialInvoice(List<Integer> listId, String userName);

    CommercialInvoiceDTO getCommercialInvoiceDetail(Integer id);
    CommercialInvoiceDTO getCommercialInvoiceDetailWithInvoiceNo(String invoiceNo);
    CommercialInvoiceDTO createCommercialInvoice(CommercialInvoiceDTO CommercialInvoiceDTO) throws IOException, URISyntaxException;
    CommercialInvoiceDTO updateCommercialInvoice(Integer iD,CommercialInvoiceDTO CommercialInvoiceDTO) throws IOException, URISyntaxException;

    boolean confirmed(List<Integer> Id);
    boolean send(List<Integer> Id,String userName);


 }
