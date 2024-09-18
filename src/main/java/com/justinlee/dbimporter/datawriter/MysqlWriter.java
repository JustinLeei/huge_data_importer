package com.justinlee.dbimporter.datawriter;

import com.justinlee.dbimporter.config.TableConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

@Component
@Slf4j
public class MysqlWriter implements DataWriter{
    @Autowired
    private DataSource dataSource;
    @Autowired
    ExecutorService hdiExecutorService;
    /**
     * @author lijugang
     * @desc 如下方法执行效率有点低，待优化
     *
     * ***/
    @Override
    public void batchInsertToDataTable(List<List<String>> data, String tableName, TableConfig tableConfig) {
        batchInsert(data, tableName,tableConfig);
    }
    private void batchInsert(List<List<String>> data, String tableName, TableConfig tableConfig) {
        if (data == null || data.isEmpty()) {
            return;
        }
        //看时间格式是什么,修改为对应的时间格式
        Collection<String> values = tableConfig.getTimeColumnMapping().values();
        boolean success = false;
        for (String value : values) {
            try {
                //固定使用第一个阁子的时间格式
                modifyDateField(data.get(0).get(0), tableName, value);
                success = true;
                break;  // 如果没有异常，立即跳出循环
            } catch (Exception e) {
                // 处理异常，可以选择打印日志或其他操作
                System.out.println("处理 " + value + " 时发生异常: " + e.getMessage());
            }
        }



        // 构建基础的SQL语句
        StringBuilder baseSql = new StringBuilder("INSERT INTO ");
        baseSql.append(tableName).append(" VALUES (");
        List<String> firstRow = data.get(0);
        for (int i = 0; i < firstRow.size(); i++) {
            baseSql.append("?,");
        }
        baseSql.setLength(baseSql.length() - 1); // 移除最后一个逗号
        baseSql.append(")");

        // 使用try-with-resources自动关闭资源
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(baseSql.toString())) {

            // 开启批处理
            for (List<String> record : data) {
                for (int i = 0; i < record.size(); i++) {
                    String value = record.get(i);
                    if ("blank".equals(value)) {
                        preparedStatement.setNull(i + 1, Types.VARCHAR);
                    } else {
                        preparedStatement.setString(i + 1, value);
                    }
                }
                preparedStatement.addBatch();
            }

            // 执行批处理
            preparedStatement.executeBatch();

        } catch (Exception e) {
            throw new RuntimeException("执行原始数据insert异常", e);
        }
    }
    public  void modifyDateField(String inputDate, String tableName, String fieldName) {
        log.error("输入时间是：{}",inputDate);

        // 定义日期时间格式的正则表达式
        String yearMonthPattern = "^\\d{4}-\\d{2}$";
        String yearMonthDayPattern = "^\\d{4}-\\d{2}-\\d{2}$";
        String yearMonthDayTimePattern = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$";

        String newType;

        // 判断输入的日期时间格式
        if (Pattern.matches(yearMonthPattern, inputDate)) {
            newType = "VARCHAR(7)";
        } else if (Pattern.matches(yearMonthDayPattern, inputDate)) {
            newType = "DATE";
        } else if (Pattern.matches(yearMonthDayTimePattern, inputDate)) {
            newType = "DATETIME";
        } else {
            throw new IllegalArgumentException("Unsupported date format");
        }
        // 修改表字段
        String alterQuery = "ALTER TABLE `" + tableName + "` MODIFY COLUMN `" + fieldName + "` " + newType;
        log.error("修改时间类型的字段是：",alterQuery);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(alterQuery)) {
            preparedStatement.execute();
        }catch (Exception e){
            throw new RuntimeException("修改数据类型失败",e);
        }


    }

    @Override
    public void createDataTable(String sqlColumns, String tableName) {
        StringBuilder createTableSql = new StringBuilder().append("CREATE TABLE IF NOT EXISTS ");
        createTableSql.append(tableName).append(" (");
        createTableSql.append(sqlColumns);
        createTableSql.setLength(createTableSql.length() - 1);
        createTableSql.append("); ");
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(createTableSql.toString())) {
            preparedStatement.execute();
        }catch (Exception e){
            throw new RuntimeException("创建数据表异常",e);
        }
    }

    @Override
    public String queryColumnName(Integer id, String tableName) {
        String originColName = null;
        String query = "SELECT originColName FROM " + tableName + " WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    originColName = resultSet.getString("originColName");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询失败", e);
        }

        return originColName;
    }


    public List<ColumnNameDTO> getAllIdAndColName(String tableName) {
        List<ColumnNameDTO> resultList = new ArrayList<>();
        String query = "SELECT id, originColName FROM " + tableName;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String colName = resultSet.getString("originColName");
                ColumnNameDTO columnNameDTO = new ColumnNameDTO();
                columnNameDTO.setId(id);
                columnNameDTO.setColName(colName);
                resultList.add(columnNameDTO);
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询失败", e);
        }

        return resultList;
    }
    public Integer getIdByColName(String tableName, String colName) {
        Integer id = null;
        String query = "SELECT id FROM " + tableName + " WHERE originColName = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, colName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    id = resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询失败", e);
        }

        return id;
    }
    @Override
    public void insertToColumnName(String tableName, List<ColumnNameDTO> data) {
        StringBuilder insertSql = new StringBuilder("INSERT IGNORE INTO ");
        insertSql.append(tableName);
        insertSql.append(" (id,originColName)");
        insertSql.append(" VALUES ");
        for (ColumnNameDTO columnNameDTO : data) {
            insertSql.append("(");
            insertSql.append(columnNameDTO.getId()).append(",");
            if(columnNameDTO.getColName().contains("\\")){
                String s = columnNameDTO.getColName().replaceAll("\\\\", "\\\\\\\\");
                insertSql.append("'").append(s).append("'");
            }else {
                insertSql.append("'").append(columnNameDTO.getColName()).append("'");
            }
            insertSql.append("),");
        }
        insertSql.setLength(insertSql.length() - 1);
        insertSql.append(";");
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertSql.toString())) {
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException("插入非数值列列名",e);
        }
    }


    @Override
    public void createColumnName(String colNameTable) {
        StringBuilder createTableSql = new StringBuilder().append("CREATE TABLE IF NOT EXISTS ");
        createTableSql.append(colNameTable).append(" (");
        createTableSql.append("id INT(5) PRIMARY KEY ,originColName  text");
        createTableSql.append("); ");
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(createTableSql.toString())) {
            preparedStatement.execute();
        }catch (Exception e){
            throw new RuntimeException("创建存储非数值列异常",e);
        }
    }
}
