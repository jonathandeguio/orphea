import {
  LANGUAGE_FETCH_REQUEST,
  LANGUAGE_FETCH_SUCCESS,
  LANGUAGE_FETCH_FAIL,
  LANGUAGE_UPDATE_REQUEST,
  LANGUAGE_UPDATE_SUCCESS,
  LANGUAGE_UPDATE_FAIL,
} from "../constants/languageConstants";

import { ThunkAppDispatch, RootState } from "../types/store";

export const getLanguage =
  () => async (dispatch: ThunkAppDispatch, getState: RootState) => {
    try {
      dispatch({
        type: LANGUAGE_FETCH_REQUEST,
      });

      dispatch({
        type: LANGUAGE_FETCH_SUCCESS,
      });
    } catch (err) {
      dispatch({
        type: LANGUAGE_FETCH_FAIL,
      });
    }
  };

export const updateLanguage =
  (newLanguage: string) =>
  async (dispatch: ThunkAppDispatch, getState: RootState) => {
    try {
      dispatch({
        type: LANGUAGE_UPDATE_REQUEST,
      });

      dispatch({
        type: LANGUAGE_UPDATE_SUCCESS,
        payload: newLanguage,
      });
    } catch (err) {
      dispatch({
        type: LANGUAGE_UPDATE_FAIL,
      });
    }
  };
