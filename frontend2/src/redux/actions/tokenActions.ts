import axios from "axios";
import {
  TOKEN_CREATE_FAIL,
  TOKEN_CREATE_REQUEST,
  TOKEN_CREATE_SUCCESS,
  TOKEN_LIST_FAIL,
  TOKEN_LIST_REQUEST,
  TOKEN_LIST_SUCCESS,
  SET_INVALID_TOKEN,
  SET_VALID_TOKEN,
  REFRESH_TOKEN_STATUS
} from "../constants/tokenConstants";

// const BASE_URL = process.env.REACT_APP_BASE_URL;
const BASE_URL_API = process.env.REACT_APP_BASE_URL_API;

export const refreshTokenStatus = () => {
  return ({type: REFRESH_TOKEN_STATUS});
}
export const setTokenValid = () => {
  return ({type: SET_VALID_TOKEN});
}
export const setTokenInvalid = () => {
  return ({type: SET_INVALID_TOKEN});
}


export const listTokens = () => async (dispatch: any, getState: any) => {
  try {
    dispatch({ type: TOKEN_LIST_REQUEST });

    const boslerToken = localStorage.getItem("boslerToken");

    const config = {
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${boslerToken}`,
      },
    };

    const { data } = await axios.get(
      `${BASE_URL_API}/passport/token/GetLongLived`,
      config
    );

    dispatch({
      type: TOKEN_LIST_SUCCESS,
      payload: data,
    });

    //
  } catch (error) {
    dispatch({
      type: TOKEN_LIST_FAIL,
      payload:
        (error as any).response && (error as any).response.data.detail
          ? (error as any).response.data.detail
          : (error as any).message,
    });
  }
};

export const createToken = ({
      name,
      expiry
    }: any) =>
    async (dispatch: any, getState: any) => {
      try {
        //
        //
        dispatch({ type: TOKEN_CREATE_REQUEST });

        const boslerToken = localStorage.getItem("boslerToken");

        const config = {
          headers: {
            "Content-type": "application/json",
            Authorization: `Bearer ${boslerToken}`,
          },
        };

        const { data } = await axios.post(
          `${BASE_URL_API}/passport/token/CreateLongLived`,
          JSON.stringify({ name: name, expiration: expiry }),
          config
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
            (error as any).response &&
            (error as any).response.data.detail
              ? (error as any).response.data.detail
              : (error as any).message,
        });
      }
    };
