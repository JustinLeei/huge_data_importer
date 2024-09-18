package com.justinlee.dbimporter.util;



import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.Cell;
import com.alibaba.excel.metadata.data.CellData;
import com.alibaba.excel.metadata.data.ReadCellData;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
@Slf4j
public class CellUtil {
    /***
     *@desc 处理每一个单元格的数字，循环处理一行的多个
     *
     * ***/
    public static  void processAndNormalizeListValues(ArrayList<String> values, AnalysisContext context,List<Integer> cachedIdList){
        try {
            int size = values.size();
            for (int i = 0; i < size; i++) {
                if (Optional.ofNullable(values.get(i)).isPresent()) {
                    CellData cell = (CellData) context.readRowHolder().getCellMap().get(i);
                    if (!Objects.equals(cell,null)){
                        CellDataTypeEnum cellDataTypeEnum = cell.getType();
                        //字符串
                        if (CellDataTypeEnum.STRING == cellDataTypeEnum && !cachedIdList.contains(Integer.valueOf(i))) {
                            String stringValue = cell.getStringValue();
                            values.set(i, stringValue);
                        }
                        //开发者没有配置的非数值列，将其变成数值列
                        if (CellDataTypeEnum.STRING == cellDataTypeEnum && cachedIdList.contains(Integer.valueOf(i))) {
                            values.set(i, "0");
                        }
                        //数字列
                        if (CellDataTypeEnum.NUMBER == cellDataTypeEnum) {
                            BigDecimal numberValue = cell.getNumberValue();
                            values.set(i, numberValue.toString());
                        }
                    }else {
                        values.set(i, "blank");
                    }
                } else {
                    //null值的处理
                    values.set(i, "blank");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
