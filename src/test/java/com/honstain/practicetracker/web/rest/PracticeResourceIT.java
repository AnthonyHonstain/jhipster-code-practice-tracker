package com.honstain.practicetracker.web.rest;

import static com.honstain.practicetracker.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.honstain.practicetracker.IntegrationTest;
import com.honstain.practicetracker.domain.Practice;
import com.honstain.practicetracker.domain.PracticeSession;
import com.honstain.practicetracker.domain.enumeration.PracticeResult;
import com.honstain.practicetracker.repository.EntityManager;
import com.honstain.practicetracker.repository.PracticeRepository;
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
 * Integration tests for the {@link PracticeResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class PracticeResourceIT {

    private static final String DEFAULT_PROBLEM_NAME = "AAAAAAAAAA";
    private static final String UPDATED_PROBLEM_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_PROBLEM_LINK = "AAAAAAAAAA";
    private static final String UPDATED_PROBLEM_LINK = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_START = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_START = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_END = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_END = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final PracticeResult DEFAULT_RESULT = PracticeResult.PASS;
    private static final PracticeResult UPDATED_RESULT = PracticeResult.FAIL;

    private static final String ENTITY_API_URL = "/api/practices";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private PracticeRepository practiceRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Practice practice;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Practice createEntity(EntityManager em) {
        Practice practice = new Practice()
            .problemName(DEFAULT_PROBLEM_NAME)
            .problemLink(DEFAULT_PROBLEM_LINK)
            .start(DEFAULT_START)
            .end(DEFAULT_END)
            .result(DEFAULT_RESULT);
        // Add required entity
        PracticeSession practiceSession;
        practiceSession = em.insert(PracticeSessionResourceIT.createEntity(em)).block();
        practice.setPracticeSession(practiceSession);
        return practice;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Practice createUpdatedEntity(EntityManager em) {
        Practice practice = new Practice()
            .problemName(UPDATED_PROBLEM_NAME)
            .problemLink(UPDATED_PROBLEM_LINK)
            .start(UPDATED_START)
            .end(UPDATED_END)
            .result(UPDATED_RESULT);
        // Add required entity
        PracticeSession practiceSession;
        practiceSession = em.insert(PracticeSessionResourceIT.createUpdatedEntity(em)).block();
        practice.setPracticeSession(practiceSession);
        return practice;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Practice.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
        PracticeSessionResourceIT.deleteEntities(em);
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        practice = createEntity(em);
    }

    @Test
    void createPractice() throws Exception {
        int databaseSizeBeforeCreate = practiceRepository.findAll().collectList().block().size();
        // Create the Practice
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(practice))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Practice in the database
        List<Practice> practiceList = practiceRepository.findAll().collectList().block();
        assertThat(practiceList).hasSize(databaseSizeBeforeCreate + 1);
        Practice testPractice = practiceList.get(practiceList.size() - 1);
        assertThat(testPractice.getProblemName()).isEqualTo(DEFAULT_PROBLEM_NAME);
        assertThat(testPractice.getProblemLink()).isEqualTo(DEFAULT_PROBLEM_LINK);
        assertThat(testPractice.getStart()).isEqualTo(DEFAULT_START);
        assertThat(testPractice.getEnd()).isEqualTo(DEFAULT_END);
        assertThat(testPractice.getResult()).isEqualTo(DEFAULT_RESULT);
    }

    @Test
    void createPracticeWithExistingId() throws Exception {
        // Create the Practice with an existing ID
        practice.setId(1L);

        int databaseSizeBeforeCreate = practiceRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(practice))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Practice in the database
        List<Practice> practiceList = practiceRepository.findAll().collectList().block();
        assertThat(practiceList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkProblemNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = practiceRepository.findAll().collectList().block().size();
        // set the field null
        practice.setProblemName(null);

        // Create the Practice, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(practice))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Practice> practiceList = practiceRepository.findAll().collectList().block();
        assertThat(practiceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkProblemLinkIsRequired() throws Exception {
        int databaseSizeBeforeTest = practiceRepository.findAll().collectList().block().size();
        // set the field null
        practice.setProblemLink(null);

        // Create the Practice, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(practice))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Practice> practiceList = practiceRepository.findAll().collectList().block();
        assertThat(practiceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllPracticesAsStream() {
        // Initialize the database
        practiceRepository.save(practice).block();

        List<Practice> practiceList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Practice.class)
            .getResponseBody()
            .filter(practice::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(practiceList).isNotNull();
        assertThat(practiceList).hasSize(1);
        Practice testPractice = practiceList.get(0);
        assertThat(testPractice.getProblemName()).isEqualTo(DEFAULT_PROBLEM_NAME);
        assertThat(testPractice.getProblemLink()).isEqualTo(DEFAULT_PROBLEM_LINK);
        assertThat(testPractice.getStart()).isEqualTo(DEFAULT_START);
        assertThat(testPractice.getEnd()).isEqualTo(DEFAULT_END);
        assertThat(testPractice.getResult()).isEqualTo(DEFAULT_RESULT);
    }

    @Test
    void getAllPractices() {
        // Initialize the database
        practiceRepository.save(practice).block();

        // Get all the practiceList
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
            .value(hasItem(practice.getId().intValue()))
            .jsonPath("$.[*].problemName")
            .value(hasItem(DEFAULT_PROBLEM_NAME))
            .jsonPath("$.[*].problemLink")
            .value(hasItem(DEFAULT_PROBLEM_LINK))
            .jsonPath("$.[*].start")
            .value(hasItem(sameInstant(DEFAULT_START)))
            .jsonPath("$.[*].end")
            .value(hasItem(sameInstant(DEFAULT_END)))
            .jsonPath("$.[*].result")
            .value(hasItem(DEFAULT_RESULT.toString()));
    }

    @Test
    void getPractice() {
        // Initialize the database
        practiceRepository.save(practice).block();

        // Get the practice
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, practice.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(practice.getId().intValue()))
            .jsonPath("$.problemName")
            .value(is(DEFAULT_PROBLEM_NAME))
            .jsonPath("$.problemLink")
            .value(is(DEFAULT_PROBLEM_LINK))
            .jsonPath("$.start")
            .value(is(sameInstant(DEFAULT_START)))
            .jsonPath("$.end")
            .value(is(sameInstant(DEFAULT_END)))
            .jsonPath("$.result")
            .value(is(DEFAULT_RESULT.toString()));
    }

    @Test
    void getNonExistingPractice() {
        // Get the practice
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewPractice() throws Exception {
        // Initialize the database
        practiceRepository.save(practice).block();

        int databaseSizeBeforeUpdate = practiceRepository.findAll().collectList().block().size();

        // Update the practice
        Practice updatedPractice = practiceRepository.findById(practice.getId()).block();
        updatedPractice
            .problemName(UPDATED_PROBLEM_NAME)
            .problemLink(UPDATED_PROBLEM_LINK)
            .start(UPDATED_START)
            .end(UPDATED_END)
            .result(UPDATED_RESULT);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedPractice.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedPractice))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Practice in the database
        List<Practice> practiceList = practiceRepository.findAll().collectList().block();
        assertThat(practiceList).hasSize(databaseSizeBeforeUpdate);
        Practice testPractice = practiceList.get(practiceList.size() - 1);
        assertThat(testPractice.getProblemName()).isEqualTo(UPDATED_PROBLEM_NAME);
        assertThat(testPractice.getProblemLink()).isEqualTo(UPDATED_PROBLEM_LINK);
        assertThat(testPractice.getStart()).isEqualTo(UPDATED_START);
        assertThat(testPractice.getEnd()).isEqualTo(UPDATED_END);
        assertThat(testPractice.getResult()).isEqualTo(UPDATED_RESULT);
    }

    @Test
    void putNonExistingPractice() throws Exception {
        int databaseSizeBeforeUpdate = practiceRepository.findAll().collectList().block().size();
        practice.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, practice.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(practice))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Practice in the database
        List<Practice> practiceList = practiceRepository.findAll().collectList().block();
        assertThat(practiceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchPractice() throws Exception {
        int databaseSizeBeforeUpdate = practiceRepository.findAll().collectList().block().size();
        practice.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(practice))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Practice in the database
        List<Practice> practiceList = practiceRepository.findAll().collectList().block();
        assertThat(practiceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamPractice() throws Exception {
        int databaseSizeBeforeUpdate = practiceRepository.findAll().collectList().block().size();
        practice.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(practice))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Practice in the database
        List<Practice> practiceList = practiceRepository.findAll().collectList().block();
        assertThat(practiceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdatePracticeWithPatch() throws Exception {
        // Initialize the database
        practiceRepository.save(practice).block();

        int databaseSizeBeforeUpdate = practiceRepository.findAll().collectList().block().size();

        // Update the practice using partial update
        Practice partialUpdatedPractice = new Practice();
        partialUpdatedPractice.setId(practice.getId());

        partialUpdatedPractice.problemName(UPDATED_PROBLEM_NAME).problemLink(UPDATED_PROBLEM_LINK).end(UPDATED_END);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedPractice.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedPractice))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Practice in the database
        List<Practice> practiceList = practiceRepository.findAll().collectList().block();
        assertThat(practiceList).hasSize(databaseSizeBeforeUpdate);
        Practice testPractice = practiceList.get(practiceList.size() - 1);
        assertThat(testPractice.getProblemName()).isEqualTo(UPDATED_PROBLEM_NAME);
        assertThat(testPractice.getProblemLink()).isEqualTo(UPDATED_PROBLEM_LINK);
        assertThat(testPractice.getStart()).isEqualTo(DEFAULT_START);
        assertThat(testPractice.getEnd()).isEqualTo(UPDATED_END);
        assertThat(testPractice.getResult()).isEqualTo(DEFAULT_RESULT);
    }

    @Test
    void fullUpdatePracticeWithPatch() throws Exception {
        // Initialize the database
        practiceRepository.save(practice).block();

        int databaseSizeBeforeUpdate = practiceRepository.findAll().collectList().block().size();

        // Update the practice using partial update
        Practice partialUpdatedPractice = new Practice();
        partialUpdatedPractice.setId(practice.getId());

        partialUpdatedPractice
            .problemName(UPDATED_PROBLEM_NAME)
            .problemLink(UPDATED_PROBLEM_LINK)
            .start(UPDATED_START)
            .end(UPDATED_END)
            .result(UPDATED_RESULT);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedPractice.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedPractice))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Practice in the database
        List<Practice> practiceList = practiceRepository.findAll().collectList().block();
        assertThat(practiceList).hasSize(databaseSizeBeforeUpdate);
        Practice testPractice = practiceList.get(practiceList.size() - 1);
        assertThat(testPractice.getProblemName()).isEqualTo(UPDATED_PROBLEM_NAME);
        assertThat(testPractice.getProblemLink()).isEqualTo(UPDATED_PROBLEM_LINK);
        assertThat(testPractice.getStart()).isEqualTo(UPDATED_START);
        assertThat(testPractice.getEnd()).isEqualTo(UPDATED_END);
        assertThat(testPractice.getResult()).isEqualTo(UPDATED_RESULT);
    }

    @Test
    void patchNonExistingPractice() throws Exception {
        int databaseSizeBeforeUpdate = practiceRepository.findAll().collectList().block().size();
        practice.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, practice.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(practice))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Practice in the database
        List<Practice> practiceList = practiceRepository.findAll().collectList().block();
        assertThat(practiceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchPractice() throws Exception {
        int databaseSizeBeforeUpdate = practiceRepository.findAll().collectList().block().size();
        practice.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(practice))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Practice in the database
        List<Practice> practiceList = practiceRepository.findAll().collectList().block();
        assertThat(practiceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamPractice() throws Exception {
        int databaseSizeBeforeUpdate = practiceRepository.findAll().collectList().block().size();
        practice.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(practice))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Practice in the database
        List<Practice> practiceList = practiceRepository.findAll().collectList().block();
        assertThat(practiceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deletePractice() {
        // Initialize the database
        practiceRepository.save(practice).block();

        int databaseSizeBeforeDelete = practiceRepository.findAll().collectList().block().size();

        // Delete the practice
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, practice.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Practice> practiceList = practiceRepository.findAll().collectList().block();
        assertThat(practiceList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
