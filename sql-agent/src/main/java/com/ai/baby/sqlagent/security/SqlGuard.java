package com.ai.baby.sqlagent.security;

import org.springframework.stereotype.Component;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

@Component
public class SqlGuard {

    private static final int MAX_ROWS = 100;

    public void check(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new RuntimeException(
                    "SQL不能为空");
        }

        try {
            Statement statement = CCJSqlParserUtil.parse(sql);

            /*
             * 只允许Select
             */
            if (!(statement instanceof Select)) {
                throw new RuntimeException(
                        "只允许执行SELECT查询");
            }

        } catch (JSQLParserException e) {
            throw new RuntimeException(
                    "SQL解析失败:"
                            + e.getMessage());
        }
    }

    public String checkAndRewrite(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);

            if (!(statement instanceof Select)) {
                throw new RuntimeException(
                        "只允许SELECT");
            }
            if (!sql.toLowerCase()
                    .contains("limit")) {
                sql = sql.trim()
                        .replace(";", "")
                        + " LIMIT 100";
            }
        } catch (JSQLParserException e) {
            throw new RuntimeException(
                    "SQL解析失败:"
                            + e.getMessage());
        }

        return sql;
    }
}
