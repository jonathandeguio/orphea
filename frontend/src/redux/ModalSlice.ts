import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { ResourceType } from "Apps/explorer/explorer.utils";
import { isDefined } from "utils/utilities";
interface ModalState {
  fileExplorerModal: {
    open: boolean;
    type: ResourceType[];
    activeProject?: string;
    activeId: string;
    action: (data: any) => void;
    projectSwitchAllowed?: boolean;
  };
}

const initialState: ModalState = {
  fileExplorerModal: {
    open: false,
    type: ["FOLDER"],
    activeProject: undefined,
    activeId: "",
    action: () => {},
  },
};

const ModalSlice = createSlice({
  name: "index",
  initialState,
  reducers: {
    openFileExplorerModal: (
      state: ModalState,
      action: PayloadAction<{
        type: ResourceType[];
        action: (data: any) => void;
        activeProject?: string;
        activeId?: string;
        projectSwitchAllowed?: boolean;
      }>
    ) => {
      state.fileExplorerModal.open = true;
      state.fileExplorerModal.action = action.payload.action;
      state.fileExplorerModal.type = action.payload.type;
      state.fileExplorerModal.activeProject =
        action.payload.activeProject ?? state.fileExplorerModal.activeProject;
      state.fileExplorerModal.activeId =
        action.payload.activeId ?? state.fileExplorerModal.activeId;
      state.fileExplorerModal.projectSwitchAllowed =
        action.payload.projectSwitchAllowed ??
        state.fileExplorerModal.projectSwitchAllowed;
    },
    closeFileExplorerModal: (state: ModalState) => {
      state.fileExplorerModal.open = false;
    },
    updateFileExplorerModalState: (
      state: ModalState,
      action: PayloadAction<{
        activeProject?: string;
        activeId?: string;
      }>
    ) => {
      if (isDefined(action.payload.activeProject))
        state.fileExplorerModal.activeProject = action.payload.activeProject;
      if (isDefined(action.payload.activeId))
        state.fileExplorerModal.activeId = action.payload.activeId;
    },
  },
});

export const {
  openFileExplorerModal,
  closeFileExplorerModal,
  updateFileExplorerModalState,
} = ModalSlice.actions;
export default ModalSlice.reducer;
