import {
  USER_ROLES_OF_PROJECT_REQUEST,
  USER_ROLES_OF_PROJECT_SUCCESS,
  USER_ROLES_OF_PROJECT_FAIL,
  All_ROLES_FAIL,
  All_ROLES_REQUEST,
  All_ROLES_SUCCESS,
  GROUP_ALL_REQUEST,
  GROUP_ALL_SUCCESS,
  GROUP_CREATE_FAIL,
  GROUP_CREATE_REQUEST,
  GROUP_CREATE_SUCCESS,
  GROUP_DATA_FAIL,
  GROUP_DATA_REQUEST,
  GROUP_DATA_SUCCESS,
  GROUP_DELETE_FAIL,
  GROUP_DELETE_REQUEST,
  GROUP_DELETE_SUCCESS,
  MANAGE_GROUP_FAIL,
  MANAGE_GROUP_REQUEST,
  MANAGE_GROUP_SUCCESS,
  PERMISSION_MAPPING_ALL_FAIL,
  PERMISSION_MAPPING_ALL_REQUEST,
  PERMISSION_MAPPING_ALL_SUCCESS,
  PERMISSION_MAPPING_BY_RESOURCEID_FAIL,
  PERMISSION_MAPPING_BY_RESOURCEID_REQUEST,
  PERMISSION_MAPPING_BY_RESOURCEID_SUCCESS,
  USER_DELETE_FAIL,
  USER_DELETE_REQUEST,
  USER_DELETE_SUCCESS,
  USER_RESOURCE_PERMISSIONS_REQUEST,
  USER_RESOURCE_PERMISSIONS_SUCCESS,
  USER_RESOURCE_PERMISSIONS_FAIL,
  DELETE_PERMISSIONS_MAPPING_REQUEST,
  DELETE_PERMISSIONS_MAPPING_SUCCESS,
  DELETE_PERMISSIONS_MAPPING_FAIL,
  SET_NETWORK_ERROR_FALSE,
  SET_NETWORK_ERROR_TRUE,
} from "../constants/authConstants";

export const getAllGroupsReducer = (
  state : { allGroups?: Object, loading?: boolean, error?: boolean } = { allGroups: undefined, loading: true, error: false },
  action: any
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

export const getGroupByIdReducer = (state = {}, action: any) => {
  switch (action.type) {
    case GROUP_DATA_REQUEST:
      return { group: undefined, loading: true, error: false };

    case GROUP_DATA_SUCCESS:
      return { group: action.payload, loading: false, success: true };

    case GROUP_DATA_FAIL:
      return { loading: false, error: action.payload };

    default:
      return state;
  }
};

export const groupCreateReducer = (state = {}, action: any) => {
  switch (action.type) {
    case GROUP_CREATE_REQUEST:
      return { loading: true };

    case GROUP_CREATE_SUCCESS:
      return { loading: false, success: true, group: action.payload };

    case GROUP_CREATE_FAIL:
      return { loading: false, error: action.payload };

    default:
      return state;
  }
};

export const groupDeleteReducer = (state : {group?: any} = { group: [] }, action: any) => {
  switch (action.type) {
    case GROUP_DELETE_REQUEST:
      return { loading: true, group: [] };

    case GROUP_DELETE_SUCCESS:
      return {
        loading: false,
        groups: action.payload,
      };
    case GROUP_DELETE_FAIL:
      return { loading: false, error: action.payload };
    default:
      return state;
  }
};

export const userDeleteReducer = (state : any = { user: [] }, action: any) => {
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

export const manageGroupReducer = (state = {}, action: any) => {
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

export const getAllRolesReducer = (
  state : any = { allRoles: undefined, loading: true, error: false },
  action: any
) => {
  switch (action.type) {
    case All_ROLES_REQUEST:
      return {
        allRoles: (state as any).roles,
        loading: true,
        error: false,
      };

    case All_ROLES_SUCCESS:
      return { allRoles: action.payload, loading: false, success: true };

    case All_ROLES_FAIL:
      return { loading: false, error: action.payload };

    default:
      return state;
  }
};

export const getAllPermissionMappingReducer = (state = {}, action: any) => {
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
  action: any
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

export const userRolesOfProjectReducer = (state = {}, action: any) => {
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

export const userResourcePermissionsReducer = (state = {}, action: any) => {
  switch (action.type) {
    case USER_RESOURCE_PERMISSIONS_REQUEST:
      return { data: undefined, loading: true, error: false };

    case USER_RESOURCE_PERMISSIONS_SUCCESS:
      return { data: action.payload, loading: false, success: true };

    case USER_RESOURCE_PERMISSIONS_FAIL:
      return { loading: false, error: action.payload };

    default:
      return state;
  }
};

export const deletePermissionsMappingReducer = (
  state : any = { data: undefined, loading: true, error: false },
  action: any
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
  state : any = { networkError: false },
  action: any
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


