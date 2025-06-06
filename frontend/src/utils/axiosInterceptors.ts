import { BASE_URL, USERNAME } from "Authentication/constants";
import { notification } from "antd";
import axios, { AxiosRequestConfig } from "axios";
import { redirectTo } from "./Redirect";
import { getLanguageLabel, isDefined, isEmpty } from "./utilities";
const uuid = require("uuid");

interface CustomAxiosRequestConfig extends AxiosRequestConfig {
  _refreshAPI?: boolean; // Optional property
}

export const addInterceptors = () => {
  // Add a request interceptor
  axios.interceptors.request.use(
    function (config) {
      config.baseURL = process.env.REACT_APP_BASE_URL_API;

      if (isEmpty(config.headers["Content-Type"])) {
        config.headers["Content-Type"] = "application/json";
      }
      config.headers["Username"] = localStorage.getItem(USERNAME);
      config.headers["Source"] = BASE_URL;
      config.headers["Environment"] = process.env.NODE_ENV;
      config.headers["RequestId"] = uuid.v4();
      config.headers["DeploymentId"] = window.location.hostname;
      config.withCredentials = true;
      return config;
    },
    function (error) {
      return Promise.reject(error);
    }
  );

  axios.interceptors.response.use(
    function (response) {
      return response;
    },
    function (error) {
      const originalRequest = error.config;
      if (
        error.response &&
        error.response.status === 401 &&
        !originalRequest._refreshAPI
      ) {
        // Add _retry flag to the refresh token request
        const refreshTokenRequest: CustomAxiosRequestConfig = {
          method: "post",
          url: `${process.env.REACT_APP_BASE_URL_API}/passport/refresh-token`,
          withCredentials: true,
          _refreshAPI: true,
        };

        // Refresh the access token using the refresh token stored in cookies
        return axios(refreshTokenRequest)
          .then(() => {
            return axios(originalRequest);
          })
          .catch((refreshError) => {
            return Promise.reject(refreshError);
          });
      }
      if (error.response && error.response.status === 401) {
        redirectTo("/auth/login");
      } else if (error.code === "ERR_NETWORK") {
        notification.error({
          key: "networkErrorNotification",
          message: getLanguageLabel("networkError"),
          description: getLanguageLabel("networkErrorNote"),
          duration: 10,
          placement: "bottomLeft",
        });
      } else if (isDefined(error?.response?.data?.error)) {
        // openNotification(
        //   error.response.data.error,
        //   error?.response?.data?.description,
        //   "error",
        // );
      } else {
        // notification.error({
        //   key: "networkErrorNotification",
        //   message: getLanguageLabel("networkError"),
        //   description: getLanguageLabel("networkErrorNote"),
        //   duration: 1,
        //   placement: "bottomLeft",
        // });
      }
      return Promise.reject(error);
    }
  );
};
