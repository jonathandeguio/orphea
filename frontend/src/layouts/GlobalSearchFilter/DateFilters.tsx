import { DatePicker } from "antd";
import React, { useEffect, useState } from "react";
import dayjs, { Dayjs } from "dayjs";
import { IDropdown } from "./HeaderSearch.types";
import { TimeRangePickerProps } from "antd/lib";

export const DateFilters: React.FC<IDropdown> = ({ state, onChange }) => {
  const [createdAtFrom, setCreatedAtFrom] = useState<string | null>(null);
  const [createdAtTo, setCreatedAtTo] = useState<string | null>(null);
  const [updatedAtFrom, setUpdatedAtFrom] = useState<string | null>(null);
  const [updatedAtTo, setUpdatedAtTo] = useState<string | null>(null);

  useEffect(() => {
    if (state.createdAtFrom === null) {
      setCreatedAtFrom(null);
    }
    if (state.createdAtTo === null) {
      setCreatedAtTo(null);
    }
    if (state.updatedAtFrom === null) {
      setUpdatedAtFrom(null);
    }
    if (state.updatedAtTo === null) {
      setUpdatedAtTo(null);
    }
  }, [state]);

  const handleChange = (key: string, value: (string | null)[]) => {
    if (key === "createdAt" && value) {
      setCreatedAtFrom(value[0]);
      setCreatedAtTo(value[1]);
      onChange("createdAtFrom", value[0]!);
      onChange("createdAtTo", value[1]!);
    }

    if (key === "updatedAt" && value) {
      setUpdatedAtFrom(value[0]);
      setUpdatedAtTo(value[1]);
      onChange("updatedAtFrom", value[0]!);
      onChange("updatedAtTo", value[1]!);
    }
  };
  return (
    <>
      <div className="date-dropdown-comtainer">
        <div>
          <span className="--wd-20">Created</span>
        </div>
        <DatePicker.RangePicker
          allowEmpty={[true, true]}
          onChange={(dates) => {
            const rangeArr: (string | null)[] = dates
              ? [
                  dates[0]?.toISOString() ?? null,
                  dates[1]?.toISOString() ?? null,
                ]
              : [null, null];
            handleChange("createdAt", rangeArr);
          }}
          placeholder={["from", "to"]}
        />
        <div>
          <span className="--wd-20">Updated</span>
        </div>
        <DatePicker.RangePicker
          allowEmpty={[true, true]}
          onChange={(dates) => {
            const rangeArr: (string | null)[] = dates
              ? [
                  dates[0]?.toISOString() ?? null,
                  dates[1]?.toISOString() ?? null,
                ]
              : [null, null];
            handleChange("updatedAt", rangeArr);
          }}
          placeholder={["from", "to"]}
        />
      </div>
    </>
  );
};
