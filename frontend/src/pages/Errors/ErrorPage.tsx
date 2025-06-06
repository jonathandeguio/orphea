import React, { FC } from "react";
import { getLanguageLabel } from "utils/utilities";
import ErrorComponent from "./ErrorComponent/ErrorComponent";

interface IProps {
  type?: string;
  className?: string;
  redirectionUrl?: string;
}

const ErrorPage: FC<IProps> = (props) => {
  return (
    <ErrorComponent
      lineStroke="orange"
      errorCode="410"
      errorHeading={getLanguageLabel("error")}
      errorMsg={getLanguageLabel("errorPageMsg")}
    />
  );
};

export default ErrorPage;
