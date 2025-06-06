import axios from "axios";
import { getOnlyIcon, isDefined, notEmpty } from "utils/utilities";
import {
  FOLDER_CREATE_FAIL,
  FOLDER_CREATE_REQUEST,
  FOLDER_CREATE_SUCCESS,
  FOLDER_DETAILS_FAIL,
  FOLDER_DETAILS_REQUEST,
  FOLDER_DETAILS_SUCCESS,
  HEADER_DATA_FAIL,
  HEADER_DATA_REQUEST,
  HEADER_DATA_SUCCESS,
  PROJECT_CREATE_FAIL,
  PROJECT_CREATE_REQUEST,
  PROJECT_CREATE_SUCCESS,
  PROJECT_LIST_FAIL,
  PROJECT_LIST_REQUEST,
  PROJECT_LIST_SUCCESS,
} from "../constants/projectConstants";

export const listProjects =
  () => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      dispatch({ type: PROJECT_LIST_REQUEST });

      const { data } = await axios.get(`/kitab/project/all`);

      dispatch({
        type: PROJECT_LIST_SUCCESS,
        payload: data,
      });
      return data;
    } catch (error) {
      dispatch({
        type: PROJECT_LIST_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
      return error;
    }
  };

export const createProject =
  (project: $TSFixMe) => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      dispatch({ type: PROJECT_CREATE_REQUEST });

      const { data } = await axios.post(
        `/kitab/project/create`,
        JSON.stringify(project)
      );

      dispatch({
        type: PROJECT_CREATE_SUCCESS,
        payload: data,
      });
    } catch (error) {
      dispatch({
        type: PROJECT_CREATE_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.message
            ? (error as $TSFixMe).response.data.message
            : (error as $TSFixMe).message,
      });
    }
  };

export const listFolderDetails =
  (id: $TSFixMe) => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      dispatch({ type: FOLDER_DETAILS_REQUEST });

      const { data } = await axios.get(`/kitab/folder/children/${id}/active`);

      try {
        await Promise.all(
          data.map(async (item: any) => {
            await getOnlyIcon(item.id, item.type, item.subType).then(
              (result: any) => {
                item.icon = result;
              }
            );
          })
        );
        dispatch({
          type: FOLDER_DETAILS_SUCCESS,
          payload: data,
        });
        return data;
      } catch (error) {
        dispatch({
          type: FOLDER_DETAILS_SUCCESS,
          payload: data,
        });
        return data;
      }

      dispatch({
        type: FOLDER_DETAILS_SUCCESS,
        payload: data,
      });
      return data;
    } catch (error) {
      dispatch({
        type: FOLDER_DETAILS_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
      return error;
    }
  };
export const getChildrenFolders =
  (id: $TSFixMe) => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      const { data } = await axios.get(
        `/kitab/folder/children/${id}/folderOnly`
      );
      return data;
    } catch (err) {
      return err;
    }
  };
export const getDatasetAndFolders =
  (id: $TSFixMe) => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      const { data } = await axios.get(
        `/kitab/folder/children/${id}/datasetsOnlyAndFolders`
      );
      return data;
    } catch (err) {
      return err;
    }
  };
export const getDashboardsAndFolders =
  (id: $TSFixMe) => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      const { data } = await axios.get(
        `/kitab/folder/children/${id}/dashboardsOnlyAndFolders`
      );
      return data;
    } catch (err) {
      return err;
    }
  };

export const headerLinks =
  (id: $TSFixMe) => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      if (notEmpty(id)) {
        dispatch({ type: HEADER_DATA_REQUEST });

        const { data } = await axios.get(`/kitab/${id}/getPath`);

        dispatch({
          type: HEADER_DATA_SUCCESS,
          payload: data,
        });
      }
    } catch (error) {
      dispatch({
        type: HEADER_DATA_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
    }
  };
