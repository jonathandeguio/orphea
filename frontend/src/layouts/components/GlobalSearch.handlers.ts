import { ResourceTypeEnumForGlobalsearch } from "layouts/GlobalSearchFilter/ResourceTypeFilter";
import { CGlobalFilters } from "./GlobalSearch.constants";
import {
  IGlobalResourceSearchFilters,
  IGlobalSearchHeader,
  ResourceStatus,
  SortOrder,
} from "layouts/GlobalSearchFilter/HeaderSearch.types";

export const sortOrderChangeHandler = (headerProps: IGlobalSearchHeader) => {
  headerProps.setPage(0);
  headerProps.setHasMoreDataToShow(true);
  headerProps.setSortOrder(
    headerProps.sortOrder === SortOrder.DESCENDING
      ? SortOrder.ASCENDING
      : SortOrder.DESCENDING
  );
};

export const resetFilters = (headerProps: IGlobalSearchHeader) => {
  headerProps.setPage(0);
  headerProps.setGlobalSearchFilters(CGlobalFilters);
  headerProps.setResources([]);
  headerProps.setTotalPage(null);
  headerProps.setHasMoreDataToShow(true);
  headerProps.setSortOrder(SortOrder.DESCENDING);
};

export const textSearchChangeHandler = (
  headerProps: IGlobalSearchHeader,
  value: string
) => {
  headerProps.setPage(0);
  headerProps.setHasMoreDataToShow(true);
  headerProps.setGlobalSearchFilters({
    ...headerProps.globalSearchFilters,
    searchText: value,
  });
};

export const statusChangeHandler = (
  headerProps: IGlobalSearchHeader,
  key: string,
  value: string
) => {
  headerProps.setPage(0);
  headerProps.setHasMoreDataToShow(true);
  if (value === ResourceStatus.ACTIVE) {
    headerProps.setGlobalSearchFilters({
      ...headerProps.globalSearchFilters,
      resourceStatus: ResourceStatus.ACTIVE,
    });
  } else {
    headerProps.setGlobalSearchFilters({
      ...headerProps.globalSearchFilters,
      resourceStatus: ResourceStatus.IN_TRASH,
    });
  }
};

export const userCheckedHandler = (
  headerProps: IGlobalSearchHeader,
  key: string,
  id: string
) => {
  headerProps.setPage(0);
  headerProps.setHasMoreDataToShow(true);
  const userId = id;
  const updatedCreatedBy = headerProps.globalSearchFilters?.createdBy?.includes(
    userId
  )
    ? headerProps.globalSearchFilters.createdBy?.filter(
        (id: string) => id !== userId
      )
    : [...headerProps.globalSearchFilters.createdBy!, userId];

  headerProps.setGlobalSearchFilters({
    ...headerProps.globalSearchFilters,
    createdBy: updatedCreatedBy,
  });
};

export const datefilterChageHandler = (
  headerProps: IGlobalSearchHeader,
  key: string,
  e: string
) => {
  headerProps.setPage(0);
  headerProps.setHasMoreDataToShow(true);
  headerProps.setGlobalSearchFilters((prev: any) => ({
    ...prev,
    [key]: e || null,
  }));
};

export const resourceTypeCheckedHandler = (
  headerProps: IGlobalSearchHeader,
  key: string,
  resourceType: any
) => {
  headerProps.setIsLoading(true);
  headerProps.setHasMoreDataToShow(true);
  headerProps.setPage(0);
  if (
    headerProps.globalSearchFilters.resourceType?.length == 1 &&
    headerProps.globalSearchFilters.resourceType[0] === resourceType
  ) {
    headerProps.setGlobalSearchFilters({
      ...headerProps.globalSearchFilters,
      resourceType: null,
    });
  } else if (headerProps.globalSearchFilters.resourceType !== null) {
    if (headerProps.globalSearchFilters.resourceType?.includes(resourceType)) {
      const newResourceTypeArray =
        headerProps.globalSearchFilters.resourceType.filter(
          (e: ResourceTypeEnumForGlobalsearch | string) => e != resourceType
        );
      headerProps.setGlobalSearchFilters({
        ...headerProps.globalSearchFilters,
        resourceType: newResourceTypeArray,
      });
    } else {
      const newResourceTypeArray = [
        ...headerProps.globalSearchFilters.resourceType,
        resourceType,
      ];
      headerProps.setGlobalSearchFilters({
        ...headerProps.globalSearchFilters,
        resourceType: newResourceTypeArray,
      });
    }
  } else {
    headerProps.setGlobalSearchFilters((prev: IGlobalResourceSearchFilters) => {
      return {
        ...prev,
        resourceType: [resourceType],
      };
    });
  }
};
