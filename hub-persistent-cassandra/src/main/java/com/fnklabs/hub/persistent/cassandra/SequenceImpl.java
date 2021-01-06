package com.fnklabs.hub.persistent.cassandra;

import com.fnklabs.hub.core.Sequence;

public class SequenceImpl implements Sequence {
    private final int domain;
    private final long value;

    public SequenceImpl(int domain, Long value) {
        this.domain = domain;
        this.value = value;
    }

    @Override
    public int getDomain() {
        return domain;
    }

    @Override
    public long getValue() {
        return value;
    }
}
