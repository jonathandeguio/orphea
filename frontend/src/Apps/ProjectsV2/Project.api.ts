import axios, { AxiosResponse } from "axios";
import { IResourceFilters } from "./interfaces/Project";

/**
 * get all Projects List
 */
export const getAllProjectsAPI = (
  page: number,
  pageSize: number,
  filters: IResourceFilters
): Promise<AxiosResponse<any, any>> => {
  return axios.post(
    `/kitab/projects/paginated/all?page=${page}&elementPerPage=${pageSize}&direction=${filters.sortDirection}&key=${filters.sortBy}`,
    filters
  );
};

export const deleteProjectAPI = (
  projectId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.delete(`/kitab/${projectId}/moveToTrash`);
};

export const renameProjectAPI = (
  projectId: string,
  name: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/kitab/${projectId}/${name}/rename`);
};

export const changeDescProjectAPI = (
  projectId: string,
  desc: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/kitab/${projectId}/${desc}/renameDescription`);
};
