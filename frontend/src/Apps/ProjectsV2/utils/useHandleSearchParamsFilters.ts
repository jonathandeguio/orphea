import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import { encodeToBase64, decodeFromBase64 } from "utils/utilities";
import { getDefaultProjectFilters } from "./Projects.utils";
import { IResourceFilters } from "../interfaces/Project";

export const useHandleSearchParamsFilters = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  const getInitialFilters = () => {
    if (searchParams.has("filters")) {
      const base64Filters = searchParams.get("filters");
      return JSON.parse(decodeFromBase64(base64Filters as string));
    }
    return getDefaultProjectFilters();
  };

  const [filters, setFilters] = useState<IResourceFilters>(getInitialFilters());

  const addFiltersInUrl = (filters: IResourceFilters) => {
    const base64Filters = encodeToBase64(JSON.stringify(filters));
    setSearchParams({ filters: base64Filters });
  };

  useEffect(() => {
    addFiltersInUrl(filters);
  }, [filters]);

  return { filters, setFilters };
};
