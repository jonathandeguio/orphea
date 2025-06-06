import {
  MODE_UPDATED_SUCCESS,
  PERMISISON_UPDATED_SUCCESS
} from "../../redux/constants/resourcePermissionConstants";
export type TAllowedPermission = "OWNER" | "EDITOR" | "VIEWER" | "NONE";
export type TAllowedModes =
  | "VIEW"
  | "EDIT"
  | "VERSION"
  | "EMBEDDED"
  | "DATASETHISTORY";
export const resourcePermissionUpdate = (
  permission: TAllowedPermission,
  resourceId: string
) => {
  return {
    type: PERMISISON_UPDATED_SUCCESS,
    resourceId: resourceId,
    permission: permission,
  };
};

export const resourceModeUpdate = (mode: TAllowedModes, resourceId: string) => {
  return {
    type: MODE_UPDATED_SUCCESS,
    resourceId: resourceId,
    mode: mode,
  };
};
