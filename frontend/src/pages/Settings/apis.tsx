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

export const decryptLicenseKeyAPI = (licenseKey: string) => {
  return axios.post(`/platform/decryptLicenseKey`, licenseKey);
};
