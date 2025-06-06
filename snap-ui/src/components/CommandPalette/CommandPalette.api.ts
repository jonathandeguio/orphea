import axios, { AxiosResponse } from "axios";

/**
 * update User Data API
 * @param body  payload
 */
export const updateUserDataAPI = (
  body: any
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/passport/users/updateMetaData`, body);
};
