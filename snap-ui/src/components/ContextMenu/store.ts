import { useReducer } from "react";
import { notEmpty } from "utils/utilities";

const SHOW_CONTEXT_MENU = "SHOW_CONTEXT_MENU";
const HIDE_CONTEXT_MENU = "HIDE_CONTEXT_MENU";

export interface ContextMenuState {
  isVisible: boolean;
  x: number;
  y: number;
}

export interface ContextMenuAction {
  payload: ContextMenuState;
  type: string;
}

export interface ContextMenuStore {
  state: ContextMenuState;
  displayContextMenu: (x: number, y: number) => void;
  hideContextMenu: () => void;
}

export const contextMenuReducer = (
  state: ContextMenuState,
  action: ContextMenuAction
): ContextMenuState => {
  switch (action.type) {
    case SHOW_CONTEXT_MENU:
      return {
        ...state,
        isVisible: true,
        x: action.payload.x,
        y: action.payload.y,
      };
    case HIDE_CONTEXT_MENU:
      return {
        ...state,
        isVisible: false,
      };
    default:
      return state;
  }
};

const initialState: ContextMenuState = {
  isVisible: false,
  x: 0,
  y: 0,
};

export const useContextMenuState = (): ContextMenuStore => {
  const [state, dispatch] = useReducer(contextMenuReducer, initialState);

  const displayContextMenu = (x: number, y: number) => {
    dispatch({
      payload: {
        x: x,
        y: y,
        isVisible: true,
      },
      type: SHOW_CONTEXT_MENU,
    });
  };

  const hideContextMenu = () => {
    dispatch({
      payload: state,
      type: HIDE_CONTEXT_MENU,
    });
  };

  return { state, displayContextMenu, hideContextMenu };
};
