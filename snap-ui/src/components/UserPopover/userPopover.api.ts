import axios, { AxiosResponse } from "axios";

/**
 * Fetches user
 * @param id user id
 */
export const getUserApi = (id: string): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/passport/users/${id}`);
};
