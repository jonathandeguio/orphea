import axios, { AxiosResponse } from "axios";
import { ICloseRequest } from "../AccessManager";

export const getAccessRequestDetailsAPI = (
  id: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/accessManager/getAccessRequest/${id}`);
};

export const closeAccessRequestAPI = (
  closeRequest: ICloseRequest
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/accessManager/closeAccessRequest`, closeRequest);
};
