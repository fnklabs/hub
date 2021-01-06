package com.fnklabs.hub.persistent.cassandra;

import com.fnklabs.hub.core.Domain;
import com.google.common.base.MoreObjects;

import java.util.Objects;

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

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("name", name)
                          .add("id", id)
                          .add("acquireBlockSize", acquireBlockSize)
                          .add("maxValue", maxValue)
                          .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainImpl domain = (DomainImpl) o;
        return name.equals(domain.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
