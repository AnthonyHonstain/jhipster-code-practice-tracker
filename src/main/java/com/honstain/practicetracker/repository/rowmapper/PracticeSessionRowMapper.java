package com.honstain.practicetracker.repository.rowmapper;

import com.honstain.practicetracker.domain.PracticeSession;
import io.r2dbc.spi.Row;
import java.time.ZonedDateTime;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link PracticeSession}, with proper type conversions.
 */
@Service
public class PracticeSessionRowMapper implements BiFunction<Row, String, PracticeSession> {

    private final ColumnConverter converter;

    public PracticeSessionRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link PracticeSession} stored in the database.
     */
    @Override
    public PracticeSession apply(Row row, String prefix) {
        PracticeSession entity = new PracticeSession();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setStart(converter.fromRow(row, prefix + "_start", ZonedDateTime.class));
        entity.setEnd(converter.fromRow(row, prefix + "_jhi_end", ZonedDateTime.class));
        return entity;
    }
}
