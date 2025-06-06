import {
  PROD_FAIL,
  PROD_REQUEST,
  PROD_SUCCESS,
  REFRESH_TOKEN_STATUS,
  SET_INVALID_TOKEN,
  SET_VALID_TOKEN,
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

export const configReducer = (state = {}, action: any) => {
  switch (action.type) {
    case PROD_REQUEST:
      return { isDev: true };
    case PROD_SUCCESS:
      return {
        isDev: action.payload.development,
      };

    case PROD_FAIL:
      return { isDev: true };

    default:
      return state;
  }
};
