package com.honstain.practicetracker.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class PracticeSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("problem_name", table, columnPrefix + "_problem_name"));
        columns.add(Column.aliased("problem_link", table, columnPrefix + "_problem_link"));
        columns.add(Column.aliased("start", table, columnPrefix + "_start"));
        columns.add(Column.aliased("jhi_end", table, columnPrefix + "_jhi_end"));
        columns.add(Column.aliased("result", table, columnPrefix + "_result"));

        columns.add(Column.aliased("practice_session_id", table, columnPrefix + "_practice_session_id"));
        return columns;
    }
}
