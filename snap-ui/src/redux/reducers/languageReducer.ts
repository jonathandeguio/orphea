import {
  LANGUAGE_FETCH_REQUEST,
  LANGUAGE_FETCH_SUCCESS,
  LANGUAGE_FETCH_FAIL,
  LANGUAGE_UPDATE_REQUEST,
  LANGUAGE_UPDATE_SUCCESS,
  LANGUAGE_UPDATE_FAIL,
} from "../constants/languageConstants";

export const getlanguageReducer = (
  state = { loading: false, language: "en", error: "NO ERRO" },
  action: $TSFixMe
) => {
  switch (action.type) {
    case LANGUAGE_FETCH_REQUEST:
      return { ...state, loading: true };

    case LANGUAGE_FETCH_SUCCESS:
      return { ...state, loading: false };

    case LANGUAGE_FETCH_FAIL:
      return { ...state, loading: false, error: "Cannot update language" };

    case LANGUAGE_UPDATE_REQUEST:
      return { ...state, loading: true };

    case LANGUAGE_UPDATE_SUCCESS:
      return { ...state, loading: false, language: action.payload };

    case LANGUAGE_UPDATE_FAIL:
      return { ...state, loading: false, error: "Cannot update language" };

    default:
      return state;
  }
};
