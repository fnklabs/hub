package com.fnklabs.hub;

import com.fnklabs.hub.core.HubKey;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HubId implements Comparable<HubId> {
    private final HubKey hubKey;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private long id;

    public HubId(HubKey hubKey, long id) {
        this.hubKey = hubKey;
        this.id = id;
    }

    public HubKey getHubKey() {
        return hubKey;
    }

    public long getId() {
        return id;
    }

    public HubId setId(long id) {
        this.id = id;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hubKey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HubId hubId = (HubId) o;
        return hubKey.equals(hubId.hubKey);
    }

    @Override
    public int compareTo(@NotNull HubId o) {
        return hubKey.compareTo(o.hubKey);
    }

    ReentrantReadWriteLock.WriteLock writeLock() {
        return lock.writeLock();
    }

    ReentrantReadWriteLock.ReadLock readLock() {
        return lock.readLock();
    }
}
