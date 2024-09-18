package com.justinlee.dbimporter;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import com.justinlee.dbimporter.constant.DataImportConst;
import com.justinlee.dbimporter.datawriter.ColumnNameDTO;
import com.justinlee.dbimporter.exception.FileTypeNotSupportExpection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
/**
 * {para}_colNameTable的形式如下：
 *
 * id |originColName
 * ---+-------------------------------------
 *  1 |学生ID
 *  2 |学生姓名
 *  3 |学生年龄
 *  4 |入学年份
 *  5 |专业
 *  6 |学期GPA
 *  7 |出勤率(%)
 *  8 |学费缴纳状态
 *  9 |毕业状态
 * 10 |奖学金获得情况
 * -----------------------------------------
 */

/**
 * {para}_dataTable的示例：
 * 开始时间          |学生ID |学生姓名     |1   |2     |3 |4     |5   |6   |7   |8
 * ----------------+------+------------+----+------+---+------+----+----+----+---
 * 2024-04-29 12:00:00|1001 |张三        |1001|张三   |20 |2022  |计算机|3.5 |95.0|已缴
 * 2024-04-29 12:15:00|1002 |李四        |1002|李四   |21 |2021  |数学  |3.8 |90.0|已缴
 * ---------------------------------------------------------------------
 */


/***
 * para是传入的表名前缀
 * {para}_dataTable中的_数字列名_是{para}_colNameTable中的id列
 * {para}_colNameTable是原始列名和数字列名的一一映射
 *
 * **/


@Component
@Slf4j
public class DatabaseImporterImpl implements DatabaseImporter {
    @Autowired
    private FileParserFactory fileParserFactory;
    /**
     * 数据入库入口方法
     *
     * **/
    @Override
    public void importToDatabase(String[] fileNames,ImportConfig importConfig){
        TimeInterval timeInterval = DateUtil.timer();
        for (String fileName : fileNames) {
            File file = new File(fileName);
            FileParser parser = fileParserFactory.getFileParser(file);
            if (parser != null) {
                parser.parse(file,importConfig);
            } else {
                throw new FileTypeNotSupportExpection("file is not supported!");
            }
        }
        log.error("入库耗时:{}",timeInterval.intervalPretty());
    }
    /***
     * 查询表为{para}_colNameTable
     * 根据id查询原始数数值列列名
     * @param colNameTablePrefix 即为para
     * **/
    public String queryColumnName(Integer id, String colNameTablePrefix){
        String tableName = colNameTablePrefix+ DataImportConst.COLNAME_TABLE_SUFFIX;
        return fileParserFactory.queryColumnName(id,tableName);
    }
    /**
     * @apiNote 查询表为{para}_colNameTable
     * @desc 查询所有列名和id的映射
     * @param colNameTablePrefix 即为para
     * **/
    public List<ColumnNameDTO> getAllIdAndColName(String colNameTablePrefix){
        String tableName = colNameTablePrefix+ DataImportConst.COLNAME_TABLE_SUFFIX;
        return fileParserFactory.getAllIdAndColName(tableName);
    }
    /**
     * @apiNote 查询表为{para}_colNameTable
     * @desc 根据原始列名查询数字列名
     * @param colNameTablePrefix 即为para
     * **/
    public Integer getIdByColName(String colNameTablePrefix, String colName) {
        String tableName = colNameTablePrefix+ DataImportConst.COLNAME_TABLE_SUFFIX;
        return fileParserFactory.getIdByColName(tableName,colName);
    }
}