package com.telyvo.platform.research;

import org.springframework.http.HttpStatus;

public class ResearchException extends RuntimeException {
    private final HttpStatus status;
    private final String code;
    public ResearchException(HttpStatus status, String code, String message) {
        super(message); this.status = status; this.code = code;
    }
    public HttpStatus status() { return status; }
    public String code() { return code; }
}