import axios from "axios";
import { openNotification } from "utils/utilities";

export const getSmtpConfig = async () => {
  try {
    const { data } = await axios.get(`/platform/getSMTPConfig`);

    return data;
  } catch (error) {
    openNotification("Config not fetched.", " ", "error");
    return [];
  }
};
export const updateSmtpConfig = async (payload: any) => {
  try {
    const { data } = await axios.post(`/platform/updateSMTPConfig`, payload);

    return data;
  } catch (error) {
    openNotification("Config not updated.", " ", "error");
    return [];
  }
};

export const deleteUserAPI = (id: string) => {
  return axios.delete(`/passport/users/${id}`);
};

export const fetchAllSSODetailsAPI = () => {
  return axios.get(`/passport/oath2/registrations/all`);
};

export const getUserDetailsByIdAPI = (id: string) => {
  return axios.get(`/passport/users/${id}`);
};

export const fetchAllTokensAPI = () => {
  return axios.get(`/passport/token/GetLongLived`);
};
export const createTokenAPI = (payload: any) => {
  return axios.post(`/passport/token/CreateLongLived`, payload);
};
