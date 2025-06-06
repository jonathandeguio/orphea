import React, { useState } from "react";
import { Popover, Tooltip } from "antd";
import { SingleChevronDownIcon } from "assets/icons/boslerNavigationIcon";
import { IFilterChip } from "./HeaderSearch.types";
import { BoslerTypography } from "components/CommonUI/BoslerTypography";
import "./GlobalFilters.scss";
const FilterChip: React.FC<IFilterChip> = ({
  icon,
  className,
  defaultName = "Search by",
  name,
  myDropDownComponent,
}) => {
  return (
    <Popover
      className="filter-chip-popover"
      placement="bottom"
      trigger="hover"
      overlayClassName="filterchip-popover"
      content={myDropDownComponent}
    >
      {/* <Tooltip title={name || defaultName}> */}
      <div
        className={`chip-container show-differentiator-on-hover ${className}`}
      >
        {icon}
      </div>
      {/* </Tooltip> */}
    </Popover>
  );
};

export default FilterChip;
