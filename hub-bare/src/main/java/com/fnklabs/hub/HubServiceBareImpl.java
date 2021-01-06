package com.fnklabs.hub;

import com.fnklabs.hub.core.*;
import com.fnklabs.hub.core.persistent.DomainDao;
import com.fnklabs.hub.core.persistent.HubDao;
import com.fnklabs.hub.core.persistent.SourceDao;
import com.fnklabs.metrics.Metrics;
import com.fnklabs.metrics.MetricsFactory;
import com.fnklabs.metrics.Timer;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.StringUtils.lowerCase;

public class HubServiceBareImpl implements HubService {
    public static final String SYSTEM_SOURCE_SEQUENCE = "system.source_sequence";
    public static final String SYSTEM_DOMAIN_SEQUENCE = "system.domain_sequence";
    private static final Logger log = LoggerFactory.getLogger(HubServiceBareImpl.class);
    private static final Metrics METRICS = MetricsFactory.getMetrics();

    private final SequenceService sequenceService;

    private final HubDao hubDao;
    private final DomainDao domainDao;

    private final LoadingCache<HubKey, HubId> hubCache;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private final LoadingCache<String, Domain> domainCache;

    private final LoadingCache<String, Source> sourceCache;
    private final SourceDao sourceDao;


    /**
     * @param cacheSize       Hub cache size
     * @param cacheTtl        Cache TTL in sec
     * @param cleanUpTime     Hub cache clean up period in ms
     * @param sequenceService {@link SequenceService} instance
     * @param hubDao          {@link HubDao} instance
     * @param domainDao       {@link DomainDao} instance
     * @param sourceDao       {@link SourceDao} instance
     */
    public HubServiceBareImpl(int cacheSize, int cacheTtl, int cleanUpTime,
                              SequenceService sequenceService,
                              HubDao hubDao, DomainDao domainDao, SourceDao sourceDao) {

        this.sequenceService = sequenceService;
        this.hubDao = hubDao;
        this.domainDao = domainDao;
        this.sourceDao = sourceDao;

        domainCache = CacheBuilder.<String, Domain>newBuilder().maximumSize(Short.MAX_VALUE)
                                                               .build(new CacheLoader<String, Domain>() {
                                                                   @Override
                                                                   public Domain load(String domain) throws Exception {
                                                                       Domain entity = domainDao.find(domain);

                                                                       if (entity == null) {
                                                                           throw new DomainException(String.format("domain `%s` doesn't exists", domain));
                                                                       }
                                                                       return entity;
                                                                   }
                                                               });


        sourceCache = CacheBuilder.<String, Source>newBuilder().maximumSize(Short.MAX_VALUE)
                                                               .build(new CacheLoader<String, Source>() {
                                                                   @Override
                                                                   public Source load(String source) throws Exception {
                                                                       Source entry = sourceDao.find(source);
                                                                       if (entry == null) {
                                                                           throw new SourceException(String.format("source `%s` doesn't exists", source));
                                                                       }
                                                                       return entry;
                                                                   }
                                                               });

        hubCache = CacheBuilder.<HubKey, HubId>newBuilder().maximumSize(cacheSize)
                                                           .expireAfterAccess(cacheTtl, TimeUnit.SECONDS)
                                                           .build(
                                                                   new CacheLoader<HubKey, HubId>() {
                                                                       @Override
                                                                       public HubId load(HubKey key) throws Exception {
                                                                           long value = hubDao.find(key);

                                                                           return new HubId(key, value);
                                                                       }
                                                                   });

        if (cleanUpTime > 0) {
            scheduledExecutorService.scheduleWithFixedDelay(hubCache::cleanUp, 0, cleanUpTime, TimeUnit.MILLISECONDS);
        }
    }

    /** {@inheritDoc} */
    @Override
    public long getIdFor(String domain, Map<String, String> sourceKeys) {
        Timer timer = METRICS.getTimer("hub.id.translate");

        try {
            Domain domainObj = domainCache.get(domain);

            Collection<HubId> existingSurrogateKeys = getExistingSurrogateKeys(domainObj, sourceKeys);

            existingSurrogateKeys.forEach(hubId -> {
                hubId.writeLock().lock();
            });

            try {
                long existingSurrogateKey = 0;

                List<HubId> dirtyRecords = new ArrayList<>();

                for (HubId id : existingSurrogateKeys) {
                    if (id.getId() != 0 && existingSurrogateKey == 0) {
                        existingSurrogateKey = id.getId();
                    } else if (id.getId() == 0) {
                        dirtyRecords.add(id);
                    }
                }

                if (existingSurrogateKey == 0) {
                    existingSurrogateKey = getNextID(domainObj);
                }

                if (dirtyRecords.size() > 0) {
                    updateHubIds(dirtyRecords, existingSurrogateKey);
                }

                return existingSurrogateKey;
            } finally {
                existingSurrogateKeys.forEach(hubId -> {
                    hubId.writeLock().unlock();
                });
            }
        } catch (Exception e) {
            throw new HubException(e);
        } finally {
            timer.stop();
        }
    }

    @Override
    public Domain register(String name, int acquiredBlockSize, long maxValue) {
        Domain domain = domainDao.find(name);

        if (domain == null) {
            long domainId = StringUtils.equals(name, SYSTEM_DOMAIN_SEQUENCE) ? 1 : getNextDomainSequence();

            domain = domainDao.save(name, (int) domainId, acquiredBlockSize, maxValue);

            sequenceService.register(domain);
        }

        return domain;
    }

    @Override
    public void insert(String domain, String source, String sourceId, long hubId) {
        Domain domainObj = domainCache.getUnchecked(domain);
        HubKey hubKey = buildKey(domainObj, source, sourceId);

        Timer timer = METRICS.getTimer("hub.id.update");

        try {
            hubDao.save(hubKey, hubId);
        } finally {
            timer.stop();
        }
    }

    @Override
    public Domain find(String domain) {
        return domainCache.getUnchecked(domain);
    }

    @Override
    public Source registerSource(String source) {
        int nextSourceSequence = StringUtils.equals(source, SYSTEM_SOURCE_SEQUENCE) ? 1 : getNextSourceSequence();
        return sourceDao.save(source, nextSourceSequence);
    }

    @Override
    public void close() throws Exception {
        scheduledExecutorService.shutdown();

        scheduledExecutorService.awaitTermination(5, TimeUnit.MINUTES);
    }

    Collection<HubKey> buildKeys(Domain domain, Map<String, String> sourceKeys) {
        Timer timer = METRICS.getTimer("hub.id.keys.build");

        try {
            Set<HubKey> keys = new HashSet<>();

            sourceKeys.forEach((k, v) -> keys.add(buildKey(domain, k, v)));

            return keys;
        } finally {
            timer.stop();
        }


    }

    /**
     * Build hub key
     *
     * @param domain Hub domain
     * @param source Hub source
     * @param number Hub source number
     *
     * @return Existing hub key
     */
    private HubKey buildKey(Domain domain, String source, String number) {

        try {
            Source sourceImpl = sourceCache.get(source);

            int sourceId = sourceImpl.getId();

            return new HubKey(domain.getId(), sourceId, number);
        } catch (ExecutionException e) {
            throw new SourceException(String.format("source %s doesn't exists", source));
        }
    }

    private Collection<HubId> getExistingSurrogateKeys(Domain domain, Map<String, String> sourceKeys) {
        Timer timer = METRICS.getTimer("hub.id.keys.get_existing");

        try {
            Collection<HubKey> hubKeys = buildKeys(domain, sourceKeys);

            // order keys to permit deadlocks
            Set<HubId> keys = new TreeSet<>();

            for (HubKey hubKey : hubKeys) {
                keys.add(getExistingValue(hubKey));
            }

            return keys;
        } finally {
            timer.stop();
        }

    }

    @Nullable
    private HubId getExistingValue(HubKey hubKey) {
        Timer timer = METRICS.getTimer("hub.id.key.get_existing");

        try {
            return hubCache.getUnchecked(hubKey);
        } finally {
            timer.stop();
        }
    }

    void updateHubIds(Collection<HubId> keys, long id) {
        Timer timer = METRICS.getTimer("hub.id.update-all");

        try {
            keys.forEach(value -> {
                value.setId(id);
                updateHub(value);
            });
        } finally {
            timer.stop();
        }
    }

    private void updateHub(HubId hubId) {
        Timer timer = METRICS.getTimer("hub.id.update");

        try {
            hubDao.save(hubId.getHubKey(), hubId.getId());
            hubCache.put(hubId.getHubKey(), hubId);
        } finally {
            timer.stop();
        }
    }

    private int getNextDomainSequence() {
        Domain seq = domainCache.getUnchecked(SYSTEM_DOMAIN_SEQUENCE);

        return (int) getNextID(seq);
    }

    private int getNextSourceSequence() {
        Domain sourceSequence = domainCache.getUnchecked(SYSTEM_SOURCE_SEQUENCE);

        return (int) getNextID(sourceSequence);
    }

    long getNextID(Domain domain) {
        return sequenceService.next(domain);
    }


}
