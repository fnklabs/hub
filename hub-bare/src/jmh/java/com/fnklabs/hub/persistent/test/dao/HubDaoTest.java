package com.fnklabs.hub.persistent.test.dao;

import com.fnklabs.hub.core.HubKey;
import com.fnklabs.hub.core.persistent.HubDao;

import java.util.concurrent.ConcurrentHashMap;

public class HubDaoTest implements HubDao {
    private final ConcurrentHashMap<HubKey, Long> keys = new ConcurrentHashMap<>();

    @Override
    public long find(HubKey hubKey) {
        Long value = keys.get(hubKey);
        return value == null ? 0 : value;
    }

    @Override
    public void save(HubKey hubKey, long id) {
        keys.put(hubKey, id);
    }
}
