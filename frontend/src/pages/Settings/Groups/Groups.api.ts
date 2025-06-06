import axios, { AxiosResponse } from "axios";

/**
 * creates a new group
 * @param body new group details
 */
export const createGroupAPI = (body: any): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/passport/groups/add`, JSON.stringify(body));
};

/**
 * delete a  group
 * @param id group id to delete
 */
export const deleteGroupAPI = (
  id: string
): Promise<AxiosResponse<any, any>> => {
  return axios.delete(`/passport/groups/delete/${id}`);
};

/**
 * get group details
 * @param id groupId
 */
export const getGroupDetailsAPI = (
  id: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/passport/groups/${id}`);
};

/**
 * update group details
 * @param body
 */
export const updateGroupDetailsAPI = (
  body: any
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/passport/groups/manageGroups`, body);
};

export const changeGroupMetaDataAPI = (
  body: any
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/passport/groups/changeMetaData`, body);
};
