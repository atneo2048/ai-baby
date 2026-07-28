package com.ai.baby.sqlagent.formatter;

import com.ai.baby.sqlagent.domain.ColumnInfo;
import com.ai.baby.sqlagent.domain.SchemaInfo;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class SchemaFormatter {

    public String format(
            List<SchemaInfo> schemas) {

        StringBuilder sb = new StringBuilder();
        for (SchemaInfo schema : schemas) {
            sb.append("表:")
                    .append(schema.getTableName())
                    .append("\n");
            sb.append("字段:\n");
            for (ColumnInfo column : schema.getColumns()) {
                sb.append("- ")
                        .append(column.getColumnName())
                        .append(" ")
                        .append(column.getDataType())
                        .append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
