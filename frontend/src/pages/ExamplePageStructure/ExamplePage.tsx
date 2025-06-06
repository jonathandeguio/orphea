import React from "react";
import { getLanguageLabel } from "utils/utilities";
import "./ExamplePage.scss";
interface props {}
const ExamplePage: React.FC<props> = ({}: props) => {
  return <div>{getLanguageLabel("dark")}</div>;
};
export default ExamplePage;
