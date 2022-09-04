import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import PracticeSession from './practice-session';
import PracticeSessionDetail from './practice-session-detail';
import PracticeSessionUpdate from './practice-session-update';
import PracticeSessionDeleteDialog from './practice-session-delete-dialog';

const PracticeSessionRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<PracticeSession />} />
    <Route path="new" element={<PracticeSessionUpdate />} />
    <Route path=":id">
      <Route index element={<PracticeSessionDetail />} />
      <Route path="edit" element={<PracticeSessionUpdate />} />
      <Route path="delete" element={<PracticeSessionDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default PracticeSessionRoutes;
