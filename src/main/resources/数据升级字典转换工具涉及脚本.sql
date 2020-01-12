--创建要导入的表配置表
create table DATA_EXTRACTION
(
  ID  VARCHAR2(32) not null
    primary key,
  TABLENAME VARCHAR2(32)
);
comment on table DATA_EXTRACTION is '要抽取数据的表名配置';
comment on column DATA_EXTRACTION.id is '主键';
comment on column DATA_EXTRACTION.TABLENAME is '要抽取数据的表名';
--创建表-字段字典对应表
create table table_field(
  id number primary key not null ,
  table_name varchar2(200),
  field_name varchar2(200),
  dic_name varchar2(200)
);
comment on table table_field is '表中是字典的字段及字典名配置';
comment on column table_field.id is '主键';
comment on column table_field.table_name is '表名';
comment on column table_field.field_name is '字段名';
comment on column table_field.dic_name is '字典名';

--创建新老字典值对应表
create table dic_dic(
  id number primary key not null ,
  dic_name varchar2(200),
  old_value varchar2(200),
  new_value varchar2(200)
);
comment on table dic_dic is '新老字典对应表';
comment on column dic_dic.id is '主键';
comment on column dic_dic.dic_name is '字典名';
comment on column dic_dic.old_value is '老版本值';
comment on column dic_dic.new_value is '新版本值';

--创建测试表1--目标库
create table tast_table(
  name varchar2(200),
  sexCode varchar2(200)
);
comment on table tast_table is '测试表';
comment on column tast_table.name is '姓名';
comment on column tast_table.sexCode is '性别';
--创建测试表另一个库 源库
create table tast_table(
  name varchar2(200),
  sexCode varchar2(200)
);
comment on table tast_table is '对拉库测试表';
comment on column tast_table.name is '姓名';
comment on column tast_table.sexCode is '性别';

insert into table_field (id, table_name, field_name, dic_name)
values (1,'tast_table','sexCode','sexCode');
insert into dic_dic (id, dic_name, old_value, new_value)
values (1,'sexCode','1','男');
insert into dic_dic (id, dic_name, old_value, new_value)
values (2,'sexCode','2','女');
--插入源库数据
insert into tast_table (name, sexCode)
values ('丁海锋','2');
delete DATA_EXTRACTION;
insert into DATA_EXTRACTION (ID, TABLENAME)
values (1,'tast_table');
