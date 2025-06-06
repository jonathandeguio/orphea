import { License } from "redux/licenseInfoSlice";

export const transformFieldValuesIntoLicense = (fieldValues: any) => {
  return {
    ...fieldValues,
    maximumUsers:
      fieldValues.maximumUsers == "∞" ? -1 : fieldValues.maximumUsers,
    maximumBuildsPerDay:
      fieldValues.maximumBuildsPerDay == "∞"
        ? -1
        : fieldValues.maximumBuildsPerDay,
    maximumDatasets:
      fieldValues.maximumDatasets == "∞" ? -1 : fieldValues.maximumDatasets,
    maximumRepositories:
      fieldValues.maximumRepositories == "∞"
        ? -1
        : fieldValues.maximumRepositories,
    maximumCharts:
      fieldValues.maximumCharts == "∞" ? -1 : fieldValues.maximumCharts,
    maximumDashboards:
      fieldValues.maximumDashboards == "∞" ? -1 : fieldValues.maximumDashboards,
  } as License;
};
export const transformLicenseIntoVisualValues = (value: number) => {
  return value == -1 ? "∞" : value;
};

export enum PRODUCT_ENUM {
  DATA_PLATFORM = "DATA_PLATFORM",
  DATA_HUB = "DATA_HUB",
  DATA_VIZ = "DATA_VIZ",
}

export type PRODUCT_TYPE = keyof typeof PRODUCT_ENUM;
