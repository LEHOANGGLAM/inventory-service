package com.yes4all.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtil;
import com.yes4all.service.RestLoggingService;
import com.yes4all.service.dto.RestLoggingDTO;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RestLoggingServiceImpl implements RestLoggingService {

    private static final Logger logger = LoggerFactory.getLogger(RestLoggingServiceImpl.class);

    private static Gson GSON;

    private static ObjectMapper OBJECT_MAPPER;

    @PostConstruct
    protected void init() {
        GSON = new Gson();
        OBJECT_MAPPER = new ObjectMapper();
    }

    @Override
    public void inboundLogWritingREST(HttpServletRequest httpServletRequest, Object body, String ticketId, long responseTime) {
        String bodyAsString = GSON.toJson(body);
        if (bodyAsString != null && !bodyAsString.isEmpty()) {
            bodyAsString = CommonDataUtil.evaluateJsonString(bodyAsString);
        }
        RestLoggingDTO loggingDTO = new RestLoggingDTO();
        loggingDTO.setTimestamp(DateUtil.formatDate(new Date(), DateUtil.ISO_DATE_TIME_FORMAT));
        loggingDTO.setDetailed_message(bodyAsString);
        loggingDTO.setFunctionName(httpServletRequest.getRequestURI());
        loggingDTO.setLevel("INFO");
        loggingDTO.setRefID(ticketId);
        loggingDTO.setRequestURL(httpServletRequest.getRequestURL().toString());
        loggingDTO.setResponseTime(responseTime);
        logger.debug(convertObj2String(loggingDTO));
    }

    @Override
    public void outboundLogWritingREST(
        String reqText,
        String resText,
        long processTime,
        String url,
        String funcName,
        String ticketId,
        Date reqDate,
        Date resDate
    ) {
        writeOutboundLog(reqText, resText, processTime, url, funcName, ticketId, reqDate, resDate);
    }

    private void writeOutboundLog(
        String reqText,
        String resText,
        long processTime,
        String url,
        String funcName,
        String ticketId,
        Date reqDate,
        Date resDate
    ) {
        RestLoggingDTO requestLog = new RestLoggingDTO();
        requestLog.setTimestamp(DateUtil.formatDate(reqDate, DateUtil.ISO_DATE_TIME_FORMAT));
        requestLog.setDetailed_message(reqText);
        requestLog.setFunctionName(funcName);
        requestLog.setLevel("INFO");
        requestLog.setRefID(ticketId);
        requestLog.setRequestURL(url);

        RestLoggingDTO responseLog = new RestLoggingDTO();
        responseLog.setTimestamp(DateUtil.formatDate(resDate, DateUtil.ISO_DATE_TIME_FORMAT));
        responseLog.setDetailed_message(resText);
        responseLog.setFunctionName(funcName);
        responseLog.setLevel("INFO");
        responseLog.setRefID(ticketId);
        responseLog.setRequestURL(url);
        responseLog.setResponseTime(processTime);
        logger.debug(makeMsg("", "", "%s", convertObj2String(requestLog)));
        logger.debug(makeMsg("", "", "%s", convertObj2String(responseLog)));
    }

    private String convertObj2String(Object object) {
        String objectAsString = null;
        try {
            objectAsString = OBJECT_MAPPER.writeValueAsString(object);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        return objectAsString;
    }

    private static String makeMsg(String prefix, String key, String format, Object... args) {
        if ("".equals(prefix)) {
            return String.format("%s", String.format(format, args));
        }
        return String.format("%s%s %s", key, prefix, String.format(format, args));
    }
}
