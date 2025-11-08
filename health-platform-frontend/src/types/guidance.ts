export interface GuidancePlan {
  planId: number;
  userId: number;
  doctorId?: number;
  type?: string;
  rules?: string;
  frequency?: string;
  dailyTime?: string;
  startAt?: string;
  endAt?: string;
  status?: string;
  createdAt?: string;
}
