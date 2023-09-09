package com.yes4all.service;

import com.yes4all.domain.ProformaInvoice;
import com.yes4all.domain.model.ProformaInvoiceDTO;
import com.yes4all.domain.model.PurchaseOrderDetailPageDTO;
import com.yes4all.domain.model.ProformaInvoiceMainDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * Service Interface for managing {@link ProformaInvoice}.
 */
public interface ProformaInvoiceService {

    Page<ProformaInvoiceMainDTO> listingProformaInvoiceWithCondition(Integer page, Integer limit, Map<String, String> filterParams);

    boolean removeProformaInvoice(List<Integer> listId, String userName);

    ProformaInvoiceDTO getProformaInvoiceDetail(Integer id);


    ProformaInvoiceDTO createProformaInvoice(ProformaInvoiceDTO proformaInvoiceDTO) ;
    ProformaInvoiceDTO updateProformaInvoice(Integer iD,ProformaInvoiceDTO proformaInvoiceDTO) throws IOException, URISyntaxException;


    boolean confirmed(List<Integer> Id);
    boolean send(List<Integer> Id,String userName);

 }
