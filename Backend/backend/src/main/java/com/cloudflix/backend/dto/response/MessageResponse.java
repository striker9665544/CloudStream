package com.cloudflix.backend.dto.response;

//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;

//@Data
//@NoArgsConstructor
//@AllArgsConstructor
public class MessageResponse {
    public String message; // Make public

    public MessageResponse() {} // Add manual no-arg

    public MessageResponse(String message) {
        this.message = message;
    }
}