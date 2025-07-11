package org.FRFood.util;

public class DataAlreadyExistsException extends RuntimeException {
    public DataAlreadyExistsException() {
        super();
    }

    public DataAlreadyExistsException(String message) {
        super(message);
    }
}
