import axios, { AxiosResponse } from "axios";

/**
 *  get tags for particular dataset
 * @param datasetId Dataset Id
 */
export const fetchDatasetTagsAPI = (
  datasetId: string
): Promise<AxiosResponse<any, any>> => {
  return axios.get(`/docket/tag/getTagsByDatasetId/${datasetId}`);
};

/**
 *  manage tags for particular dataset
 * @param body payload
 */
export const manageDatasetTagsAPI = (
  body: any
): Promise<AxiosResponse<any, any>> => {
  return axios.post(`/docket/tag/manage`, body);
};

/**
 *  get all available  tags for with category
 */
export const fetchAllTagsWithCategoryAPI = (): Promise<
  AxiosResponse<any, any>
> => {
  return axios.get(`/docket/category/all`);
};
