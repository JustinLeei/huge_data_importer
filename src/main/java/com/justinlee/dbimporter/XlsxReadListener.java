package com.justinlee.dbimporter;


import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.justinlee.dbimporter.config.TableConfig;
import com.justinlee.dbimporter.constant.DataImportConst;
import com.justinlee.dbimporter.datawriter.ColumnNameDTO;
import com.justinlee.dbimporter.datawriter.DataWriter;
import com.justinlee.dbimporter.exception.CellProcessExpection;
import com.justinlee.dbimporter.exception.CreateDataTableExpection;
import com.justinlee.dbimporter.exception.WriteMysqlExpection;
import com.justinlee.dbimporter.util.CellUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;
/**
 * 构造器中传入的是用户和开发者传入的参数
 * @para readListenerProperties 用户传入的参数
 * @para tableConfig 开发者传入的参数
 * ***/
@Slf4j
public class XlsxReadListener implements ReadListener<Map<Integer,String>>{
    private static final int BATCH_COUNT = 10000;
    private  List<List<String>> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    private ReadListenerProperties readListenerProperties;
    private final TableConfig tableConfig;
    private CellUtil cellUtil;
    private List<Integer> cachedIdList = new ArrayList<>();

    public XlsxReadListener(ReadListenerProperties readListenerProperties,TableConfig tableConfig) {
        this.readListenerProperties = readListenerProperties;
        this.tableConfig = tableConfig;
    }

    @Override
    public void invoke(Map<Integer,String> map, AnalysisContext context) {

        int currentRow = context.readRowHolder().getRowIndex();
        //循环处理每一个单元格
        List<String> collect = map.values().stream().collect(Collectors.toList());
        ArrayList<String> strings = new ArrayList<>(collect);
        try {
            cellUtil.processAndNormalizeListValues(strings,context,cachedIdList);
        } catch (Exception e) {
            log.error(currentRow + "行处理单元格数据异常,异常数据为:" + map.toString(), e);
            throw new CellProcessExpection(currentRow + "行处理单元格数据异常,异常数据为:" + map.toString(), e);
        }
        // 缓存起来
        cachedDataList.add(strings);

        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (cachedDataList.size() >= BATCH_COUNT) {
            try {
                //保存到数据库
                saveData();
                // 存储完成清理 list
                cachedDataList.clear();
            } catch (Exception e) {
                //抛出的是非受检异常，最外层可以捕获到
                log.error("写入数据库异常,输入前缀为：" + readListenerProperties.getImportConfig().getDataTablePrefix(), e);
                throw new WriteMysqlExpection("写入数据库异常," + readListenerProperties.getImportConfig().getDataTablePrefix(), e);
            }
        }

    }
    @Override
    public  void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
            List<String> originColNameList = new ArrayList<>();
            //获取excel的表头，也就是kpi name
            for (ReadCellData<?> value : headMap.values()) {
                String originColName = value.getStringValue();
                originColNameList.add(originColName);
            }
            log.error("列头信息为:{}",originColNameList.toString());
            //统一非数值的列头
            normalizeColumnNames(originColNameList,tableConfig);
            String dataTablePrefix = readListenerProperties.getImportConfig().getDataTablePrefix();
            String colNameTablePrefix = readListenerProperties.getImportConfig().getColNameTablePrefix();
            try {
                //创建非数值列列名Table
                readListenerProperties.getDataWriter().createColumnName(
                        colNameTablePrefix
                                + DataImportConst.COLNAME_TABLE_SUFFIX);
                //生成字段和类型的字符串
                String sqlColumns = buildSql(originColNameList);
                //创建原始数据表
                readListenerProperties.getDataWriter().createDataTable(sqlColumns,
                        dataTablePrefix+ DataImportConst.DATA_TABLE_SUFFIX);
            } catch (Exception e) {
                throw new CreateDataTableExpection("创建数据表出错！",e);
            }

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

        try {
            // 这里也要保存数据，确保最后遗留的数据也存储到数据库
            if (!cachedDataList.isEmpty()){
                if (cachedDataList.size()>0) {
                    log.info("{}条数据，开始存储数据库！", cachedDataList.size());
                    saveData();
                }
                log.info("所有数据解析完成！");
                cachedDataList.clear();
                cachedIdList.clear();
                readListenerProperties.getExecutorService().shutdown();
            }
        } catch (Exception e) {
            readListenerProperties.getExecutorService().shutdown();
            log.error("写入数据库异常",e);
            throw new WriteMysqlExpection("写入数据库异常",e);
        }
    }

    /**
     * 加上存储数据库
     */
    private  void saveData(){
        if (cachedDataList.size() > 0) {
            log.info("{}条数据，开始存储数据库！", cachedDataList.size());
            DataWriter dataWriter = readListenerProperties.getDataWriter();
            String dataTablePrefix = readListenerProperties.getImportConfig().getDataTablePrefix();

            dataWriter.batchInsertToDataTable(cachedDataList,
                    dataTablePrefix + DataImportConst.DATA_TABLE_SUFFIX,tableConfig);
            log.info("存储数据库成功！");

        }
    }
    /***
     * 将非数值的列统一列头
     * ***/
    public void normalizeColumnNames(List<String> originalColNameList,TableConfig tableConfig) {
        Map<String, String> nonTimeColumnMapping = tableConfig.getNonTimeColumnMapping();
        Map<String, String> timeColumnMapping = tableConfig.getTimeColumnMapping();
        for (int i = 0; i < originalColNameList.size(); i++) {
            String mappingNonTimeColumn = nonTimeColumnMapping.get(originalColNameList.get(i));
            String mappingTimeColumn = timeColumnMapping.get(originalColNameList.get(i));
            if (mappingTimeColumn != null || mappingNonTimeColumn != null){
                originalColNameList.set(i,mappingTimeColumn != null ? mappingTimeColumn : mappingNonTimeColumn);
            }
        }
    }
    private String buildSql(List<String> originColNameList) {
        StringBuilder creatSql = new StringBuilder();
        String buildSqlColName = null;
        String colNameTablePrefix = readListenerProperties.getImportConfig().getColNameTablePrefix();
        HashMap<Integer, String> buildSqlColNameMap = new HashMap<>(1000);
        //替换列名
        for (int i = 0; i < originColNameList.size(); i++) {
            Map<String, String> nonTimeColumnMapping = tableConfig.getNonTimeColumnMapping();
            Map<String, String> timeColumnMapping = tableConfig.getTimeColumnMapping();
            if (nonTimeColumnMapping.containsValue(originColNameList.get(i))) {
                //非数值类型的列名使用开发者配置
                buildSqlColNameMap.put(i,originColNameList.get(i));
            }else if (timeColumnMapping != null && timeColumnMapping.containsValue(originColNameList.get(i))){
                buildSqlColNameMap.put(i,originColNameList.get(i));
            } else {
                //数值类型的列
                buildSqlColName = replaceOriginalColumnNameWithNumeric(originColNameList, colNameTablePrefix, i);
                buildSqlColNameMap.put(i,buildSqlColName);
                cachedIdList.add(i);
            }
        }
        //生成sql
        for (int i = 0; i < originColNameList.size(); i++) {
            //生成mysql的ddl的字段和字段类型
            creatSql.append("`").append(buildSqlColNameMap.get(i));
            buildSqlColNameMap.get(i);
            Map<String, String> nonTimeColumnMapping = tableConfig.getNonTimeColumnMapping();
            Map<String, String> timeColumnMapping = tableConfig.getTimeColumnMapping();
            if (isStringInMapValues(buildSqlColNameMap.get(i),timeColumnMapping)) {
                //时间列
                creatSql.append("` DATETIME,");
            } else if (isStringInMapValues(buildSqlColNameMap.get(i),nonTimeColumnMapping)) {
                //非时间列
                creatSql.append("` TEXT,");
            }else {
                //以下这些情况都认为是数值类型
                creatSql.append("` DOUBLE,");
            }
        }
        return creatSql.toString();
    }

    private String replaceOriginalColumnNameWithNumeric(List<String> originColNameList, String colNameTablePrefix, int colIndex) {
        String buildSqlColName;
        if (originColNameList.get(colIndex) != null){
            //把表头信息记录到数据库中
            ColumnNameDTO columnNameDTO = new ColumnNameDTO(colIndex,originColNameList.get(colIndex));

            ArrayList<ColumnNameDTO> columnNameDTOS = new ArrayList<>();
            columnNameDTOS.add(columnNameDTO);
            //插入后缀为_colNameTable的表，形式为id，原始的列名
            readListenerProperties.getDataWriter().insertToColumnName(
                    readListenerProperties.getImportConfig().getColNameTablePrefix()+
                            DataImportConst.COLNAME_TABLE_SUFFIX, columnNameDTOS
            );
            buildSqlColName = String.valueOf(colIndex);
            return buildSqlColName;
        }

        return null;
    }

    public static boolean isStringInMapValues(String input, Map<String, String> map) {
        return map.values().stream().anyMatch(value -> value.equals(input));
    }

}

