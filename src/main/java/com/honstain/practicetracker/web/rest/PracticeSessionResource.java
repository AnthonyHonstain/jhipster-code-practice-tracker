package com.honstain.practicetracker.web.rest;

import com.honstain.practicetracker.domain.PracticeSession;
import com.honstain.practicetracker.repository.PracticeSessionRepository;
import com.honstain.practicetracker.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.honstain.practicetracker.domain.PracticeSession}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class PracticeSessionResource {

    private final Logger log = LoggerFactory.getLogger(PracticeSessionResource.class);

    private static final String ENTITY_NAME = "practiceSession";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PracticeSessionRepository practiceSessionRepository;

    public PracticeSessionResource(PracticeSessionRepository practiceSessionRepository) {
        this.practiceSessionRepository = practiceSessionRepository;
    }

    /**
     * {@code POST  /practice-sessions} : Create a new practiceSession.
     *
     * @param practiceSession the practiceSession to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new practiceSession, or with status {@code 400 (Bad Request)} if the practiceSession has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/practice-sessions")
    public Mono<ResponseEntity<PracticeSession>> createPracticeSession(@RequestBody PracticeSession practiceSession)
        throws URISyntaxException {
        log.debug("REST request to save PracticeSession : {}", practiceSession);
        if (practiceSession.getId() != null) {
            throw new BadRequestAlertException("A new practiceSession cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return practiceSessionRepository
            .save(practiceSession)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/practice-sessions/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /practice-sessions/:id} : Updates an existing practiceSession.
     *
     * @param id the id of the practiceSession to save.
     * @param practiceSession the practiceSession to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated practiceSession,
     * or with status {@code 400 (Bad Request)} if the practiceSession is not valid,
     * or with status {@code 500 (Internal Server Error)} if the practiceSession couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/practice-sessions/{id}")
    public Mono<ResponseEntity<PracticeSession>> updatePracticeSession(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody PracticeSession practiceSession
    ) throws URISyntaxException {
        log.debug("REST request to update PracticeSession : {}, {}", id, practiceSession);
        if (practiceSession.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, practiceSession.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return practiceSessionRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return practiceSessionRepository
                    .save(practiceSession)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /practice-sessions/:id} : Partial updates given fields of an existing practiceSession, field will ignore if it is null
     *
     * @param id the id of the practiceSession to save.
     * @param practiceSession the practiceSession to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated practiceSession,
     * or with status {@code 400 (Bad Request)} if the practiceSession is not valid,
     * or with status {@code 404 (Not Found)} if the practiceSession is not found,
     * or with status {@code 500 (Internal Server Error)} if the practiceSession couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/practice-sessions/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<PracticeSession>> partialUpdatePracticeSession(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody PracticeSession practiceSession
    ) throws URISyntaxException {
        log.debug("REST request to partial update PracticeSession partially : {}, {}", id, practiceSession);
        if (practiceSession.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, practiceSession.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return practiceSessionRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<PracticeSession> result = practiceSessionRepository
                    .findById(practiceSession.getId())
                    .map(existingPracticeSession -> {
                        if (practiceSession.getStart() != null) {
                            existingPracticeSession.setStart(practiceSession.getStart());
                        }
                        if (practiceSession.getEnd() != null) {
                            existingPracticeSession.setEnd(practiceSession.getEnd());
                        }

                        return existingPracticeSession;
                    })
                    .flatMap(practiceSessionRepository::save);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, res.getId().toString()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /practice-sessions} : get all the practiceSessions.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of practiceSessions in body.
     */
    @GetMapping("/practice-sessions")
    public Mono<List<PracticeSession>> getAllPracticeSessions() {
        log.debug("REST request to get all PracticeSessions");
        return practiceSessionRepository.findAll().collectList();
    }

    /**
     * {@code GET  /practice-sessions} : get all the practiceSessions as a stream.
     * @return the {@link Flux} of practiceSessions.
     */
    @GetMapping(value = "/practice-sessions", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<PracticeSession> getAllPracticeSessionsAsStream() {
        log.debug("REST request to get all PracticeSessions as a stream");
        return practiceSessionRepository.findAll();
    }

    /**
     * {@code GET  /practice-sessions/:id} : get the "id" practiceSession.
     *
     * @param id the id of the practiceSession to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the practiceSession, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/practice-sessions/{id}")
    public Mono<ResponseEntity<PracticeSession>> getPracticeSession(@PathVariable Long id) {
        log.debug("REST request to get PracticeSession : {}", id);
        Mono<PracticeSession> practiceSession = practiceSessionRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(practiceSession);
    }

    /**
     * {@code DELETE  /practice-sessions/:id} : delete the "id" practiceSession.
     *
     * @param id the id of the practiceSession to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/practice-sessions/{id}")
    public Mono<ResponseEntity<Void>> deletePracticeSession(@PathVariable Long id) {
        log.debug("REST request to delete PracticeSession : {}", id);
        return practiceSessionRepository
            .deleteById(id)
            .then(
                Mono.just(
                    ResponseEntity
                        .noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
                        .build()
                )
            );
    }
}
