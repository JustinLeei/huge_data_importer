package com.justinlee.dbimporter.datawriter;





import com.justinlee.dbimporter.config.TableConfig;

import java.util.List;

public interface DataWriter {
    //写入数据表
    void batchInsertToDataTable(List<List<String>> data, String tableName, TableConfig tableConfig);
    //创建数据表
    void createDataTable(String sqlColumns, String tableName);
    //查询列名
    String queryColumnName(Integer id, String tableName);
    List<ColumnNameDTO> getAllIdAndColName(String tableName);
    //插入列名表
    void insertToColumnName(String tableName, List<ColumnNameDTO> data);
    //创建列名表
    void createColumnName(String kpiCounterTableName);

    }
