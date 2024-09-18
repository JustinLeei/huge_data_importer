package com.justinlee.dbimporter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/***
 * 非数值字段映射到统一名称
 * 未定义的字段，如果是非数值字段，值统一成0
 * ***/
@Component
@ConfigurationProperties(prefix = "hdi.table")
public class TableConfig {
    private Map<String, String> nonTimeColumnMapping = new HashMap<>(1000);
    private boolean keepOriginalColumnName;
    private  Map<String, String> timeColumnMapping = new HashMap<>(1000);

    public Map<String, String> getNonTimeColumnMapping() {
        return nonTimeColumnMapping;
    }

    public void setNonTimeColumnMapping(Map<String, String> nonTimeColumnMapping) {
        this.nonTimeColumnMapping = nonTimeColumnMapping;
    }

    public boolean isKeepOriginalColumnName() {
        return keepOriginalColumnName;
    }

    public void setKeepOriginalColumnName(boolean keepOriginalColumnName) {
        this.keepOriginalColumnName = keepOriginalColumnName;
    }

    public Map<String, String> getTimeColumnMapping() {
        return timeColumnMapping;
    }

    public void setTimeColumnMapping(Map<String, String> timeColumnMapping) {
        this.timeColumnMapping = timeColumnMapping;
    }

}
