package com.fnklabs.hub.persistent.cassandra;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.*;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Serializable;

public class CassandraFactory implements AutoCloseable {
    private static transient Session session;

    private final String[] hosts;

    private final String keyspace;
    private final String username;
    private final String password;
    private final ConsistencyLevel consistencyLevel;
    private final String localDc;
    private final int readTimeout;
    private final int connectionTimeout;
    private final int connectionsPerHostCore;
    private final int connectionsPerHostMax;
    private final Cluster cluster;

    public CassandraFactory(String[] hosts,
                            String keyspace,
                            @Nullable String username,
                            @Nullable String password,
                            String consistencyLevel,
                            @Nullable String localDc,
                            int readTimeout,
                            int connectionTimeout,
                            int connectionsPerHostCore,
                            int connectionsPerHostMax
    ) {
        this.hosts = hosts;
        this.keyspace = keyspace;
        this.username = username;
        this.password = password;
        this.consistencyLevel = ConsistencyLevel.valueOf(consistencyLevel);
        this.localDc = localDc;
        this.readTimeout = readTimeout;
        this.connectionTimeout = connectionTimeout;
        this.connectionsPerHostCore = connectionsPerHostCore;
        this.connectionsPerHostMax = connectionsPerHostMax;

        Cluster.Builder builder = Cluster.builder();

        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            builder.withAuthProvider(new PlainTextAuthProvider(username, password));
        }


        cluster = builder.addContactPoints(hosts)
                         .withTimestampGenerator(new AtomicMonotonicTimestampGenerator())
                         .withRetryPolicy(new LoggingRetryPolicy(DefaultRetryPolicy.INSTANCE))
                         .withLoadBalancingPolicy(getLoadBalancingPolicy())
                         .withQueryOptions(new QueryOptions() {{
                             setConsistencyLevel(ConsistencyLevel.valueOf(consistencyLevel));
                         }})
                         .withSocketOptions(new SocketOptions() {{
                             setReadTimeoutMillis(readTimeout);
                             setConnectTimeoutMillis(connectionTimeout);
                         }})
                         .withPoolingOptions(new PoolingOptions() {{
                             setConnectionsPerHost(HostDistance.LOCAL, connectionsPerHostCore, connectionsPerHostMax);
                             setConnectionsPerHost(HostDistance.REMOTE, connectionsPerHostCore, connectionsPerHostMax);
                         }})
                         .withoutMetrics()
                         .withoutJMXReporting()
                         .build();

        session = cluster.connect(keyspace);
    }

    @Override
    public void close() throws IOException {
        session.close();
        cluster.close();
    }


    public Session getSession() {
        return session;
    }

    private LoadBalancingPolicy getLoadBalancingPolicy() {

        if (StringUtils.isEmpty(localDc)) {
            return new TokenAwarePolicy(new RoundRobinPolicy(), TokenAwarePolicy.ReplicaOrdering.TOPOLOGICAL);
        }

        return new TokenAwarePolicy(
                new DCAwareRoundRobinPolicy.Builder()
                        .withLocalDc(localDc)
                        .build(),
                TokenAwarePolicy.ReplicaOrdering.TOPOLOGICAL
        );
    }
}
