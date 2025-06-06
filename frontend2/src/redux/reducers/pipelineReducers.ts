import {
  PIPELINE_DETAILS_FAIL,
  PIPELINE_DETAILS_REQUEST,
  PIPELINE_DETAILS_SUCCESS,
  PIPELINE_NAME_REQUEST,
  PIPELINE_NAME_SUCCESS,
  PIPELINE_SCHEMA_FAIL,
  PIPELINE_SCHEMA_REQUEST,
  PIPELINE_SCHEMA_SUCCESS,
  // PIPELINE_CREATE_REQUEST,
  // PIPELINE_CREATE_SUCCESS,
  // PIPELINE_CREATE_FAIL,
  // PIPELINE_TREE_REQUEST,
  // PIPELINE_TREE_SUCCESS,
  // PIPELINE_TREE_FAIL,
  // PIPELINE_NODE_DATA_REQUEST,
  // PIPELINE_NODE_DATA_SUCCESS,
  // PIPELINE_NODE_DATA_FAIL,
  PIPELINE_SELECTED_NODE,
  PIPELINE_SOURCE_FAIL,
  PIPELINE_SOURCE_REQUEST,
  PIPELINE_SOURCE_SUCCESS,
  SCHEDULE_ADD_FAIL,
  SCHEDULE_ADD_REQUEST,
  SCHEDULE_ADD_SUCCESS,
  SCHEDULE_DELETE_FAIL,
  SCHEDULE_DELETE_REQUEST,
  SCHEDULE_DELETE_SUCCESS,
  SCHEDULE_DETAILS_FAIL,
  SCHEDULE_DETAILS_REQUEST,
  SCHEDULE_DETAILS_SUCCESS,
  SCHEDULE_UPDATE_FAIL,
  SCHEDULE_UPDATE_REQUEST,
  SCHEDULE_UPDATE_SUCCESS,
} from "../constants/pipelineConstants";

export const pinelineDetailsReducer = (
  state = { node: { name: "DEFAULT STATE" } },
  action: any
) => {
  switch (action.type) {
    case PIPELINE_DETAILS_REQUEST:
      return { loading: true, ...state };

    case PIPELINE_DETAILS_SUCCESS:
      return { ...state, loading: false, pipeline: action.payload };

    case PIPELINE_DETAILS_FAIL:
      return { ...state, loading: false, error: action.payload };

    case PIPELINE_SELECTED_NODE:
      return { ...state, loading: false, node: action.payload };

    case SCHEDULE_DETAILS_REQUEST:
      return { ...state, loading: true };

    case SCHEDULE_DETAILS_SUCCESS:
      return { ...state, loading: false, schedule: action.payload };

    case SCHEDULE_DETAILS_FAIL:
      return { ...state, loading: false, error: action.payload };

    case SCHEDULE_ADD_REQUEST:
      return { ...state, loading: true };

    case SCHEDULE_ADD_SUCCESS:
      return {
        ...state,
        loading: false,
        schedule_request: action.payload,
      };

    case SCHEDULE_ADD_FAIL:
      return { ...state, loading: false, error: action.payload };

    case SCHEDULE_UPDATE_REQUEST:
      return { ...state, loading: true };

    case SCHEDULE_UPDATE_SUCCESS:
      return {
        ...state,
        loading: false,
        schedule_request: action.payload,
      };

    case SCHEDULE_UPDATE_FAIL:
      return { ...state, loading: false, error: action.payload };

    case SCHEDULE_DELETE_REQUEST:
      return {
        ...state,
        node: state.node,
        pipeline: (state as any).pipeline,
        loading: true,
      };

    case SCHEDULE_DELETE_SUCCESS:
      return {
        ...state,
        node: state.node,
        loading: false,
        schedule: null,
        schedule_request: action.payload,
      };

    case SCHEDULE_DELETE_FAIL:
      return { ...state, node: state.node, loading: false, error: action.payload };

    case PIPELINE_SOURCE_REQUEST:
      return { ...state, loading: true };

    case PIPELINE_SOURCE_SUCCESS:
      return {
        ...state,
        loading: false,
        pipeline_source: action.payload,
      };

    case PIPELINE_SOURCE_FAIL:
      return { ...state, loading: false, error: action.payload };

    case PIPELINE_NAME_REQUEST:
      return { ...state, loading: true };

    case PIPELINE_NAME_SUCCESS:
      return {
        ...state,
        loading: false,
        pipeline_name: action.payload,
      };
    default:
      return state;
  }
};

export const schemeDetailsReducer = (
  state = { schemaDetails: undefined, loading: true, error: false },
  action: any
) => {
  switch (action.type) {
    case PIPELINE_SCHEMA_REQUEST:
      return {
        schemaDetails: state.schemaDetails,
        loading: true,
        error: false,
      };

    case PIPELINE_SCHEMA_SUCCESS:
      return {
        schemaDetails: action.payload,
        loading: false,
        error: false,
      };

    case PIPELINE_SCHEMA_FAIL:
      return {
        schemaDetails: undefined,
        loading: false,
        error: action.payload,
      };

    default:
      return state;
  }
};
