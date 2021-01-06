package com.fnklabs.hub.persistent.cassandra.repository;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.fnklabs.hub.core.Source;
import com.fnklabs.hub.core.persistent.SourceDao;
import com.fnklabs.hub.persistent.cassandra.CassandraFactory;
import com.fnklabs.hub.persistent.cassandra.SourceImpl;
import com.fnklabs.metrics.MetricsFactory;
import com.fnklabs.metrics.Timer;

import java.util.Optional;

public class SourceDaoImpl implements SourceDao {
    private final CassandraFactory cassandraFactory;
    private final Session session;

    public SourceDaoImpl(CassandraFactory cassandraFactory) {
        this.cassandraFactory = cassandraFactory;

        session = cassandraFactory.getSession();
    }

    @Override
    public Source find(String name) {
        Timer timer = MetricsFactory.getMetrics().getTimer("hub.dao.source.read");

        ResultSet resultSet = session.execute(
                QueryBuilder.select()
                            .from("source")
                            .where(QueryBuilder.eq("name", name))
        );


        SourceImpl source = Optional.ofNullable(resultSet.one())
                                    .map(r -> new SourceImpl(r.getString("name"), r.getInt("source_id")))
                                    .orElse(null);

        timer.stop();

        return source;
    }

    @Override
    public Source save(String name, int id) {
        Timer timer = MetricsFactory.getMetrics().getTimer("hub.dao.source.write");

        session.execute(
                QueryBuilder.insertInto("source")
                            .value("name", name)
                            .value("source_id", id)
        );

        timer.stop();

        return new SourceImpl(name, id);
    }
}
