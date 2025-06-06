import {
  ALL_CONFIGURATION_DETAILS_FAIL,
  ALL_CONFIGURATION_DETAILS_REQUEST,
  ALL_CONFIGURATION_DETAILS_RESET,
  ALL_CONFIGURATION_DETAILS_SUCCESS,
} from "../constants/configurationConstants";

export const allConfigurationDetailsReducer = (
  state = { allConfiguration: undefined, loading: true, error: false },
  action: $TSFixMe
) => {
  switch (action.type) {
    case ALL_CONFIGURATION_DETAILS_REQUEST:
      return { allConfiguration: state.allConfiguration, loading: true, error: false };

    case ALL_CONFIGURATION_DETAILS_SUCCESS:
      return {
        allConfiguration: action.payload,
        loading: false,
        error: false,
      };

    case ALL_CONFIGURATION_DETAILS_FAIL:
      return { allConfiguration: undefined, loading: false, error: action.payload };

    case ALL_CONFIGURATION_DETAILS_RESET:
      return { allConfiguration: undefined, loading: false, error: false };

    default:
      return state;
  }
};
