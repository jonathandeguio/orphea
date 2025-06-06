import { Layout } from "antd";
import React, { useEffect, useState } from "react";
import { Outlet, useLocation, useNavigate } from "react-router-dom";

import { Content } from "antd/es/layout/layout";
import FloatingBanner from "components/FloatingBanner/FloatingBanner.view";
import { useDispatch, useSelector } from "react-redux";
import { RootState, ThunkAppDispatch } from "../redux/types/store";
import Sidebar from "./Sidebar";
import { LayoutViewEnum } from "./Sidebar/Sidebar.utils";

const MainLayout = () => {
  const { user: platformAdmin } = useSelector(
    (state: RootState) => (state as any).platformAdmin
  );
  const { user } = useSelector((state: RootState) => state.userDetails);
  const [sidebarWidth, setSidebarWidth] = useState("37px");
  const { config } = useSelector((state) => (state as any).platformConfig);
  const location = useLocation();
  const navigate = useNavigate();
  const dispatch = useDispatch<ThunkAppDispatch>();
  useEffect(() => {
    if (user.preferences.layoutView == LayoutViewEnum.COMFORTABLE) {
      setSidebarWidth("59px");
    } else if (user.preferences.layoutView == LayoutViewEnum.COMPACT) {
      setSidebarWidth("37px");
    }
  }, [user.preferences]);
  useEffect(() => {
    console.log("hello");
  }, []);
  useEffect(() => {
    if (platformAdmin === false) {
      if (
        location.pathname !== "/portal/home" &&
        config.mfaEnforced === true &&
        (user.isMfaEnabled === false || user.isMfaEnabled === null)
      ) {
        navigate("/portal/home");
      }
      if (
        user.isMfaSkipped === true &&
        (user.isMfaEnabled === false || user.isMfaEnabled === null) &&
        config.mfaEnabled === true
      ) {
        const mfaSkippedDate = new Date(user.mfaSkippedAt);
        const sevenDaysAgo = new Date();
        sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);

        if (mfaSkippedDate < sevenDaysAgo) {
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
        <FloatingBanner />

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
