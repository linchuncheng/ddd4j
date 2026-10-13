DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id VARCHAR(64) PRIMARY KEY,
    username VARCHAR(64),
    age INT,
    tenant_id VARCHAR(64),
    created_by VARCHAR(64),
    updated_by VARCHAR(64),
    create_time TIMESTAMP,
    update_time TIMESTAMP
);
