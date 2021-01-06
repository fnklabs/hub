package com.fnklabs.hub.core;

public class HubException extends RuntimeException{
    public HubException() {
        super();
    }

    public HubException(String message) {
        super(message);
    }

    public HubException(String message, Throwable cause) {
        super(message, cause);
    }

    public HubException(Throwable cause) {
        super(cause);
    }

    protected HubException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
