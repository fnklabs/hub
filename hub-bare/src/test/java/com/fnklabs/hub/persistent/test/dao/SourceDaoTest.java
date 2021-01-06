package com.fnklabs.hub.persistent.test.dao;

import com.fnklabs.hub.core.Source;
import com.fnklabs.hub.core.persistent.SourceDao;
import com.fnklabs.hub.persistent.test.SourceImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SourceDaoTest implements SourceDao {
    private final Map<String, Source> sources = new ConcurrentHashMap<>();

    @Override
    public Source find(String name) {
        return sources.get(name);
    }

    @Override
    public Source save(String name, int id) {
        sources.put(name, new SourceImpl(name, id));

        return sources.get(name);
    }

}
