package com.justinlee.dbimporter.parse;

import com.justinlee.dbimporter.FileParser;
import com.justinlee.dbimporter.ImportConfig;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class CSVParser implements FileParser {
    @Override
    public void parse(File file, ImportConfig importConfig) {

    }
}
