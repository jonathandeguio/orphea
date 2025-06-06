import {
  INITIATE_SOURCE_PREVIEW,
  QUERY_SOURCE,
  SOURCE_DELETE_FAIL,
  SOURCE_DELETE_REQUEST,
  SOURCE_DELETE_SUCCESS,
  SOURCE_LINKS_LIST_FAIL,
  SOURCE_LINKS_LIST_REQUEST,
  SOURCE_LINKS_LIST_SUCCESS,
  SOURCE_LIST_FAIL,
  SOURCE_LIST_REQUEST,
  SOURCE_LIST_SUCCESS,
} from "../constants/sourceConstants";

export const sourceListReducer = (
  state: $TSFixMe = { source: [] },
  action: $TSFixMe
) => {
  switch (action.type) {
    case SOURCE_LIST_REQUEST:
      return { sourcesLoading: true, sources: [] };

    case SOURCE_LIST_SUCCESS:
      return {
        sourcesLoading: false,
        sources: action.payload,
      };
    case SOURCE_LIST_FAIL:
      return { sourcesLoading: false, error: action.payload };
    default:
      return state;
  }
};

export const sourceDeleteReducer = (
  state: $TSFixMe = { source: [] },
  action: $TSFixMe
) => {
  switch (action.type) {
    case SOURCE_DELETE_REQUEST:
      return { sourcesLoading: true, sources: [] };

    case SOURCE_DELETE_SUCCESS:
      return {
        sourcesLoading: false,
        sources: action.payload,
      };
    case SOURCE_DELETE_FAIL:
      return { sourcesLoading: false, error: action.payload };
    default:
      return state;
  }
};

export const sourceLinksListReducer = (
  state: $TSFixMe = { sourceLinks: [] },
  action: $TSFixMe
) => {
  switch (action.type) {
    case SOURCE_LINKS_LIST_REQUEST:
      return { loading: true, sourceLinks: [] };

    case SOURCE_LINKS_LIST_SUCCESS:
      return {
        loading: false,
        sourceLinks: action.payload,
      };
    case SOURCE_LINKS_LIST_FAIL:
      return { loading: false, error: action.payload };
    default:
      return state;
  }
};

export const sourceOpsReducer = (
  state: $TSFixMe = {
    querySource: {
      sourceId: undefined,
      code: undefined,
    },
    previewSource: {
      sourceId: undefined,
      code: undefined,
    },
  },
  action: $TSFixMe
) => {
  switch (action.type) {
    case INITIATE_SOURCE_PREVIEW:
      return {
        ...state,
        previewSource: action.payload,
      };

    case QUERY_SOURCE:
      return {
        ...state,
        querySource: action.payload.append
          ? {
              ...action.payload,
              code: state.querySource.code + "\n " + action.payload.code,
            }
          : action.payload,
      };

    default:
      return state;
  }
};
