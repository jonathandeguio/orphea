import axios, { AxiosResponse } from "axios";

/**
 * update BackingFs Config api
 * @param body
 */
export const updateBackingFsConfigAPI = (
  body: any
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/platform/updateBackingFsConfig`, body);
};

/**
 * get BackingFs Config api
 */
export const getBackingFsConfigAPI = (): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/platform/getBackingFsConfig`);
};
