package com.yes4all.service.impl;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtils;
import com.yes4all.constants.GlobalConstant;
import com.yes4all.domain.Resource;
import com.yes4all.domain.UserSync;
import com.yes4all.domain.model.UserSyncDTO;
import com.yes4all.repository.ResourceRepository;
import com.yes4all.repository.UserSyncRepository;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.Objects;


/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserSyncService {

    private final static Logger log = LoggerFactory.getLogger(UserSyncService.class);
    private final Log logger = LogFactory.getLog(getClass());
    @Value("${attribute.host.url}")
    private static String hostUrl;

    private static ResourceRepository resourceRepository;
    private static ResourceServiceImpl resourceService;
    private static UserSyncRepository userSyncRepository;

    public UserSyncService(UserSyncRepository userSyncRepository) {
        this.userSyncRepository = userSyncRepository;
    }

    public static void getAttributes(String jwtToken, String role) throws JsonProcessingException {
        jwtToken = jwtToken.substring(jwtToken.indexOf("[{"), jwtToken.indexOf("}]") + 2);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        List<UserSyncDTO> participantJsonList = mapper.readValue(jwtToken, new TypeReference<>() {
        });
        participantJsonList.stream().forEach(item -> {
            UserSync userSync = new UserSync();
            BeanUtils.copyProperties(item, userSync);
            userSync.setLogin(item.getUsername());
            userSync.setRole(role);
            if (userSync.getEmail() != null) {
                userSyncRepository.saveAndFlush(userSync);
            }
        });

    }



}
