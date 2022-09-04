package com.honstain.practicetracker.repository.rowmapper;

import com.honstain.practicetracker.domain.Practice;
import com.honstain.practicetracker.domain.enumeration.PracticeResult;
import io.r2dbc.spi.Row;
import java.time.ZonedDateTime;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Practice}, with proper type conversions.
 */
@Service
public class PracticeRowMapper implements BiFunction<Row, String, Practice> {

    private final ColumnConverter converter;

    public PracticeRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Practice} stored in the database.
     */
    @Override
    public Practice apply(Row row, String prefix) {
        Practice entity = new Practice();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setProblemName(converter.fromRow(row, prefix + "_problem_name", String.class));
        entity.setProblemLink(converter.fromRow(row, prefix + "_problem_link", String.class));
        entity.setStart(converter.fromRow(row, prefix + "_start", ZonedDateTime.class));
        entity.setEnd(converter.fromRow(row, prefix + "_jhi_end", ZonedDateTime.class));
        entity.setResult(converter.fromRow(row, prefix + "_result", PracticeResult.class));
        entity.setPracticeSessionId(converter.fromRow(row, prefix + "_practice_session_id", Long.class));
        return entity;
    }
}
