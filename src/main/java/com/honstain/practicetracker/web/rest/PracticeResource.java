package com.honstain.practicetracker.web.rest;

import com.honstain.practicetracker.domain.Practice;
import com.honstain.practicetracker.repository.PracticeRepository;
import com.honstain.practicetracker.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
 * REST controller for managing {@link com.honstain.practicetracker.domain.Practice}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class PracticeResource {

    private final Logger log = LoggerFactory.getLogger(PracticeResource.class);

    private static final String ENTITY_NAME = "practice";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PracticeRepository practiceRepository;

    public PracticeResource(PracticeRepository practiceRepository) {
        this.practiceRepository = practiceRepository;
    }

    /**
     * {@code POST  /practices} : Create a new practice.
     *
     * @param practice the practice to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new practice, or with status {@code 400 (Bad Request)} if the practice has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/practices")
    public Mono<ResponseEntity<Practice>> createPractice(@Valid @RequestBody Practice practice) throws URISyntaxException {
        log.debug("REST request to save Practice : {}", practice);
        if (practice.getId() != null) {
            throw new BadRequestAlertException("A new practice cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return practiceRepository
            .save(practice)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/practices/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /practices/:id} : Updates an existing practice.
     *
     * @param id the id of the practice to save.
     * @param practice the practice to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated practice,
     * or with status {@code 400 (Bad Request)} if the practice is not valid,
     * or with status {@code 500 (Internal Server Error)} if the practice couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/practices/{id}")
    public Mono<ResponseEntity<Practice>> updatePractice(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Practice practice
    ) throws URISyntaxException {
        log.debug("REST request to update Practice : {}, {}", id, practice);
        if (practice.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, practice.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return practiceRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return practiceRepository
                    .save(practice)
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
     * {@code PATCH  /practices/:id} : Partial updates given fields of an existing practice, field will ignore if it is null
     *
     * @param id the id of the practice to save.
     * @param practice the practice to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated practice,
     * or with status {@code 400 (Bad Request)} if the practice is not valid,
     * or with status {@code 404 (Not Found)} if the practice is not found,
     * or with status {@code 500 (Internal Server Error)} if the practice couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/practices/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<Practice>> partialUpdatePractice(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Practice practice
    ) throws URISyntaxException {
        log.debug("REST request to partial update Practice partially : {}, {}", id, practice);
        if (practice.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, practice.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return practiceRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<Practice> result = practiceRepository
                    .findById(practice.getId())
                    .map(existingPractice -> {
                        if (practice.getProblemName() != null) {
                            existingPractice.setProblemName(practice.getProblemName());
                        }
                        if (practice.getProblemLink() != null) {
                            existingPractice.setProblemLink(practice.getProblemLink());
                        }
                        if (practice.getStart() != null) {
                            existingPractice.setStart(practice.getStart());
                        }
                        if (practice.getEnd() != null) {
                            existingPractice.setEnd(practice.getEnd());
                        }
                        if (practice.getResult() != null) {
                            existingPractice.setResult(practice.getResult());
                        }

                        return existingPractice;
                    })
                    .flatMap(practiceRepository::save);

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
     * {@code GET  /practices} : get all the practices.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of practices in body.
     */
    @GetMapping("/practices")
    public Mono<List<Practice>> getAllPractices() {
        log.debug("REST request to get all Practices");
        return practiceRepository.findAll().collectList();
    }

    /**
     * {@code GET  /practices} : get all the practices as a stream.
     * @return the {@link Flux} of practices.
     */
    @GetMapping(value = "/practices", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Practice> getAllPracticesAsStream() {
        log.debug("REST request to get all Practices as a stream");
        return practiceRepository.findAll();
    }

    /**
     * {@code GET  /practices/:id} : get the "id" practice.
     *
     * @param id the id of the practice to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the practice, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/practices/{id}")
    public Mono<ResponseEntity<Practice>> getPractice(@PathVariable Long id) {
        log.debug("REST request to get Practice : {}", id);
        Mono<Practice> practice = practiceRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(practice);
    }

    /**
     * {@code DELETE  /practices/:id} : delete the "id" practice.
     *
     * @param id the id of the practice to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/practices/{id}")
    public Mono<ResponseEntity<Void>> deletePractice(@PathVariable Long id) {
        log.debug("REST request to delete Practice : {}", id);
        return practiceRepository
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
