package com.yes4all.service.impl;

import com.yes4all.common.constants.ErrorConstant;
import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.errors.ConnectionException;
import com.yes4all.service.RestLoggingService;
import com.yes4all.service.dto.RestInfo;
import java.net.SocketTimeoutException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public abstract class ExternalRestService<R, S> extends RestService<R, S> {

    private static final Logger logger = LoggerFactory.getLogger(ExternalRestService.class);

    @Autowired
    RestLoggingService loggingService;

    @Autowired
    private RequestHolderService requestHolderService;

    protected CloseableHttpClient getREST() {
        return HttpClients.createDefault();
    }

    public final S callREST(R request, String httpMethod) {
        RestInfo<S> restInfo = getRestInfo();
        Class<S> resType = restInfo.getResType();

        String errorStatus;
        String errorMsg;

        try {
            HttpUriRequest httpRequest = getHttpUriRequest(httpMethod, request);

            CloseableHttpClient client = getREST();
            CloseableHttpResponse result = client.execute(httpRequest);
            String resText = EntityUtils.toString(result.getEntity());

            S response = mapper.readValue(resText, resType);
            logger.debug("callREST- Response: " + resText);
            errorMsg = toError(response);
            if (errorMsg != null) {
                throw new BusinessException(errorMsg);
            }

            return response;
        } catch (SocketTimeoutException e) {
            errorMsg = e.toString();
            errorStatus = "99";
            logger.error(e.getMessage());
            throw new ConnectionException(errorStatus, errorMsg);
        } catch (Exception e) {
            errorMsg = e.toString();
            errorStatus = ErrorConstant.ERROR_RESPONSE_CODE;
            logger.error(e.getMessage());
            throw new ConnectionException(errorStatus, errorMsg);
        }
    }
}
