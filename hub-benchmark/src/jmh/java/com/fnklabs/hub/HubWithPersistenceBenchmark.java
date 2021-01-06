package com.fnklabs.hub;

import com.datastax.driver.core.ConsistencyLevel;
import com.fnklabs.hub.core.Domain;
import com.fnklabs.hub.persistent.cassandra.CassandraFactory;
import com.fnklabs.hub.persistent.cassandra.repository.DomainDaoImpl;
import com.fnklabs.hub.persistent.cassandra.repository.HubDaoImpl;
import com.fnklabs.hub.persistent.cassandra.repository.SequenceDaoImpl;
import com.fnklabs.hub.persistent.cassandra.repository.SourceDaoImpl;
import com.google.common.collect.ImmutableMap;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;


public class HubWithPersistenceBenchmark extends AbstractBenchmark {

    @Benchmark
    public void generateKey(CreateKeyContext context, Parameters parameters) {
        context.hubService.getIdFor(Parameters.DOMAIN, parameters.key());
    }

    @Benchmark
    public void getExistingKey(GetKeyContext context, Parameters parameters) {
        context.hubService.getIdFor(Parameters.DOMAIN, parameters.existingKey);
    }

    @Benchmark
    public void buildKeys(CreateKeyContext context, Parameters parameters) {
        context.hubService.buildKeys(context.domain, parameters.key());
    }

    @Benchmark
    public void nextId(CreateKeyContext context) {
        context.hubService.getNextID(context.domain);
    }


    @State(Scope.Benchmark)
    public static class GetKeyContext extends HubContext {


        @Param(value = {"10000"})
        public int cacheSize;

        @Param(value = {"100"})
        public int cacheTtl;

        @Param(value = {"-1"})
        public int cacheCleanup;

        @Param({"1"})
        public int acquiredBlockSize;


        @Override
        int getCacheSize() {
            return cacheSize;
        }

        @Override
        int getCacheTtl() {
            return cacheTtl;
        }

        @Override
        int getCacheCleanup() {
            return cacheCleanup;
        }

        @Override
        int getAcquireBlockSize() {
            return acquiredBlockSize;
        }

        @Setup
        @Override
        public void setUp() {
            super.setUp();
        }

        @TearDown
        @Override
        public void tearDown() throws IOException {
            super.tearDown();
        }
    }

    @State(Scope.Benchmark)
    public static class CreateKeyContext extends HubContext {

        @Param(value = {"10000"})
        public int cacheSize;

        @Param(value = {"100"})
        public int cacheTtl;

        @Param(value = {"-1"})
        public int cacheCleanup;

        @Param({"1", "10000"})
        public int acquiredBlockSize;


        @Override
        int getCacheSize() {
            return cacheSize;
        }

        @Override
        int getCacheTtl() {
            return cacheTtl;
        }

        @Override
        int getCacheCleanup() {
            return cacheCleanup;
        }

        @Override
        int getAcquireBlockSize() {
            return acquiredBlockSize;
        }

        @Setup
        @Override
        public void setUp() {
            super.setUp();
        }

        @TearDown
        @Override
        public void tearDown() throws IOException {
            super.tearDown();
        }
    }

    public static abstract class HubContext {
        private CassandraFactory cassandraFactory;
        public HubServiceBareImpl hubService;
        public Domain domain;

        abstract int getCacheSize();

        abstract int getCacheTtl();

        abstract int getCacheCleanup();

        abstract int getAcquireBlockSize();

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
            hubService = new HubServiceBareImpl(getCacheSize(), getCacheTtl(), getCacheCleanup(),
                                                new SequenceServiceImpl(new SequenceDaoImpl(cassandraFactory)),
                                                new HubDaoImpl(cassandraFactory),
                                                new DomainDaoImpl(cassandraFactory),
                                                new SourceDaoImpl(cassandraFactory)
            );

            hubService.register(HubServiceBareImpl.SYSTEM_DOMAIN_SEQUENCE, 1, Integer.MAX_VALUE);
            hubService.register(HubServiceBareImpl.SYSTEM_SOURCE_SEQUENCE, 1, Integer.MAX_VALUE);

            domain = hubService.register(Parameters.DOMAIN, getAcquireBlockSize(), Long.MAX_VALUE);

            hubService.registerSource(Parameters.SOURCE);
        }

        public void tearDown() throws IOException {
            cassandraFactory.getSession().execute("truncate domain");
            cassandraFactory.getSession().execute("truncate sequence");
            cassandraFactory.getSession().execute("truncate source");
            cassandraFactory.getSession().execute("truncate hub");

            cassandraFactory.close();
        }
    }

    @State(Scope.Thread)
    public static class Parameters {
        public static final String DOMAIN = "domain";
        public static final String SOURCE = "source";

        public Map<String, String> existingKey = ImmutableMap.of(SOURCE, new UUID(0, 1).toString());

        public Map<String, String> key() {
            return ImmutableMap.of(
                    SOURCE, UUID.randomUUID().toString()
            );
        }
    }
}
