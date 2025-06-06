import axios, { AxiosResponse } from "axios";
import {
  IAccessManagerFilters,
  IRequestAccess,
  IRequestAccessReview,
} from "./AccessManager";

/**
 * get all Access requests
 */
export const getAllAccessRequestsAPI = (
  page: number,
  pageSize: number,
  filters: IAccessManagerFilters
): Promise<AxiosResponse<any, any>> => {
  return axios.post(
    `/accessManager/all?page=${page}&elementPerPage=${pageSize}&direction=dsc&key=createdAt`,
    filters
  );
};

/**
 * create new access request
 */
export const requestAccessAPI = (
  requestAccess: IRequestAccessReview
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/accessManager/createAccessRequest`, requestAccess);
};
