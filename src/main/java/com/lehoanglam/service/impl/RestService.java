package com.yes4all.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yes4all.service.dto.RestInfo;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.springframework.stereotype.Service;

@Service
public abstract class RestService<R, S> {

    protected static final String POST_METHOD = "POST";

    protected static final String GET_METHOD = "GET";

    protected final ObjectMapper mapper = new ObjectMapper();

    protected final HttpUriRequest getHttpUriRequest(String httpMethod, R request) throws JsonProcessingException {
        RestInfo<S> restInfo = getRestInfo();
        String endPoint = restInfo.getEndpoint();
        int timeOut = restInfo.getTimeOut();
        HttpUriRequest httpRequest = null;
        if (POST_METHOD.equalsIgnoreCase(httpMethod)) {
            httpRequest = buildHttpPostRequest(endPoint, timeOut, request);
        } else if (GET_METHOD.equalsIgnoreCase(httpMethod)) {
            httpRequest = buildHttpGetRequest(endPoint, timeOut);
        }
        return httpRequest;
    }

    protected final RestInfo<S> getRestInfo() {
        return RestInfo.<S>builder().endpoint(getEndPoint()).timeOut(getTimeOut()).resType(getRSClass()).build();
    }

    protected final HttpUriRequest buildHttpGetRequest(String hostUrl, int timeOut) {
        HttpGet httpGet = new HttpGet(hostUrl);
        RequestConfig config = RequestConfig
            .custom()
            .setConnectTimeout(timeOut)
            .setSocketTimeout(timeOut)
            .setAuthenticationEnabled(true)
            .build();

        httpGet.setHeader("Content-type", "application/json;charset=utf-8");
        httpGet.setHeader("Accept", "application/json");
        httpGet.setConfig(config);
        return httpGet;
    }

    protected final HttpUriRequest buildHttpPostRequest(String hostUrl, int timeOut, Object request) throws JsonProcessingException {
        String reqText = mapper.writeValueAsString(request);

        final HttpPost httpPost = new HttpPost(hostUrl);
        final RequestConfig config = RequestConfig
            .custom()
            .setConnectTimeout(timeOut)
            .setSocketTimeout(timeOut)
            .setAuthenticationEnabled(true)
            .build();

        httpPost.setEntity(new StringEntity(reqText, "UTF-8"));
        httpPost.setHeader("Content-type", "application/json;charset=utf-8");
        httpPost.setHeader("Accept", "application/json");

        httpPost.setConfig(config);
        return prepareRequestHeader(httpPost);
    }

    protected HttpUriRequest prepareRequestHeader(HttpUriRequest httpUriRequest) {
        return httpUriRequest;
    }

    protected abstract Class<S> getRSClass();

    protected abstract Integer getTimeOut();

    protected abstract String getEndPoint();

    protected abstract String toError(S response);
}
