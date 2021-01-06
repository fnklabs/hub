package com.fnklabs.hub.core;

public class SequenceLimitsExceeded extends HubException {
    public SequenceLimitsExceeded(String message) {
        super(message, null, true, false);
    }
}
