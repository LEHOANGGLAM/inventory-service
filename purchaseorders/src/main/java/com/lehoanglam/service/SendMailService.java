package com.yes4all.service;

import java.util.List;

public interface SendMailService {
      void doSendMail(String subject, String content, List<String> receivers);

}
