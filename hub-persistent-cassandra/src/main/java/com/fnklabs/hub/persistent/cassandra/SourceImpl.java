package com.fnklabs.hub.persistent.cassandra;

import com.fnklabs.hub.core.Source;

import java.util.Objects;

public class SourceImpl implements Source {
    private final String name;
    private final int id;

    public SourceImpl(String name, int id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceImpl source = (SourceImpl) o;
        return Objects.equals(name, source.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
