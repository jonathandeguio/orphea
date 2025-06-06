import { IResourceFilters } from "Apps/ProjectsV2/interfaces/Project";
import { RESOURCE_FILTER_FIELDS } from "Apps/ProjectsV2/utils/Projects.utils";
import { isDefined } from "utils/utilities";

export const openFilterMenuByDefault = (filters: IResourceFilters) => {
  return !!filters.searchText || !!filters.permissions;
};

export const togglePermissionFilters = (
  filters: IResourceFilters,
  updateFilters: any,
  role: string
) => {
  if ((filters.permissions || []).find((permission) => permission == role))
    updateFilters(
      RESOURCE_FILTER_FIELDS.PERMISSIONS,
      (filters.permissions || []).filter((permission) => permission != role)
    );
  else
    updateFilters(RESOURCE_FILTER_FIELDS.PERMISSIONS, [
      ...(filters.permissions || []),
      role,
    ]);
};

export const getFilterMenuSelectedKeys = (
  permissions: IResourceFilters["permissions"]
) => {
  let keys: string[] = [];
  if (isDefined(permissions) && Array.isArray(permissions))
    for (let i = 0; i < (permissions as Array<string>)?.length || 0; i++)
      keys.push(permissions[i]);
  return keys;
};

export const doesPermissionFilterIncludeRole = (
  role: string,
  permissons: string[] | undefined
) => {
  if (!isDefined(permissons)) return false;
  return permissons.includes(role);
};
