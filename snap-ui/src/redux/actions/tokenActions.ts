import {
  REFRESH_TOKEN_STATUS,
  SET_INVALID_TOKEN,
  SET_VALID_TOKEN,
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
