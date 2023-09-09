package com.yes4all.common.errors;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiError {

    private HttpStatus status;
    private String message;
    private List<String> errors = new ArrayList<>();

    public ApiError(HttpStatus status, String message, List<String> errors) {
        super();
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    public ApiError(HttpStatus status, String message, String error) {
        super();
        this.status = status;
        this.message = message;
        errors = List.of(error);
    }

    public ApiError(HttpStatus status, String message) {
        super();
        this.status = status;
        this.message = message;
    }
}
