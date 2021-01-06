package com.fnklabs.hub.core;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * Hub Key for ID
 */
public final class HubKey implements Serializable, Comparable<HubKey> {
    private final int domain;
    private final int source;
    private final String sourceID;

    public HubKey(int domain, int source, String sourceID) {
        this.domain = domain;
        this.source = source;
        this.sourceID = sourceID;
    }

    public int getDomain() {
        return domain;
    }

    public int getSource() {
        return source;
    }

    public String getSourceID() {
        return sourceID;
    }

    @Override
    public int compareTo(HubKey key) {
        int domainCompare = Integer.compare(domain, key.getDomain());

        if (domainCompare != 0) {
            return domainCompare;
        }

        int sourceCompare = Integer.compare(source, key.getSource());

        if (sourceCompare != 0) {
            return sourceCompare;
        }

        return StringUtils.compare(sourceID, key.getSourceID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, source, sourceID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HubKey hubKey = (HubKey) o;
        return domain == hubKey.domain &&
                source == hubKey.source &&
                sourceID.equals(hubKey.sourceID);
    }
}
