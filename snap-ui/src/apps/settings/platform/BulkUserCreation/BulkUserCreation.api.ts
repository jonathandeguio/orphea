import axios, { AxiosResponse } from "axios";

/**
 * bulk User Creation api
 * @param file
 */
export const bulkUserCreationAPI = (
  body: any
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/passport/users/bulkUserCreation`, body, {
    headers: {
      "Content-type": "multipart/form-data",
    },
  });
};
