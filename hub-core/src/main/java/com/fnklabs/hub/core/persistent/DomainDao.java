package com.fnklabs.hub.core.persistent;

import com.fnklabs.hub.core.Domain;

/**
 * Domain DAO
 *
 */
public interface DomainDao {
    /**
     * Save domain object
     *
     * @param name Domain name
     * @param id   Domain ID
     */
    Domain save(String name, int id, int acquireBlockSize, long maxValue);

    /**
     * Find domain object
     *
     * @param name Domain name
     *
     * @return Domain
     */
    Domain find(String name);
}
