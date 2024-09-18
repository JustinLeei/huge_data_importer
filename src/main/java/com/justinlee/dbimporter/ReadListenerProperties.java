package com.justinlee.dbimporter;

import com.justinlee.dbimporter.datawriter.DataWriter;
import lombok.Builder;
import lombok.Data;

import java.util.concurrent.ExecutorService;
@Data
@Builder
public class ReadListenerProperties {
    //数据入库操作器
    private DataWriter dataWriter;
    //多线程执行器
    private ExecutorService executorService;
    //创建表配置
    private ImportConfig importConfig;
}
