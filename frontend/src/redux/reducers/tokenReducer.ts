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

export const tokenStatus = (
  state = { isTokenValid: false, loading: true },
  action: $TSFixMe
) => {
  switch (action.type) {
    case REFRESH_TOKEN_STATUS:
      return { isTokenValid: true, loading: true };
    case SET_INVALID_TOKEN:
      return { isTokenValid: false, loading: false };
    case SET_VALID_TOKEN:
      return { isTokenValid: true, loading: false };
    default:
      return state;
  }
};

export const tokenCreateReducer = (
  state: $TSFixMe = { tokens: "" },
  action: $TSFixMe
) => {
  switch (action.type) {
    case TOKEN_CREATE_REQUEST:
      return { loading: true, tokens: "" };

    case TOKEN_CREATE_SUCCESS:
      return {
        loading: false,
        success: true,
        tokens: action.payload.accessToken,
      };

    case TOKEN_CREATE_FAIL:
      return { loading: false, error: action.payload };

    default:
      return state;
  }
};

export const tokenListReducer = (state = {}, action: $TSFixMe) => {
  switch (action.type) {
    case TOKEN_LIST_REQUEST:
      return { loading: true, ...state };
    case TOKEN_LIST_SUCCESS:
      return { loading: false, tokens: action.payload };

    case TOKEN_LIST_FAIL:
      return { loading: false, error: action.payload };

    default:
      return state;
  }
};
