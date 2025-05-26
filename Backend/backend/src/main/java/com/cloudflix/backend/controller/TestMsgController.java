package com.cloudflix.backend.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudflix.backend.dto.response.MessageResponse;

//In a new TestMsgController.java or even AuthController for a quick test
@RestController
public class TestMsgController { // Or add to AuthController temporarily
 @GetMapping(value = "/api/test-msg", produces = MediaType.APPLICATION_JSON_VALUE)
 public ResponseEntity<MessageResponse> testMessage() {
     return ResponseEntity.ok(new MessageResponse("Test successful"));
 }
}
