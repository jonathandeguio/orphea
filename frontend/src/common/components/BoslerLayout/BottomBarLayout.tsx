import React from "react";
import { BoslerBottomBar } from "./BoslerBottomBar";
import "./bottomBarLayout.scss";

export interface IBottomBarLayoutBody {}

export interface IBottomBarLayout {
  children: JSX.Element;
}

export const BottomBarLayout: React.FC<{ children: JSX.Element }> = ({
  children,
}) => {
  return <BoslerBottomBar children={children} />;
};
