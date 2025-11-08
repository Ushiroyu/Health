export interface JwtUser {
  userId: number;
  username: string;
  role: 'ADMIN' | 'DOCTOR' | 'RESIDENT' | 'STAFF';
  name?: string;
  token?: string;
}
