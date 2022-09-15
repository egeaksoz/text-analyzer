create sequence wordslist_id_seq start with 1;
create table wordslist ( 
	id bigint not null default nextval('wordslist_id_seq') primary key,
	word varchar(50) not null UNIQUE,
	count bigint
);
