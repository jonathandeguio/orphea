import axios from "axios";
import {
  LINK_CREATE_FAIL,
  LINK_CREATE_REQUEST,
  LINK_CREATE_SUCCESS,
  LINK_DELETE_FAIL,
  LINK_DELETE_REQUEST,
  LINK_DELETE_SUCCESS,
  LINK_LIST_FAIL,
  LINK_LIST_REQUEST,
  LINK_LIST_SUCCESS,
  PUT_LINK_EDITOR_CODE,
} from "../constants/linkConstants";

export const listLinks =
  () => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      dispatch({ type: LINK_LIST_REQUEST });

      const { data } = await axios.get(`/connect/link/Getall`);

      dispatch({
        type: LINK_LIST_SUCCESS,
        payload: data,
      });
    } catch (error) {
      dispatch({
        type: LINK_LIST_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
    }
  };

export const deleteLink = (id: $TSFixMe) => async (dispatch: $TSFixMe) => {
  try {
    dispatch({ type: LINK_DELETE_REQUEST });

    const { data } = await axios.delete(`/connect/link/DeleteById/${id}`);

    dispatch({
      type: LINK_DELETE_SUCCESS,
      payload: data,
    });
  } catch (error) {
    dispatch({
      type: LINK_DELETE_FAIL,
      payload:
        (error as $TSFixMe).response && (error as $TSFixMe).response.data.detail
          ? (error as $TSFixMe).response.data.detail
          : (error as $TSFixMe).message,
    });
  }
};

export const createLink =
  (link: $TSFixMe) => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      dispatch({ type: LINK_CREATE_REQUEST });

      const { data } = await axios.post(
        `/connect/link/create`,
        JSON.stringify(link)
      );

      dispatch({
        type: LINK_CREATE_SUCCESS,
        payload: data,
      });
      return data;
    } catch (error) {
      dispatch({
        type: LINK_CREATE_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
      return error;
    }
  };

export const putLinkEditorCode =
  (link: any, code: string) =>
  async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    dispatch({
      type: PUT_LINK_EDITOR_CODE,
      payload: code,
    });
  };
