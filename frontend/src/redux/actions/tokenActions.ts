import axios from "axios";
import {
  REFRESH_TOKEN_STATUS,
  SET_INVALID_TOKEN,
  SET_VALID_TOKEN,
  TOKEN_CREATE_FAIL,
  TOKEN_CREATE_REQUEST,
  TOKEN_CREATE_SUCCESS,
  TOKEN_LIST_FAIL,
  TOKEN_LIST_REQUEST,
  TOKEN_LIST_SUCCESS,
} from "../constants/tokenConstants";

// const BASE_URL = process.env.REACT_APP_BASE_URL;

export const refreshTokenStatus = () => {
  return { type: REFRESH_TOKEN_STATUS };
};
export const setTokenValid = () => {
  return { type: SET_VALID_TOKEN };
};
export const setTokenInvalid = () => {
  return { type: SET_INVALID_TOKEN };
};

export const listTokens =
  () => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      dispatch({ type: TOKEN_LIST_REQUEST });

      const { data } = await axios.get(`/passport/token/GetLongLived`);

      dispatch({
        type: TOKEN_LIST_SUCCESS,
        payload: data,
      });

      //
    } catch (error) {
      dispatch({
        type: TOKEN_LIST_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
    }
  };

export const createToken =
  ({ name, expiry }: $TSFixMe) =>
  async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      dispatch({ type: TOKEN_CREATE_REQUEST });

      const { data } = await axios.post(
        `/passport/token/CreateLongLived`,
        JSON.stringify({ name: name, expiration: expiry })
      );

      dispatch({
        type: TOKEN_CREATE_SUCCESS,
        payload: data,
      });
    } catch (error) {
      //
      dispatch({
        type: TOKEN_CREATE_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
    }
  };
