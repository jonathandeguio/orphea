import axios from "axios";

export const getProjectsBasedOnSearchTextAPI = (query: string) => {
  return axios.post(`/kitab/searchProject`, query);
};

export const getAllSystemGroupsAPI = () => {
  return axios.get(`/passport/groups/getAllSystemGroups`);
};

export const getAllResourceGroupsAPI = () => {
  return axios.get(`/passport/groups/getAllResourceGroups`);
};
