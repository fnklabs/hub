package com.fnklabs.hub.persistent.cassandra.repository;

import com.datastax.driver.core.*;
import com.fnklabs.hub.core.HubKey;
import com.fnklabs.hub.core.persistent.HubDao;
import com.fnklabs.hub.persistent.cassandra.CassandraFactory;
import com.fnklabs.metrics.Metrics;
import com.fnklabs.metrics.MetricsFactory;
import com.fnklabs.metrics.Timer;

public class HubDaoImpl implements HubDao {
    private static final Metrics METRICS = MetricsFactory.getMetrics();

    private final Session session;

    /** insert hub key prepared stmt */
    private final PreparedStatement insert;

    /** select hub key prepared stmt */
    private final PreparedStatement select;

    public HubDaoImpl(CassandraFactory cassandraFactory) {
        this.session = cassandraFactory.getSession();
        insert = prepareHubInsert(session);
        select = prepareHubSelect(session);
    }

    private static PreparedStatement prepareHubSelect(Session session) {
        return session.prepare("select * from hub where domain_id = ? and source_id = ? and system_number = ?");
    }

    private static PreparedStatement prepareHubInsert(Session session) {
        return session.prepare("insert into hub (domain_id, source_id, system_number, hub_id) values (?,?,?,?)");
    }

    @Override
    public long find(HubKey key) {
        Timer timer = METRICS.getTimer("hub.dao.hub.read");

        try {
            BoundStatement boundStatement = select.bind(key.getDomain(), key.getSource(), key.getSourceID());

            ResultSet resultSet = session.execute(boundStatement);

            Row row = resultSet.one();
            return row != null ? row.getLong("hub_id") : 0;
        } finally {
            timer.stop();
        }
    }

    @Override
    public void save(HubKey key, long id) {
        Timer timer = METRICS.getTimer("hub.dao.hub.write");

        try {
            BoundStatement boundStatement = insert.bind(key.getDomain(), key.getSource(), key.getSourceID(), id);

            session.execute(boundStatement);
        } finally {
            timer.stop();
        }
    }
}
