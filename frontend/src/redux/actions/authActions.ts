import axios from "axios";
import { openNotification } from "utils/utilities";
import {
  GROUP_ALL_FAIL,
  GROUP_ALL_REQUEST,
  GROUP_ALL_SUCCESS,
  MANAGE_GROUP_FAIL,
  MANAGE_GROUP_REQUEST,
  MANAGE_GROUP_SUCCESS,
  SET_NETWORK_ERROR_FALSE,
  SET_NETWORK_ERROR_TRUE,
  SSO_DETAILS_FAIL,
  SSO_DETAILS_REQUEST,
  SSO_DETAILS_SUCCESS,
  USER_DELETE_FAIL,
  USER_DELETE_REQUEST,
  USER_DELETE_SUCCESS,
  USER_ROLES_OF_PROJECT_FAIL,
  USER_ROLES_OF_PROJECT_REQUEST,
  USER_ROLES_OF_PROJECT_SUCCESS,
} from "../constants/authConstants";

export const getAllGroups =
  () => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      dispatch({
        type: GROUP_ALL_REQUEST,
      });

      const { data } = await axios.get(`/passport/groups/all`);

      dispatch({
        type: GROUP_ALL_SUCCESS,
        payload: data,
      });
      return data;
    } catch (error) {
      dispatch({
        type: GROUP_ALL_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
    }
  };

export const manageGroup =
  (newdetails: $TSFixMe) => async (dispatch: $TSFixMe) => {
    try {
      dispatch({ type: MANAGE_GROUP_REQUEST });
      const { data } = await axios.post(
        `/passport/groups/manageGroups`,
        newdetails
      );
      dispatch({
        type: MANAGE_GROUP_SUCCESS,
        payload: data,
      });
      return data;
    } catch (error) {
      dispatch({
        type: MANAGE_GROUP_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
    }
  };

export const deleteUser = (id: $TSFixMe) => async (dispatch: $TSFixMe) => {
  try {
    dispatch({ type: USER_DELETE_REQUEST });
    const { data } = await axios.delete(`/passport/users/${id}`);
    dispatch({
      type: USER_DELETE_SUCCESS,
      payload: data,
    });
    // openNotification(
    //   "User Deleted",
    //   "User is Deleted Successfully!",
    //   "success"
    // );
    return data;
  } catch (error) {
    dispatch({
      type: USER_DELETE_FAIL,
      payload:
        (error as $TSFixMe).response && (error as $TSFixMe).response.data.detail
          ? (error as $TSFixMe).response.data.detail
          : (error as $TSFixMe).message,
    });

    openNotification(
      "Failed to Delete User",
      "User cannot be deleted",
      "error"
    );
  }
};

export const setnetworkErrorTrue =
  () => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    dispatch({
      type: SET_NETWORK_ERROR_TRUE,
    });
  };
export const setnetworkErrorFalse =
  () => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    dispatch({
      type: SET_NETWORK_ERROR_FALSE,
    });
  };

export const getSSODetails = () => async (dispatch: any, getState: any) => {
  try {
    dispatch({ type: SSO_DETAILS_REQUEST });

    const { data } = await axios.get(`/passport/oath2/registrations/all`);

    dispatch({
      type: SSO_DETAILS_SUCCESS,
      payload: data,
    });
    return data;
  } catch (error) {
    dispatch({
      type: SSO_DETAILS_FAIL,
      payload:
        (error as $TSFixMe).response && (error as $TSFixMe).response.data.detail
          ? (error as $TSFixMe).response.data.detail
          : (error as $TSFixMe).message,
    });
  }
};
