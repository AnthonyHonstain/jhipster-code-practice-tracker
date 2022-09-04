import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './practice.reducer';

export const PracticeDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const practiceEntity = useAppSelector(state => state.practice.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="practiceDetailsHeading">Practice</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{practiceEntity.id}</dd>
          <dt>
            <span id="problemName">Problem Name</span>
          </dt>
          <dd>{practiceEntity.problemName}</dd>
          <dt>
            <span id="problemLink">Problem Link</span>
          </dt>
          <dd>{practiceEntity.problemLink}</dd>
          <dt>
            <span id="start">Start</span>
          </dt>
          <dd>{practiceEntity.start ? <TextFormat value={practiceEntity.start} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="end">End</span>
          </dt>
          <dd>{practiceEntity.end ? <TextFormat value={practiceEntity.end} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="result">Result</span>
          </dt>
          <dd>{practiceEntity.result}</dd>
          <dt>Practice Session</dt>
          <dd>{practiceEntity.practiceSession ? practiceEntity.practiceSession.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/practice" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/practice/${practiceEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default PracticeDetail;
