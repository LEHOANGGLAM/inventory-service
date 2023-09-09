package com.yes4all.web.rest.controller;


import com.yes4all.common.constants.Constant;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.Resource;
import com.yes4all.repository.ResourceRepository;
import com.yes4all.service.impl.ResourceServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tech.jhipster.web.util.HeaderUtil;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hibernate.id.IdentifierGenerator.ENTITY_NAME;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final Logger log = LoggerFactory.getLogger(ResourceController.class);

    @Autowired
    private ResourceRepository photoRepository;
    @Autowired
    private ResourceServiceImpl resourceService;


    public ResourceController() {
    }


    @GetMapping(value = "/{module}/{id}/{fileName}")
    public void getProformaInvoiceFile(HttpServletRequest request, HttpServletResponse response,@PathVariable String module, @PathVariable Integer id, @PathVariable String fileName) {
        log.debug("REST request to get Technical Files : {}", fileName);
        try {
            String filePath = GlobalConstant.FILE_UPLOAD_FOLDER_PATH + module+ "/" + id + File.separator + fileName;
            File resource = new File(filePath);
            InputStream targetStream = new FileInputStream(resource);
            String contentType = request.getServletContext().getMimeType(resource.getAbsolutePath());
            if (CommonDataUtil.isEmpty(contentType)) {
                contentType = "application/octet-stream";
            }
            log.info("Content-type = {}", contentType);
            response.setContentType(contentType);
            StreamUtils.copy(targetStream, response.getOutputStream());
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<Object>> uploadResources(@RequestParam("file") @Nullable List<MultipartFile> files,@RequestParam("id") Integer id, @RequestParam("module") String module) {
        log.debug("REST request to save Photo");
        List<Object> result = new ArrayList<>();
        try {
            String message = "";
            if (CommonDataUtil.isNotNull(files) && files.get(0).getSize() > 0) {
                for (MultipartFile file : files) {
                    Resource resource = resourceService.handleUploadFile(file, id, module, GlobalConstant.FILE_UPLOAD);
                    result.add(resource);
                }
                message = "Uploaded files success";
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert(ENTITY_NAME, message, "")).body(result);
        } catch (Exception ipe) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ipe.getMessage());
        }
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<Integer, Boolean>> removeResource(@PathVariable Integer id) {
        try {
            Map<Integer, Boolean> result = new HashMap<>();
            boolean isResult = resourceService.deleteFileUpload(id);
            result.put(id, isResult);
            String message = "files success";
            return ResponseEntity.ok().headers(HeaderUtil.createAlert(ENTITY_NAME, message, "")).body(result);
        } catch (Exception ipe) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ipe.getMessage());
        }

    }

}
