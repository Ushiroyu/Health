export interface CommunityActivity {
  eventId: number;
  title: string;
  description?: string;
  eventDate: string;
  location?: string;
  capacity?: number;
  organizer?: string;
  status?: string;
}

export interface ActivityRegistration {
  regId: number;
  eventId: number;
  userId: number;
  regTime: string;
  status?: string;
}
