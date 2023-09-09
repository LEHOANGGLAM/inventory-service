package com.yes4all.web.rest.config;

import static com.yes4all.common.constants.ErrorConstant.ERROR_RESPONSE_CODE;
import static com.yes4all.common.constants.ErrorConstant.NOT_FOUND_RESPONSE_CODE;

import com.yes4all.common.errors.ApiError;
import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.errors.NotFoundException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.common.utils.DateUtil;
import com.yes4all.service.impl.RequestHolderService;
import com.yes4all.web.rest.payload.RestRequest;
import com.yes4all.web.rest.payload.RestResponse;
import com.yes4all.web.rest.payload.RestResponseHeader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomRestExceptionHandler.class);

    @Autowired
    private RequestHolderService requestHolderService;

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpHeaders headers,
        HttpStatus status,
        WebRequest request
    ) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            //            errors.add(error.getField() + ": " + error.getDefaultMessage());
            errors.add(error.getDefaultMessage());
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            //            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
            errors.add(error.getDefaultMessage());
        }

        RestResponseHeader responseHeader = RestResponseHeader
            .builder()
            .respCode(ERROR_RESPONSE_CODE)
            .respDesc(String.join("\n", errors))
            .build();

        RestResponse restResponse = RestResponse.builder().header(responseHeader).build();
        mapCommonResponseHeader(responseHeader);
        requestHolderService.setRestResponse(restResponse);
        requestHolderService.calculateProcessTime();
        return ResponseEntity.ok().body(restResponse);
        //        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), errors);
        //        return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
        MissingServletRequestParameterException ex,
        HttpHeaders headers,
        HttpStatus status,
        WebRequest request
    ) {
        String error = ex.getParameterName() + " parameter is missing";

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({ MethodArgumentTypeMismatchException.class })
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        String error = ex.getName() + " should be of type " + ex.getRequiredType().getName();

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(BusinessException.class)
    public final ResponseEntity<Object> handleBusinessException(BusinessException ex, WebRequest request) {
        RestResponseHeader responseHeader = RestResponseHeader
            .builder()
            .respCode(CommonDataUtil.isNotEmpty(ex.getErrorCode()) ? ex.getErrorCode() : ERROR_RESPONSE_CODE)
            .respDesc(ex.getErrorDesc())
            .build();

        log.info("-----handleBusinessException-----");
        RestResponse restResponse = RestResponse.builder().header(responseHeader).build();
        mapCommonResponseHeader(responseHeader);
        requestHolderService.setRestResponse(restResponse);
        requestHolderService.calculateProcessTime();
        return ResponseEntity.ok().body(restResponse);
    }

    @ExceptionHandler(NotFoundException.class)
    public final ResponseEntity<Object> handleNotFoundException(NotFoundException ex, WebRequest request) {
        RestResponseHeader responseHeader = RestResponseHeader
            .builder()
            .respCode(CommonDataUtil.isNotEmpty(ex.getErrorCode()) ? ex.getErrorCode() : NOT_FOUND_RESPONSE_CODE)
            .respDesc(ex.getErrorDesc())
            .build();

        RestResponse restResponse = RestResponse.builder().header(responseHeader).build();
        mapCommonResponseHeader(responseHeader);
        requestHolderService.setRestResponse(restResponse);
        requestHolderService.calculateProcessTime();
        return ResponseEntity.ok().body(restResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public final ResponseEntity<Object> handleConstraintViolationException(Exception ex, WebRequest request) {
        RestResponseHeader responseHeader = RestResponseHeader.builder().respCode(ERROR_RESPONSE_CODE).respDesc(ex.getMessage()).build();

        RestResponse restResponse = RestResponse.builder().header(responseHeader).build();

        mapCommonResponseHeader(responseHeader);
        requestHolderService.setRestResponse(restResponse);
        requestHolderService.calculateProcessTime();
        return ResponseEntity.ok().body(restResponse);
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Object> handleUnknownException(Exception ex, WebRequest request) {
        RestResponseHeader responseHeader = RestResponseHeader.builder().respCode(ERROR_RESPONSE_CODE).respDesc(ex.getMessage()).build();

        RestResponse restResponse = RestResponse.builder().header(responseHeader).build();
        log.info("-----handleUnknownException-----");
        mapCommonResponseHeader(responseHeader);
        requestHolderService.setRestResponse(restResponse);
        requestHolderService.calculateProcessTime();
        return ResponseEntity.ok().body(restResponse);
    }

    private void mapCommonResponseHeader(RestResponseHeader responseHeader) {
        responseHeader.setMessageDt(DateUtil.toString(DateUtil.STANDARD_DATE_TIME_CURRENT_FORMAT));
        responseHeader.setMessageUid(UUID.randomUUID().toString());
        log.info("-----mapCommonResponseHeader-----");
        if (requestHolderService.getRestRequest() instanceof RestRequest) {
            RestRequest restRequest = (RestRequest) requestHolderService.getRestRequest();
            responseHeader.setReqMessageUid(restRequest.getHeader().getMessageUid());
        }
    }
}
