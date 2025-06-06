import axios, { AxiosResponse } from "axios";
import { TDatamartSync } from "./Sync.types";

/**
 * create or update database sync
 */
export const putSyncAPI = (payload: any): Promise<AxiosResponse<any, any>> => {
  return axios.put(`/synchro/sync`, payload);
};

/**
 * Delete database sync
 */
export const deleteSyncAPI = (
  datasetId: string,
  syncId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.delete(`/synchro/sync/${datasetId}/${syncId}`);
};

/**
 * Get database syncs
 */
export const getDatasetSyncAPI = (
  datasetId: string,
  branch: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/synchro/sync/${datasetId}/${branch}`);
};

/**
 * Perform database syncs
 */
export const performDatasetSyncAPI = (
  datasetId: string,
  syncId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/synchro/perform/${datasetId}/${syncId}`);
};

export const getUnInitializedDataMartsAPI = (
  datasetId: string,
  branch: string
) => {
  return axios.get(`/synchro/getUnInitializedDataMarts/${datasetId}/${branch}`);
};

export const getDatamartSyncAPI = (
  datasetId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/datamart/getDatamartConfig/${datasetId}`);
};

export const setDatamartSyncAPI = (
  payload: TDatamartSync
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/datamart/setDatamartConfig`, payload);
};

export const getDataMartDetailAPI = (datamartId: string) => {
  return axios.get(`/datamart/${datamartId}`);
};
