import { Radio } from "antd";
import React, { useEffect, useState } from "react";
import { SortOrder } from "./HeaderSearch.types";
import { IDropdown } from "./HeaderSearch.types";
import { getLanguageLabel } from "utils/utilities";

export const SelectSortOrder: React.FC<IDropdown> = ({ state, onChange }) => {
  const [sortOrder, setSortOrder] = useState<SortOrder>(SortOrder.ASCENDING);

  const handleChange = (key: string, value: SortOrder) => {
    setSortOrder(value);
    onChange(key, value);
  };
  useEffect(() => {
    setSortOrder(state);
  }, [state]);
  return (
    <Radio.Group
      className="radio-btns-container"
      // defaultValue={SortOrder.ASCENDING}
      onChange={(e) => handleChange("sortOrder", e.target.value)}
      value={sortOrder}
    >
      <Radio value={SortOrder.ASCENDING}>{getLanguageLabel("ascending")}</Radio>
      <Radio value={SortOrder.DESCENDING}>
        {getLanguageLabel("descending")}
      </Radio>
    </Radio.Group>
  );
};
