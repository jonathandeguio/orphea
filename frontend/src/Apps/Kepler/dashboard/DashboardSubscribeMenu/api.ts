import axios from "axios";
import { openNotification } from "utils/utilities";

export const getSubscriptions = async (resourceId: string) => {
  try {
    const { data } = await axios.get(
      `/subscribe/getSubscription/${resourceId}`
    );

    return data;
  } catch (error) {
    openNotification("Subscriptions not fetched.", " ", "error");
    return [];
  }
};

export const deleteSubscriptionAPI = async (subscription: any) => {
  try {
    const { data } = await axios.delete(
      `/subscribe/deleteSubscription/${subscription.id}`
    );

    return data;
  } catch (error) {
    openNotification("Subscriptions delete failed.", " ", "error");
    return {};
  }
};

export const updateSubscriptionAPI = async (subscription: any) => {
  try {
    const { data } = await axios.post(
      `/subscribe/update/${subscription.id}`,
      subscription
    );

    return data;
  } catch (error) {
    openNotification("Subscriptions update failed.", " ", "error");
    return {};
  }
};
