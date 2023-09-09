package com.yes4all.web.rest.config;

import com.yes4all.common.constants.ErrorConstant;
import com.yes4all.common.utils.DateUtil;
import com.yes4all.service.impl.LoggingService;
import com.yes4all.service.impl.RequestHolderService;
import com.yes4all.web.rest.payload.RestRequest;
import com.yes4all.web.rest.payload.RestResponse;
import com.yes4all.web.rest.payload.RestResponseHeader;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class CustomResponseBodyAdviceAdapter implements ResponseBodyAdvice<Object> {

    private static final Logger log = LoggerFactory.getLogger(CustomResponseBodyAdviceAdapter.class);

    @Autowired
    RequestHolderService requestHolderService;

    @Autowired
    LoggingService loggingService;

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
        Object body,
        MethodParameter methodParameter,
        MediaType mediaType,
        Class<? extends HttpMessageConverter<?>> aClass,
        ServerHttpRequest serverHttpRequest,
        ServerHttpResponse serverHttpResponse
    ) {
        long responseTime = requestHolderService.calculateProcessTime();
        log.info("On RestResponse");
        if (serverHttpRequest instanceof ServletServerHttpRequest && serverHttpResponse instanceof ServletServerHttpResponse) {
            log.info("-----RestResponse-----");
            log.info(body.toString());
            if (body instanceof RestResponse) {
                log.info("RestResponse");
                RestResponse restResponse = (RestResponse) body;
                if (restResponse.getHeader() == null) {
                    RestResponseHeader responseHeader = RestResponseHeader
                        .builder()
                        .respCode(ErrorConstant.SUCCESS_RESPONSE_CODE)
                        .respDesc("Success")
                        .build();
                    restResponse.setHeader(responseHeader);
                    mapCommonResponseHeader(responseHeader);
                }
            }
            log.info("In RestResponse");
            loggingService.logResponse(((ServletServerHttpRequest) serverHttpRequest).getServletRequest(), body, responseTime);
        }
        return body;
    }

    private void mapCommonResponseHeader(RestResponseHeader responseHeader) {
        responseHeader.setMessageDt(DateUtil.toString(DateUtil.STANDARD_DATE_TIME_CURRENT_FORMAT));
        responseHeader.setMessageUid(UUID.randomUUID().toString());
        if (requestHolderService.getRestRequest() instanceof RestRequest) {
            RestRequest restRequest = (RestRequest) requestHolderService.getRestRequest();
            responseHeader.setReqMessageUid(restRequest.getHeader().getMessageUid());
        }
    }
}
