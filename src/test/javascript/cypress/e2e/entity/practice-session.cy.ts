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

describe('PracticeSession e2e test', () => {
  const practiceSessionPageUrl = '/practice-session';
  const practiceSessionPageUrlPattern = new RegExp('/practice-session(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const practiceSessionSample = { start: '2022-09-04T07:30:08.891Z' };

  let practiceSession;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/practice-sessions+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/practice-sessions').as('postEntityRequest');
    cy.intercept('DELETE', '/api/practice-sessions/*').as('deleteEntityRequest');
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

  it('PracticeSessions menu should load PracticeSessions page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('practice-session');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('PracticeSession').should('exist');
    cy.url().should('match', practiceSessionPageUrlPattern);
  });

  describe('PracticeSession page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(practiceSessionPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create PracticeSession page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/practice-session/new$'));
        cy.getEntityCreateUpdateHeading('PracticeSession');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', practiceSessionPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/practice-sessions',
          body: practiceSessionSample,
        }).then(({ body }) => {
          practiceSession = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/practice-sessions+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [practiceSession],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(practiceSessionPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details PracticeSession page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('practiceSession');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', practiceSessionPageUrlPattern);
      });

      it('edit button click should load edit PracticeSession page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('PracticeSession');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', practiceSessionPageUrlPattern);
      });

      it('edit button click should load edit PracticeSession page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('PracticeSession');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', practiceSessionPageUrlPattern);
      });

      it('last delete button click should delete instance of PracticeSession', () => {
        cy.intercept('GET', '/api/practice-sessions/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('practiceSession').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', practiceSessionPageUrlPattern);

        practiceSession = undefined;
      });
    });
  });

  describe('new PracticeSession page', () => {
    beforeEach(() => {
      cy.visit(`${practiceSessionPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('PracticeSession');
    });

    it('should create an instance of PracticeSession', () => {
      cy.get(`[data-cy="start"]`).type('2022-09-04T17:54').blur().should('have.value', '2022-09-04T17:54');

      cy.get(`[data-cy="end"]`).type('2022-09-03T23:35').blur().should('have.value', '2022-09-03T23:35');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response.statusCode).to.equal(201);
        practiceSession = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response.statusCode).to.equal(200);
      });
      cy.url().should('match', practiceSessionPageUrlPattern);
    });
  });
});
