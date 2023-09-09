package com.yes4all.service.impl;

import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.Booking;
import com.yes4all.domain.CommercialInvoice;
import com.yes4all.domain.ProformaInvoice;
import com.yes4all.domain.Resource;
import com.yes4all.repository.BookingRepository;
import com.yes4all.repository.CommercialInvoiceRepository;
import com.yes4all.repository.ProformaInvoiceRepository;
import com.yes4all.repository.ResourceRepository;
import com.yes4all.service.ResourceService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.yes4all.common.constants.Constant.*;
import static com.yes4all.constants.GlobalConstant.*;

@Service
public class ResourceServiceImpl implements ResourceService {
    private final Logger logger = LoggerFactory.getLogger(ResourceServiceImpl.class);
    @Autowired
    private BookingRepository bookingRepository;
    @Value("${attribute.host.url}")
    private String hostUrl;
    @Autowired
    private CommercialInvoiceRepository commercialInvoiceRepository;
    @Autowired
    private ResourceRepository resourceRepository;
    @Autowired
    private ProformaInvoiceRepository proformaInvoiceRepository;

    @Override
    public Resource handleUploadFile(MultipartFile multipartFile, Integer id, String module, String fileType) throws IOException, URISyntaxException {
        String filePath = getFileResourcePath(fileType);
        filePath += module + "/" + id;
        Path source = Paths.get("");
        Path resourcePath = Paths.get(source.toAbsolutePath() + File.separator + filePath);
        if (!Files.exists(resourcePath)) {
            FileUtils.createParentDirectories(resourcePath.toFile());
            Files.createDirectory(resourcePath);
        }
        String originalFileName = Objects.requireNonNull(multipartFile.getOriginalFilename()).replace(" ", "_");
        String uploadPath = resourcePath + File.separator + originalFileName;
        File uploadFile = doUploadFile(multipartFile, uploadPath);
        if (!uploadFile.exists()) {
            throw new IOException("Fail to upload file");
        }
        return setMetadata(uploadFile, id, module, fileType, multipartFile.getSize());
    }

    @Override
    public Boolean deleteFileUploadTemp(Integer id, String module, String fileType) throws IOException, URISyntaxException {
        String filePath = getFileResourcePath(fileType);
        filePath += module + "/" + id + "_temp";
        Path source = Paths.get("");
        Path resourcePath = Paths.get(source.toAbsolutePath() + File.separator + filePath);
        FileUtils.cleanDirectory(resourcePath.toFile());
        return Files.deleteIfExists(resourcePath);
    }

    public void moveFileUploadToFileTemp(Integer id, String module, String fileType) throws IOException, URISyntaxException {
        String filePath = getFileResourcePath(fileType);
        String filePathTemp = getFileResourcePath(fileType);
        filePath += module + "/" + id;
        filePathTemp += module + "/" + id + "_temp";
        Path source = Paths.get("");
        Path resourcePath = Paths.get(source.toAbsolutePath() + File.separator + filePath);
        Path resourcePathTemp = Paths.get(source.toAbsolutePath() + File.separator + filePathTemp);
        if (!Files.exists(resourcePathTemp)) {
            Files.move(resourcePath, resourcePathTemp);
        }
    }

    public void moveFileTempToFileUpload(Integer id, String module, String fileType) throws IOException, URISyntaxException {
        String filePath = getFileResourcePath(fileType);
        String filePathTemp = getFileResourcePath(fileType);
        filePath += module + "/" + id;
        filePathTemp += module + "/" + id + "_temp";
        Path source = Paths.get("");
        Path resourcePath = Paths.get(source.toAbsolutePath() + File.separator + filePath);
        Path resourcePathTemp = Paths.get(source.toAbsolutePath() + File.separator + filePathTemp);
        if (Files.exists(resourcePath)) {
            FileUtils.cleanDirectory(resourcePath.toFile());
            Files.delete(resourcePath);
        }
        if (!Files.exists(resourcePath)) {
            Files.move(resourcePathTemp, resourcePath);
        }
    }


    private File doUploadFile(MultipartFile multipartFile, String filePath) {
        File uploadedFile = new File(filePath);
        try {
            multipartFile.transferTo(uploadedFile);
            logger.info("Moved file {} to path {}", uploadedFile.getName(), filePath);
        } catch (IOException e) {
            logger.error(String.format("Fail to upload file %s to inbound folder", uploadedFile.getName()));
        }
        return uploadedFile;
    }

    private Resource setMetadata(File uploadedFile, Integer id, String module, String fileType, long fileSize) {
        Resource resource = new Resource();
        String fileName = uploadedFile.getName();
        resource.setName(fileName);
        String resourcePath = "";
        if (FILE_UPLOAD.equals(fileType)) {
            resourcePath = hostUrl + RESOURCE_FILE_UPLOAD_URL + module + "/";
        }
        resource.setPath(resourcePath + id + "/" + fileName);
        resource.setType(FilenameUtils.getExtension(fileName));
        resource.setModule(module);
        resource.setFileSize(fileSize);
        resource.setFileType(fileType);
        if (module.equals(MODULE_PROFORMA_INVOICE)) {
            resource.setProformaInvoiceId(id);
        } else if (module.equals(MODULE_COMMERCIAL_INVOICE)) {
            resource.setCommercialInvoiceId(id);
        } else if (module.equals(MODULE_BOOKING)) {
            resource.setBookingId(id);
            Optional<Booking> oBooking = bookingRepository.findById(id);
            if (oBooking.isPresent()) {
                Booking booking = oBooking.get();
                booking.setStatus(STATUS_BOOKING_UPLOAD_FCR);
                bookingRepository.saveAndFlush(booking);
            }
        }
        resource = resourceRepository.saveAndFlush(resource);

        return resource;
    }

    private static String getFileResourcePath(String fileType) {
        switch (fileType) {
            case FILE_UPLOAD:
                return FILE_UPLOAD_FOLDER_PATH;
            default:
                return "";
        }
    }

    public Boolean deleteFileUpload(Integer id) throws IOException {
        Optional<Resource> oResource = resourceRepository.findById(id);
        Integer functionId = 0;
        String fileType = "";
        String fileName = "";
        String module = "";
        if (!oResource.isPresent()) {
            return false;
        } else {
            Resource resource = oResource.get();
            fileType = resource.getFileType();
            fileName = resource.getName();
            module = resource.getModule();
            if (resource.getCommercialInvoiceId() != null) {
                functionId = resource.getCommercialInvoiceId();
            } else if (resource.getProformaInvoiceId() != null) {
                functionId = resource.getProformaInvoiceId();
            } else if (resource.getBookingId() != null) {
                functionId = resource.getBookingId();
            }
        }
        String filePath = getFileResourcePath(fileType);
        filePath += module + "/" + functionId + "/" + fileName;
        Path source = Paths.get("");
        Path resourcePath = Paths.get(source.toAbsolutePath() + File.separator + filePath);
        resourceRepository.deleteById(id);
        return Files.deleteIfExists(resourcePath);
    }

}
