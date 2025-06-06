import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { SortingState } from "@tanstack/react-table";

interface fileExplorerState {
  activeId: string | undefined;
  projectId: string | undefined;
  sorting: SortingState;
  selected: {
    action: "CUT" | "COPY" | "SELECTED";
    list: string[];
  };
}

const initialState: fileExplorerState = {
  activeId: undefined,
  projectId: undefined,
  sorting: [
    {
      id: "name",
      desc: false,
    },
  ],
  selected: {
    action: "SELECTED",
    list: [],
  },
};

const fileExplorerSlice = createSlice({
  name: "fileExplorer",
  initialState,
  reducers: {
    updateFileExplorer: (
      state: fileExplorerState,
      action: PayloadAction<{
        activeId?: string;
        projectId?: string;
      }>
    ) => {
      state.activeId = action.payload.activeId;
      state.projectId = action.payload.projectId;
    },
    sortFileExplorer: (
      state: fileExplorerState,
      action: PayloadAction<{
        sorting: SortingState;
      }>
    ) => {
      state.sorting = action.payload.sorting;
    },
    setSelectedFileExplorer: (
      state: fileExplorerState,
      action: PayloadAction<{
        action: "CUT" | "COPY" | "SELECTED";
        list: any[];
      }>
    ) => {
      state.selected.action = action.payload.action;
      state.selected.list = action.payload.list;
    },
  },
});

export const { updateFileExplorer, sortFileExplorer, setSelectedFileExplorer } =
  fileExplorerSlice.actions;
export default fileExplorerSlice.reducer;
