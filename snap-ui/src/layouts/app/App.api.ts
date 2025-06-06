import axios, { AxiosResponse } from "axios";

/**
 * ping
 * @param path path we are calling ping from
 */
export const ping = (path: string): Promise<AxiosResponse<any, any>> => {
  return axios.post("/ping", { path });
};

