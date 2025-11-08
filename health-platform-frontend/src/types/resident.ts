export interface HealthProfile {
  profileId?: number;
  userId: number;
  fullName?: string;
  gender?: string;
  birthDate?: string;
  bloodType?: string;
  phone?: string;
  email?: string;
  address?: string;
  idNumber?: string;
  chronicConditions?: string;
  allergies?: string;
  medicalHistory?: string;
  medications?: string;
  emergencyContact?: string;
  emergencyPhone?: string;
  lifestyleNotes?: string;
  lastUpdated?: string;
}

export interface HealthRecord {
  recordId: number;
  userId: number;
  recordDate: string;
  type: string;
  value: string;
  note?: string;
}

export interface Reminder {
  reminderId: number;
  userId: number;
  type: string;
  dailyTime: string;
  enabled: boolean;
}

export interface TrendPoint {
  date: string;
  value: number;
}
