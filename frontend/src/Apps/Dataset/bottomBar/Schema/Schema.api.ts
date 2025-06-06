import axios, { AxiosResponse } from "axios";

export const getDatasetSchemaAPI = (
  datasetId: string,
  branch: string,
  transactionId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/dataset/schema/${datasetId}/${branch}/${transactionId}`);
};

export const getResourceAPI = (
  id: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/kitab/${id}`);
};
