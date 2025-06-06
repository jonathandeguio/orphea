import React, { useEffect, useState } from "react";
import "./ExampleComponent.scss";

import { getLanguageLabel } from "utils/utilities";
import { getData } from "./api";

// Keep the types here, if needed these types can be shifted to another file
interface props {}

// Shift all the logical treatment to _{COMPONENT}.services.ts file of the component

const ExampleComponent: React.FC<props> = ({}: props) => {
  const [data, setData] = useState();
  useEffect(() => {
    getData().then((res: any) => setData(res));
  }, []);

  // Everything which can be a constant keep it in the _constants file of there respective component
  return <div>{getLanguageLabel("dataset")}</div>;
};

export default ExampleComponent;
