package com.github.threadconfined;

public class ThreadConfinementViolationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ThreadConfinementViolationException() {
        super();
    }
    
    public ThreadConfinementViolationException(String message) {
        super(message);
    }
    
    public ThreadConfinementViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
