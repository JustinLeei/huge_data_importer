package com.justinlee.dbimporter;

import java.io.File;

public interface FileParser {
    void parse(File file,ImportConfig importConfig);


}
