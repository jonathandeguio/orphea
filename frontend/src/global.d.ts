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

export type TPlatformPage =
  | "DATASET"
  | "LINK"
  | "BUILD"
  | "SOURCE"
  | "REPOSITORY";

export interface IResource {
  id: string;
  project: string;
  parent: string;
  size: number;
  name: string;
  description: string;
  type: string;
  subType: string;
  status: string;
  createdBy: string;
  createdAt: number;
  updatedBy: string;
  updatedAt: number;
  favourite: false;
  children: IResource[];
  metaData: any;
}

export enum WriteModeEnum {
  SNAPSHOT = "SNAPSHOT",
  APPEND = "APPEND"  
}

export type TWriteMode = keyof typeof WriteModeEnum;
