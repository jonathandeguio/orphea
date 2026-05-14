import axios, { AxiosResponse } from "axios";
import { TCreateSync } from "./Sync.types";

/**
 * create or update database sync
 */
export const putSyncAPI = (
  payload: TCreateSync
): Promise<AxiosResponse<any, any>> => {
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
