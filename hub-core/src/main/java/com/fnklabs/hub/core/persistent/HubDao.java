package com.fnklabs.hub.core.persistent;

import com.fnklabs.hub.core.HubKey;

/**
 * Hub DAO
 */
public interface HubDao {
    /**
     * Find Hub ID by {@link HubKey}
     *
     * @param hubKey Hub key
     *
     * @return Hub ID or {@code 0} if hub id not found
     */
    long find(HubKey hubKey);

    /**
     * Save {@link HubKey} id
     *
     * @param hubKey {@link HubKey} instance
     * @param id     Hub ID
     */
    void save(HubKey hubKey, long id);
}
