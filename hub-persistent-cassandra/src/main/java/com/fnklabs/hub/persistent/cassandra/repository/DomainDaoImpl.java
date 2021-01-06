package com.fnklabs.hub.persistent.cassandra.repository;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.fnklabs.hub.core.Domain;
import com.fnklabs.hub.core.persistent.DomainDao;
import com.fnklabs.hub.persistent.cassandra.CassandraFactory;
import com.fnklabs.hub.persistent.cassandra.DomainImpl;
import com.fnklabs.metrics.MetricsFactory;
import com.fnklabs.metrics.Timer;

import java.util.Optional;

public class DomainDaoImpl implements DomainDao {
    private final Session session;

    public DomainDaoImpl(CassandraFactory cassandraFactory) {
        session = cassandraFactory.getSession();
    }

    @Override
    public Domain save(String name, int id, int acquireBlockSize, long maxValue) {
        Timer timer = MetricsFactory.getMetrics().getTimer("hub.dao.domain.write");

        session.execute(
                QueryBuilder.insertInto("domain")
                            .value("name", name)
                            .value("domain_id", id)
                            .value("acquire_block_size", acquireBlockSize)
                            .value("max_value", maxValue)
        );

        timer.stop();
        return new DomainImpl(name, id, acquireBlockSize, maxValue);
    }

    @Override
    public Domain find(String name) {
        Timer timer = MetricsFactory.getMetrics().getTimer("hub.dao.domain.read");
        ResultSet resultSet = session.execute(
                QueryBuilder.select()
                            .from("domain")
                            .where(QueryBuilder.eq("name", name))

        );

        DomainImpl domain = Optional.ofNullable(resultSet.one())
                                    .map(r -> new DomainImpl(
                                            r.getString("name"),
                                            r.getInt("domain_id"),
                                            r.getInt("acquire_block_size"),
                                            r.getLong("max_value")
                                    ))
                                    .orElse(null);

        timer.stop();

        return domain;
    }
}
