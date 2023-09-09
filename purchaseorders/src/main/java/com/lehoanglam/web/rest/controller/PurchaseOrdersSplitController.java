package com.yes4all.web.rest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtils;
import com.yes4all.common.utils.ExcelHelper;
import com.yes4all.common.utils.UploadPurchaseOrderSplitStatus;
import com.yes4all.domain.PurchaseOrdersSplit;
import com.yes4all.domain.PurchaseOrdersSplitData;
import com.yes4all.domain.PurchaseOrdersSplitResult;
import com.yes4all.domain.model.*;
import com.yes4all.service.PurchaseOrdersService;
import com.yes4all.service.PurchaseOrdersSplitService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PurchaseOrdersSplitController {
    private final Logger log = LoggerFactory.getLogger(PurchaseOrdersSplitController.class);

    @Autowired
    private UploadExcelService uploadExcelService;
    @Autowired
    private PurchaseOrdersSplitService service;
    @Value("${attribute.template.path}")
    private String TemplatePath;
    private static final String NAME_TEMPLATE = "/PurchaseOrder_Template.xlsx";
    @GetMapping("/purchase-order-split")
    public ResponseEntity<RestResponse<Object>> findAll(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer limit) throws JsonProcessingException {
        Page<PurchaseOrdersMainSplitDTO> response = service.getAll(page, limit);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(null).build());
    }

    @PostMapping(value = "/purchase-order-split")
    public ResponseEntity<RestResponse<Object>> splitPO(@RequestParam Integer id) {
        PurchaseOrdersSplit result = service.splitPurchaseOrder(id);
        return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
    }

    @GetMapping("/purchase-order-split/data/{id}")
    public ResponseEntity<RestResponse<Object>> findDataWithId(@PathVariable Integer id, @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer limit) throws JsonProcessingException {
        PurchaseOrderDataPageDTO response = service.getPurchaseOrdersSplitData(id, page, limit);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(null).build());
    }

    @GetMapping("/purchase-order-split/result/{id}")
    public ResponseEntity<RestResponse<Object>> findResultWithId(@PathVariable Integer id, @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer limit) throws JsonProcessingException {
        PurchaseOrderResultPageDTO response = service.getPurchaseOrdersSplitResult(id, page, limit);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(null).build());
    }

    @GetMapping("/purchase-order-split/result/details/{id}")
    public ResponseEntity<RestResponse<Object>> findResultDetailsWithId(@PathVariable Integer id, @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer limit) throws JsonProcessingException {
        PurchaseOrderSplitResultDetailsDTO response = service.getPurchaseOrdersSplitResultDetail(id, page, limit);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(null).build());
    }

    @PostMapping("/purchase-order-split/delete")
    public ResponseEntity<List<Integer>> removePurchaseOrders(@RequestBody() @Validated ListIdDTO request) throws JsonProcessingException {
        boolean isRemoved = service.removePurchaseOrdersSplit(request.getId(), request.getUserName());
        return isRemoved ? ResponseEntity.ok(request.getId()) : ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/purchase-order-split/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestResponse<Object>> uploadResources(@RequestParam("file") List<MultipartFile> file, @RequestParam("user") String user) {
        ResultUploadDTO response = null;
        String message = "";
        List<ResultUploadDTO> resultUploadDTOs = new ArrayList<>();
        List<UploadPurchaseOrderSplitStatus> uploadPurchaseOrderSplitStatus = new ArrayList<>();
        for (MultipartFile element : file) {
            if (ExcelHelper.hasExcelFormat(element)) {
                try {
                    response = uploadExcelService.mappingToPOSplit(element);
                    long countRowErrors = response.getUploadPurchaseOrderSplitStatus().stream().filter(i -> i.getStatus().equals("errors")).count();
                    if (countRowErrors > 0) {
                        uploadPurchaseOrderSplitStatus.add(response.getUploadPurchaseOrderSplitStatus().get(0));
                    } else {
                        resultUploadDTOs.add(response);
                    }
                } catch (Exception e) {
                    message = "Could not upload the file: " + element.getOriginalFilename() + "!";
                    response.setStatus(message);
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(RestResponse.builder().body(response).build());
                }
            }
        }
        if(uploadPurchaseOrderSplitStatus.size()>0){
            message = "Upload the file failed!";
            response.setStatus(message);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestResponse.builder().body(uploadPurchaseOrderSplitStatus).build());
        }
        if (resultUploadDTOs.size() > 0) {
            List<PurchaseOrdersSplit> result = service.createPurchaseOrdersSplit(resultUploadDTOs, user);
            if (result.size() > 0) {
                message = "Uploaded all file successfully. ";
                response.setStatus(message);
                return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
            } else {
                message = "Could not save the file!";
                response.setStatus(message);
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(RestResponse.builder().body(response).build());
            }
        } else {
            message = "Please upload an excel file!";
            response.setStatus(message);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestResponse.builder().body(response).build());
        }
    }

    @GetMapping(value = "/purchase-order-split/template")
    public HttpEntity<ByteArrayResource> downloadCustomFieldTemplate() throws IOException {
        try {
            log.info("START download template");
            File resource = new ClassPathResource(TemplatePath + NAME_TEMPLATE).getFile();
            byte[] excelContent = Files.readAllBytes(resource.toPath());
            HttpHeaders header = new HttpHeaders();
            header.setContentType(new MediaType("application", "force-download"));
            header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=PurchaseOrderSplit_Template.xlsx");
            return new HttpEntity<>(new ByteArrayResource(excelContent), header);
        } catch (FileNotFoundException ex) {
            log.error("Cannot found template file.");
            return null;
        }
    }

    @GetMapping(value = "/purchase-order-split/export/{id}")
    public HttpEntity<ByteArrayResource> downloadResultExport(@PathVariable int id) throws IOException {
        try {
            log.info("START download product template");
            String nameTemplate=service.getNameFile(id)+".xlsx";
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
