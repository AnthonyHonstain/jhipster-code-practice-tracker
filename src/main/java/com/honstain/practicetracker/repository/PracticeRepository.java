package com.honstain.practicetracker.repository;

import com.honstain.practicetracker.domain.Practice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Practice entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PracticeRepository extends ReactiveCrudRepository<Practice, Long>, PracticeRepositoryInternal {
    @Query("SELECT * FROM practice entity WHERE entity.practice_session_id = :id")
    Flux<Practice> findByPracticeSession(Long id);

    @Query("SELECT * FROM practice entity WHERE entity.practice_session_id IS NULL")
    Flux<Practice> findAllWherePracticeSessionIsNull();

    @Override
    <S extends Practice> Mono<S> save(S entity);

    @Override
    Flux<Practice> findAll();

    @Override
    Mono<Practice> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface PracticeRepositoryInternal {
    <S extends Practice> Mono<S> save(S entity);

    Flux<Practice> findAllBy(Pageable pageable);

    Flux<Practice> findAll();

    Mono<Practice> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Practice> findAllBy(Pageable pageable, Criteria criteria);

}
