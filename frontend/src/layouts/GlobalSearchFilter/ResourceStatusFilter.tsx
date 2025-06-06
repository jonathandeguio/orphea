import { Radio } from "antd";
import React, { useEffect, useState } from "react";
import { ResourceStatus } from "./HeaderSearch.types";
import { IDropdown } from "./HeaderSearch.types";

export const ResourceStatusFilter: React.FC<IDropdown> = ({
  state,
  onChange,
}) => {
  const [resourceStatus, setResourceStatus] = useState<ResourceStatus>(
    ResourceStatus.ACTIVE
  );
  const handleChange = (key: string, value: ResourceStatus) => {
    setResourceStatus(value);
    onChange(key, value);
  };
  useEffect(() => {
    setResourceStatus(state);
  }, [state]);
  return (
    <Radio.Group
      className="radio-btns-container"
      onChange={(e) => handleChange("sortOrder", e.target.value)}
      value={resourceStatus}
    >
      <Radio value={ResourceStatus.ACTIVE}>Active</Radio>
      <Radio value={ResourceStatus.IN_TRASH}>In Trash</Radio>
    </Radio.Group>
  );
};
