export interface Prescription {
  prescId: number;
  appointmentId: number;
  doctorId: number;
  userId: number;
  prescDate: string;
  medicines: string;
  advice?: string;
}
