import { PlatformConfig } from "apps/settings/platform/PlatformConfig";
import {
  getPlatformConfigAPI,
  updatePlatformConfigAPI,
} from "apps/settings/platform/PlatformConfig.api";
import {
  PLATFORM_CONFIG_FAIL,
  PLATFORM_CONFIG_REQUEST,
  PLATFORM_CONFIG_SUCCESS,
  PLATFORM_CONFIG_UPDATE,
} from "../constants/platformSettingsConstants";

export const getPlatformConfig = () => async (dispatch: any, getState: any) => {
  dispatch({ type: PLATFORM_CONFIG_REQUEST });
  getPlatformConfigAPI()
    .then(({ data }) => {
      dispatch({
        type: PLATFORM_CONFIG_SUCCESS,
        payload: data.platformConfig,
      });
    })
    .catch((error) => {
      dispatch({
        type: PLATFORM_CONFIG_FAIL,
        payload: error,
      });
    });
};

export const updatePlatformConfig =
  (payload: PlatformConfig) => async (dispatch: any, getState: any) => {
    updatePlatformConfigAPI(payload)
      .then(({ data }) => {
        dispatch({
          type: PLATFORM_CONFIG_UPDATE,
          payload: data,
        });
      })
      .catch((error) => {
        dispatch({
          type: PLATFORM_CONFIG_FAIL,
          payload: error,
        });
      });
  };
