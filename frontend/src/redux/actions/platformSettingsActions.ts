import {
  getPlatformConfigAPI,
  updatePlatformConfigAPI,
} from "pages/Settings/PlatformConfig/PlatformConfig.api";
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
        payload: {
          ...data.platformConfig,
          customTheme: JSON.parse(data.platformConfig.customTheme),
        },
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
  (payload: any) => async (dispatch: any, getState: any) => {
    updatePlatformConfigAPI({
      ...payload,
      customTheme: JSON.stringify(payload.customTheme),
    })
      .then(({ data }) => {
        dispatch({
          type: PLATFORM_CONFIG_UPDATE,
          payload: { ...data, customTheme: JSON.parse(data.customTheme) },
        });
      })
      .catch((error) => {
        dispatch({
          type: PLATFORM_CONFIG_FAIL,
          payload: error,
        });
      });
  };
