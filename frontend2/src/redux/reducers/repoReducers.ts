import {
  REPO_CREATE_FAIL,
  REPO_CREATE_REQUEST,
  REPO_CREATE_SUCCESS,
  REPO_DETAILS_FAIL,
  REPO_DETAILS_REQUEST,
  REPO_DETAILS_SUCCESS,
  REPO_NODE_DATA_FAIL,
  REPO_NODE_DATA_REQUEST,
  REPO_NODE_DATA_SUCCESS,
  REPO_TREE_FAIL,
  REPO_TREE_REQUEST,
  REPO_TREE_SUCCESS,
} from "../constants/repoConstants";

export const repoCreateReducer = (state = {}, action: any) => {
  switch (action.type) {
    case REPO_CREATE_REQUEST:
      return { loading: true };

    case REPO_CREATE_SUCCESS:
      return { loading: false, success: true, repo: action.payload };

    case REPO_CREATE_FAIL:
      return { loading: false, error: action.payload };

    default:
      return state;
  }
};

export const repoDetailsReducer = (state = {}, action: any) => {
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

export const repoTreeReducer = (state : any = { tree: [] }, action: any) => {
  switch (action.type) {
    case REPO_TREE_REQUEST:
      return { loading: true, tree: [] };

    case REPO_TREE_SUCCESS:
      return {
        loading: false,
        tree: action.payload,
      };

    case REPO_TREE_FAIL:
      return { loading: false, error: action.payload };

    default:
      return state;
  }
};

export const repoNodeDataReducer = (state : any = { node: {} }, action: any) => {
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
