package com.fnklabs.hub;

import com.fnklabs.hub.core.Domain;
import com.fnklabs.hub.core.SequenceLimitsExceeded;
import com.fnklabs.hub.persistent.test.dao.SequenceTestDao;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class SequenceServiceImplTest {

    private SequenceServiceImpl sequenceService;

    private ExecutorService executorService;

    @Mock
    private Domain domain;

    @Spy
    private SequenceTestDao sequenceDao;
    private int poolSize;

    @BeforeEach
    public void setUp() throws Exception {
        sequenceService = new SequenceServiceImpl(sequenceDao);
        poolSize = 8;
        executorService = new ThreadPoolExecutor(poolSize, poolSize, Integer.MAX_VALUE, TimeUnit.DAYS, new ArrayBlockingQueue<Runnable>(poolSize));

        doReturn("test").when(domain).getName();
        doReturn(1).when(domain).getId();
        doReturn(1).when(domain).getAcquireBlockSize();
        doReturn(Long.MAX_VALUE).when(domain).getMaxValue();
    }

    @AfterEach
    public void tearDown() throws Exception {
        executorService.shutdownNow();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
    }

    @Test
    public void register() throws Exception {
        sequenceService.register(domain);

        verify(sequenceDao).create(domain.getId(), 0L);
    }

    @Test
    public void changeSequence() throws Exception {
        sequenceService.changeSequence(domain, 1L);

        verify(sequenceDao).update(domain.getId(), 1L);
    }

    @Test
    public void nextIfDomainNotExists() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            sequenceService.next(domain);
        });
    }

    @Test()
    public void nextWithAcquireBlockSize1() throws Exception {
        sequenceService.register(domain);

        for (long i = 0; i < 10; i++) {
            long test = sequenceService.next(domain);

            assertEquals(i + 1, test);
        }
    }

    @Test()
    public void nextWithAcquireBlockSize10() throws Exception {
        doReturn(10).when(domain).getAcquireBlockSize();

        sequenceService.register(domain);

        for (long i = 0; i < 101; i++) {
            long test = sequenceService.next(domain);

            assertEquals(i + 1, test);
        }
    }

    @Test
    public void nextWithLimitsExceeded() throws Exception {
        doReturn(10L).when(domain).getMaxValue();

        sequenceService.register(domain);

        for (int i = 0; i < 10; i++) {
            long id = sequenceService.next(domain);

            assertEquals(i + 1, id);
        }

        assertThrows(SequenceLimitsExceeded.class, () -> {
            long id = sequenceService.next(domain);
        });
    }

    /**
     * Check that thread acquire sequence block
     *
     * @throws Exception
     */
    @Test
    public void nextConcurrently() throws Exception {
        doReturn(10).when(domain).getAcquireBlockSize();

        sequenceService.register(domain);

        List<Future<Long>> result = new ArrayList<>();

        int attempts = 2;
        for (int j = 0; j < attempts; j++) {
            CountDownLatch syncLatch = new CountDownLatch(poolSize);
            CountDownLatch setUpLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(poolSize);


            for (int i = 0; i < poolSize; i++) {
                Future<Long> fut = executorService.submit(() -> {
                    syncLatch.countDown();
                    setUpLatch.await();

                    long next = sequenceService.next(domain);

                    endLatch.countDown();

                    return next;
                });


                result.add(fut);
            }

            syncLatch.await();
            setUpLatch.countDown();
            endLatch.await();
        }
        List<Long> sequences = result.stream()
                                     .map(r -> Futures.getUnchecked(r))
                                     .collect(Collectors.toList());

        assertEquals(poolSize * attempts, result.size(), () -> {
            return sequences.toString();
        });

        LoggerFactory.getLogger(getClass()).debug("Sequences: {}", sequences);

        for (int i = 0; i < poolSize; i++) {
            for (int attempt = 1; attempt <= attempts; attempt++) {
                long expectedVal = (i * 10 + attempt);
                assertTrue(sequences.contains(expectedVal), () -> {
                    return String.format("Expected %d in %s", expectedVal, sequences);
                });
            }
        }
    }

}