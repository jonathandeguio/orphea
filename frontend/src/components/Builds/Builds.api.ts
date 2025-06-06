import axios, { AxiosResponse } from "axios";
import { TBuildTrigger } from "./Builds.types";

/**
 * fetches Mutliple user details
 * @param userIdList User Id list
 */
export const fetchUsersDetailsAPI = (
  userIdList: string[]
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/passport/users/byIds`, userIdList);
};

/**
 * fetches Resource details
 * @param resourceIdList resource Id list
 */
export const fetchResourcesListAPI = (
  resourceIdList: string[]
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/kitab/byIds`, resourceIdList);
};

/**
 * get all builds
 */
export const getAllBuildsAPI = (
  page: number,
  pageSize: number,
  filters: any
): Promise<AxiosResponse<any, any>> => {
  return axios.post(
    `/build/status/all?page=${page}&elementPerPage=${pageSize}&direction=dsc&key=startedAt`,
    filters
  );
};

/**
 * abort a build
 * @param id build Id
 */
export const abortBuildAPI = (
  payload: any
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/build/abort`, payload);
};

/**
 * abort a dataset writing transaction
 * @param datasetId
 * @param branch
 */
export const abortDatasetWritingTransactionAPI = (
  datasetId: string,
  branch: string
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/kitab/transactions/${datasetId}/${branch}/abort`);
};

/**
 * abort a dataset writing transaction for whole build datasets
 * @param buildId
 */
export const abortDatasetWritingTransactionForWholeBuildAPI = (
  buildId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/kitab/transactions/${buildId}/abort`);
};

/**
 * fetch detailed build logs
 * @param id build Id
 */
export const fetchDetailedBuildLogsAPI = (
  id: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/build/${id}/detailedLog`);
};

/**
 * fetch detailed logs
 * @param id build Id
 */
export const fetchDetailedLogs = (
  id: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/build/${id}/detailedLog`);
};

/**
 * fetch build logs
 * @param id build Id
 */
export const fetchBuildLogsAPI = (
  id: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/build/${id}/log`);
};

/**
 * fetch preview result
 * @param id previewId
 */
export const getPreviewResultAPI = (
  id: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/build/previewResult/${id}`);
};

/**
 * fetch Build Specification by buildId
 * @param payload body
 */
export const fetchBuildSpecificationsByBuildId = (
  buildId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/build/getBuildSpecificationByBuildId/${buildId}`);
};

export const buildByTriggerAPI = (
  trigger: TBuildTrigger,
  body: any
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/build/build/${trigger}`, body);
};
