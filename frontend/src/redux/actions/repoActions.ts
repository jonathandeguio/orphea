import axios from "axios";
import { TBuildLogMessage } from "components/Builds/Builds.types";
import {
  PREVIEW_DATA_FAIL,
  PREVIEW_DATA_REQUEST,
  PREVIEW_DATA_SUCCESS,
  PREVIEW_ID_UPDATE,
  REPO_DETAILS_FAIL,
  REPO_DETAILS_REQUEST,
  REPO_DETAILS_SUCCESS,
  REPO_NODE_DATA_FAIL,
  REPO_NODE_DATA_REQUEST,
  REPO_NODE_DATA_SUCCESS,
  TOGGLE_PREVIEW_PANEL,
} from "../constants/repoConstants";

const sleep = (ms: number | undefined) => new Promise((r) => setTimeout(r, ms));

// const BASE_URL = process.env.REACT_APP_BASE_URL;

export const togglePreviewBuildPanel =
  (id: string, state?: boolean) => (dispatch: any, getState: any) => {
    dispatch({ type: TOGGLE_PREVIEW_PANEL, payload: state, id: id });
  };

export const initiatePreview =
  (id: string, showDetailedLogs: boolean) => (dispatch: any, getState: any) => {
    dispatch({
      type: PREVIEW_DATA_REQUEST,
      id: id,
      showDetailedLogs: showDetailedLogs,
    });
  };

export const updatePreviewId =
  (previewId: string, id: string) => (dispatch: any, getState: any) => {
    dispatch({ type: PREVIEW_ID_UPDATE, payload: previewId, id: id });
  };

export const errorPreview =
  (error: TBuildLogMessage, id: string) => (dispatch: any, getState: any) => {
    dispatch({ type: PREVIEW_DATA_FAIL, payload: error, id: id });
  };

export const getPreviewResult =
  (data: any, id: string) => (dispatch: any, getState: any) => {
    dispatch({ type: PREVIEW_DATA_SUCCESS, payload: data, id: id });
  };

export const listRepoDetails =
  (id: $TSFixMe) => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      dispatch({ type: REPO_DETAILS_REQUEST });

      const { data } = await axios.get(`/kitab/${id}`);

      dispatch({
        type: REPO_DETAILS_SUCCESS,
        payload: data,
      });
    } catch (error) {
      dispatch({
        type: REPO_DETAILS_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
    }
  };

export const nodeDataRepo =
  (id: $TSFixMe, branch: $TSFixMe, name: $TSFixMe, path: $TSFixMe) =>
  async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      dispatch({ type: REPO_NODE_DATA_REQUEST });

      const { data } = await axios.get(
        `/fractal/${id}/${branch}/viewFileContent/workingTree?filePath=` + path
      );

      dispatch({
        type: REPO_NODE_DATA_SUCCESS,
        payload: data,
      });
    } catch (error) {
      dispatch({
        type: REPO_NODE_DATA_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
    }
  };
