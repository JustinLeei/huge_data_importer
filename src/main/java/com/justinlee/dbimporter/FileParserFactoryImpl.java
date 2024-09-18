package com.justinlee.dbimporter;

import com.justinlee.dbimporter.datawriter.ColumnNameDTO;
import com.justinlee.dbimporter.datawriter.MysqlWriter;
import com.justinlee.dbimporter.parse.CSVParser;
import com.justinlee.dbimporter.parse.XLSXParser;
import com.justinlee.dbimporter.parse.ZipExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;


@Component
public class FileParserFactoryImpl implements FileParserFactory {

    @Autowired
    private CSVParser csvParser;

    @Autowired
    private XLSXParser xlsxParser;

    @Autowired
    private ZipExtractor zipExtractor;
    @Autowired
    private MysqlWriter mysqlWriter;
    @Override
    public FileParser getFileParser(File file) {
        String fileName = file.getName();
        if (fileName.toLowerCase().endsWith(".zip")) {
            return zipExtractor;
        } else if (fileName.toLowerCase().endsWith(".csv")) {
            return csvParser;
        } else if (fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls")) {
            return xlsxParser;
        } else {
            return null;
        }
    }

    @Override
    public List<ColumnNameDTO> getAllIdAndColName(String tableName) {
        return mysqlWriter.getAllIdAndColName(tableName);
    }

    @Override
    public String queryColumnName(Integer id, String tableName) {
        return mysqlWriter.queryColumnName(id,tableName);
    }

    @Override
    public Integer getIdByColName(String tableName, String colName) {
        return mysqlWriter.getIdByColName(tableName,colName);
    }
}