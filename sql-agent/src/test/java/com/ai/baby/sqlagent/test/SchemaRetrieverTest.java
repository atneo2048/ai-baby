package com.ai.baby.sqlagent.test;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ai.baby.sqlagent.schema.domain.SchemaInfo;
import com.ai.baby.sqlagent.schema.service.SchemaRetriever;

@SpringBootTest
class SchemaRetrieverTest {

    @Autowired
    private SchemaRetriever schemaRetriever;

    @Test
    void testRetrieve() {
        test("查询每个部门平均工资\n");
    }

    private void test(String question) {
        System.out.println();
        System.out.println("================================");
        System.out.println("QUESTION: " + question);

        List<SchemaInfo> schemas = schemaRetriever.retrieve(question);

        System.out.println("RESULT:");
        schemas.forEach(schema -> System.out.println("  " + schema.getTableName()));
    }
}
