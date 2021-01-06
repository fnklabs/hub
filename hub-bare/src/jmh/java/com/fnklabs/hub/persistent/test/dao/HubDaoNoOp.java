package com.fnklabs.hub.persistent.test.dao;

import com.fnklabs.hub.core.HubKey;
import com.fnklabs.hub.core.persistent.HubDao;

public class HubDaoNoOp implements HubDao {

    @Override
    public long find(HubKey hubKey) {
        return 0;
    }

    @Override
    public void save(HubKey hubKey, long id) {

    }
}
