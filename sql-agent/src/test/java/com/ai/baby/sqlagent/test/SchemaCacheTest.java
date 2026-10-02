package com.ai.baby.sqlagent.test;

import com.ai.baby.sqlagent.schema.service.SchemaCache;
import com.ai.baby.sqlagent.schema.domain.SchemaInfo;
import com.ai.baby.sqlagent.schema.domain.SchemaRelation;
import com.ai.baby.sqlagent.schema.service.SchemaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ObjectUtils;

import java.util.List;

@SpringBootTest
class SchemaCacheTest {

    @Autowired
    private SchemaCache schemaCache;

    @Autowired
    private SchemaService schemaService;

    @Test
    void testSchemaCache() {

        // 触发 SchemaService 加载
        List<SchemaInfo> schemas =
                schemaService.loadSchemaList();

        System.out.println(
                "=============================="
        );

        System.out.println(
                "Schema count = "
                        + schemaCache.size()
        );

        System.out.println(
                "=============================="
        );

        for (SchemaInfo schema : schemas) {

            System.out.println(
                    "TABLE = "
                            + schema.getTableName()
            );

            System.out.println(
                    "COMMENT = "
                            + schema.getComment()
            );

            System.out.println(
                    "COLUMNS = "
                            + schema.getColumns().size()
            );
        }

        System.out.println(
                "=============================="
        );

        // 测试大小写
        SchemaInfo employee1 =
                schemaCache.get("employee");

        SchemaInfo employee2 =
                schemaCache.get("EMPLOYEE");

        System.out.println(
                "employee = "
                        + employee1.getTableName()
        );

        System.out.println(
                "EMPLOYEE = "
                        + (ObjectUtils.isEmpty(employee2) ? "NULL" : employee2.getTableName())
        );

        System.out.println(
                "=============================="
        );

        // 测试关系
        List<SchemaRelation> relations =
                schemaCache.getRelations("employee");

        System.out.println(
                "employee relations = "
                        + relations.size()
        );

        relations.forEach(
                relation ->
                        System.out.println(
                                relation.getFromTable()
                                        + "."
                                        + relation.getFromColumn()
                                        + " -> "
                                        + relation.getToTable()
                                        + "."
                                        + relation.getToColumn()
                        )
        );
    }
}