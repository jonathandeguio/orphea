import axios from "axios";
import {
  ALL_USER_DETAILS_FAIL,
  ALL_USER_DETAILS_REQUEST,
  ALL_USER_DETAILS_SUCCESS,
  IS_GROUP_ADMIN_FAIL,
  IS_GROUP_ADMIN_REQUEST,
  IS_GROUP_ADMIN_SUCCESS,
  IS_PROJECT_ADMIN_FAIL,
  IS_PROJECT_ADMIN_REQUEST,
  IS_PROJECT_ADMIN_SUCCESS,
  IS_USER_ADMIN_FAIL,
  IS_USER_ADMIN_REQUEST,
  IS_USER_ADMIN_SUCCESS,
  USER_BY_ID_FAIL,
  USER_BY_ID_REQUEST,
  USER_BY_ID_SUCCESS,
  USER_DETAILS_FAIL,
  USER_DETAILS_REQUEST,
  USER_DETAILS_RESET,
  USER_DETAILS_SUCCESS,
  USER_LOGIN_FAIL,
  USER_LOGIN_REQUEST,
  USER_LOGIN_SUCCESS,
  USER_LOGOUT,
} from "../constants/userConstants";

const BASE_URL_API = process.env.REACT_APP_BASE_URL_API;

export const login = (username: string, password: string) => async (dispatch: any) => {
  try {
    dispatch({
      type: USER_LOGIN_REQUEST,
    });

    const config = {
      headers: {
        "Content-type": "application/json",
      },
    };

    const payload = {"username": username, "password": password} 

    const { data } = await axios.post(
      `${BASE_URL_API}/passport/login`,
      payload,
      config
    );

    dispatch({
      type: USER_LOGIN_SUCCESS,
      payload: data,
    });

    localStorage.setItem("movetodataToken", data.accessToken);
  } catch (error) {
    dispatch({
      type: USER_LOGIN_FAIL,
      payload: error,
    });
  }
};

export const logout = () => async (dispatch: any) => {
  localStorage.removeItem("movetodataToken");
  dispatch({ type: USER_LOGOUT });
  dispatch({ type: USER_DETAILS_RESET });
};

export const getUserDetails = () => async (dispatch: any, getState: any) => {
  try {
    dispatch({
      type: USER_DETAILS_REQUEST,
    });

    const config = {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("movetodataToken")}`,
      },
    };

    const { data } = await axios.get(
      `${BASE_URL_API}/passport/users/me`,
      config
    );

    dispatch({
      type: USER_DETAILS_SUCCESS,
      payload: data,
    });
    return data;
  } catch (error) {
    dispatch({
      type: USER_DETAILS_FAIL,
      payload:
        (error as any).response && (error as any).response.data.detail
          ? (error as any).response.data.detail
          : (error as any).message,
    });
  }
};

export const getAllUserDetails = () => async (dispatch: any, getState: any) => {
  try {
    dispatch({
      type: ALL_USER_DETAILS_REQUEST,
    });

    const config = {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("movetodataToken")}`,
      },
    };

    const { data } = await axios.get(
      `${BASE_URL_API}/passport/users/all`,
      config
    );

    dispatch({
      type: ALL_USER_DETAILS_SUCCESS,
      payload: data,
    });
    return data;
  } catch (error) {
    dispatch({
      type: ALL_USER_DETAILS_FAIL,
      payload:
        (error as any).response && (error as any).response.data.detail
          ? (error as any).response.data.detail
          : (error as any).message,
    });
  }
};

export const isUserAdmin = () => async (dispatch: any, getState: any) => {
  try {
    dispatch({
      type: IS_USER_ADMIN_REQUEST,
    });

    const config = {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("movetodataToken")}`,
      },
    };

    const data = await axios.get(
      `${BASE_URL_API}/passport/users/isUserAdministrator`,
      config
    );
    dispatch({
      type: IS_USER_ADMIN_SUCCESS,
      payload: data,
    });
    return data;
  } catch (error) {
    dispatch({
      type: IS_USER_ADMIN_FAIL,
      payload:
        (error as any).response && (error as any).response.data.detail
          ? (error as any).response.data.detail
          : (error as any).message,
    });
  }
};

export const isProjectAdmin = () => async (dispatch: any, getState: any) => {
  try {
    dispatch({
      type: IS_PROJECT_ADMIN_REQUEST,
    });

    const config = {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("movetodataToken")}`,
      },
    };

    const data = await axios.get(
      `${BASE_URL_API}/passport/users/isProjectAdministrator`,
      config
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
        (error as any).response && (error as any).response.data.detail
          ? (error as any).response.data.detail
          : (error as any).message,
    });
  }
};

export const isGroupAdmin = () => async (dispatch: any, getState: any) => {
  try {
    dispatch({
      type: IS_GROUP_ADMIN_REQUEST,
    });

    const config = {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("movetodataToken")}`,
      },
    };

    const data = await axios.get(
      `${BASE_URL_API}/passport/users/isGroupAdministrator`,
      config
    );
    dispatch({
      type: IS_GROUP_ADMIN_SUCCESS,
      payload: data,
    });
    return data;
  } catch (error) {
    dispatch({
      type: IS_GROUP_ADMIN_FAIL,
      payload:
        (error as any).response && (error as any).response.data.detail
          ? (error as any).response.data.detail
          : (error as any).message,
    });
  }
};

export const getUserDetailByID = (id: any) => async (dispatch: any, getState: any) => {
  try {
    dispatch({
      type: USER_BY_ID_REQUEST,
    });

    const config = {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("movetodataToken")}`,
      },
    };

    const { data } = await axios.get(
      `${BASE_URL_API}/passport/users/${id}`,
      config
    );

    dispatch({
      type: USER_BY_ID_SUCCESS,
      payload: data,
    });
    return data;
  } catch (error) {
    dispatch({
      type: USER_BY_ID_FAIL,
      payload:
        (error as any).response && (error as any).response.data.detail
          ? (error as any).response.data.detail
          : (error as any).message,
    });
  }
};
