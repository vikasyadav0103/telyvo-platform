package com.telyvo.platform.collaboration;

import org.springframework.http.HttpStatus;

public class ChatIntegrationException extends RuntimeException {
    private final HttpStatus status;
    private final String code;
    public ChatIntegrationException(HttpStatus status, String code, String message) {
        super(message); this.status = status; this.code = code;
    }
    public HttpStatus status() { return status; }
    public String code() { return code; }
}