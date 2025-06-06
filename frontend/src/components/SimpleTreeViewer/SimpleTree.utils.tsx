import { Skeleton } from "antd";
import React from "react";

export const SimpleTreeChildrenLoader = () => {
  return (
    <>
      <div className="--flex-row-start --flex-gap10 --mt5  --ml30">
        <Skeleton.Button size={"small"} shape={"circle"} />
        <Skeleton.Input
          size={"small"}
          style={{
            height: "16px",
          }}
        />
      </div>
      <div className="--flex-row-start --flex-gap10 --mt5 --mb5 --ml30">
        <Skeleton.Button size={"small"} shape={"circle"} />
        <Skeleton.Input
          size={"small"}
          style={{
            height: "16px",
          }}
        />
      </div>
    </>
  );
};
