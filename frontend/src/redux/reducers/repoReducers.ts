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

interface TPanel {
  showDetailedLogs: boolean;
  previewId: string;
  status: boolean;
  loading: boolean;
  data: any;
  error: any;
}

export const previewBuildPanelReducer = (
  state: Record<any, TPanel> = {},
  action: any
) => {
  switch (action.type) {
    case TOGGLE_PREVIEW_PANEL:
      return {
        ...state,
        [action.id]: {
          ...state[action.id],
          status:
            action.payload != undefined
              ? action.payload
              : !state[action.id].status,
        },
      };

    case PREVIEW_DATA_REQUEST:
      return {
        ...state,
        [action.id]: {
          ...state[action.id],
          previewId: undefined,
          error: undefined,
          showDetailedLogs: action.showDetailedLogs,
          loading: true,
        },
      };

    case PREVIEW_DATA_SUCCESS:
      return {
        ...state,
        [action.id]: {
          ...state[action.id],
          loading: false,
          data: action.payload,
        },
      };

    case PREVIEW_DATA_FAIL:
      return {
        ...state,
        [action.id]: {
          ...state[action.id],
          loading: false,
          error: action.payload,
        },
      };

    case PREVIEW_ID_UPDATE:
      return {
        ...state,
        [action.id]: {
          ...state[action.id],
          previewId: action.payload,
        },
      };

    default:
      return state;
  }
};

export const repoDetailsReducer = (state = {}, action: $TSFixMe) => {
  switch (action.type) {
    case REPO_DETAILS_REQUEST:
      return { loading: true, ...state };

    case REPO_DETAILS_SUCCESS:
      return { loading: false, repo: action.payload };

    case REPO_DETAILS_FAIL:
      return { loading: false, error: action.payload };

    default:
      return state;
  }
};

export const repoNodeDataReducer = (
  state: $TSFixMe = { node: {} },
  action: $TSFixMe
) => {
  switch (action.type) {
    case REPO_NODE_DATA_REQUEST:
      return { loading: true, node: [] };

    case REPO_NODE_DATA_SUCCESS:
      return {
        loading: false,
        node: action.payload,
      };

    case REPO_NODE_DATA_FAIL:
      return { loading: false, error: action.payload };

    default:
      return state;
  }
};
