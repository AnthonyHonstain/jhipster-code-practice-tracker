import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { Translate, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IPractice } from 'app/shared/model/practice.model';
import { getEntities } from './practice.reducer';

export const Practice = () => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();

  const practiceList = useAppSelector(state => state.practice.entities);
  const loading = useAppSelector(state => state.practice.loading);

  useEffect(() => {
    dispatch(getEntities({}));
  }, []);

  const handleSyncList = () => {
    dispatch(getEntities({}));
  };

  return (
    <div>
      <h2 id="practice-heading" data-cy="PracticeHeading">
        Practices
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link to="/practice/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new Practice
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {practiceList && practiceList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>ID</th>
                <th>Problem Name</th>
                <th>Problem Link</th>
                <th>Start</th>
                <th>End</th>
                <th>Result</th>
                <th>Practice Session</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {practiceList.map((practice, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/practice/${practice.id}`} color="link" size="sm">
                      {practice.id}
                    </Button>
                  </td>
                  <td>{practice.problemName}</td>
                  <td>{practice.problemLink}</td>
                  <td>{practice.start ? <TextFormat type="date" value={practice.start} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{practice.end ? <TextFormat type="date" value={practice.end} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{practice.result}</td>
                  <td>
                    {practice.practiceSession ? (
                      <Link to={`/practice-session/${practice.practiceSession.id}`}>{practice.practiceSession.id}</Link>
                    ) : (
                      ''
                    )}
                  </td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/practice/${practice.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button tag={Link} to={`/practice/${practice.id}/edit`} color="primary" size="sm" data-cy="entityEditButton">
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button tag={Link} to={`/practice/${practice.id}/delete`} color="danger" size="sm" data-cy="entityDeleteButton">
                        <FontAwesomeIcon icon="trash" /> <span className="d-none d-md-inline">Delete</span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && <div className="alert alert-warning">No Practices found</div>
        )}
      </div>
    </div>
  );
};

export default Practice;
