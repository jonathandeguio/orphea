import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import { encodeToBase64, decodeFromBase64 } from "utils/utilities";
import { IAccessManagerFilters } from "../AccessManager";
import { getDefaultAccessManagerFilters } from "../AccessManager.utils";
import { IResourceFilters } from "Apps/ProjectsV2/interfaces/Project";
import { getDefaultProjectFilters } from "Apps/ProjectsV2/utils/Projects.utils";

export type IFilters = IAccessManagerFilters | IResourceFilters;

export const FILTER_TYPES = {
  ACCESS_MANAGER: "ACCESS_MANAGER",
  RESOURCE: "RESOURCE",
};

export const useHandleSearchParamsFilters = (filterType: string) => {
  const [searchParams, setSearchParams] = useSearchParams();

  const getInitialFilters = () => {
    if (searchParams.has("filters")) {
      const base64Filters = searchParams.get("filters");
      return JSON.parse(decodeFromBase64(base64Filters as string));
    }
    switch (filterType) {
      case FILTER_TYPES.ACCESS_MANAGER:
        return getDefaultAccessManagerFilters();
      case FILTER_TYPES.RESOURCE:
        return getDefaultProjectFilters();
      default:
        return {};
    }
  };

  const [filters, setFilters] = useState<IFilters>(getInitialFilters());

  const addFiltersInUrl = (filters: IFilters) => {
    const base64Filters = encodeToBase64(JSON.stringify(filters));
    setSearchParams({ filters: base64Filters });
  };

  useEffect(() => {
    addFiltersInUrl(filters);
  }, [filters]);

  return { filters, setFilters };
};
