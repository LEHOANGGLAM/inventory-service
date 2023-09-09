package com.yes4all.service.impl;

import javax.servlet.http.HttpServletRequest;

import com.yes4all.service.RestLoggingService;
import com.yes4all.service.impl.RequestHolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoggingService {

    @Autowired
    private RequestHolderService requestHolderService;

    @Autowired
    private RestLoggingService restLoggingService;

    public void logRequest(HttpServletRequest httpServletRequest, Object body) {
        getRequestHolderService().setServiceId(httpServletRequest.getRequestURI());
        restLoggingService.inboundLogWritingREST(httpServletRequest, body, requestHolderService.getTicketId(), 0);
    }

    public void logResponse(HttpServletRequest httpServletRequest, Object body, long responseTime) {
        restLoggingService.inboundLogWritingREST(httpServletRequest, body, requestHolderService.getTicketId(), responseTime);
    }

    public RequestHolderService getRequestHolderService() {
        return requestHolderService;
    }
}
