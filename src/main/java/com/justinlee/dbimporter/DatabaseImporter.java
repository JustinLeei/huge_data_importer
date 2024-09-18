package com.justinlee.dbimporter;

public interface DatabaseImporter {
    void importToDatabase(String[] fileNames,ImportConfig importConfig);
}
