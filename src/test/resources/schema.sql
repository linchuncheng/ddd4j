DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(64),
    age INT,
    tenant_id BIGINT,
    create_time TIMESTAMP,
    update_time TIMESTAMP
);
