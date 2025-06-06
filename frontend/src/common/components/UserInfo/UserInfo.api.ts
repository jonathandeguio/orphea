import axios, { AxiosResponse } from "axios";

/**
 * fetches user details
 * @param userId User Id
 */
export const fetchUserDetailsAPI = (
  userId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/passport/users/${userId}`);
};

/**
 * fetches user details
 * @param userId User Id
 */
export const fetchUserDetailsAPI2 = (props: {
  id: string;
}): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/passport/users/${props.id}`);
};
