import axios, { AxiosResponse } from "axios";

/**
 * get all Access requests
 */
export const getAllUserProjectsAPI = (
  userId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/passport/users/userProjects/${userId}`);
};
