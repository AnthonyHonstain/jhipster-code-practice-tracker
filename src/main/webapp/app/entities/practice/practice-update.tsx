import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IPracticeSession } from 'app/shared/model/practice-session.model';
import { getEntities as getPracticeSessions } from 'app/entities/practice-session/practice-session.reducer';
import { IPractice } from 'app/shared/model/practice.model';
import { PracticeResult } from 'app/shared/model/enumerations/practice-result.model';
import { getEntity, updateEntity, createEntity, reset } from './practice.reducer';

export const PracticeUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const practiceSessions = useAppSelector(state => state.practiceSession.entities);
  const practiceEntity = useAppSelector(state => state.practice.entity);
  const loading = useAppSelector(state => state.practice.loading);
  const updating = useAppSelector(state => state.practice.updating);
  const updateSuccess = useAppSelector(state => state.practice.updateSuccess);
  const practiceResultValues = Object.keys(PracticeResult);

  const handleClose = () => {
    navigate('/practice');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getPracticeSessions({}));
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
      ...practiceEntity,
      ...values,
      practiceSession: practiceSessions.find(it => it.id.toString() === values.practiceSession.toString()),
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
          result: 'PASS',
          ...practiceEntity,
          start: convertDateTimeFromServer(practiceEntity.start),
          end: convertDateTimeFromServer(practiceEntity.end),
          practiceSession: practiceEntity?.practiceSession?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="codePracticeTrackerApp.practice.home.createOrEditLabel" data-cy="PracticeCreateUpdateHeading">
            Create or edit a Practice
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? <ValidatedField name="id" required readOnly id="practice-id" label="ID" validate={{ required: true }} /> : null}
              <ValidatedField
                label="Problem Name"
                id="practice-problemName"
                name="problemName"
                data-cy="problemName"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField
                label="Problem Link"
                id="practice-problemLink"
                name="problemLink"
                data-cy="problemLink"
                type="text"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedField
                label="Start"
                id="practice-start"
                name="start"
                data-cy="start"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField label="End" id="practice-end" name="end" data-cy="end" type="datetime-local" placeholder="YYYY-MM-DD HH:mm" />
              <ValidatedField label="Result" id="practice-result" name="result" data-cy="result" type="select">
                {practiceResultValues.map(practiceResult => (
                  <option value={practiceResult} key={practiceResult}>
                    {practiceResult}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                id="practice-practiceSession"
                name="practiceSession"
                data-cy="practiceSession"
                label="Practice Session"
                type="select"
                required
              >
                <option value="" key="0" />
                {practiceSessions
                  ? practiceSessions.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <FormText>This field is required.</FormText>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/practice" replace color="info">
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

export default PracticeUpdate;
