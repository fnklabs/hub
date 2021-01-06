package com.fnklabs.hub;

import com.fnklabs.hub.core.HubService;
import com.fnklabs.hub.core.persistent.DomainDao;
import com.fnklabs.hub.core.persistent.HubDao;
import com.fnklabs.hub.core.persistent.SequenceDao;
import com.fnklabs.hub.core.persistent.SourceDao;
import com.fnklabs.hub.persistent.test.HubDaoInMemoryFactory;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.openjdk.jmh.annotations.*;

import java.util.Map;
import java.util.UUID;


public class HubBenchmark extends AbstractBenchmark {

    @Benchmark
    public void generateKey(GenerateKeyContext context, Parameters parameters) {
        context.hubService.getIdFor(Parameters.DOMAIN, parameters.key());
    }

    @Benchmark
    public void getExistingKey(Context context, Parameters parameters) {
        context.hubService.getIdFor(Parameters.DOMAIN, parameters.existingKey);
    }

    public static abstract class AbstractContext {
        public HubService hubService;

        public void setUp() {
            DomainDao domainDao = HubDaoInMemoryFactory.domainDao();
            SourceDao sourceDao = HubDaoInMemoryFactory.sourceDao();
            SequenceDao sequenceDao = HubDaoInMemoryFactory.sequenceDao();
            SequenceServiceImpl sequenceService = new SequenceServiceImpl(sequenceDao);

            hubService = new HubServiceBareImpl(
                    getCacheSize(),
                    getCacheTtl(),
                    getCacheCleanup(),
                    sequenceService,
                    getHubDao(),
                    domainDao,
                    sourceDao
            );

            hubService.register(HubServiceBareImpl.SYSTEM_DOMAIN_SEQUENCE, 1, Integer.MAX_VALUE);
            hubService.register(HubServiceBareImpl.SYSTEM_SOURCE_SEQUENCE, 1, Integer.MAX_VALUE);


            hubService.register(Parameters.DOMAIN, getAcquireBlockSize(), Long.MAX_VALUE);
            hubService.registerSource(Parameters.SOURCE);
        }

        protected HubDao getHubDao() {
            return HubDaoInMemoryFactory.hubDaoNoOp();
        }

        public void tearDown() throws Exception {
            hubService.close();
        }

        protected abstract int getAcquireBlockSize();

        protected abstract int getCacheCleanup();

        protected abstract int getCacheTtl();

        protected abstract int getCacheSize();
    }

    @State(Scope.Benchmark)
    public static class GenerateKeyContext extends AbstractContext {
        @Param(value = {"1000"})
        public int cacheSize;

        @Param(value = {"100"})
        public int cacheTtl;

        @Param(value = {"-1"})
        public int cacheCleanup;

        @Param(value = {"1", "1000", "10000"})
        public int acquireBlockSize;

        @Setup
        @Override
        public void setUp() {
            super.setUp();
        }

        @Override
        protected int getAcquireBlockSize() {
            return acquireBlockSize;
        }

        @Override
        protected int getCacheCleanup() {
            return cacheCleanup;
        }

        @Override
        protected int getCacheTtl() {
            return cacheTtl;
        }

        @Override
        protected int getCacheSize() {
            return cacheSize;
        }

        @TearDown
        @Override
        public void tearDown() throws Exception {
            super.tearDown();
        }
    }

    @State(Scope.Benchmark)
    public static class Context extends AbstractContext {
        public HubService hubService;

        @Param(value = {"1000"})
        public int cacheSize;

        @Param(value = {"100"})
        public int cacheTtl;

        @Param(value = {"-1"})
        public int cacheCleanup;

        @Setup
        @Override
        public void setUp() {
            super.setUp();
        }

        @TearDown
        @Override
        public void tearDown() throws Exception {
            super.tearDown();
        }

        @Override
        protected HubDao getHubDao() {
            return HubDaoInMemoryFactory.hubDao();
        }

        @Override
        protected int getAcquireBlockSize() {
            return 1;
        }

        @Override
        protected int getCacheCleanup() {
            return cacheCleanup;
        }

        @Override
        protected int getCacheTtl() {
            return cacheTtl;
        }

        @Override
        protected int getCacheSize() {
            return cacheSize;
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
