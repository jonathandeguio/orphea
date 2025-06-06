import React from "react";
import { Outlet } from "react-router";
import "./HomeOutlet.module.scss";

const HomeOutlet = () => {
  return (
    <div className="portalHomePage-layout">
      <div className="portalHomePage-layout-outlet">
        <Outlet />
      </div>
    </div>
  );
};

export default HomeOutlet;
