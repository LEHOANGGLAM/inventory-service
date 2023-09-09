package com.yes4all.web.rest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtils;
import com.yes4all.common.utils.ExcelHelper;
import com.yes4all.domain.PurchaseOrdersDetail;
import com.yes4all.domain.model.*;
import com.yes4all.service.PurchaseOrdersService;


import com.yes4all.service.impl.UploadExcelService;
import com.yes4all.web.rest.payload.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class PurchaseOrdersController {
    private final Logger log = LoggerFactory.getLogger(PurchaseOrdersController.class);

    @Autowired
    private UploadExcelService uploadExcelService;
    @Autowired
    private PurchaseOrdersService service;
    @Value("${attribute.template.path}")
    private String TemplatePath;
    private static final String NAME_TEMPLATE = "/PurchaseOrder_Template.xlsx";


    @PostMapping("/purchase-order/{purchaseOrderId}")
    public ResponseEntity<RestResponse<Object>> findDetailsWithFilter(@PathVariable Integer purchaseOrderId, @RequestBody(required = false) @Validated ListingDTO ListingDTO) throws JsonProcessingException {
        ListingDTO.setSearchBy(CommonDataUtil.isNotEmpty(ListingDTO.getSearchBy()) ? ListingDTO.getSearchBy() : "");
        ListingDTO.setSearchByValue(CommonDataUtil.isNotEmpty(ListingDTO.getSearchByValue()) ? ListingDTO.getSearchByValue() : "");
        String searchBy = ListingDTO.getSearchBy();
        String searchByValue = ListingDTO.getSearchByValue().toUpperCase();
        Integer page = ListingDTO.getPage();
        Integer size = ListingDTO.getSize();
        searchBy = CommonDataUtil.isNotEmpty(searchBy) ? searchBy : "";
        searchByValue = CommonDataUtil.isNotEmpty(searchByValue) ? searchByValue : "";
        String sku = searchBy.equals("sku") ? searchByValue : "";
        String aSin = searchBy.equals("aSin") ? searchByValue : "";
        String productName = searchBy.equals("productName") ? searchByValue : "";
        PurchaseOrderDetailPageDTO response = service.getPurchaseOrdersDetailWithFilter(purchaseOrderId, sku, aSin, productName, page, size);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.notFound().build();
    }
    @GetMapping("/purchase-order/{orderNo}")
    public ResponseEntity<RestResponse<Object>> findDetailsWithFilter(@PathVariable String orderNo,@RequestParam(required = false) Integer page,@RequestParam(required = false)Integer limit) throws JsonProcessingException {
        PurchaseOrderDetailPageDTO response = service.getPurchaseOrdersDetailWithFilterOrderNo(orderNo, page, limit);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(null).build());
    }
    @PostMapping("/purchase-order/edit/{purchaseOrderId}")
    public ResponseEntity<RestResponse<Object>> editPurchaseOrder(@PathVariable Integer purchaseOrderId, @RequestBody(required = false) @Validated PurchaseOrdersMainDTO request,@RequestParam("userName")  String userName)   {
        if (CommonDataUtil.isNotNull(request)) {
            PurchaseOrdersMainDTO result = service.editPurchaseOrder(request,purchaseOrderId,userName);
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/purchase-order/edit-sku/{purchaseOrderId}")
    public ResponseEntity<RestResponse<Object>> editSkuPurchaseOrder(@PathVariable Integer purchaseOrderId, @RequestBody(required = false) @Validated Set<PurchaseOrderDetailDTO> request)   {
        if (CommonDataUtil.isNotNull(request)) {
            PurchaseOrderDetailPageDTO result = service.editSkuPurchaseOrder(request,purchaseOrderId);
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/purchase-order/listing")
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
        filterParams.put("poNumber", searchBy.equals("poNumber") ? searchByValue : "");
        filterParams.put("vendor", searchBy.equals("vendor") ? searchByValue : "");
        filterParams.put("country", searchBy.equals("country") ? searchByValue : "");
        filterParams.put("status", searchBy.equals("status") ? searchByValue : "-1");
        filterParams.put("fulfillmentCenter", searchBy.equals("fulfillmentCenter") ? searchByValue : "");
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
        Page<PurchaseOrdersMainDTO> response = service.listingPurchaseOrdersWithCondition(page, size, filterParams);
        if (!CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }

    @PostMapping("/purchase-order/detail/delete/{purchaseOrderId}")
    public ResponseEntity<List<Integer>> removeSkuFromDetails(@PathVariable Integer purchaseOrderId, @RequestBody() @Validated ListIdDTO request) {
        boolean isRemoved = service.removeSkuFromDetails(purchaseOrderId, request.getId(), request.getUserName());
        return isRemoved ? ResponseEntity.ok(request.getId()) : ResponseEntity.notFound().build();

    }

    @PostMapping("/purchase-order/delete")
    public ResponseEntity<List<Integer>> removePurchaseOrders(@RequestBody() @Validated ListIdDTO request) throws JsonProcessingException {
        boolean isRemoved = service.removePurchaseOrders(request.getId(), request.getUserName());
        return isRemoved ? ResponseEntity.ok(request.getId()) : ResponseEntity.notFound().build();
    }
    @PostMapping("/purchase-order/get-list-sku")
    public ResponseEntity<RestResponse<Object>> getListSkuFromPO(@RequestBody() @Validated ListIdDTO request) throws JsonProcessingException {
        ListDetailPODTO result = service.getListSkuFromPO(request.getId());
        if (!CommonDataUtil.isNotNull(result)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }

    @PostMapping("/purchase-order/confirmed")
    public ResponseEntity<RestResponse<Object>> confirm(@RequestBody() @Validated ListIdDTO request) {
        boolean isConfirmed = service.confirmedPurchaseOrders(request.getId(),request.getUserName());
        return ResponseEntity.ok().body(RestResponse.builder().body(isConfirmed).build());
    }
    @PostMapping("/purchase-order/send")
    public ResponseEntity<RestResponse<Object>> send(@RequestBody() @Validated ListIdDTO request) {
        boolean isConfirmed = service.sendPurchaseOrders(request.getId(),request.getUserName());
        return ResponseEntity.ok().body(RestResponse.builder().body(isConfirmed).build());
    }
    @PostMapping("/purchase-order/cancel")
    public ResponseEntity<RestResponse<Object>> cancel(@RequestBody() @Validated ListIdDTO request) {
        boolean isConfirmed = service.cancelPurchaseOrders(request.getId(),request.getUserName());
        return ResponseEntity.ok().body(RestResponse.builder().body(isConfirmed).build());
    }

    @PostMapping(value = "/purchase-order/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestResponse<Object>> uploadResources(@RequestParam("file") MultipartFile file,@RequestParam("userName") String userName) {
        ResultUploadDTO response = null;
        String message = "";
        if (ExcelHelper.hasExcelFormat(file)) {
            try {
                response = uploadExcelService.mappingToPO(file,userName);
                message = "Uploaded the file successfully: " + file.getOriginalFilename();
                response.setStatus(message);
                return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
            } catch (Exception e) {
                message = "Could not upload the file: " + file.getOriginalFilename() + "!";
                response.setStatus(message);
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(RestResponse.builder().body(response).build());
            }
        }
        message = "Please upload an excel file!";
        response.setStatus(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestResponse.builder().body(response).build());

    }
    @PostMapping(value = "/purchase-order/detail/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestResponse<Object>> uploadResourcesDetail(@RequestParam("file") MultipartFile file,@RequestParam("id") Integer id) {
        ResultUploadDTO response = null;
        String message = "";
        if (ExcelHelper.hasExcelFormat(file)) {
            try {
                response = uploadExcelService.mappingToDetailPO(file,id);
                message = "Uploaded the file successfully: " + file.getOriginalFilename();
                response.setStatus(message);
                return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
            } catch (Exception e) {
                message = "Could not upload the file: " + file.getOriginalFilename() + "!";
                response.setStatus(message);
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(RestResponse.builder().body(response).build());
            }
        }
        message = "Please upload an excel file!";
        response.setStatus(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestResponse.builder().body(response).build());

    }
    @GetMapping(value = "/purchase-order/template")
    public HttpEntity<ByteArrayResource> downloadCustomFieldTemplate() throws IOException {
        try {
            log.info("START download template");
            File resource = new ClassPathResource(TemplatePath + NAME_TEMPLATE).getFile();
            byte[] excelContent = Files.readAllBytes(resource.toPath());
            HttpHeaders header = new HttpHeaders();
            header.setContentType(new MediaType("application", "force-download"));
            header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=PurchaseOrder_Template.xlsx");
            return new HttpEntity<>(new ByteArrayResource(excelContent), header);
        } catch (FileNotFoundException ex) {
            log.error("Cannot found template file.");
            return null;
        }
    }
    @GetMapping(value = "/purchase-order/export/{id}")
    public HttpEntity<ByteArrayResource> downloadResultExport(@PathVariable int id) throws IOException {
        try {
            log.info("START download product template");
            String nameTemplate="PurchaseOrder.xlsx";
            File file = new File(nameTemplate);
            String fileName = file.getPath();
            service.export(fileName,id);
            byte[] excelContent = Files.readAllBytes(file.toPath());
            HttpHeaders header = new HttpHeaders();
            header.setContentType(new MediaType("application", "force-download"));
            header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nameTemplate + "");
            return new HttpEntity<>(new ByteArrayResource(excelContent), header);
        } catch (FileNotFoundException ex) {
            log.error("Cannot found template file.");
            return null;
        }
    }
}
