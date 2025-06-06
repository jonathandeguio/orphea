import axios from "axios";
import {
  GROUP_ALL_FAIL,
  GROUP_ALL_REQUEST,
  GROUP_ALL_SUCCESS,
} from "../constants/authConstants";
import {
  PROD_FAIL,
  PROD_REQUEST,
  PROD_SUCCESS,
} from "../constants/tokenConstants";

export const getAllGroups =
  () => async (dispatch: $TSFixMe, getState: $TSFixMe) => {
    try {
      dispatch({
        type: GROUP_ALL_REQUEST,
      });

      const { data } = await axios.get(`/passport/groups/all`);

      dispatch({
        type: GROUP_ALL_SUCCESS,
        payload: data,
      });
      return data;
    } catch (error) {
      dispatch({
        type: GROUP_ALL_FAIL,
        payload:
          (error as $TSFixMe).response &&
          (error as $TSFixMe).response.data.detail
            ? (error as $TSFixMe).response.data.detail
            : (error as $TSFixMe).message,
      });
    }
  };

export const getConfig = () => async (dispatch: any, getState: any) => {
  try {
    dispatch({
      type: PROD_REQUEST,
    });

    const { data } = await axios.get(`/platform/config`);
    dispatch({
      type: PROD_SUCCESS,
      payload: data,
    });
  } catch (error) {
    dispatch({
      type: PROD_FAIL,
    });
    return true;
  }
};
