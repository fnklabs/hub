package com.fnklabs.hub.persistent.test;

import com.fnklabs.hub.core.Domain;

public class DomainImpl implements Domain {
    private final String name;
    private final int id;
    private final int acquireBlockSize;
    private final long maxValue;


    public DomainImpl(String name, int id, int acquireBlockSize, long maxValue) {
        this.name = name;
        this.id = id;
        this.acquireBlockSize = acquireBlockSize;
        this.maxValue = maxValue;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAcquireBlockSize() {
        return acquireBlockSize;
    }

    @Override
    public long getMaxValue() {
        return maxValue;
    }
}
