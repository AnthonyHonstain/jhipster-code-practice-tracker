import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './practice-session.reducer';

export const PracticeSessionDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const practiceSessionEntity = useAppSelector(state => state.practiceSession.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="practiceSessionDetailsHeading">Practice Session</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{practiceSessionEntity.id}</dd>
          <dt>
            <span id="start">Start</span>
          </dt>
          <dd>
            {practiceSessionEntity.start ? <TextFormat value={practiceSessionEntity.start} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="end">End</span>
          </dt>
          <dd>
            {practiceSessionEntity.end ? <TextFormat value={practiceSessionEntity.end} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
        </dl>
        <Button tag={Link} to="/practice-session" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/practice-session/${practiceSessionEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default PracticeSessionDetail;
