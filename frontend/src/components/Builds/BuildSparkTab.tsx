import { BASE_URL } from "Authentication/constants";
import React from "react";

interface TProps {
  sparkApplicationId: string;
}
const BuildSparkTab = ({ sparkApplicationId }: TProps) => {
  return (
    <div
      style={{
        whiteSpace: "pre-wrap",
        width: "100%",
        height: "76vh",
      }}
    >
      <iframe
        src={`${BASE_URL}/bosler-shs/history/${sparkApplicationId}/jobs/`}
        width="100%"
        height="100%"
      ></iframe>
    </div>
  );
};

export default BuildSparkTab;
