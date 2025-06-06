import { REQUEST_ACCESS_TYPE } from "../RequestAccessModal/RequestAccessModal.utils";

export const getRequestTargetLinkBasedOnType = (id: string, type: string) => {
  switch (type) {
    case REQUEST_ACCESS_TYPE.ADMINISTRATOR:
      return `/portal/settings/groups/${id}/manageGroup`;
    case REQUEST_ACCESS_TYPE.PROJECT:
      return `/portal/kitab/folder/${id}`;
    default:
      return "";
  }
};
