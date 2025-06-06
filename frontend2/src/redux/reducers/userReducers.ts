import {
  ALL_USER_DETAILS_FAIL,
  ALL_USER_DETAILS_REQUEST,
  ALL_USER_DETAILS_RESET,
  ALL_USER_DETAILS_SUCCESS,
  IS_GROUP_ADMIN_FAIL,
  IS_GROUP_ADMIN_REQUEST,
  IS_GROUP_ADMIN_SUCCESS,
  IS_PROJECT_ADMIN_FAIL,
  IS_PROJECT_ADMIN_REQUEST,
  IS_PROJECT_ADMIN_SUCCESS,
  IS_USER_ADMIN_FAIL,
  IS_USER_ADMIN_REQUEST,
  IS_USER_ADMIN_SUCCESS,
  USER_BY_ID_FAIL,
  USER_BY_ID_REQUEST,
  USER_BY_ID_SUCCESS,
  USER_DETAILS_FAIL,
  USER_DETAILS_REQUEST,
  USER_DETAILS_RESET,
  USER_DETAILS_SUCCESS,
  USER_LOGIN_FAIL,
  USER_LOGIN_REQUEST,
  USER_LOGIN_SUCCESS,
  USER_LOGOUT,
} from "../constants/userConstants";

export const userLoginReducer = (state = {loading: true, userInfo: undefined, error: undefined}, action: any) => {
  switch (action.type) {
    case USER_LOGIN_REQUEST:
      return { loading: true, userInfo: undefined, error: undefined };

    case USER_LOGIN_SUCCESS:
      return {
        loading: false,
        userInfo: action.payload,
        error: undefined
      };

    case USER_LOGIN_FAIL:
      return { loading: false, userInfo: undefined, error: action.payload };

    case USER_LOGOUT:
      return {loading : false, userInfo: undefined, error: undefined};

    default:
      return state;
  }
};

export const userDetailsReducer = (state = {user: undefined, loading: true, error: false}, action: any) => {
  switch (action.type) {
    case USER_DETAILS_REQUEST:
      return { user: undefined, loading: true, error: false };

    case USER_DETAILS_SUCCESS:
      return {
        user: action.payload,
        loading: false,
        error: false,
      };

    case USER_DETAILS_FAIL:
      return { user: undefined, loading: false, error: action.payload };

    case USER_DETAILS_RESET:
      return { user: undefined, loading: false, error: false };

    default:
      return state;
  }
};

export const userByIdReducer = (
  state = { user: undefined, loading: true, error: false },
  action: any
) => {
  switch (action.type) {
    case USER_BY_ID_REQUEST:
      return { user: undefined, loading: true, error: false };

    case USER_BY_ID_SUCCESS:
      return {
        user: action.payload,
        loading: false,
        error: false,
      };

    case USER_BY_ID_FAIL:
      return { user: undefined, loading: false, error: action.payload };

    default:
      return state;
  }
};

export const allUserDetailsReducer = (
  state = { allusers: undefined, loading: true, error: false },
  action: any
) => {
  switch (action.type) {
    case ALL_USER_DETAILS_REQUEST:
      return { allusers: state.allusers, loading: true, error: false };

    case ALL_USER_DETAILS_SUCCESS:
      return {
        allusers: action.payload,
        loading: false,
        error: false,
      };

    case ALL_USER_DETAILS_FAIL:
      return { allusers: undefined, loading: false, error: action.payload };

    case ALL_USER_DETAILS_RESET:
      return { allusers: undefined, loading: false, error: false };

    default:
      return state;
  }
};

export const isUserAdminReducer = (state = {}, action: any) => {
  switch (action.type) {
    case IS_USER_ADMIN_REQUEST:
      return { user: undefined, loading: true, error: false };

    case IS_USER_ADMIN_SUCCESS:
      return { user: action.payload, loading: false, error: false };

    case IS_USER_ADMIN_FAIL:
      return { user: undefined, loading: false, error: action.payload };

    default:
      return state;
  }
};

export const isProjectAdminReducer = (state = {}, action: any) => {
  switch (action.type) {
    case IS_PROJECT_ADMIN_REQUEST:
      return { user: undefined, loading: true, error: false };

    case IS_PROJECT_ADMIN_SUCCESS:
      return { user: action.payload, loading: false, error: false };

    case IS_PROJECT_ADMIN_FAIL:
      return { user: undefined, loading: false, error: action.payload };

    default:
      return state;
  }
};

export const isGroupAdminReducer = (state = {}, action: any) => {
  switch (action.type) {
    case IS_GROUP_ADMIN_REQUEST:
      return { user: undefined, loading: true, error: false };

    case IS_GROUP_ADMIN_SUCCESS:
      return { user: action.payload, loading: false, error: false };

    case IS_GROUP_ADMIN_FAIL:
      return { user: undefined, loading: false, error: action.payload };

    default:
      return state;
  }
};
