import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IPracticeSession } from 'app/shared/model/practice-session.model';
import { getEntity, updateEntity, createEntity, reset } from './practice-session.reducer';

export const PracticeSessionUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const practiceSessionEntity = useAppSelector(state => state.practiceSession.entity);
  const loading = useAppSelector(state => state.practiceSession.loading);
  const updating = useAppSelector(state => state.practiceSession.updating);
  const updateSuccess = useAppSelector(state => state.practiceSession.updateSuccess);

  const handleClose = () => {
    navigate('/practice-session');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    values.start = convertDateTimeToServer(values.start);
    values.end = convertDateTimeToServer(values.end);

    const entity = {
      ...practiceSessionEntity,
      ...values,
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {
          start: displayDefaultDateTime(),
          end: displayDefaultDateTime(),
        }
      : {
          ...practiceSessionEntity,
          start: convertDateTimeFromServer(practiceSessionEntity.start),
          end: convertDateTimeFromServer(practiceSessionEntity.end),
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="codePracticeTrackerApp.practiceSession.home.createOrEditLabel" data-cy="PracticeSessionCreateUpdateHeading">
            Create or edit a Practice Session
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? (
                <ValidatedField name="id" required readOnly id="practice-session-id" label="ID" validate={{ required: true }} />
              ) : null}
              <ValidatedField
                label="Start"
                id="practice-session-start"
                name="start"
                data-cy="start"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label="End"
                id="practice-session-end"
                name="end"
                data-cy="end"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/practice-session" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">Back</span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp; Save
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default PracticeSessionUpdate;
