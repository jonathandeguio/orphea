import { PayloadAction, createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { decryptLicenseKeyAPI } from "pages/Settings/apis";
import { isDefined } from "utils/utilities";

export interface License {
  client: String;
  product: String;
  baseUrl: String;
  displayBlockedFeatures: Boolean;
  maximumUsers: Number;
  maximumBuildsPerDay: Number;
  maximumDatasets: Number;
  maximumDashboards: Number;
  maximumCharts: Number;
  maximumRepositories: Number;
  expiresOn: Date;
}

const intialInfo: License = {
  client: "Invalid Client",
  product: "None",
  baseUrl: "http://localhost:3000",
  displayBlockedFeatures: false,
  maximumUsers: 0,
  maximumBuildsPerDay: 0,
  maximumDatasets: 0,
  maximumDashboards: 0,
  maximumCharts: 0,
  maximumRepositories: 0,
  expiresOn: new Date(0),
};

interface LicenseState {
  loading: boolean;
  error: boolean;
  info: License;
}

const initialState: LicenseState = {
  loading: true,
  error: false,
  info: intialInfo,
};

export const getLicenseInfo = createAsyncThunk(
  "decryptLicenseKey",
  async (payload: string) => {
    const { data } = await decryptLicenseKeyAPI(payload);
    return data;
  }
);

const licenseInfoSlice = createSlice({
  name: "licenseInfo",
  initialState,
  reducers: {
    updateLicenseInfo: (
      state: LicenseState,
      action: PayloadAction<License>
    ) => {
      state.info = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(getLicenseInfo.pending, (state) => {
        state.loading = true;
        state.error = false;
      })

      .addCase(getLicenseInfo.fulfilled, (state, action) => {
        if (isDefined(action.payload.client)) state.info = action.payload;
        else state.info = intialInfo;
        state.loading = false;
      })
      .addCase(getLicenseInfo.rejected, (state) => {
        state.loading = false;
        state.info = intialInfo;
        state.error = true;
      });
  },
});

export const { updateLicenseInfo } = licenseInfoSlice.actions;
export default licenseInfoSlice.reducer;
