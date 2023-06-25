CREATE DATABASE IF NOT EXISTS enode;
USE enode;
CREATE TABLE IF NOT EXISTS event_stream (
  id BIGINT AUTO_INCREMENT NOT NULL,
  aggregate_root_type_name VARCHAR(256) NOT NULL,
  aggregate_root_id VARCHAR(64) NOT NULL,
  version INT NOT NULL,
  command_id VARCHAR(64) NOT NULL,
  events MEDIUMTEXT NOT NULL,
  create_at BIGINT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_aggregate_root_id_version (aggregate_root_id, version),
  UNIQUE KEY uk_aggregate_root_id_command_id (aggregate_root_id, command_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS published_version (
  id BIGINT AUTO_INCREMENT NOT NULL,
  processor_name VARCHAR(128) NOT NULL,
  aggregate_root_type_name VARCHAR(256) NOT NULL,
  aggregate_root_id VARCHAR(64) NOT NULL,
  version INT NOT NULL,
  create_at BIGINT NOT NULL,
  update_at BIGINT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_aggregate_root_id_version_processor_name (aggregate_root_id, version, processor_name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
