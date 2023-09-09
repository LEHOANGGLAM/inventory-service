package com.yes4all.service.impl;

import com.yes4all.common.errors.BusinessException;
import com.yes4all.service.SendMailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;

import static com.yes4all.constants.MailConstant.*;

@Service
public class SendMailServiceImpl implements SendMailService {

    private static final Logger log = LoggerFactory.getLogger(SendMailServiceImpl.class);
    @Value("${spring.mail.host}")
    private String host;
    @Value("${spring.mail.username}")
    private String username;
    @Value("${spring.mail.password}")
    private String password;
    @Value("${spring.mail.port}")
    private String port;
    @Value("${spring.mail.smtp.auth}")
    private String auth;
    @Value("${spring.mail.smtp.starttls.enable}")
    private String tlsStart;
    @Value("${spring.mail.smtp.ssl.trust}")
    private String sslTrust;
    @Value("${spring.mail.sender}")
    private String sender;

    @Override
    public void doSendMail(String subject, String content, List<String> receivers) {

        log.info("START send mail subject: {}", subject);
        Properties props = new Properties();
        props.put(MAIL_CONFIG_AUTH, auth);
        props.put(MAIL_CONFIG_TLS_START, tlsStart);
        props.put(MAIL_CONFIG_HOST, host);
        props.put(MAIL_CONFIG_PORT, port);
        props.put(MAIL_CONFIG_SSL_TRUST, sslTrust);

        Session session = Session.getInstance(props,
            new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            for (String toEmail : receivers) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            }
            message.setSubject(subject);
            message.setContent(content,"text/html");
            Transport.send(message);
            log.info("Sent message successfully");

        } catch (MessagingException e) {
            log.error(e.getMessage());
            throw new BusinessException("Fail to send mail.");
        }
    }
}
