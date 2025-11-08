export interface Appointment {
  apptId: number;
  userId: number;
  doctorId: number;
  apptDate: string;
  apptTime: string;
  status: string;
  symptom?: string;
  doctorName?: string;
  deptName?: string;
}

export interface Slot {
  time: string;
  capacity: number;
  status: string;
  left: number;
}

export interface AppointmentSearchParams {
  doctorId?: number;
  dateFrom?: string;
  dateTo?: string;
  status?: string;
  symptom?: string;
  page?: number;
  size?: number;
}
