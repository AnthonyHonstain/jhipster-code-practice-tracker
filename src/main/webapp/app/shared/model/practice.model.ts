import dayjs from 'dayjs';
import { IPracticeSession } from 'app/shared/model/practice-session.model';
import { PracticeResult } from 'app/shared/model/enumerations/practice-result.model';

export interface IPractice {
  id?: number;
  problemName?: string;
  problemLink?: string;
  start?: string | null;
  end?: string | null;
  result?: PracticeResult | null;
  practiceSession?: IPracticeSession;
}

export const defaultValue: Readonly<IPractice> = {};
