import axios, { AxiosResponse } from "axios";
import { CacheConfig } from "./Cache";

/**
 * update Cache Config api
 * @param body
 */
export const updateCacheConfigAPI = (
  body: CacheConfig
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/platform/updateCacheConfig`, body);
};

/**
 * get Cache Config api
 */
export const getCacheConfigAPI = (): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/platform/getCacheConfig`);
};
