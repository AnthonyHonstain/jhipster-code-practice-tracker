package com.honstain.practicetracker.repository;

import static org.springframework.data.relational.core.query.Criteria.where;

import com.honstain.practicetracker.domain.Practice;
import com.honstain.practicetracker.domain.enumeration.PracticeResult;
import com.honstain.practicetracker.repository.rowmapper.PracticeRowMapper;
import com.honstain.practicetracker.repository.rowmapper.PracticeSessionRowMapper;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC custom repository implementation for the Practice entity.
 */
@SuppressWarnings("unused")
class PracticeRepositoryInternalImpl extends SimpleR2dbcRepository<Practice, Long> implements PracticeRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final PracticeSessionRowMapper practicesessionMapper;
    private final PracticeRowMapper practiceMapper;

    private static final Table entityTable = Table.aliased("practice", EntityManager.ENTITY_ALIAS);
    private static final Table practiceSessionTable = Table.aliased("practice_session", "practiceSession");

    public PracticeRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        PracticeSessionRowMapper practicesessionMapper,
        PracticeRowMapper practiceMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Practice.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.practicesessionMapper = practicesessionMapper;
        this.practiceMapper = practiceMapper;
    }

    @Override
    public Flux<Practice> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Practice> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = PracticeSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(PracticeSessionSqlHelper.getColumns(practiceSessionTable, "practiceSession"));
        SelectFromAndJoinCondition selectFrom = Select
            .builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(practiceSessionTable)
            .on(Column.create("practice_session_id", entityTable))
            .equals(Column.create("id", practiceSessionTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Practice.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Practice> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Practice> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    private Practice process(Row row, RowMetadata metadata) {
        Practice entity = practiceMapper.apply(row, "e");
        entity.setPracticeSession(practicesessionMapper.apply(row, "practiceSession"));
        return entity;
    }

    @Override
    public <S extends Practice> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
