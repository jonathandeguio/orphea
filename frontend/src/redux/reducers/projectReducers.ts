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
  PROJECT_DETAILS_FAIL,
  PROJECT_DETAILS_REQUEST,
  PROJECT_DETAILS_SUCCESS,
  PROJECT_LIST_FAIL,
  PROJECT_LIST_REQUEST,
  PROJECT_LIST_SUCCESS,
} from "../constants/projectConstants";

export const projectListReducer = (
  state: $TSFixMe = { projects: [] },
  action: $TSFixMe
) => {
  switch (action.type) {
    case PROJECT_LIST_REQUEST:
      return { loading: true, projects: state.projects };

    case PROJECT_LIST_SUCCESS:
      return {
        loading: false,
        projects: action.payload,
      };

    case PROJECT_LIST_FAIL:
      return { loading: false, error: action.payload };

    default:
      return state;
  }
};

export const projectCreateReducer = (state = {}, action: $TSFixMe) => {
  switch (action.type) {
    case PROJECT_CREATE_REQUEST:
      return { loading: true };

    case PROJECT_CREATE_SUCCESS:
      return { loading: false, success: true, project: action.payload };

    case PROJECT_CREATE_FAIL:
      return { loading: false, error: action.payload };

    default:
      return state;
  }
};

export const folderCreateReducer = (state = {}, action: $TSFixMe) => {
  switch (action.type) {
    case FOLDER_CREATE_REQUEST:
      return { loading: true };

    case FOLDER_CREATE_SUCCESS:
      return { loading: false, success: true, folder: action.payload };

    case FOLDER_CREATE_FAIL:
      return { loading: false, error: action.payload };

    default:
      return state;
  }
};

export const projectDetailsReducer = (state = {}, action: $TSFixMe) => {
  switch (action.type) {
    case PROJECT_DETAILS_REQUEST:
      return { loading: true, ...state };

    case PROJECT_DETAILS_SUCCESS:
      return { loading: false, project: action.payload };

    case PROJECT_DETAILS_FAIL:
      return { loading: false, error: action.payload };

    default:
      return state;
  }
};

export const folderDetailsReducer = (state = {}, action: $TSFixMe) => {
  switch (action.type) {
    case FOLDER_DETAILS_REQUEST:
      return { loading: true, folder: undefined, error: undefined };

    case FOLDER_DETAILS_SUCCESS:
      return { loading: false, folder: action.payload };

    case FOLDER_DETAILS_FAIL:
      return { loading: true, error: action.payload };

    default:
      return state;
  }
};

export const headerLinkReducer = (
  state = { loading: true, links: [], error: false },
  action: $TSFixMe
) => {
  switch (action.type) {
    case HEADER_DATA_REQUEST:
      return { loading: true, links: [], error: false };

    case HEADER_DATA_SUCCESS:
      return {
        loading: false,
        links: action.payload,
        error: false,
      };

    case HEADER_DATA_FAIL:
      return {
        loading: false,
        error: true,
        errorContent: action.payload,
        links: [],
      };

    default:
      return state;
  }
};
