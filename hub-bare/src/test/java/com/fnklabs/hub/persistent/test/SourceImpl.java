package com.fnklabs.hub.persistent.test;

import com.fnklabs.hub.core.Source;

public class SourceImpl implements Source {
    private final String name;
    private final int id;

    public SourceImpl(String name, int id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }
}
