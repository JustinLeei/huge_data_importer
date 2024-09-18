package com.justinlee.dbimporter;

import lombok.Data;

@Data
public class ImportConfig {
    // 数据表的前缀
    private String dataTablePrefix;
    // 列名表的前缀
    private String colNameTablePrefix;
    // 是否创建数据表
    private Boolean isCreateDataTable;
    // 是否创建列名表
    private Boolean isCreateColumnTable;
}