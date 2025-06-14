// src/main/java/com/cloudflix/backend/exception/StorageException.java
package com.cloudflix.backend.exception;

public class StorageException extends RuntimeException {
    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}