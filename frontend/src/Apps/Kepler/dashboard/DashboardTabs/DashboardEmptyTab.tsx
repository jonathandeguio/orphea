import React from "react";
import { getLanguageLabel } from "utils/utilities";

const DashboardEmptyTab = () => {
  return (
    <div
      style={{
        display: "flex",
        justifyContent: "center",
        color: "#80808045",
        fontSize: "2rem",
        fontKerning: "normal",
        fontWeight: "600",
      }}
    >
      {getLanguageLabel("dropComponentsInTheBox")}
    </div>
  );
};

export default DashboardEmptyTab;
