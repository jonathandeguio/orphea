import React from "react";
import { getLanguageLabel } from "utils/utilities";
import ErrorComponent from "./ErrorComponent/ErrorComponent";

const NotFound = () => {
  return (
    <ErrorComponent
      lineStroke="red"
      errorCode="404"
      errorHeading={getLanguageLabel("errorNotFound")}
      errorMsg={getLanguageLabel("lostMsg")}
    />
  );
};

export default NotFound;
