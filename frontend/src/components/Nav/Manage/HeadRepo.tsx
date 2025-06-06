import React from "react";
import { getLanguageLabel } from "utils/utilities";

const HeadRepo = () => {
  return (
    <div style={{ width: "100%" }}>
      <h3>{getLanguageLabel("repository")}</h3>
    </div>
  );
};

export default HeadRepo;
