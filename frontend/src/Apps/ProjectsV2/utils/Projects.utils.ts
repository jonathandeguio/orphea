import { ROLES_TYPES } from "Apps/AccessManager/RequestAccessModal/RequestAccessModal.utils";
import { IResourceFilters } from "../interfaces/Project";
import { ObjectKeys } from "utils/utilities";

export const getDefaultProjectFilters = () => {
  return {
    searchText: undefined,
    sortBy: RESOURCE_SORT_BY_TYPE.NAME,
    sortDirection: RESOURCE_SORT_DIRECTION.ASC,
    permissions: ObjectKeys(ROLES_TYPES),
  } as unknown as IResourceFilters;
};

export const RESOURCE_FILTER_FIELDS = {
  SEARCH_TEXT: "searchText",
  SORT_BY: "sortBy",
  SORT_DIRECTION: "sortDirection",
  PERMISSIONS: "permissions",
};

export const FORM_FIELDS = {
  NAME: "name",
  DESCRIPTION: "description",
};

export const RESOURCE_SORT_BY_TYPE = {
  CREATED_AT: "createdAt",
  NAME: "name",
};

export const RESOURCE_SORT_DIRECTION = {
  ASC: "asc",
  DESC: "dsc",
};

export type TRESOURCE_SORT_DIRECTION = keyof typeof RESOURCE_SORT_DIRECTION;
