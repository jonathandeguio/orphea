import {
  ENVOY_CREATE_FAIL,
  ENVOY_CREATE_REQUEST,
  ENVOY_CREATE_SUCCESS,
  ENVOY_DELETE_FAIL,
  ENVOY_DELETE_REQUEST,
  ENVOY_DELETE_SUCCESS,
  ENVOY_LINKS_LIST_FAIL,
  ENVOY_LINKS_LIST_REQUEST,
  ENVOY_LINKS_LIST_SUCCESS,
  ENVOY_LIST_FAIL,
  ENVOY_LIST_REQUEST,
  ENVOY_LIST_SUCCESS,
  ENVOY_SOURCES_LIST_FAIL,
  ENVOY_SOURCES_LIST_REQUEST,
  ENVOY_SOURCES_LIST_SUCCESS,
} from "../constants/agentConstants";

export const agentListReducer = (state : any = { agent: [] }, action: any) => {
  switch (action.type) {
    case ENVOY_LIST_REQUEST:
      return { loading: true, agent: [] };

    case ENVOY_LIST_SUCCESS:
      return {
        loading: false,
        agents: action.payload,
      };
    case ENVOY_LIST_FAIL:
      return { loading: false, error: action.payload };
    default:
      return state;
  }
};

export const agentDeleteReducer = (state : any = { agent: [] }, action: any) => {
  switch (action.type) {
    case ENVOY_DELETE_REQUEST:
      return { loading: true, agent: [] };

    case ENVOY_DELETE_SUCCESS:
      return {
        loading: false,
        agents: action.payload,
      };
    case ENVOY_DELETE_FAIL:
      return { loading: false, error: action.payload };
    default:
      return state;
  }
};

export const agentCreateReducer = (state = {}, action: any) => {
  switch (action.type) {
    case ENVOY_CREATE_REQUEST:
      return { loading: true };

    case ENVOY_CREATE_SUCCESS: {
      return {
        createdAgentLoading: false,
        success: true,
        createdAgent: action.payload,
      };
    }
    case ENVOY_CREATE_FAIL:
      return { loading: false, error: action.payload };

    default:
      return state;
  }
};

export const agentSourcesListReducer = (
  state : any = { agentSources: [] },
  action: any
) => {
  switch (action.type) {
    case ENVOY_SOURCES_LIST_REQUEST:
      return { loading: true, agentSources: [] };

    case ENVOY_SOURCES_LIST_SUCCESS:
      return {
        loading: false,
        agentSources: action.payload,
      };
    case ENVOY_SOURCES_LIST_FAIL:
      return { loading: false, error: action.payload };
    default:
      return state;
  }
};
export const agentLinksListReducer = (state : any = { agentLinks: [] }, action: any) => {
  switch (action.type) {
    case ENVOY_LINKS_LIST_REQUEST:
      return { loading: true, agentLinks: [] };

    case ENVOY_LINKS_LIST_SUCCESS:
      return {
        loading: false,
        agentLinks: action.payload,
      };
    case ENVOY_LINKS_LIST_FAIL:
      return { loading: false, error: action.payload };
    default:
      return state;
  }
};
