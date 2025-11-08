export interface ConsultSession {
  sessionId: number;
  userId: number;
  doctorId: number;
  status: string;
  chiefComplaint?: string;
  createdAt: string;
  closedAt?: string;
}

export interface ConsultMessage {
  msgId: number;
  sessionId: number;
  senderType: 'DOCTOR' | 'USER';
  contentType: string;
  content: string;
  createdAt: string;
  read?: boolean;
}
