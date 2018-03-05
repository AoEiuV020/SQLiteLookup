package com.example.sqlitelookup.model;

import io.sqliteutils.Table;
import io.sqliteutils.Table.Column;

@Table(name = "t_dbinfo", version = 1)
public class DbModel {

    @Column(name = "db_id", type = Column.TYPE_INTEGER, isPrimaryKey = true)
    public Integer dbId;

    @Column(name = "db_name", type = Column.TYPE_STRING, isNull = false)
    public String dbName;

    @Column(name = "db_path", type = Column.TYPE_STRING, isNull = false)
    public String dbPath;
}
