import React from "react";
import { CustomGridItemComponent } from "./CustomGridItemComponent";
import HeaderElement from "./HeaderElement";
export const CustomComp = () => {
  return (
    <CustomGridItemComponent key="123124123324">
      <HeaderElement layout={{ i: "sadasdasd" }} />
    </CustomGridItemComponent>
  );
};
