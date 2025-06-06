import axios, { AxiosResponse } from "axios";
import { Trigger } from "react-hotkeys-hook/dist/types";

export const deleteTriggerAPI = (id: string) => {
  return axios.delete(`/build/trigger/${id}`);
};

export const getTriggerDetailsByIdAPI = (id: string) => {
  return axios.get(`/build/trigger/${id}`);
};

export const createTriggerAPI = (payload: Trigger) => {
  return axios.post(`/build/trigger/create`, payload);
};

/**
 * Start a Build by Trigger
 * @param id Build Id
 
 */
export const runTrigger = (id: string): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/build/trigger/run/${id}`);
};

export const downloadArtifact = (
  id: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/artifact/download/${id}`);
};

export const deleteArtifact = (
  id: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/artifact/delete/${id}`);
};

export const artifactLog = (id: string): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/artifact/log/${id}`);
};

export const userById = (userId: string): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/passport/users/${userId}`);
};

export const getAllRegistry = () => {
  return axios.get("/build/trigger/registry");
};

export const getAllBranch = () => {
  return axios.get("/build/trigger/branch");
};

export const getFilteredTriggers = (
  selectedRegistry: string[],
  selectedBranch: string[]
) => {
  const params: any = {};
  if (selectedRegistry && selectedRegistry.length > 0) {
    params.registry = selectedRegistry.join(",");
  }
  if (selectedBranch && selectedBranch.length > 0) {
    params.branch = selectedBranch.join(",");
  }
  console.log("params", params);
  return axios.get("/build/trigger/registry-branch", { params });
};
