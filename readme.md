# huge_data_importer

[中文版](README_zh.md)

This is a Java project for importing data from CSV, XLSX, or ZIP files into a MySQL database. It offers flexible configuration options and an extensible architecture, making the data import process simpler and more efficient.

## Features

- Supports parsing CSV, XLSX, and ZIP file formats
- Configurable prefixes for data tables and column name tables, with options to create new tables
- Supports importing over 1 million rows of data with low memory usage and high speed
- Easy integration into other Java projects

## Project Structure

- `ImportConfig`: Class for storing import configurations, including data table prefix, column name table prefix, options to create data tables and column name tables, etc.
- `FileParser`: File parser interface, defining methods for parsing files.
- `CSVParser`: Implementation class for CSV file parsing.
- `XLSXParser`: Implementation class for XLSX file parsing.
- `CellUtil`: Utility class for handling and normalizing cell data.
- `CSVReader`: Utility class for reading CSV files.
- `XlsxReadListener`: XLSX file reading listener.
- `DatabaseImporter`: Database importer interface.
- `DatabaseImporterImpl`: Implementation class for the database importer.
- `DataWriter`: Data writer interface.
- `MysqlWriter`: Implementation class for MySQL data writing.
- `exception`: Contains various custom exception classes.
- `config`: Contains configuration classes such as `TableConfig` and `ThreadExecuter`.

## Configuration

The project uses the Spring framework for configuration management, configuring database connections through `application.properties` or `application.yml` files.

## Usage

1. Clone or download the project locally.
2. Configure `application.yaml`, specifying the original and mapped names for **non-numeric columns**. For numeric columns, please check the **Database Entry Effect** section.
   ```yaml
   hdi:
     table:
       # Numeric column name retention setting, when set to false, numeric column names will be converted to 1, 2, 3..., actual column names stored in _colNameTable
       keep-original-numeric-names: false
   
       # Time column mapping: original column name -> converted column name
       time-column-mapping:
         "[Enrollment Date]": EnrollmentDate
         "[Graduation Date]": GraduationDate
         "[Date of Birth]": DateOfBirth
         "[Registration Date]": RegistrationDate
         "[Last Login Time]": LastLoginTime
   
       # Non-time column mapping: original column name -> converted column name
       non-time-column-mapping:
         "[Student ID]": StudentNumber
         "[Name]": Name
         "[Gender]": Gender
         "[Age]": Age
         "[Major Name]": Major
         "[Class Name]": Class
         "[Contact Phone]": ContactPhone
         "[Email]": Email
         "[Home Address]": HomeAddress
         "[ID Number]": IDNumber
         "[Emergency Contact]": EmergencyContact
         "[Emergency Contact Phone]": EmergencyContactPhone
         "[Education Level]": EducationLevel
         "[Enrollment Status]": EnrollmentStatus
         "[Scholarship Level]": ScholarshipLevel
         "[Credits]": TotalCredits
         "[Course Name]": CurrentCourseName
         "[Advisor Name]": AdvisorName
         "[Dorm Number]": DormNumber
   ```
3. Place the CSV, XLSX, or ZIP files to be imported in the specified directory.
4. Import the dependency in your project and inject the `DatabaseImporterImpl` object:
   ```java
   @Autowired 
   DatabaseImporterImpl databaseImporter;
   ```
5. Call the `importToDatabase` method to execute the data import:
   ```java
   databaseImporter.importToDatabase(filePaths, importConfig);
   ```
   Where `filePaths` is a list of file paths to be imported, and `importConfig` is the related import configuration.
6. After the data import is complete, you can view the imported data in the configured MySQL database.

## Database Entry Effect

```
/**
 * The format of {para}_colNameTable is as follows:
 *
 * id |originColName
 * ---+-------------------------------------
 *  1 |Student ID
 *  2 |Student Name
 *  3 |Student Age
 *  4 |Enrollment Year
 *  5 |Major
 *  6 |Semester GPA
 *  7 |Attendance Rate (%)
 *  8 |Tuition Payment Status
 *  9 |Graduation Status
 * 10 |Scholarship Status
 * -----------------------------------------
 */
/**
 * Example of {para}_dataTable:
 * StartTime        |StudentID|StudentName |1   |2     |3 |4     |5       |6   |7   |8
 * ----------------+--------+------------+----+------+---+------+--------+----+----+---
 * 2024-04-29 12:00:00|1001    |John Doe    |1001|John Doe|20 |2022  |Computer|3.5 |95.0|Paid
 * 2024-04-29 12:15:00|1002    |Jane Smith  |1002|Jane Smith|21 |2021  |Math    |3.8 |90.0|Paid
 * ---------------------------------------------------------------------
 */
```

## Contribution

Suggestions and code contributions to this project are welcome. If you find any issues or have ideas for improvements, please submit an Issue or Pull Request.

## License

This project is licensed under the [MIT License](LICENSE).
