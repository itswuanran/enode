CREATE TABLE event_stream (
  id bigserial,
  aggregate_root_type_name varchar(256),
  aggregate_root_id varchar(64),
  version integer,
  command_id varchar(64),
  events text,
  create_at bigint,
  PRIMARY KEY (id),
  CONSTRAINT uk_aggregate_root_id_version UNIQUE (aggregate_root_id, version),
  CONSTRAINT uk_aggregate_root_id_command_id UNIQUE (aggregate_root_id, command_id)
);

CREATE TABLE published_version (
  id bigserial,
  processor_name varchar(128),
  aggregate_root_type_name varchar(256),
  aggregate_root_id varchar(64),
  version integer,
  create_at bigint,
  update_at bigint,
  PRIMARY KEY (id),
  CONSTRAINT uk_aggregate_root_id_version_processor_name UNIQUE (aggregate_root_id, version, processor_name)
);
