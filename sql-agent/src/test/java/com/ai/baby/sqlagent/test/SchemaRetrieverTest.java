package com.ai.baby.sqlagent.test;

import com.ai.baby.sqlagent.schema.domain.SchemaInfo;
import com.ai.baby.sqlagent.schema.service.SchemaRetriever;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class SchemaRetrieverTest {

    @Autowired
    private SchemaRetriever schemaRetriever;

    @Test
    void testRetrieve() {

//        test("查询所有员工\n");

//        test("查询研发部员工\n");
//
//        test("查询研发部平均工资\n");
//
        test("查询每个部门平均工资\n");
//
//        test("查询参与 SQL Agent 智能查询系统项目的员工");
    }

    private void test(String question) {

        System.out.println();
        System.out.println(
                "================================"
        );

        System.out.println(
                "QUESTION: " + question
        );

        List<SchemaInfo> schemas =
                schemaRetriever.retrieve(question);

        System.out.println( "RESULT:" );
        schemas.forEach(schema ->
                System.out.println( "  " + schema.getTableName() )
        );
    }
}