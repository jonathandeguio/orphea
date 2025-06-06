export interface User {
  id: string;
  name: string;
  username: string;
  givenName: string;
  familyName: string;
  location: string;
  profileImage: any;
  email: string;
  preferences: any;
  provider: any;
  providerId: any;
  ssoAttributes: any;
  lastLoginAt: number;
  createdAt: number;
  updatedAt: number;
  createdBy: any;
  updatedBy: any;
}

export interface ErrorResponse {
  error: string;
  description: string;
  status: Number;
}
