package com.fnklabs.hub;

import com.fnklabs.hub.core.Sequence;
import com.fnklabs.hub.core.SequenceLimitsExceeded;
import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class SequenceThreadLocal implements Sequence {
    private final static Logger log = LoggerFactory.getLogger(SequenceThreadLocal.class);
    private final int domain;

    /**
     * sequence ThreadLocal block max value
     */
    private final long maxValue;

    /**
     * sequence ThreadLocal block start value
     */
    private long value;

    SequenceThreadLocal(int domain, long value, long maxValue) {
        this.domain = domain;
        this.value = value;
        this.maxValue = maxValue;

        log.debug("thread local sequence: {}-{}", value, maxValue);
    }

    @Override
    public int getDomain() {
        return domain;
    }

    @Override
    public long getValue() {
        return value;
    }

    long next() throws SequenceLimitsExceeded {
        value++;

        if (value > maxValue) {
            throw new SequenceLimitsExceeded(String.format("thread local sequence limits exceeded. local value: %d local maxValue: %d", value, maxValue));
        }

        return value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("domain", domain)
                          .add("maxValue", maxValue)
                          .add("value", value)
                          .toString();
    }
}
