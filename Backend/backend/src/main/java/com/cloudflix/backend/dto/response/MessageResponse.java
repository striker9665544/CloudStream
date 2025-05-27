package com.cloudflix.backend.dto.response;

public class MessageResponse {
    public String message; // Make public

    public MessageResponse() {} // Add manual no-arg

    public MessageResponse(String message) {
        this.message = message;
    }
}