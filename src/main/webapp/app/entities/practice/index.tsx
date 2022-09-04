import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Practice from './practice';
import PracticeDetail from './practice-detail';
import PracticeUpdate from './practice-update';
import PracticeDeleteDialog from './practice-delete-dialog';

const PracticeRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Practice />} />
    <Route path="new" element={<PracticeUpdate />} />
    <Route path=":id">
      <Route index element={<PracticeDetail />} />
      <Route path="edit" element={<PracticeUpdate />} />
      <Route path="delete" element={<PracticeDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default PracticeRoutes;
