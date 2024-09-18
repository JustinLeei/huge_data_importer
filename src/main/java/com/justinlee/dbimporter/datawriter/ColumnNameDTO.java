package com.justinlee.dbimporter.datawriter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColumnNameDTO {
    private Integer id;
    private String colName;
}
