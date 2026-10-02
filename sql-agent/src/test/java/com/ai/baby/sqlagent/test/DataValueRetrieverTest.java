package com.ai.baby.sqlagent.test;

import com.ai.baby.sqlagent.schema.domain.DataValueMatch;
import com.ai.baby.sqlagent.schema.service.DataValueRetriever;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class DataValueRetrieverTest {

    @Autowired
    private DataValueRetriever dataValueRetriever;

    @Test
    void testRetrieve() {

        List<DataValueMatch> results =
                dataValueRetriever.retrieve(
                        List.of("研发部"),
                        List.of(
                                "employee_salary",
                                "employee",
                                "department"
                        )
                );

        results.forEach(
                System.out::println
        );
    }

    @Test
    void testProjectValue() {

        List<DataValueMatch> results =
                dataValueRetriever.retrieve(
                        List.of(
                                "SQL Agent 智能查询系统"
                        ),
                        List.of(
                                "employee",
                                "employee_project",
                                "project"
                        )
                );

        results.forEach(
                System.out::println
        );
    }

    @Test
    void testNotExist() {

        List<DataValueMatch> result =
                dataValueRetriever.retrieve(
                        List.of("不存在的部门"),
                        List.of(
                                "employee_salary",
                                "employee",
                                "department"
                        )
                );

        System.out.println(result);
    }
}