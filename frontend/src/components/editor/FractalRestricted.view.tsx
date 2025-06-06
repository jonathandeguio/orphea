import ErrorComponent from "pages/Errors/ErrorComponent/ErrorComponent";
import React from "react";

export const FractalRestricted = () => {
  return (
    <ErrorComponent
      lineStroke="orange"
      errorCode="402"
      errorHeading={"LOOKS LIKE YOUR PRODUCT TYPE DOESNOT SUPPORT FRACTAL"}
      errorMsg={`CONTACT PLATFORM ADMIN TO UPDATE LICENSE TO USE THIS FEATURE`}
    />
  );
};
