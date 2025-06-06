import { TACCESS_MANAGER_STATUS } from "./AccessManager.utils";
import {
  TREQUEST_ACCESS_TYPE,
  TROLES_TYPES,
} from "./RequestAccessModal/RequestAccessModal.utils";

export interface IAccessManagerFilters {
  searchText: string | undefined;
  status: string[];
  rangeFrom: string;
  rangeTo: string;
  type: TREQUEST_ACCESS_TYPE;
  showMyRequestsOnly: boolean;
  requesters: string[];
}

export interface IRequestAccess {
  title: string;
  description: string;
  type: TREQUEST_ACCESS_TYPE;
  role: TROLES_TYPES;
  requestTargetId: string;
  requesters: string[];
}

export interface IRequestAccessReview {
  title: string;
  description: string;
  type: TREQUEST_ACCESS_TYPE;
  role: TROLES_TYPES;
  requestTargetId: string | null;
  requestTargetName: string | null;
  requesterNames: string[];
  requesters: string[];
}

export interface IAccessRequest {
  id: string;
  title: string;
  description: string;
  assignees: string[];
  type: TREQUEST_ACCESS_TYPE;
  role: TROLES_TYPES;
  status: TACCESS_MANAGER_STATUS;
  requestTargetId: string;
  requestTargetName: string;
  requesters: string[];
  closingRemarks: string;
  createdAt: number;
  updatedAt: number;
  closedAt: number;
  createdBy: string;
  updatedBy: string;
  closedBy: string;
}

export interface ICloseRequest {
  requestId: string;
  closingRemarks: string;
  status: TACCESS_MANAGER_STATUS;
}
