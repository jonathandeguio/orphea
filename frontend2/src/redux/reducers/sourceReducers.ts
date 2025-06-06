import {
  SOURCE_CREATE_FAIL,
  SOURCE_CREATE_REQUEST,
  SOURCE_CREATE_SUCCESS,
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

export const sourceListReducer = (state : any = { source: [] }, action: any) => {
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
  state : any = { source: [] },
  action: any
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

export const sourceCreateReducer = (state = {}, action: any) => {
  switch (action.type) {
    case SOURCE_CREATE_REQUEST:
      return { sourcesLoading: true };

    case SOURCE_CREATE_SUCCESS:
      return { sourcesLoading: false, success: true, sources: action.payload };

    case SOURCE_CREATE_FAIL:
      return { sourcesLoading: false, error: action.payload };

    default:
      return state;
  }
};
export const sourceLinksListReducer = (state : any = { sourceLinks: [] }, action: any) => {
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
