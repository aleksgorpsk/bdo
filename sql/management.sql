CREATE TABLE etl.step_instance (
	step_instance_id varchar(40) NOT NULL,
	parentStepInstanceIds text[] null,
	status int4 null,
	etl_instance_id numeric(38) not null,
	step_id numeric(38) not null,
	etl_id numeric(38) not null,
	log Text null,
	vars Text null,
	attempts int4,
	start timestamp(6) null,
	stop timestamp(6) null,

	CONSTRAINT step_instance_pkey PRIMARY KEY (step_instance_id)
);

ALTER TABLE etl.step_instance ADD CONSTRAINT ctn1 FOREIGN KEY (etl_instance_id) REFERENCES etl.etl_instance(etl_instance_id);
ALTER TABLE etl.step_instance ADD CONSTRAINT ctn2 FOREIGN KEY (step_id) REFERENCES etl.step(step_id);
ALTER TABLE etl.step_instance ADD CONSTRAINT ctn3 FOREIGN KEY (etl_id) REFERENCES etl.etl(id);