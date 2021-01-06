package com.fnklabs.hub.persistent.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.fnklabs.hub.core.Domain;
import com.fnklabs.hub.core.HubKey;
import com.fnklabs.hub.persistent.cassandra.repository.DomainDaoImpl;
import com.fnklabs.hub.persistent.cassandra.repository.HubDaoImpl;
import com.fnklabs.hub.persistent.cassandra.repository.SequenceDaoImpl;
import com.fnklabs.hub.persistent.cassandra.repository.SourceDaoImpl;
import com.google.common.collect.ImmutableMap;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;


public class HubDaoBenchmark extends AbstractBenchmark {

    @Benchmark
    public void read(HubDaoContext context) {
        context.hubDao.find(context.key());
    }

    @Benchmark
    public void write(HubDaoContext context) {
        context.hubDao.save(context.key(), context.id.incrementAndGet());
    }


    @State(Scope.Benchmark)
    public static class HubDaoContext {
        private CassandraFactory cassandraFactory;

        public HubDaoImpl hubDao;

        public final AtomicLong id = new AtomicLong();

        @Setup
        public void setUp() {
            cassandraFactory = new CassandraFactory(
                    System.getProperty("cassandra.hosts", "127.0.0.1").split(","),
                    "hub",
                    null,
                    null,
                    ConsistencyLevel.QUORUM.name(),
                    null,
                    10_000,
                    10_000,
                    8,
                    8
            );

            hubDao = new HubDaoImpl(cassandraFactory);
        }

        @TearDown
        public void tearDown() throws IOException {
            cassandraFactory.getSession().execute("truncate hub");

            cassandraFactory.close();
        }

        public HubKey key() {
            return new HubKey(1, 1, UUID.randomUUID().toString());
        }
    }

}
