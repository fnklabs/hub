package com.fnklabs.hub.persistent.cassandra;

import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@Fork(
        value = 1,
        warmups = 1,
        jvmArgs = {
                "-server",
                "-Xms512m",
                "-Xmx1G",
                "-XX:NewSize=512m",
                "-XX:SurvivorRatio=6",
                "-XX:+AlwaysPreTouch",
                "-XX:+UseZGC",
                "-XX:+UseNUMA",
                "-XX:MaxGCPauseMillis=2000",
                "-XX:GCTimeRatio=4",
                "-XX:InitiatingHeapOccupancyPercent=30",
                "-XX:ConcGCThreads=8",
                "-XX:+UseTLAB",
                "-XX:+ScavengeBeforeFullGC",
                "-XX:+DisableExplicitGC",
        }
)
@Threads(4)
@Warmup(iterations = 1, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 1)
public abstract class AbstractBenchmark {
}
