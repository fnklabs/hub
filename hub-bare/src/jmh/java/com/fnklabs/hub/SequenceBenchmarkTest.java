package com.fnklabs.hub;

import com.fnklabs.hub.core.Domain;
import com.fnklabs.hub.core.SequenceService;
import com.fnklabs.hub.persistent.test.DomainImpl;
import com.fnklabs.hub.persistent.test.HubDaoInMemoryFactory;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SequenceBenchmarkTest extends AbstractBenchmark {

    @Benchmark
    public void next(Context context) {
        context.sequenceService.next(context.domain);
    }

    @State(Scope.Benchmark)
    public static class Context {
        public SequenceService sequenceService;

        @Param({"1", "100", "10000"})
        public int acquireBlockSize;

        public Domain domain;

        @Setup
        public void setUp() {
            sequenceService = new SequenceServiceImpl(HubDaoInMemoryFactory.sequenceDao());

            domain = new DomainImpl("domain", 0, acquireBlockSize, Long.MAX_VALUE);

            sequenceService.register(domain);
        }

        @TearDown
        public void tearDown() {}
    }
}
