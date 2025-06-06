import axios from "axios";
import {
  INITIATE_SOURCE_PREVIEW,
  QUERY_SOURCE,
  SOURCE_DELETE_FAIL,
  SOURCE_DELETE_REQUEST,
  SOURCE_DELETE_SUCCESS,
  SOURCE_LINKS_LIST_FAIL,
  SOURCE_LINKS_LIST_REQUEST,
  SOURCE_LINKS_LIST_SUCCESS,
  SOURCE_LIST_FAIL,
  SOURCE_LIST_REQUEST,
  SOURCE_LIST_SUCCESS,
} from "../constants/sourceConstants";

export const listSources =
  () => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      dispatch({ type: SOURCE_LIST_REQUEST });

      const { data } = await axios.get(`/connect/source/Getall`);

      dispatch({
        type: SOURCE_LIST_SUCCESS,
        payload: data,
      });
    } catch (error) {
      dispatch({
        type: SOURCE_LIST_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
    }
  };

export const deleteSource = (id: $TSFixMe) => async (dispatch: $TSFixMe) => {
  try {
    dispatch({ type: SOURCE_DELETE_REQUEST });

    const { data } = await axios.delete(`/connect/source/DeleteById/${id}`);

    dispatch({
      type: SOURCE_DELETE_SUCCESS,
      payload: data,
    });
  } catch (error) {
    dispatch({
      type: SOURCE_DELETE_FAIL,
      payload:
        (error as $TSFixMe).response && (error as $TSFixMe).response.data.detail
          ? (error as $TSFixMe).response.data.detail
          : (error as $TSFixMe).message,
    });
  }
};

export const getSourceById =
  (id: $TSFixMe) => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      const { data } = await axios.get(`/connect/source/GetById/${id}`);
      return data;
    } catch (err) {
      return err;
    }
  };

export const listSourceLinks =
  (sourceId: $TSFixMe) => async (dispatch: $TSFixMe) => {
    try {
      dispatch({ type: SOURCE_LINKS_LIST_REQUEST });
      const { data } = await axios.get(`/connect/source/${sourceId}/links`);

      dispatch({
        type: SOURCE_LINKS_LIST_SUCCESS,
        payload: data,
      });
      return data;
    } catch (error) {
      dispatch({
        type: SOURCE_LINKS_LIST_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
    }
  };

export const updatePreviewQuery =
  (sourceId: $TSFixMe, code: string) => async (dispatch: $TSFixMe) => {
    dispatch({
      type: INITIATE_SOURCE_PREVIEW,
      payload: {
        sourceId: sourceId,
        code: code,
      },
    });
  };

export const updateSourceQuery =
  (sourceId: $TSFixMe, code: string, append: boolean = false) =>
  async (dispatch: $TSFixMe) => {
    dispatch({
      type: QUERY_SOURCE,
      payload: {
        sourceId: sourceId,
        code: code,
        append: append,
      },
    });
  };
