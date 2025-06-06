import {
  GROUP_ALL_FAIL,
  GROUP_ALL_REQUEST,
  GROUP_ALL_SUCCESS,
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

    case GROUP_ALL_FAIL:
      return { loading: false, error: action.payload };

    default:
      return state;
  }
};
