// src/main/java/com/cloudflix/backend/exception/StorageFileNotFoundException.java
package com.cloudflix.backend.exception;

public class StorageFileNotFoundException extends StorageException {
    public StorageFileNotFoundException(String message) {
        super(message);
    }

    public StorageFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}