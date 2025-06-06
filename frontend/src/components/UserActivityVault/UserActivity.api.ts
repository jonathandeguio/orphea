import axios from "axios";
/**
 * fetches Mutliple user details
 * @param userIdList User Id list
 */
export const fetchUsersDetailsAPI = (userIdList: string[]) => {
  return axios.post(`/passport/users/byIds`, userIdList);
};

export const getNewsAPI = () => {
  return axios.get("/news/all");
};

export const createLatestNewsAPI = (body: any) => {
  return axios.post("/news/latest", body);
};

export const updateLatestNewsAPI = (body: any) => {
  return axios.put(`/news/edit/${body.id}`, body);
};
