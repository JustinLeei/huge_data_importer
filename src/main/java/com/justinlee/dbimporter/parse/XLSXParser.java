package com.justinlee.dbimporter.parse;

import com.justinlee.dbimporter.FileParser;
import com.justinlee.dbimporter.ImportConfig;
import com.justinlee.dbimporter.ReadListenerProperties;
import com.justinlee.dbimporter.XlsxReadListener;
import com.justinlee.dbimporter.config.TableConfig;
import com.justinlee.dbimporter.datawriter.MysqlWriter;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@Component
public class XLSXParser implements FileParser {
    @Autowired
    private MysqlWriter mysqlWriter;
    @Autowired
    private ExecutorService hdiExecutorService;
    @Autowired
    private  TableConfig tableConfig;

    @Override
    public void parse(File file, ImportConfig importConfig) {
        ReadListenerProperties readListenerProperties = ReadListenerProperties.builder()
                .importConfig(importConfig)
                .executorService(hdiExecutorService)
                .dataWriter(mysqlWriter)
                .build();
        //生成listener的形式
        XlsxReadListener xlsxReadListener = new XlsxReadListener(readListenerProperties,tableConfig);
        //行数据的形式
        Map<String, String> oneRowData = new HashMap<>();
        ExcelReader excelReader0 = EasyExcel.read(file, oneRowData.getClass().getDeclaringClass(), xlsxReadListener).build();
        // 构建一个sheet 这里可以指定名字或者no
        ReadSheet readSheet0 = EasyExcel.readSheet(0).build();
        // 读取一个sheet
        excelReader0.read(readSheet0);

    }
}
