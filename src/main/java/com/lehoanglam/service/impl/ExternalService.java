package com.yes4all.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yes4all.common.constants.ErrorConstant;
import com.yes4all.common.errors.ConnectionException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.function.Function;

import com.yes4all.service.RestLoggingService;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExternalService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalService.class);

    @Autowired
    private RestLoggingService restLoggingService;

    @Autowired
    private RequestHolderService requestHolderService;

    protected static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();

    protected CloseableHttpClient getREST(String hostUrl) {
        return HttpClients.createDefault();
    }

    protected <R, S> S callREST(
        String serviceId,
        String hostUrl,
        String contentType,
        int timeOut,
        R request,
        Class<S> resType,
        Function<S, String> toError
    ) {
        String reqText = null;
        Date reqDate = null;
        String resText = null;
        Date resDate = null;
        String errorStatus = ErrorConstant.SUCCESS_RESPONSE_CODE;
        String errorMsg = null;
        long beginRequest = System.nanoTime();
        try {
            reqText = gson.toJson(request);
            final HttpPost httpPost = new HttpPost(hostUrl);
            final RequestConfig config = RequestConfig
                .custom()
                .setConnectTimeout(timeOut)
                .setSocketTimeout(timeOut)
                .setAuthenticationEnabled(true)
                .build();

            httpPost.setEntity(new StringEntity(reqText, "UTF-8"));
            httpPost.setHeader("Content-type", contentType);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setConfig(config);
            try (CloseableHttpClient client = getREST(hostUrl)) {
                reqDate = new Date();
                try (CloseableHttpResponse result = client.execute(httpPost)) {
                    resDate = new Date();
                    resText = EntityUtils.toString(result.getEntity());
                    S response = null;
                    try {
                        logger.info(String.format("CallREST:resText = %s", resText));
                        response = gson.fromJson(resText, resType);
                        logger.info(String.format("CallREST:response = %s", response));
                    } catch (Exception e) {
                        logger.info("exception while parsing json response.");
                    }

                    errorMsg = toError.apply(response);
                    if (errorMsg != null) {
                        return null;
                    }

                    return response;
                }
            }
        } catch (SocketTimeoutException e) {
            errorMsg = e.toString();
            errorStatus = "99";
            logger.error(e.getMessage());
            throw new ConnectionException(errorStatus, errorMsg);
        } catch (Exception e) {
            errorMsg = e.toString();
            errorStatus = "20";
            logger.error(e.getMessage());
            throw new ConnectionException(errorStatus, errorMsg);
        } finally {
            long processTime = (System.nanoTime() - beginRequest) / 1000000;
            if (resDate == null) {
                resDate = new Date();
            }
            restLoggingService.outboundLogWritingREST(
                reqText,
                resText,
                processTime,
                hostUrl,
                serviceId,
                requestHolderService.getTicketId(),
                reqDate,
                resDate
            );
        }
    }
}
