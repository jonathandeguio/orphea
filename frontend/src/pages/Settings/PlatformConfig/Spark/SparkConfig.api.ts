import axios, { AxiosResponse } from "axios";

/**
 * update Spark Config api
 * @param body
 */
export const updateSparkConfigAPI = (
  body: any
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/platform/updateSparkConfig`, body);
};

/**
 * get Spark Config api
 */
export const getSparkConfigAPI = (): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/platform/getSparkConfig`);
};
