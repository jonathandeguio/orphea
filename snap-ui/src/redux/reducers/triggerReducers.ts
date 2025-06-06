import {
  ALL_TRIGGER_DETAILS_FAIL,
  ALL_TRIGGER_DETAILS_REQUEST,
  ALL_TRIGGER_DETAILS_RESET,
  ALL_TRIGGER_DETAILS_SUCCESS,
} from "../constants/triggerConstants";

export const allTriggerDetailsReducer = (
  state = { allTriggers: undefined, loading: true, error: false },
  action: $TSFixMe
) => {
  switch (action.type) {
    case ALL_TRIGGER_DETAILS_REQUEST:
      return { allTriggers: state.allTriggers, loading: true, error: false };

    case ALL_TRIGGER_DETAILS_SUCCESS:
      return {
        allTriggers: action.payload,
        loading: false,
        error: false,
      };

    case ALL_TRIGGER_DETAILS_FAIL:
      return { allTriggers: undefined, loading: false, error: action.payload };

    case ALL_TRIGGER_DETAILS_RESET:
      return { allTriggers: undefined, loading: false, error: false };

    default:
      return state;
  }
};
