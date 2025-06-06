import axios from "axios";
import { openNotification } from "utils/utilities";

export const getPlatformUsersAPI = async () => {
  try {
    const { data } = await axios.get(`/passport/users/all`);

    return data;
  } catch (error) {
    openNotification("Users not fetched.", " ", "error");
    return [];
  }
};

export const getDashboardTabsAPI = async (dashboardId: string) => {
  try {
    const { data } = await axios.get(
      `/kepler/tabs/getDashboardTabs/${dashboardId}`
    );
    return data;
  } catch (error) {
    openNotification("Tabs not fetched.", " ", "error");
    return [];
  }
};

export const createSubscription = async (payload: any) => {
  try {
    const { data } = await axios.post(`/subscribe/create`, payload);

    return data;
  } catch (error) {
    openNotification("Error Creating Subscription", " ", "error");
    return {};
  }
};

export const updateSubscription = async (
  payload: any,
  subscriptionId: string
) => {
  try {
    const { data } = await axios.post(
      `/subscribe/update/${subscriptionId}`,
      payload
    );

    return data;
  } catch (error) {
    openNotification("Error Updating Subscription", " ", "error");
    return {};
  }
};
