import {
  DELETE_PERMISSIONS_MAPPING_FAIL,
  DELETE_PERMISSIONS_MAPPING_REQUEST,
  DELETE_PERMISSIONS_MAPPING_SUCCESS,
  GROUP_ALL_REQUEST,
  GROUP_ALL_SUCCESS,
  GROUP_CREATE_FAIL,
  MANAGE_GROUP_FAIL,
  MANAGE_GROUP_REQUEST,
  MANAGE_GROUP_SUCCESS,
  PERMISSION_MAPPING_ALL_FAIL,
  PERMISSION_MAPPING_ALL_REQUEST,
  PERMISSION_MAPPING_ALL_SUCCESS,
  PERMISSION_MAPPING_BY_RESOURCEID_FAIL,
  PERMISSION_MAPPING_BY_RESOURCEID_REQUEST,
  PERMISSION_MAPPING_BY_RESOURCEID_SUCCESS,
  SET_NETWORK_ERROR_FALSE,
  SET_NETWORK_ERROR_TRUE,
  SSO_DETAILS_FAIL,
  SSO_DETAILS_REQUEST,
  SSO_DETAILS_SUCCESS,
  USER_DELETE_FAIL,
  USER_DELETE_REQUEST,
  USER_DELETE_SUCCESS,
  USER_ROLES_OF_PROJECT_FAIL,
  USER_ROLES_OF_PROJECT_REQUEST,
  USER_ROLES_OF_PROJECT_SUCCESS,
} from "../constants/authConstants";

export const getAllGroupsReducer = (
  state: { allGroups?: Object; loading?: boolean; error?: boolean } = {
    allGroups: undefined,
    loading: true,
    error: false,
  },
  action: $TSFixMe
) => {
  switch (action.type) {
    case GROUP_ALL_REQUEST:
      return { allGroups: state.allGroups, loading: true, error: false };

    case GROUP_ALL_SUCCESS:
      return { allGroups: action.payload, loading: false, success: true };

    case GROUP_CREATE_FAIL:
      return { loading: false, error: action.payload };

    default:
      return state;
  }
};

export const userDeleteReducer = (
  state: $TSFixMe = { user: [] },
  action: $TSFixMe
) => {
  switch (action.type) {
    case USER_DELETE_REQUEST:
      return { loading: true, user: [] };

    case USER_DELETE_SUCCESS:
      return {
        loading: false,
        user: action.payload,
      };
    case USER_DELETE_FAIL:
      return { loading: false, error: action.payload };
    default:
      return state;
  }
};

export const manageGroupReducer = (state = {}, action: $TSFixMe) => {
  switch (action.type) {
    case MANAGE_GROUP_REQUEST:
      return { loading: true, data: [] };

    case MANAGE_GROUP_SUCCESS:
      return {
        loading: false,
        data: action.payload,
      };
    case MANAGE_GROUP_FAIL:
      return { loading: false, error: action.payload };
    default:
      return state;
  }
};

export const getAllPermissionMappingReducer = (
  state = {},
  action: $TSFixMe
) => {
  switch (action.type) {
    case PERMISSION_MAPPING_ALL_REQUEST:
      return { data: undefined, loading: true, error: false };

    case PERMISSION_MAPPING_ALL_SUCCESS:
      return { data: action.payload, loading: false, success: true };

    case PERMISSION_MAPPING_ALL_FAIL:
      return { loading: false, error: action.payload };

    default:
      return state;
  }
};

export const getPermissionsMappingByResourceIdReducer = (
  state = {},
  action: $TSFixMe
) => {
  switch (action.type) {
    case PERMISSION_MAPPING_BY_RESOURCEID_REQUEST:
      return { data: undefined, loading: true, error: false };

    case PERMISSION_MAPPING_BY_RESOURCEID_SUCCESS:
      return { data: action.payload, loading: false, success: true };

    case PERMISSION_MAPPING_BY_RESOURCEID_FAIL:
      return { loading: false, error: action.payload };

    default:
      return state;
  }
};

export const userRolesOfProjectReducer = (state = {}, action: $TSFixMe) => {
  switch (action.type) {
    case USER_ROLES_OF_PROJECT_REQUEST:
      return { data: undefined, loading: true, error: false };

    case USER_ROLES_OF_PROJECT_SUCCESS:
      return { data: action.payload, loading: false, success: true };

    case USER_ROLES_OF_PROJECT_FAIL:
      return { loading: false, error: action.payload };

    default:
      return state;
  }
};

export const deletePermissionsMappingReducer = (
  state: $TSFixMe = { data: undefined, loading: true, error: false },
  action: $TSFixMe
) => {
  switch (action.type) {
    case DELETE_PERMISSIONS_MAPPING_REQUEST:
      return { data: state.data, loading: true, error: false };

    case DELETE_PERMISSIONS_MAPPING_SUCCESS:
      return { data: action.payload, loading: false, success: true };

    case DELETE_PERMISSIONS_MAPPING_FAIL:
      return { loading: false, error: action.payload };

    default:
      return state;
  }
};

export const networkErrorReducer = (
  state: $TSFixMe = { networkError: false },
  action: $TSFixMe
) => {
  switch (action.type) {
    case SET_NETWORK_ERROR_TRUE:
      return { networkError: true };

    case SET_NETWORK_ERROR_FALSE:
      return { networkError: false };

    default:
      return state;
  }
};

export const ssoDetailsReducer = (state = {}, action: $TSFixMe) => {
  switch (action.type) {
    case SSO_DETAILS_REQUEST:
      return { ssoDetails: undefined, loading: true, error: false };

    case SSO_DETAILS_SUCCESS:
      return { ssoDetails: action.payload, loading: false, success: true };

    case SSO_DETAILS_FAIL:
      return { ssoDetails: false, error: action.payload };

    default:
      return state;
  }
};
