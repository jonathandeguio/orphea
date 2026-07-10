import axios from "axios";
import {
  MOVETODATA_TOKEN,
  MOVETODATA_LOGGED_IN_USER_PASSWORD,
  LANGUAGE,
  MOVETODATA_LOGGED_IN_USER,
} from "layouts/auth/constants";
import {
  ALL_USER_DETAILS_FAIL,
  ALL_USER_DETAILS_REQUEST,
  ALL_USER_DETAILS_SUCCESS,
  IS_CONNECT_ADMIN_SUCCESS,
  IS_GROUP_ADMIN_FAIL,
  IS_GROUP_ADMIN_REQUEST,
  IS_GROUP_ADMIN_SUCCESS,
  IS_LOGEDIN_WITH_OTP,
  IS_PLATFORM_ADMIN_FAIL,
  IS_PLATFORM_ADMIN_REQUEST,
  IS_PLATFORM_ADMIN_SUCCESS,
  IS_PROJECT_ADMIN_FAIL,
  IS_PROJECT_ADMIN_REQUEST,
  IS_PROJECT_ADMIN_SUCCESS,
  IS_USER_ADMIN_FAIL,
  IS_USER_ADMIN_REQUEST,
  IS_USER_ADMIN_SUCCESS,
  SET_LOGIN_METHOD,
  SET_MFA_SHOWN,
  USER_DETAILS_FAIL,
  USER_DETAILS_REQUEST,
  USER_DETAILS_RESET,
  USER_DETAILS_SUCCESS,
  USER_DETAILS_UPDATE,
  USER_LOGIN_FAIL,
  USER_LOGIN_REQUEST,
  USER_LOGIN_SUCCESS,
  USER_LOGOUT,
} from "../constants/userConstants";
import { isDefined } from "utils/utilities";

export const login =
  (
    username: string,
    password: string,
    successCallback: any,
    errorCallback: any,
    mfaCallback: any
  ) =>
  async (dispatch: any) => {
    try {
      dispatch({
        type: USER_LOGIN_REQUEST,
      });

      const config = {
        headers: {
          "Content-type": "application/json",
        },
      };

      const payload = { username, password, loginType: "plain" };

      // Step 1: Make the login request
      const { data, status } = await axios.post(
        `/passport/login`,
        payload,
        config
      );

      // Step 2: Check for successful status and accessToken
      if (status === 200 && data.accessToken) {
        localStorage.setItem(MOVETODATA_TOKEN, data.accessToken);
        successCallback(); // Proceed with the success callback
        dispatch({
          type: USER_LOGIN_SUCCESS,
          payload: data,
        });
      } else if (
        status === 200 &&
        isDefined(data.mfaEnabled) &&
        data.mfaEnabled === true
      ) {
        dispatch(setLoginMethod(IS_LOGEDIN_WITH_OTP));
        mfaCallback(); // Trigger MFA flow here
      } else {
        // Handle the case where the response is unsuccessful or accessToken is missing
        errorCallback?.("Login failed: Invalid credentials or response");
        dispatch({
          type: USER_LOGIN_FAIL,
          payload: "Login failed: Invalid credentials or response",
        });
      }
    } catch (error: any) {
      // Step 3: Catch any error, including 400 Bad Request
      if (error.response && error.response.status === 400) {
        errorCallback?.("Login failed: Bad request (400)");
      } else {
        errorCallback?.(error.message || "An error occurred during login");
      }
      dispatch({
        type: USER_LOGIN_FAIL,
        payload: error.message || "An error occurred during login",
      });
    }
  };

export const setLoginMethod = (type: string) => (dispatch: any) => {
  dispatch({
    type: type,
  });
};

export const fetchUserDetails =
  (token: any) => async (dispatch: any, getState: any) => {
    dispatch({
      type: USER_DETAILS_REQUEST,
    });

    const baseUrl = process.env.REACT_APP_BASE_URL_API;

    const response = await fetch(`${baseUrl}/passport/users/me`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    });

    // Parse the response
    const data = await response.json();

    return data;
  };

export const logout = () => async (dispatch: $TSFixMe) => {
  localStorage.removeItem(MOVETODATA_TOKEN);
  dispatch({ type: USER_LOGOUT });
  dispatch({ type: USER_DETAILS_RESET });
};

export const getUserDetails =
  () => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      dispatch({
        type: USER_DETAILS_REQUEST,
      });

      const { data } = await axios.get(`/passport/users/me`);

      localStorage.setItem(LANGUAGE, data?.preferences?.language);

      dispatch({
        type: USER_DETAILS_SUCCESS,
        payload: data,
      });
      return data;
    } catch (error) {
      dispatch({
        type: USER_DETAILS_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
    }
  };

export const updateUserDetails = (payload: any) => {
  localStorage.setItem(
    LANGUAGE,
    payload?.preferences?.language ?? localStorage.getItem(LANGUAGE)
  );

  return {
    type: USER_DETAILS_UPDATE,
    payload: payload,
  };
};

export const getAllUserDetails =
  () => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      dispatch({
        type: ALL_USER_DETAILS_REQUEST,
      });

      const { data } = await axios.get(`/passport/users/all`);

      dispatch({
        type: ALL_USER_DETAILS_SUCCESS,
        payload: data,
      });
      return data;
    } catch (error) {
      dispatch({
        type: ALL_USER_DETAILS_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
    }
  };

export const isUserAdmin =
  () => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      dispatch({
        type: IS_USER_ADMIN_REQUEST,
      });

      const { data } = await axios.get(`/passport/users/isUserAdministrator`);
      dispatch({
        type: IS_USER_ADMIN_SUCCESS,
        payload: data,
      });
      return data;
    } catch (error) {
      dispatch({
        type: IS_USER_ADMIN_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
    }
  };

export const isProjectAdmin =
  () => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      dispatch({
        type: IS_PROJECT_ADMIN_REQUEST,
      });

      const { data } = await axios.get(
        `/passport/users/isProjectAdministrator`
      );
      dispatch({
        type: IS_PROJECT_ADMIN_SUCCESS,
        payload: data,
      });
      return data;
    } catch (error) {
      dispatch({
        type: IS_PROJECT_ADMIN_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
    }
  };

export const isGroupAdmin =
  () => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      dispatch({
        type: IS_GROUP_ADMIN_REQUEST,
      });

      const { data } = await axios.get(`/passport/users/isGroupAdministrator`);
      dispatch({
        type: IS_GROUP_ADMIN_SUCCESS,
        payload: data,
      });
      return data;
    } catch (error) {
      dispatch({
        type: IS_GROUP_ADMIN_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
    }
  };

export const isPlatformAdmin = () => async (dispatch: any) => {
  try {
    dispatch({
      type: IS_PLATFORM_ADMIN_REQUEST,
    });

    const { data } = await axios.get(`/passport/users/isPlatformAdministrator`);
    dispatch({
      type: IS_PLATFORM_ADMIN_SUCCESS,
      payload: data,
    });
    return data;
  } catch (error) {
    dispatch({
      type: IS_PLATFORM_ADMIN_FAIL,
      payload:
        (error as $TSFixMe).response && (error as $TSFixMe).response.data.detail
          ? (error as $TSFixMe).response.data.detail
          : (error as $TSFixMe).message,
    });
  }
};

export const setIsConnectAdmin = (data: boolean) => {
  return {
    type: IS_CONNECT_ADMIN_SUCCESS,
    payload: data,
  };
};
