import axios, { AxiosResponse } from "axios";
import { IGlobalResourceSearchFilters } from "./HeaderSearch.types";

/**
 * Fetches the directory tree
 * @param id Repo id
 * @param branch branch
 */
export const getGlobalSearchResults = (
  globalSearchFilters: IGlobalResourceSearchFilters,
  page: number,
  pageSize: number,
  sortOrder: string,
  sortby: string
): Promise<AxiosResponse<any, any>> => {
  return axios.post(
    `/kitab/globalSearchV2?page=${page}&elementPerPage=${pageSize}&direction=${sortOrder}&key=${sortby}`,
    globalSearchFilters
  );
};
