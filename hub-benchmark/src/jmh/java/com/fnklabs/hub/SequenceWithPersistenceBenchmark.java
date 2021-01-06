package com.fnklabs.hub;

import com.datastax.driver.core.ConsistencyLevel;
import com.fnklabs.hub.core.Domain;
import com.fnklabs.hub.persistent.cassandra.CassandraFactory;
import com.fnklabs.hub.persistent.cassandra.DomainImpl;
import com.fnklabs.hub.persistent.cassandra.repository.SequenceDaoImpl;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;

public class SequenceWithPersistenceBenchmark extends AbstractBenchmark {
    @Benchmark
    public void nextId(CreateKeyContext context) {
        context.sequenceService.next(context.domain);
    }

    @State(Scope.Benchmark)
    public static class CreateKeyContext {
        public SequenceServiceImpl sequenceService;

        @Param({"1", "10000"})
        public int acquiredBlockSize;

        private CassandraFactory cassandraFactory;

        private Domain domain;

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

            sequenceService = new SequenceServiceImpl(
                    new SequenceDaoImpl(cassandraFactory)
            );

            domain = new DomainImpl("test", 1, acquiredBlockSize, Long.MAX_VALUE);

            sequenceService.register(domain);
        }

        @TearDown
        public void tearDown() throws IOException {
            cassandraFactory.close();
        }
    }
}
