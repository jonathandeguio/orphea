import axios, { AxiosResponse } from "axios";

/**
 *  send  a  call to zoro service to calculate Stats of a dataset
 * @param datasetId Dataset Id
 * @param branch Branch
 * @param transactionId Transaction
 */
export const calculateStatsAPI = (
  datasetId: string,
  branch: string,
  transactionId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(
    `/dataset/schema/${datasetId}/${branch}/${transactionId}/calculateStats`
  );
};

/**
 * fetch Stats of a dataset
 * @param datasetId Dataset Id
 * @param branch Branch
 * @param transactionId Transaction
 */
export const fetchStatsAPI = (
  datasetId: string,
  branch: string,
  transactionId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(
    `/dataset/schema/${datasetId}/${branch}/${transactionId}/stats`
  );
};
