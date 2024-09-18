package com.justinlee.dbimporter;



import com.csvreader.CsvReader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CSVReader {

    public List<String> getHeader(String path) throws IOException {
        CsvReader csvReader = new CsvReader(path, ',', Charset.forName("utf-8"));
        //这里是个坑，必须先设置readHeader才能获取标题
        csvReader.readHeaders();
        String[] headers = csvReader.getHeaders();
        List<String> headersList = Arrays.asList(headers);
        return  headersList;
    }

    public static List<List<String>> getRawData(String path) throws IOException {
        List<List<String>> stringList2 = new ArrayList<>();
        CsvReader csvReader = new CsvReader(path, ',', Charset.forName("utf-8"));
        //跳过第一行
        csvReader.readHeaders();
        int headerCount = csvReader.getHeaderCount();
        while (csvReader.readRecord()){
            String rawRecord = csvReader.getRawRecord();
            List<String> stringList = new ArrayList<>();
            //这个正则匹配很关键，如果没有对数据中逗号处理有问题
            String[] fields = rawRecord.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            for (String field : fields) {
                if(field.contains("\""))
                    stringList.add(field);
                else
                    stringList.add("\""+field+"\"");
            }
            if(headerCount > stringList.size())
                for(int i=0; i< (headerCount-stringList.size()); i++)
                    stringList.add("\"\"");
            stringList2.add(stringList);
        }
        return stringList2;
    }
}
