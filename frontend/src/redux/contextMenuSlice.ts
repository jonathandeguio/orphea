import { createSlice, PayloadAction } from "@reduxjs/toolkit";

interface ContextMenuState {
  isVisible: boolean;
  contextMenuId: any;
  x: number;
  y: number;
}

const initialState: ContextMenuState = {
  isVisible: false,
  contextMenuId: null,
  x: 0,
  y: 0,
};

const contextMenuSlice = createSlice({
  name: "contextMenu",
  initialState,
  reducers: {
    showContextMenu: (
      state: ContextMenuState,
      action: PayloadAction<{ x: number; y: number; contextMenuId: any }>
    ) => {
      state.isVisible = true;
      state.contextMenuId = action.payload.contextMenuId;
      state.x = action.payload.x;
      state.y = action.payload.y;
    },
    hideContextMenu: (state: ContextMenuState) => {
      state.isVisible = false;
      state.contextMenuId = null;
      state.x = 0;
      state.y = 0;
    },
  },
});

export const { showContextMenu, hideContextMenu } = contextMenuSlice.actions;
export default contextMenuSlice.reducer;
