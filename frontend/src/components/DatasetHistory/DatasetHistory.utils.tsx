import { TIntent } from "./DatasetHistory.types";

export const getIntent = (result: TIntent) => {
  if (result == "success") {
    return "success";
  } else if (result == "default") {
    return "action";
  } else {
    return "dangerous";
  }
};
