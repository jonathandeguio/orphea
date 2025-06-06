import {
  TAllowedModes,
  TAllowedPermission,
} from "../actions/resourcePermissionActions";
export const PERMISISON_UPDATED_SUCCESS = "PERMISSION_UPDATED_SUCCESS";
export const MODE_UPDATED_SUCCESS = "MODE_UPDATED_SUCCESS";

export const VIEWER_MODE = "VIEW";
export const VERSION_MODE = "VERSION";
export const DATASET_HISTORY_MODE = "DATASETHISTORY";
export const EDIT_MODE = "EDIT";
export const EMBEDDED_MODE = "EMBEDDED";

export const DEFAULT_MODE: TAllowedModes = "VIEW";

export const VIEWER_PERMISSION = "VIEWER";
export const EDITOR_PERMISSION = "EDITOR";
export const OWNER_PERMISSION = "OWNER";
export const NONE_PERMISSION = "NONE";
export const DEFAULT_PERMISISON: TAllowedPermission = "NONE";
