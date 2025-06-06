import { TDatasetColumn } from "./Filters.view";

export const FIELD_OPTIONS: TDatasetColumn[] = [
  {
    name: "Product",
    value: "Product",
    type: "string",
    datasetId: "1373e827-bf2b-40f0-a6bf-b2ba855c7f86",
  },
  {
    name: "Price",
    value: "Price",
    type: "integer",
    datasetId: "1373e827-bf2b-40f0-a6bf-b2ba855c7f86",
  },
  {
    name: "Name",
    value: "Name",
    type: "string",
    datasetId: "1373e827-bf2b-40f0-a6bf-b2ba855c7f86",
  },
  {
    name: "Transaction_date",
    value: "Transaction_date",
    type: "timestamp",
    datasetId: "1373e827-bf2b-40f0-a6bf-b2ba855c7f86",
  },
];

export const getTabId = (url: any) => {
  let tabId = "";
  if (url.pathname.slice(-1) == "#") {
    tabId = url.pathname.slice(-37, -1);
  } else {
    tabId = url.pathname.slice(-36);
  }

  return tabId;
};
