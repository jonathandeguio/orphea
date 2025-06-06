import axios, { AxiosResponse } from "axios";

/**
 * update Git Config api
 * @param body
 */
export const updateGitConfigAPI = (
  body: any
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/platform/updateGitConfig`, body);
};

/**
 * get Git Config api
 */
export const getGitConfigAPI = (): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/platform/getGitConfig`);
};
