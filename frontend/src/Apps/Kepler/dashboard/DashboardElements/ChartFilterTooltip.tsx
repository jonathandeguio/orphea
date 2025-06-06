import React from "react";
import { FILTER_TOOLTIP_HEADING } from "../Dashboard.contants";

const ChartFilterTooltip = ({ filters }: { filters: any[] }) => {
  return (
    <div>
      <div> {FILTER_TOOLTIP_HEADING} </div>
      <>
        {filters.map((filter: any) => {
          if (
            filter.filter.filterType == "classic" &&
            (filter.filter.operator == "doesNotExist" ||
              filter.filter.operator == "exists")
          ) {
            return (
              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  gap: "2rem",
                }}
              >
                <div>{filter.filter.name}</div>
                <div style={{ color: "var(--SUCCESS_COLOR)" }}>
                  {filter.filter.operator}
                </div>
              </div>
            );
          } else if (filter.filter.filterType == "classic") {
            return (
              <div>
                <div
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    gap: "2rem",
                  }}
                >
                  <div>{filter.filter.name}</div>
                  <div style={{ color: "var(--SUCCESS_COLOR)" }}>
                    {filter.filter.operator}
                  </div>
                </div>
                <div style={{ color: "var(--ACTION_COLOR)" }}>
                  {" => "}
                  {filter.filterValue}
                </div>
              </div>
            );
          }
          return (
            <div>
              <div>{filter.filter.name}</div>
              <div style={{ color: "var(--ACTION_COLOR)" }}>
                {" => "}
                {filter.filterValue}
              </div>
            </div>
          );
        })}
      </>
    </div>
  );
};

export default ChartFilterTooltip;
