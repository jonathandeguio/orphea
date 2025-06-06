import type { CheckboxChangeEvent } from "antd/es/checkbox";
import React from "react";
import { useDispatch, useSelector } from "react-redux";
import { Outlet, useLocation } from "react-router";
import { RootState, ThunkAppDispatch } from "redux/types/store";
import "./Home.scss";

function Home() {
  const location = useLocation();
  const dispatch = useDispatch<ThunkAppDispatch>();
  const lastSlashIndex = location.pathname.lastIndexOf("/");
  const trimmedString = location.pathname.slice(lastSlashIndex + 1);
  const remainingString = location.pathname.slice(0, lastSlashIndex);
  const secondLastWord = remainingString.slice(
    remainingString.lastIndexOf("/") + 1
  );
  const isConnectAdmin = useSelector(
    (state: RootState) => (state.connectAdmin as any).user
  );

  const onChange = (e: CheckboxChangeEvent) => {};

  return (
    <div className="portalHomePage-layout">
      <div className="portalHomePage-layout-outlet">
        <Outlet />
      </div>
      {/* <div className="portalHomePage-layout-information">iiii</div> */}
    </div>
  );
}

export default Home;
