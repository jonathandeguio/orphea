import {
  QUERY_FETCH_REQUEST,
  QUERY_FETCH_SUCCESS,
  QUERY_FETCH_FAIL,
  QUERY_UPDATE_REQUEST,
  QUERY_UPDATE_SUCCESS,
  QUERY_UPDATE_FAIL,
  QUERY_SLIDER_UPDATE_REQUEST,
  QUERY_SLIDER_UPDATE_SUCCESS,
  QUERY_SLIDER_UPDATE_FAIL,
} from "../constants/queryConstants";

export const getQueryReducer = (state = {"query" : {}}, action : any) => {
  switch (action.type) {
    case QUERY_FETCH_REQUEST: {
      return { loading: true, query: state.query, error: undefined };
    }
    case QUERY_FETCH_SUCCESS: {
      return { loading: false, query: {...state.query, ...action.payload}, error: undefined };
    }
    case QUERY_FETCH_FAIL: {
      return { laoding: false, query: state.query, error: action.payload };
    }
    case QUERY_UPDATE_REQUEST: {
      return { loading: false, query: state.query, error: undefined };
    }
    case QUERY_UPDATE_SUCCESS: {
      console.log("UPDATE QUERY : " , {...state.query, ...action.payload})
      return { loading: false, query: {...state.query, ...action.payload}, error: undefined };
    }
    case QUERY_UPDATE_FAIL: {
      return { laoding: false, query: state.query, error: action.payload.err };
    }
    case QUERY_SLIDER_UPDATE_REQUEST: {
      return { loading: false, query: state.query, error: undefined };
    }
    case QUERY_SLIDER_UPDATE_SUCCESS: {
      console.log("UPDATE QUERY : " , action.payload)
      return { loading: false, query: action.payload, error: undefined };
    }
    case QUERY_SLIDER_UPDATE_FAIL: {
      return { laoding: false, query: state.query, error: action.payload.err };
    }
    default:
      return state;
  }
};
