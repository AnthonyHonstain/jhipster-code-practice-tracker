package com.honstain.practicetracker.repository;

import com.honstain.practicetracker.domain.PracticeSession;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the PracticeSession entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PracticeSessionRepository extends ReactiveCrudRepository<PracticeSession, Long>, PracticeSessionRepositoryInternal {
    @Override
    <S extends PracticeSession> Mono<S> save(S entity);

    @Override
    Flux<PracticeSession> findAll();

    @Override
    Mono<PracticeSession> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface PracticeSessionRepositoryInternal {
    <S extends PracticeSession> Mono<S> save(S entity);

    Flux<PracticeSession> findAllBy(Pageable pageable);

    Flux<PracticeSession> findAll();

    Mono<PracticeSession> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<PracticeSession> findAllBy(Pageable pageable, Criteria criteria);

}
