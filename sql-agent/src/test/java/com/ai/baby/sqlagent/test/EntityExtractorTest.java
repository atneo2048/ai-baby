package com.ai.baby.sqlagent.test;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ai.baby.sqlagent.schema.service.EntityExtractor;

@SpringBootTest
class EntityExtractorTest {

    @Autowired
    private EntityExtractor entityExtractor;

    @Test
    void testExtract() {
        List<String> entities = entityExtractor.extract("查询研发部平均工资");
        System.out.println("entities = " + entities);
    }
}
