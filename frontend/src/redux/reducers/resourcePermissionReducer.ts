import {
  DEFAULT_MODE,
  MODE_UPDATED_SUCCESS,
  PERMISISON_UPDATED_SUCCESS
} from "../../redux/constants/resourcePermissionConstants";

export const resourcePermissionReducer = (state: any = {}, action: any) => {
  switch (action.type) {
    case PERMISISON_UPDATED_SUCCESS:
      return {
        ...state,
        [action.resourceId]: {
          ...state[action.resourceId],
          permission: action.permission,
          mode: DEFAULT_MODE,
        },
      };

    case MODE_UPDATED_SUCCESS:
      return {
        ...state,
        [action.resourceId]: {
          ...state[action.resourceId],
          mode: action.mode,
        },
      };

    default:
      return state;
  }
};
