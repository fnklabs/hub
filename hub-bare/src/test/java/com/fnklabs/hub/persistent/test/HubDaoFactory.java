package com.fnklabs.hub.persistent.test;

import com.fnklabs.hub.core.persistent.DomainDao;
import com.fnklabs.hub.core.persistent.HubDao;
import com.fnklabs.hub.core.persistent.SequenceDao;
import com.fnklabs.hub.core.persistent.SourceDao;
import com.fnklabs.hub.persistent.test.dao.*;

public class HubDaoFactory {
    public static DomainDao domainDao() {
        return new DomainDaoTest();
    }

    public static HubDao hubDao() {
        return new HubDaoTest();
    }

    public static HubDao hubDaoNoOp() {
        return new HubDaoNoOp();
    }

    public static SourceDao sourceDao() {
        return new SourceDaoTest();
    }

    public static SequenceDao sequenceDao() {
        return new SequenceTestDao();
    }

}
