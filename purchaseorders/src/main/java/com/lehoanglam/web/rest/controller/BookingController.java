package com.yes4all.web.rest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yes4all.common.errors.ResponseMessage;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.ExcelHelper;
import com.yes4all.domain.model.*;
import com.yes4all.service.BookingService;
import com.yes4all.service.impl.UploadExcelService;
import com.yes4all.web.rest.payload.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
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
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BookingController {
    private final Logger log = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private UploadExcelService uploadExcelService;
    @Autowired
    private BookingService service;
    @Value("${attribute.template.path}")
    private String TemplatePath;
    private static final String NAME_TEMPLATE = "/PurchaseOrder_Template.xlsx";


    @PostMapping(value = "/booking/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("userId") String userId) {
        String message = "";


        try {
            service.save(file, userId);
            message = "Uploaded the file successfully: " + file.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + "!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }


        //return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage(message));
    }

    @PostMapping(value = "/booking")
    public ResponseEntity<RestResponse<Object>> submitBooking(@RequestBody @Validated BookingDTO request) {
        if (CommonDataUtil.isNotNull(request)) {
            BookingDTO result = service.createBooking(request);
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/booking/completed")
    public ResponseEntity<RestResponse<Object>> completedBooking(@RequestParam("id") Integer id) {

        BookingDTO result = service.completedBooking(id);
        if (result != null) {
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/booking/submit-packing-list")
    public ResponseEntity<RestResponse<Object>> submitPackingList(@RequestBody BookingPackingListDTO request) {
        BookingPackingListDTO result = service.submitPackingList(request);
        if (CommonDataUtil.isNotNull(result)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/booking/confirm-packing-list")
    public ResponseEntity<RestResponse<Object>> confirmPackingList(@RequestBody BookingPackingListDTO request) {
        Integer result = service.confirmPackingList(request);
        if (CommonDataUtil.isNotNull(result)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(result).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/booking/detail")
    public ResponseEntity<RestResponse<Object>> findOne(@RequestBody BookingPageGetDetailDTO request) {
        BookingDetailsDTO response = service.getBookingDetailsDTO(request);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/booking/update")
    public ResponseEntity<RestResponse<Object>> updateBooking(@RequestBody BookingPackingListDTO request) {
        Integer response = service.updateBooking(request);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/booking/packing-list/detail")
    public ResponseEntity<RestResponse<Object>> findOnePackingList(@RequestParam("id") Integer id) {
        BookingPackingListDTO response = service.getPackingListDetailsDTO(id);
        if (CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/booking/listing")
    public ResponseEntity<RestResponse<Object>> findAll(@RequestBody(required = false) @Validated ListingDTO ListingDTO) throws JsonProcessingException {
        Map<String, String> filterParams = new HashMap<>();
        ListingDTO.setSearchBy(CommonDataUtil.isNotEmpty(ListingDTO.getSearchBy()) ? ListingDTO.getSearchBy() : "");
        ListingDTO.setSearchByValue(CommonDataUtil.isNotEmpty(ListingDTO.getSearchByValue()) ? ListingDTO.getSearchByValue() : "");
        String searchBy = ListingDTO.getSearchBy();
        String searchByValue = ListingDTO.getSearchByValue().toUpperCase();
        Integer page = ListingDTO.getPage();
        Integer size = ListingDTO.getSize();
        String supplier=ListingDTO.getSupplier();
        filterParams.put("supplier",supplier);
        filterParams.put("booking", searchBy.equals("booking") ? searchByValue : "");
        filterParams.put("poAmazon", searchBy.equals("poAmazon") ? searchByValue : "");
        filterParams.put("masterPO", searchBy.equals("masterPO") ? searchByValue : "");

        Page<BookingMainDTO> response = service.listingBookingWithCondition(page, size, filterParams);
        if (!CommonDataUtil.isNotNull(response)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(RestResponse.builder().body(response).build());
    }

    @GetMapping(value = "/booking/packing-list/export/{id}")
    public HttpEntity<ByteArrayResource> downloadResultExport(@PathVariable int id) throws IOException {
        try {
            log.info("START download product template");
            String nameTemplate = "PackingList.xlsx";
            File file = new File(nameTemplate);
            String fileName = file.getPath();
            service.export(fileName, id);
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

    @PostMapping(value = "/booking/packing-list/detail/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestResponse<Object>> uploadResourcesDetail(@RequestParam("file") MultipartFile file, @RequestParam("id") Integer id) {
        ResultUploadDTO response = null;
        String message = "";
        if (ExcelHelper.hasExcelFormat(file)) {
            try {
                response = uploadExcelService.mappingToDetailPackingList(file, id);
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
}
