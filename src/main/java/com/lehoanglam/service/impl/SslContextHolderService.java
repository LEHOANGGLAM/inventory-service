package com.yes4all.service.impl;

import javax.annotation.PostConstruct;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Data
@Service("sslContextHolderService")
public class SslContextHolderService {

    private static final Logger logger = LoggerFactory.getLogger(SslContextHolderService.class);

    @Autowired
    private Environment environment;

    private SSLContext sslContext;
    private SSLContext sslContext12;
    private HostnameVerifier hostnameVerifier;

    private static final String TLS_V1 = "TLSv1";
    private static final String TLS_V12 = "TLSv1.2";

    @PostConstruct
    void init() throws Exception {
        hostnameVerifier = initHostnameVerifier();
    }

    private HostnameVerifier initHostnameVerifier() {
        return (String hostname, SSLSession session) -> true;
    }
}
