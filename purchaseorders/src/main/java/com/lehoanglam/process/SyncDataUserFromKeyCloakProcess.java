package com.yes4all.process;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yes4all.domain.Authority;
import com.yes4all.domain.model.*;
import com.yes4all.repository.AuthorityRepository;
import com.yes4all.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SyncDataUserFromKeyCloakProcess {
    private static final Logger log = LoggerFactory.getLogger(SyncDataUserFromKeyCloakProcess.class);

    @Autowired
    private UserRepository repository;
    @Autowired
    private  AuthorityRepository authorityRepository;


    @Value("${info-sync-data-user.username}")
    private String username;
    @Value("${info-sync-data-user.password}")
    private String password;
    @Value("${info-sync-data-user.client_secret}")
    private String clientSecret;
    @Value("${info-sync-data-user.client_id}")
    private String clientId;

    @Value("${info-sync-data-user.group_pims_id}")
    private String  groupPIMSId;

    private String URL_ENDPOINT = "https://keycloak.yes4all.com/auth/realms/oms-realm/protocol/openid-connect/token";
    private String URL_KEYCLOAK_GROUPS_ID = "https://keycloak.yes4all.com/auth/admin/realms/oms-realm/groups/";


  //  @Scheduled(cron = "* */20 * * * *")
    public void processSyncData() {
        try {
            log.info("SyncDataUserFromKeyCloakProcess[processSyncData]::Start::" + System.currentTimeMillis());
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("client_secret", clientSecret);
            map.add("client_id", clientId);
            map.add("username", username);
            map.add("password", password);
            map.add("grant_type", "password");
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            //mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<TokenKeyCloakDTO> response = restTemplate.postForEntity(URL_ENDPOINT, request, TokenKeyCloakDTO.class);
            TokenKeyCloakDTO TokenKeyCloakDTO = response.getBody();
            String access_token = TokenKeyCloakDTO.getAccess_token();
            HttpHeaders headersGroups = new HttpHeaders();
            headersGroups.add("Content-Type", "application/json");
            headersGroups.add("Authorization", "Bearer " + access_token);
            ;
            HttpEntity<String> entity = new HttpEntity<String>("parameters", headersGroups);
            ResponseEntity<String> responseGroups = restTemplate.exchange(URL_KEYCLOAK_GROUPS_ID, HttpMethod.GET, entity, String.class);
            KeyCloakGroupDTO[] keyCloakGroupDTO = mapper.readValue(responseGroups.getBody(), new TypeReference<>() {
            });
            List<KeyCloakGroupDTO> keyCloakGroupDTOList = Arrays.asList(keyCloakGroupDTO);
            String groupIdPIMS= keyCloakGroupDTOList.stream().filter(i->i.getName().equals("PIMS")).map(k->k.getId()).collect(Collectors.joining());
            ResponseEntity<String> responseGroupsPIMS = restTemplate.exchange(URL_KEYCLOAK_GROUPS_ID+"/"+groupIdPIMS, HttpMethod.GET, entity, String.class);
            KeyCloakGroupDTO keyCloakGroupDTOPIMS = mapper.readValue(responseGroupsPIMS.getBody(), new TypeReference<>() {
            });
            List<KeyCloakSubGroupDTO> keyCloakSubGroupDTOListPIMS = Arrays.asList(keyCloakGroupDTOPIMS.getSubGroups());

            keyCloakSubGroupDTOListPIMS.stream().forEach(item -> {
                String role = item.getName();
                UserSyncDTO[] userSyncDTOs;
                ResponseEntity<String> responseSubGroups = restTemplate.exchange(URL_KEYCLOAK_GROUPS_ID + item.getId() + "/members", HttpMethod.GET, entity, String.class);
                try {
                    userSyncDTOs = mapper.readValue(responseSubGroups.getBody(), UserSyncDTO[].class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                List<UserSyncDTO> userSyncDTOList = Arrays.asList(userSyncDTOs);
                userSyncDTOList.stream().forEach(element -> {
                    User user = new User();
                    BeanUtils.copyProperties(element, user);
                    user.setLogin(element.getUsername());
                    user.setId(element.getEmail());
                    user.setSupplier(false);
                    if(element.getAttributes()!=null){
                        if(element.getAttributes().getVendor()!=null) {
                            user.setVendor(element.getAttributes().getVendor()[0]);
                        }
                        if(element.getAttributes().getSupplier()!=null) {
                            user.setSupplier(Boolean.valueOf(element.getAttributes().getSupplier()[0]));
                        }
                        if(element.getAttributes().getCompany()!=null) {
                            user.setCompany(element.getAttributes().getCompany()[0]);
                        }
                        if(element.getAttributes().getAddress()!=null) {
                            user.setAddress(element.getAttributes().getAddress()[0]);
                        }
                        if(element.getAttributes().getTelephone()!=null) {
                            user.setTelephone(element.getAttributes().getTelephone()[0]);
                        }
                        if(element.getAttributes().getFax()!=null) {
                            user.setFax(element.getAttributes().getFax()[0]);
                        }
                    }
                    Set<Authority> authorities = new HashSet<Authority>();
                    Authority authority = new Authority();
                    authority.setName(role);
                    authorities.add(authority);
                    user.setAuthorities(authorities);
                    user.setActivated(true);
                    if (user.getEmail() != null) {
                        Collection<String> dbAuthorities = getAuthorities();
                        Collection<String> userAuthorities = user.getAuthorities().stream().map(Authority::getName).collect(Collectors.toList());
                        for (String authorityElement : userAuthorities) {
                            if (!dbAuthorities.contains(authorityElement)) {
                                log.debug("Saving authority '{}' in local database", authority);
                                Authority authorityToSave = new Authority();
                                authorityToSave.setName(authorityElement);
                                authorityRepository.save(authorityToSave);
                            }
                        }
                        repository.saveAndFlush(user);
                    }
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Fail to sync inventory log with message: {}", e.getMessage());
        } finally {
            log.info("InventoryLogProcess[processUpdateInventory]::End::" + System.currentTimeMillis());
        }
    }
    public List<String> getAuthorities() {
        return authorityRepository.findAll().stream().map(Authority::getName).collect(Collectors.toList());
    }
}
