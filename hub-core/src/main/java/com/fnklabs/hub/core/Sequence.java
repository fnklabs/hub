package com.fnklabs.hub.core;

/**
 * Sequencer to generate ID
 */
public interface Sequence {
    int DEFAULT_ACQUIRE_BLOCK_SIZE = 10_000;

    /**
     * Sequence name
     *
     * @return Sequence name
     */
    int getDomain();

    /**
     * Current/last sequence value
     *
     * @return sequence value
     */
    long getValue();

}
