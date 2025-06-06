import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { IWebhookCallData } from "Apps/Connect/Sources/RestAPIConnector/RestAPIConnector.types";

interface IWebhookSliceState {
  executionLoading: boolean;
  executionResult: IWebhookCallData[] | undefined;
  executionError: string | false;

  saveLoading: boolean;
  saveStatus: undefined | boolean;
}

const initialState: IWebhookSliceState = {
  executionLoading: false,
  executionResult: undefined,
  executionError: false,

  saveLoading: false,
  saveStatus: undefined,
};

const webhookSlice = createSlice({
  name: "webhook",
  initialState,
  reducers: {
    webhookExecution: (
      state: IWebhookSliceState,
      action: PayloadAction<{
        executionLoading: boolean;
        executionResult: any;
        executionError: string | false;
      }>
    ) => {
      state.executionLoading = action.payload.executionLoading;
      state.executionResult = action.payload.executionResult;
      state.executionError = action.payload.executionError;
    },
    webhookSaving: (
      state: IWebhookSliceState,
      action: PayloadAction<{
        saveLoading: boolean;
        saveStatus: undefined | boolean;
      }>
    ) => {
      state.saveLoading = action.payload.saveLoading;
      state.saveStatus = action.payload.saveStatus;
    },
  },
});

export const { webhookExecution, webhookSaving } = webhookSlice.actions;
export default webhookSlice.reducer;
