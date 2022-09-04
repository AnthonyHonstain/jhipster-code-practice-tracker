import {
  entityTableSelector,
  entityDetailsButtonSelector,
  entityDetailsBackButtonSelector,
  entityCreateButtonSelector,
  entityCreateSaveButtonSelector,
  entityCreateCancelButtonSelector,
  entityEditButtonSelector,
  entityDeleteButtonSelector,
  entityConfirmDeleteButtonSelector,
} from '../../support/entity';

describe('Practice e2e test', () => {
  const practicePageUrl = '/practice';
  const practicePageUrlPattern = new RegExp('/practice(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const practiceSample = { problemName: 'Landing Hills Bacon', problemLink: 'deposit Account' };

  let practice;
  let practiceSession;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/practice-sessions',
      body: { start: '2022-09-04T07:05:39.902Z', end: '2022-09-04T14:50:02.456Z' },
    }).then(({ body }) => {
      practiceSession = body;
    });
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/practices+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/practices').as('postEntityRequest');
    cy.intercept('DELETE', '/api/practices/*').as('deleteEntityRequest');
  });

  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/practice-sessions', {
      statusCode: 200,
      body: [practiceSession],
    });
  });

  afterEach(() => {
    if (practice) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/practices/${practice.id}`,
      }).then(() => {
        practice = undefined;
      });
    }
  });

  afterEach(() => {
    if (practiceSession) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/practice-sessions/${practiceSession.id}`,
      }).then(() => {
        practiceSession = undefined;
      });
    }
  });

  it('Practices menu should load Practices page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('practice');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Practice').should('exist');
    cy.url().should('match', practicePageUrlPattern);
  });

  describe('Practice page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(practicePageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Practice page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/practice/new$'));
        cy.getEntityCreateUpdateHeading('Practice');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', practicePageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/practices',
          body: {
            ...practiceSample,
            practiceSession: practiceSession,
          },
        }).then(({ body }) => {
          practice = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/practices+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [practice],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(practicePageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Practice page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('practice');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', practicePageUrlPattern);
      });

      it('edit button click should load edit Practice page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Practice');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', practicePageUrlPattern);
      });

      it('edit button click should load edit Practice page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Practice');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', practicePageUrlPattern);
      });

      it('last delete button click should delete instance of Practice', () => {
        cy.intercept('GET', '/api/practices/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('practice').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', practicePageUrlPattern);

        practice = undefined;
      });
    });
  });

  describe('new Practice page', () => {
    beforeEach(() => {
      cy.visit(`${practicePageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Practice');
    });

    it('should create an instance of Practice', () => {
      cy.get(`[data-cy="problemName"]`).type('support').should('have.value', 'support');

      cy.get(`[data-cy="problemLink"]`).type('Multi-layered').should('have.value', 'Multi-layered');

      cy.get(`[data-cy="start"]`).type('2022-09-04T13:05').blur().should('have.value', '2022-09-04T13:05');

      cy.get(`[data-cy="end"]`).type('2022-09-03T18:54').blur().should('have.value', '2022-09-03T18:54');

      cy.get(`[data-cy="result"]`).select('PASS');

      cy.get(`[data-cy="practiceSession"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response.statusCode).to.equal(201);
        practice = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response.statusCode).to.equal(200);
      });
      cy.url().should('match', practicePageUrlPattern);
    });
  });
});
