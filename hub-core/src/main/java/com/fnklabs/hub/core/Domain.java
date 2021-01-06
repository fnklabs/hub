package com.fnklabs.hub.core;

public interface Domain {
    /**
     * Domain ID
     *
     * @return Domain ID
     */
    int getId();

    /**
     * Domain name
     *
     * @return Domain name
     */
    String getName();

    /**
     * Get sequence acquire block size
     *
     * @return {@code > 0}
     */
    int getAcquireBlockSize();

    /**
     * Get max sequence value
     *
     * @return {@code > 0}
     */
    long getMaxValue();
}
