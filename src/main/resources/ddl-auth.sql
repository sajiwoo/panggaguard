create table routes (
  route_id uuid default gen_random_uuid () not null,
  "domain" varchar(255) null,
  uri varchar(255) null,
  created_at timestamp not null default current_timestamp,
  created_by varchar(255) null,
  updated_at timestamp null,
  updated_by varchar(255) null,
  deleted_at timestamp null,
  deleted_by varchar(255) null,
  constraint pk_routes primary key (route_id)
);

create table users (
  user_id uuid default gen_random_uuid () not null,
  first_name varchar(255) not null,
  last_name varchar(255) null,
  email varchar(255) not null,
  "password" varchar(255) not null,
  "role" varchar(255) not null,
  created_at timestamp not null default current_timestamp,
  created_by varchar(255) null,
  updated_at timestamp null,
  updated_by varchar(255) null,
  deleted_at timestamp null,
  deleted_by varchar(255) null,
  constraint pk_users primary key (user_id)
);

create table user_activities (
  user_activities_id varchar(255) default gen_random_uuid () not null,
  is_valid bool not null,
  user_id varchar(255) not null,
  "type" varchar(255) null,
  platform varchar(255) null,
  created_at timestamp not null default current_timestamp,
  created_by varchar(255) null,
  updated_at timestamp null,
  updated_by varchar(255) null,
  deleted_at timestamp null,
  deleted_by varchar(255) null,
  constraint pk_user_activities primary key (user_activities_id)
);

create table mailing (
  mailing_id varchar(55) not null unique,
  subject varchar(55) not null,
  sender varchar(55) not null,
  message text not null,
  created_at timestamp not null default current_timestamp,
  created_by varchar(255) null,
  updated_at timestamp null,
  updated_by varchar(255) null,
  deleted_at timestamp null,
  deleted_by varchar(255) null,
  constraint pk_mailing primary key (mailing_id)
);

create table otp (
  token varchar(55) not null unique,
  user_id varchar(55) not null,
  reference varchar(55),
  is_valid boolean not null default true,
  created_at timestamp not null default current_timestamp,
  created_by varchar(255) null,
  updated_at timestamp null,
  updated_by varchar(255) null,
  deleted_at timestamp null,
  deleted_by varchar(255) null,
  constraint pk_otp primary key (token)
);

insert into
  mailing (mailing_id, subject, sender, message)
values
  (
    'FORGOT_PASSWORD',
    'Your Password Reset Link',
    'noreply@panggaguard.dev',
    'Hello,

You have requested to reset your password for the account associated with {{EMAIL}}.

Please click the link below to reset your password:
http://localhost:3000/reset-password?email={{EMAIL}}&token={{OTP}}

This link will expire in 15 minutes. 
If you did not request this password reset, please ignore this email.

Regards,
Panggaguard Team'
  );
