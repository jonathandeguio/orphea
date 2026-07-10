import axios from "axios";
import {
  USER_ROLES_OF_PROJECT_REQUEST,
  USER_ROLES_OF_PROJECT_SUCCESS,
  USER_ROLES_OF_PROJECT_FAIL,
  USER_DELETE_SUCCESS,
  USER_DELETE_FAIL,
  USER_DELETE_REQUEST,
  GROUP_ALL_FAIL,
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
  MANAGE_GROUP_REQUEST,
  MANAGE_GROUP_SUCCESS,
  MANAGE_GROUP_FAIL,
  All_ROLES_REQUEST,
  All_ROLES_FAIL,
  All_ROLES_SUCCESS,
  PERMISSION_MAPPING_ALL_REQUEST,
  PERMISSION_MAPPING_ALL_SUCCESS,
  PERMISSION_MAPPING_ALL_FAIL,
  PERMISSION_MAPPING_BY_RESOURCEID_REQUEST,
  PERMISSION_MAPPING_BY_RESOURCEID_SUCCESS,
  PERMISSION_MAPPING_BY_RESOURCEID_FAIL,
  USER_RESOURCE_PERMISSIONS_SUCCESS,
  USER_RESOURCE_PERMISSIONS_REQUEST,
  USER_RESOURCE_PERMISSIONS_FAIL,
  SET_NETWORK_ERROR_TRUE,
  SET_NETWORK_ERROR_FALSE,
} from "../constants/authConstants";

const BASE_URL_API = process.env.REACT_APP_BASE_URL_API;

export const getAllGroups =
  () => async (dispatch: any, getState: any) => {
    try {
      dispatch({
        type: GROUP_ALL_REQUEST,
      });

      const config = {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("movetodataToken")}`,
        },
      };

      const { data } = await axios.get(
        `${BASE_URL_API}/passport/groups/all`,
        config
      );

      dispatch({
        type: GROUP_ALL_SUCCESS,
        payload: data,
      });
      return data;
    } catch (error) {
      dispatch({
        type: GROUP_ALL_FAIL,
        payload:
          (error as any).response &&
          (error as any).response.data.detail
            ? (error as any).response.data.detail
            : (error as any).message,
      });
    }
  };

export const getGroupById = (id: any) => async (dispatch: any, getState: any) => {
  try {
    dispatch({
      type: GROUP_DATA_REQUEST,
    });

    const config = {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("movetodataToken")}`,
      },
    };

    const { data } = await axios.get(
      `${BASE_URL_API}/passport/groups/${id}`,
      config
    );

    dispatch({
      type: GROUP_DATA_SUCCESS,
      payload: data,
    });
    return data;
  } catch (error) {
    dispatch({
      type: GROUP_DATA_FAIL,
      payload:
        (error as any).response && (error as any).response.data.detail
          ? (error as any).response.data.detail
          : (error as any).message,
    });
  }
};

export const createGroup = (groupDetails: any) => async (dispatch: any, getState: any) => {
  try {
    dispatch({ type: GROUP_CREATE_REQUEST });

    const movetodataToken = localStorage.getItem("movetodataToken");

    const config = {
      headers: {
        "Content-type": "application/json",
        Authorization: `Bearer ${movetodataToken}`,
      },
    };

    const { data } = await axios.post(
      `${BASE_URL_API}/passport/groups/add`,
      JSON.stringify(groupDetails),
      config
    );

    dispatch({
      type: GROUP_CREATE_SUCCESS,
      payload: data,
    });
    return data;
  } catch (error) {
    dispatch({
      type: GROUP_CREATE_FAIL,
      payload:
        (error as any).response && (error as any).response.data.detail
          ? (error as any).response.data.detail
          : (error as any).message,
    });
  }
};

export const deleteGroup = (id: any) => async (dispatch: any) => {
  const movetodataToken = localStorage.getItem("movetodataToken");

  const config = {
    headers: {
      "Content-type": "application/json",
      Authorization: `Bearer ${movetodataToken}`,
    },
  };
  try {
    dispatch({ type: GROUP_DELETE_REQUEST });
    const { data } = await axios.post(
      `${BASE_URL_API}/passport/groups/deleteById`,
      id,
      config
    );
    dispatch({
      type: GROUP_DELETE_SUCCESS,
      payload: data,
    });
  } catch (error) {
    dispatch({
      type: GROUP_DELETE_FAIL,
      payload:
        (error as any).response && (error as any).response.data.detail
          ? (error as any).response.data.detail
          : (error as any).message,
    });
  }
};

export const deleteUser = (id: any) => async (dispatch: any) => {
  const movetodataToken = localStorage.getItem("movetodataToken");

  const config = {
    headers: {
      "Content-type": "application/json",
      Authorization: `Bearer ${movetodataToken}`,
    },
  };
  try {
    dispatch({ type: USER_DELETE_REQUEST });
    const { data } = await axios.delete(
      `${BASE_URL_API}/passport/users/${id}`,
      config
    );
    dispatch({
      type: USER_DELETE_SUCCESS,
      payload: data,
    });
    return data;
  } catch (error) {
    dispatch({
      type: USER_DELETE_FAIL,
      payload:
        (error as any).response && (error as any).response.data.detail
          ? (error as any).response.data.detail
          : (error as any).message,
    });
  }
};

export const manageGroup = (newdetails: any) => async (dispatch: any) => {
  const movetodataToken = localStorage.getItem("movetodataToken");

  const config = {
    headers: {
      "Content-type": "application/json",
      Authorization: `Bearer ${movetodataToken}`,
    },
  };
  try {
    dispatch({ type: MANAGE_GROUP_REQUEST });
    const { data } = await axios.post(
      `${BASE_URL_API}/passport/groups/manageGroups`,
      newdetails,
      config
    );
    dispatch({
      type: MANAGE_GROUP_SUCCESS,
      payload: data,
    });
    return data;
  } catch (error) {
    dispatch({
      type: MANAGE_GROUP_FAIL,
      payload:
        (error as any).response && (error as any).response.data.detail
          ? (error as any).response.data.detail
          : (error as any).message,
    });
  }
};

export const getAllRoles = () => async (dispatch: any, getState: any) => {
  try {
    dispatch({
      type: All_ROLES_REQUEST,
    });

    const config = {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("movetodataToken")}`,
      },
    };

    const { data } = await axios.get(
      `${BASE_URL_API}/passport/roles/all`,
      config
    );

    dispatch({
      type: All_ROLES_SUCCESS,
      payload: data,
    });
    return data;
  } catch (error) {
    dispatch({
      type: All_ROLES_FAIL,
      payload:
        (error as any).response && (error as any).response.data.detail
          ? (error as any).response.data.detail
          : (error as any).message,
    });
  }
};

export const getAllPermissionMapping = () => async (dispatch: any, getState: any) => {
  try {
    dispatch({
      type: PERMISSION_MAPPING_ALL_REQUEST,
    });

    const config = {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("movetodataToken")}`,
      },
    };

    const { data } = await axios.get(
      `${BASE_URL_API}/passport/permissionsMapping/all`,
      config
    );

    dispatch({
      type: PERMISSION_MAPPING_ALL_SUCCESS,
      payload: data,
    });
    return data;
  } catch (error) {
    dispatch({
      type: PERMISSION_MAPPING_ALL_FAIL,
      payload:
        (error as any).response && (error as any).response.data.detail
          ? (error as any).response.data.detail
          : (error as any).message,
    });
  }
};

export const getPermissionsMappingByResourceId =
  (id: any) => async (dispatch: any, getState: any) => {
    try {
      dispatch({
        type: PERMISSION_MAPPING_BY_RESOURCEID_REQUEST,
      });

      const config = {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("movetodataToken")}`,
        },
      };

      const { data } = await axios.get(
        `${BASE_URL_API}/passport/permissionsMapping/getByResourceId/${id}`,
        config
      );

      dispatch({
        type: PERMISSION_MAPPING_BY_RESOURCEID_SUCCESS,
        payload: data,
      });
      return data;
    } catch (error) {
      dispatch({
        type: PERMISSION_MAPPING_BY_RESOURCEID_FAIL,
        payload:
          (error as any).response &&
          (error as any).response.data.detail
            ? (error as any).response.data.detail
            : (error as any).message,
      });
    }
  };

export const getUserRolesOfProject = (id: any) => async (dispatch: any, getState: any) => {
  try {
    dispatch({
      type: USER_ROLES_OF_PROJECT_REQUEST,
    });

    const config = {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("movetodataToken")}`,
      },
    };

    const { data } = await axios.get(
      `${BASE_URL_API}/passport/permissionsMapping/getByResourceId/${id}`,
      config
    );

    dispatch({
      type: USER_ROLES_OF_PROJECT_SUCCESS,
      payload: data,
    });
    return data;
  } catch (error) {
    dispatch({
      type: USER_ROLES_OF_PROJECT_FAIL,
      payload:
        (error as any).response && (error as any).response.data.detail
          ? (error as any).response.data.detail
          : (error as any).message,
    });
  }
};

export const getUserResourcePermissions =
  (id: any) => async (dispatch: any, getState: any) => {
    try {
      dispatch({
        type: USER_RESOURCE_PERMISSIONS_REQUEST,
      });

      const config = {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("movetodataToken")}`,
        },
      };

      const { data } = await axios.get(
        `${BASE_URL_API}/passport/users/userResourcePermissions/${id}`,
        config
      );

      dispatch({
        type: USER_RESOURCE_PERMISSIONS_SUCCESS,
        payload: data,
      });
      return data;
    } catch (error) {
      dispatch({
        type: USER_RESOURCE_PERMISSIONS_FAIL,
        payload:
          (error as any).response &&
          (error as any).response.data.detail
            ? (error as any).response.data.detail
            : (error as any).message,
      });
    }
  };

export const deletePermissionsMapping = (id: any) => async (dispatch: any, getState: any) => {
  try {
    dispatch({
      type: USER_RESOURCE_PERMISSIONS_REQUEST,
    });

    const config = {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("movetodataToken")}`,
      },
    };

    const { data } = await axios.delete(
      `${BASE_URL_API}/passport/permissionsMapping/deleteById/${id}`,
      config
    );

    dispatch({
      type: USER_RESOURCE_PERMISSIONS_SUCCESS,
      payload: data,
    });
  } catch (error) {
    dispatch({
      type: USER_RESOURCE_PERMISSIONS_FAIL,
      payload:
        (error as any).response && (error as any).response.data.detail
          ? (error as any).response.data.detail
          : (error as any).message,
    });
  }
};
export const setnetworkErrorTrue = () => async (dispatch: any, getState: any) => {
  dispatch({
    type: SET_NETWORK_ERROR_TRUE,
  });
};
export const setnetworkErrorFalse = () => async (dispatch: any, getState: any) => {
  dispatch({
    type: SET_NETWORK_ERROR_FALSE,
  });
};
