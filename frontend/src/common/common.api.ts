import axios, { AxiosResponse } from "axios";

/**
 * ping
 * @param path path we are calling ping from
 */
export const ping = (path: string): Promise<AxiosResponse<any, any>> => {
  return axios.post("/ping", { path });
};

/**
 * Get Connect Admin
 */
export const getIsConnectAdmin = (): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/passport/users/isConnectAdministrator`);
};

/**
 * Get Resource Permission for current user
 */
export const getResourcePermissionAPI = (
  resourceId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/passport/authz/resourcePermission/${resourceId}`);
};
