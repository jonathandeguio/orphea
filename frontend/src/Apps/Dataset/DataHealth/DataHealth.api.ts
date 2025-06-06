import axios, { AxiosResponse } from "axios";
import { IDataHealthDTO } from "./DataHealth.types";

export const putDataHealthAPI = (
  datasetId: string,
  branch: string,
  dataHealthDTO: IDataHealthDTO
): Promise<AxiosResponse<any, any>> => {
  return axios.put(`/health/${datasetId}/${branch}`, dataHealthDTO);
};

export const getDataHealthAPI = (
  datasetId: string,
  branch: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/health/${datasetId}/${branch}`);
};

export const getDataHealthCheckLogsAPI = (
  datasetId: string,
  healthCheckId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/health/logs/${datasetId}/${healthCheckId}`);
};
