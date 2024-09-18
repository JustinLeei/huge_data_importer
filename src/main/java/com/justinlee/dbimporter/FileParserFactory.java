package com.justinlee.dbimporter;



import com.justinlee.dbimporter.datawriter.ColumnNameDTO;

import java.io.File;
import java.util.List;

public interface FileParserFactory {
    FileParser getFileParser(File file);
    List<ColumnNameDTO> getAllIdAndColName(String tableName);
    String queryColumnName(Integer id, String tableName);
    Integer getIdByColName(String tableName, String colName);
}
