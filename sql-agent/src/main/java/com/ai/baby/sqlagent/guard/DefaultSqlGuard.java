package com.ai.baby.sqlagent.guard;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.ai.baby.sqlagent.domain.GuardResult;
import com.ai.baby.sqlagent.domain.SqlStatementType;
import com.ai.baby.sqlagent.enums.RiskLevel;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

@Slf4j
@Component
public class DefaultSqlGuard implements SqlGuard {

    @Override
    public GuardResult check(String sql) {

        if (sql == null || sql.isBlank()) {
            return blocked("SQL为空");
        }

        String normalizedSql = sql.trim();
        log.info("开始SQL安全检查: {}", normalizedSql);

        try {
            Statement statement = CCJSqlParserUtil.parse(normalizedSql);
            SqlStatementType type = detectType(statement);

            log.info("SQL statement type: {}", type);

            if (type != SqlStatementType.SELECT) {
                return blocked("禁止执行 " + type + " 类型SQL");
            }

            Select select = (Select) statement;

            if (containsSelectAll(select)) {
                return GuardResult.builder()
                        .allowed(true)
                        .risk(RiskLevel.MEDIUM)
                        .reason("SQL包含SELECT *，字段范围较大")
                        .build();
            }

            if (!containsLimit(select)) {
                return GuardResult.builder()
                        .allowed(true)
                        .risk(RiskLevel.MEDIUM)
                        .reason("SQL安全，但未设置LIMIT，可能产生较大结果集")
                        .build();
            }

            return GuardResult.builder()
                    .allowed(true)
                    .risk(RiskLevel.LOW)
                    .reason("SQL安全检查通过")
                    .build();

        } catch (Exception e) {
            log.warn("SQL parse failed: {}", normalizedSql, e);
            return blocked("SQL解析失败，禁止执行");
        }
    }

    private SqlStatementType detectType(Statement statement) {

        if (statement instanceof Select) {
            return SqlStatementType.SELECT;
        }
        if (statement instanceof Insert) {
            return SqlStatementType.INSERT;
        }
        if (statement instanceof Update) {
            return SqlStatementType.UPDATE;
        }
        if (statement instanceof Delete) {
            return SqlStatementType.DELETE;
        }
        if (statement instanceof CreateTable
                || statement instanceof Alter
                || statement instanceof Drop
                || statement instanceof Truncate) {
            return SqlStatementType.DDL;
        }

        return SqlStatementType.UNKNOWN;
    }

    private boolean containsSelectAll(Select select) {
        return select.toString().matches("(?is).*SELECT\\s+\\*.*");
    }

    private boolean containsLimit(Select select) {
        return select.toString().toUpperCase(Locale.ROOT).contains("LIMIT");
    }

    private GuardResult blocked(String reason) {
        return GuardResult.builder()
                .allowed(false)
                .risk(RiskLevel.BLOCKED)
                .reason(reason)
                .build();
    }
}
