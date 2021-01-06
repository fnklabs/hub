package com.fnklabs.hub.persistent.test.dao;

import com.fnklabs.hub.core.Sequence;
import com.fnklabs.hub.core.persistent.SequenceDao;
import com.fnklabs.hub.persistent.test.SequenceTestImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SequenceTestDao implements SequenceDao {
    private final Map<Integer, SequenceTestImpl> sequence = new ConcurrentHashMap<>();

    public SequenceTestDao() {}

    @Override
    public Sequence find(int domain) {
        return sequence.get(domain);
    }

    @Override
    public void create(int domain, long value) {
        sequence.put(domain, new SequenceTestImpl(domain, value));
    }


    @Override
    public void update(int domain, long value) {
        sequence.computeIfPresent(domain, (k, v) -> new SequenceTestImpl(domain, value));
    }
}
