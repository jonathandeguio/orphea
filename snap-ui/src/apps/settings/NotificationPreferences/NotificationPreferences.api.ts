import axios, { AxiosResponse } from "axios";

/**
 * get Notification preferences
 */
export const getNotificationPreferencesAPI = (): Promise<
  AxiosResponse<any, any>
> => {
  return axios.get(`/passport/users/getNotificationPreferences`);
};

/**
 * update Notification preferences
 */
export const updateNotificationPreferencesAPI = (body: {
  mention: boolean;
  subscription: boolean;
}) => {
  return axios.post(`/passport/users/updateNotificationPreferences`, body);
};
