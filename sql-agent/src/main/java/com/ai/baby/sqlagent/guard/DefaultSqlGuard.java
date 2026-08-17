package com.ai.baby.sqlagent.guard;

import org.springframework.stereotype.Component;

import com.ai.baby.sqlagent.domain.GuardResult;
import com.ai.baby.sqlagent.domain.SqlStatementType;
import com.ai.baby.sqlagent.enums.RiskLevel;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.CreateTable;

import java.util.Locale;

@Slf4j
@Component
public class DefaultSqlGuard implements SqlGuard {

        @Override
        public GuardResult check(String sql) {

                if (sql == null || sql.isBlank()) {

                        return blocked(
                                        "SQL为空");
                }

                try {

                        Statement statement = CCJSqlParserUtil.parse(sql);

                        SqlStatementType type = detectType(statement);

                        log.info(
                                        "SQL statement type: {}",
                                        type);

                        // =========================
                        // 1. 只允许 SELECT
                        // =========================

                        if (type != SqlStatementType.SELECT) {

                                return blocked(
                                                "禁止执行 "
                                                                + type
                                                                + " 类型SQL");
                        }

                        // =========================
                        // 2. SELECT进一步检查
                        // =========================

                        Select select = (Select) statement;

                        // SELECT *
                        if (containsSelectAll(select)) {

                                return GuardResult.builder()
                                                .allowed(true)
                                                .risk(RiskLevel.MEDIUM)
                                                .reason(
                                                                "SQL包含SELECT *")
                                                .build();
                        }

                        // =========================
                        // 3. LIMIT检查
                        // =========================

                        if (!containsLimit(select)) {

                                return GuardResult.builder()
                                                .allowed(true)
                                                .risk(RiskLevel.MEDIUM)
                                                .reason("SQL安全，但未设置LIMIT，存在大结果集风险")
                                                .build();
                        }

                        // =========================
                        // 4. 正常SQL
                        // =========================

                        return GuardResult.builder()
                                        .allowed(true)
                                        .risk(RiskLevel.LOW)
                                        .reason(
                                                        "SQL安全检查通过")
                                        .build();

                } catch (Exception e) {

                        log.warn(
                                        "SQL parse failed: {}",
                                        sql,
                                        e);

                        return blocked(
                                        "SQL解析失败，禁止执行");
                }
        }

        private SqlStatementType detectType(
                        Statement statement) {

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

        private boolean containsSelectAll(
                        Select select) {

                return select
                                .toString()
                                .matches(
                                                "(?is).*SELECT\\s+\\*.*");
        }

        private boolean containsLimit(
                        Select select) {

                return select
                                .toString()
                                .toUpperCase(
                                                Locale.ROOT)
                                .contains("LIMIT");
        }

        private GuardResult blocked(
                        String reason) {

                return GuardResult.builder()
                                .allowed(false)
                                .risk(RiskLevel.BLOCKED)
                                .reason(reason)
                                .build();
        }
}