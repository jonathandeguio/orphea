import axios, { AxiosResponse } from "axios";

/**
 * fetches user details
 * @param userId User Id
 */
export const getAllNotificationAPI = (
  userId: string,
  page: number,
  pageSize: number
): Promise<AxiosResponse<any, any>> => {
  return axios.get(
    `/kitab/notifications/allNotifications?page=${page}&elementPerPage=${pageSize}&direction=dsc&key=timestamp`
  );
};

/**
 * deletes the notification
 * @param notificationId
 */
export const deleteNotificationAPI = (
  notificationId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.delete(`/kitab/notifications/${notificationId}`);
};

/**
 * read the notification
 * @param notificationId
 */
export const readNotificationAPI = (
  notificationId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/kitab/notifications/${notificationId}/read`);
};

/**
 * read all my notifications

 */
export const readAllNotificationAPI = (): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/kitab/notifications/readAll`);
};
