import {
  PLATFORM_CONFIG_FAIL,
  PLATFORM_CONFIG_REQUEST,
  PLATFORM_CONFIG_SUCCESS,
  PLATFORM_CONFIG_UPDATE,
} from "../constants/platformSettingsConstants";

export const platformConfigReducer = (state = {}, action: any) => {
  switch (action.type) {
    case PLATFORM_CONFIG_REQUEST:
      return { ...state, config: undefined, loading: true, error: false };

    case PLATFORM_CONFIG_SUCCESS:
      return {
        ...state,
        config: action.payload,
        loading: false,
        error: false,
      };

    case PLATFORM_CONFIG_UPDATE:
      return {
        ...state,
        config: action.payload,
        loading: false,
        error: false,
      };

    case PLATFORM_CONFIG_FAIL:
      return {
        ...state,
        config: undefined,
        loading: false,
        error: action.payload,
      };

    default:
      return state;
  }
};
