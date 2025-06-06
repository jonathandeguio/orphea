import {
  ADD_FILTERS_FROM_DATASET_SUCCESS,
  FILTERS_DATASET_SUCCESS
} from "../../redux/constants/filtersConstants";

export const updateFilters = (
  state: any = {
    loading: false,
    filters: undefined,
    filter: undefined,
  },
  action: $TSFixMe
) => {
  switch (action.type) {
    case FILTERS_DATASET_SUCCESS:
      return {
        ...state,
        [action.resourceId]: {
          ...state[action.resourceId],
          filters: action.payload,
        },
      };

    case ADD_FILTERS_FROM_DATASET_SUCCESS:
      return {
        ...state,
        [action.resourceId]: {
          ...state[action.resourceId],
          filter: action.payload,
        },
      };

    default:
      return state;
  }
};
