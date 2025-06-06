import axios, { AxiosResponse } from "axios";
// import { License } from "../deployments/configuration/licenseInfoSlice";
export const deleteDeploymentAPI = (id: string) => {
  return axios.delete(`/deployments/${id}`);
};

export const getDeploymentDetailsByIdAPI = (id: string) => {
  return axios.get(`/deployments/${id}`);
};

export const createDeploymentAPI = (payload: any) => {
  return axios.post(`/deployments/create`, payload);
};

export const getAllDeploymentsAPI = (payload: any) => {
  return axios.post(`/deployments/all`, payload);
};

export const deleteConfigurationAPI = (id: string) => {
  return axios.delete(`/deployments/configuration/${id}`);
};

export const getConfigurationDetailsByIdAPI = (id: string) => {
  return axios.get(`/deployments/configuration/${id}`);
};

export const createConfigurationAPI = (payload: any) => {
  return axios.post(`/deployments/configuration/create`, payload);
};

export const getAllConfigurationsAPI = (payload: any) => {
  return axios.post(`/deployments/configuration/all`, payload);
};

export const updateTargetState = (id: string, payload: any) => {
  return axios.put(`/deployments/configuration/state/target/${id}`, payload);
};

export const artifactObjectByName = (
  triggerName: string,
  branch: string
): Promise<AxiosResponse<any, any>> => {
  console.log(branch);
  return axios.get(`/artifact/byTriggerName/${triggerName}/${branch}`);
};

export const getGlobalVersionByPayload = (
  deploymentId: string,
  payload: any
) => {
  return axios.post(
    `/deployments/configuration/getGlobalVersion/${deploymentId}`,
    payload
  );
};

export const updateDeploymentMethod = (
  deploymentId: string,
  deploymentMethod: string,
  pauseUntil: string
) => {
  return axios.put(
    `/deployments/update/${deploymentId}/${deploymentMethod}/${pauseUntil}`
  );
};
/**
 * Start a Build by Trigger
 * @param id Build Id
 
 */
// export const runTrigger = (id: string): Promise<AxiosResponse<any, any>> => {
//   return axios.get(`/build/trigger/run/${id}`);
// };

// export const downloadArtifact = (
//   id: string
// ): Promise<AxiosResponse<any, any>> => {
//   return axios.get(`/artifact/download/${id}`);
// };

// export const deleteArtifact = (
//   id: string
// ): Promise<AxiosResponse<any, any>> => {
//   return axios.get(`/artifact/delete/${id}`);
// };

// export const artifactLog = (id: string): Promise<AxiosResponse<any, any>> => {
//   return axios.get(`/artifact/log/${id}`);
// };
