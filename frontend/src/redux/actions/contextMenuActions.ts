import {
  CLOSE_CONTEXT_MENU,
  OPEN_CONTEXT_MENU,
  UPDATE_CONTEXT_MENU,
} from "../constants/contextMenuConstants";

// export const openContextMenu = (
//   contextMenuData: $TSFixMe,
//   x: $TSFixMe,
//   y: $TSFixMe,
//   target: $TSFixMe,
//   record: $TSFixMe,
//   menu: $TSFixMe
// ) => {
//
//   return { type: OPEN_CONTEXT_MENU, x, y };
// };

export const openContextMenu = () => {
  return { type: OPEN_CONTEXT_MENU };
};

export const closeContextMenu = () => {
  return { type: CLOSE_CONTEXT_MENU };
};

export const updateContextMenu = (menu: $TSFixMe) => {
  return { type: UPDATE_CONTEXT_MENU };
};
