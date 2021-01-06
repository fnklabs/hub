package com.fnklabs.hub.core;

/**
 * Sequence service will acquire block of ID from name and store it locally for more performance
 */
public interface SequenceService {

    /**
     * Register new sequence
     *
     * @param domain Sequence name
     */
    void register(Domain domain);

    /**
     * Change sequence value for provided name
     *
     * @param domain   Domain
     * @param newValue New sequence value
     */
    void changeSequence(Domain domain, long newValue);

    /**
     * Get next value from sequence for provided domain
     *
     * @param domain domain
     *
     * @return long
     *
     * @throws NullPointerException if domain doesn't exists
     */
    long next(Domain domain);
}