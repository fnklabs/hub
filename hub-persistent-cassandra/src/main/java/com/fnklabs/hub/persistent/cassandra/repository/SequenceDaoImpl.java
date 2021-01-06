package com.fnklabs.hub.persistent.cassandra.repository;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.fnklabs.hub.core.Sequence;
import com.fnklabs.hub.core.persistent.SequenceDao;
import com.fnklabs.hub.persistent.cassandra.CassandraFactory;
import com.fnklabs.hub.persistent.cassandra.SequenceImpl;
import com.fnklabs.metrics.Metrics;
import com.fnklabs.metrics.MetricsFactory;
import com.fnklabs.metrics.Timer;

public class SequenceDaoImpl implements SequenceDao {
    private static final Metrics METRICS = MetricsFactory.getMetrics();

    private final Session session;

    private final PreparedStatement insert;
    private final PreparedStatement select;
    private final PreparedStatement update;

    public SequenceDaoImpl(CassandraFactory cassandraFactory) {
        this.session = cassandraFactory.getSession();
        insert = prepareDomainInsert(session);
        select = prepareDomainSelect(session);
        update = prepareDomainUpdate(session);
    }

    private static PreparedStatement prepareDomainUpdate(Session session) {
        return session.prepare("update sequence set value = ? where domain_id = ?");
    }

    private static PreparedStatement prepareDomainInsert(Session session) {
        return session.prepare("insert into sequence (domain_id, value) values (?,?)");
    }

    private static PreparedStatement prepareDomainSelect(Session session) {
        return session.prepare("select domain_id, value from sequence where domain_id = ?");
    }

    @Override
    public Sequence find(int domain) {
        Timer timer = METRICS.getTimer("hub.dao.sequence.read");
        try {
            ResultSet resultSet = session.execute(select.bind(domain));

            return resultSet.all()
                            .stream()
                            .findFirst()
                            .map(r -> new SequenceImpl(
                                    r.getInt("domain_id"),
                                    r.getLong("value")
                            ))
                            .orElse(null);
        } finally {
            timer.stop();
        }
    }

    @Override
    public void create(int domain, long value) {
        Timer timer = METRICS.getTimer("hub.dao.sequence.write");

        session.execute(insert.bind(domain, value));

        timer.stop();
    }

    @Override
    public void update(int domain, long value) {
        Timer timer = METRICS.getTimer("hub.dao.sequence.update");

        try {
            session.execute(update.bind(value, domain));

            find(domain);
        } finally {
            timer.stop();
        }
    }
}