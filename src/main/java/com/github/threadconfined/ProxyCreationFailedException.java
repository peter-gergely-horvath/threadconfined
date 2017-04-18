package com.github.threadconfined;

public class ProxyCreationFailedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProxyCreationFailedException() {
        super();
    }
    
    public ProxyCreationFailedException(String message) {
        super(message);
    }
    
    public ProxyCreationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
    
}

