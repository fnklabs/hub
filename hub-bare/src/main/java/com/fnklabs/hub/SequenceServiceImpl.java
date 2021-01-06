package com.fnklabs.hub;

import com.fnklabs.hub.core.Domain;
import com.fnklabs.hub.core.Sequence;
import com.fnklabs.hub.core.SequenceLimitsExceeded;
import com.fnklabs.hub.core.SequenceService;
import com.fnklabs.hub.core.persistent.SequenceDao;
import com.fnklabs.metrics.Metrics;
import com.fnklabs.metrics.MetricsFactory;
import com.fnklabs.metrics.Timer;
import com.google.common.base.Function;
import com.google.common.base.Verify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SequenceServiceImpl implements SequenceService {
    private static final Metrics METRICS = MetricsFactory.getMetrics();
    private static final Logger log = LoggerFactory.getLogger(SequenceServiceImpl.class);

    private final SequenceDao sequenceDao;

    private final ThreadLocal<Map<Domain, SequenceThreadLocal>> localSequences;

    private final ReentrantReadWriteLock readLock = new ReentrantReadWriteLock();

    /**
     * @param sequenceDao {@link SequenceDao} instance
     */
    public SequenceServiceImpl(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;

        localSequences = ThreadLocal.withInitial(HashMap::new);
    }

    @Override
    public void register(Domain domain) {
        tryLockAndExecute(lock -> {
            sequenceDao.create(domain.getId(), 0L);

            return null;
        });
    }

    @Override
    public void changeSequence(Domain domain, long newValue) {
        tryLockAndExecute(lock -> {
            sequenceDao.update(domain.getId(), newValue);

            return true;
        });
    }

    @Override
    public long next(Domain domain) {
        Timer timer = METRICS.getTimer("sequence.next");

        try {

            for (; ; ) {
                Map<Domain, SequenceThreadLocal> sequenceCache = localSequences.get();

                SequenceThreadLocal sequence = sequenceCache.computeIfAbsent(domain, k -> {
                    return acquireNewBlock(domain);
                });

                try {
                    return sequence.next();
                } catch (SequenceLimitsExceeded e) {
                    log.debug("sequence limits exceeded. try to acquire again", e);

                    sequenceCache.remove(domain);
                }
            }

        } finally {
            timer.stop();
        }
    }

    private SequenceThreadLocal acquireNewBlock(Domain domain) {
        Timer timer = METRICS.getTimer("sequence-service.next.acquire-block");

        SequenceThreadLocal seq = tryLockAndExecute(lock -> {

            Sequence prevValue = sequenceDao.find(domain.getId());

            Objects.requireNonNull(prevValue, () -> {
                return String.format("unknown sequence domain: `%s`", domain);
            });

            long newValue = prevValue.getValue() + domain.getAcquireBlockSize();

            if (newValue > domain.getMaxValue()) {
                throw new SequenceLimitsExceeded(String.format("sequence overflow %d/%d (currentValue/maxValue), can't acquire new block", newValue, domain.getMaxValue()));
            }

            sequenceDao.update(domain.getId(), newValue);

            log.debug("acquire new block {} ({},{}]", domain, prevValue.getValue(), newValue);

            return new SequenceThreadLocal(domain.getId(), prevValue.getValue(), newValue);
        });

        timer.stop();

        return seq;
    }

    private <O> O tryLockAndExecute(Function<Lock, O> func) {
        ReentrantReadWriteLock.WriteLock writeLock = readLock.writeLock();

        try {
            writeLock.lock();

            return func.apply(writeLock);
        } finally {
            writeLock.unlock();
        }
    }
}
