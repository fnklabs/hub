package com.fnklabs.hub.persistent.test;

import com.fnklabs.hub.core.Sequence;

public class SequenceTestImpl implements Sequence {
    private final int domain;
    private final long value;

    public SequenceTestImpl(int domain, long value) {
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
