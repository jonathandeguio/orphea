import Forbidden from "pages/Errors/Forbidden";
import React from "react";
import { useSelector } from "react-redux";
import { Outlet } from "react-router";
import { RootState } from "redux/types/store";

export const PlatformOutlet = () => {
  const { user: platformAdmin } = useSelector(
    (state: RootState) => (state as any).platformAdmin
  );

  if (platformAdmin === undefined) return <></>;
  else if (platformAdmin) return <Outlet />;
  else {
    return <Forbidden id={"403"} />;
  }
};
