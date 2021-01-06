package com.fnklabs.hub.core.persistent;

import com.fnklabs.hub.core.Source;

public interface SourceDao {
    Source find(String name);

    Source save(String name, int id);
}
