# Hub

Service for providing sequence keys for DWH.

## Contents

* [Terms](#terms)
* [High-Level overview](#high-level-overview)
    * [Examples](#example)
    * [Flow](#flow) 
* [Build and install](#build-and-install)
* [Requirement](#requirements)

## Terms

* Domain - Business model name
* Source - Object's source name
* Source ID - Source object ID number

## High-Level overview 

DWH consists of multiple data sources for domain object. Domain object in each source can contain a unique identifier. 
As a result, the problem arises that with the growth of sources and the complexity of finding or joining objects from 
different sources increases.

This service helps to organize the creation and storage of ID for a domain objects from several sources.

### Example

We have domain object `UserProfile`

`UserProfile` could be loaded from several sources
 
* DMP system
* Feedback system
* Transaction system

| Domain | Source | Source ID | 
|----|---|---|
| UserProfile | DMP System | DMP-01 |
| UserProfile | Feedback System | F-ABC |
| UserProfile | Transaction System | 999 |


For each of this sources we have sequential ETL job that load data from source and must return same ID for unique `UserProfile`

| Domain | Source | Source ID | Hub ID |
|---|---|---|---|
| UserProfile | DMP System | DMP-01 | 1 |
| UserProfile | Feedback System | F-ABC | 1 |
| UserProfile | Transaction System | 999 | 1 |


### Flow

* ETL Job 1 load data from `DMP System`
    * ETL Job send request to HUB
    * Hub retrieve request ```getIdFor( [{"UserProfile", "DMP System", "DMP-01"}])``` 
    * Hub can't find existing ID or related object for provided key and create new ID = `1` 
    * Hub return result `[{{"UserProfile", "DMP System", "DMP-01"} =  1}]` to `ETL Job 1`
    * ETL Job put id to result
* ETL Job 2 load data from `Feedback System`
    * ETL Job send request to HUB
    * Hub retrieve request ```getIdFor( [{"UserProfile", "DMP System", "DMP-01"}, {"UserProfile", "Feedback System", "F-ABC" }])``` 
    * Hub found existing ID = ```1``` for key ```{"UserProfile", "DMP System", "DMP-01"}```
    * Hub can't find existing ID for provided key ```{"UserProfile", "Feedback System", "F-ABC" }```
        * Hub link key ```{"UserProfile", "Feedback System", "F-ABC" }``` with existing ID ```1``` 
    * Hub return result `[{{"UserProfile", "DMP System", "DMP-01"} =  1}, {"UserProfile", "Feedback System", "F-ABC" } = 1]` to `ETL Job 2`
    * ETL Job put id to result
* ETL Job 3 load data from `Transaction System`
    * ETL Job send request to HUB
    * Hub retrieve request ```getIdFor( [{"UserProfile", "DMP System", "DMP-01"}, {"UserProfile", "Feedback System", "F-ABC" }, {"UserProfile", "Transaction System", "999"}])``` 
    * Hub found existing ID = ```1``` for key ```{"UserProfile", "DMP System", "DMP-01"}```
    * Hub find existing ID = ```1``` for provided key ```{"UserProfile", "Feedback System", "F-ABC" }```
    * Hub can't find existing ID for provided key ```{"UserProfile", "Transaction System", "999"}```
        * Hub link key ```{"UserProfile", "Transaction System", "999"}``` with existing ID ```1``` 
    * Hub return result `[{{"UserProfile", "DMP System", "DMP-01"} =  1}, {{"UserProfile", "Feedback System", "F-ABC" } = 1}, {{"UserProfile", "Transaction System", "999"} = 1}]` to `ETL Job 3`
    * ETL Job put id to result

## Build and Install

### Build
```
./gradlew build
```

### Install

```
compile "com.fnklabs.hub:hub-core:$vers.hub"
```

## Usage

```
compile "com.fnklabs.hub:hub-core:$vers.hub"
```

```
CassandraFactory cassandraFactory = new CassandraFactory(
        System.getProperty("cassandra.hosts", "127.0.0.1").split(","),
        "hub",
        null,
        null,
        ConsistencyLevel.QUORUM.name(),
        null,
        10_000,
        10_000,
        8,
        8
);
HubService hubService = new HubServiceBareImpl(10_000, 60_000, 60_000,
                                    new SequenceServiceImpl(new SequenceDaoImpl(cassandraFactory)),
                                    new HubDaoImpl(cassandraFactory),
                                    new DomainDaoImpl(cassandraFactory),
                                    new SourceDaoImpl(cassandraFactory)
);

hubService.register(HubServiceBareImpl.SYSTEM_DOMAIN_SEQUENCE, 1, Integer.MAX_VALUE);
hubService.register(HubServiceBareImpl.SYSTEM_SOURCE_SEQUENCE, 1, Integer.MAX_VALUE);

hubService.register("domain", 10_000, Long.MAX_VALUE);

hubService.registerSource("source");

long id = hubService.getIdFor("domain", ImmutableMap.of("source", "ABC"));
```

## Requirements:
```
Oracle JDK 1.15
Apache Cassandra 3.0+
```

