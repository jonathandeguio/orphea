import { Checkbox } from "antd";
import React, { useEffect, useState } from "react";
import { IDropdown } from "./HeaderSearch.types";
import BoslerInput from "components/BoslerComponents/InputComponent/BoslerInput";
import { getLanguageLabel } from "utils/utilities";
export enum ResourceTypeEnumForGlobalsearch {
  CHART = "CHART",
  NOCODE = "NOCODE",
  REPOSITORY = "REPOSITORY",
  DASHBOARD = "DASHBOARD",
  SOURCE = "SOURCE",
  LINK = "LINK",
  DATASET = "DATASET",
}

export const ResourceTypeFilter: React.FC<IDropdown> = ({
  state,
  onChange,
}) => {
  const [checkedResources, setCheckedResources] = useState<string[] | null>(
    state
  );
  const [resourceTypeArr, setresourceTypeArr] = useState(
    Object.values(ResourceTypeEnumForGlobalsearch)
  );
  const handleChange = (
    key: boolean,
    resourceType: ResourceTypeEnumForGlobalsearch | string
  ) => {
    if (key === true) {
      setCheckedResources((prev) => {
        return prev ? [...prev, resourceType] : [resourceType];
      });
    } else {
      setCheckedResources((prev) => {
        return prev?.filter((e) => e !== resourceType) || [];
      });
    }
    onChange("resourceType", resourceType);
  };
  useEffect(() => {
    setCheckedResources(state);
  }, [state]);

  return (
    <>
      {resourceTypeArr.map((resourceType) => (
        <div>
          <Checkbox
            onChange={(e) => handleChange(e.target.checked, resourceType)}
            checked={checkedResources?.includes(resourceType)}
            value={resourceType}
          >
            {resourceType}
          </Checkbox>
        </div>
      ))}
    </>
  );
};
