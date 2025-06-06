import { ResourceTypeEnum } from "Apps/explorer/explorer.utils";
import { IResource, User } from "global";
import React from "react";
export interface IGlobalResourceSearchFilters {
  searchText: string | null;
  resourceStatus: ResourceStatus;
  resourceType: ResourceTypeEnum[] | string[] | null;
  createdAtTo: Date | null;
  createdAtFrom: Date | null;
  updatedAtTo: Date | null;
  updatedAtFrom: Date | null;
  createdBy: string[] | null;
}

export enum ResourceStatus {
  ACTIVE = "ACTIVE",
  IN_TRASH = "IN_TRASH",
}

export enum SortOrder {
  ASCENDING = "asc",
  DESCENDING = "dsc",
}

export interface IFilterChip {
  icon: React.ReactNode;
  defaultName?: string;
  className?: string;
  name?: string | null;
  myDropDownComponent: React.ReactNode;
}
export interface IDropdown {
  state?: any;
  onChange: (
    key: string,
    value: string | ResourceTypeEnum
  ) => void | ((key: boolean, value: string | ResourceTypeEnum) => void);
}

export interface IResourceList {
  allResources: IResource[];
  isLoading: boolean;
  hasMoreDataToShow: boolean;
  setPage: React.Dispatch<React.SetStateAction<number>>;
  setResources: React.Dispatch<React.SetStateAction<IResource[]>>;
  setIsHeaderSearchModalOpen: React.Dispatch<React.SetStateAction<boolean>>;
  allusers: User[];
  totalpage: number | null;
}

export interface IApplications {
  id: string;
  value: string;
  name: string;
  label: JSX.Element;
}

export interface IResourceCard {
  resource: IResource;
  lastResourceElementRef: React.Ref<HTMLDivElement> | null;
  handleResourceClick: (id: string) => void;
  setIsHeaderSearchModalOpen: React.Dispatch<React.SetStateAction<boolean>>;
  allusers: User[];
}

export interface IGlobalSearchHeader {
  globalSearchFilters: IGlobalResourceSearchFilters;
  setGlobalSearchFilters: React.Dispatch<
    React.SetStateAction<IGlobalResourceSearchFilters>
  >;
  sortOrder: SortOrder;
  setSortOrder: React.Dispatch<React.SetStateAction<SortOrder>>;
  setPage: React.Dispatch<React.SetStateAction<number>>;
  setHasMoreDataToShow: React.Dispatch<React.SetStateAction<boolean>>;
  setIsLoading: React.Dispatch<React.SetStateAction<boolean>>;
  setResources: React.Dispatch<React.SetStateAction<IResource[]>>;
  setTotalPage: React.Dispatch<React.SetStateAction<number | null>>;
}

export interface IHeaderSearchFooter {
  applications: any[];
  setIsHeaderSearchModalOpen: (val: boolean) => void;
}
export interface IApplicationChip {
  name: string;
  onClick: () => void;
}
