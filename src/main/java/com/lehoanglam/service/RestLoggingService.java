package com.yes4all.service;

import java.util.Date;
import javax.servlet.http.HttpServletRequest;

public interface RestLoggingService {
    void inboundLogWritingREST(HttpServletRequest httpServletRequest, Object body, String ticketId, long responseTime);
    void outboundLogWritingREST(
        String reqText,
        String resText,
        long processTime,
        String url,
        String funcName,
        String ticketId,
        Date reqDate,
        Date resDate
    );
}
