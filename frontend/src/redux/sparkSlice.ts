import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import {
  getSparkConfigAPI,
  updateSparkConfigAPI,
} from "pages/Settings/PlatformConfig/Spark/SparkConfig.api";

type TSparkOption = "platform" | "local[*]" | "local" | "kubernetes";

interface TSparkConfig {
  config: TSparkOption;
  master: TSparkOption;
  pgSync: TSparkOption;
  dataset: TSparkOption;
  columnStats: TSparkOption;
  sqlBuild: TSparkOption;
  pythonBuild: TSparkOption;
  sqlPreview: TSparkOption;
  pythonPreview: TSparkOption;
}

interface TSparkState {
  loading: boolean;
  error: boolean;
  config: TSparkConfig;
}

const initialConfig: TSparkConfig = {
  config: "platform",
  master: "local[*]",
  pgSync: "local",
  dataset: "local",
  columnStats: "local",
  sqlBuild: "kubernetes",
  pythonBuild: "kubernetes",
  sqlPreview: "local",
  pythonPreview: "kubernetes",
};

const initialState: TSparkState = {
  loading: false,
  error: false,
  config: initialConfig,
};

export const getSparkConfig = createAsyncThunk(
  "sparkConfig/getSparkConfig",
  async () => {
    const response = await getSparkConfigAPI();
    return response.data;
  }
);

export const updateSparkConfig = createAsyncThunk(
  "sparkConfig/updateSparkConfig",
  async (payload: TSparkConfig) => {
    const response = await updateSparkConfigAPI(payload);
    return payload;
  }
);

const sparkConfigSlice = createSlice({
  name: "sparkConfig",
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(getSparkConfig.pending, (state) => {
        state.loading = true;
        state.error = false;
      })
      .addCase(getSparkConfig.fulfilled, (state, action) => {
        state.loading = false;
        state.config = action.payload;
      })
      .addCase(getSparkConfig.rejected, (state) => {
        state.loading = false;
        state.config = initialConfig;
        state.error = true;
      })
      .addCase(updateSparkConfig.pending, (state) => {
        state.loading = true;
        state.error = false;
      })
      .addCase(updateSparkConfig.fulfilled, (state, action) => {
        state.loading = false;
        state.config = action.payload;
      })
      .addCase(updateSparkConfig.rejected, (state) => {
        state.loading = false;
        state.config = initialConfig;
        state.error = true;
      });
  },
});

// export const { getSparkConfig, updateSparkConfig } = sparkConfigSlice.actions;
export default sparkConfigSlice.reducer;
