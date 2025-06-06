import React from "react";
import { getLanguageLabel } from "utils/utilities";
import ErrorComponent from "./ErrorComponent/ErrorComponent";

const Forbidden = (props: { id: string }) => {
  return (
    <ErrorComponent
      lineStroke="orange"
      errorCode="403"
      errorHeading={getLanguageLabel("accessDenied")}
      errorMsg={`${getLanguageLabel("id")}: ${props.id}`}
    />
  );
};

export default Forbidden;
