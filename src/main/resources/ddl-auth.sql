create table routes (
	route_id uuid default gen_random_uuid() not null,
	"domain" varchar(255) null,
	uri varchar(255) null,
	created_at timestamp null,
	created_by varchar(255) null,
	updated_at timestamp null,
	updated_by varchar(255) null,
	deleted_at timestamp null,
	deleted_by varchar(255) null,
	constraint pk_routes primary key (route_id)
);

create table users (
	user_id uuid default gen_random_uuid() not null,
	first_name varchar(255) not null,
	last_name varchar(255) null,
	email varchar(255) not null,
	"password" varchar(255) not null,
	"role" varchar(255) not null,
	created_at timestamp null,
	created_by varchar(255) null,
	updated_at timestamp null,
	updated_by varchar(255) null,
	deleted_at timestamp null,
	deleted_by varchar(255) null,
	constraint pk_users primary key (user_id)
);


create table user_activities (
	user_activities_id varchar(255) default gen_random_uuid() not null,
	is_valid bool not null,
	user_id varchar(255) not null,
	"type" varchar(255) null,
	platform varchar(255) null,
	created_at timestamp null,
	created_by varchar(255) null,
	updated_at timestamp null,
	updated_by varchar(255) null,
	deleted_at timestamp null,
	deleted_by varchar(255) null,
	constraint pk_user_activities primary key (user_activities_id)
);
