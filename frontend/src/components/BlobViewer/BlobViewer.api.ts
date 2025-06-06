import axios, { AxiosResponse } from "axios";

/**
 * fetches Blob File
 * @param id File Id
 */
export const fetchBlobFileAPI = (id: string) => {
  return axios.get(`/kitab/folder/getFile/${id}`, {
    responseType: "blob",
  });
};

/**
 * fetches Resource details
 * @param id Resource Id
 */
export const fetchResourceAPI = (id: string) => {
  return axios.get(`/kitab/${id}`);
};

/**
 * fetches Resource details
 * @param id Resource Id
 */
export const updateFileAPI = (id: string, data: string) => {
  return axios.post(`/kitab/rawFileUpdate/${id}`, data);
};
