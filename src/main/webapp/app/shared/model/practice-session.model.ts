import dayjs from 'dayjs';
import { IPractice } from 'app/shared/model/practice.model';

export interface IPracticeSession {
  id?: number;
  start?: string | null;
  end?: string | null;
  practices?: IPractice[] | null;
}

export const defaultValue: Readonly<IPracticeSession> = {};
