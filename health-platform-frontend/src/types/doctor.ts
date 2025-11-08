export interface Department {
  deptId: number;
  deptName: string;
  description?: string;
}

export interface Doctor {
  doctorId: number;
  userId: number;
  deptId: number;
  title?: string;
  specialty?: string;
  status?: string;
  tags?: string[];
  available?: boolean;
  availableSlots?: string[];
  date?: string;
  timeSlot?: string;
}

export interface ScheduleSlot {
  time: string;
  capacity: number;
  status: string;
  left: number;
}
