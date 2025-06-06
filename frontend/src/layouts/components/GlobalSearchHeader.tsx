import { Input, InputRef, Tooltip } from "antd";
import { RemoveIcon } from "assets/icons/boslerActionIcons";
import { ArchiveIcon, FolderIcon } from "assets/icons/boslerFileIcons";
import { CalendarIcon, UserIcon } from "assets/icons/boslerInterfaceIcons";
import { ArrowDownIcon, ArrowUpIcon } from "assets/icons/boslerNavigationIcon";
import { DateFilters } from "layouts/GlobalSearchFilter/DateFilters";
import FilterChip from "layouts/GlobalSearchFilter/GlobalSearchFilterchip";
import {
  IGlobalSearchHeader,
  ResourceStatus,
  SortOrder,
} from "layouts/GlobalSearchFilter/HeaderSearch.types";
import { ResourceStatusFilter } from "layouts/GlobalSearchFilter/ResourceStatusFilter";
import { ResourceTypeFilter } from "layouts/GlobalSearchFilter/ResourceTypeFilter";
import { SelectUserFilter } from "layouts/GlobalSearchFilter/SelectUserFilter";
import React, { useEffect, useRef } from "react";
import { getLanguageLabel, isEmpty } from "utils/utilities";
import {
  datefilterChageHandler,
  resetFilters,
  resourceTypeCheckedHandler,
  sortOrderChangeHandler,
  statusChangeHandler,
  textSearchChangeHandler,
  userCheckedHandler,
} from "./GlobalSearch.handlers";

const GlobalSearchHeader: React.FC<IGlobalSearchHeader> = (headerProps) => {
  const inputRef = useRef<InputRef>(null);
  const { globalSearchFilters, sortOrder } = headerProps;

  useEffect(() => {
    inputRef.current && inputRef.current?.focus();
    const handleKeyDown = (event: any) => {
      if (event.key === "/") {
        inputRef.current && inputRef.current.focus();
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => {
      window.removeEventListener("keydown", handleKeyDown);
    };
  }, []);

  return (
    <div className="header-search-filter-container">
      <Input
        variant="borderless"
        className="global-search-input"
        ref={inputRef}
        value={globalSearchFilters.searchText!}
        placeholder={getLanguageLabel("searchMsg")}
        onChange={(e) => textSearchChangeHandler(headerProps, e.target.value)}
      />
      <div className="global-search-filter-container">
        <FilterChip
          className={"active"}
          icon={<ArchiveIcon />}
          defaultName={getLanguageLabel("status")}
          myDropDownComponent={
            <ResourceStatusFilter
              state={globalSearchFilters.resourceStatus}
              onChange={(key, value) =>
                statusChangeHandler(headerProps, key, value)
              }
            />
          }
        />

        <FilterChip
          className={
            isEmpty(globalSearchFilters.createdBy) ? "inActive" : "active"
          }
          icon={<UserIcon />}
          defaultName={getLanguageLabel("createdBy")}
          myDropDownComponent={
            <SelectUserFilter
              state={globalSearchFilters.createdBy}
              onChange={(key, id) => userCheckedHandler(headerProps, key, id)}
            />
          }
        />

        <FilterChip
          className={
            isEmpty(globalSearchFilters.createdAtFrom) &&
            isEmpty(globalSearchFilters.createdAtTo) &&
            isEmpty(globalSearchFilters.updatedAtFrom) &&
            isEmpty(globalSearchFilters.updatedAtTo)
              ? "inActive"
              : "active"
          }
          icon={<CalendarIcon />}
          defaultName={"Epoch"}
          myDropDownComponent={
            <DateFilters
              state={{
                createdAtFrom: globalSearchFilters.createdAtFrom,
                createdAtTo: globalSearchFilters.createdAtTo,
                updatedAtFrom: globalSearchFilters.updatedAtFrom,
                updatedAtTo: globalSearchFilters.updatedAtTo,
              }}
              onChange={(key, e) => datefilterChageHandler(headerProps, key, e)}
            />
          }
        />

        <FilterChip
          className={
            isEmpty(globalSearchFilters?.resourceType) ? "inActive" : "active"
          }
          icon={<FolderIcon />}
          defaultName="ResourceType"
          myDropDownComponent={
            <ResourceTypeFilter
              state={globalSearchFilters.resourceType}
              onChange={(key, value) =>
                resourceTypeCheckedHandler(headerProps, key, value)
              }
            />
          }
        />
        <div
          onClick={() => sortOrderChangeHandler(headerProps)}
          className={` show-differentiator-on-hover chip-container ${
            sortOrder !== SortOrder.DESCENDING ? "active" : "inActive"
          }`}
        >
          {sortOrder === SortOrder.ASCENDING ? (
            <ArrowUpIcon />
          ) : (
            <ArrowDownIcon />
          )}
        </div>
        <Tooltip title={getLanguageLabel("reset")}>
          <div
            className=" show-differentiator-on-hover chip-container"
            onClick={() => resetFilters(headerProps)}
          >
            <RemoveIcon color="red" />
          </div>
        </Tooltip>
      </div>
    </div>
  );
};

export default GlobalSearchHeader;
