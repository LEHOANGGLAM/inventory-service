package com.yes4all.service.impl;


import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.config.Constants;
import com.yes4all.domain.Authority;
import com.yes4all.domain.model.AdminUserDTO;
import com.yes4all.domain.model.User;
import com.yes4all.domain.model.UserDTO;
import com.yes4all.repository.AuthorityRepository;
import com.yes4all.repository.UserRepository;
import com.yes4all.security.SecurityUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private final static Logger log = LoggerFactory.getLogger(UserService.class);
    private final Log logger = LogFactory.getLog(getClass());

    private final UserRepository userRepository;

    private final AuthorityRepository authorityRepository;


    public UserService(UserRepository userRepository, AuthorityRepository authorityRepository) {
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user.
     * @param lastName  last name of user.
     * @param email     email id of user.
     * @param langKey   language key.
     * @param imageUrl  image URL of user.
     */
    public void updateUser(String firstName, String lastName, String email, String langKey, String imageUrl) {
        SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                if (email != null) {
                    user.setEmail(email.toLowerCase());
                }
                user.setLangKey(langKey);
                user.setImageUrl(imageUrl);
                log.debug("Changed Information for User: {}", user);
            });
    }


    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(AdminUserDTO::new);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllPublicUsers(Pageable pageable) {
        return userRepository.findAllByIdNotNullAndActivatedIsTrue(pageable).map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUserWithRole(String role) {
        return userRepository.findAllWithRole(role).stream().map(UserDTO::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneWithAuthoritiesByLogin(login);
    }
    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByEmail(String email) {
        return userRepository.findOneWithAuthoritiesByEmail(email);
    }
    /**
     * Gets a list of all the authorities.
     *
     * @return a list of all the authorities.
     */
    @Transactional(readOnly = true)
    public List<String> getAuthorities() {
        return authorityRepository.findAll().stream().map(Authority::getName).collect(Collectors.toList());
    }

    private User syncUserWithIdP(Map<String, Object> details, User user) {
        // save authorities in to sync user roles/groups between IdP and JHipster's local database
        Collection<String> dbAuthorities = getAuthorities();
        Collection<String> userAuthorities = user.getAuthorities().stream().map(Authority::getName).collect(Collectors.toList());
        for (String authority : userAuthorities) {
            if (!dbAuthorities.contains(authority)) {
                log.debug("Saving authority '{}' in local database", authority);
                Authority authorityToSave = new Authority();
                authorityToSave.setName(authority);
                authorityRepository.save(authorityToSave);
            }
        }
        // save account in to sync users between IdP and JHipster's local database
        Optional<User> existingUser = userRepository.findOneByLogin(user.getLogin());
        if (existingUser.isPresent()) {
            // if IdP sends last updated information, use it to determine if an update should happen
            if (details.get("updated_at") != null) {
                Instant dbModifiedDate = Instant.from(existingUser.get().getLastModifiedDate());
                Instant idpModifiedDate;
                if (details.get("updated_at") instanceof Instant) {
                    idpModifiedDate = (Instant) details.get("updated_at");
                } else {
                    idpModifiedDate = Instant.ofEpochSecond((Integer) details.get("updated_at"));
                }
                if (idpModifiedDate.isAfter(dbModifiedDate)) {
                    log.debug("Updating user '{}' in local database", user.getLogin());
                    updateUser(user.getFirstName(), user.getLastName(), user.getEmail(), user.getLangKey(), user.getImageUrl());
                }
                // no last updated info, blindly update
            } else {
                log.debug("Updating user '{}' in local database", user.getLogin());
                updateUser(user.getFirstName(), user.getLastName(), user.getEmail(), user.getLangKey(), user.getImageUrl());
            }
        } else {
            log.debug("Saving user '{}' in local database", user.getLogin());
            userRepository.save(user);
        }
        return user;
    }

    /**
     * Returns the user from an OAuth 2.0 login or resource server with JWT.
     * Synchronizes the user in the local repository.
     *
     * @param jwtToken the authentication token.
     * @return the user from the authentication.
     */
    @Transactional
    public AdminUserDTO getUserFromAuthentication(String jwtToken) {
        Map<String, Object> attributes;
        attributes = CommonDataUtil.getAttributes(jwtToken);
        User user = getUser(attributes);
        return new AdminUserDTO(syncUserWithIdP(attributes, user));
    }


    private static User getUser(Map<String, Object> details) {
        User user = new User();
        Boolean activated = Boolean.TRUE;
        String sub = String.valueOf(details.get("sub"));
        String username = null;
        if (details.get("preferred_username") != null) {
            username = ((String) details.get("preferred_username")).toLowerCase();
        }
        // handle resource server JWT, where sub claim is email and uid is ID
        if (details.get("uid") != null) {
            user.setId((String) details.get("uid"));
            user.setLogin(sub);
        } else {
            user.setId(sub);
        }
        if (username != null) {
            user.setLogin(username);
        } else if (user.getLogin() == null) {
            user.setLogin(user.getId());
        }
        if (details.get("given_name") != null) {
            user.setFirstName((String) details.get("given_name"));
        } else if (details.get("name") != null) {
            user.setFirstName((String) details.get("name"));
        }
        if (details.get("family_name") != null) {
            user.setLastName((String) details.get("family_name"));
        }
        if (details.get("email_verified") != null) {
            activated = (Boolean) details.get("email_verified");
        }
        if (details.get("email") != null) {
            user.setEmail(((String) details.get("email")).toLowerCase());
        } else if (sub.contains("|") && (username != null && username.contains("@"))) {
            // special handling for Auth0
            user.setEmail(username);
        } else {
            user.setEmail(sub);
        }
        if (details.get("langKey") != null) {
            user.setLangKey((String) details.get("langKey"));
        } else if (details.get("locale") != null) {
            // trim off country code if it exists
            String locale = (String) details.get("locale");
            if (locale.contains("_")) {
                locale = locale.substring(0, locale.indexOf('_'));
            } else if (locale.contains("-")) {
                locale = locale.substring(0, locale.indexOf('-'));
            }
            user.setLangKey(locale.toLowerCase());
        } else {
            // set langKey to default if not specified by IdP
            user.setLangKey(Constants.DEFAULT_LANGUAGE);
        }
        if (details.get("picture") != null) {
            user.setImageUrl((String) details.get("picture"));
        }
        if (details.get("roles") != null) {
            ArrayList<String> roles = (ArrayList<String>) details.get("roles");
            Set<Authority> authorities = new HashSet<Authority>();
            roles.forEach(role ->
                {
                    Authority authority = new Authority();
                    authority.setName(role);
                    authorities.add(authority);
                }
            );
            user.setAuthorities(authorities);
        }
        user.setActivated(activated);
        return user;
    }
}
