import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { Translate, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IPracticeSession } from 'app/shared/model/practice-session.model';
import { getEntities } from './practice-session.reducer';

export const PracticeSession = () => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();

  const practiceSessionList = useAppSelector(state => state.practiceSession.entities);
  const loading = useAppSelector(state => state.practiceSession.loading);

  useEffect(() => {
    dispatch(getEntities({}));
  }, []);

  const handleSyncList = () => {
    dispatch(getEntities({}));
  };

  return (
    <div>
      <h2 id="practice-session-heading" data-cy="PracticeSessionHeading">
        Practice Sessions
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link to="/practice-session/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new Practice Session
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {practiceSessionList && practiceSessionList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>ID</th>
                <th>Start</th>
                <th>End</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {practiceSessionList.map((practiceSession, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/practice-session/${practiceSession.id}`} color="link" size="sm">
                      {practiceSession.id}
                    </Button>
                  </td>
                  <td>
                    {practiceSession.start ? <TextFormat type="date" value={practiceSession.start} format={APP_DATE_FORMAT} /> : null}
                  </td>
                  <td>{practiceSession.end ? <TextFormat type="date" value={practiceSession.end} format={APP_DATE_FORMAT} /> : null}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        tag={Link}
                        to={`/practice-session/${practiceSession.id}`}
                        color="info"
                        size="sm"
                        data-cy="entityDetailsButton"
                      >
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/practice-session/${practiceSession.id}/edit`}
                        color="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/practice-session/${practiceSession.id}/delete`}
                        color="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                      >
                        <FontAwesomeIcon icon="trash" /> <span className="d-none d-md-inline">Delete</span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && <div className="alert alert-warning">No Practice Sessions found</div>
        )}
      </div>
    </div>
  );
};

export default PracticeSession;
