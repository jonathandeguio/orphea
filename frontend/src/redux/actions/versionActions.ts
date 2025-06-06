import { RootState, ThunkAppDispatch } from "redux/types/store";
import {
  VERSION_AUTOSAVE_CHECK,
  VERSION_CHART_CHANGE,
  VERSION_DASH_CHANGE,
  VERSION_UPDATE_SUCCESS,
} from "../constants/versionConstants";
export const versionUpdate =
  () => async (dispatch: ThunkAppDispatch, getState: RootState) => {
    dispatch({
      type: VERSION_UPDATE_SUCCESS,
    });
  };

export const changeVersionDash =
  (payload: any) => async (dispatch: ThunkAppDispatch, getState: RootState) => {
    dispatch({
      type: VERSION_DASH_CHANGE,
      payload: payload,
    });
  };

export const changeVersionChart =
  (payload: any) => async (dispatch: ThunkAppDispatch, getState: RootState) => {
    dispatch({
      type: VERSION_CHART_CHANGE,
      payload: payload,
    });
  };

export const autoSaveHandler =
  (payload: any) => async (dispatch: ThunkAppDispatch, getState: RootState) => {
    dispatch({
      type: VERSION_AUTOSAVE_CHECK,
      payload: payload,
    });
  };
