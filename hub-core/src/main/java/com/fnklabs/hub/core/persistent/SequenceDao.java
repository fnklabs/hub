package com.fnklabs.hub.core.persistent;


import com.fnklabs.hub.core.Sequence;

public interface SequenceDao {
    /**
     * Find actual sequence by domain
     *
     * @param domain Domain name
     *
     * @return Current sequence or null
     */
    Sequence find(int domain);

    /**
     * Save sequence
     *
     * @param domain Sequence name
     * @param value  current sequence value
     */
    void create(int domain, long value);

    /**
     * Update sequence value
     *
     * @param domain Domain name
     * @param value  Sequence value
     */
    void update(int domain, long value);
}