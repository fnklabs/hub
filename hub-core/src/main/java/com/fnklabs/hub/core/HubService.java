package com.fnklabs.hub.core;

import java.util.Map;

/**
 * Api to generate surrogate keys.
 */
public interface HubService extends AutoCloseable {

    /**
     * Obtain a surrogate key from global atomic sequence using domain, source and source ID
     *
     * @param domain     Business logic domain name
     * @param sourceKeys Source keys where key - source name and value - source ID
     *
     * @return Surrogate ID
     */
    long getIdFor(String domain, Map<String, String> sourceKeys);

    /**
     * Register new hub domain
     *
     * @param name              Domain name
     * @param acquiredBlockSize Sequence acquire block size
     * @param maxValue          Sequence max value
     *
     * @return Domain entity
     */
    Domain register(String name, int acquiredBlockSize, long maxValue);

    /**
     * Update/Add already existing hub key with provided id
     *
     * @param domain   Hub domain name
     * @param source   Hub source name
     * @param sourceId Hub source system number
     * @param hubId    existing id
     */
    void insert(String domain, String source, String sourceId, long hubId);

    Domain find(String domain);

    /**
     * Register source in hub
     *
     * @param source Source name
     *
     * @return Source entity
     */
    Source registerSource(String source);
}
