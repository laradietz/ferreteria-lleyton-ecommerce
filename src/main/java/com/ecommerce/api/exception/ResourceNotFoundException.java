package com.ecommerce.api.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Object identifier) {
        super(resource + " no encontrado: " + identifier);
    }
}
