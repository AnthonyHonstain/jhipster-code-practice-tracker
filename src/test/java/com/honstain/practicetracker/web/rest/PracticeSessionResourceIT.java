package com.honstain.practicetracker.web.rest;

import static com.honstain.practicetracker.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.honstain.practicetracker.IntegrationTest;
import com.honstain.practicetracker.domain.PracticeSession;
import com.honstain.practicetracker.repository.EntityManager;
import com.honstain.practicetracker.repository.PracticeSessionRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link PracticeSessionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class PracticeSessionResourceIT {

    private static final ZonedDateTime DEFAULT_START = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_START = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_END = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_END = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String ENTITY_API_URL = "/api/practice-sessions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private PracticeSessionRepository practiceSessionRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private PracticeSession practiceSession;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PracticeSession createEntity(EntityManager em) {
        PracticeSession practiceSession = new PracticeSession().start(DEFAULT_START).end(DEFAULT_END);
        return practiceSession;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PracticeSession createUpdatedEntity(EntityManager em) {
        PracticeSession practiceSession = new PracticeSession().start(UPDATED_START).end(UPDATED_END);
        return practiceSession;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(PracticeSession.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        practiceSession = createEntity(em);
    }

    @Test
    void createPracticeSession() throws Exception {
        int databaseSizeBeforeCreate = practiceSessionRepository.findAll().collectList().block().size();
        // Create the PracticeSession
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(practiceSession))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the PracticeSession in the database
        List<PracticeSession> practiceSessionList = practiceSessionRepository.findAll().collectList().block();
        assertThat(practiceSessionList).hasSize(databaseSizeBeforeCreate + 1);
        PracticeSession testPracticeSession = practiceSessionList.get(practiceSessionList.size() - 1);
        assertThat(testPracticeSession.getStart()).isEqualTo(DEFAULT_START);
        assertThat(testPracticeSession.getEnd()).isEqualTo(DEFAULT_END);
    }

    @Test
    void createPracticeSessionWithExistingId() throws Exception {
        // Create the PracticeSession with an existing ID
        practiceSession.setId(1L);

        int databaseSizeBeforeCreate = practiceSessionRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(practiceSession))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the PracticeSession in the database
        List<PracticeSession> practiceSessionList = practiceSessionRepository.findAll().collectList().block();
        assertThat(practiceSessionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void getAllPracticeSessionsAsStream() {
        // Initialize the database
        practiceSessionRepository.save(practiceSession).block();

        List<PracticeSession> practiceSessionList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(PracticeSession.class)
            .getResponseBody()
            .filter(practiceSession::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(practiceSessionList).isNotNull();
        assertThat(practiceSessionList).hasSize(1);
        PracticeSession testPracticeSession = practiceSessionList.get(0);
        assertThat(testPracticeSession.getStart()).isEqualTo(DEFAULT_START);
        assertThat(testPracticeSession.getEnd()).isEqualTo(DEFAULT_END);
    }

    @Test
    void getAllPracticeSessions() {
        // Initialize the database
        practiceSessionRepository.save(practiceSession).block();

        // Get all the practiceSessionList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(practiceSession.getId().intValue()))
            .jsonPath("$.[*].start")
            .value(hasItem(sameInstant(DEFAULT_START)))
            .jsonPath("$.[*].end")
            .value(hasItem(sameInstant(DEFAULT_END)));
    }

    @Test
    void getPracticeSession() {
        // Initialize the database
        practiceSessionRepository.save(practiceSession).block();

        // Get the practiceSession
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, practiceSession.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(practiceSession.getId().intValue()))
            .jsonPath("$.start")
            .value(is(sameInstant(DEFAULT_START)))
            .jsonPath("$.end")
            .value(is(sameInstant(DEFAULT_END)));
    }

    @Test
    void getNonExistingPracticeSession() {
        // Get the practiceSession
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewPracticeSession() throws Exception {
        // Initialize the database
        practiceSessionRepository.save(practiceSession).block();

        int databaseSizeBeforeUpdate = practiceSessionRepository.findAll().collectList().block().size();

        // Update the practiceSession
        PracticeSession updatedPracticeSession = practiceSessionRepository.findById(practiceSession.getId()).block();
        updatedPracticeSession.start(UPDATED_START).end(UPDATED_END);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedPracticeSession.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedPracticeSession))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the PracticeSession in the database
        List<PracticeSession> practiceSessionList = practiceSessionRepository.findAll().collectList().block();
        assertThat(practiceSessionList).hasSize(databaseSizeBeforeUpdate);
        PracticeSession testPracticeSession = practiceSessionList.get(practiceSessionList.size() - 1);
        assertThat(testPracticeSession.getStart()).isEqualTo(UPDATED_START);
        assertThat(testPracticeSession.getEnd()).isEqualTo(UPDATED_END);
    }

    @Test
    void putNonExistingPracticeSession() throws Exception {
        int databaseSizeBeforeUpdate = practiceSessionRepository.findAll().collectList().block().size();
        practiceSession.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, practiceSession.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(practiceSession))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the PracticeSession in the database
        List<PracticeSession> practiceSessionList = practiceSessionRepository.findAll().collectList().block();
        assertThat(practiceSessionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchPracticeSession() throws Exception {
        int databaseSizeBeforeUpdate = practiceSessionRepository.findAll().collectList().block().size();
        practiceSession.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(practiceSession))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the PracticeSession in the database
        List<PracticeSession> practiceSessionList = practiceSessionRepository.findAll().collectList().block();
        assertThat(practiceSessionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamPracticeSession() throws Exception {
        int databaseSizeBeforeUpdate = practiceSessionRepository.findAll().collectList().block().size();
        practiceSession.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(practiceSession))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the PracticeSession in the database
        List<PracticeSession> practiceSessionList = practiceSessionRepository.findAll().collectList().block();
        assertThat(practiceSessionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdatePracticeSessionWithPatch() throws Exception {
        // Initialize the database
        practiceSessionRepository.save(practiceSession).block();

        int databaseSizeBeforeUpdate = practiceSessionRepository.findAll().collectList().block().size();

        // Update the practiceSession using partial update
        PracticeSession partialUpdatedPracticeSession = new PracticeSession();
        partialUpdatedPracticeSession.setId(practiceSession.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedPracticeSession.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedPracticeSession))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the PracticeSession in the database
        List<PracticeSession> practiceSessionList = practiceSessionRepository.findAll().collectList().block();
        assertThat(practiceSessionList).hasSize(databaseSizeBeforeUpdate);
        PracticeSession testPracticeSession = practiceSessionList.get(practiceSessionList.size() - 1);
        assertThat(testPracticeSession.getStart()).isEqualTo(DEFAULT_START);
        assertThat(testPracticeSession.getEnd()).isEqualTo(DEFAULT_END);
    }

    @Test
    void fullUpdatePracticeSessionWithPatch() throws Exception {
        // Initialize the database
        practiceSessionRepository.save(practiceSession).block();

        int databaseSizeBeforeUpdate = practiceSessionRepository.findAll().collectList().block().size();

        // Update the practiceSession using partial update
        PracticeSession partialUpdatedPracticeSession = new PracticeSession();
        partialUpdatedPracticeSession.setId(practiceSession.getId());

        partialUpdatedPracticeSession.start(UPDATED_START).end(UPDATED_END);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedPracticeSession.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedPracticeSession))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the PracticeSession in the database
        List<PracticeSession> practiceSessionList = practiceSessionRepository.findAll().collectList().block();
        assertThat(practiceSessionList).hasSize(databaseSizeBeforeUpdate);
        PracticeSession testPracticeSession = practiceSessionList.get(practiceSessionList.size() - 1);
        assertThat(testPracticeSession.getStart()).isEqualTo(UPDATED_START);
        assertThat(testPracticeSession.getEnd()).isEqualTo(UPDATED_END);
    }

    @Test
    void patchNonExistingPracticeSession() throws Exception {
        int databaseSizeBeforeUpdate = practiceSessionRepository.findAll().collectList().block().size();
        practiceSession.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, practiceSession.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(practiceSession))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the PracticeSession in the database
        List<PracticeSession> practiceSessionList = practiceSessionRepository.findAll().collectList().block();
        assertThat(practiceSessionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchPracticeSession() throws Exception {
        int databaseSizeBeforeUpdate = practiceSessionRepository.findAll().collectList().block().size();
        practiceSession.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(practiceSession))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the PracticeSession in the database
        List<PracticeSession> practiceSessionList = practiceSessionRepository.findAll().collectList().block();
        assertThat(practiceSessionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamPracticeSession() throws Exception {
        int databaseSizeBeforeUpdate = practiceSessionRepository.findAll().collectList().block().size();
        practiceSession.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(practiceSession))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the PracticeSession in the database
        List<PracticeSession> practiceSessionList = practiceSessionRepository.findAll().collectList().block();
        assertThat(practiceSessionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deletePracticeSession() {
        // Initialize the database
        practiceSessionRepository.save(practiceSession).block();

        int databaseSizeBeforeDelete = practiceSessionRepository.findAll().collectList().block().size();

        // Delete the practiceSession
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, practiceSession.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<PracticeSession> practiceSessionList = practiceSessionRepository.findAll().collectList().block();
        assertThat(practiceSessionList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
