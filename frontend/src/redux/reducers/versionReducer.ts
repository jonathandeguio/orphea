import {
  VERSION_AUTOSAVE_CHECK,
  VERSION_CHART_CHANGE,
  VERSION_DASH_CHANGE,
  VERSION_UPDATE_SUCCESS
} from "../../redux/constants/versionConstants";

export const versionReducer = (
  state = {
    newVersion: 0,
    changedVersionDash: undefined,
    changedVersionChart: undefined,
    isAutoSaveReady: false,
  },
  action: any
) => {
  switch (action.type) {
    case VERSION_UPDATE_SUCCESS:
      return { ...state, newVersion: state.newVersion + 1 };

    case VERSION_DASH_CHANGE:
      return { ...state, changedVersionDash: action.payload.versionId };

    case VERSION_CHART_CHANGE:
      return { ...state, changedVersionChart: action.payload.versionId };

    case VERSION_AUTOSAVE_CHECK:
      return { ...state, isAutoSaveReady: action.payload.isAutoSaveReady };

    default:
      return state;
  }
};
