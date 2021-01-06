CREATE KEYSPACE hub WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};

use hub;

CREATE TABLE domain
(
    name               text PRIMARY KEY,
    domain_id          int,
    acquire_block_size int,
    max_value          bigint,
);

CREATE TABLE source
(
    name      text PRIMARY KEY,
    source_id int
);


CREATE TABLE sequence
(
    domain_id int PRIMARY KEY,
    value     bigint
);

CREATE TABLE hub
(
    domain_id     int,
    source_id     int,
    system_number text,
    hub_id        bigint,
    PRIMARY KEY ((domain_id, source_id, system_number))
);


insert into domain (name, domain_id, acquire_block_size, max_value) values ('system.source_sequence', 1,1,2147483647);
insert into domain (name, domain_id, acquire_block_size, max_value) values ('system.domain_sequence',2,1,2147483647);

insert into sequence (domain_id, value) VALUES (1,1);
insert into sequence (domain_id, value) VALUES (2,1);
