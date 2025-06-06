import axios, { AxiosResponse } from "axios";
import { PlatformConfig } from "./PlatformConfig";

/**
 * fetches platform config
 */
export const getPlatformConfigAPI = (): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/platform/config`);
};

/**
 * update platform config
 * @param payload
 */
export const updatePlatformConfigAPI = (
  payload: PlatformConfig
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/platform/config`, payload);
};

/**
 * fetches download logs
 */
export const getDownloadLogsAPI = (): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/dataset/downloadLog`);
};

/**
 * fetches upload logs
 */
export const getUploadLogsAPI = (): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/dataset/uploadLog`);
};

/**
 * download CSV,PARQUET dataset files
 * @param id datasetId
 * @param branch branch
 * @param transactionId transactionId
 * @param format dataFormat
 */
export const downloadDatasetAPI = (
  id: string,
  branch: string,
  transactionId: string,
  format: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(
    `/dataset/download/${id}/${branch}/${transactionId}/${format}`
  );
};