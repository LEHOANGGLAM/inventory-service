package com.yes4all.web.rest.config;

import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.service.impl.LoggingService;
import com.yes4all.service.impl.RequestHolderService;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

@ControllerAdvice
public class CustomRequestBodyAdviceAdapter extends RequestBodyAdviceAdapter {

    @Autowired
    HttpServletRequest httpServletRequest;

    @Autowired
    RequestHolderService requestHolderService;

    @Autowired
    LoggingService loggingService;

    @Override
    public Object afterBodyRead(
        Object body,
        HttpInputMessage inputMessage,
        MethodParameter parameter,
        Type targetType,
        Class<? extends HttpMessageConverter<?>> converterType
    ) {
        loggingService.logRequest(httpServletRequest, body);
        if (CommonDataUtil.isNotNull(body)) {
            requestHolderService.setRestRequest(body);
            requestHolderService.setTicketId(UUID.randomUUID().toString());
            requestHolderService.setRequestDate(new Date());
        }
        requestHolderService.setBeginRequest(System.nanoTime());
        return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }
}
