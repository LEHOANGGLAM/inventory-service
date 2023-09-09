package com.yes4all.web.rest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtils;
import com.yes4all.domain.model.CommercialInvoiceDTO;
import com.yes4all.domain.model.CommercialInvoiceMainDTO;
import com.yes4all.domain.model.ListIdDTO;
import com.yes4all.domain.model.ListingDTO;
import com.yes4all.service.CommercialInvoiceService;
import com.yes4all.web.rest.payload.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommercialInvoiceController {
    private final Logger log = LoggerFactory.getLogger(CommercialInvoiceController.class);


    @Autowired
    private CommercialInvoiceService service;


    @GetMapping("/commercial-invoice/{Id}")
    public ResponseEntity<RestResponse<Object>> findOne(@PathVariable Integer Id) {
        CommercialInvoiceDTO response = service.getCommercialInvoiceDetail(Id);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/commercial-invoice/detail/{invoiceNo}")
    public ResponseEntity<RestResponse<Object>> findWithInvoiceNo(@PathVariable String invoiceNo) {
        CommercialInvoiceDTO response = service.getCommercialInvoiceDetailWithInvoiceNo(invoiceNo);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.notFound().build();
    }
    @PostMapping(value = "/commercial-invoice")
    public ResponseEntity<RestResponse<Object>> submitCommercialInvoice(@RequestBody @Validated CommercialInvoiceDTO request) throws IOException, URISyntaxException {
        if (CommonDataUtil.isNotNull(request)) {
            CommercialInvoiceDTO result = service.createCommercialInvoice(request);
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping(value = "/commercial-invoice/{commercialInvoiceId}")
    public ResponseEntity<RestResponse<Object>> submitCommercialInvoice(@PathVariable Integer commercialInvoiceId, @RequestBody @Validated CommercialInvoiceDTO request) throws IOException, URISyntaxException {
        if (CommonDataUtil.isNotNull(request)) {
            CommercialInvoiceDTO result = service.updateCommercialInvoice(commercialInvoiceId, request);
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/commercial-invoice/listing")
    public ResponseEntity<RestResponse<Object>> findAll(@RequestBody(required = false) @Validated ListingDTO ListingDTO) throws JsonProcessingException {
        Map<String, String> filterParams = new HashMap<>();
        ListingDTO.setSearchBy(CommonDataUtil.isNotEmpty(ListingDTO.getSearchBy()) ? ListingDTO.getSearchBy() : "");
        ListingDTO.setSearchByValue(CommonDataUtil.isNotEmpty(ListingDTO.getSearchByValue()) ? ListingDTO.getSearchByValue() : "");
        String searchBy = ListingDTO.getSearchBy();
        String searchByValue = ListingDTO.getSearchByValue().toUpperCase();
        String fromValue = (String) ListingDTO.getFromValue();
        String toValue = (String) ListingDTO.getToValue();
        Integer page = ListingDTO.getPage();
        Integer size = ListingDTO.getSize();
        String supplier=ListingDTO.getSupplier();
        filterParams.put("supplier",supplier);
        searchBy = CommonDataUtil.isNotEmpty(searchBy) ? searchBy : "";
        searchByValue = CommonDataUtil.isNotEmpty(searchByValue) ? searchByValue : "";
        filterParams.put("invoiceNo", searchBy.equals("invoiceNo") ? searchByValue : "");
        filterParams.put("fromSO", searchBy.equals("fromSO") ? searchByValue : "");
        filterParams.put("term", searchBy.equals("term") ? searchByValue : "");
        filterParams.put("shipDateFrom", searchBy.equals("shipDate") ? fromValue : "");
        filterParams.put("shipDateTo", searchBy.equals("shipDate") ? toValue : "");
        filterParams.put("amountFrom", searchBy.equals("amount") ? fromValue : "0");
        filterParams.put("amountTo", searchBy.equals("amount") ? toValue : "0");
        filterParams.put("status", searchBy.equals("status") ? searchByValue : "0");
        if (searchBy.equals("updateDate")) {
            if (searchByValue.equals("CUSTOM")) {
                filterParams.put("updateDateFrom", DateUtils.convertStringLocalDate_Search(fromValue));
                filterParams.put("updateDateTo", DateUtils.convertStringLocalDate_Search(toValue));
            } else {
                filterParams.put("updateDateFrom", fromValue);
                filterParams.put("updateDateTo", toValue);
            }
        } else {
            filterParams.put("updateDateFrom", "");
            filterParams.put("updateDateTo", "");
        }

        Page<CommercialInvoiceMainDTO> response = service.listingCommercialInvoiceWithCondition(page, size, filterParams);
        if (!CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }


    @PostMapping("/commercial-invoice/delete")
    public ResponseEntity<List<Integer>> removePurchaseOrders(@RequestBody() @Validated ListIdDTO request) {
        boolean isRemoved = service.removeCommercialInvoice(request.getId(), request.getUserName());
        return isRemoved ? ResponseEntity.ok(request.getId()) : ResponseEntity.notFound().build();
    }

    @PostMapping("/commercial-invoice/confirmed")
    public ResponseEntity<RestResponse<Object>> confirm(@RequestBody() @Validated ListIdDTO request) {
        boolean isConfirmed = service.confirmed(request.getId());
        return ResponseEntity.ok().body(RestResponse.builder().body(isConfirmed).build());
    }
    @PostMapping("/commercial-invoice/send")
    public ResponseEntity<RestResponse<Object>> send(@RequestBody() @Validated ListIdDTO request) {
        boolean isConfirmed = service.send(request.getId(),request.getUserName());
        return ResponseEntity.ok().body(RestResponse.builder().body(isConfirmed).build());
    }


}
