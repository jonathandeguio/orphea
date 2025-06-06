import { Layout } from "antd";
import React, { useEffect, useState } from "react";
import { Outlet, useNavigate } from "react-router-dom";

import { Content } from "antd/es/layout/layout";

import { useSelector } from "react-redux";
import { RootState } from "../redux/types/store";
import Sidebar from "./Sidebar";
import { LayoutViewEnum } from "./Sidebar/Sidebar.utils";

const MainLayout = () => {
  const { user } = useSelector((state: RootState) => state.userDetails);
  const [sidebarWidth, setSidebarWidth] = useState("37px");
  const { config, loading } = useSelector(
    (state) => (state as any).platformConfig
  );
  const { user: platformAdmin } = useSelector(
    (state) => (state as any).platformAdmin
  );
  const navigate = useNavigate();
  useEffect(() => {
    if (user.preferences.layoutView == LayoutViewEnum.COMFORTABLE) {
      setSidebarWidth("59px");
    } else if (user.preferences.layoutView == LayoutViewEnum.COMPACT) {
      setSidebarWidth("37px");
    }
  }, [user.preferences]);
  useEffect(() => {
    if (platformAdmin === false) {
      console.log("here");

      if (
        location.pathname !== "/portal/home" &&
        config.mfaEnforced === true &&
        (user.isMfaEnabled === false || user.isMfaEnabled === null)
      ) {
        console.log("here1");
        navigate("/portal/home");
      }
      if (
        user.isMfaSkipped === true &&
        (user.isMfaEnabled === false || user.isMfaEnabled === null) &&
        config.mfaEnabled === true
      ) {
        console.log("here2");

        const mfaSkippedDate = new Date(user.mfaSkippedAt);
        const sevenDaysAgo = new Date();
        sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);

        if (mfaSkippedDate < sevenDaysAgo) {
          console.log("here3");
          navigate("/portal/home");
        }
      }
    }
  }, [location.pathname, platformAdmin, config, user]);
  return (
    <Layout
      style={{
        height: "100vh",
        width: "100vw",
        display: "flex",
        flexDirection: "row",
      }}
    >
      <Sidebar />
      <Layout>
        {/* <MainHeader /> */}
        <Content style={{ overflow: "auto" }}>
          <div
            style={{
              height: "100%",
              width: "100%",
              maxWidth: `calc(100vw - ${sidebarWidth})`,
              // maxWidth: "100%",
              // overflow: "auto",
            }}
          >
            <Outlet />
          </div>
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;
