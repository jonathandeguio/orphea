import {
  CLOSE_CONTEXT_MENU,
  OPEN_CONTEXT_MENU,
  UPDATE_CONTEXT_MENU,
} from "../constants/contextMenuConstants";

export const contextMenuReducer = (
  state = {
    x: 0,
    y: 0,
    target: undefined,
    record: undefined,
    visible: false,
    menu: undefined,
  },
  action: $TSFixMe
) => {
  switch (action.type) {
    case OPEN_CONTEXT_MENU:
      return {
        x: action.x,
        y: action.y,
        target: action.target,
        record: action.record,
        visible: true,
        menu: action.menu,
      };
    case UPDATE_CONTEXT_MENU:
      return {
        x: state.x,
        y: state.y,
        target: state.target,
        record: state.record,
        visible: state.visible,
        menu: action.menu,
      };
    case CLOSE_CONTEXT_MENU:
      return {
        x: state.x,
        y: state.y,
        target: state.target,
        record: state.record,
        visible: false,
        menu: state.menu,
      };

    default:
      return state;
  }
};
