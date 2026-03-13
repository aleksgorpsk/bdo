
create schema etl;
CREATE SEQUENCE etl.etl
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;

CREATE TABLE etl.etl (
	id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
	"name" text NULL,
	CONSTRAINT etl_pk PRIMARY KEY (id)
);


 create table etl.etl (
        id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
        active boolean,
        comment varchar(255),
        cron_scheduling varchar(255),
        interval integer,
        name varchar(255),
        primary key (id)
    )
