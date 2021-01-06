package com.fnklabs.hub.persistent.test.dao;

import com.fnklabs.hub.core.Domain;
import com.fnklabs.hub.core.persistent.DomainDao;
import com.fnklabs.hub.persistent.test.DomainImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DomainDaoTest implements DomainDao {
    private final Map<String, Domain> domain = new ConcurrentHashMap<>();

    @Override
    public Domain save(String name, int id, int acquireBlockSize, long maxValue) {
        DomainImpl domain = new DomainImpl(name, id, acquireBlockSize, maxValue);
        this.domain.put(name, domain);

        return domain;
    }

    @Override
    public Domain find(String name) {
        return domain.get(name);
    }

}
